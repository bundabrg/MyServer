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
import au.com.grieve.myserver.platform.bungeecord.MyServerPlugin;
import au.com.grieve.myserver.platform.bungeecord.api.templates.IBungeeTemplate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.List;

@Command(value="msat", input="template")
@Arg("template|t")
//@Permission("myserver.*")
public class AdminTemplateCommand extends BungeecordAnnotationCommand {

    @Arg("help")
    public void onHelp(CommandSender sender) {
        sender.sendMessage(
                new ComponentBuilder("========= [ Test ] =========").color(ChatColor.AQUA).create()
        );
    }

    @Arg("list")
    public void onTemplateList(CommandSender sender) {
        ComponentBuilder cb = new ComponentBuilder();

        cb.append("========= [ Templates ] =========").color(ChatColor.AQUA);

        List<IBungeeTemplate> templates = MyServerPlugin.INSTANCE.getMyServer().getTemplateManager().getTemplates(IBungeeTemplate.class);

        if (templates.size() > 0) {
            for (IBungeeTemplate template : templates) {
                cb.append("\n").append(template.getName()).color(ChatColor.WHITE);
            }
        } else {
            cb.append("\nNone Found").color(ChatColor.YELLOW);
        }

        sender.sendMessage( cb.create());
    }

    @Arg("info @MSTemplate(filter=server)")
    public void onTemplateInfo(CommandSender sender, IBungeeTemplate template) {
        sender.sendMessage( template.bungeeGetInfo());
    }

}
