
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

package apb.metadata;

import apb.Environment;
import apb.tasks.IdeaTask;
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
     * The module version
     */
    @BuildProperty public String version = "";
    /**
     * The jdk version used for the module
     */
    @BuildProperty public String jdk = "1.6";

    //~ Methods ..............................................................................................

    @BuildTarget public abstract void clean(Environment env);

    @BuildTarget public abstract void resources(Environment env);

    @BuildTarget(depends = "resources")
    public abstract void compile(Environment env);

    @BuildTarget(depends = "compile")
    public abstract void compileTests(Environment env);

    @BuildTarget(depends = "compile-tests")
    public abstract void runTests(Environment env);

    @BuildTarget(
                 depends = "compile",
                 name = "package"
                )
    public abstract void packageit(Environment env);

    @BuildTarget public void genIdea(Environment env)
    {
        new IdeaTask(env).execute();
    }

    public String getName()
    {
        return NameUtils.name(getClass());
    }

    public String toString()
    {
        return getName();
    }
}
