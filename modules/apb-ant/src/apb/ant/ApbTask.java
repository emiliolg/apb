

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
import java.util.HashMap;
import java.util.Map;

import apb.ApbService;
import apb.Environment;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ApbTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private final ApbService apb;

    private boolean                   recurse;
    private final Map<String, String> properties;

    private String command;
    private String defdir;
    private String module;

    //~ Constructors .........................................................................................

    public ApbTask()
        throws Exception
    {
        properties = new HashMap<String, String>();

        apb = ApbService.Factory.create();
        apb.init(new AntLogger(this, apb), properties);
        recurse = true;
    }

    //~ Methods ..............................................................................................

    public void execute()
        throws BuildException
    {
        super.execute();

        final Environment env = apb.getEnvironment();
        env.setNonRecursive(!recurse);

        if (module == null) {
            throw new BuildException("You must specify a module name");
        }

        try {
            final File dir = defdir == null ? null : new File(defdir);
            apb.build(dir, module, command);
        }
        catch (Throwable throwable) {
            throw new BuildException(throwable);
        }
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public void setDefdir(String defdir)
    {
        this.defdir = defdir;
    }

    public void setRecurse(boolean v)
    {
        recurse = v;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public String getDefdir()
    {
        return defdir;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }
}
