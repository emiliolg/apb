

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import apb.metadata.ProjectElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.FileUtils.normalizeFile;
//
// User: emilio
// Date: Oct 10, 2008
// Time: 10:41:05 AM

/**
 * Provides additional functionality for {@link apb.metadata.ProjectElement} objects
 *
 */
public class ProjectElementHelper
    extends DelegatedEnvironment
{
    //~ Instance fields ......................................................................................

    /**
     * The Map for Info objects
     */
    @NotNull final Map<String, Object> infoMap;
    private boolean                    initialized;
    private boolean                    topLevel;

    @Nullable private CommandBuilder      commandBuilder;
    @NotNull private final File           projectDirectory;
    @NotNull private final File           sourceFile;
    @NotNull private final ProjectElement element;

    @NotNull private final Set<Command> executedCommands;

    //~ Constructors .........................................................................................

    ProjectElementHelper(@NotNull ProjectBuilder pb, @NotNull ProjectElement element)
    {
        super(pb.getBaseEnvironment());

        this.element = element;
        executedCommands = new HashSet<Command>();
        infoMap = new TreeMap<String, Object>();

        sourceFile = pb.sourceFile(element);
        projectDirectory = findProjectDir(element, sourceFile);
        initialized = false;
    }

    //~ Methods ..............................................................................................

    /**
     * Returns a list of all Modules related wih the current one, including dependencies and associated test modules
     */
    public Set<String> listAllModules()
    {
        return Collections.emptySet();
    }

    @NotNull public Iterable<ModuleHelper> getDependencies()
    {
        return Collections.emptyList();
    }

    /**
     * A String representation of the current Element
     */
    public final String toString()
    {
        return getName();
    }

    /**
     * The name of the current element
     */
    @NotNull public final String getName()
    {
        return element.getName();
    }

    /**
     * The id of the current element
     */
    public final String getId()
    {
        return element.getId();
    }

    /**
     * Sets whether this is the original top level target or not.
     */
    public final void setTopLevel(boolean b)
    {
        topLevel = b;
    }

    /**
     * Returns true if this Element was the original top level target
     */
    public final boolean isTopLevel()
    {
        return topLevel;
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

            basedir = normalizeFile(basedir);

            result = new File(basedir, result.getPath());
        }

        return result;
    }

    /**
     * Returns the File that contains the definition for the current Element
     */
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

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param   o   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the o
     *          argument; <code>false</code> otherwise.
     */
    @Override public boolean equals(Object o)
    {
        return this == o ||
               o instanceof ProjectElementHelper && getName().equals(((ProjectElementHelper) o).getName());
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.HashMap</code>.
     * @return  a hash code value for this object.
     */
    @Override public int hashCode()
    {
        return getName().hashCode();
    }

    /**
     * Get the given Info object for this Module
     * @param name  The name for the Info object (For example 'resourcesInfo')
     * @param type  The class that defines the type for the Info object (For example: apb.metadata.ResourcesInfo)
     */
    @NotNull public <T> T getInfoObject(@NotNull String name, @NotNull Class<T> type)
    {
        return PropertyExpansor.retrieveInfoObject(this, name, type);
    }

    /**
     * List all valid commands for this element
     */
    public Iterable<Command> listCommands()
    {
        final CommandBuilder builder = getCommandBuilder();
        return new TreeSet<Command>(builder.commands().values());
    }

    void build(ProjectBuilder pb, String commandName)
    {
        Command command = findCommand(commandName);

        if (command == null) {
            throw new BuildException("Invalid command: " + commandName);
        }

        if (command.isRecursive() && !isNonRecursive()) {
            for (ModuleHelper dep : getDependencies()) {
                pb.execute(dep, commandName);
            }
        }
        else {
            for (Command cmd : command.getDirectDependencies()) {
                pb.build(this, cmd.getName());
            }
        }

        pb.execute(this, commandName);
    }

    @NotNull CommandBuilder getCommandBuilder()
    {
        if (commandBuilder == null) {
            commandBuilder = new CommandBuilder(getElement());
        }

        return commandBuilder;
    }

    @NotNull ProjectElement getElement()
    {
        return element;
    }

    Command findCommand(String commandName)
    {
        return getCommandBuilder().commands().get(commandName);
    }

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
        File   result = normalizeFile(sourceFile.getAbsoluteFile());
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
