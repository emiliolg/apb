

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


package apb.metadata;

import apb.ProjectBuilder;
import apb.ProjectElementHelper;

import apb.utils.NameUtils;

import org.jetbrains.annotations.NotNull;

/**
 * The base class for project elements
 */
public class ProjectElement
    implements Named
{
    //~ Instance fields ......................................................................................

    /**
     * The base directory. All file references are based on this one
     * The default assumes that you place all your definitions
     * under a sub-directory where each module content directory is placed.
     */
    @BuildProperty public String basedir = "$projects-home/..";

    /**
     * A description of the Element.
     */
    @BuildProperty public String description = "";

    @NotNull private final ProjectElementHelper helper;

    //~ Constructors .........................................................................................

    public ProjectElement()
    {
        helper = ProjectBuilder.register(NameRegistry.intern(this));
    }

    //~ Methods ..............................................................................................

    //    @BuildTarget(description = "Deletes all output directories (compiled code and packages).")
    //    public abstract void clean();

    @NotNull public ProjectElementHelper getHelper()
    {
        return helper;
    }

    /**
     * Initialization hook.
     * You can override this method to provide initializations AFTER
     * the Object is constructed. This can be useful to avoid problem with cyclic references.
     */
    public void init() {}

    @NotNull public String getName()
    {
        return NameUtils.name(getClass());
    }

    public String getId()
    {
        return NameUtils.idFromJavaId(getName());
    }

    public String getDir()
    {
        return NameUtils.dirFromId(getId());
    }

    public String toString()
    {
        return getName();
    }
}
