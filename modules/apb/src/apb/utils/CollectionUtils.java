

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


package apb.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Some utility Collection Methods
 */
public class CollectionUtils
{
    //~ Methods ..............................................................................................

    /**
     * Ensures that this collection contains the specified element.
     * But only tries to add it if the element is not null
     * @param collection
     * @param element The elment to be added if not null
     * @param <T> The type of the element that must match the type of the collection
     */
    public static <T> void addIfNotNull(@NotNull Collection<T> collection, @Nullable T element)
    {
        if (element != null) {
            collection.add(element);
        }
    }

    /**
     * Creates a java.util.List with the specified (Optional) element
     * If the element is null it returns the empty List
     * @param element The element to create the List from
     * @param <T> The type of the element and the List
     * @return A singleton list if the element is not null an empty list otherwise.
     */
    public static <T> List<T> optionalSingleton(@Nullable T element)
    {
        if (element == null) {
            return Collections.emptyList();
        }
        else {
            return Collections.singletonList(element);
        }
    }

    public static void copyProperties(Map<String, String> m, Properties p)
    {
        for (String id : p.stringPropertyNames()) {
            m.put(id, p.getProperty(id));
        }
    }

    public static <T> void addAll(Collection<T> target, Iterable<? extends T> source)
    {
        for (T t : source) {
            target.add(t);
        }
    }
}
