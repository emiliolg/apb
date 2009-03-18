
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

package apb.ant;

import apb.Main;
import apb.Command;

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

    private AntEnvironment env;
    private String         module;

    //~ Constructors .........................................................................................

    public ApbTask()
    {
        env = new AntEnvironment(this);
    }

    //~ Methods ..............................................................................................

    public void execute()
        throws BuildException
    {
        super.execute();

        if (defdir != null) {
            env.initDefinitionDir(defdir);
        }

        if (module == null) {
            throw new BuildException("You must specify a module name");
        }

        try {
            Main.execute(env, module, command);
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

    public void setCommand(@NotNull String command)
    {
        this.command = command;
    }

    public String getTaskName()
    {
        return super.getTaskName() + " " + module + "." + command;
    }

    public String getDefdir()
    {
        return defdir;
    }
}
