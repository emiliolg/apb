

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import apb.utils.CollectionUtils;
import apb.utils.IdentitySet;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class IdentitySetTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void test1()
        throws IOException
    {
        Set<String> set = new IdentitySet<String>();
        assertTrue(set.add("a"));
        assertFalse(set.add("a"));
        assertTrue(set.contains("a"));
        final String a = new String("a");

        assertFalse(set.contains(a));
        assertTrue(set.add(a));

        assertEquals(2, set.size());

        List<String> list = new ArrayList<String>();
        CollectionUtils.addAll(list, set);
        assertEquals(asList("a", "a"), list);

        set.clear();

        assertEquals(0, set.size());
    }
}
