

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


package apb.ant;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.Map;

import apb.Command;
import apb.ProjectBuilder;
import apb.Apb;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Dec 10, 2008
// Time: 12:55:25 PM

//
public class ApbTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private String command = Command.DEFAULT_COMMAND;
    private String          defdir;

    @NotNull private final AntEnvironment env;
    private String                        module;
    private boolean                       recurse;
    private Map<String, String> properties;

    //~ Constructors .........................................................................................

    public ApbTask()
    {
        env = new AntEnvironment(this);
        recurse = true;
    }

    //~ Methods ..............................................................................................

    public void execute()
        throws BuildException
    {
        super.execute();

        final Set<File> projectPath;

        if (defdir == null) {
            projectPath = Apb.loadProjectPath();
        }
        else {
            File f = new File(defdir);

            if (!f.isDirectory()) {
                throw new BuildException("Non existent project definiton directory: '" + defdir + '\'');
            }

            env.logVerbose("Definition directory = '%s'", defdir);
            projectPath = Collections.singleton(f);
        }

        if (!recurse) {
            env.setNonRecursive();
        }

        if (module == null) {
            throw new BuildException("You must specify a module name");
        }

        try {
            ProjectBuilder b = new ProjectBuilder(env, projectPath);
            b.build(module, command);
        }
        catch (Throwable throwable) {
            throw new BuildException(throwable);
        }
    }

    public void setModule(@NotNull String module)
    {
        this.module = module;
    }

    public void setDefdir(@NotNull String defdir)
    {
        this.defdir = defdir;
    }

    public void setRecurse(boolean v)
    {
        recurse = v;
    }

    public void setCommand(@NotNull String command)
    {
        this.command = command;
    }

    public String getDefdir()
    {
        return defdir;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
