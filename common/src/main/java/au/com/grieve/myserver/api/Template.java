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

package au.com.grieve.myserver.api;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public abstract class Template {
    private final TemplateManager templateManager;
    private final JsonNode node;
    private final String name;
    private final List<Template> parents = new ArrayList<>();

    public Template(TemplateManager templateManager, JsonNode node) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        this.templateManager = templateManager;
        this.node = node;
        this.name = node.get("name").asText();
        if (node.has("parents")) {
            for (JsonNode n : node.get("parents")) {
                Template parentTemplate = templateManager.getTemplate(Template.class, n.asText());
                this.parents.add(parentTemplate);
            }
        }
    }

    /**
     * Return a list of every node including those inherited
     *
     * @return list of all nodes
     */
    public List<JsonNode> getAllNodes() {
        return getAllNodes(new ArrayList<>());
    }

    protected List<JsonNode> getAllNodes(List<JsonNode> current) {
        List<JsonNode> result = new ArrayList<>();

        // Short circuit any loops
        if (current.contains(node)) {
            return result;
        }

        result.add(node);
        current.add(node);

        for (Template parent : parents) {
            List<JsonNode> parentNodes = parent.getAllNodes(current);
            result.addAll(parentNodes);
            current.addAll(parentNodes);
        }

        return result;
    }
}
