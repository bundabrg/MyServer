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

package au.com.grieve.myserver.templates.server.paper;

import au.com.grieve.myserver.TemplateManager;
import au.com.grieve.myserver.api.templates.server.IServer;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.server.ServerTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A Spigot Server Template
 */
@Getter
@ToString(callSuper = true)
public abstract class PaperTemplate extends ServerTemplate {
    private final String version;
    private final URL url;
    private CompletableFuture<Boolean> updateServerFuture;

    /**
     * Load Server Template from a JsonNode
     *
     * @param templatePath The template path
     */
    public PaperTemplate(TemplateManager templateManager, Path templatePath) throws NoSuchTemplateException, InvalidTemplateException, IOException {
        super(templateManager, templatePath);

        String version = null;
        String url = null;
        for (JsonNode n : getAllNodes()) {
            if (n.has("paper")) {
                JsonNode serverNode = n.get("paper");
                if (version == null && serverNode.has("version")) {
                    version = serverNode.get("version").asText();
                }

                if (url == null && serverNode.has("url")) {
                    url = serverNode.get("url").asText();
                }
            }
        }

        if (version == null) {
            throw new InvalidTemplateException("Missing field: paper.version");
        }
        this.version = version;

        if (url == null) {
            throw new InvalidTemplateException("Missing field: paper.url");
        }
        this.url = new URL(url);
    }

    /**
     * Prepare Server Files
     * <p>
     * This may include downloading BuildUtils and updating the server
     */
    @Override
    public void prepareServer(IServer server) throws IOException {
        if (this.updateServerFuture == null) {
            this.updateServerFuture = new CompletableFuture<>();

            getTemplateManager().getMyServer().getScheduler().runAsync(() -> {
                try {
                    updateServerFuture.complete(updateServer());
                } catch (IOException e) {
                    updateServerFuture.complete(false);
                }
            });
        }

        try {
            this.updateServerFuture.get();
            // Copy to our files area (done all the time in case the server is updated)
            Path cacheFolder = getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
            Path cachedServerPath = cacheFolder.resolve("servers").resolve("paper").resolve(getVersion());

            Path executable = server.getServerPath().resolve("files").resolve("server.jar");
            FileUtils.copyFile(cachedServerPath.resolve("paper-" + getVersion() + ".jar").toFile(), executable.toFile());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            this.updateServerFuture = null;
        }
    }

    /**
     * Takes care of downloading and patching files
     *
     * @return true if anything happened
     * @throws IOException on error
     */
    protected boolean updateServer() throws IOException {
        boolean changed = false;

        Path cacheFolder = getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
        Path cachedServerPath = cacheFolder.resolve("servers").resolve("paper").resolve(getVersion());
        Files.createDirectories(cachedServerPath);

        // Download server if it doesn't exists
        // TODO check for minor updates
        Path serverPath = cachedServerPath.resolve("paper-" + getVersion() + ".jar");
        if (!Files.exists(serverPath)) {
            downloadServer(serverPath.toFile());
            changed = true;
        }

        return changed;
    }

    /**
     * Download Paperclip and create the server
     *
     * @param serverFile The file to save to
     */
    protected void downloadServer(File serverFile) throws IOException {
        Path cacheFolder = getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
        Path paperclipFolder = cacheFolder.resolve("paperclip").resolve(getVersion());
        Files.createDirectories(paperclipFolder);

        // Download Paperclip
        FileUtils.copyURLToFile(url, paperclipFolder.resolve("paperclip.jar").toFile());

        // Execute Paperclip
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", "paperclip.jar", "-v")
                .inheritIO()
                .directory(paperclipFolder.toFile());
        Process process = builder.start();
        try {
            process.waitFor(600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Copy patched file
        FileUtils.copyFile(paperclipFolder.resolve("cache").resolve("patched_" + getVersion() + ".jar").toFile(), serverFile);
    }
}
