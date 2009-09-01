

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
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

// User: emilio
// Date: Aug 24, 2009
// Time: 5:46:51 PM

/**
 * Entry point to common APB services
 */
public class Apb
{
    //~ Methods ..............................................................................................

    public static File applicationJarFile()
    {
        String url = Main.class.getResource("").toExternalForm();
        int    ind = url.lastIndexOf('!');

        if (ind == -1 || !url.startsWith(JAR_FILE_URL_PREFIX)) {
            throw new BuildException("Can't not find 'apb' jar " + url);
        }

        return new File(url.substring(JAR_FILE_URL_PREFIX.length(), ind));
    }

    /**
     * Load the projectpath list.
     */
    public static Set<File> loadProjectPath()
    {
        Set<File> result = new LinkedHashSet<File>();
        String    path = System.getenv("APB_PROJECT_PATH");
        String    path2 = getEnv().getProperty("project.path", "");

        if (!path2.isEmpty()) {
            path = path == null ? path2 : path + File.pathSeparator + path2;
        }

        if (path.isEmpty()) {
            path = "./project-definitions";
        }

        for (String p : path.split(File.pathSeparator)) {
            File dir = new File(p);

            if (dir.isAbsolute() && !dir.isDirectory()) {
                getEnv().logWarning(Messages.INV_PROJECT_DIR(dir));
            }

            result.add(dir);
        }

        return result;
    }

    /**
     * Get an instance of the current Environment
     * @return An instance of the current Environment
     */
    @NotNull public static Environment getEnv()
    {
        final Environment result = env.get();

        if (result == null) {
            throw new IllegalStateException("Environment not initialized");
        }

        return result;
    }

    static void setEnv(Environment e)
    {
        env.set(e);
    }

    //~ Static fields/initializers ...........................................................................

    private static final InheritableThreadLocal<Environment> env = new InheritableThreadLocal<Environment>();

    private static final String JAR_FILE_URL_PREFIX = "jar:file:";
}
