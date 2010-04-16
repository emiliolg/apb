

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

/**
 * A set of common constants
 */
public interface Constants
{
    //~ Instance fields ......................................................................................

    String JAR_FILE_URL_PREFIX = "jar:file:";

    //~ Static fields/initializers ...........................................................................

    /**
     * Property names
     */
    String EXT_PATH_PROPERTY = "ext.path";
    String PROJECT_PATH_EXCLUDE_PROPERTY = "project.path.exclude";
    String REPOSITORY_PROPERTY = "repository";
    String DEFINITIONS_CACHE_PROPERTY = "definitions.cache";

    /**
     * Environment variables
     */
    String APB_HOME_ENV = "APB_HOME";

    /**
     * Default directories
     */
    String APB_DIR = ".apb";

    /**
     * Default file names
     */
    String APB_PROPERTIES = "apb.properties";
    String DEFINITIONS_CACHE = "definitions.cache";

    /**
     * Default extensions
     */
    String JAVA_EXT = ".java";

    /**
     * Default values
     */
    String DEFAULT_REPOSITORY = "http://mirrors.ibiblio.org/pub/mirrors/maven2";

    /**
     * Misc constants
     */
    int    MB = 1024 * 1024;
    String UTF8 = "UTF-8";

    /**
     * The name of the <code>default</code> command.
     * Invoking this command will invoke the one defined with the apb.metadata.DefaultTarget annotation.
     */
    String DEFAULT_COMMAND = "default";

    /**
     * The emma jar name
     */
    String EMMA_JAR = "emma.jar";

    /**
     * The apb jar name
     */
    String APB_JAR = "apb.jar";

    /**
     * The directory for jars
     */
    String LIB_DIR = "lib";
}
