

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
import static apb.utils.StringUtils.nChars;

/**
 * Format a Compiler message, based on a Diagnostic
 * Some dragons here...
 * It tries to use the internal JCDiagnostic class from the JDK thorugh reflection
 * If it fails it extract the message from the String
 */
//
public class DiagnosticFormat
{
    //~ Instance fields ......................................................................................

    private Diagnostic diagnostic;
    private boolean    isJCDiagnostic;

    //~ Constructors .........................................................................................

    DiagnosticFormat(Diagnostic d)
    {
        diagnostic = d;
        isJCDiagnostic = diagnostic.getClass().getName().endsWith(".JCDiagnostic");
    }

    //~ Methods ..............................................................................................

    static String getPrefix(Diagnostic d)
    {
        switch (d.getKind()) {
        case NOTE:
            return "Note";
        case MANDATORY_WARNING:
        case WARNING:
            return "Warning";
        case ERROR:
            return "Error";
        default:
            return "";
        }
    }

    static void appendLines(StringBuilder result, String msg)
    {
        String indent = nChars(result.length(), ' ');
        int    nl;

        while ((nl = msg.indexOf('\n')) >= 0) {
            result.append(msg.substring(0, ++nl));
            msg = msg.substring(nl);
            result.append(indent);
        }

        if (!msg.isEmpty()) {
            result.append(msg);
        }
    }

    String format()
    {
        StringBuilder msg = new StringBuilder(nChars(8, ' '));
        msg.append(prefix());
        msg.append('(');
        msg.append(diagnostic.getLineNumber());
        msg.append(',');
        msg.append(diagnostic.getColumnNumber());
        msg.append(") ");
        appendLines(msg, text());
        return msg.toString();
    }

    private String text()
    {
        String result = null;

        if (isJCDiagnostic) {
            result = invoke("getText");
        }

        if (result == null) {
            String s = diagnostic.toString();
            result = s.replaceFirst(".*:[0-9]+: (warning: )?", "");
        }

        return result;
    }

    private String prefix()
    {
        String result = null;

        if (isJCDiagnostic) {
            result = invoke("getPrefix");
        }
        if (result != null) {
            result = result.substring(0, 1).toUpperCase() + result.substring(1);
        }
        else {

            switch (diagnostic.getKind()) {
            case NOTE:
                result = "Note"; break;
            case MANDATORY_WARNING:
            case WARNING:
                result = "Warning"; break;
            case ERROR:
                result = "Error"; break;
            default:
                result = ""; break;
            }

            result += ": --";
        }
        return result;
    }

    private String invoke(String method)
    {
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

        return null;
    }

}
