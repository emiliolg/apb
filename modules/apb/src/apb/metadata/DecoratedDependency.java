

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
//
// User: emilio
// Date: Apr 29, 2009
// Time: 6:27:47 PM

public class DecoratedDependency
    implements Dependency
{
    //~ Instance fields ......................................................................................

    private Decoration decoration;
    private Dependency dependency;

    //~ Constructors .........................................................................................

    public DecoratedDependency(Dependency dep, Decoration dec)
    {
        dependency = dep;
        decoration = dec;
    }

    //~ Methods ..............................................................................................

    public static Dependency asCompileOnly(Dependency dep)
    {
        return decorate(dep, Decoration.COMPILE);
    }

    public static Dependency asRuntimeOnly(Dependency dep)
    {
        return decorate(dep, Decoration.COMPILE);
    }

    @NotNull public String getName()
    {
        return dependency.getName();
    }

    @NotNull public Module asModule()
    {
        return dependency.asModule();
    }

    public boolean isModule()
    {
        return dependency.isModule();
    }

    public boolean isCompileDependency()
    {
        return decoration == Decoration.COMPILE;
    }

    public boolean isRuntimeDependency()
    {
        return decoration == Decoration.RUNTIME;
    }

    public boolean isLibrary()
    {
        return dependency.isLibrary();
    }

    @NotNull public Library asLibrary()
    {
        return dependency.asLibrary();
    }

    private static Dependency decorate(Dependency dep, Decoration decoration)
    {
        String     key = decoration + ":" + dep.getName();
        Dependency result = decoratedDependencies.get(key);

        if (result == null) {
            result = new DecoratedDependency(dep, decoration);
            decoratedDependencies.put(key, result);
        }

        return dep;
    }

    //~ Static fields/initializers ...........................................................................

    @NotNull private static final Map<String, Dependency> decoratedDependencies =
        new HashMap<String, Dependency>();

    //~ Enums ................................................................................................

    private enum Decoration
    {
        COMPILE,
        RUNTIME
    }
}
