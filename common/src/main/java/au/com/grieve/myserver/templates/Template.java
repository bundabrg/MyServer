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
import au.com.grieve.myserver.api.templates.ITemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class Template implements ITemplate {
    protected static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final TemplateManager templateManager;
    private final ITemplateDefinition templateDefinition;
    private final Config config;

    public Template(TemplateManager templateManager, ITemplateDefinition templateDefinition) throws InvalidTemplateException {
        this.templateManager = templateManager;
        this.templateDefinition = templateDefinition;

        // Load Parent data and merge them
        JsonNode config = null;
        if (templateDefinition.getConfig().has("parents")) {
            for (JsonNode n : templateDefinition.getConfig().get("parents")) {
                ITemplate parentTemplate = templateManager.loadTemplate(Template.class, n.asText());

                config = config != null ? JsonUtils.merge(config, parentTemplate.getConfig()) : parentTemplate.getConfig();
            }
        }

        config = config != null ? JsonUtils.merge(config, templateDefinition.getConfig()) : templateDefinition.getConfig();
        this.config = loadConfig(config);
    }

    /**
     * Load Config
     */
    protected abstract Config loadConfig(JsonNode config);

    protected static abstract class Config {
    }
}
