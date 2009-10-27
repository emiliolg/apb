

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of common constants
 */
public class Constants
{
    //~ Static fields/initializers ...........................................................................

    /**
     * Property names
     */
    public static final String EXT_PATH_PROPERTY = "ext.path";
    public static final String PROJECT_PATH_PROPERTY = "project.path";
    public static final String PROJECT_PATH_EXCLUDE_PROPERTY = "project.path.exclude";
    public static final String REPOSITORY_PROPERTY = "repository";
    public static final String DEFINITIONS_CACHE_PROPERTY = "definitions.cache";

    /**
     * Environment variables names
     */
    public static final String PROJECT_PATH_ENV_VARIABLE = "APB_PROJECT_PATH";

    /**
     * Default directories
     */
    public static final String APB_DIR = ".apb";

    /**
     * Default file names
     */
    public static final String APB_PROPERTIES = "apb.properties";
    public static final String DEFINITIONS_CACHE = "definitions.cache";

    /**
     * Default extensions
     */
    public static final String JAVA_EXT = ".java";

    /**
     * Default values
     */
    public static final String       DEFAULT_REPOSITORY = "http://mirrors.ibiblio.org/pub/mirrors/maven2";
    public static final Set<String>  DEFAULT_DIR_EXCLUDES =
        new HashSet<String>(java.util.Arrays.asList("CVS", "SCCS", ".svn", ".ade_path", ".arch-ids", ".bzr"));
    public static final List<String> DEFAULT_EXCLUDES =
        java.util.Arrays.asList(

                                // Miscellaneous typical temporary files
                                "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

                                // CVS
                                "**/CVS", "**/CVS/**", "**/.cvsignore",

                                // SCCS
                                "**/SCCS", "**/SCCS/**",

                                // Visual SourceSafe
                                "**/vssver.scc",

                                // Subversion
                                "**/.svn", "**/.svn/**",

                                // Oracle ADE
                                "**/.ade_path", "**/.ade_path/**",

                                // Arch
                                "**/.arch-ids", "**/.arch-ids/**",

                                //Bazaar
                                "**/.bzr", "**/.bzr/**",

                                //SurroundSCM
                                "**/.MySCMServerInfo",

                                // Mac
                                "**/.DS_Store");

    /**
     * Misc constants
     */
    public static final int    MB = 1024 * 1024;
    public static final String UTF8 = "UTF-8";
}
