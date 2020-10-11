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

import au.com.grieve.myserver.ServerManager;
import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.templates.server.ServerTemplate;

import java.io.IOException;
import java.util.Map;

public interface IServer {
    IServer load() throws InvalidServerException, IOException;

    IServer save() throws IOException;

    String getTag(String name);

    String getTag(String name, String defaultValue);

    Map<String, String> getAllTags();

    void setTag(String name, String value);

    void resetTag(String name);

    void start() throws InvalidServerException, IOException;

    void stop() throws InvalidServerException, IOException;

    void destroy() throws IOException;

    ServerManager getServerManager();

    ServerTemplate getTemplate();

    java.nio.file.Path getServerPath();

    Map<String, java.util.List<String>> getPermissions();

    Map<String, String> getTags();

    String getServerIp();

    void setServerIp(String serverIp);

    Integer getServerPort();

    void setServerPort(Integer serverPort);

    String getName();

    void setName(String name);

    java.util.UUID getUuid();

    void setUuid(java.util.UUID uuid);

    /**
     * Send a command to a running server
     *
     * @param command command to send
     */
    void sendCommand(String command) throws InvalidServerException, IOException;
}
