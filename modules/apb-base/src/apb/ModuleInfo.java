

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


package apb;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

import apb.metadata.Project;
import apb.metadata.ProjectElement;

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

    @NotNull private final File   contentDir;
    @NotNull private final String defaultCommand;

    @NotNull private final String id;
    @NotNull private final String name;
    private transient String      path;

    //~ Constructors .........................................................................................

    ModuleInfo(ProjectElementHelper element)
    {
        name = element.getName();
        id = element.getId();
        contentDir = element.getDirFile();
        commands = new TreeSet<String>();

        if (element instanceof ProjectHelper) {
            Project p = ((ProjectHelper) element).getProject();

            for (ProjectElement e : p.components()) {
                addAllCommands(commands, e.getHelper().getCommandBuilder());
            }
        }

        CommandBuilder b = element.getCommandBuilder();
        addAllCommands(commands, b);
        final Command dflt = b.getDefaultCommand();
        defaultCommand = dflt == null ? Command.DEFAULT_COMMAND : dflt.getName();
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

    @NotNull public String getDefaultCommand()
    {
        return defaultCommand;
    }

    void establishPath(File pdefDir)
    {
        path = new File(pdefDir, getName().replace('.', File.separatorChar)).getPath();
    }

    private static void addAllCommands(Collection<String> commands, CommandBuilder b)
    {
        for (Command command : b.listCommands()) {
            commands.add(command.getName());
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = 8545780614351258679L;
}
