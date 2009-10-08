

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


package apb.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apb.Apb;

import apb.metadata.ResourcesInfo;

import apb.utils.FileUtils;
import apb.utils.Filter;

import org.jetbrains.annotations.NotNull;

import static java.util.Collections.singletonList;

import static apb.tasks.FileSet.fromDir;
import static apb.tasks.FileSet.fromFile;

/**
 * Fluent interface based Copy Task
 */
public class FilterTask
    extends CopyTask
{
    //~ Instance fields ......................................................................................

    @NotNull private final List<Filter> filters;
    @NotNull private final List<String> linesToAppend;
    @NotNull private final List<String> linesToInsert;

    @NotNull private String encoding = ResourcesInfo.DEFAULT_ENCODING;

    //~ Constructors .........................................................................................

    FilterTask(@NotNull List<FileSet> from, @NotNull File to)
    {
        super(from, to);

        if (singleSource == null) {
            for (FileSet fileSet : from) {
                fileSet.excluding(ResourcesInfo.DEFAULT_DO_NOT_FILTER);
            }
        }

        filters = new ArrayList<Filter>();
        linesToInsert = new ArrayList<String>();
        linesToAppend = new ArrayList<String>();
    }

    //~ Methods ..............................................................................................

    /**
     * Specify the encoding to use when doing filtering
     * If none is specfied UTF8 will be used
     * @param enc The encoding to be used
     */
    @NotNull public FilterTask withEncoding(@NotNull String enc)
    {
        encoding = enc;
        return this;
    }

    /**
    * When filtering replaces each substring of the input that matches the given <a
    * href="../util/regex/Pattern.html#sum">regular expression</a> with the
    * given replacement.
    *
    * @param   regex
    *          the regular expression to which this string is to be matched
    *
    * @throws java.util.regex.PatternSyntaxException
    *          if the regular expression's syntax is invalid
    *
    * @see java.util.regex.Pattern
    */
    @NotNull public FilterTask replacing(@NotNull String regex, @NotNull String replacement)
    {
        filters.add(Filter.Factory.replaceAll(regex, replacement));
        return this;
    }

    /**
     * Define a line to be inserted at the top of the new File
     * @param line The line to be inserted at the top of the new File
     */
    public FilterTask insertingLine(@NotNull String line)
    {
        linesToInsert.add(line);
        return this;
    }

    /**
     * Define a line to be appended at the end of the new File
     * @param line The line to be appended at the end of the new File
     */
    public FilterTask appendingLine(@NotNull String line)
    {
        linesToAppend.add(line);
        return this;
    }

    @Override protected void doCopyFile(File source, File dest)
        throws IOException
    {
        // By default filter expanding properties
        if (filters.isEmpty()) {
            filters.add(Filter.Factory.expandProperties(env));
        }

        if (isVerbose()) {
            logVerbose("Filtering %s\n", source);
            logVerbose("       to %s\n", dest);

            for (Filter filter : filters) {
                logVerbose("%s\n", filter);
            }
        }

        FileUtils.copyFileFiltering(source, dest, false, encoding, filters, linesToInsert, linesToAppend);
    }

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final List<FileSet> from;

        Builder(@NotNull FileSet... from)
        {
            this.from = Arrays.asList(from);
        }

        /**
         * Private constructor called from factory methods
         * @param from The source to copy from. It can be a file or a directory
         */

        Builder(@NotNull File from)
        {
            this.from = singletonList(from.isDirectory() ? fromDir(from) : fromFile(from));
        }

        /**
        * Specify the target file or directory
        * If not specified, then the file/s will be copied to the current module output
        * @param to The File or directory to copy from
        * @throws IllegalArgumentException if trying to copy a directoy to a single file.
        */
        @NotNull public FilterTask to(@NotNull String to)
        {
            return to(Apb.getEnv().fileFromBase(to));
        }

        /**
         * Specify the target file or directory
         * If not specified, then the file/s will be copied to the current module output
         * @param to The File or directory to copy from
         */
        @NotNull public FilterTask to(@NotNull File to)
        {
            return new FilterTask(from, to);
        }
    }
}
