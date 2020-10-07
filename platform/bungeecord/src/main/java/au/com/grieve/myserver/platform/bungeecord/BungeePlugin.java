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

package au.com.grieve.myserver.platform.bungeecord;

import au.com.grieve.bcf.platform.bungeecord.BungeeCommandManager;
import au.com.grieve.myserver.platform.bungeecord.commands.AdminCommand;
import au.com.grieve.myserver.platform.bungeecord.commands.AdminServerCommand;
import au.com.grieve.myserver.platform.bungeecord.commands.AdminTemplateCommand;
import au.com.grieve.myserver.platform.bungeecord.commands.MyServerCommand;
import au.com.grieve.myserver.platform.bungeecord.config.BungeeConfig;
import au.com.grieve.myserver.platform.bungeecord.config.YamlBungeeConfig;
import au.com.grieve.myserver.platform.bungeecord.parsers.MSServer;
import au.com.grieve.myserver.platform.bungeecord.parsers.MSTemplate;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

@Getter
public class BungeePlugin extends Plugin {
    public static BungeePlugin INSTANCE;

    private BungeeCommandManager bcf;
    private BungeeMyServer myServer;
    private BungeeConfig config;

    @Override
    public void onEnable() {
        super.onEnable();

        INSTANCE = this;

        // Load Config
        config = loadConfiguration();

        bcf = new BungeeCommandManager(this);

        // Register Commands
        bcf.registerCommand(MyServerCommand.class);
        bcf.registerCommand(AdminCommand.class);
        bcf.registerCommand(AdminServerCommand.class);
        bcf.registerCommand(AdminTemplateCommand.class);

        // Register Parsers
        bcf.registerParser("MSTemplate", MSTemplate.class);
        bcf.registerParser("MSServer", MSServer.class);

        // Register Server Manager
        myServer = new BungeeMyServer(config, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    protected BungeeConfig loadConfiguration() {
        // Try load from config.yml else we provide a default configuration file
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            getLogger().info("Creating new default config.yml");
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();

            try (InputStream fis = getResourceAsStream("config.yml")) {
                Files.copy(
                        fis,
                        configFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            return new YamlBungeeConfig(this, configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to load config.yml", e);
            throw new RuntimeException(e);
        }
    }
}
