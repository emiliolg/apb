

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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * A decorator for a dependency
 * It is used to mark a dependency as Runtime or Compile-time only
 */

//
// User: emilio
// Date: Apr 29, 2009
// Time: 6:27:47 PM

public class DecoratedDependency
    implements Dependency
{
    //~ Instance fields ......................................................................................

    /**
     * Wheter the dependency is only for compilation or runtime
     */
    private final boolean compileTime;

    /**
     * The original Dependency
     */
    @NotNull private final Dependency dependency;

    //~ Constructors .........................................................................................

    private DecoratedDependency(Dependency dep, boolean compile)
    {
        dependency = dep;
        compileTime = compile;
    }

    //~ Methods ..............................................................................................

    public static DepOrDepList asCompileOnly(Dependency... dep)
    {
        return decorate(true, dep);
    }

    private static DepOrDepList decorate(boolean compile, Dependency... dep) {
        if (dep.length == 1) {
            return decorate(dep[0], compile);
        }
        else {
            DependencyList result = new DependencyList();
            for (Dependency d : dep) {
                result.add(decorate(d, compile));
            }
            return result;
        }
    }

    public static DepOrDepList asRuntimeOnly(Dependency... dep)
    {
        return decorate(false, dep);
    }

    @NotNull public String getName()
    {
        return decoratedName(dependency, compileTime);
    }

    @NotNull public Module asModule()
    {
        return dependency.asModule();
    }

    public boolean isModule()
    {
        return dependency.isModule();
    }

    public boolean isLibrary()
    {
        return dependency.isLibrary();
    }

    @NotNull public Library asLibrary()
    {
        return dependency.asLibrary();
    }

    public boolean mustInclude(boolean compile)
    {
        return compile == compileTime;
    }

    @Override public String toString()
    {
        return getName();
    }

    private static Dependency decorate(Dependency dep, boolean compile)
    {
        String     key = decoratedName(dep, compile);
        Dependency result = decoratedDependencies.get(key);

        if (result == null) {
            result = new DecoratedDependency(dep, compile);
            decoratedDependencies.put(key, result);
        }

        return result;
    }

    private static String decoratedName(Dependency dep, boolean compile)
    {
        return dep.getName() + (compile ? ":C" : ":R");
    }

    //~ Static fields/initializers ...........................................................................

    @NotNull private static final Map<String, Dependency> decoratedDependencies =
        new HashMap<String, Dependency>();
}
