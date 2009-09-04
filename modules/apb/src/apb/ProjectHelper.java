

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

import java.util.LinkedHashSet;
import java.util.Set;

import apb.metadata.Project;
import apb.metadata.ProjectElement;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 10, 2008
// Time: 12:33:47 PM

//
public class ProjectHelper
    extends ProjectElementHelper
{
    //~ Constructors .........................................................................................

    ProjectHelper(ProjectBuilder pb, @NotNull Project project)
    {
        super(pb, project);
        putProperty(PROJECT_PROP_KEY, getName());
        putProperty(PROJECT_PROP_KEY + ID_SUFFIX, getId());
        putProperty(PROJECT_PROP_KEY + DIR_SUFFIX, project.getDir());
    }

    //~ Methods ..............................................................................................

    public Project getProject()
    {
        return (Project) getElement();
    }

    public Set<ModuleHelper> listAllModules()
    {
        Set<ModuleHelper> result = new LinkedHashSet<ModuleHelper>();

        for (ProjectElement e : getProject().components()) {
            result.addAll(e.getHelper().listAllModules());
        }

        return result;
    }

    protected void build(ProjectBuilder pb, String commandName)
    {
        for (ProjectElement component : getProject().components()) {
            pb.build(component.getHelper(), commandName);
        }

        pb.execute(this, commandName);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String PROJECT_PROP_KEY = "project";
}
