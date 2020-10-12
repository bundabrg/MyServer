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

package au.com.grieve.myserver.platform.bungeecord.templates.server.paper;

import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.platform.bungeecord.BungeeServerManager;
import au.com.grieve.myserver.platform.bungeecord.api.templates.server.IBungeeServer;
import au.com.grieve.myserver.platform.bungeecord.exceptions.PortNotFoundException;
import au.com.grieve.myserver.templates.server.paper.PaperServer;
import au.com.grieve.myserver.templates.server.paper.PaperTemplate;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Getter
@ToString(callSuper = true)
public class BungeePaperServer extends PaperServer implements IBungeeServer {

    public BungeePaperServer(PaperTemplate template, Path serverPath) {
        super(template, serverPath);
    }

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

        cb.append("\nStatus: ").color(ChatColor.YELLOW).append("[").color(ChatColor.WHITE)
                .append(getServerManager().statusToComponent(getStatus()))
                .append("]").color(ChatColor.WHITE);

        cb.append("\nServer: ").color(ChatColor.YELLOW)
                .append("\n  Listen: ").color(ChatColor.YELLOW).append(getServerIp() != null ? getServerIp() : "").color(ChatColor.WHITE)
                .append("\n  Port: ").color(ChatColor.YELLOW).append(getServerPort() != null ? getServerPort().toString() : "").color(ChatColor.WHITE);

        return cb.create();
    }

    @Override
    protected void startServer() throws InvalidServerException, IOException {
        // We can't create a server with the same name as a running Bungee server
        if (getServerManager().getMyServer().getPlugin().getProxy().getServers().containsKey(getName())) {
            throw new InvalidServerException("Another running server has our name");
        }

        // Register with Bungeecord
        try {
            getServerManager().registerBungeeServer(this);
        } catch (PortNotFoundException e) {
            throw new InvalidServerException(e);
        }

        try {
            super.startServer();
        } catch (Exception e) {
            getServerManager().unregisterBungeeServer(this);
            throw e;
        }
    }

    @Override
    protected boolean serverPing() {
        try {
            return getServerManager().serverPing(this).get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    protected void onServerStop() {
        super.onServerStop();

        // Unregister from Bungeecord
        getServerManager().unregisterBungeeServer(this);
    }

    @Override
    public BungeeServerManager getServerManager() {
        return (BungeeServerManager) super.getServerManager();
    }

    @Override
    protected void handleOutput(String output) {
        getServerManager().getMyServer().getPlugin().getLogger().info("[" + getName() + "]: " + output);
    }
}
