
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

package apb.utils;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import apb.Environment;
import apb.Command;
//
// User: emilio
// Date: Dec 4, 2008
// Time: 4:40:10 PM

public class SimpleFormatter
    extends Formatter
{
    //~ Instance fields ......................................................................................

    private final Environment env;
    private boolean beginOfLine;

    //~ Constructors .........................................................................................

    public SimpleFormatter(Environment env)
    {
        this.env = env;
        beginOfLine = true;
    }

    //~ Methods ..............................................................................................

    public String format(LogRecord record)
    {
        String str = formatMsg(record);
        StringBuilder result = new StringBuilder(str.length() + 3 * HEADER_LENGTH);

        LineSplitter splitter = new LineSplitter(str);

        while (splitter.nextLine()) {
            if (beginOfLine) {
                appendHeader(result);
            }
            beginOfLine = splitter.appendLine(result);
        }

        return result.toString();
    }

    protected void appendHeader(StringBuilder result)
    {
        final String current = env.getCurrentName();
        if (!current.isEmpty()) {
            int n = result.length();
            result.append('[');
            result.append(current);

            final Command cmd = env.getCurrentCommand();
            if (cmd != null) {
                result.append('.');
                result.append(cmd.getQName());
            }

            int maxLength = HEADER_LENGTH + n;

            if (result.length() > maxLength) {
                result.setLength(maxLength);
            }

            result.append(']');
            n = result.length() - n;

            for (int i = HEADER_LENGTH + 1 - n; i >= 0; i--) {
                result.append(' ');
            }
        }
    }

    private static String formatMsg(LogRecord record)
    {
        String         msg = record.getMessage();
        final Object[] pars = record.getParameters();

        if (pars.length > 0) {
            msg = String.format(msg, pars);
        }

        return msg;
    }

    //~ Static fields/initializers ...........................................................................

    private static final int HEADER_LENGTH = 30;
    private static final String      ls = System.getProperty("line.separator");

    //~ Inner Classes ........................................................................................

    /**
     * Splits a string by new-line characters
     */
    private static final class LineSplitter
    {
        private int length;
        private int nextChar;

        private int          start;
        private final char[] str;

        LineSplitter(String str)
        {
            int    len = str.length();
            char[] chars = new char[len];
            str.getChars(0, len, chars, 0);
            this.str = chars;
        }

        boolean appendLine(StringBuilder sb)
        {
            sb.append(str, start, length);
            // Append ls?
            final boolean eol = nextChar > start + length;
            if (eol)
                sb.append(ls);
            return eol;
        }

        boolean nextLine()
        {
            int    i = nextChar;
            char[] chars = str;
            int    len = chars.length;

            if (i >= len) {
                return false;
            }

            int offset = i;

            char c;

            do {
                c = chars[i];
                i++;
            }
            while (i < len && c != '\n' && (c != '\r' || chars[i] == '\n'));

            nextChar = i;

            // We do not want to include \r \n chars
            int end = c == '\r' ? i - 1 : c != '\n' ? i : i >= 2 && chars[i - 2] == '\r' ? i - 2 : i - 1;

            start = offset;
            length = end - offset;

            return true;
        }
    }
}
