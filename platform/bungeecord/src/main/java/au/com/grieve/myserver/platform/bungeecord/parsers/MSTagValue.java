/*
 * Copyright (c) 2020-2022 Brendan Grieve (bundabrg) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package au.com.grieve.myserver.platform.bungeecord.parsers;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.UnexpectedInputError;
import au.com.grieve.bcf.impl.parser.BaseParser;
import au.com.grieve.myserver.api.TagDefinition;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;

@Getter
@ToString(callSuper = true)
public class MSTagValue extends BaseParser<CommandSender, String> {

  public MSTagValue(Map<String, String> parameters) {
    super(parameters);
  }

  public MSTagValue(String description, String defaultValue, boolean suppress, boolean required,
      String placeholder, List<String> switchValue) {
    super(description, defaultValue, suppress, required, placeholder, switchValue);
  }

  protected TagDefinition getTagDefinition(ParserContext<CommandSender> context) {
    return context.getHistory().allObjects().stream()
        .collect(
            Collectors.collectingAndThen(
                Collectors.toList(),
                l -> {
                  Collections.reverse(l);
                  return l;
                }))
        .stream()
        .filter(o -> o instanceof TagDefinition)
        .map(o -> ((TagDefinition) o))
        .findFirst()
        .orElse(null);
  }

  @Override
  protected String doParse(ParserContext<CommandSender> context, ParsedLine line)
      throws EndOfLineException, ParserSyntaxException {

    String input = line.next();

    TagDefinition definition = getTagDefinition(context);
    if (definition != null) {
      if (definition.validate(input)) {
        return input;
      }
    }
    throw new ParserSyntaxException(line, new UnexpectedInputError()); //TODO
  }

  @Override
  protected void doComplete(ParserContext<CommandSender> context, ParsedLine line,
      List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {
    String input = line.next();

    TagDefinition definition = getTagDefinition(context);
    if (definition != null) {

      CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, getDescription());
      group.getCompletionCandidates()
          .addAll(definition.options().stream()
              .limit(20)
              .map(DefaultCompletionCandidate::new)
              .collect(Collectors.toList()));
      candidates.add(group);
    }
  }
}
