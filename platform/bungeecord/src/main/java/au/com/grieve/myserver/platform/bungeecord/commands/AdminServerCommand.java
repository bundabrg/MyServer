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
import au.com.grieve.myserver.api.TagDefinition;
import au.com.grieve.myserver.api.templates.server.IServerTemplate;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.platform.bungeecord.BungeePlugin;
import au.com.grieve.myserver.platform.bungeecord.api.templates.server.IBungeeServer;
import au.com.grieve.myserver.templates.server.Server;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.IOException;

@Command("msas")
@Arg("server|s")
//@Permission("myserver.*")
public class AdminServerCommand extends AdminCommand {

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

        for (Server server : BungeePlugin.INSTANCE.getMyServer().getServerManager().getServers()) {
            cb.append("\n").append(server.getName()).color(ChatColor.WHITE);
        }

        cb.append("\n===============================").color(ChatColor.AQUA);
        sendMessage(sender, cb.create());
    }

    @Arg("create @string @MSTemplate(filter=server)")
    public void onCreate(CommandSender sender, String name, IServerTemplate template) {
        try {
            template.createServer(name);
            sendMessage(sender, new ComponentBuilder("Created Server: " + name).color(ChatColor.AQUA).create());
        } catch (InvalidServerException | IOException e) {
            sendMessage(sender, new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
        }
    }

    @Arg("destroy @MSServer")
    public void onDestroy(CommandSender sender, IBungeeServer server) {
        try {
            server.destroy();
            sendMessage(sender, new ComponentBuilder("Destroyed Server: " + server.getName()).create());
        } catch (IOException e) {
            sendMessage(sender, new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
        }
    }

    @Arg("info @MSServer")
    public void onServerInfo(CommandSender sender, IBungeeServer server) {
        sendMessage(sender, server.bungeeGetInfo());
    }

    @Arg("edit @MSServer tag set @MSTagDefinition @MSTagValue")
    public void onServerTagsSet(CommandSender sender, IBungeeServer server, TagDefinition definition, String value) {
        server.setTag(definition.getName(), value);
        sendMessage(sender, new ComponentBuilder("Set Tag: ").color(ChatColor.AQUA)
                .append(definition.getName()).color(ChatColor.WHITE)
                .append(" = ").color(ChatColor.AQUA)
                .append(value).color(ChatColor.WHITE)
                .create()
        );
    }

    @Arg("edit @MSServer tag reset @MSTagDefinition")
    public void onServerTagsClear(CommandSender sender, IBungeeServer server, TagDefinition definition) {
        server.resetTag(definition.getName());
        sendMessage(sender, new ComponentBuilder("Reset Tag: ").color(ChatColor.AQUA)
                .append(definition.getName()).color(ChatColor.WHITE)
                .append(" = ").color(ChatColor.AQUA)
                .append(definition.getDefaultValue()).color(ChatColor.WHITE)
                .append(" (Default Value)").color(ChatColor.YELLOW)
                .create()
        );
    }

    @Arg("start @MSServer")
    public void onServerStart(CommandSender sender, IBungeeServer server) {
        try {
            sendMessage(sender, new ComponentBuilder("Starting Server: ").color(ChatColor.AQUA)
                    .append(server.getName()).color(ChatColor.WHITE).create());
            server.start();
        } catch (InvalidServerException | IOException e) {
            sendMessage(sender, new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
        }
    }

    @Arg("stop @MSServer")
    public void onServerStop(CommandSender sender, IBungeeServer server) {
        try {
            sendMessage(sender, new ComponentBuilder("Stopping Server: ").color(ChatColor.AQUA)
                    .append(server.getName()).color(ChatColor.WHITE).create());
            server.stop();
        } catch (InvalidServerException | IOException e) {
            sendMessage(sender, new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
        }
    }
}
