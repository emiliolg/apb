

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

import org.jetbrains.annotations.NotNull;

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
     * Package the following dependencies into the jar
     */
    private final List<Dependency> includeDependencies = new ArrayList<Dependency>();

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

    public List<Dependency> includeDependencies()
    {
        return includeDependencies;
    }

    public Map<String, Set<String>> services()
    {
        return services;
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

        ps.addAll(Arrays.asList(providers));
        services.put(service, ps);
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
