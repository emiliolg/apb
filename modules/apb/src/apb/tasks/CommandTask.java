
// ...........................................................................................................
//
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// ...........................................................................................................

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

    @NotNull protected final List<String>        cmd;
    @NotNull private File                      currentDirectory;
    @NotNull protected final Map<String, String> environment;

    //~ Constructors .........................................................................................

    public CommandTask(@NotNull Environment env, List<String> cmd)
    {
        super(env);
        this.cmd = cmd;
        environment = new HashMap<String, String>();
        currentDirectory = env.getBaseDir();
    }

    //~ Methods ..............................................................................................

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
}
