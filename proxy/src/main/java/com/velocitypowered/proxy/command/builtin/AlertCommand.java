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
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Implements the TesseractVelocity default {@code /alert} command.
 */
public class AlertCommand implements SimpleCommand {
  private final ProxyServer server;

  public AlertCommand(ProxyServer server) {
    this.server = server;
  }

  @Override
  public void execute(Invocation invocation) {
    Component message = Component.text("Alert: ", NamedTextColor.RED)
        .append(Component.text(String.join(" ", invocation.arguments()), NamedTextColor.WHITE));
    server.getAllPlayers().forEach(player -> {
      player.sendMessage(message);
    });
    server.getConsoleCommandSource().sendMessage(message);
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().getPermissionValue("velocity.command.alert") == Tristate.TRUE;
  }
}