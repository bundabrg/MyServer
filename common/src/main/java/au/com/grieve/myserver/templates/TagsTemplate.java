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

package au.com.grieve.myserver.templates;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.ITemplateDefinition;
import au.com.grieve.myserver.api.TagDefinition;
import au.com.grieve.myserver.api.TypeEnum;
import au.com.grieve.myserver.api.templates.ITagsTemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public enum TagType {
    INT,
    STRING,
    BOOLEAN;


    @JsonValue
    public String getName() {
        return name().toLowerCase();
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
protected static class Config extends Template.Config {
    Tags tags;
}

@Data
protected static class Tags {
    Map<String, Tag> tags = new HashMap<>();
}

@Data
protected static class Tag {
    // Description of the Tag
    String description = "";

    // If true the tag requires a value
    Boolean required = false;

    // Default value if none set
    String defaultValue;

    // Type of value
    TagType type;

    // Permission to use tag
    String permission;

    // Choices available for value with descriptions
    Map<String, String> choices;
}

/**
 * A Template that contains tags and template files
 */
@Getter
public class TagsTemplate extends Template implements ITagsTemplate {
    private final Map<String, TagDefinition> tags = new HashMap<>();
    private final Map<TemplateFileEnum, List<Path>> templateFiles = new HashMap<>();

    {
        for (Iterator<Map.Entry<String, JsonNode>> it = getConfig().get("tags").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            JsonNode node = entry.getValue();
            tags.put(entry.getKey(), TagDefinition.builder()
                    .name(entry.getKey())
                    .type(node.has("type") ? TypeEnum.valueOf(node.get("type").asText().toUpperCase()) : TypeEnum.STRING)
                    .description(node.has("description") ? node.get("description").asText() : null)
                    .defaultValue(node.has("default") ? node.get("default").asText() : null)
                    .permission(node.has("permission") ? node.get("permission").asText() : null)
                    .required(node.has("required") && node.get("required").asBoolean())
                    .build());
        }
    }

    {
        for (Iterator<Map.Entry<String, JsonNode>> it = getConfig().get("templates").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            JsonNode node = entry.getValue();
            TemplateFileEnum templateFileType;
            try {
                templateFileType = TemplateFileEnum.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidTemplateException("Illegal Template Type: " + entry.getKey(), e);
            }
            if (!node.isArray()) {
                throw new InvalidTemplateException("Error in template file. Template list should be an array: " + node);
            }

            if (!templateFiles.containsKey(templateFileType)) {
                templateFiles.put(templateFileType, new ArrayList<>());
            }
            for (JsonNode templateName : node) {
                templateFiles.get(templateFileType).add(Paths.get(templateName.asText()));
            }
        }
    }if(

    public TagsTemplate(TemplateManager templateManager, ITemplateDefinition templateDefinition) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, templateDefinition);
    }.

    getConfig())

    has("tags")

        if(

    getConfig().

    has("templates"))

    @Override
    protected Config loadConfig(JsonNode config) {
        return null;
    }
}


}
