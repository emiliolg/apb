
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-28 15:20:37 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb.index;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import apb.Command;
import apb.ProjectElementHelper;
import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Apr 27, 2009
// Time: 12:17:35 PM

/**
 * This class holds basic information from a Project or Module Definition
 */
public class ModuleInfo
    implements Serializable,
               Comparable<ModuleInfo>
{
    //~ Instance fields ......................................................................................

    @NotNull private final Collection<String> commands;

    @NotNull private final File contentDir;

    @NotNull private final String id;
    @NotNull private final String name;
    private transient String      path;
    @NotNull private String defaultCommand;

    //~ Constructors .........................................................................................

    ModuleInfo(ProjectElementHelper element)
    {
        name = element.getName();
        id = element.getId();
        contentDir = element.getDirFile();
        commands = new ArrayList<String>();
        defaultCommand = Command.DEFAULT_COMMAND;

        for (Map.Entry<String,Command> cmd : element.listCommands().entrySet()) {
            final String nm = cmd.getValue().getQName();
            if (cmd.getKey().equals(Command.DEFAULT_COMMAND))
                defaultCommand = nm;
            else
                commands.add(nm);
        }
    }

    //~ Methods ..............................................................................................

    @NotNull public Collection<String> getCommands()
    {
        return commands;
    }

    @NotNull public File getContentDir()
    {
        return contentDir;
    }

    @NotNull public String getId()
    {
        return id;
    }

    @Override public String toString()
    {
        return getName();
    }

    @NotNull public String getName()
    {
        return name;
    }

    public int compareTo(ModuleInfo o)
    {
        return name.compareTo(o.name);
    }

    public String getPath()
    {
        return path;
    }

    void establishPath(File pdefDir)
    {
        path = new File(pdefDir, getName().replace('.', File.separatorChar)).getPath();
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = 8545780614351258679L;

    @NotNull
    public String getDefaultCommand() {
        return defaultCommand;
    }
}
