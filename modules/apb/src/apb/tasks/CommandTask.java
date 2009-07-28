
// ...........................................................................................................
//
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// ...........................................................................................................

package apb.tasks;

import java.util.Collection;
import java.util.Arrays;
import java.util.List;

import apb.Environment;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Jul 28, 2009
// Time: 6:32:43 PM

//
public abstract class CommandTask
    extends Task
{
    @NotNull protected final List<String> cmd;

    //~ Constructors .........................................................................................

    public CommandTask(@NotNull Environment env, List<String> cmd)
    {
        super(env);
        this.cmd = cmd;
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
}
