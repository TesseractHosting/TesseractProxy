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

public class LobbyCommand implements SimpleCommand {
    public static final int MAX_SERVERS_TO_LIST = 50;
    private final ProxyServer server;

    public LobbyCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        Player player = (Player) source;
        Optional<RegisteredServer> toConnect = this.server.getServer(serverName);
        RegisteredServer server;
        if (toConnect.isEmpty()) {
            toConnect = this.server.getServer("limbo");
            if(toConnect.isEmpty()){
                player.sendMessage(
                        Identity.nil(),
                        CommandMessages.SERVER_DOES_NOT_EXIST.args(Component.text(serverName)));
                return;
            }

        }
        server = toConnect.get();



        player.createConnectionRequest((RegisteredServer) toConnect.get()).fireAndForget();
        player.sendMessage(Component.text("Sent to hub"));
    }
    @Override
    public List<String> suggest(Invocation invocation) {
        return new ArrayList<String>();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

}