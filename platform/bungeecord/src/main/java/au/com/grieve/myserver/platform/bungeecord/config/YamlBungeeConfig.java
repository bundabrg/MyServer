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

package au.com.grieve.myserver.platform.bungeecord.config;

import au.com.grieve.myserver.platform.bungeecord.MyServerPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Provide configuration from a YAML file
 */
@Getter
public class YamlBungeeConfig implements BungeeConfig {
    public static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final MyServerPlugin plugin;
    private final File configFile;
    private final JsonNode rootNode;

    public YamlBungeeConfig(MyServerPlugin plugin, File configFile) throws IOException {
        this.plugin = plugin;
        this.configFile = configFile;
        rootNode = MAPPER.readTree(configFile);
    }

    @Override
    public BungeeSection getBungeecord() {
        return new YamlBungeeSection(rootNode.get("bungeecord"));
    }

    @Override
    public BaseFolderSection getFolderConfig() {
        return new YamlFolderSection(rootNode.get("folder"));
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Getter
    @RequiredArgsConstructor
    public class YamlBungeeSection implements BungeeSection {
        private final JsonNode node;

        @Override
        public String getIpListen() {
            return node != null && node.has("ip-listen") ? node.get("ip-listen").asText() : "0.0.0.0";
        }

        @Override
        public int getPortStart() {
            return node != null && node.has("port-start") ? node.get("port-start").asInt() : 20000;
        }

        @Override
        public int getPortAmount() {
            return node != null && node.has("port-amount") ? node.get("port-amount").asInt() : 1000;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public class YamlFolderSection implements BaseFolderSection {
        private final JsonNode node;

        @Override
        public Path getTemplatePath() {
            return plugin.getDataFolder().toPath().resolve(node.has("templates") ? node.get("templates").asText() : "templates");
        }

        @Override
        public Path getCachePath() {
            return plugin.getDataFolder().toPath().resolve(node.has("cache") ? node.get("cache").asText() : "cache");
        }

        @Override
        public Path getServersPath() {
            return plugin.getDataFolder().toPath().resolve(node.has("servers") ? node.get("servers").asText() : "servers");
        }
    }
}