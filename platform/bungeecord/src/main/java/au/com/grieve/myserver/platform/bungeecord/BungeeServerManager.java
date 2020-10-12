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

package au.com.grieve.myserver.platform.bungeecord;

import au.com.grieve.myserver.ServerManager;
import au.com.grieve.myserver.api.ServerStatus;
import au.com.grieve.myserver.platform.bungeecord.api.templates.server.IBungeeServer;
import au.com.grieve.myserver.platform.bungeecord.exceptions.PortNotFoundException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BungeeServerManager extends ServerManager {

    private final List<Integer> reservedPorts = new ArrayList<>();


    public BungeeServerManager(BungeeMyServer myServer) {
        super(myServer);
    }

    @Override
    public BungeeMyServer getMyServer() {
        return (BungeeMyServer) super.getMyServer();
    }

    /**
     * Return an unused port and reserves it
     *
     * @return first unused free port
     */
    protected int reservePort() throws PortNotFoundException {
        int startPort = getMyServer().getConfig().getBungeecord().getPortStart();
        int amount = getMyServer().getConfig().getBungeecord().getPortAmount();
        for (int port = startPort; port < startPort + amount; port++) {
            if (!reservedPorts.contains(port)) {
                return port;
            }
        }
        throw new PortNotFoundException("No free port");
    }

    protected void releasePort(int port) {
        reservedPorts.remove(port);
    }

    public void registerBungeeServer(IBungeeServer server) throws PortNotFoundException {
        int port = reservePort();
        try {
            server.setServerPort(port);
            server.setServerIp("127.0.0.1");

            ServerInfo info = getMyServer().getPlugin().getProxy().constructServerInfo(
                    server.getName(),
                    new InetSocketAddress("127.0.0.1", port),
                    server.getName(),
                    true); // TODO Implement restricted tag

            getMyServer().getPlugin().getProxy().getServers().put(server.getName(), info);
        } catch (Exception e) {
            releasePort(port);
            throw e;
        }
    }

    public void unregisterBungeeServer(IBungeeServer server) {
        getMyServer().getPlugin().getProxy().getServers().remove(server.getName());
        server.setServerPort(null);
        server.setServerIp(null);
    }

    public CompletableFuture<Boolean> serverPing(IBungeeServer server) {
        ServerInfo serverInfo = getMyServer().getPlugin().getProxy().getServers().get(server.getName());
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        if (serverInfo == null) {
            cf.complete(false);
            return cf;
        }

        serverInfo.ping((serverPing, ex) -> cf.complete(serverPing != null));

        return cf;
    }

    public BaseComponent[] statusToComponent(ServerStatus status) {
        ComponentBuilder cb = new ComponentBuilder(status.name());
        switch (status) {
            case ERROR:
            case STOPPING:
            case STOPPED:
                cb.color(ChatColor.RED);
                break;
            case INIT:
                cb.color(ChatColor.BLUE);
                break;
            case STARTING:
            case STARTED:
                cb.color(ChatColor.GREEN);
                break;
            default:
                cb.color(ChatColor.GRAY);
        }
        return cb.create();
    }

}
