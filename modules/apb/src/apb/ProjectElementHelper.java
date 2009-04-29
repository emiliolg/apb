

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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

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

    private CommandBuilder builder;

    @NotNull private final ProjectElement proto;
    private boolean                       topLevel;

    //~ Constructors .........................................................................................

    protected ProjectElementHelper(@NotNull ProjectElement element, @NotNull Environment environment)
    {
        proto = element;
        env = environment;
        env.addHelper(this);
        executedCommands = new HashSet<Command>();
    }

    //~ Methods ..............................................................................................

    public abstract Set<ModuleHelper> listAllModules();

    @NotNull public static ProjectElementHelper create(ProjectElement element, Environment environment)
    {
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
        return env.sourceLastModified(getElement().getClass());
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
        File result = new File(env.expand(proto.getDir()));

        if (!result.isAbsolute()) {
            File basedir = new File(env.expand(proto.basedir));

            try {
                basedir = basedir.getCanonicalFile();
            }
            catch (IOException ignore) {
                // Keep non canonized version of basedir
            }

            result = new File(basedir, result.getPath());
        }

        return result;
    }

    protected void initDependencyGraph() {}

    protected void execute(@NotNull String commandName)
    {
        Command command = findCommand(commandName);

        if (command != null && notExecuted(command)) {
            ProjectElement projectElement = activate();
            long           ms = startExecution(command);

            for (Command cmd : command.getDependencies()) {
                if (notExecuted(cmd)) {
                    env.setCurrentCommand(cmd);
                    markExecuted(cmd);
                    cmd.invoke(projectElement, env);
                }
            }

            env.setCurrentCommand(null);
            endExecution(command, ms);
            env.deactivate();
        }
    }

    abstract void build(String commandName);

    void activate(@NotNull ProjectElement activatedElement)
    {
        element = activatedElement;
    }

    @NotNull private CommandBuilder getBuilder()
    {
        if (builder == null) {
            builder = new CommandBuilder(proto);
        }

        return builder;
    }

    private void endExecution(Command command, long ms)
    {
        if (env.isVerbose()) {
            ms = System.currentTimeMillis() - ms;
            long free = Runtime.getRuntime().freeMemory() / MB;
            long total = Runtime.getRuntime().totalMemory() / MB;
            env.logVerbose("Execution of '%s'. Finished in %d milliseconds. Memory usage: %dM of %dM\n",
                           command, ms, total - free, total);
        }
    }

    private long startExecution(Command command)
    {
        long result = 0;

        if (env.isVerbose()) {
            env.logVerbose("About to execute '%s'\n", command);
            result = System.currentTimeMillis();
        }

        return result;
    }

    private void markExecuted(Command cmd)
    {
        executedCommands.add(cmd);
    }

    private boolean notExecuted(Command cmd)
    {
        return !executedCommands.contains(cmd);
    }

    //~ Static fields/initializers ...........................................................................

    private static final long MB = (1024 * 1024);
}
