/*
 * MIT License
 *
 * Copyright (c) 2022 MyServer Developers
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

package au.com.grieve.myserver.api.templates;

import au.com.grieve.myserver.templates.TagsTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TagsTemplateConfig {
    Map<String, TagConfig> tags = new HashMap<>();
    TemplatesConfig templates;

    @Data
    protected static class TagConfig {
        // Description of the Tag
        String description = "";

        // If true the tag requires a value
        Boolean required = false;

        // Default value if none set
        String defaultValue;

        // Type of value
        TagsTemplate.TagType type;

        // Permission to use tag
        String permission;

        // Choices available for value with descriptions
        Map<String, String> choices;

        /**
         * Return true if input validates this tag
         *
         * @param input data to test against
         * @return true if valid
         */
        public boolean validate(String input) {
            switch (type) {
                case STRING:
                    return choices.size() == 0 || choices.keySet().stream().anyMatch(c -> c.equalsIgnoreCase(input));
                case INT:
                    try {
                        Integer.valueOf(input);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case BOOLEAN:
                    return (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false"));
            }
            return false;
        }
    }

    @Data
    protected static class TemplatesConfig {
        @JsonProperty("static")
        List<String> staticFiles = new ArrayList<>();

        @JsonProperty("dynamic")
        List<String> dynamicFiles = new ArrayList<>();
    }
}
