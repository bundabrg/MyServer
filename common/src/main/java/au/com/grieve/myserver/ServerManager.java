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

import au.com.grieve.myserver.api.templates.server.IServer;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchServerException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.server.Server;
import au.com.grieve.myserver.templates.server.ServerTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.MapMaker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class ServerManager {
    public static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private CompletableFuture<List<IServer>> getServersFuture;

    // Weak references to Servers (for since instance purposes)
    private final ConcurrentMap<UUID, IServer> serverCache = new MapMaker()
            .weakValues()
            .makeMap();

    // Strong references to Servers
    private final ConcurrentMap<UUID, IServer> serverInstances = new MapMaker()
            .makeMap();

    private final MyServer myServer;

    /**
     * Return true if the server with name exists
     *
     * @param name Name of server
     * @return true if exists
     */
    public boolean hasServer(String name) {
        return getServers().stream()
                .anyMatch(s -> s.getName().equals(name));
    }

    public boolean hasServer(UUID uuid) {
        return getServers().stream()
                .anyMatch(s -> s.getUuid().equals(uuid));
    }

    public IServer getServer(String name) throws NoSuchServerException {
        return getServers().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchServerException("No such server name: " + name));
    }

    public IServer getServer(UUID uuid) throws NoSuchServerException {
        return getServers().stream()
                .filter(s -> s.getUuid().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new NoSuchServerException("No such server uuid: " + uuid.toString()));
    }

    /**
     * Enumerate all servers
     *
     * @return list of servers
     */
    public List<IServer> getServers() {
        if (getServersFuture == null) {
            CompletableFuture<List<IServer>> getServersFuture = new CompletableFuture<>();
            this.getServersFuture = getServersFuture;
            getMyServer().getScheduler().runAsync(() -> {
                List<IServer> result = new ArrayList<>();

                if (!myServer.getConfig().getFolderConfig().getServersPath().toFile().exists()) {
                    getServersFuture.complete(result);
                    return;
                }

                try (Stream<Path> walk = Files.walk(myServer.getConfig().getFolderConfig().getServersPath(), 5)) {
                    Path lastPath = null;
                    for (Path path : walk
                            .filter(Files::isDirectory)
                            .filter(p -> p.resolve("server.yml").toFile().exists())
                            .collect(Collectors.toList())) {

                        // If a server.yml file is found in a directory then prune out all child directories
                        if (lastPath == null || !path.startsWith(lastPath)) {
                            lastPath = path;

                            try {
                                result.add(loadServer(path));
                            } catch (IOException | NoSuchTemplateException | NoSuchServerException | InvalidServerException | InvalidTemplateException ignored) {
                            }
                        }
                    }

                } catch (IOException ignored) {
                }
                getServersFuture.complete(result);
            });
        }

        List<IServer> result = new ArrayList<>();
        try {
            result.addAll(getServersFuture.get());
        } catch (ExecutionException | InterruptedException ignored) {
        }
        getServersFuture = null;
        return result;
    }

    /**
     * Load a server from a stream
     *
     * @param serverPath Path to server folder
     */
    public IServer loadServer(Path serverPath) throws IOException, NoSuchTemplateException, NoSuchServerException, InvalidServerException, InvalidTemplateException {
        JsonNode rootNode = MAPPER.readTree(serverPath.resolve("server.yml").toFile());

        if (!rootNode.has("uuid")) {
            throw new NoSuchServerException("Can't find field 'uuid'");
        }

        UUID uuid = UUID.fromString(rootNode.get("uuid").asText());

        // Check if we already have this server in use and return that instead
        if (serverCache.containsKey(uuid)) {
            // TODO update instance with new data maybe
            return serverCache.get(uuid);
        }

        if (!rootNode.has("template")) {
            throw new IOException("Failed to find a template field");
        }

        String templateName = rootNode.get("template").asText();

        ServerTemplate template = getMyServer().getTemplateManager().getTemplate(ServerTemplate.class, templateName);

        Server server = template.loadServer(serverPath);
        serverCache.put(uuid, server);
        return server;
    }

}
