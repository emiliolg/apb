
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

import apb.Command;
import apb.Environment;

import apb.utils.StringUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import org.jetbrains.annotations.NotNull;

import static apb.utils.StringUtils.isEmpty;
//
// User: emilio
// Date: Dec 10, 2008
// Time: 6:34:03 PM

//
public class AntEnvironment
    extends Environment
{
    //~ Instance fields ......................................................................................

    private Set<File> definitionDir;

    private ApbTask task;

    //~ Constructors .........................................................................................

    public AntEnvironment(ApbTask task)
    {
        loadProjectPath();
        this.task = task;
        setFailOnError(true);
    }

    //~ Methods ..............................................................................................

    public Set<File> getProjectPath()
    {
        return definitionDir == null ? super.getProjectPath() : definitionDir;
    }

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

    void initDefinitionDir(@NotNull String defdir)
    {
        File f = new File(defdir);

        if (!f.isDirectory()) {
            throw new BuildException("Non existent project definiton directory: '" + defdir + '\'');
        }

        logVerbose("Definition directory = '%s'", defdir);
        definitionDir = Collections.singleton(f);
    }

    private void log(int level, String msg, Object... args)
    {
        StringBuilder result = new StringBuilder();
        final String  currentModule = getCurrentName();

        if (!isEmpty(currentModule)) {
            result.append('[');
            result.append(currentModule);
            final Command cmd = getCurrentCommand();

            if (cmd != null) {
                result.append('.').append(cmd.getQName());
            }

            if (result.length() >= HEADER_LENGTH - 1) {
                result.setLength(HEADER_LENGTH - 1);
            }

            result.append(']');
            result.append(StringUtils.nChars(HEADER_LENGTH - result.length(), ' '));
        }

        msg = StringUtils.appendIndenting(result.toString() + ' ', msg);

        task.log(args.length == 0 ? msg : String.format(msg, args), level);
    }

    //~ Static fields/initializers ...........................................................................

    private static final int HEADER_LENGTH = 30;
}
