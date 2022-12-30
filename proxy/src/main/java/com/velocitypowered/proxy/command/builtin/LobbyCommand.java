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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LobbyCommand implements SimpleCommand {
    private final ProxyServer server;

    public LobbyCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        Player player = (Player) source;
        String lobbyServer = server.getConfiguration().getHubServer();
        Optional<RegisteredServer> toConnect = this.server.getServer(lobbyServer);
        RegisteredServer server;
        if (toConnect.isEmpty()) {
            toConnect = this.server.getServer("limbo");
            if(toConnect.isEmpty()){
                player.sendMessage(
                        Identity.nil(),
                        Component.text("Hub server is not online.",NamedTextColor.RED)
                        );
                return;
            }

        }
        server = toConnect.get();

        player.createConnectionRequest(server)
                .connectWithIndication().thenAccept(result -> {
                    if(result) {
                        player.sendMessage(Identity.nil(), Component.translatable(
                                "velocity.command.server-current-server",
                                NamedTextColor.YELLOW,
                                Component.text(lobbyServer)));
                    }
                });
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