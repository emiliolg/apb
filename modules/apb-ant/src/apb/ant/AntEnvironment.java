

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

import apb.Environment;
import apb.ProjectBuilder;

import apb.utils.StringUtils;

import org.apache.tools.ant.Project;

//
// User: emilio
// Date: Dec 10, 2008
// Time: 6:34:03 PM

//
public class AntEnvironment
    extends Environment
{
    //~ Instance fields ......................................................................................

    private ApbTask task;

    //~ Constructors .........................................................................................

    public AntEnvironment(ApbTask task)
    {
        postInit();
        this.task = task;
        setFailOnError(true);
    }

    //~ Methods ..............................................................................................

    public void logInfo(String msg, Object... args)
    {
        log(Project.MSG_INFO, msg, args);
    }

    public void logWarning(String msg, Object... args)
    {
        log(Project.MSG_WARN, msg, args);
    }

    public void logSevere(String msg, Object... args)
    {
        log(Project.MSG_ERR, msg, args);
    }

    public void logVerbose(String msg, Object... args)
    {
        log(Project.MSG_VERBOSE, msg, args);
    }

    private void log(int level, String msg, Object... args)
    {
        msg = StringUtils.appendIndenting(ProjectBuilder.getInstance().makeStandardHeader() + ' ', msg);
        task.log(args.length == 0 ? msg : String.format(msg, args), level);
    }
}
