

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


package apb;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import apb.utils.FileUtils;
import apb.utils.StringUtils;

@SuppressWarnings("UnusedDeclaration")
public class DefaultApbService
    implements ApbService
{
    //~ Instance fields ......................................................................................

    private Environment env;

    //~ Methods ..............................................................................................

    @Override public void init(Logger logger, Map<String, String> properties)
    {
        env = Apb.createBaseEnvironment(logger, properties);
    }

    @Override public Environment getEnvironment()
    {
        if (env == null) {
            throw new IllegalStateException("Forgot to call init method?");
        }

        return env;
    }

    @Override public void build(File dir, String module, String command)
        throws DefinitionException
    {
        final Environment e = getEnvironment();
        final Set<File>   projectPath;

        if (dir == null) {
            projectPath = Apb.loadProjectPath();
        }
        else {
            FileUtils.validateDirectory(dir);
            projectPath = Collections.singleton(dir);
        }

        ProjectBuilder b = new ProjectBuilder(e, projectPath);
        b.build(e, module, command);
    }

    @Override public String prependStandardHeader(String msg)
    {
        return StringUtils.appendIndenting(Apb.makeStandardHeader() + ' ', msg);
    }
}
