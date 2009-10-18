

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apb.metadata.ProjectElement;

import apb.utils.IdentitySet;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class defined commands to be run over a Module or Project.
 * Apb automatically implements instances of this class for each method annotated as a {@link apb.metadata.BuildTarget}.
 * You can also provide implementation of this class to extend apb functionality.
 */
public abstract class Command
    implements Comparable<Command>
{
    //~ Instance fields ......................................................................................

    @Nullable private List<Command> dependencies;

    @NotNull private final String description;

    @NotNull private final String                          name;
    @Nullable private final String                         nameSpace;
    private final boolean                                  recursive;
    @NotNull private final Class<? extends ProjectElement> targetElementClass;

    //~ Constructors .........................................................................................

    /**
     * Constructs a command with the specified arguments
     * @param targetElementClass The class of ProjectElements this command can be applied
     * @param nameSpace    The namespace of the command.
     *                     (All commands in a given extension must have the same namespace)
     * @param name         The name of the command (The name must be nique inside a given  namespace)
     * @param description  A description for the command
     * @param recursive    Wheter APB must invoke the command recursively to all module dependencies.
     */
    protected Command(Class<? extends ProjectElement> targetElementClass, @NotNull String nameSpace,
                      @NotNull String name, @NotNull String description, boolean recursive)
    {
        this.targetElementClass = targetElementClass;
        this.name = name;
        this.description = description;
        this.nameSpace = nameSpace;
        this.recursive = recursive;
    }

    Command(@NotNull String name, @NotNull String description, boolean recursive)
    {
        this.name = name;
        this.description = description;
        nameSpace = null;
        this.recursive = recursive;
        targetElementClass = ProjectElement.class;
    }

    //~ Methods ..............................................................................................

    /**
     * This is the method that will be invoked when running this command over a Module or Project
     * @param projectElement The Module or Project to be processes.
     */
    public abstract void invoke(ProjectElement projectElement);

    /**
     * Returns the, qualified, name of the command
     * @return The qualified name of the command
     */
    @NotNull public final String getName()
    {
        return nameSpace == null ? name : nameSpace + ":" + name;
    }

    /**
     * Returns a description for the command
     * @return a description for the command
     */
    @NotNull public String getDescription()
    {
        return description;
    }

    @Override public boolean equals(Object obj)
    {
        return this == obj || obj instanceof Command && getName().equals(((Command) obj).getName());
    }

    @Override public int hashCode()
    {
        return 31 * name.hashCode() + (nameSpace != null ? nameSpace.hashCode() : 0);
    }

    public final String toString()
    {
        return getName();
    }

    /**
     * Returns the namespace of the command.
     * The namespace is not empty for extension commands.
     * @return The namespace of the command.
     */
    @NotNull public final String getNameSpace()
    {
        return nameSpace == null ? "" : nameSpace;
    }

    /**
     * Returns true if the command has a namespace (What implies that is an extension command)
     * @return true if the command has a namespace, false otherwise.
     */
    public final boolean hasNameSpace()
    {
        return nameSpace != null && !nameSpace.isEmpty();
    }

    /**
     * Returns true if the command must be applied recursively to the dependent Modules
     */
    public final boolean isRecursive()
    {
        return recursive;
    }

    @Override public int compareTo(Command o)
    {
        return getName().compareTo(o.getName());
    }

    /**
     * Return the class of objects this command can be applied to
     */
    @NotNull public Class<? extends ProjectElement> getTargetElementClass()
    {
        return targetElementClass;
    }

    @NotNull Iterable<Command> getDependencies()
    {
        if (dependencies == null) {
            final ArrayList<Command> list = new ArrayList<Command>();
            tsort(list, new IdentitySet<Command>());
            dependencies = list;
        }

        return dependencies;
    }

    @NotNull List<Command> getDirectDependencies()
    {
        return Collections.emptyList();
    }

    /**
     * Is this the default command
     * @return true if this is the default command, false otherwise
     */
    final boolean isDefault()
    {
        return nameSpace == null && name.equals(DEFAULT_COMMAND);
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     * @param elements All descendant elements
     * @param visited  Already visited elements
     */
    private void tsort(List<Command> elements, IdentitySet<Command> visited)
    {
        for (Command dependency : getDirectDependencies()) {
            if (!visited.contains(dependency)) {
                visited.add(dependency);
                dependency.tsort(elements, visited);
            }
        }

        elements.add(this);
    }

    //~ Static fields/initializers ...........................................................................

    /**
     * The name of the <code>default</code> command.
     * Invoking this command will invoke the one defined with the {@link apb.metadata.DefaultTarget} annotation.
     */
    @NonNls public static final String DEFAULT_COMMAND = "default";
}
