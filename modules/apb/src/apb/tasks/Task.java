

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
import java.util.concurrent.Callable;

import apb.Apb;
import apb.Environment;

import apb.utils.DebugOption;

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

    @NotNull protected final Environment env;

    //~ Constructors .........................................................................................

    public Task(@NotNull Environment env)
    {
        this.env = env;
    }

    protected Task()
    {
        this(Apb.getEnv());
    }

    //~ Methods ..............................................................................................

    public abstract void execute();

    @NotNull public Environment getEnv()
    {
        return env;
    }

    /**
     * Execute the task if it is required because either the target does not exists
     * or any of the dependencies has been modified after the target
     * @param target The target file, if relative it will use the project base directory as its parent
     * @param dependencies The dependencies, if relative it will try first the project source directory
     * and then the project base directory
     */
    public final void executeIfRequired(@NotNull String target, @NotNull String... dependencies)
    {
        boolean exec = env.forceBuild();

        if (!exec) {
            File targetFile = env.fileFromBase(target);

            if (!targetFile.exists()) {
                logVerbose("Executing because file '%s' does not exist.\n", target);
                exec = true;
            }
            else {
                final long targetMod = targetFile.lastModified();

                for (String dependency : dependencies) {
                    File source = env.fileFromSource(dependency);

                    long sourceMod = source.lastModified();

                    if (sourceMod == 0) {
                        source = env.fileFromBase(dependency);

                        sourceMod = source.lastModified();

                        if (sourceMod == 0) {
                            env.logWarning("Not existent dependency '%s'.\n", dependency);
                        }
                    }

                    if (sourceMod > targetMod) {
                        logVerbose("Executing because file '%s' is more recent than '%s'.\n", dependency,
                                   target);
                        exec = true;
                        break;
                    }
                }
            }
        }

        if (exec) {
            execute();
        }
        else {
            logVerbose("Skipping because file '%s' is more recent than all the dependencies.\n", target);
        }
    }

    public final void executeIfRequired(@NotNull String targetDir, @NotNull String sourceDir,
                                        @NotNull String replaceFrom, @NotNull String replaceTo,
                                        @NotNull String... sourceFiles)
    {
        boolean exec = env.forceBuild();

        if (!exec) {
            for (String f : sourceFiles) {
                File targetFile = new File(targetDir, f.replace(replaceFrom, replaceTo));

                final long targetMod = targetFile.lastModified();

                if (targetMod == 0) {
                    logVerbose("File '%s' does not exist.\n", targetFile);
                    exec = true;
                    break;
                }

                File sourceFile = new File(sourceDir, f);

                if (sourceFile.lastModified() > targetMod) {
                    logVerbose("Executing because file '%s' is more recent than '%s'.\n",
                               sourceFile.getPath(), targetFile.getPath());
                    exec = true;
                    break;
                }
            }
        }

        if (exec) {
            execute();
        }
        else {
            logVerbose("Skipping because all target files are more recent than source files\n");
        }
    }

    public Object call()
    {
        execute();
        return null;
    }

    protected void logVerbose(String msg, Object... args)
    {
        if (env.mustShow(DebugOption.TASK_INFO)) {
            env.logVerbose(msg, args);
        }
    }
}
