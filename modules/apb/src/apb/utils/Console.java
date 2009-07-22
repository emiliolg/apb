

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

public class Console
{
    //~ Methods ..............................................................................................

    @NotNull public static String getString(@NotNull final String prompt, @Nullable String defValue)
    {
        String result = "";

        while (result.isEmpty()) {
            result = getConsole().readLine(isEmpty(defValue) ? "%s: " : "%s [%s]: ", prompt, defValue);

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
            String st = getConsole().readLine(defValue == null ? "%s: " : "%s [%d]: ", prompt, defValue);

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

    public static void main(String[] args)
    {
        String s = getString("Entre una opcion", "");
        System.out.println("s = " + s);
        s = getString("Entre una opcion", "pepe");
        System.out.println("s = " + s);
        int n = getInt("Entre una opcion", 9);
        System.out.println("n = " + n);
    }

    public static void printf(String msg, Object... args)
    {
        System.console().printf(msg, args);
    }

    private static java.io.Console getConsole()
    {
        final java.io.Console console = System.console();

        if (console == null) {
            throw new IllegalStateException("Cannot find a console");
        }

        return console;
    }
}
