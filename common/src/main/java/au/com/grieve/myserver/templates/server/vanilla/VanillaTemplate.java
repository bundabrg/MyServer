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
import au.com.grieve.myserver.api.templates.server.IServer;
import au.com.grieve.myserver.exceptions.InvalidTemplateException;
import au.com.grieve.myserver.exceptions.NoSuchTemplateException;
import au.com.grieve.myserver.templates.server.ServerTemplate;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * A Vanilla Server Template
 * <p>
 * A Vanilla server is able to download and patch a vanilla server ready to run behind bungeecord
 */
@Getter
@ToString(callSuper = true)
public abstract class VanillaTemplate extends ServerTemplate {
    private final String version;
    private CompletableFuture<Boolean> updateServerFuture;

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

    /**
     * Prepare Server Files
     * <p>
     * This may include downloading the vanilla server and patching it for IP Forwarding
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
            if (this.updateServerFuture.get()) {
                // Copy to our files area (done all the time in case the server is updated)
                Path cacheFolder = getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
                Path cachedServerPath = cacheFolder.resolve("servers").resolve("vanilla").resolve(getVersion());

                Path executable = server.getServerPath().resolve("files").resolve("server.jar");
                FileUtils.copyFile(cachedServerPath.resolve("patched-server.jar").toFile(), executable.toFile());
            }
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
        Path cachedServerPath = cacheFolder.resolve("servers").resolve("vanilla").resolve(getVersion());
        Files.createDirectories(cachedServerPath);

        // Download the Vanilla Server if we don't already have it cached
        // TODO check that there are no minor updates
        Path vanillaServer = cachedServerPath.resolve("original-server.jar");
        if (!Files.exists(vanillaServer)) {
            downloadServer(vanillaServer.toFile());
            changed = true;
        }

        // Patch the file for IP-Forward if we don't already have one cached
        Path patchedServer = cachedServerPath.resolve("patched-server.jar");
        if (!Files.exists(patchedServer)) {
            patchServer(vanillaServer.toFile(), patchedServer.toFile());
            changed = true;
        }
        return changed;
    }

    /**
     * Download original server from Mojang
     *
     * @param serverFile The file to download to
     */
    protected void downloadServer(File serverFile) throws IOException {
        // Get Vanilla Manifest
        ObjectMapper JsonMapper = new ObjectMapper(new JsonFactory());

        JsonNode manifestRoot = JsonMapper.readTree(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"));
        URL serverManifest = null;
        for (JsonNode n : manifestRoot.get("versions")) {
            if (n.get("id").asText().equalsIgnoreCase(getVersion())) {
                serverManifest = new URL(n.get("url").asText());
            }
        }

        if (serverManifest == null) {
            throw new IOException("Unable to download server version: " + getVersion());
        }

        JsonNode serverManifestRoot = JsonMapper.readTree(serverManifest);

        // Download File
        FileUtils.copyURLToFile(new URL(serverManifestRoot.get("downloads").get("server").get("url").asText()), serverFile);
    }

    /**
     * Patch originalFile to allow IP-Forwarding
     * <p>
     * This makes use of the excellent VanillaCord
     *
     * @param originalFile The original file
     * @param patchedFile  The patched file
     */
    protected void patchServer(File originalFile, File patchedFile) throws IOException {
        Path cacheFolder = getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
        Path vanillacordPath = cacheFolder.resolve("vanillacord");
        Files.createDirectories(vanillacordPath);

        Path vanillacord = vanillacordPath.resolve("VanillaCord-1.12.jar");

        // Download Vanillacord if needed
        if (!Files.exists(vanillacord)) {
            // Get Vanillacord Manifest
            ObjectMapper JsonMapper = new ObjectMapper(new JsonFactory());

            JsonNode manifestRoot = JsonMapper.readTree(new URL("https://raw.githubusercontent.com/ME1312/VanillaCord/1.12/profile.json"));

            FileUtils.copyURLToFile(new URL(manifestRoot.get("download").get("url").asText()), vanillacord.toFile());
        }

        // Copy files to a Temporary directory
        Path tmpDir = Files.createTempDirectory("vanilla");
        try {
            Files.createDirectory(tmpDir.resolve("in"));
            Files.createDirectory(tmpDir.resolve("out"));
            FileUtils.copyFile(originalFile, tmpDir.resolve("in").resolve(getVersion() + ".jar").toFile());
            FileUtils.copyFile(vanillacord.toFile(), tmpDir.resolve("vanillacord.jar").toFile());

            // Execute Vanillacord
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", "vanillacord.jar", getVersion())
                    .inheritIO()
                    .directory(tmpDir.toFile());
            Process process = builder.start();
            try {
                process.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Copy patched file
            FileUtils.copyFile(tmpDir.resolve("out").resolve(getVersion() + "-bungee.jar").toFile(), patchedFile);
        } finally {
            // Clean up
            FileUtils.deleteDirectory(tmpDir.toFile());
        }
    }

}
