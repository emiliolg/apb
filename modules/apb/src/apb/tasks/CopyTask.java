

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
import java.util.LinkedHashMap;
import java.util.Map;

import apb.Apb;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

// User: emilio
// Date: Sep 5, 2009
// Time: 6:15:46 PM

public class CopyTask
    extends FileTask
{
    //~ Instance fields ......................................................................................

    @NotNull protected final File from;
    @NotNull protected final File to;

    //~ Constructors .........................................................................................

    protected CopyTask(@NotNull File from, @NotNull File to)
    {
        this.from = env.fileFromBase(from);
        this.to = env.fileFromBase(to);
    }

    //~ Methods ..............................................................................................

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

        if (from.isDirectory()) {
            copyFromDirectory(to);
        }
        else {
            File dest = to.isDirectory() ? new File(to, from.getName()) : to;
            copyFile(from, dest);
        }
    }

    protected void copyFile(File source, File dest)
    {
        try {
            logVerbose("Copy %s\n", source);
            logVerbose("  to %s\n", dest);
            FileUtils.copyFile(source, dest, false);
        }
        catch (IOException e) {
            env.handle(e);
        }
    }

    private void copyFromDirectory(@NotNull File target)
    {
        if (!target.exists() && !target.mkdirs()) {
            env.handle("Cannot create resource output directory: " + target);
            return;
        }

        if (env.isVerbose()) {
            logVerbose("Copying resources from: %s\n", from);
            logVerbose("                    to: %s\n", target);
            logVerbose("              includes: %s\n", includes);

            if (!excludes.isEmpty()) {
                logVerbose("              excludes: %s\n", excludes);
            }
        }

        Map<File, File> includedFiles = findFiles(from, target);

        if (!includedFiles.isEmpty()) {
            env.logInfo("Copying %2d resource%s\nto %s\n", includedFiles.size(),
                        includedFiles.size() > 1 ? "s" : "", target);

            for (Map.Entry<File, File> entry : includedFiles.entrySet()) {
                copyFile(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<File, File> findFiles(File fromDirectory, File outputDirectory)
    {
        Map<File, File> files = new LinkedHashMap<File, File>();

        for (String name : includedFiles(fromDirectory)) {
            File source = new File(fromDirectory, name);
            File target = new File(outputDirectory, name);

            if (env.forceBuild() || !target.exists() || source.lastModified() > target.lastModified()) {
                files.put(source, target);
            }
        }

        return files;
    }

    //~ Inner Classes ........................................................................................

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
        @NotNull public CopyTask to(@NotNull String to)
        {
            return to(new File(Apb.getEnv().expand(to)));
        }

        /**
         * Specify the target file or directory
         * If not specified, then the file/s will be copied to the current module output
         * @param to The File or directory to copy from
         * @throws IllegalArgumentException if trying to copy a directoy to a signle file.
         */
        @NotNull public CopyTask to(@NotNull File to)
        {
            if (from.isDirectory() && to.exists() && !to.isDirectory()) {
                throw new IllegalArgumentException("Trying to copy directory '" + from.getPath() + "'" +
                                                   " to a file '" + to.getPath() + "'.");
            }

            return new CopyTask(from, to);
        }
    }
}
