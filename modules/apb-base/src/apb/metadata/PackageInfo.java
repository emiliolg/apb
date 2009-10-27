

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
import java.util.Arrays;
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
     * Whether to generate a jar with the sources or not.
     */
    @BuildProperty public boolean generateSourcesJar;

    /**
     * Indicates whether dependencies must be included in package.
     */
    @BuildProperty public IncludeDependencies includeDependencies = IncludeDependencies.NONE;

    /**
     * The packaging type for the module
     */
    public PackageType type = PackageType.JAR;

    /**
     * The directory for the package.
     */
    @BuildProperty public String dir = "lib";

    /**
     * The main class of the package
     */
    @BuildProperty public String mainClass = "";

    /**
     * The name of the package file without the extension
     */
    @BuildProperty public String name = "${group}-${moduleid}-${version}";

    /**
     * Additional dependencies added into the package.
     * Libraries are not included in module package. Specified library dependencies will be ignored.
     */
    private final DependencyList additionalDependencies = new DependencyList();

    /**
     * Excluded patterns specifying files that must not be included in package.
     */
    private List<String> excludes = new ArrayList<String>();

    /**
     * Extra entries to add to the manifest Class-Path
     */
    private final List<String> extraClassPathEntries = new ArrayList<String>();

    /**
     * Additional attribute for the package manifest
     */
    private final Map<Attributes.Name, String> attributes = new HashMap<Attributes.Name, String>();

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

    public List<String> excludes()
    {
        return excludes;
    }

    /**
     * Method used to set excludes to define the list of files to be excluded
     * @param patterns The list of patterns to be excluded
     */
    public void excludes(String... patterns)
    {
        excludes.addAll(Arrays.asList(patterns));
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
}
