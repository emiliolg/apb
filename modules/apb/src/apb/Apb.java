

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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import apb.utils.ClassUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.StringUtils.isEmpty;
import static apb.utils.StringUtils.isNotEmpty;

/**
 * Entry point to common APB services
 */
public class Apb
{
    //~ Methods ..............................................................................................

    public static File applicationJarFile()
    {
        return ClassUtils.jarFromClass(Apb.class);
    }

    /**
     * Load the projectpath list.
     */
    public static Set<File> loadProjectPath()
    {
        Set<File> result = new LinkedHashSet<File>();
        String    path = System.getenv("APB_PROJECT_PATH");
        String    path2 = getEnv().getProperty("project.path", "");

        if (isNotEmpty(path2)) {
            path = isEmpty(path) ? path2 : path + File.pathSeparator + path2;
        }

        if (isEmpty(path)) {
            path = "./DEFS";
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
        final Environment result = currentEnvironment.get();

        if (result == null) {
            throw new IllegalStateException("Environment not initialized");
        }

        return result;
    }

    /**
     * Get the Apb home directory
     * Returns null if we cannot find it.
     */
    @Nullable public static File getHome()
    {
        try {
            return applicationJarFile().getParentFile().getParentFile();
        }
        catch (BuildException e) {
            return null;
        }
    }

    public static Environment createBaseEnvironment()
    {
        return createBaseEnvironment(Collections.<String, String>emptyMap());
    }

    public static Environment createBaseEnvironment(@NotNull final Map<String, String> ps)
    {
        return createBaseEnvironment(new StandaloneLogger(), ps);
    }

    public static Environment createBaseEnvironment(@NotNull ApbOptions options)
    {
        final StandaloneLogger logger = new StandaloneLogger();
        final BaseEnvironment  env = new BaseEnvironment(logger, options.definedProperties());
        setCurrentEnv(env);
        logger.setColor(env.getBooleanProperty("color", true));
        options.initEnv(env);
        initProxies(env);
        return env;
    }

    public static Environment createBaseEnvironment(@NotNull Logger                    logger,
                                                    @NotNull final Map<String, String> ps)
    {
        final BaseEnvironment env = new BaseEnvironment(logger, ps);
        setCurrentEnv(env);
        initProxies(env);
        return env;
    }

    public static String makeStandardHeader()
    {
        final ProjectBuilder pb = getCurrentProjectBuilder();
        return pb == null ? "" : pb.standardHeader();
    }

    public static void exit(int status)
    {
        if (avoidSystemExit) {
            throw new ExitException();
        }

        System.exit(status);
    }

    public static void setAvoidSystemExit(boolean b)
    {
        avoidSystemExit = b;
    }

    static Environment setCurrentEnv(Environment e)
    {
        Environment result = currentEnvironment.get();
        currentEnvironment.set(e);
        return result;
    }

    @Nullable static ProjectBuilder getCurrentProjectBuilder()
    {
        final Environment e = currentEnvironment.get();
        return e != null && e instanceof DefaultEnvironment
               ? ((DefaultEnvironment) e).getCurrentProjectBuilder() : null;
    }

    private static void initProxies(Environment env)
    {
        Proxy proxy = Proxy.getDefaultProxy(env);

        if (proxy != null && !proxy.getHost().isEmpty()) {
            System.setProperty("http.proxyHost", proxy.getHost());

            if (proxy.getPort() > 0) {
                System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
            }

            if (!proxy.getNonProxyHosts().isEmpty()) {
                System.setProperty("http.nonProxyHosts", proxy.getNonProxyHosts());
            }
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static boolean avoidSystemExit;

    private static final InheritableThreadLocal<Environment> currentEnvironment =
        new InheritableThreadLocal<Environment>();

    //~ Inner Classes ........................................................................................

    public static class ExitException
        extends RuntimeException
    {
        private static final long serialVersionUID = 2893150484674194063L;
    }
}
