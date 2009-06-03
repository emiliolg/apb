

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


package apb.testrunner;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 10, 2008
// Time: 3:00:44 PM

//
public interface TestSetCreator<T>
{
    //~ Methods ..............................................................................................

    @Nullable TestSet<T> createTestSet(@NotNull Class<T> testClass)
        throws TestSetFailedException;

    @NotNull Class<T> getTestClass();

    @NotNull String getName();

    //~ Inner Classes ........................................................................................

    static class Factory
    {
        @NotNull private final Map<String, TestSetCreator> registry;

        @NotNull TestSetCreator<?> fromName(String name)
        {
            final TestSetCreator result = registry.get(name);

            if (result == null) {
                throw new IllegalStateException("Invalid test type: " + name);
            }

            return result;
        }

        @NotNull Set<String> names()
        {
            return registry.keySet();
        }

        void register(@NotNull TestSetCreator t)
        {
            registry.put(t.getName(), t);
        }

        {
            registry = new HashMap<String, TestSetCreator>();

            for (TestSetCreator c : ServiceLoader.load(TestSetCreator.class)) {
                registry.put(c.getName(), c);
            }
        }
    }
}
