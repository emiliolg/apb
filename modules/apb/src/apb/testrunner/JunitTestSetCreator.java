

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

import java.io.File;
import java.lang.reflect.Method;

import apb.utils.ClassUtils;

import junit.framework.Test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JunitTestSetCreator
    implements TestSetCreator<Object>
{
    //~ Methods ..............................................................................................

    @Nullable public TestSet<Object> createTestSet(@NotNull Class<Object> testClass,
                                                   @NotNull String        singleTest)
        throws TestSetFailedException
    {
        TestSet<Object> result;

        if (isJUnit4Test(testClass)) {
            result = JUnit4TestSet.buildTestSet(testClass, singleTest);
        }
        else {
            result = JUnit3TestSet.buildTestSet(testClass, singleTest);
        }

        return result;
    }

    @NotNull public String getName()
    {
        return "junit";
    }

    @NotNull public File getTestFrameworkJar()
    {
        final File file = ClassUtils.jarFromClass(Test.class);

        if (file == null) {
            throw new RuntimeException("Cannot find junit jar");
        }

        return file;
    }

    static boolean isJUnit4Test(@NotNull Class<?> clazz)
    {
        final Method[] methods = clazz.getDeclaredMethods();

        boolean result = clazz.isAnnotationPresent(org.junit.runner.RunWith.class);

        if (!result) {
            for (final Method method : methods) {
                if (method.isAnnotationPresent(org.junit.Test.class)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
