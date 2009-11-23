

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import apb.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Some utility Collection Methods
 */
public class CollectionUtils
{
    //~ Methods ..............................................................................................

    /**
     * Ensures that this collection contains the specified element.
     * But only tries to add it if the element is not null
     * @param collection
     * @param element The elment to be added if not null
     * @param <T> The type of the element that must match the type of the collection
     */
    public static <T> void addIfNotNull(@NotNull Collection<T> collection, @Nullable T element)
    {
        if (element != null) {
            collection.add(element);
        }
    }

    /**
     * Creates a java.util.List with the specified (Optional) element
     * If the element is null it returns the empty List
     * @param element The element to create the List from
     * @param <T> The type of the element and the List
     * @return A singleton list if the element is not null an empty list otherwise.
     */
    public static <T> List<T> optionalSingleton(@Nullable T element)
    {
        if (element == null) {
            return emptyList();
        }
        else {
            return singletonList(element);
        }
    }

    public static <T> void addAll(Collection<T> target, Iterable<? extends T> source)
    {
        for (T t : source) {
            target.add(t);
        }
    }

    public static List<String> expandAll(Environment env, List<String> args)
    {
        List<String> cmd = new ArrayList<String>(args.size());

        for (String arg : args) {
            cmd.add(env.expand(arg));
        }

        return cmd;
    }

    public static List<String> expandAll(@NotNull Environment env, @NotNull String... patterns)
    {
        return expandAll(env, asList(patterns));
    }

    public static List<File> filesFromBase(@NotNull Environment env, @NotNull String... fileNames)
    {
        List<File> files = new ArrayList<File>(fileNames.length);

        for (String arg : fileNames) {
            files.add(env.fileFromBase(arg));
        }

        return files;
    }

    /**
     * Create a string with the elements separated by the indicated character
     * @param collection
     * @param sep
     * @return A String with the elements of the Iterable separated by the specified separator
     */
    @NotNull public static String listToString(@Nullable Iterable<?> collection, @NotNull String sep)
    {
        StringBuilder result = new StringBuilder();

        if (collection != null) {
            for (Object s : collection) {
                if (result.length() > 0) {
                    result.append(sep);
                }

                result.append(s);
            }
        }

        return result.toString();
    }

    /**
     * Create a string with the elements separated by the indicated character
     * @param collection
     * @return A String with the elements of the Iterable separated by ','
     */
    @NotNull public static String listToString(@Nullable Iterable<?> collection)
    {
        return listToString(collection, DEFAULT_SEP);
    }

    /**
     * Splits this string around matches of the given regular expression.
     *
     * @param  string
     *          The string to be splitted
     * @param  sep
     *         the delimiting regular expression
     *
     * @return  the list of strings computed by splitting this string
     *          around matches of the given regular expression
     *
     */
    @NotNull public static List<String> stringToList(@Nullable String string, @NotNull String sep)
    {
        List<String> result = new ArrayList<String>();

        if (string != null && !string.isEmpty()) {
            result.addAll(asList(string.split(sep)));
        }

        return result;
    }

    /**
     * Splits this string around matches of ','
     *
     * @param  string
     *          The string to be splitted
     * @return  the list of strings computed
     *
     */

    @NotNull public static List<String> stringToList(@Nullable final String string)
    {
        return stringToList(string, DEFAULT_SEP);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String DEFAULT_SEP = ",";
}
