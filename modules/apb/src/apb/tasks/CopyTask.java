

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
import java.util.List;
import java.util.Map;

import apb.Apb;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.singletonList;

import static apb.tasks.FileSet.fromDir;
import static apb.tasks.FileSet.fromFile;

// User: emilio
// Date: Sep 5, 2009
// Time: 6:15:46 PM

public class CopyTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull protected final List<FileSet> from;

    @NotNull protected final File to;

    //~ Constructors .........................................................................................

    protected CopyTask(@NotNull List<FileSet> fileSets, @NotNull File to)
    {
        from = fileSets;
        this.to = to;
    }

    //~ Methods ..............................................................................................

    /**
       * Execute the copy
       */
    public void execute()
    {
        final File source = extractSingleFile();

        if (source == null) {
            copyToDirectory();
        }
        else {
            final File dest = to.isDirectory() ? new File(to, source.getName()) : to;

            if (env.forceBuild() || !dest.exists() || source.lastModified() > dest.lastModified()) {
                copyFile(source, dest);
            }
        }
    }

    protected void doCopyFile(File source, File dest)
        throws IOException
    {
        if (env.isVerbose()) {
            logVerbose("Copy %s\n", source);
            logVerbose("  to %s\n", dest);
        }

        FileUtils.copyFile(source, dest, false);
    }

    @Nullable private File extractSingleFile()
    {
        if (from.size() != 1) {
            return null;
        }

        FileSet fs = from.get(0);

        if (!fs.isFile()) {
            return null;
        }

        return new File(fs.getDir(), fs.list().get(0));
    }

    private void copyFile(File source, File dest)
    {
        try {
            doCopyFile(source, dest);
        }
        catch (IOException e) {
            env.handle(e);
        }
    }

    private void copyToDirectory()
    {
        Map<File, File> files = FileUtils.listAllMappingToTarget(from, to, true, true);

        /**
         * If the source file/directories are empty or not existent just skip the copy
         */
        if (!files.isEmpty()) {
            if (!to.exists() && !to.mkdirs()) {
                env.handle("Cannot create output directory: " + to);
            }
            else {
                env.logInfo("Copying %2d file%s\nto %s\n", files.size(), files.size() > 1 ? "s" : "", to);

                for (Map.Entry<File, File> entry : files.entrySet()) {
                    copyFile(entry.getKey(), entry.getValue());
                }
            }
        }
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
            this.from = singletonList(from.isFile() ? fromFile(from) : fromDir(from));
        }

        /**
        * Specify the target file or directory
        * @param to The File or directory to copy from
        * @throws IllegalArgumentException if trying to copy a directoy to a single file.
        */
        @NotNull public CopyTask to(@NotNull String to)
        {
            return to(Apb.getEnv().fileFromBase(to));
        }

        /**
         * Specify the target file or directory
         * @param to The File or directory to copy from
         * @throws IllegalArgumentException if trying to copy a directoy to a signle file.
         */
        @NotNull public CopyTask to(@NotNull File to)
        {
            if (to.isFile() && (from.size() != 1 || !from.get(0).isFile())) {
                throw new IllegalArgumentException("Trying to copy multiple files to a single file '" +
                                                   to.getPath() + "'.");
            }

            return new CopyTask(from, to);
        }
    }
}
