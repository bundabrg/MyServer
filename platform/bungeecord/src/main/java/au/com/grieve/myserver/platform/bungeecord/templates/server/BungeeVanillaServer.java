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

package au.com.grieve.myserver.platform.bungeecord.templates.server;

import au.com.grieve.myserver.platform.bungeecord.BungeeBridge;
import au.com.grieve.myserver.templates.server.vanilla.VanillaServer;
import lombok.experimental.SuperBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.List;
import java.util.Map;

@SuperBuilder
public class BungeeVanillaServer extends VanillaServer implements BungeeBridge {

//    @Builder
//    public BungeeVanillaServer(VanillaServerTemplate template, Path serverPath, UUID uuid, Map<String, List<String>> permissions, Map<String, Object> tags, String name) {
//        super(template, serverPath, uuid, permissions, tags, name);
//    }

    @Override
    public BaseComponent[] bungeeGetInfo() {
        ComponentBuilder cb = new ComponentBuilder()
                .append("Name: ").color(ChatColor.YELLOW).append(getName()).color(ChatColor.WHITE)
                .append("\nTemplate: ").color(ChatColor.YELLOW).append(getTemplate().getName()).color(ChatColor.WHITE)
                .append("\nUUID: ").color(ChatColor.YELLOW).append(getUuid().toString()).color(ChatColor.WHITE)
                .append("\nPermissions: ").color(ChatColor.YELLOW);

        for (Map.Entry<String, List<String>> entry : getPermissions().entrySet()) {
            cb.append("\n  " + entry.getKey() + ":").color(ChatColor.YELLOW);
            for (String accountId : entry.getValue()) {
                cb.append("\n").append("    - " + accountId).color(ChatColor.WHITE);
            }
        }

        cb.append("\nTags: ").color(ChatColor.YELLOW);
        for (Map.Entry<String, String> entry : getTags().entrySet()) {
            cb.append("\n  " + entry.getKey() + ": ").color(ChatColor.YELLOW).append(entry.getValue()).color(ChatColor.WHITE);
        }

        return cb.create();
    }

}
