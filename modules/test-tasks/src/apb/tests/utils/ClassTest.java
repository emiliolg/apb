

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

import java.io.File;
import java.util.Date;

import apb.Apb;

import apb.metadata.Dependency;

import apb.utils.ClassUtils;
import apb.utils.FileUtils;

import junit.framework.TestCase;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class ClassTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testNewInstance()
        throws Exception
    {
        Object o = ClassUtils.newInstance("java.lang.String", "hello");
        assertEquals("hello", o);

        o = ClassUtils.newInstance("java.lang.String", (Object[]) null);
        assertEquals("", o);
    }

    @SuppressWarnings("deprecation")
    public void testMethodInvoke()
        throws Exception
    {
        Object o = ClassUtils.invoke("hello world", "substring", 1, 5);
        assertEquals("ello", o);

        o = ClassUtils.invokeStatic(String.class, "valueOf", 10);
        assertEquals("10", o);

        Date d = new Date("1/1/1990");
        o = ClassUtils.invokeNonPublic(d, "normalize");
        o = ClassUtils.invoke(o, "getYear");
        assertEquals("1990", o.toString());
    }

    public void testField()
        throws Exception
    {
        Object o = ClassUtils.fieldValue("hello world", "count");
        assertEquals(11, o);
    }

    public void testFail()
        throws Exception
    {
        String msg = "";

        try {
            ClassUtils.newInstance("java.lang.String", 10, "xx");
        }
        catch (NoSuchMethodException m) {
            msg = m.getMessage();
        }

        assertEquals("new java.lang.String(java.lang.Integer, java.lang.String)", msg);
        msg = "";

        try {
            ClassUtils.invoke("aString", "substring", "xx", null);
        }
        catch (NoSuchMethodException m) {
            msg = m.getMessage();
        }

        assertEquals("java.lang.String.substring(java.lang.String, null)", msg);
    }

    public void testJar()
        throws Exception
    {
        File libDir = FileUtils.normalizeFile(new File(Apb.getEnv().getProperty("apb-jar")).getParentFile());

        File f = ClassUtils.jarFromClass(Dependency.class);
        assertEquals(new File(libDir, "apb.jar"), f);

        final Dependency[] array = new Dependency[0];
        f = ClassUtils.jarFromClass(array.getClass());
        assertEquals(new File(libDir, "apb.jar"), f);
    }

    public void testToolsJar()
        throws Exception
    {
        File toolsJar = ClassUtils.toolsJar();
        assertTrue(toolsJar == null || toolsJar.getPath().endsWith("tools.jar"));
    }
}
