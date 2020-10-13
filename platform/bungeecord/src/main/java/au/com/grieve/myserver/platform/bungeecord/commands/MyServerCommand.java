/*
 * MIT License
 *
 * Copyright (c) 2020 MyServer Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package au.com.grieve.myserver.platform.bungeecord.commands;

import au.com.grieve.bcf.annotations.Arg;
import au.com.grieve.bcf.annotations.Command;
import au.com.grieve.bcf.platform.bungeecord.BungeeCommand;
import au.com.grieve.myserver.api.ServerStatus;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.platform.bungeecord.MyServerPlugin;
import au.com.grieve.myserver.platform.bungeecord.api.templates.server.IBungeeServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;

@Command("myserver|ms")
//@Permission("myserver.*")
public class MyServerCommand extends BungeeCommand {

//    @Arg("reload")
//    @Permission("portalnetwork.admin")
//    @Permission("portalnetwork.command.reload")
//    public void onReload(CommandSender sender) {
//        // Read main config
//        PortalNetwork.getInstance().reload();
//
//        sender.spigot().sendMessage(
//                new ComponentBuilder("Reloaded PortalNetwork").color(ChatColor.YELLOW).create()
//        );
//    }

    @Arg("@MSServer")
    public void onConnect(CommandSender sender, IBungeeServer server) {
        if (!(sender instanceof ProxiedPlayer)) {
            sendMessage(sender, new ComponentBuilder("You need to be a player!").color(ChatColor.RED).create());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        MyServerPlugin.INSTANCE.getProxy().getScheduler().runAsync(MyServerPlugin.INSTANCE, () -> {
            // Send player immediately if server is started
            if (server.getStatus().equals(ServerStatus.STARTED)) {
                server.send(player);
                return;
            }

            // Start server and then send player
            try {
                sendMessage(sender, new ComponentBuilder("Please wait whilst server starts: ").color(ChatColor.AQUA)
                        .append(server.getName()).color(ChatColor.WHITE).create());
                server.start();
                server.send(player);
            } catch (InvalidServerException | IOException e) {
                sendMessage(sender, new ComponentBuilder("Server failed to start: ").color(ChatColor.RED)
                        .append(server.getName()).color(ChatColor.WHITE).create());
            }
        });
    }

}
