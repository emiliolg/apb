

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


package apb.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

public class PackageInfo
{
    //~ Instance fields ......................................................................................

    /**
     * Whether to generate a Class-Path manifest entry.
     */
    @BuildProperty public boolean addClassPath;

    /**
     * The directory for the package.
     */
    @BuildProperty public String dir = "lib";

    /**
     * Whether to generate a jar with the sources or not.
     */
    @BuildProperty public boolean generateSourcesJar;

    /**
     * The main class of the package
     */
    @BuildProperty public String mainClass = "";

    /**
     * The name of the package file without the extension
     */
    @BuildProperty public String name = "${group}-${moduleid}-${version}";

    /**
     * The packaging type for the module
     */
    public PackageType type = PackageType.JAR;

    /**
     * Additional attribute for the package manifest
     */
    private final Map<Attributes.Name, String> attributes = new HashMap<Attributes.Name, String>();

    /**
     * Extra entries to add to the manifest Class-Path
     */
    private final List<String> extraClassPathEntries = new ArrayList<String>();

    /**
     * Additional dependencies added into the package.
     * Libraries are not included in module package. Specified library dependencies will be ignored.
     */
    private final DependencyList additionalDependencies = new DependencyList();

    /**
     * Indicates whether dependencies must be included in package.
     */
    private IncludeDependenciesMode includeDependenciesMode = IncludeDependenciesMode.NONE;


    /**
     * Excluded patterns specifying files that must not be included in package.
     */
    private List<String> excludes = new ArrayList<String>();

    /**
     * Services defined in the package
     * @see java.util.ServiceLoader
     */
    private final Map<String, Set<String>> services = new HashMap<String, Set<String>>();

    //~ Methods ..............................................................................................

    /**
     * The package file with extension
     * @return The package file with extension
     */
    public String getName()
    {
        return name + type.getExt();
    }

    public DependencyList additionalDependencies()
    {
        return additionalDependencies;
    }


    public IncludeDependenciesMode getIncludeDependenciesMode() {
        return includeDependenciesMode;
    }

    public void setIncludeDependenciesMode(IncludeDependenciesMode includeDependenciesMode) {
        this.includeDependenciesMode = includeDependenciesMode;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public List<String> extraClassPathEntries()
    {
        return extraClassPathEntries;
    }

    public Map<String, Set<String>> services()
    {
        return services;
    }

    public Map<Attributes.Name, String> attributes()
    {
        return attributes;
    }

    /**
     * Add implementations (providers) of a given service
     * @see java.util.ServiceLoader
     * @param service the service to add providers to
     * @param providers The list of providers to add
     */
    public void services(@NotNull String service, @NotNull String... providers)
    {
        Set<String> ps = services.get(service);

        if (ps == null) {
            ps = new HashSet<String>();
        }

        ps.addAll(asList(providers));
        services.put(service, ps);
    }

    /**
     * Add entries to the manifest Class-Path
     * @param entries The entries to add
     */
    public void extraClassPathEntries(@NotNull String... entries)
    {
        extraClassPathEntries.addAll(asList(entries));
    }

    /**
     * Add an attribute to the manifest
     * @param attName attribute name
     * @param attValue attribute value
     */
    public void addAttribute(@NotNull Attributes.Name attName, @NotNull String attValue)
    {
        attributes.put(attName, attValue);
    }

    /**
     * Add an attribute to the manifest
     * @param attName attribute name
     * @param attValue attribute value
     */
    public void addAttribute(@NotNull String attName, @NotNull String attValue)
    {
        attributes.put(new Attributes.Name(attName), attValue);
    }

    public enum IncludeDependenciesMode {
        /**
         * No module dependencies are included in jar.
         */
        NONE,

        /**
         * Only direct module dependencies are included in jar.
         */
        DIRECT_MODULES,

        /**
         * Recursive module dependencies are included in jar.
         */
        DEEP_MODULES
    }

    /**
     * Method used to set dependencies to be added to the package
     * @param dependencyList The list of dependencies to be added to the package
    public final void includeDependencies(Dependency... dependencyList)
    {
        includeDependencies.addAll(asList(dependencyList));
    }
    */
}
