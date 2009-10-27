

// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License
//


package apb;

import apb.utils.ColorUtils;
import apb.utils.LineSplitter;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 1, 2009
// Time: 3:36:26 PM

/**
 * A logger that emmits the output to the standard output
 * @exclude
 */
public class StandaloneLogger
    implements Logger
{
    //~ Instance fields ......................................................................................

    private boolean beginOfLine = true;
    private boolean color = true;
    private Level   minLevel = Level.INFO;

    //~ Methods ..............................................................................................

    public void log(@NotNull Level level, @NotNull String msg, Object... args)
    {
        if (level.compareTo(minLevel) >= 0) {
            String str = format(args == null || args.length == 0 ? msg : String.format(msg, args));
            System.out.print(str);
        }
    }

    public String format(String msg)
    {
        String        str = trimColors(msg);
        StringBuilder result = new StringBuilder();

        LineSplitter splitter = new LineSplitter(str);

        while (splitter.nextLine()) {
            if (beginOfLine) {
                result.append(header());
            }

            beginOfLine = splitter.appendLine(result);
        }

        return result.toString();
    }

    public void setColor(boolean b)
    {
        color = b;
    }

    public void setLevel(@NotNull Level level)
    {
        minLevel = level;
    }

    protected String trimColors(String str)
    {
        return color ? str : ColorUtils.trimColors(str);
    }

    protected String header()
    {
        final String str = Apb.makeStandardHeader();
        return color && !str.isEmpty() ? ColorUtils.colorize(ColorUtils.GREEN, str) : str;
    }
}
