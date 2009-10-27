

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.StringUtils.isEmpty;

public abstract class Console
{
    //~ Methods ..............................................................................................

    @NotNull public static String getString(@NotNull final String prompt, @Nullable String defValue)
    {
        String result = "";

        while (result.isEmpty()) {
            result = getConsole().readLine(prompt, defValue);

            if (result.isEmpty() && defValue != null) {
                result = defValue;
            }
        }

        return result;
    }

    public static int getInt(@NotNull final String prompt, @Nullable Integer defValue)
    {
        Integer result = null;

        while (result == null) {
            String st = getConsole().readLine(prompt, String.valueOf(defValue));

            if (st.isEmpty() && defValue != null) {
                result = defValue;
            }
            else {
                try {
                    result = Integer.parseInt(st);
                }
                catch (NumberFormatException n) {
                    result = null;
                }
            }
        }

        return result;
    }

    public static void printf(String msg, Object... args)
    {
        System.console().printf(msg, args);
    }

    public static Console getConsole()
    {
        if (instance == null) {
            final java.io.Console console = System.console();

            if (console == null) {
                throw new IllegalStateException("Cannot find a console");
            }

            instance = new DefaultConsole(console);
        }

        return instance;
    }

    public static void setConsole(Console c)
    {
        instance = c;
    }

    protected abstract String readLine(String prompt, String defaultValue);

    //~ Static fields/initializers ...........................................................................

    private static Console instance;

    //~ Inner Classes ........................................................................................

    public static class DefaultConsole
        extends Console
    {
        private java.io.Console javaConsole;

        public DefaultConsole(java.io.Console c)
        {
            javaConsole = c;
        }

        protected String readLine(String prompt, String defValue)
        {
            return javaConsole.readLine(isEmpty(defValue) ? "%s: " : "%s [%s]: ", prompt, defValue);
        }
    }
}
