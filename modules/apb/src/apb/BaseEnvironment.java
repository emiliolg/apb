

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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import apb.utils.CollectionUtils;
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

public abstract class BaseEnvironment
    extends DefaultEnvironment
{
    //~ Instance fields ......................................................................................

    /**
     * The directory where the project definition files are stored
     */
    @Nullable File projectsHome;

    /**
     * Override properties are argument properties
     * They take preference over the properties defined in project elements
     */
    @NotNull private final Map<String, String> overrideProperties;

    /**
     * The set of jars that comprise the extension class path
     */
    @NotNull private final Set<File> extClassPath;

    //~ Constructors .........................................................................................

    /**
     * Crate an Environment
     * @param override
     */
    protected BaseEnvironment(Map<String, String> override)
    {
        // Read Environment
        //        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
        //            baseProperties.put(entry.getKey(), entry.getValue());
        //        }
        CollectionUtils.copyProperties(properties, FileUtils.userProperties());

        // Read System Properties
        overrideProperties = new TreeMap<String,String>();
        CollectionUtils.copyProperties(overrideProperties, System.getProperties());
        overrideProperties.putAll(override);

        extClassPath = loadExtensionsPath(properties);
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


    @Nullable
    protected String overrideProperty(@NotNull String id)
    {
        return overrideProperties.get(id);
    }

    /**
     * Do all initializations that needs properties already initialized
     */
    protected void postInit()
    {
        initProxies();
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

    private void initProxies()
    {
        Proxy proxy = Proxy.getDefaultProxy(this);

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

    static final String GROUP_PROP_KEY = "group";
    static final String VERSION_PROP_KEY = "version";
    static final String PKG_PROP_KEY = "pkg";
    static final String PKG_DIR_KEY = PKG_PROP_KEY + ".dir";
}