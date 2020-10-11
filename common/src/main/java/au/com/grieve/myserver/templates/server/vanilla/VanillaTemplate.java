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

package au.com.grieve.myserver.templates.server.vanilla;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.server.ServerTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A Vanilla Server Template
 * <p>
 * A Vanilla server is able to download and patch a vanilla server ready to run behind bungeecord
 */
@Getter
@ToString(callSuper = true)
public abstract class VanillaTemplate extends ServerTemplate {
    private final String version;

    /**
     * Load Server Template from a JsonNode
     *
     * @param templatePath The template path
     */
    public VanillaTemplate(TemplateManager templateManager, Path templatePath) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, templatePath);

        String version = null;
        for (JsonNode n : getAllNodes()) {
            if (n.has("vanilla")) {
                JsonNode serverNode = n.get("vanilla");
                if (version == null && serverNode.has("version")) {
                    version = serverNode.get("version").asText();
                }
            }
        }

        if (version == null) {
            throw new InvalidTemplateException("Missing field: vanilla.version");
        }
        this.version = version;
    }

}
