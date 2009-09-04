

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
//
// User: emilio
// Date: Sep 1, 2009
// Time: 3:10:01 PM

/**
     * Splits a string by new-line characters
 */
public final class LineSplitter
{
    //~ Instance fields ......................................................................................

    private final char[] str;

    private int length;
    private int nextChar;

    private int start;

    //~ Constructors .........................................................................................

    public LineSplitter(String str)
    {
        int    len = str.length();
        char[] chars = new char[len];
        str.getChars(0, len, chars, 0);
        this.str = chars;
    }

    //~ Methods ..............................................................................................

    public boolean appendLine(StringBuilder sb)
    {
        sb.append(str, start, length);

        // Append ls?
        final boolean eol = nextChar > start + length;

        if (eol) {
            sb.append(LINE_SEPARATOR);
        }

        return eol;
    }

    public boolean nextLine()
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

    //~ Static fields/initializers ...........................................................................

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
}
