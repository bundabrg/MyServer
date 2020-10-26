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

package au.com.grieve.myserver.loaders.local;

import au.com.grieve.myserver.api.ITemplateDefinition;
import au.com.grieve.myserver.api.ITemplateLoader;
import au.com.grieve.myserver.api.templates.ITemplate;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;

@Getter
public class LocalTemplateLoader implements ITemplateLoader {
    // Root path of local templates
    private final Path templatePath;


    public LocalTemplateLoader(Path templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public List<ITemplateDefinition> findTemplatesByName(String search) {
        Map<String, Path> result = new HashMap<>();

        if (!myServer.getConfig().getFolderConfig().getTemplatePath().toFile().exists()) {
            return result;
        }

        try (Stream<Path> walk = Files.walk(myServer.getConfig().getFolderConfig().getTemplatePath(), 10, FileVisitOption.FOLLOW_LINKS)) {
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

    @Override
    public ITemplate loadTemplate(String name) {
        return null;
    }
}
