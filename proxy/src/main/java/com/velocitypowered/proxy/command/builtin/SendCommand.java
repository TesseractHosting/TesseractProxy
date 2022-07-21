/*
 * Copyright (C) 2018 Velocity Contributors & TropicalShadow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.command.builtin;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;

public class SendCommand implements SimpleCommand {
  public static final int MAX_SERVERS_TO_LIST = 50;
  private final ProxyServer server;

  public SendCommand(ProxyServer server) {
    this.server = server;
  }

  @Override
  public void execute(Invocation invocation) {
    CommandSource source = invocation.source();
    String[] args = invocation.arguments();
    //Player player = (Player) source;
    if (args.length >= 2) {
      String playerNameOrAll = args[0];
      Optional<Player> selectedPlayer = this.server.getPlayer(playerNameOrAll);
      boolean all = playerNameOrAll.equalsIgnoreCase("all");
      String serverName = args[1];
      Optional<RegisteredServer> toConnect = this.server.getServer(serverName);
      List<Player> selected;
      if (selectedPlayer.isPresent()) {
        selected = new ArrayList();
        selected.add(selectedPlayer.get());
      } else {
        if (!all) {
          source.sendMessage(
                  Identity.nil(),
                  CommandMessages.UNKNOWN_PLAYER.args(Component.text(playerNameOrAll)));
          return;
        }

        selected = new ArrayList<>(this.server.getAllPlayers());
      }

      if (toConnect.isEmpty()) {
        source.sendMessage(
                Identity.nil(),
                CommandMessages.SERVER_DOES_NOT_EXIST.args(Component.text(serverName)));
        return;
      }

      selected.forEach((it) -> {
        it.createConnectionRequest((RegisteredServer) toConnect.get()).fireAndForget();
        it.sendMessage(Component.translatable("velocity.command.send-sent")
                .args(Component.text(toConnect.get().getServerInfo().getName())));
      });
    } else {
      this.outputServerInformation(source);
    }

  }

  private void outputServerInformation(CommandSource executor) {
    if(executor instanceof Player){
      String currentServer = ((Player)executor).getCurrentServer()
              .map(ServerConnection::getServerInfo)
              .map(ServerInfo::getName)
              .orElse("<unknown>");
      executor.sendMessage(
              Identity.nil(),
              Component.translatable(
                      "velocity.command.server-current-server",
                      NamedTextColor.YELLOW,
                      Component.text(currentServer)));
    }
    List<RegisteredServer> servers = BuiltinCommandUtil.sortedServerList(this.server);
    if (servers.size() > MAX_SERVERS_TO_LIST) {
      executor.sendMessage(
              Identity.nil(),
              Component.translatable("velocity.command.server-too-many", NamedTextColor.RED));
      return;
    }
    // Assemble the list of servers as components
    TextComponent.Builder serverListBuilder = Component.text()
            .append(Component.translatable("velocity.command.server-available",
                    NamedTextColor.YELLOW))
            .append(Component.space());
    for (int i = 0; i < servers.size(); i++) {
      RegisteredServer rs = servers.get(i);
      serverListBuilder.append(Component.text(rs.getServerInfo().getName()));
      if (i != servers.size() - 1) {
        serverListBuilder.append(Component.text(", ", NamedTextColor.GRAY));
      }
    }

    executor.sendMessage(Identity.nil(), serverListBuilder.build());
  }


  @Override
  public List<String> suggest(Invocation invocation) {
    String[] currentArgs = (String[]) invocation.arguments();
    List<String> playerList = new ArrayList(this.server.getAllPlayers().stream().map((player) -> {
      return player.getGameProfile().getName();
    }).toList());
    playerList.add("all");
    Stream<String> possibilities = playerList.stream();
    Stream<String> possibleServers = this.server.getAllServers().stream().map((rs) -> {
      return rs.getServerInfo().getName();
    });
    if (currentArgs.length == 0) {
      return possibilities.collect(Collectors.toList());
    } else if (currentArgs.length == 1) {
      return (List) possibilities.filter((name) -> {
        return name.regionMatches(true, 0, currentArgs[0], 0, currentArgs[0].length());
      }).collect(Collectors.toList());
    } else {
      return (List) (currentArgs.length == 2 ? (List) possibleServers.filter((name) -> {
        return name.regionMatches(true, 0, currentArgs[1], 0, currentArgs[1].length());
      }).collect(Collectors.toList()) : ImmutableList.of());
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().getPermissionValue("velocity.command.send") != Tristate.FALSE;
  }

}