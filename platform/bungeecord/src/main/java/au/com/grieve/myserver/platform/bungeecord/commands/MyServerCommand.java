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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

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

    @Arg("help")
    public void onHelp(CommandSender sender) {
        sender.sendMessage(
                new ComponentBuilder("========= [ Test ] =========").color(ChatColor.AQUA).create()
        );
    }

}
