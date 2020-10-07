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

import java.nio.file.Path;

public interface BaseConfig {

    /**
     * Return configuration for folders
     *
     * @return Folder section config
     */
    BaseFolderSection getFolderConfig();

    interface BaseFolderSection {
        /**
         * Return the path of the templates.
         * <p>
         * All folders underneath this path will be enumerated for templates
         *
         * @return path of Templates
         */
        Path getTemplatePath();

        /**
         * Return path of cache folder
         * <p>
         * Cached files will be stored under this folder
         *
         * @return path of Cache folder
         */
        Path getCachePath();

        /**
         * Return the path of created server instances
         *
         * @return path of Servers folder
         */
        Path getServersPath();
    }

}
