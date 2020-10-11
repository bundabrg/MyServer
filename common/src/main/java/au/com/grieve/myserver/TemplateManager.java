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

package au.com.grieve.myserver;

import au.com.grieve.myserver.api.templates.ITemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.Template;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.MapMaker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Management Class for Templates
 */
@Getter
@RequiredArgsConstructor
public class TemplateManager {
    public static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final Map<String, Class<? extends ITemplate>> registeredTemplateTypes = new HashMap<>();
    private final ConcurrentMap<String, ITemplate> templateInstances = new MapMaker()
            .weakValues()
            .makeMap();
    private final Queue<String> templateLocks = new ConcurrentLinkedQueue<>();

    private final MyServer myServer;

    /**
     * Return a template by name
     *
     * @param typeClass Type of template
     * @param name      Name of the template
     * @param <T>       Type of template
     * @return the returned template else null
     */
    public <T extends Template> T getTemplate(Class<T> typeClass, String name) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        // Check if we already have an instance in use
        if (templateInstances.containsKey(name)) {
            //noinspection unchecked
            return (T) templateInstances.get(name);
        }

        // Look for matching template
        Path templatePath = getTemplatePaths().get(name);

        if (templatePath == null) {
            throw new NoSuchTemplateException("No such template: " + name);
        }

        // Load the template
        ITemplate template = loadTemplate(templatePath);

        if (!typeClass.isAssignableFrom(template.getClass())) {
            throw new InvalidTemplateException("Template '" + name + "' is not of type '" + typeClass);
        }

        //noinspection unchecked
        return (T) template;
    }

    /**
     * Return a list of Template Versions along with their Json data
     *
     * @return map of names to paths
     */
    protected Map<String, Path> getTemplatePaths() {
        Map<String, Path> result = new HashMap<>();

        if (!myServer.getConfig().getFolderConfig().getTemplatePath().toFile().exists()) {
            return result;
        }

        try (Stream<Path> walk = Files.walk(myServer.getConfig().getFolderConfig().getTemplatePath(), 5)) {
            Path lastPath = null;
            for (Path path : walk
                    .filter(Files::isDirectory)
                    .filter(p -> p.resolve("template.yml").toFile().exists())
                    .collect(Collectors.toList())) {

                // If a template.yml file is found in a directory then prune out all child directories
                if (lastPath == null || !path.startsWith(lastPath)) {
                    lastPath = path;
                    try {
                        JsonNode node = MAPPER.readTree(new FileInputStream(path.resolve("template.yml").toFile()));
                        if (!node.has("name")) {
                            throw new InvalidTemplateException("Failed to find a name field");
                        }

                        result.put(node.get("name").asText(), path);
                    } catch (InvalidTemplateException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Return a list of Templates
     *
     * @param typeClass Type of template
     * @param <T>       Type of Template
     * @return Array of Templates
     */
    @SuppressWarnings("unused")
    public <T extends Template> List<T> getTemplates(Class<T> typeClass) {
        List<T> result = new ArrayList<>();

        for (Map.Entry<String, Path> entry : getTemplatePaths().entrySet()) {
            ITemplate template;
            try {
                template = loadTemplate(entry.getValue());
            } catch (IOException | InvalidTemplateException e) {
                e.printStackTrace();
                continue;
            }

            if (!(typeClass.isAssignableFrom(template.getClass()))) {
                continue;
            }

            //noinspection unchecked
            result.add((T) template);
        }
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TemplateManager registerTemplateType(String name, Class<? extends ITemplate> templateClass) {
        registeredTemplateTypes.put(name, templateClass);
        return this;
    }

    /**
     * Load a template from a stream
     *
     * @param path Path of template
     */
    public ITemplate loadTemplate(Path path) throws IOException, InvalidTemplateException {
        JsonNode rootNode = MAPPER.readTree(path.resolve("template.yml").toFile());

        if (!rootNode.has("name")) {
            throw new InvalidTemplateException("Failed to find a name field");
        }

        String templateName = rootNode.get("name").asText();

        // Check if we already have this template in use and return that instead
        if (templateInstances.containsKey(templateName)) {
            // TODO update instance with new data maybe
            return templateInstances.get(templateName);
        }

        // Take away characters from the right of the version string till we match a templateType. This allows
        // use to set a template version but also allow template designers to add their own version suffixes
        String templateSubName = templateName;
        while (templateSubName.length() > 0) {
            if (registeredTemplateTypes.containsKey(templateSubName)) {
                break;
            }
            templateSubName = templateSubName.substring(0, templateSubName.length() - 1);
        }
        if (!registeredTemplateTypes.containsKey(templateSubName)) {
            throw new InvalidTemplateException("Unknown template name: " + templateName);
        }

        // Make sure the template is not already locked
        // TODO perhaps a proper lock here instead to allow concurrency
        if (templateLocks.contains(templateName)) {
            throw new InvalidTemplateException("Template is locked. Do you have a parent loop?: " + templateName);
        }
        templateLocks.add(templateName);

        try {
            ITemplate result = registeredTemplateTypes.get(templateSubName)
                    .getConstructor(TemplateManager.class, Path.class)
                    .newInstance(this, path);
            templateInstances.put(templateName, result);
            return result;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            templateLocks.remove(templateName);
        }
    }

}
