/*
 * Copyright (C) 2023 Velocity Contributors
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

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


/**
 * Implements the TesseractVelocity default {@code /builder} command.
 */
public class BuilderCommand implements SimpleCommand {
  private final ProxyServer server;

  public BuilderCommand(ProxyServer server) {
    this.server = server;
  }

  @Override
  public void execute(Invocation invocation) {
    ConnectedPlayer source = (ConnectedPlayer) invocation.source();
    String[] args = invocation.arguments();
    Optional<RegisteredServer> toConnect = this.server.getServer("builder");
    if (toConnect.isEmpty()) {
      source.sendMessage(Component.text("Builder server is not online.", NamedTextColor.RED));
      return;
    }

    AtomicBoolean isBuilder = new AtomicBoolean(false);
    source.getCurrentServer().ifPresent(server -> 
        isBuilder.set(server.getServer() == toConnect.get()));
    if (isBuilder.get()) {
      String argsString = String.join(" ", args);
      MinecraftPacket packet = source.getChatBuilderFactory().builder()
              .asPlayer(source)
              .message(("/builder " + argsString).strip())
              .setTimestamp(Instant.now())
              .toServer();

      assert source.getConnection().getSessionHandler() != null;
      source.getConnection().getSessionHandler().handleGeneric(packet);
      return;
    }


    source.sendMessage(Component.text("Connecting to builder server..."));
    source.createConnectionRequest(toConnect.get())
        .connectWithIndication().thenAccept(result -> {
          if (result) {
            source.sendMessage(Identity.nil(), Component.translatable(
                    "velocity.command.server-current-server",
                    NamedTextColor.YELLOW,
                    Component.text("Builder")));
          }
        })
    ;
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    return new ArrayList<>();
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return ((invocation.source().getPermissionValue("velocity.command.builder") == Tristate.TRUE) 
      && (invocation.source() instanceof Player));
  }
}