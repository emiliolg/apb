

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
import java.util.Collections;

import apb.Environment;
import apb.Apb;

import apb.metadata.ResourcesInfo;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Fluent interface based Copy Task
 */
public class FilterTask
    extends CopyTask
{
    //~ Instance fields ......................................................................................

    @NotNull private String encoding = ResourcesInfo.DEFAULT_ENCODING;

    //~ Constructors .........................................................................................

    FilterTask(@NotNull File from, @NotNull File to)
    {
        super(from, to);
        excludes.addAll(ResourcesInfo.DEFAULT_DO_NOT_FILTER);
    }

    //~ Methods ..............................................................................................

    /**
     * Specify the encoding to use when doing filtering
     * If none is specfied UTF8 will be used
     * @param enc The encoding to be used
     */
    @NotNull public CopyTask withEncoding(@NotNull String enc)
    {
        encoding = enc;
        return this;
    }

    @Override
    protected void copyFile(File source, File dest)
    {
        try {
            logVerbose("Filtering %s\n", source);
            logVerbose("       to %s\n", dest);
            final FileUtils.Filter f = new PropertyFilter(env);
            FileUtils.copyFileFiltering(source, dest, false, encoding, Collections.singletonList(f));
        }
        catch (IOException e) {
            env.handle(e);
        }
    }

    public static class Builder
    {
        @NotNull private final File from;

        /**
         * Private constructor called from factory methods
         * @param from The source to copy from. It can be a file or a directory
         */

        Builder(@NotNull File from)
        {
            this.from = from;
        }

        /**
         * Private constructor called from factory methods
         * @param from The source to copy from. It can be a file or a directory
         */
        Builder(@NotNull String from)
        {
            this(new File(Apb.getEnv().expand(from)));
        }

        /**
        * Specify the target file or directory
        * If not specified, then the file/s will be copied to the current module output
        * @param to The File or directory to copy from
        * @throws IllegalArgumentException if trying to copy a directoy to a single file.
        */
        @NotNull public FilterTask to(@NotNull String to)
        {
            return to(new File(Apb.getEnv().expand(to)));
        }

        /**
         * Specify the target file or directory
         * If not specified, then the file/s will be copied to the current module output
         * @param to The File or directory to copy from
         * @throws IllegalArgumentException if trying to copy a directoy to a signle file.
         */
        @NotNull public FilterTask to(@NotNull File to)
        {
            if (from.isDirectory() && to.exists() && !to.isDirectory()) {
                throw new IllegalArgumentException("Trying to copy directory '" + from.getPath() + "'" +
                                                   " to a file '" + to.getPath() + "'.");
            }

            return new FilterTask(from, to);
        }
    }

    //~ Inner Classes ........................................................................................

    private static class PropertyFilter
        implements FileUtils.Filter
    {
        private final Environment env;

        public PropertyFilter(Environment env)
        {
            this.env = env;
        }

        public String filter(String str)
        {
            return env.expand(str);
        }
    }
}
