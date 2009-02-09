
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

package apb.tasks;

import java.io.File;
import java.util.concurrent.Callable;

import apb.Environment;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 23, 2008
// Time: 5:23:24 PM

//
public abstract class Task
    implements Callable
{
    //~ Instance fields ......................................................................................

    @NotNull protected File currentDirectory;

    @NotNull protected Environment env;

    //~ Constructors .........................................................................................

    public Task(@NotNull Environment env)
    {
        this.env = env;
        currentDirectory = env.getBaseDir();
    }

    //~ Methods ..............................................................................................

    public abstract void execute();

    public final void executeIfNewer(@NotNull String sourceFileName, @NotNull String targetFileName)
    {
        if (env.forceBuild()) {
            execute();
        }
        else {
            File          source = env.fileFromBase(sourceFileName);
            File          target = env.fileFromBase(targetFileName);
            final boolean newer = source.lastModified() > target.lastModified();

            if (env.isVerbose()) {
                logDoNotExists(source);
                logDoNotExists(target);

                if (!newer) {
                    env.logVerbose("Skipping because '%s'\n", target);
                    env.logVerbose("   is newer than '%s'\n", source);
                }
            }

            if (newer) {
                execute();
            }
        }
    }

    public Object call()
    {
        execute();
        return null;
    }

    public void setCurrentDirectory(@NotNull File dir)
    {
        currentDirectory = dir;
    }

    public void setCurrentDirectory(@NotNull String dir)
    {
        currentDirectory = env.fileFromBase(dir);
    }

    private void logDoNotExists(File source)
    {
        if (!source.exists()) {
            env.logVerbose("File '%s' does not exist.\n", source);
        }
    }
}
