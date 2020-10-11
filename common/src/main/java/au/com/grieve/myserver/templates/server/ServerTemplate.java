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

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.ServerStatus;
import au.com.grieve.myserver.api.templates.server.IServerTemplate;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.TagsTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A template for a server
 */
@Getter
@ToString(callSuper = true)
public abstract class ServerTemplate extends TagsTemplate implements IServerTemplate {
    private final String serverStartExecute;
    private final List<String> serverStartCommands = new ArrayList<>();
    private final int serverStartDelay;
    private final List<String> serverStopCommands = new ArrayList<>();
    private final int serverStopWait;

    /**
     * Load Template from a JsonNode
     *
     * @param templatePath template path
     */
    public ServerTemplate(TemplateManager templateManager, Path templatePath) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, templatePath);


        String serverStartExecute = null;
        boolean foundServerStartCommands = false;
        Integer serverStartDelay = null;
        boolean foundServerStopCommands = false;
        Integer serverStopWait = null;
        for (JsonNode n : getAllNodes()) {
            if (n.has("server")) {
                JsonNode serverNode = n.get("server");

                if (serverNode.has("start")) {
                    JsonNode serverStartNode = serverNode.get("start");
                    if (serverStartExecute == null && serverStartNode.has("execute")) {
                        serverStartExecute = serverStartNode.get("execute").asText();
                    }
                    if (!foundServerStartCommands && serverStartNode.has("commands")) {
                        foundServerStartCommands = true;
                        for (JsonNode item : serverStartNode.get("commands")) {
                            serverStartCommands.add(item.asText());
                        }
                    }
                    if (serverStartDelay == null && serverStartNode.has("delay")) {
                        serverStartDelay = serverStartNode.get("delay").asInt();
                    }
                }

                if (serverNode.has("stop")) {
                    JsonNode serverStopNode = serverNode.get("stop");
                    if (!foundServerStopCommands && serverStopNode.has("commands")) {
                        foundServerStopCommands = true;
                        for (JsonNode item : serverStopNode.get("commands")) {
                            serverStopCommands.add(item.asText());
                        }
                    }
                    if (serverStopWait == null && serverStopNode.has("wait")) {
                        serverStopWait = serverStopNode.get("wait").asInt();
                    }
                }
            }
        }
        this.serverStartExecute = serverStartExecute;
        this.serverStartDelay = serverStartDelay != null ? serverStartDelay : 0;
        this.serverStopWait = serverStopWait != null ? serverStopWait : 300;
    }

    /**
     * Create new Server from this template
     */
    @Override
    public Server createServer(String name) throws InvalidServerException, IOException {
        // Check that name is valid
        if (name.isEmpty() || !name.matches("[0-9a-zA-Z-.]+")) {
            throw new InvalidServerException("Invalid Name. Must only contains a-zA-Z0-9.-");
        }

        // Does Server Exist?
        if (getTemplateManager().getMyServer().getServerManager().hasServer(name)) {
            throw new InvalidServerException("Server " + name + " already exists");
        }

        // Create a new random id for server
        UUID uuid = UUID.randomUUID();

        // Create path and copy all files
        Path serverPath = getTemplateManager().getMyServer().getConfig().getFolderConfig().getServersPath().resolve(uuid.toString());
        Files.createDirectories(serverPath);
        FileUtils.copyDirectory(getTemplatePath().resolve("files").toFile(), serverPath.resolve("files").toFile());

        Server server = newServer(serverPath);
        server.setName(name);
        server.setUuid(uuid);
        server.setStatus(ServerStatus.STOPPED);
        server.save();
        server.updateFiles(TemplateFileEnum.STATIC);
        return server;
    }

    /**
     * Load Server from path
     */
    @Override
    public Server loadServer(Path serverPath) throws InvalidServerException, IOException {
        Server server = newServer(serverPath);
        server.load();
        server.setStatus(ServerStatus.STOPPED);
        return server;
    }


}
