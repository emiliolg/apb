

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
// Date: Jul 29, 2009
// Time: 5:46:12 PM

//
public class ColorUtils
{
    //~ Methods ..............................................................................................

    public static String trimColors(String str)
    {
        if (str.indexOf(ANSI_ESCAPE) == -1) {
            return str;
        }

        boolean       skip = false;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (skip) {
                skip = c != ANSI_END;
            }
            else if (c == ANSI_ESCAPE) {
                skip = true;
            }
            else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static String colorize(String color, String str)
    {
        return color + str + RESET;
    }

    //~ Static fields/initializers ...........................................................................

    public static final String GREEN = "\033[32m";
    public static final String RED = "\033[31m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String RESET = "\033[0m";

    private static final char ANSI_ESCAPE = '\033';
    private static final char ANSI_END = 'm';
}
