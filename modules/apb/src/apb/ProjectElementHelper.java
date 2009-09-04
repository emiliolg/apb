

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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import apb.metadata.ProjectElement;
import apb.metadata.Synthetic;

import apb.utils.FileUtils;
import apb.utils.PropertyExpansor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Oct 10, 2008
// Time: 10:41:05 AM

//
public abstract class ProjectElementHelper
    extends DelegatedEnvironment
{
    //~ Instance fields ......................................................................................

    @NotNull protected final Set<Command> executedCommands;
    private boolean                       initialized;
    private boolean                       topLevel;

    @Nullable private CommandBuilder      commandBuilder;
    @NotNull private final File           projectDirectory;
    @NotNull private final File           sourceFile;
    @NotNull private final ProjectElement element;

    //~ Constructors .........................................................................................

    protected ProjectElementHelper(@NotNull ProjectBuilder pb, @NotNull ProjectElement element)
    {
        super(pb.getBaseEnvironment());
        this.element = element;
        executedCommands = new HashSet<Command>();

        if (element instanceof Synthetic) {
            projectDirectory = ((Synthetic) element).getProjectDirectory();
            sourceFile = new File(projectDirectory, element.getClass().getSimpleName());
        }
        else {
            sourceFile = pb.sourceFile(element);
            projectDirectory = findProjectDir(element, sourceFile);
        }

        initialized = false;
    }

    //~ Methods ..............................................................................................

    public abstract Set<ModuleHelper> listAllModules();

    public final String toString()
    {
        return getName();
    }

    @NotNull public final String getName()
    {
        return element.getName();
    }

    public final String getId()
    {
        return element.getId();
    }

    @NotNull public ProjectElement getElement()
    {
        return element;
    }

    @NotNull public final File getBaseDir()
    {
        return new File(element.basedir);
    }

    public final void setTopLevel(boolean b)
    {
        topLevel = b;
    }

    public final boolean isTopLevel()
    {
        return topLevel;
    }

    public final String getJdkName()
    {
        return getElement().jdk;
    }

    public final long lastModified()
    {
        return sourceFile.lastModified();
    }

    public final SortedMap<String, Command> listCommands()
    {
        return getCommandBuilder().commands();
    }

    public Command findCommand(String commandName)
    {
        return getCommandBuilder().commands().get(commandName);
    }

    /**
     * Return the Absolute file for the content of the project/module
     * This method works regardless of the Module being activated.
     * @return The File for the content of the project/module
     */
    @NotNull public File getDirFile()
    {
        File result = new File(expand(element.getDir()));

        if (!result.isAbsolute()) {
            File basedir = new File(expand(element.basedir));

            basedir = FileUtils.normalizeFile(basedir);

            result = new File(basedir, result.getPath());
        }

        return result;
    }

    @NotNull public File getSourceFile()
    {
        return sourceFile;
    }

    /**
     * Returns The directory where the project definition files are stored
     */
    @NotNull public File getProjectDirectory()
    {
        return projectDirectory;
    }

    @NotNull public CommandBuilder getCommandBuilder()
    {
        if (commandBuilder == null) {
            commandBuilder = new CommandBuilder(getElement());
        }

        return commandBuilder;
    }

    @Override public boolean equals(Object o)
    {
        return this == o ||
               o instanceof ProjectElementHelper && getName().equals(((ProjectElementHelper) o).getName());
    }

    @Override public int hashCode()
    {
        return getName().hashCode();
    }

    protected abstract void build(ProjectBuilder pb, String commandName);

    void markExecuted(Command cmd)
    {
        executedCommands.add(cmd);
    }

    boolean notExecuted(Command cmd)
    {
        return !executedCommands.contains(cmd);
    }

    void init()
    {
        if (!initialized) {
            PropertyExpansor.expandProperties(this, "", getElement());
            getElement().init();
            initialized = true;
        }
    }

    private static File findProjectDir(ProjectElement proto, File sourceFile)
    {
        File   result = sourceFile;
        String className = proto.getClass().getName();

        for (int i = 0; i != -1; i = className.indexOf('.', i + 1)) {
            result = result.getParentFile();
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    static final String ID_SUFFIX = "id";
    static final String DIR_SUFFIX = "dir";
}
