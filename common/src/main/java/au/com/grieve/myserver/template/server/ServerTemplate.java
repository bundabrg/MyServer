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

package au.com.grieve.myserver.template.server;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.api.Tag;
import au.com.grieve.myserver.api.Template;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A template for a server
 */
@Getter
@ToString(callSuper = true)
public abstract class ServerTemplate extends Template {
    private final Map<String, Tag> tags = new HashMap<>();

    /**
     * Load Template from a JsonNode
     *
     * @param node root JsonNode
     */
    public ServerTemplate(TemplateManager templateManager, JsonNode node) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, node);

        for (JsonNode n : getAllNodes()) {
            if (n.has("tags")) {
                for (Iterator<Map.Entry<String, JsonNode>> it = n.get("tags").fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    tags.putIfAbsent(entry.getKey(), new Tag(entry.getValue()));
                }
            }
        }
    }

    /**
     * Create new Server from this template
     */
    public abstract Server createServer(String instanceName);

    /**
     * Load Server from stream
     */
    public abstract Server loadServer(JsonNode node) throws InvalidServerException;


}
