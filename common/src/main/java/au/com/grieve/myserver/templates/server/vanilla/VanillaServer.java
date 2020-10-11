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

import au.com.grieve.myserver.SimpleTemplater;
import au.com.grieve.myserver.templates.server.Server;
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
import java.util.concurrent.TimeUnit;

@Getter
@ToString(callSuper = true)
public abstract class VanillaServer extends Server {
    public VanillaServer(VanillaTemplate template, Path serverPath) {
        super(template, serverPath);
    }

    @Override
    public VanillaTemplate getTemplate() {
        return (VanillaTemplate) super.getTemplate();
    }

    @Override
    protected SimpleTemplater newTemplater() {
        return super.newTemplater()
                .register("EXECUTABLE", "server.jar");
    }

    /**
     * Prepare Server Files
     * <p>
     * This may include downloading the vanilla server and patching it for IP Forwarding
     */
    protected void prepareServer() throws IOException {
        Path cacheFolder = getTemplate().getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
        Path serverPath = cacheFolder.resolve("servers").resolve("vanilla").resolve(getTemplate().getVersion());
        Files.createDirectories(serverPath);

        // Download the Vanilla Server if we don't already have it cached
        Path vanillaServer = serverPath.resolve("original-server.jar");
        if (!Files.exists(vanillaServer)) {
            downloadServer(vanillaServer.toFile());
        }

        // Patch the file for IP-Forward if we don't already have one cached
        Path patchedServer = serverPath.resolve("patched-server.jar");
        if (!Files.exists(patchedServer)) {
            patchServer(vanillaServer.toFile(), patchedServer.toFile());
        }

        // Copy to our files area
        Path executable = getServerPath().resolve("files").resolve("server.jar");
        if (!Files.exists(executable)) {
            FileUtils.copyFile(patchedServer.toFile(), executable.toFile());
        }
    }

    /**
     * Download original server from Mojang
     *
     * @param serverFile The file to download to
     */
    protected void downloadServer(File serverFile) throws IOException {
        System.err.println("Downloading server to: " + serverFile);
        // Get Vanilla Manifest
        ObjectMapper JsonMapper = new ObjectMapper(new JsonFactory());

        JsonNode manifestRoot = JsonMapper.readTree(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"));
        URL serverManifest = null;
        for (JsonNode n : manifestRoot.get("versions")) {
            if (n.get("id").asText().equalsIgnoreCase(getTemplate().getVersion())) {
                serverManifest = new URL(n.get("url").asText());
            }
        }

        if (serverManifest == null) {
            throw new IOException("Unable to download server version: " + getTemplate().getVersion());
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
        System.err.println("Patching server to: " + patchedFile);
        Path cacheFolder = getTemplate().getTemplateManager().getMyServer().getConfig().getFolderConfig().getCachePath();
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
        Files.createDirectory(tmpDir.resolve("in"));
        Files.createDirectory(tmpDir.resolve("out"));
        FileUtils.copyFile(originalFile, tmpDir.resolve("in").resolve(getTemplate().getVersion() + ".jar").toFile());
        FileUtils.copyFile(vanillacord.toFile(), tmpDir.resolve("vanillacord.jar").toFile());

        // Execute Vanillacord
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", "vanillacord.jar", getTemplate().getVersion())
                .inheritIO()
                .directory(tmpDir.toFile());
        Process process = builder.start();
        try {
            process.waitFor(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Copy patched file
        FileUtils.copyFile(tmpDir.resolve("out").resolve(getTemplate().getVersion() + "-bungee.jar").toFile(), patchedFile);

        // Clean up
        FileUtils.deleteDirectory(tmpDir.toFile());
    }
}
