

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
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import apb.utils.CollectionUtils;
import apb.utils.DebugOption;
import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents an Environment that includes common services like:
 * - Properties
 * - Options
 * - Logging
 * - Error handling
 */

final class BaseEnvironment
    extends DefaultEnvironment
{
    //~ Instance fields ......................................................................................

    /**
     * Control what to show when logging
     */

    @NotNull protected final EnumSet<DebugOption> debugOptions;

    /**
     * The directory where the project definition files are stored
     */
    @Nullable File projectsHome;

    /**
     * The set of jars that comprise the extension class path
     */
    @NotNull private final Set<File> extClassPath;

    private boolean failOnError = true;
    private boolean forceBuild;

    private boolean nonRecursive;

    /**
     * Override properties are argument properties
     * They take preference over the properties defined in project elements
     */
    @NotNull private final Map<String, String> overrideProperties;

    /**
     * Processing and messaging options
     */
    private boolean quiet;

    //~ Constructors .........................................................................................

    /**
     * Crate an Environment
     * @param override
     */
    BaseEnvironment(@NotNull Logger logger, Map<String, String> override)
    {
        super(logger);
        debugOptions = EnumSet.noneOf(DebugOption.class);

        CollectionUtils.copyProperties(properties, FileUtils.userProperties());

        // Read System Properties
        overrideProperties = new TreeMap<String, String>();
        CollectionUtils.copyProperties(overrideProperties, System.getProperties());
        overrideProperties.putAll(override);

        extClassPath = loadExtensionsPath(properties);
        initOptions();
    }

    //~ Methods ..............................................................................................

    /**
     * Get the Extension Jars to be searched when we compile definitions
     * @return the extension Jars to be searched when we compiled definitions
     */
    @NotNull public Collection<File> getExtClassPath()
    {
        return extClassPath;
    }

    /**
     * Returns true if log level is quiet
     * @return true if log level is quiet
     */
    public boolean isQuiet()
    {
        return quiet;
    }

    public void setQuiet()
    {
        quiet = true;
        logger.setLevel(Logger.Level.WARNING);
    }

    /**
     * Returns true if the build must NOT proceed recursive to the module dependecies
     */
    public boolean isNonRecursive()
    {
        return nonRecursive;
    }

    public void setNonRecursive(boolean b)
    {
        nonRecursive = b;
    }

    public void setFailOnError(boolean b)
    {
        failOnError = b;
    }

    @Override public String getId()
    {
        return "base";
    }

    /**
     * Returns true if we want the build to proceed unconditionally without checking file timestamps
     * @return true if we want the build to proceed unconditionally without checking file timestamps
     */
    public boolean forceBuild()
    {
        return forceBuild;
    }

    public void setForceBuild(boolean b)
    {
        forceBuild = b;
    }

    /**
     * Returns true if log level is verbose
     * @return true if log level is verbose
     */
    public boolean isVerbose()
    {
        return !debugOptions.isEmpty();
    }

    /**
     * Returns true if must show the following option
     */
    public boolean mustShow(DebugOption option)
    {
        return debugOptions.contains(option);
    }

    public void setDebugOptions(@NotNull EnumSet<DebugOption> options)
    {
        debugOptions.addAll(options);

        if (!options.isEmpty()) {
            setVerbose();
        }
    }

    public boolean isFailOnError()
    {
        return failOnError;
    }

    @Nullable protected String overrideProperty(@NotNull String id)
    {
        return overrideProperties.get(id);
    }

    protected void setVerbose()
    {
        logger.setLevel(Logger.Level.VERBOSE);
    }

    private static Set<File> loadExtensionsPath(Map<String, String> baseProperties)
    {
        String path = System.getenv("APB_EXT_PATH");

        String path2 = baseProperties.get("ext.path");

        if (path2 != null) {
            path = path == null ? path2 : path + File.pathSeparator + path2;
        }

        Set<File> jars = new LinkedHashSet<File>();

        if (path != null) {
            for (String p : path.split(File.pathSeparator)) {
                jars.addAll(FileUtils.listAllFilesWithExt(new File(p), ".jar"));
            }
        }

        return jars;
    }

    private void initOptions()
    {
        setDebugOptions(DebugOption.findAll(getProperty("debug", "")));
    }

    //~ Static fields/initializers ...........................................................................

    static final String GROUP_PROP_KEY = "group";
    static final String VERSION_PROP_KEY = "version";
    static final String PKG_PROP_KEY = "pkg";
    static final String PKG_DIR_KEY = PKG_PROP_KEY + ".dir";
}
