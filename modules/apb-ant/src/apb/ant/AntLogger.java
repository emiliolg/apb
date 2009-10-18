

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

import apb.Apb;
import apb.Logger;

import apb.utils.StringUtils;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 1, 2009
// Time: 3:34:39 PM

class AntLogger
    implements Logger
{
    //~ Instance fields ......................................................................................

    private Task antTask;

    //~ Constructors .........................................................................................

    public AntLogger(Task task)
    {
        antTask = task;
    }

    //~ Methods ..............................................................................................

    @Override public void log(@NotNull Level level, @NotNull String msg, Object... args)
    {
        int l = convertLevel(level);
        msg = StringUtils.appendIndenting(Apb.makeStandardHeader() + ' ', msg);
        antTask.log(args.length == 0 ? msg : String.format(msg, args), l);
    }

    @Override public void setLevel(@NotNull Level level)
    {
        // Ignore
    }

    private int convertLevel(@NotNull Level level)
    {
        switch (level) {
        case INFO:
            return Project.MSG_INFO;
        case VERBOSE:
            return Project.MSG_VERBOSE;
        case SEVERE:
            return Project.MSG_ERR;
        case WARNING:
            return Project.MSG_WARN;
        default:
            throw new IllegalArgumentException(String.valueOf(level));
        }
    }
}
