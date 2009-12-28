

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


package apb.compiler;

import java.lang.reflect.InvocationTargetException;

import javax.tools.Diagnostic;

import apb.utils.ClassUtils;
import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.ColorUtils.*;
import static apb.utils.StringUtils.nChars;

/**
 * Formats a Compiler message, based on a Diagnostic
 * Some dragons here...
 * It tries to use the internal JCDiagnostic class from the JDK through reflection
 * If it fails it extracts the message from the String
 */

class DiagnosticFormatter
{
    //~ Instance fields ......................................................................................

    /**
     * If true the Diagnostic object is a JCDiagnostic class from the JDK
     * then we can call its methods: getPrefix() and getText()
     */
    private boolean isJCDiagnostic;

    /**
     * The diagnostic to format
     */
    @NotNull private final Diagnostic diagnostic;

    //~ Constructors .........................................................................................

    /**
     * Construct a DiagnosticFormatter
     * @param d The Diagnostic to format
     */
    DiagnosticFormatter(@NotNull Diagnostic d)
    {
        diagnostic = d;
        isJCDiagnostic = diagnostic.getClass().getName().endsWith(".JCDiagnostic");
    }

    //~ Methods ..............................................................................................

    /**
     * Returns a String with the formatted Diagnostic info
     * @return the formatted Diagnostic info
     */

    @NotNull String format()
    {
        StringBuilder msg = new StringBuilder(nChars(8, ' '));
        msg.append(prefix());
        msg.append('(');
        msg.append(diagnostic.getLineNumber());
        msg.append(',');
        msg.append(diagnostic.getColumnNumber());
        msg.append(") ");

        return StringUtils.appendIndenting(msg.toString(), text());
    }

    /**
     * Returns a text with the description of the diagnostic
     * It tries to invoke the 'getText' method of the JCDiagnostic object
     * if it fails it invokes the 'toString' method and applies a regular expression pattern
     * to extract the description of the diagnostic
     * @return a text with the description of the diagnostic
     */
    @NotNull private String text()
    {
        String result = invoke("getText");

        if (result == null) {
            String s = diagnostic.toString();

            // This regular expression tries to skip the file name and line number plus an optional 'warning' string
            result = s.replaceFirst(".*:[0-9]+: (warning: )?", "");
        }

        return result;
    }

    /**
     * Returns the 'prefix' of the diagnostic
     * The prefix contains a text description of the diagnostic type
     * It tries to invoke the 'getPrefix' method of the JCDiagnostic object
     * if it fails it creates the description based on the diagnostic kind.
     * @return a description ot the diagnostic type
     */
    @NotNull private String prefix()
    {
        String result = invoke("getPrefix");

        if (result != null) {
            result = result.substring(0, 1).toUpperCase() + result.substring(1);
        }
        else {
            switch (diagnostic.getKind()) {
            case NOTE:
                result = colorize(BLUE, "Note");
                break;
            case MANDATORY_WARNING:
            case WARNING:
                result = colorize(YELLOW, "Warning");
                break;
            case ERROR:
                result = colorize(RED, "Error");
                break;
            default:
                result = "";
                break;
            }

            result += ": ";
        }

        return result;
    }

    /**
     * Invoke an instance method over the diagnostic object if the object is a JCDiagnostic one
     * If the invocation fails it sets isJCDiagnostic to false to avoid further invocations
     * It returns null if the invocation fails
     * @param method The name of the method to invoke
     * @return The result of the invocation or null if the object is not a JCDiagnostic or the invocations fails
     */
    @Nullable private String invoke(@NotNull String method)
    {
        if (!isJCDiagnostic) {
            try {
                return (String) ClassUtils.invoke(diagnostic, method);
            }
            catch (NoSuchMethodException e) {
                isJCDiagnostic = false;
            }
            catch (InvocationTargetException e) {
                isJCDiagnostic = false;
            }
            catch (IllegalAccessException e) {
                isJCDiagnostic = false;
            }
        }

        return null;
    }
}
