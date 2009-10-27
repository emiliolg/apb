

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

import apb.ProjectHelper;

import org.jetbrains.annotations.NotNull;

/**
 * This class defines a Module for the building system
 * Every Module definition must inherit (directly or indirectly) from this class
*/
public class Project
    extends ProjectElement
{
    //~ Instance fields ......................................................................................

    /**
     * The "group" (Usually organization name) for this element.
     */
    @BuildProperty public String group = "";

    /**
     * The module version
     */
    @BuildProperty public String version = "";

    private ProjectElementList components = new ProjectElementList();

    //~ Methods ..............................................................................................

    @NotNull @Override public ProjectHelper getHelper()
    {
        return (ProjectHelper) super.getHelper();
    }

    public ProjectElementList components()
    {
        return components;
    }

    public void components(ProjectElement... ms)
    {
        components.addAll(ms);
    }
}
