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

import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.platform.bungeecord.impl.command.BungeecordAnnotationCommand;
import au.com.grieve.myserver.api.TagDefinition;
import au.com.grieve.myserver.api.templates.server.IServer;
import au.com.grieve.myserver.api.templates.server.IServerTemplate;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.platform.bungeecord.MyServerPlugin;
import au.com.grieve.myserver.platform.bungeecord.api.templates.server.IBungeeServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.IOException;
import java.util.List;

@Command(value="msas", input="server")
@Arg("server|s")
//@Permission("myserver.*")
public class AdminServerCommand extends BungeecordAnnotationCommand {

    @Arg("help")
    public void onHelp(CommandSender sender) {
        sender.sendMessage(
                new ComponentBuilder("========= [ Test ] =========").color(ChatColor.AQUA).create()
        );
    }

    @Arg("list")
    public void onServerList(CommandSender sender) {
        ComponentBuilder cb = new ComponentBuilder();

        cb.append("========= [ Servers ] =========").color(ChatColor.AQUA);

        List<IServer> servers = MyServerPlugin.INSTANCE.getMyServer().getServerManager().getServers();
        if (servers.size() > 0) {
            for (IServer server : servers) {
                cb.append("\n").append(server.getName()).color(ChatColor.WHITE);
            }
        } else {
            cb.append("\nNone Found").color(ChatColor.YELLOW);
        }

        sender.sendMessage(cb.create());
    }

    @Arg("create @string @MSTemplate(filter=server)")
    public void onCreate(CommandSender sender, String name, IServerTemplate template) {
        MyServerPlugin.INSTANCE.getProxy().getScheduler().runAsync(MyServerPlugin.INSTANCE, () -> {
            try {
                sender.sendMessage(new ComponentBuilder("Initializing Server: " + name).color(ChatColor.AQUA).create());
                template.createServer(name);
                sender.sendMessage(new ComponentBuilder("Created Server: " + name).color(ChatColor.AQUA).create());
            } catch (InvalidServerException | IOException e) {
                sender.sendMessage(new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
            }
        });
    }

    @Arg("delete @MSServer")
    public void onDestroy(CommandSender sender, IBungeeServer server) {
        MyServerPlugin.INSTANCE.getProxy().getScheduler().runAsync(MyServerPlugin.INSTANCE, () -> {
            try {
                server.destroy();
                sender.sendMessage(new ComponentBuilder("Deleted Server: ").color(ChatColor.AQUA)
                        .append(server.getName()).color(ChatColor.WHITE).create());
            } catch (IOException e) {
                sender.sendMessage(new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
            }
        });
    }

    @Arg("info @MSServer")
    public void onServerInfo(CommandSender sender, IBungeeServer server) {
        sender.sendMessage( server.bungeeGetInfo());
    }

    @Arg("edit @MSServer set @MSTagDefinition @MSTagValue")
    public void onServerEditSet(CommandSender sender, IBungeeServer server, TagDefinition definition, String value) {
        server.setTag(definition.getName(), value);
        sender.sendMessage( new ComponentBuilder("Set Tag: ").color(ChatColor.AQUA)
                .append(definition.getName()).color(ChatColor.WHITE)
                .append(" = ").color(ChatColor.AQUA)
                .append(value).color(ChatColor.WHITE)
                .create()
        );
    }

    @Arg("edit @MSServer unset @MSTagDefinition")
    public void onServerEditUnset(CommandSender sender, IBungeeServer server, TagDefinition definition) {
        server.resetTag(definition.getName());
        sender.sendMessage( new ComponentBuilder("Unset Tag: ").color(ChatColor.AQUA)
                .append(definition.getName()).color(ChatColor.WHITE)
                .append(" = ").color(ChatColor.AQUA)
                .append(definition.getDefaultValue()).color(ChatColor.WHITE)
                .append(" (Default Value)").color(ChatColor.YELLOW)
                .create()
        );
    }

    @Arg("start @MSServer")
    public void onServerStart(CommandSender sender, IBungeeServer server) {
        MyServerPlugin.INSTANCE.getProxy().getScheduler().runAsync(MyServerPlugin.INSTANCE, () -> {
            try {
                sender.sendMessage( new ComponentBuilder("Starting Server: ").color(ChatColor.AQUA)
                        .append(server.getName()).color(ChatColor.WHITE).create());
                server.start();
//                sender.sendMessage( new ComponentBuilder("Server Started: ").color(ChatColor.AQUA)
//                        .append(server.getName()).color(ChatColor.WHITE).create());
            } catch (InvalidServerException | IOException e) {
                sender.sendMessage( new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
            }
        });
    }

    @Arg("stop @MSServer")
    public void onServerStop(CommandSender sender, IBungeeServer server) {
        MyServerPlugin.INSTANCE.getProxy().getScheduler().runAsync(MyServerPlugin.INSTANCE, () -> {
            try {
                sender.sendMessage( new ComponentBuilder("Stopping Server: ").color(ChatColor.AQUA)
                        .append(server.getName()).color(ChatColor.WHITE).create());
                server.stop();
//                sender.sendMessage( new ComponentBuilder("Server Stopped: ").color(ChatColor.AQUA)
//                        .append(server.getName()).color(ChatColor.WHITE).create());
            } catch (InvalidServerException | IOException e) {
                sender.sendMessage( new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
            }
        });
    }
}
