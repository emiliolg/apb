

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

public abstract class Command
{
    //~ Instance fields ......................................................................................

    @Nullable private List<Command> dependencies;

    @NotNull private final String description;

    @NotNull private final String  name;
    @Nullable private final String nameSpace;
    private boolean recursive;

    //~ Constructors .........................................................................................

    protected Command(@NotNull String nameSpace, @NotNull String name, @NotNull String description, boolean recursive)
    {
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
    }

    //~ Methods ..............................................................................................

    public abstract void invoke(ProjectElement projectElement, Environment env);

    @NotNull public String getQName()
    {
        return nameSpace == null ? name : nameSpace + ":" + name;
    }

    @NotNull public String getDescription()
    {
        return description;
    }

    @NotNull public Iterable<Command> getDependencies()
    {
        if (dependencies == null) {
            final ArrayList<Command> list = new ArrayList<Command>();
            tsort(list, new IdentitySet<Command>());
            dependencies = list;
        }

        return dependencies;
    }

    @NotNull public List<Command> getDirectDependencies()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Command && getQName().equals(((Command) obj).getQName());
    }

    public final String toString()
    {
        return getQName();
    }

    public final boolean isDefault()
    {
        return nameSpace == null && name.equals(DEFAULT_COMMAND);
    }

    @NotNull public String getNameSpace()
    {
        return nameSpace == null ? "" : nameSpace;
    }
    public boolean hasNameSpace()
    {
        return nameSpace != null && !nameSpace.isEmpty();
    }

    @NotNull public String getName()
    {
        return name;
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

    @NonNls public static final String DEFAULT_COMMAND = "default";

    final public boolean isRecursive() { return recursive; }
}
