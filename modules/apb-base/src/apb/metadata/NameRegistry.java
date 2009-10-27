

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
 * Private for the package, an utility class that keeps a registry of Objects by class
 * to avoid combinatorial explosion of Dependency objects to be created
 */

class NameRegistry
{
    //~ Methods ..............................................................................................

    @SuppressWarnings("unchecked")
    static <T extends Named> T intern(T obj)
    {
        final String name = obj.getName();
        T            result = (T) registry.get(name);

        if (result == null || !result.getClass().equals(obj.getClass())) {
            registry.put(name, obj);
            result = obj;
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    @NotNull private static final Map<String, Named> registry = new HashMap<String, Named>();
}
