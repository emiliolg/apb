

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


package apb.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import apb.Apb;
import apb.Environment;

import junit.framework.TestCase;

import org.jetbrains.annotations.NotNull;

import static apb.tasks.CoreTasks.delete;

public abstract class TaskTestCase
    extends TestCase
{
    //~ Instance fields ......................................................................................

    @NotNull protected Environment env;

    @NotNull protected File basedir;
    @NotNull protected File datadir;

    //~ Methods ..............................................................................................

    @Override protected void setUp()
        throws IOException
    {
        basedir = new File("tmp");
        final String path = basedir.getAbsolutePath();

        Map<String, String> props = new HashMap<String, String>();
        props.put("basedir", path);
        props.put("color", Boolean.FALSE.toString());
        props.put("debug", "task_info");

        env = Apb.createBaseEnvironment(props);

        if (basedir.exists()) {
            delete(basedir).execute();
        }

        if (!basedir.mkdirs()) {
            throw new IOException("Cannot create temporary directory: '" + path + "' for tests.");
        }

        datadir = new File(env.expand("$datadir"));
    }

    protected File dataFile(String name)
    {
        return new File(datadir, name);
    }

    protected String dataPath(String name)
    {
        return dataFile(name).getAbsolutePath();
    }

    // truncate to seconds
    protected long currentTime()
    {
        return System.currentTimeMillis() / 1000 * 1000;
    }
}
