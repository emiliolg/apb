

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


package apb.tests.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import apb.Constants;
import apb.Messages;

import apb.sunapi.Base64;

import apb.tasks.CoreTasks;

import apb.tests.testutils.FileAssert;

import apb.utils.*;

import junit.framework.TestCase;

//

/**
 * Just to mark coverage of the constructor of Util classes....
 * Plus Messages
 */
public class DummyTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testConstructors()
    {
        new Base64();
        new FileAssert();
        new ClassUtils();
        new CollectionUtils();
        new NameUtils();
        new StringUtils();
        new ColorUtils();
        new CoreTasks();
        new Constants();
        new Messages();
    }

    public void testMessages()
    {
        Method[] msgs = Messages.class.getMethods();

        for (Method method : msgs) {
            if (Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(String.class)) {
                try {
                    String msg = (String) method.invoke(null, buildArgs(method));
                    assertNotNull(msg);
                }
                catch (Exception e) {
                    assertTrue("Exception invoking: " + method.getName(), false);
                }
            }
        }
    }

    private Object[] buildArgs(Method method)
    {
        final Class<?>[] argTypes = method.getParameterTypes();
        Object[]         result = new Object[argTypes.length];

        for (int i = 0; i < result.length; i++) {
            assertEquals("On method " + method.getName() + " argument " + i, String.class, argTypes[i]);
            result[i] = "";
        }

        return result;
    }
}
