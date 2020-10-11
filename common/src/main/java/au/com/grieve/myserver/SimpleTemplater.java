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

package au.com.grieve.myserver;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We allow strings to be registered along with their values and given an input and output stream we will perform
 * substitutions where the variable is found encased in double brackets like {{variable}}
 */
@RequiredArgsConstructor
public class SimpleTemplater {
    protected final Map<String, String> variables = new HashMap<>();

    public SimpleTemplater register(String key, String value) {
        variables.put(key, value);
        return this;
    }

    public SimpleTemplater register(Map<String, String> tags) {
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            variables.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Replace parameters in a String returning the updated String
     *
     * @param input the original string
     * @return the updated string
     */
    public String replace(String input) {
        Pattern p = Pattern.compile("\\{\\{ *([^} ]+) *}}");

        for (int maxTries = 20; maxTries > 0; maxTries--) {
            Matcher m = p.matcher(input);
            StringBuffer sb = new StringBuffer(input.length());

            boolean found = false;
            while (m.find()) {
                String tag = m.group(1);
                if (!variables.containsKey(tag) || variables.get(tag) == null) {
                    m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                } else {
                    found = true;
                    m.appendReplacement(sb, Matcher.quoteReplacement(variables.get(tag)));
                }
            }

            if (!found) {
                return input;
            }

            m.appendTail(sb);
            input = sb.toString();
        }
        throw new RuntimeException("Too many recursions in Placeholder");
    }

    /**
     * Replace strings in the reader and write to the writer
     *
     * @param in  the input
     * @param out the output
     * @return ourself for chaining
     * @throws IOException IOException
     */
    public SimpleTemplater process(BufferedReader in, BufferedWriter out) throws IOException {
        for (String line; ((line = in.readLine()) != null); ) {
            out.write(replace(line));
            out.newLine();
        }
        return this;
    }
}
