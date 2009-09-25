

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

import java.util.SortedMap;
import java.util.TreeMap;

import apb.utils.NameUtils;

import junit.framework.TestCase;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class NameTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testDirFromId()
    {
        String str = NameUtils.dirFromId("a-b.c.d");
        assertEquals("a-b/c/d", str);
    }

    public void testName()
    {
        String str = NameUtils.name(String.class);
        assertEquals("java.lang.String", str);

        String[] v = { "a", "b" };

        str = NameUtils.name(v.getClass());

        assertEquals("java.lang.String[]", str);
    }

    public void testIdFromClass()
    {
        String str = NameUtils.idFromClass(String.class);
        assertEquals("java.lang.string", str);

        SortedMap<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");

        str = NameUtils.idFromClass(m.entrySet().toArray()[0].getClass());

        assertEquals("java.util.tree-map.entry", str);

        str = NameUtils.idFromClass(StringBuilder.class);
        assertEquals("java.lang.string-builder", str);

        str = NameUtils.idFromClass(AClassWith$And_Inside.class);

        assertEquals("apb.tests.utils.name-test.aclass-with.and-inside", str);
    }

    public void testIdFromMember()
        throws NoSuchMethodException
    {
        String str = NameUtils.idFromMember(String.class.getFields()[0]);
        assertEquals("case-insensitive-order", str);
        str = NameUtils.idFromMember(String.class.getMethod("charAt", Integer.TYPE));
        assertEquals("char-at", str);
    }

    //~ Inner Classes ........................................................................................

    static class AClassWith$And_Inside {}
}
