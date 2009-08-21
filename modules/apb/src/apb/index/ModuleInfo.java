

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


package apb.index;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import apb.Command;
import apb.CommandBuilder;
import apb.Environment;
import apb.ProjectBuilder;

import apb.metadata.ProjectElement;

import org.jetbrains.annotations.NotNull;

import static apb.utils.FileUtils.normalizeFile;
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

    @NotNull private final String basedir;

    @NotNull private final Collection<String> commands;
    @NotNull private String                   defaultCommand;

    @NotNull private final String id;

    @NotNull private final String moduleDir;
    @NotNull private final String name;
    private transient String      path;
    private File                  projectPath;

    //~ Constructors .........................................................................................

    ModuleInfo(File projectPath, ProjectElement element)
    {
        this.projectPath = projectPath;
        name = element.getName();
        id = element.getId();
        moduleDir = element.getDir();
        basedir = element.basedir;

        commands = new ArrayList<String>();
        defaultCommand = Command.DEFAULT_COMMAND;

        CommandBuilder b = new CommandBuilder(element);

        for (Map.Entry<String, Command> cmd : b.commands().entrySet()) {
            final String nm = cmd.getValue().getQName();

            if (cmd.getKey().equals(Command.DEFAULT_COMMAND)) {
                defaultCommand = nm;
            }
            else {
                commands.add(nm);
            }
        }
    }

    //~ Methods ..............................................................................................

    @NotNull public Collection<String> getCommands()
    {
        return commands;
    }

    @NotNull public File contentDir(Environment env)
    {
        env.putProperty(ProjectBuilder.PROJECTS_HOME_PROP_KEY, projectPath.getPath());
        File result = new File(env.expand(moduleDir));

        if (!result.isAbsolute()) {
            result = new File(normalizeFile(new File(env.expand(basedir))), result.getPath());
        }

        return result;
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

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = 8545780614351258679L;
}
