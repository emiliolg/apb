

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

import apb.Environment;
import apb.utils.NameUtils;

/**
 * The base class for project elements
 */
@DefaultTarget("package")
public abstract class ProjectElement
{
    //~ Instance fields ......................................................................................

    /**
     * The base directory. All file references are based on this one
     * The default assumes that you place all your definitions
     * under a sub-directory where each module content directory is placed.
     */
    @BuildProperty public String basedir = "$projects-home/..";

    /**
     * A description of the Module.
     */
    @BuildProperty public String description = "";

    /**
     * The "group" (Usually organization name) for this element.
     */
    @BuildProperty public String group = "";

    /**
     * The jdk version used for the module
     */
    @BuildProperty public String jdk = "1.6";

    /**
     * The module version
     */
    @BuildProperty public String version = "";

    //~ Methods ..............................................................................................

    @BuildTarget(description = "Deletes all output directories (compiled code and packages).")
    public abstract void clean(Environment env);

    @BuildTarget(description = "Copy (eventually filtering) resources to the output directory.")
    public abstract void resources(Environment env);

    @BuildTarget(
                 depends = "resources",
                 description = "Compile classes and place them in the output directory."
                )
    public abstract void compile(Environment env);

    @BuildTarget(
                 depends = "compile",
                 description = "Compile test classes."
                )
    public abstract void compileTests(Environment env);

    @BuildTarget(
                 depends = "compile-tests",
                 description =
                 "Test the compiled sources, generating reports and (optional) coverage information."
                )
    public abstract void runTests(Environment env);

    @BuildTarget(
                 depends = "compile",
                 name = "package",
                 description =
                 "Creates a jar file containing the compiled classes and resources of the module."
                )
    public abstract void packageit(Environment env);

//    @BuildTarget(description = "Generate Idea project and module files.")
//    public void genIdea(Environment env)
//    {
//        IdeaTask.execute(env);
//    }

    public String getName()
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
