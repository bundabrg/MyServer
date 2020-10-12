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

package au.com.grieve.myserver.api.templates.server;

import au.com.grieve.myserver.api.templates.ITagsTemplate;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.templates.server.Server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IServerTemplate extends ITagsTemplate {
    Server createServer(String name) throws InvalidServerException, IOException;

    Server loadServer(Path serverPath) throws InvalidServerException, IOException;

    /**
     * Create a new Server instance
     *
     * @return Server Instance
     */
    Server newServer(Path serverPath);

    String getServerStartExecute();

    List<String> getServerStartCommands();

    int getServerStartDelay();

    List<String> getServerStopCommands();

    int getServerStopWait();

    void prepareServer(IServer server) throws IOException;
}