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

import au.com.grieve.myserver.api.ITemplateDefinition;
import au.com.grieve.myserver.api.ITemplateLoader;
import au.com.grieve.myserver.api.templates.ITemplate;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.MapMaker;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Management Class for Templates
 */
@Getter
public class TemplateManager {
    public static TemplateManager INSTANCE;
    public static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
//    public static Pattern TEMPLATE_FULL_PATTERN = Pattern.compile("([^:]+):([^@]+)@(.+)");
//    public static Pattern TEMPLATE_PARTIAL_PATTERN = Pattern.compile("([^:]+):(.+)");

    // Types of Templates Supported
    private final Map<String, Class<? extends ITemplate>> registeredTemplateTypes = new HashMap<>();

    // Types of TemplateLoaders Supported
    private final Map<String, ITemplateLoader> registeredTemplateLoaders = new HashMap<>();

    // Templates that currently exist in memory (singleton)
    private final ConcurrentMap<String, ITemplate> templateInstances = new MapMaker()
            .weakValues()
            .makeMap();
    private final Queue<String> templateLocks = new ConcurrentLinkedQueue<>();

    private final MyServer myServer;

    public TemplateManager(MyServer myServer) {
        INSTANCE = this;
        this.myServer = myServer;
    }

    /**
     * Return list of template definitions that match the partial name
     *
     * @param name Partial name of template in form type:name@version
     * @return list of matching definitions
     */
    public List<ITemplateDefinition> findTemplatesByName(String name) {
        List<ITemplateDefinition> result = new ArrayList<>();
        for (ITemplateLoader templateLoader : getRegisteredTemplateLoaders().values()) {
            result.addAll(templateLoader.findTemplatesByName(name));
        }
        return result;
    }

    /**
     * Load a template by name
     * <p>
     * If multiple templates are resolved then load the latest version
     *
     * @param templateClass class of template
     * @param name          template name in the form type:name@version
     * @param <T>           class of template
     * @return template
     * @throws InvalidTemplateException for template errors
     */
    public <T extends ITemplate> T loadTemplate(Class<T> templateClass, String name) throws InvalidTemplateException {
        //noinspection unchecked
        return (T) findTemplatesByName(name).stream()
                .sorted()
                .findFirst()
                .orElseThrow(() -> new InvalidTemplateException("No such template: " + name));
    }

    /**
     * Register a new Template Type Class
     *
     * @param name          Name of the template type
     * @param templateClass Class to load
     * @return ourself to allow chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public TemplateManager registerTemplateType(String name, Class<? extends ITemplate> templateClass) {
        registeredTemplateTypes.put(name, templateClass);
        return this;
    }

    /**
     * Register a new TemplateLoader
     *
     * @param name           Name of the template loader
     * @param templateLoader the loader
     * @return ourself to allow chaining
     */
    public TemplateManager registerTemplateLoader(String name, ITemplateLoader templateLoader) {
        registeredTemplateLoaders.put(name, templateLoader);
        return this;
    }

    /**
     * Load a template from a definition
     *
     * @param templateClass class of template
     * @param definition    template definition
     * @param <T>           type of template
     * @return template
     * @throws InvalidTemplateException any template errors
     */
    public <T extends ITemplate> T loadTemplate(Class<T> templateClass, ITemplateDefinition definition) throws InvalidTemplateException {
        // Check if we already cache the loaded template and return that if so
        ITemplate result = templateInstances.get(definition.getFullName());
        if (result != null) {
            //noinspection unchecked
            return (T) result;
        }

        // Find a template type by taking off characters from the right till a match is found
        String templateType = definition.getType();
        Class<? extends ITemplate> templateTypeClass = null;
        while (templateType.length() > 0) {
            templateTypeClass = registeredTemplateTypes.get(templateType);
            if (templateTypeClass != null) {
                break;
            }
            templateType = templateType.substring(0, templateType.length() - 1);
        }

        if (templateTypeClass == null) {
            throw new InvalidTemplateException("Unknown template name: " + definition.getFullName());
        }

        // Make sure the template is not already locked
        // TODO perhaps a proper lock here instead to allow concurrency
        if (templateLocks.contains(definition.getFullName())) {
            throw new InvalidTemplateException("Template is locked. Do you have a parent loop?: " + definition.getFullName());
        }
        templateLocks.add(definition.getFullName());

        try {
            result = templateTypeClass
                    .getConstructor(TemplateManager.class, ITemplateDefinition.class)
                    .newInstance(this, definition);
            templateInstances.put(definition.getFullName(), result);
            //noinspection unchecked
            return (T) result;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            templateLocks.remove(definition.getFullName());
        }
    }
}
