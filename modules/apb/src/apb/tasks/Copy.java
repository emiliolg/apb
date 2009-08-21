

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import apb.Environment;

import apb.metadata.ResourcesInfo;

import apb.utils.DirectoryScanner;
import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fluent interface based Copy Task
 */
public class Copy
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private String encoding;

    @NotNull private Set<String> excludes;
    private final boolean        filter;
    @NotNull private final File  from;
    @NotNull private Set<String> includes;
    @Nullable private File       target;

    //~ Constructors .........................................................................................

    /**
     * Private constructor called from factory methods
     * @param env The current environment
     * @param from The source to copy from. It can be a file or a directory
     * @param filter Whether to apply filtering (keyword expansion) when copying
     */
    Copy(@NotNull Environment env, @NotNull File from, boolean filter)
    {
        super(env);
        this.from = from;
        this.filter = filter;
        includes = new HashSet<String>();
        excludes = new HashSet<String>();

        if (filter) {
            excludes.addAll(ResourcesInfo.DEFAULT_DO_NOT_FILTER);
        }

        encoding = ResourcesInfo.DEFAULT_ENCODING;
    }

    //~ Methods ..............................................................................................

    /**
     * Specify the target file or directory
     * If not specified, then the file/s will be copied to the current module output
     * @param to The File or directory to copy from
     * @throws IllegalArgumentException if trying to copy a directoy to a single file.
     */
    @NotNull public Copy to(@NotNull String to)
    {
        return to(new File(to));
    }

    /**
     * Specify the target file or directory
     * If not specified, then the file/s will be copied to the current module output
     * @param to The File or directory to copy from
     * @throws IllegalArgumentException if trying to copy a directoy to a signle file.
     */
    @NotNull public Copy to(@NotNull File to)
    {
        if (!to.isDirectory() && from.isDirectory()) {
            throw new IllegalArgumentException("Trying to copy directory '" + from.getPath() + "'" +
                                               " to a file '" + to.getPath() + "'.");
        }

        target = to;
        return this;
    }

    /**
     * Specify the encoding to use when doing filtering
     * If none is specfied UTF8 will be used
     * @param enc The encoding to be used
     */
    @NotNull public Copy withEncoding(@NotNull String enc)
    {
        encoding = enc;
        return this;
    }

    /**
     * When copying a directory content specify the list of files to include
     * @param patterns The patterns that define the list of files to include
     */
    @NotNull public Copy including(@NotNull String... patterns)
    {
        includes.addAll(Arrays.asList(patterns));
        return this;
    }

    /**
     * When copying a directory content specify the list of files to exclude
     * @param patterns The patterns that define the list of files to exclude
     */
    @NotNull public Copy excluding(@NotNull String... patterns)
    {
        excludes.addAll(Arrays.asList(patterns));
        return this;
    }

    /**
     * Execute the copy
     */
    public void execute()
    {
        /**
         * If the source file/directory does not exists just skip the copy
         */
        if (!from.exists()) {
            env.logInfo("Skip non existing from directory: %s\n", from.getPath());
            return;
        }

        File to = target == null ? env.getOutputDir() : target;

        if (from.isDirectory()) {
            copyFromDirectory(to);
        }
        else {
            File dest = to.isDirectory() ? new File(to, from.getName()) : to;
            copyFile(from, dest);
        }
    }

    private void copyFromDirectory(@NotNull File to)
    {
        // Defaults
        if (includes.isEmpty()) {
            includes.add("**/**");
        }

        if (!to.exists() && !to.mkdirs()) {
            env.handle("Cannot create resource output directory: " + to);
            return;
        }

        if (env.isVerbose()) {
            logVerbose("Copying resources from: %s\n", from);
            logVerbose("                    to: %s\n", to);
            logVerbose("              includes: %s\n", includes);

            if (!excludes.isEmpty()) {
                logVerbose("              excludes: %s\n", excludes);
            }
        }

        Map<File, File> includedFiles = findFiles(from, to);

        if (!includedFiles.isEmpty()) {
            env.logInfo("Copying %2d resource%s\nto %s\n", includedFiles.size(),
                        includedFiles.size() > 1 ? "s" : "", to);

            for (Map.Entry<File, File> entry : includedFiles.entrySet()) {
                copyFile(entry.getKey(), entry.getValue());
            }
        }
    }

    private void copyFile(File source, File dest)
    {
        try {
            if (filter) {
                logVerbose("Filtering %s\n", source);
                logVerbose("       to %s\n", dest);
                final FileUtils.Filter f = new PropertyFilter(env);
                FileUtils.copyFileFiltering(source, dest, false, encoding, Collections.singletonList(f));
            }
            else {
                logVerbose("Copy %s\n", source);
                logVerbose("  to %s\n", dest);
                FileUtils.copyFile(source, dest, false);
            }
        }
        catch (IOException e) {
            env.handle(e);
        }
    }

    private Map<File, File> findFiles(File fromDirectory, File outputDirectory)
    {
        DirectoryScanner scanner = new DirectoryScanner(fromDirectory, includes, excludes);

        try {
            scanner.scan();
        }
        catch (IOException e) {
            env.handle(e);
        }

        Map<File, File> files = new LinkedHashMap<File, File>();

        for (String name : scanner.getIncludedFiles()) {
            File source = new File(fromDirectory, name);
            File to = new File(outputDirectory, name);

            if (env.forceBuild() || !to.exists() || source.lastModified() > to.lastModified()) {
                files.put(source, to);
            }
        }

        return files;
    }

    //~ Inner Classes ........................................................................................

    private static class PropertyFilter
        implements FileUtils.Filter
    {
        private Environment env;

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
