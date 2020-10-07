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

package au.com.grieve.myserver.template.server.vanilla;

import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.api.Tag;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class VanillaServer implements Server {
    private final VanillaServerTemplate template;
    private final UUID uuid;
    private final Map<String, List<String>> permissions = new HashMap<>();
    private final Map<String, Object> tags = new HashMap<>();
    @Setter
    private String name;

    public VanillaServer(VanillaServerTemplate template, JsonNode node) throws InvalidServerException {
        this.template = template;

        if (!node.has("name")) {
            throw new InvalidServerException("Missing name field");
        }
        this.name = node.get("name").asText();

        if (!node.has("uuid")) {
            throw new InvalidServerException("Missing uuid field");
        }
        this.uuid = UUID.fromString(node.get("uuid").asText());

        if (node.has("permissions")) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.get("permissions").fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();

                List<String> permissionItems = new ArrayList<>();
                for (JsonNode p : entry.getValue()) {
                    permissionItems.add(p.asText());
                }
                this.permissions.put(entry.getKey(), permissionItems);
            }
        }

        if (node.has("tags")) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.get("tags").fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();

                Tag tag = template.getTags().get(entry.getKey());
                if (tag != null) {
                    Object result = tag.parseNode(entry.getValue());
                    if (result != null) {
                        tags.put(entry.getKey(), result);
                    }
                } else {
                    // Store the tag as as string just in case
                    tags.put(entry.getKey(), entry.getValue().asText());
                }
            }
        }
    }
}
