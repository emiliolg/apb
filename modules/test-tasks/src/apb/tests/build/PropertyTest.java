

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


package apb.tests.build;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import apb.Apb;
import apb.DefinitionException;

import apb.tests.testutils.TestLogger;

import static java.util.Collections.singleton;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class PropertyTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testProp()
        throws DefinitionException
    {
        Map<String, String> ps = new LinkedHashMap<String, String>();
        ps.put("prop1", ":ID_VALUE:");

        init(ps);

        build("Properties", "info");
        checkOutput("str=str_value",  //
                    "estr=str:ID_VALUE: :ID_VALUE:value",  //
                    "bool=true",  //
                    "chr=a",  //
                    "b=1",  //
                    "s=1",  //
                    "i=1",  //
                    "l=1",  //
                    "f=1.0",  //
                    "d=1.0",  //
                    "e=ALFA",  //
                    "o-bool=true",  //
                    "oBool=true",  //
                    "o_bool=true",  //
                    "o-chr=a",  //
                    "o-b=1",  //
                    "o-s=1",  //
                    "o-i=1",  //
                    "o-l=1",  //
                    "o-f=1.0",  //
                    "o-d=1.0",  //
                    "l-str=str1,str2",  //
                    "l-estr=str1:ID_VALUE:,str2:ID_VALUE:value",  //
                    "l-bool=true,false",  //
                    "l-chr=a,b",  //
                    "l-b=10,20",  //
                    "l-s=10,20",  //
                    "l-i=10,20",  //
                    "l-l=10,20",  //
                    "l-f=10.0,20.0",  //
                    "l-d=10.0,20.0",  //
                    "l-e=BETA,GAMMA",  //
                    "inner.str=str_inner",  //
                    "inner.bool=false",  //
                    "inner.chr=b",  //
                    "inner.b=2",  //
                    "inner.s=2",  //
                    "inner.i=2",  //
                    "inner.l=2",  //
                    "inner.f=2.0",  //
                    "inner.d=2.0",  //
                    "inner.e=BETA"  //
                   );
    }

    public void testField()
        throws DefinitionException
    {
        Map<String, String> ps = new LinkedHashMap<String, String>();
        ps.put("prop1", ":ID_VALUE:");

        init(ps);

        build("Properties", "field");
        checkOutput("str=str_value",  //
                    "estr=str:ID_VALUE: :ID_VALUE:value",  //
                    "bool=true",  //
                    "chr=a",  //
                    "b=1",  //
                    "s=1",  //
                    "i=1",  //
                    "l=1",  //
                    "f=1.0",  //
                    "d=1.0",  //
                    "e=ALFA",  //
                    "o-bool=true",  //
                    "o-chr=a",  //
                    "o-b=1",  //
                    "o-s=1",  //
                    "o-i=1",  //
                    "o-l=1",  //
                    "o-f=1.0",  //
                    "o-d=1.0",  //
                    "l-str=[str1, str2]",  //
                    "l-estr=[str1:ID_VALUE:, str2:ID_VALUE:value]",  //
                    "l-bool=[true, false]",  //
                    "l-chr=[a, b]",  //
                    "l-b=[10, 20]",  //
                    "l-s=[10, 20]",  //
                    "l-i=[10, 20]",  //
                    "l-l=[10, 20]",  //
                    "l-f=[10.0, 20.0]",  //
                    "l-d=[10.0, 20.0]",  //
                    "l-e=[BETA, GAMMA]",  //
                    "inner.str=str_inner",  //
                    "inner.bool=false",  //
                    "inner.chr=b",  //
                    "inner.b=2",  //
                    "inner.s=2",  //
                    "inner.i=2",  //
                    "inner.l=2",  //
                    "inner.f=2.0",  //
                    "inner.d=2.0",  //
                    "inner.e=BETA"  //
                   );
    }

    public void testOverride()
        throws DefinitionException
    {
        Map<String, String> ps = new LinkedHashMap<String, String>();
        ps.put("prop1", ":ID_VALUE:");

        ps.put("str", "override_str");
        ps.put("estr", "str:ID_VALUE: :ID_VALUE:val");
        ps.put("bool", "false");
        ps.put("chr", "z");
        ps.put("b", "100");
        ps.put("s", "1000");
        ps.put("i", "1000");
        ps.put("l", "1000");
        ps.put("f", "1000.0");
        ps.put("d", "1000.0");
        ps.put("e", "BETA");
        ps.put("o-bool", "false");
        ps.put("oBool", "false");
        ps.put("o_bool", "false");
        ps.put("o-chr", "x");
        ps.put("o-b", "200");
        ps.put("o-s", "2000");
        ps.put("o-i", "2000");
        ps.put("o-l", "2000");
        ps.put("o-f", "2000.0");
        ps.put("o-d", "2000.0");
        ps.put("l-str", "str100,str200,str300");
        ps.put("l-estr", "a,b");
        ps.put("l-bool", "true,false,true");
        ps.put("l-chr", "a,b,c,d");
        ps.put("l-b", "10,20,30,40,50");
        ps.put("l-s", "10,20,30,40,50");
        ps.put("l-i", "10,20,30,40,50");
        ps.put("l-l", "10,20,30,40,50");
        ps.put("l-f", "10.0,20.0,30.0,40.0,50.0");
        ps.put("l-d", "10.1,20.1,30,1,40.1,50,1");
        ps.put("l-e", "ALFA, GAMMA");  //
        ps.put("inner.str", "override_inner");
        ps.put("inner.bool", "true");
        ps.put("inner.chr", "l");
        ps.put("inner.b", "21");
        ps.put("inner.s", "22");
        ps.put("inner.i", "23");
        ps.put("inner.l", "24");
        ps.put("inner.f", "25.0");
        ps.put("inner.d", "26.0");
        ps.put("inner.e", "GAMMA");

        init(ps);
        ps.remove("prop1");

        build("Properties", "info");

        List<String> expected = new ArrayList<String>();

        for (Map.Entry<String, String> entry : ps.entrySet()) {
            expected.add(entry.getKey() + "=" + entry.getValue());
        }

        checkOutput(expected.toArray(new String[expected.size()]));
    }

    protected void setUp()
        throws Exception {}

    private void init(Map<String, String> props)
    {
        output = new ArrayList<String>();
        env = Apb.createBaseEnvironment(new TestLogger(output), props);
        dataDir = new File(env.expand("$datadir"));
        projectPath = singleton(new File(dataDir, "projects/DEFS"));
    }
}
