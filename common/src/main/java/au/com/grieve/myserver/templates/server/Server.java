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

import au.com.grieve.myserver.ServerManager;
import au.com.grieve.myserver.SimpleTemplater;
import au.com.grieve.myserver.api.ServerStatus;
import au.com.grieve.myserver.api.TagDefinition;
import au.com.grieve.myserver.api.templates.ITagsTemplate;
import au.com.grieve.myserver.api.templates.server.IServer;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import java.util.concurrent.TimeUnit;

@Getter
@ToString
public abstract class Server implements IServer {
    public static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final ServerTemplate template;
    private final Path serverPath;
    private final Map<String, List<String>> permissions = new HashMap<>();
    private final Map<String, String> tags = new HashMap<>();

    // Process Options
    private Process process;
    private BufferedReader processOutput;
    private BufferedWriter processInput;

    @Setter
    private String serverIp;

    @Setter
    private Integer serverPort;

    @Setter
    private String name;

    @Setter
    private UUID uuid;

    @Setter
    private ServerStatus status;

    public Server(ServerTemplate template, Path serverPath) {
        this.template = template;
        this.serverPath = serverPath;
        this.status = ServerStatus.UNKNOWN;
    }

    @Override
    public IServer load() throws InvalidServerException, IOException {
        loadData(MAPPER.readTree(getServerPath().resolve("server.yml").toFile()));
        return this;
    }

