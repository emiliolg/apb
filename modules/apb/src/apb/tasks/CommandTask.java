

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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Jul 28, 2009
// Time: 6:32:43 PM

//
public abstract class CommandTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private final Map<String, String> environment;
    @NotNull private final List<String> cmd;
    @NotNull private File               currentDirectory;

    //~ Constructors .........................................................................................

    public CommandTask(@NotNull Environment env, List<String> cmd)
    {
        super(env);
        this.cmd = cmd;
        environment = new HashMap<String, String>();
        currentDirectory = env.getBaseDir();
    }

    //~ Methods ..............................................................................................

    @NotNull public Map<String, String> getEnvironment()
    {
        return environment;
    }

    /**
     * Add one or more arguments to the command line to be executed
     * @param arguments The arguments to be added
     */
    public final void addArguments(@NotNull String... arguments)
    {
        addArguments(Arrays.asList(arguments));
    }

    /**
     * Add one or more arguments to the command line to be executed
     * @param arguments The arguments to be added
     */
    public final void addArguments(Collection<String> arguments)
    {
        for (String arg : arguments) {
            if (arg != null) {
                cmd.add(arg);
            }
        }
    }

    public final void putEnv(String key, String value)
    {
        environment.put(key, value);
    }

    public final void putAll(@Nullable Map<String, String> environmentVariables)
    {
        if (environmentVariables != null) {
            environment.putAll(environmentVariables);
        }
    }

    public void setCurrentDirectory(@NotNull File dir)
    {
        currentDirectory = dir;
    }

    public void setCurrentDirectory(@NotNull String dir)
    {
        currentDirectory = env.fileFromBase(dir);
    }

    @NotNull public File getCurrentDirectory()
    {
        return currentDirectory;
    }

    @NotNull public List<String> getArguments()
    {
        return cmd;
    }
}
