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

package au.com.grieve.myserver.loaders.local;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.ITemplateDefinition;
import au.com.grieve.myserver.api.templates.ITemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.g00fy2.versioncompare.Version;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class LocalTemplateDefinition implements ITemplateDefinition {
    protected static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    protected static Pattern TEMPLATE_FULLNAME_PATTERN = Pattern.compile("([^:]+):([^@]+)@(.+)");

    private final LocalTemplateLoader templateLoader;
    private final Path templatePath;
    private final JsonNode rootNode;

    private final String fullName;
    private final String type;
    private final String name;
    private final Version version;

    private final String description;

    public LocalTemplateDefinition(LocalTemplateLoader templateLoader, Path templatePath) throws IOException, InvalidTemplateException {
        this.templatePath = templatePath;
        this.templateLoader = templateLoader;
        rootNode = MAPPER.readTree(templatePath.resolve("template.yml").toFile());

        // Split out name
        fullName = rootNode.get("name").asText();

        // Split out components
        Matcher matcher = TEMPLATE_FULLNAME_PATTERN.matcher(fullName);
        if (!matcher.find()) {
            throw new InvalidTemplateException("Invalid template name: " + fullName);
        }

        type = matcher.group(1);
        name = matcher.group(2);
        version = new Version(matcher.group(3));

        description = rootNode.has("description") ? rootNode.get("description").asText() : "";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public JsonNode getData() {
        return rootNode;
    }

    @Override
    public <T extends ITemplate> T loadTemplate(Class<T> templateClass) throws InvalidTemplateException {
        return TemplateManager.INSTANCE.loadTemplate(templateClass, this);
    }
}
