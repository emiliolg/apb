

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

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Oct 10, 2008
// Time: 10:41:05 AM

//
public abstract class ProjectElementHelper
{
    //~ Instance fields ......................................................................................

    public Environment                    env;
    @NotNull protected final Set<Command> executedCommands;
    @Nullable ProjectElement              element;

    private CommandBuilder      builder;
    @NotNull private final File projectDirectory;

    @NotNull private final ProjectElement proto;
    @NotNull private final File           sourceFile;
    private boolean                       topLevel;

    //~ Constructors .........................................................................................

    protected ProjectElementHelper(@NotNull ProjectElement element, @NotNull Environment environment)
    {
        proto = element;
        env = environment;
        executedCommands = new HashSet<Command>();
        final ProjectBuilder pb = ProjectBuilder.getInstance();
        pb.registerHelper(this);
        sourceFile = pb.sourceFile(element);
        projectDirectory = findProjectDir(element, sourceFile);
    }

    //~ Methods ..............................................................................................

    public abstract Set<ModuleHelper> listAllModules();

    @NotNull public static ProjectElementHelper create(ProjectElement element, Environment environment)
    {
        element.init();
        ProjectElementHelper result;

        if (element instanceof TestModule) {
            result = new TestModuleHelper((TestModule) element, environment);
        }
        else if (element instanceof Module) {
            result = new ModuleHelper((Module) element, environment);
        }
        else {
            result = new ProjectHelper((Project) element, environment);
        }

        result.initDependencyGraph();
        return result;
    }

    public final String toString()
    {
        return getName();
    }

    @NotNull public final String getName()
    {
        return proto.getName();
    }

    public final String getId()
    {
        return proto.getId();
    }

    public final Environment getEnv()
    {
        return env;
    }

    @NotNull public ProjectElement getElement()
    {
        if (element == null) {
            throw new IllegalStateException("Not activated element: " + getName());
        }

        return element;
    }

    public final File getBasedir()
    {
        return env.getBaseDir();
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
        return getBuilder().commands();
    }

    public final ProjectElement activate()
    {
        return env.activate(proto);
    }

    public Command findCommand(String commandName)
    {
        return getBuilder().commands().get(commandName);
    }

    /**
     * Return the Absolute file for the content of the project/module
     * This method works regardless of the Module being activated.
     * @return The File for the content of the project/module
     */
    @NotNull public File getDirFile()
    {
        File result = new File(expand(proto.getDir()));

        if (!result.isAbsolute()) {
            File basedir = new File(expand(proto.basedir));

            basedir = FileUtils.normalizeFile(basedir);

            result = new File(basedir, result.getPath());
        }

        return result;
    }

    public void remove()
    {
        ProjectBuilder.getInstance().remove(this);
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

    protected abstract void build(ProjectBuilder pb, String commandName);

    protected void initDependencyGraph() {}

    void activate(@NotNull ProjectElement activatedElement)
    {
        element = activatedElement;
    }

    void markExecuted(Command cmd)
    {
        executedCommands.add(cmd);
    }

    boolean notExecuted(Command cmd)
    {
        return !executedCommands.contains(cmd);
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

    /**
    * Process the string expanding property values.
    * The `$' character introduces property expansion.
    * The property  name  or  symbol  to  be expanded  may be enclosed in braces,
    * which are optional but serve to protect the variable to be expanded from characters
    * immediately following it which could be interpreted as part of the name.
    * When braces are used, the matching ending brace is the first `}' not escaped by a backslash
    *
    * @param string The string to be expanded.
    * @return An String with properties expanded.
    */
    private String expand(String string)
    {
        return env.expand(string);
    }

    @NotNull private CommandBuilder getBuilder()
    {
        if (builder == null) {
            builder = new CommandBuilder(proto);
        }

        return builder;
    }
}
