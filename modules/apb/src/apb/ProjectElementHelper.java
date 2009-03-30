

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

import apb.utils.IdentitySet;

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

    public Environment env;

    private final List<ProjectElementHelper> allElements;
    private CommandBuilder                   builder;
    @Nullable private ProjectElement         element;
    private Set<String>                      executedCommands;

    @NotNull private final ProjectElement proto;
    private boolean                       topLevel;

    //~ Constructors .........................................................................................

    protected ProjectElementHelper(@NotNull ProjectElement element, @NotNull Environment environment)
    {
        proto = element;
        env = environment;
        allElements = new ArrayList<ProjectElementHelper>();
        executedCommands = new HashSet<String>();
        env.addHelper(this);
    }

    //~ Methods ..............................................................................................

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

    public String toString()
    {
        return getName();
    }

    @NotNull public String getName()
    {
        return proto.getName();
    }

    public String getId()
    {
        return proto.getId();
    }

    public Environment getEnv()
    {
        return env;
    }

    public List<ProjectElementHelper> getAllElements()
    {
        return allElements;
    }

    public List<ModuleHelper> listAllModules()
    {
        List<ModuleHelper> result = new ArrayList<ModuleHelper>();

        for (ProjectElementHelper helper : allElements) {
            if (helper instanceof ModuleHelper) {
                final ModuleHelper mod = (ModuleHelper) helper;
                result.add(mod);

                for (TestModule testModule : mod.getModule().tests()) {
                    result.add((ModuleHelper) env.getHelper(testModule));
                }
            }
        }

        return result;
    }

    @NotNull public ProjectElement getElement()
    {
        if (element == null) {
            throw new IllegalStateException("Not activated element: " + getName());
        }

        return element;
    }

    public File getBasedir()
    {
        return env.getBaseDir();
    }

    public void setTopLevel(boolean b)
    {
        topLevel = b;
    }

    public boolean isTopLevel()
    {
        return topLevel;
    }

    public String getJdkName()
    {
        return getElement().jdk;
    }

    public void build(String commandName)
    {
        for (ProjectElementHelper h : getAllElements()) {
            env.logVerbose("About to execute %s.%s\n", h.getName(), commandName);

            if (!h.execute(commandName) && h == this) {
                env.handle("Invalid command: " + commandName);
            }
        }
    }

    public long lastModified()
    {
        return env.sourceLastModified(getElement().getClass());
    }

    public Collection<Command> listCommands()
    {
        return getBuilder().listCommands();
    }

    public Command findCommand(String commandName)
    {
        return getBuilder().commands().get(commandName);
    }

    protected abstract Iterable<? extends ProjectElementHelper> getChildren();

    void activate(@NotNull ProjectElement activatedElement)
    {
        element = activatedElement;
    }

    void initDependencyGraph()
    {
        // Topological Sort elements
        tsort(allElements, new IdentitySet<ProjectElementHelper>());

        if (env.isVerbose()) {
            env.logVerbose("Dependencies for: %s = %s\n", getName(), allElements.toString());
        }
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     * @param elements All descendant elements
     * @param visited  Already visited elements
     */
    private void tsort(List<ProjectElementHelper> elements, IdentitySet<ProjectElementHelper> visited)
    {
        for (ProjectElementHelper dependency : getChildren()) {
            if (!visited.contains(dependency)) {
                visited.add(dependency);
                dependency.tsort(elements, visited);
            }
        }

        elements.add(this);
    }

    private boolean execute(@NotNull String commandName)
    {
        boolean result = true;

        if (notExecuted(commandName)) {
            Command command = findCommand(commandName);

            if (command == null) {
                result = false;
            }
            else {
                ProjectElement projectElement = env.activate(proto);

                for (Command cmd : command.getDependencies()) {
                    if (notExecuted(cmd.getQName())) {
                        env.setCurrentCommand(cmd);
                        markExecuted(cmd);
                        cmd.invoke(projectElement, env);
                    }
                }

                env.setCurrentCommand(null);
                env.deactivate();
            }
        }

        return result;
    }

    @NotNull private CommandBuilder getBuilder()
    {
        if (builder == null) {
            builder = new CommandBuilder(proto);
        }

        return builder;
    }

    private void markExecuted(Command cmd)
    {
        executedCommands.add(cmd.getQName());
    }

    private boolean notExecuted(String commandName)
    {
        return !executedCommands.contains(commandName);
    }
}
