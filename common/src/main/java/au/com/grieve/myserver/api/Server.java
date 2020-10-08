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

package au.com.grieve.myserver.api;

import au.com.grieve.myserver.exceptions.InvalidServerException;
import au.com.grieve.myserver.templates.Template;

import java.io.IOException;
import java.util.UUID;

public interface Server {
    /**
     * Return Server's UUID
     *
     * @return server uuid
     */
    UUID getUuid();

    /**
     * Return Server's Name
     *
     * @return server name
     */
    String getName();

    /**
     * Save Server
     */
    Server save() throws IOException;

    /**
     * Load Server
     */
    Server load() throws InvalidServerException, IOException;

    /**
     * Get Server Template
     */
    Template getTemplate();

    /**
     * Destroy a Server
     */
    void destroy() throws IOException;
}