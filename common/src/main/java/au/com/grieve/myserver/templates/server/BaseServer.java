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

package au.com.grieve.myserver.templates.server;

import au.com.grieve.myserver.SimpleTemplater;
import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.api.TagDefinition;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.templates.TagsTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public abstract class BaseServer implements Server {
    public static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final ServerTemplate template;
    private final Path serverPath;
    private UUID uuid;
    @Builder.Default
    private Map<String, List<String>> permissions = new HashMap<>();
    private String name;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    @Override
    public Server load() throws InvalidServerException, IOException {
        loadData(MAPPER.readTree(getServerPath().resolve("server.yml").toFile()));
        return this;
    }

    @Override
    public Server save() throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        saveData(root);
        MAPPER.writeValue(serverPath.resolve("server.yml").toFile(), root);
        return this;
    }

    protected void saveData(ObjectNode node) {
        node.put("template", getTemplate().getName());
        node.put("name", getName());
        node.put("uuid", getUuid().toString());

        if (getPermissions().size() > 0) {
            ObjectNode permissions = node.putObject("permissions");
            for (Map.Entry<String, List<String>> entry : getPermissions().entrySet()) {
                ArrayNode permission = permissions.putArray(entry.getKey());
                for (String p : entry.getValue()) {
                    permission.add(p);
                }
            }
        }

        if (getTags().size() > 0) {
            ObjectNode tags = node.putObject("tags");
            for (Map.Entry<String, String> entry : getTags().entrySet()) {
                tags.put(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void loadData(JsonNode node) throws InvalidServerException {
        if (!node.has("name")) {
            throw new InvalidServerException("Missing name field");
        }
        this.name = node.get("name").asText();

        if (!node.has("uuid")) {
            throw new InvalidServerException("Missing uuid field");
        }
        this.uuid = UUID.fromString(node.get("uuid").asText());

        permissions = new HashMap<>();
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

        tags = new HashMap<>();
        if (node.has("tags")) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.get("tags").fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                tags.put(entry.getKey(), entry.getValue().asText());
            }
        }
    }

    public String getTag(String name) {
        return getTag(name, null);
    }

    public String getTag(String name, String defaultValue) {
        if (tags.containsKey(name)) {
            return tags.get(name);
        }

        if (defaultValue != null && getTemplate().getTags().containsKey(name)) {
            return getTemplate().getTags().get(name).getDefaultValue();
        }

        return defaultValue;
    }

    /**
     * Return a list of all tags includng those inherited from template
     *
     * @return map of tags
     */
    public Map<String, String> getAllTags() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, TagDefinition> entry : getTemplate().getTags().entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDefaultValue());
        }
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void setTag(String name, String value) {
        tags.put(name, value);
    }

    /**
     * Update template files with their current values
     */
    public void updateFiles(TagsTemplate.TemplateFileEnum templateType) {
        SimpleTemplater st = createTemplater();

        for (Path path : getTemplate().getTemplateFiles().getOrDefault(templateType, Collections.emptyList())) {
            try (BufferedReader in = new BufferedReader(new FileReader(getTemplate().getTemplatePath().resolve("files").resolve(path).toFile()));
                 BufferedWriter out = new BufferedWriter(new FileWriter(getServerPath().resolve("files").resolve(path).toFile()))) {
                st.process(in, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected SimpleTemplater createTemplater() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        return new SimpleTemplater()
                .register(getAllTags())
                .register("DATE_GENERATED", dtf.format(LocalDateTime.now()));
    }
}
