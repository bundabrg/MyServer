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
import au.com.grieve.myserver.api.templates.ITemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.platform.bungeecord.BungeePlugin;
import au.com.grieve.myserver.templates.Template;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MyServer Template
 * <p>
 * Parameters:
 * filter: Filter the template by the text
 */
public class MSTemplate extends SingleParser {

    public MSTemplate(CommandManager manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        try {
            ITemplate result = BungeePlugin.INSTANCE.getMyServer().getTemplateManager().getTemplate(Template.class, getInput().toLowerCase());
            if (result.getName().startsWith(getParameter("filter", ""))) {
                return result;
            }
        } catch (NoSuchTemplateException | InvalidTemplateException | IOException ignored) {
        }
        throw new ParserInvalidResultException(this, "No such template");
    }

    @Override
    protected List<String> complete() {
        return BungeePlugin.INSTANCE.getMyServer().getTemplateManager().getTemplates(Template.class).stream()
                .map(ITemplate::getName)
                .filter(s -> s.startsWith(getParameter("filter", "")))
                .filter(s -> s.toLowerCase().startsWith(getInput().toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
    }
}