    @Override
    public IServer save() throws IOException {
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

        permissions.clear();
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

        tags.clear();
        if (node.has("tags")) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.get("tags").fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                tags.put(entry.getKey(), entry.getValue().asText());
            }
        }
    }

    @Override
    public String getTag(String name) {
        return getTag(name, null);
    }

    @Override
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
    @Override
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

    @Override
    public void setTag(String name, String value) {
        tags.put(name, value);
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateFiles(ITagsTemplate.TemplateFileEnum.DYNAMIC);
    }

    /**
     * Remove local value for tag
     *
     * @param name Name of tag
     */
    @Override
    public void resetTag(String name) {
        if (tags.remove(name) != null) {
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updateFiles(ITagsTemplate.TemplateFileEnum.DYNAMIC);
        }
    }

    /**
     * Update template files with their current values
     */
    public void updateFiles(ITagsTemplate.TemplateFileEnum templateType) {
        SimpleTemplater st = newTemplater();

        for (Path path : getTemplate().getTemplateFiles().getOrDefault(templateType, Collections.emptyList())) {
            try (BufferedReader in = new BufferedReader(new FileReader(getTemplate().getTemplatePath().resolve("files").resolve(path).toFile()));
                 BufferedWriter out = new BufferedWriter(new FileWriter(getServerPath().resolve("files").resolve(path).toFile()))) {
                st.process(in, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected SimpleTemplater newTemplater() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        return new SimpleTemplater()
                .register(getAllTags())
                .register("DATE_GENERATED", dtf.format(LocalDateTime.now()))
                .register("MC_SERVER_IP", serverIp != null ? serverIp : "invalid")
                .register("MC_SERVER_PORT", serverPort != null ? serverPort.toString() : "invalid");
    }

    @Override
    public void start() throws InvalidServerException, IOException {
        switch (status) {
            case STARTED:
                throw new InvalidServerException("Server is already started.");
            case STARTING:
                throw new InvalidServerException("Server is already starting.");
            case INIT:
                throw new InvalidServerException("Server is still initializing.");
            case ERROR:
                // Reset Status
                setStatus(ServerStatus.STOPPED);
            case STOPPED:
                break;
            default:
                throw new InvalidServerException("Server is not in a state to be started.");
        }
        try {
            // Start Server
            startServer();

            // Save a reference to server during its lifetime
            getTemplate().getTemplateManager().getMyServer().getServerManager().getServerInstances().put(getUuid(), this);
        } catch (Exception e) {
            setStatus(ServerStatus.ERROR);
            throw e;
        }
    }

    @Override
    public void stop() throws InvalidServerException, IOException {
        switch (status) {
            case STOPPED:
                throw new InvalidServerException("Server is already stopped.");
            case STOPPING:
                throw new InvalidServerException("Server is already stopping.");
            case STARTED:
                break;
            default:
                throw new InvalidServerException("Server is not in a state to be started.");
        }

        stopServer();

        // Remove reference to server
        getTemplate().getTemplateManager().getMyServer().getServerManager().getServerInstances().remove(getUuid());
    }

    @Override
    public void destroy() throws IOException {
        FileUtils.deleteDirectory(getServerPath().toFile());
    }

    @Override
    public ServerManager getServerManager() {
        return getTemplate().getTemplateManager().getMyServer().getServerManager();
    }

    protected void startServer() throws InvalidServerException, IOException {
        setStatus(ServerStatus.INIT);
        // Make sure required tags have a value
        for (Map.Entry<String, TagDefinition> entry : getTemplate().getTags().entrySet()) {
            if (entry.getValue().isRequired() && !tags.containsKey(entry.getKey()) && entry.getValue().getDefaultValue() == null) {
                throw new InvalidServerException("Required Tag Missing: " + entry.getKey() +
                        (entry.getValue().getDescription() != null ? " - " + entry.getValue().getDescription() : ""));
            }
        }

        updateFiles(ITagsTemplate.TemplateFileEnum.DYNAMIC);

        // Prepare Server
        getTemplate().prepareServer(this);

        setStatus(ServerStatus.STARTING);

        SimpleTemplater st = newTemplater();

        ProcessBuilder pb = new ProcessBuilder(st.replace(getTemplate().getServerStartExecute()).split(" "))
                .redirectErrorStream(true)
                .directory(getServerPath().resolve("files").toFile());

        process = pb.start();
        processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        // Handler the lifetime of the process
        getServerManager().getMyServer().getScheduler().runAsync(() -> {
            try {
                for (String line; ((line = processOutput.readLine()) != null); ) {
                    handleOutput(line);
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    process = null;
                    processOutput.close();
                    processInput.close();
                    processOutput = null;
                    processInput = null;
                    onServerStop();
                } catch (IOException ignored) {
                }
            }
        });

        // Check if server is up
        getServerManager().getMyServer().getScheduler().schedule(new Runnable() {
            @Override
            public void run() {
                if (process != null) {
                    if (!serverPing()) {
                        getServerManager().getMyServer().getScheduler().schedule(this, 2, TimeUnit.SECONDS);
                        return;
                    }
                    onServerStart();
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * Return true if the server is up
     */
    protected abstract boolean serverPing();

    /**
     * Called when the server has finished starting
     */
    protected void onServerStart() {
        setStatus(ServerStatus.STARTED);
    }

    /**
     * Handle output from a running task
     *
     * @param output process output
     */
    protected abstract void handleOutput(String output);

    protected void stopServer() throws IOException, InvalidServerException {
        setStatus(ServerStatus.STOPPING);

        // Send Stop Commands
        for (String command : getTemplate().getServerStopCommands()) {
            sendCommand(command);
        }

        // Give it time to shutdown
        getServerManager().getMyServer().getScheduler().schedule(() -> {
            if (process != null) {
                killServer();
            }
        }, getTemplate().getServerStopWait(), TimeUnit.SECONDS);
    }

    protected void killServer() {
        if (process != null) {
            process.destroyForcibly();
        }
    }

    /**
     * Called when the server has finished stopping
     */
    protected void onServerStop() {
        setStatus(ServerStatus.STOPPED);
    }

    @Override
    public void sendCommand(String command) throws InvalidServerException, IOException {
        if (processInput == null) {
            throw new InvalidServerException("Server is not accepting commands.");
        }

        processInput.write(command + "\n");
        processInput.flush();
    }
}
