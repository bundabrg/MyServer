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
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.parsers.SingleParser;
import au.com.grieve.myserver.api.TagDefinition;
import au.com.grieve.myserver.api.templates.server.IServer;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MyServer Tag
 * <p>
 * Relies on a MSServer previously being set so we know what tags are available
 * <p>
 * Returns a TagDefinition
 */
public class MSTagDefinition extends SingleParser {

    public MSTagDefinition(CommandManager manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
    }

    protected IServer getServer() throws ParserInvalidResultException {
        for (Parser parser : Lists.reverse(context.getParsers())) {
            if (parser instanceof MSServer) {
                return ((MSServer) parser).result();
            }
        }
        throw new ParserInvalidResultException(this, "Unknown Server");
    }

    @Override
    protected TagDefinition result() throws ParserInvalidResultException {
        IServer server = getServer();
        if (server.getTemplate().getTags().containsKey(getInput())) {
            return server.getTemplate().getTags().get(getInput());
        }
        throw new ParserInvalidResultException(this, "Invalid Tag");
    }

    @Override
    protected List<String> complete() {
        try {
            IServer server = getServer();
            return server.getTemplate().getTags().keySet().stream()
                    .filter(s -> s.startsWith(getInput()))
                    .limit(20)
                    .collect(Collectors.toList());
        } catch (ParserInvalidResultException e) {
            return Collections.emptyList();
        }
    }
}
