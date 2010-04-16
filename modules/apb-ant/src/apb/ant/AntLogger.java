

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

import apb.ApbService;
import apb.Logger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
//
// User: emilio
// Date: Sep 1, 2009
// Time: 3:34:39 PM

class AntLogger
    implements Logger
{
    //~ Instance fields ......................................................................................

    private ApbService apb;

    private final Task antTask;

    //~ Constructors .........................................................................................

    AntLogger(Task task, ApbService apb)
    {
        antTask = task;
        this.apb = apb;
    }

    //~ Methods ..............................................................................................

    @Override public void log(Level level, String msg, Object... args)
    {
        int l = convertLevel(level);

        if (args.length != 0) {
            //noinspection ImplicitArrayToString
            msg = String.format(msg, args);
        }

        msg = apb.prependStandardHeader(msg);

        // Remove trailing \n, as Task.log() method will append one.
        msg = removeTrailingNL(msg);
        antTask.log(msg, l);
    }

    @Override public void setLevel(Level level)
    {
        // Ignore
    }

    private static String removeTrailingNL(String msg)
    {
        int len = msg.length();

        //noinspection HardcodedLineSeparator
        if (len > 0 && msg.charAt(len - 1) == '\n') {
            len--;
        }

        //noinspection HardcodedLineSeparator
        if (len > 0 && msg.charAt(len - 1) == '\r') {
            len--;
        }

        return msg.substring(0, len);
    }

    private static int convertLevel(Level level)
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
