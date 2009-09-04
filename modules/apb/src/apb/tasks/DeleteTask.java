

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

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 4, 2009
// Time: 4:49:37 PM

public class DeleteTask
    extends FileTask
{
    //~ Instance fields ......................................................................................

    @NotNull private File file;

    //~ Constructors .........................................................................................

    DeleteTask(@NotNull File file)
    {
        this.file = file;
    }

    //~ Methods ..............................................................................................

    @Override public void execute()
    {
        final boolean isDirectory = file.isDirectory() && includes.isEmpty() && excludes.isEmpty();
        final boolean isPatternBased = !isDirectory && file.isDirectory();

        if (isDirectory || file.exists()) {
            boolean ok =
                isDirectory ? removeDir(file) : isPatternBased ? removePattern(file) : removeFile(file);

            if (!ok) {
                env.logWarning("Unable to delete " + file.getAbsolutePath());
            }
        }
    }

    private boolean removeFile(File f)
    {
        env.logInfo("Deleting file %s\n", f.getAbsolutePath());
        return f.delete();
    }

    private boolean removeDir(File d)
    {
        if (FileUtils.DEFAULT_SRC_EXCLUDES.contains(d.getName())) {
            return true;
        }

        env.logInfo("Deleting directory %s\n", d.getAbsolutePath());

        String[] list = d.list();

        if (list == null) {
            list = new String[0];
        }

        for (String s : list) {
            File f = new File(d, s);

            if (f.isDirectory()) {
                removeDir(f);
            }
            else {
                logVerbose("Deleting: %s\n", f.getAbsolutePath());

                if (!f.delete()) {
                    env.handle("Unable to delete file " + f.getAbsolutePath());
                }
            }
        }

        return d.delete();
    }

    private boolean removePattern(File d)
    {
        env.logInfo("Deleting  : %s\n", d.getAbsolutePath());
        env.logInfo("  includes: %s\n", includes);

        if (!excludes.isEmpty()) {
            env.logInfo("  excludes: %s\n", excludes);
        }

        for (String s : includedFiles(d)) {
            File f = new File(d, s);
            logVerbose("Deleting: %s\n", f.getAbsolutePath());

            if (!f.delete()) {
                env.handle("Unable to delete file " + f.getAbsolutePath());
            }
        }

        return true;
    }
}
