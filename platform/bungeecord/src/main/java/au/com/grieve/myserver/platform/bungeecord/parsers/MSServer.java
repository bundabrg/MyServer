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

package au.com.grieve.myserver.platform.bungeecord.parsers;

import au.com.grieve.bcf.ArgNode;
import au.com.grieve.bcf.CommandContext;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.parsers.SingleParser;
import au.com.grieve.myserver.exceptions.NoSuchServerException;
import au.com.grieve.myserver.platform.bungeecord.MyServerPlugin;
import au.com.grieve.myserver.templates.server.Server;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MyServer Server
 */
public class MSServer extends SingleParser {

    public MSServer(CommandManager manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
    }

    @Override
    protected Server result() throws ParserInvalidResultException {
        try {
            return MyServerPlugin.INSTANCE.getMyServer().getServerManager().getServer(getInput().toLowerCase());
        } catch (NoSuchServerException ignored) {
        }
        throw new ParserInvalidResultException(this, "No such server");
    }

    @Override
    protected List<String> complete() {
        return MyServerPlugin.INSTANCE.getMyServer().getServerManager().getServers().stream()
                .map(Server::getName)
                .filter(s -> s.toLowerCase().startsWith(getInput().toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
    }
}
