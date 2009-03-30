

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
import java.util.List;

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
    //~ Instance fields ......................................................................................

    @NotNull private final List<ProjectElementHelper> components;

    //~ Constructors .........................................................................................

    ProjectHelper(@NotNull Project project, @NotNull Environment env)
    {
        super(project, env);
        components = new ArrayList<ProjectElementHelper>();

        for (ProjectElement module : project.components()) {
            components.add(env.getHelper(module));
        }
    }

    //~ Methods ..............................................................................................

    public Project getProject()
    {
        return (Project) getElement();
    }

    protected Iterable<? extends ProjectElementHelper> getChildren()
    {
        return components;
    }
}
