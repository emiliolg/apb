

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

import java.io.File;
import java.lang.reflect.Member;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.*;

import static apb.utils.StringUtils.isEmpty;
import static apb.utils.StringUtils.isNotEmpty;
//
// User: emilio
// Date: Sep 9, 2008
// Time: 5:45:50 PM

//
public class NameUtils
{
    //~ Methods ..............................................................................................

    @NotNull public static String idFromMember(@NotNull Member member)
    {
        return idFromJavaId(member.getName());
    }

    @NotNull public static String idFromClass(@NotNull Class<?> clazz)
    {
        return idFromJavaId(name(clazz));
    }

    public static String idFromJavaId(String name)
    {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            final char chr = name.charAt(i);

            if (i > 0 && isUpperCase(chr) && isLowerCase(name.charAt(i - 1))) {
                result.append('-');
            }

            if (chr == '_' || chr == '$') {
                result.append('-');
            }
            else {
                result.append(toLowerCase(chr));
            }
        }

        return result.toString();
    }

    @NotNull public static String name(@NotNull Class clazz)
    {
        return clazz.getCanonicalName();
    }

    @NotNull public static String packageName(@NotNull Class clazz)
    {
        return clazz.isArray() ? packageName(clazz.getComponentType()) : packageName(clazz.getName());
    }

    @NotNull public static String dirFromId(@NotNull String id)
    {
        return id.replace('.', File.separatorChar);
    }

    @NotNull public static String idFromDir(@NotNull String dir)
    {
        return dir.replace(File.separatorChar, '.');
    }

    @NotNull public static String packageName(@NotNull String className)
    {
        int dot = className.lastIndexOf('.');
        className = dot == -1 ? "" : className.substring(0, dot);
        return className;
    }

    @NotNull public static String simpleName(@NotNull String className)
    {
        int dot = className.lastIndexOf('.');
        className = dot == -1 ? className : className.substring(dot + 1);
        int semicolon = className.lastIndexOf(';');
        className = semicolon == -1 ? className : className.substring(0, semicolon);
        return className;
    }

    @NotNull public static String simpleName(@NotNull Class clazz)
    {
        return simpleName(clazz.getCanonicalName());
    }

    /**
     * Verify if it is a valid qualified class name
     * @param className The className  to be validated
     * @return true if valid false otherwise
     */
    public static boolean isValidQualifiedClassName(String className)
    {
        return isNotEmpty(className) && isValidPackageName(packageName(className)) &&
               isValidSimpleClassName(simpleName(className));
    }

    /**
     * Verify if it is a valid simple (not qualified) class name
     * @param className The className  to be validated
     * @return true if valid false otherwise
     */
    public static boolean isValidSimpleClassName(String className)
    {
        return isValidJavaId(className) && isUpperCase(className.charAt(0));
    }

    /**
     * Verify if it is a valid package name
     * @param packageName The packageName  to be validated
     * @return true if valid false otherwise
     */
    public static boolean isValidPackageName(String packageName)
    {
        if (packageName == null) {
            return false;
        }

        if (packageName.isEmpty()) {
            return true;
        }

        int dot = packageName.indexOf('.');

        while (dot != -1) {
            if (!isValidJavaId(packageName.substring(0, dot)) || isUpperCase(packageName.charAt(0))) {
                return false;
            }

            packageName = packageName.substring(dot + 1);
            dot = packageName.indexOf('.');
        }

        return isValidJavaId(packageName) && isLowerCase(packageName.charAt(0));
    }

    /**
     * Verify if it is a valid java identifier
     * @param id The id  to be validated
     * @return true if valid false otherwise
     */
    public static boolean isValidJavaId(String id)
    {
        if (isEmpty(id) || !Character.isJavaIdentifierStart(id.charAt(0))) {
            return false;
        }

        for (int i = 1; i < id.length(); i++) {
            if (!Character.isJavaIdentifierPart(id.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
