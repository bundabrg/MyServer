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
import au.com.grieve.myserver.api.Server;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.server.ServerTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

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
     * @param templatePath The template path
     */
    public VanillaServerTemplate(TemplateManager templateManager, Path templatePath) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, templatePath);

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
    public VanillaServer createServer(String name) throws InvalidServerException, IOException {
        // Check that name is valid
        if (name.isEmpty() || !name.matches("[0-9a-zA-Z-.]+")) {
            throw new InvalidServerException("Invalid Name. Must only contains a-zA-Z0-9.-");
        }

        // Does Server Exist?
        if (getTemplateManager().getMyServer().getServerManager().hasServer(name)) {
            throw new InvalidServerException("Server " + name + " already exists");
        }

        // Check path of server does not exists
        Path serverPath = getTemplateManager().getMyServer().getConfig().getFolderConfig().getServersPath().resolve(name);
        if (Files.exists(serverPath)) {
            throw new InvalidServerException("The path '" + serverPath + "' already exists!");
        }

        // Create path and copy all files
        Files.createDirectories(serverPath);
        FileUtils.copyDirectory(getTemplatePath().resolve("files").toFile(), serverPath.resolve("files").toFile());

        // Create the server
        VanillaServer server = VanillaServer.builder()
                .template(this)
                .serverPath(serverPath)
                .name(name)
                .uuid(UUID.randomUUID())
                .build();

        // Save server settings
        server.save();
        server.updateFiles(TemplateFileEnum.STATIC);

        return server;
    }

    @Override
    public Server loadServer(Path serverPath) throws InvalidServerException, IOException {
        return VanillaServer.builder()
                .template(this)
                .serverPath(serverPath)
                .build()
                .load();
    }

    @Data
    public static class ServerSection {
        // Server Version
        String version;

        // Server Description
        String description;
    }

}
