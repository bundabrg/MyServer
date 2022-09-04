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

package au.com.grieve.myserver.templates;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.ITemplateDefinition;
import au.com.grieve.myserver.api.templates.ITemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.utils.JsonUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class Template implements ITemplate {
    protected static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final TemplateManager templateManager;
    private final ITemplateDefinition templateDefinition;
    private final JsonNode data;

    public Template(TemplateManager templateManager, ITemplateDefinition templateDefinition) throws InvalidTemplateException {
        this.templateManager = templateManager;
        this.templateDefinition = templateDefinition;

        // Load Parent data and merge them
        JsonNode data = null;
        if (templateDefinition.getData().has("parents")) {
            for (JsonNode n : templateDefinition.getData().get("parents")) {
                ITemplate parentTemplate = templateManager.loadTemplate(Template.class, n.asText());

                data = data != null ? JsonUtils.merge(data, parentTemplate.getData()) : parentTemplate.getData();
            }
        }

        this.data = data != null ? JsonUtils.merge(data, templateDefinition.getData()) : templateDefinition.getData();
    }

}
