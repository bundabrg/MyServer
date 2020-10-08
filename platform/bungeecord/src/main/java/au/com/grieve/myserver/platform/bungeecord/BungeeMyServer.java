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

import au.com.grieve.myserver.MyServer;
import au.com.grieve.myserver.ServerManager;
import au.com.grieve.myserver.platform.bungeecord.config.BungeeConfig;
import au.com.grieve.myserver.platform.bungeecord.templates.server.BungeeVanillaServerTemplate;
import au.com.grieve.myserver.templates.definition.DefinitionTemplate;
import lombok.Getter;

@Getter
public class BungeeMyServer extends MyServer {
    private final BungeePlugin plugin;

    public BungeeMyServer(BungeeConfig config, BungeePlugin plugin) {
        super(config);
        this.plugin = plugin;

        // Register BuiltIn Template Types
        registerBuiltinTemplateTypes();
    }

    protected void registerBuiltinTemplateTypes() {
        getTemplateManager()
                .registerTemplateType("def", DefinitionTemplate.class)
                .registerTemplateType("server/vanilla/1.0", BungeeVanillaServerTemplate.class);
    }

    @Override
    protected ServerManager createServerManager() {
        return new BungeeServerManager(this);
    }

    @Override
    public BungeeServerManager getServerManager() {
        return (BungeeServerManager) super.getServerManager();
    }
}
