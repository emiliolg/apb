

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import apb.Apb;
import apb.Environment;

import apb.utils.CollectionUtils;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class CollectionsTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testAddIfNotNull()
        throws IOException
    {
        List<String> list = new ArrayList<String>();

        for (String s : ABC_LIST_WITH_NULLS) {
            CollectionUtils.addIfNotNull(list, s);
        }

        assertEquals(ABC_LIST, list);
    }

    public void testOptionalSingleton()
        throws IOException
    {
        List<String> list = new ArrayList<String>();

        for (String s : ABC_LIST_WITH_NULLS) {
            list.addAll(CollectionUtils.optionalSingleton(s));
        }

        assertEquals(ABC_LIST, list);
    }

    public void testIterableAddAll()
        throws IOException
    {
        List<String> list = new ArrayList<String>();
        CollectionUtils.addAll(list, ABC_LIST);
        assertEquals(ABC_LIST, list);
    }

    public void testExpandAll()
        throws IOException
    {
        Environment env = Apb.createBaseEnvironment();
        env.putProperty("a", "A");
        env.putProperty("b", "B");
        List<String> list = CollectionUtils.expandAll(env, asList("$a", "$b", "$a=$b"));
        assertEquals(AB_LIST, list);
        list = CollectionUtils.expandAll(env, "$a", "$b", "$a=$b");
        assertEquals(AB_LIST, list);
    }

    public void testFilesFromBase()
        throws IOException
    {
        Environment env = Apb.createBaseEnvironment();

        File         basedir = new File("tmp");
        final String path = basedir.getAbsolutePath();
        env.putProperty("basedir", path);

        List<File> list = CollectionUtils.filesFromBase(env, "A.java", "B.java", "../../lib/");
        assertEquals(asList(new File(basedir, "A.java").getCanonicalFile(), new File(basedir, "B.java")
                            .getCanonicalFile(), new File(basedir, "../../lib").getCanonicalFile()), list);
    }

    public void testStringToList()
        throws IOException
    {
        String str = CollectionUtils.listToString(ABC_LIST);
        assertEquals("a,b,c", str);
        str = CollectionUtils.listToString(ABC_LIST, "::");
        assertEquals("a::b::c", str);

        List<String> l = CollectionUtils.stringToList("a,b,c");
        assertEquals(ABC_LIST, l);

        l = CollectionUtils.stringToList("");
        assertTrue(l.isEmpty());

        l = CollectionUtils.stringToList(null);
        assertTrue(l.isEmpty());

        l = CollectionUtils.stringToList("a::b::c", "::");
        assertEquals(ABC_LIST, l);

        l = CollectionUtils.stringToList("a,,b,c,");
        assertEquals(asList("a", "", "b", "c"), l);
    }

    //~ Static fields/initializers ...........................................................................

    private static final List<String> AB_LIST = asList("A", "B", "A=B");

    private static final List<String> ABC_LIST_WITH_NULLS = asList("a", "b", null, "c", null);

    private static final List<String> ABC_LIST = asList("a", "b", "c");
}
