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

package au.com.grieve.myserver.platform.bungeecord.template.server;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.platform.bungeecord.BungeeBridge;
import au.com.grieve.myserver.template.server.vanilla.VanillaServerTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.IOException;

public class BungeeVanillaServerTemplate extends VanillaServerTemplate implements BungeeBridge {
    /**
     * Load Server Template from a JsonNode
     *
     * @param templateManager The template manager
     * @param rootNode        The root json node
     */
    public BungeeVanillaServerTemplate(TemplateManager templateManager, JsonNode rootNode) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, rootNode);
    }

    @Override
    public BaseComponent[] bungeeGetInfo() {
        return new ComponentBuilder()
                .append("Name: ").color(ChatColor.YELLOW).append(getName()).color(ChatColor.WHITE).append("\n")
                .append("Server: ").color(ChatColor.YELLOW).append("\n")
                .append("  Version: ").color(ChatColor.YELLOW).append(getServer().getVersion()).color(ChatColor.WHITE).append("\n")
                .append("  Description: ").color(ChatColor.YELLOW).append(getServer().getDescription()).color(ChatColor.WHITE).append("\n")
                .append("Tags: ").color(ChatColor.YELLOW).append(String.valueOf(getTags().size())).color(ChatColor.WHITE).append(" tag(s)")
                .create();
    }

    @Override
    public Server loadServer(JsonNode node) throws InvalidServerException {
        return new BungeeVanillaServer(this, node);
    }
}
