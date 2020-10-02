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
import au.com.grieve.myserver.platform.bungeecord.commands.MyServerCommand;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

public class MyServerBungeePlugin extends Plugin {
    @Getter
    private BungeeCommandManager bcf;

    @Override
    public void onEnable() {
        super.onEnable();

        bcf = new BungeeCommandManager(this);

        // Register Commands
        bcf.registerCommand(MyServerCommand.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
