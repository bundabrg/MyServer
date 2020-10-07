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

package au.com.grieve.myserver.template.server.vanilla;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.template.server.ServerTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;

/**
 * A Vanilla Server Template
 * <p>
 * A Vanilla server is able to download and patch a vanilla server ready to run behind bungeecord
 */
@Getter
@ToString(callSuper = true)
public class VanillaServerTemplate extends ServerTemplate {
    private final ServerSection server;

    /**
     * Load Server Template from a JsonNode
     *
     * @param rootNode The root json node
     */
    public VanillaServerTemplate(TemplateManager templateManager, JsonNode rootNode) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, rootNode);

        server = new ServerSection();

        for (JsonNode n : getAllNodes()) {
            if (n.has("server")) {
                JsonNode serverNode = n.get("server");
                if (serverNode.has("version") && server.getVersion() == null) {
                    server.setVersion(serverNode.get("version").asText());
                }

                if (serverNode.has("description") && server.getDescription() == null) {
                    server.setDescription(serverNode.get("description").asText());
                }
            }
        }
    }

    @Override
    public VanillaServer createServer(String instanceName) {
        return null;
    }

    @Override
    public Server loadServer(JsonNode node) throws InvalidServerException {
        return new VanillaServer(this, node);
    }

    @Data
    public static class ServerSection {
        // Server Version
        String version;

        // Server Description
        String description;
    }

}
