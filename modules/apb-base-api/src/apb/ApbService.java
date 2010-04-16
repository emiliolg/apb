

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
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static apb.Constants.JAR_FILE_URL_PREFIX;

public interface ApbService
{
    //~ Methods ..............................................................................................

    Environment getEnvironment();

    void build(File dir, String module, String command)
        throws DefinitionException;

    String prependStandardHeader(String msg);

    void init(Logger logger, Map<String, String> properties);

    //~ Inner Classes ........................................................................................

    public class Factory
    {
        /**
         * Return the default ApbService implementation
         * loading it from the specified jar file
         *
         * @return an object implementing an ApbService
         * @throws Exception
         */
        public static ApbService create(String apbJarFile)
            throws Exception
        {
            final File file = new File(apbJarFile);

            if (!file.exists()) {
                throw new FileNotFoundException("Cannot find '" + file.getAbsolutePath() + "'");
            }

            URL[]                       urls = { file.toURI().toURL() };
            ClassLoader                 cl = URLClassLoader.newInstance(urls, Factory.class.getClassLoader());
            Class<? extends ApbService> c = cl.loadClass(DEFAULT_APB_SERVICE).asSubclass(ApbService.class);
            return c.newInstance();
        }

        /**
         * Return the default ApbService implementation
         * Try to guess the location of the Apb.jar file
         *
         * @return an object implementing an ApbService
         * @throws Exception
         */
        public static ApbService create()
            throws Exception
        {
            // First see if the APB_HOME environment variable is defined

            String home = System.getenv(Constants.APB_HOME_ENV);

            // if null try to guess home from current jar
            if (home == null) {
                File currentJar = jarFromClass(ApbService.class);

                if (currentJar != null) {
                    home = currentJar.getParentFile().getParent();
                }
            }

            if (home != null) {
                final File jarFile = new File(home, Constants.LIB_DIR + File.separator + Constants.APB_JAR);
                return create(jarFile.getPath());
            }

            throw new FileNotFoundException("Cannot find '" + Constants.APB_JAR + "'");
        }

        private static File jarFromClass(Class<?> aClass)
        {
            String url = aClass.getResource(aClass.getSimpleName() + ".class").toExternalForm();
            int    ind = url.lastIndexOf('!');

            return ind == -1 || !url.startsWith(JAR_FILE_URL_PREFIX)
                   ? null : new File(url.substring(JAR_FILE_URL_PREFIX.length(), ind));
        }

        public static final String DEFAULT_APB_SERVICE = "apb.DefaultApbService";
    }
}
