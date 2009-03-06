
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

package apb;

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;
import apb.utils.IdentitySet;
import apb.utils.NameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
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
    private ProjectElement                   element;
    private Set<String>                      executedCommands;
    private boolean                          topLevel;

    //~ Constructors .........................................................................................

    public ProjectElementHelper(ProjectElement element, Environment env)
    {
        this.element = element;
        this.env = env;
        allElements = new ArrayList<ProjectElementHelper>();
        executedCommands = new HashSet<String>();
    }

    //~ Methods ..............................................................................................

    @NotNull
    public static ProjectElementHelper create(ProjectElement element, Environment environment)
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

    public String getName()
    {
        return element.getName();
    }

    public String getId()
    {
        return NameUtils.idFromJavaId(getName());
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

    public ProjectElement getElement()
    {
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
        return element.jdk;
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

    protected abstract List<? extends ProjectElementHelper> addChildren();

    void activate() {}

    void initDependencyGraph()
    {
        // Topological Sort elements
        tsort(allElements, addChildren(), new IdentitySet<ProjectElementHelper>());
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     * @param elements All descendant elements
     * @param children First level
     * @param visited  Already visited elements
     */
    private void tsort(List<ProjectElementHelper> elements, List<? extends ProjectElementHelper> children,
                       IdentitySet<ProjectElementHelper> visited)
    {
        for (int i = children.size() - 1; i >= 0; i--) {
            ProjectElementHelper dependency = children.get(i);

            if (!visited.contains(dependency)) {
                visited.add(dependency);
                dependency.tsort(elements, children, visited);
            }
        }

        elements.add(this);
    }

    private boolean execute(String commandName)
    {
        boolean result = true;

        if (notExecuted(commandName)) {
            Command command = Command.findCommand(element, commandName);

            if (command == null) {
                result = false;
            }
            else {
                ProjectElement projectElement = env.activate(element);

                for (Command cmd : command.getAllCommands()) {
                    if (notExecuted(cmd.getName())) {
                        env.setCurrentCommand(cmd);
                        markExecuted(cmd.getName());
                        cmd.invoke(projectElement, env);
                    }
                }

                env.setCurrentCommand(null);
                env.deactivate();
            }
        }

        return result;
    }

    private void markExecuted(String commandName)
    {
        executedCommands.add(commandName);
    }

    private boolean notExecuted(String commandName)
    {
        return !executedCommands.contains(commandName);
    }
    }
