

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


package apb.tasks;

import java.util.concurrent.Callable;

import apb.Apb;
import apb.Environment;

import apb.utils.DebugOption;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 23, 2008
// Time: 5:23:24 PM

//
public abstract class Task
    implements Callable
{
    //~ Instance fields ......................................................................................

    @NotNull protected final Environment env;

    //~ Constructors .........................................................................................

    public Task(@NotNull Environment env)
    {
        this.env = env;
    }

    protected Task()
    {
        this(Apb.getEnv());
    }

    //~ Methods ..............................................................................................

    public abstract void execute();

    @NotNull public Environment getEnv()
    {
        return env;
    }

    public Object call()
    {
        execute();
        return null;
    }

    protected void logVerbose(String msg, Object... args)
    {
        if (env.mustShow(DebugOption.TASK_INFO)) {
            env.logVerbose(msg, args);
        }
    }
}
