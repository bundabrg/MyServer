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

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
@ToString
public class TagDefinition {
    private final String name;
    private final String description;
    private final TypeEnum type;
    private final String defaultValue;
    private final String permission;
    private final boolean required;
    private final List<String> choices = new ArrayList<>();

    /**
     * Validate that input matches our rules
     *
     * @param input The input to check
     * @return true if it is valid input
     */
    public boolean validate(String input) {
        switch (type) {
            case NULL:
                return false;
            case STRING:
                return true;
            case INT:
                try {
                    Integer.valueOf(input);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case BOOLEAN:
                return (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false"));
            case CHOICE:
                return choices.stream()
                        .anyMatch(c -> c.equalsIgnoreCase(input));
        }
        return false;
    }

    public List<String> options() {
        switch (type) {
            case BOOLEAN:
                List<String> result = new ArrayList<>();
                result.add("true");
                result.add("false");
                return result;
            case CHOICE:
                return choices;
        }

        return Collections.emptyList();
    }
}