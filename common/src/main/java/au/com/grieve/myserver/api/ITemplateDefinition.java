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

import au.com.grieve.myserver.api.templates.ITemplate;

/**
 * Definition for a template
 */
public interface ITemplateDefinition {
    /**
     * Return the full template name
     */
    String getFullName();

    /**
     * Return the type of the template
     *
     * @return the type portion of the template full name
     */
    String getType();

    /**
     * Return the name portion of the template
     *
     * @return name portion of the template full name
     */
    String getName();

    /**
     * Return the version portion of the template
     *
     * @return version portion of template full name
     */
    String getVersion();

    /**
     * Load the template as a specific class
     *
     * @return loaded template
     */
    <T extends ITemplate> T loadTemplate(Class<T> templateClass);
}
