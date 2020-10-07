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
import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.platform.bungeecord.BungeeBridge;
import au.com.grieve.myserver.platform.bungeecord.BungeePlugin;
import au.com.grieve.myserver.template.server.ServerTemplate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

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

    @Arg("list|l")
    public void onServerList(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        for (Server server : BungeePlugin.INSTANCE.getMyServer().getServerManager().getServers()) {
            sb.append(server.getName()).append("\n");
        }

        sender.sendMessage(
                new ComponentBuilder(sb.toString()).create()
        );
    }

    @Arg("create|c @string @MSTemplate(filter=server)")
    public void onCreate(CommandSender sender, String name, ServerTemplate template) {

    }

    @Arg("info @MSServer")
    public void onServerInfo(CommandSender sender, BungeeBridge server) {
        sender.sendMessage(server.bungeeGetInfo());
    }

}
