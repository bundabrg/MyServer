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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class Tag {
    private final String description;
    private final TypeEnum type;
    private final Object defaultValue;
    private final String permission;
    private final List<String> choices = new ArrayList<>();

    public Tag(JsonNode node) {
        description = node.has("description") ? node.get("description").asText() : "";
        type = node.has("type") ? TypeEnum.valueOf(node.get("type").asText().toUpperCase()) : TypeEnum.STRING;
        defaultValue = parseNode(node.get("default"));
        permission = node.has("permission") ? node.get("permission").asText() : "";
        if (node.has("choice")) {
            for (JsonNode choice : node.get("choice")) {
                choices.add(choice.asText());
            }
        }
    }

    public Object parseNode(JsonNode node) {
        return parseNode(node, null);
    }

    /**
     * Read the value from a JsonNode
     *
     * @param node Data node
     * @return Object returned data
     */
    public Object parseNode(JsonNode node, Object defaultValue) {
        if (node == null) {
            return defaultValue;
        }

        switch (type) {
            case STRING:
                return node.asText();
            case INT:
                return node.asInt();
            case BOOLEAN:
                return node.asBoolean();
            case CHOICE:
                String result = node.asText();
                return choices.contains(result) ? result : null;
            default:
                return null;
        }

    }
}