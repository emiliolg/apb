

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

import apb.DefinitionException;

import java.util.Map;
import java.io.File;

public class ShowDepsTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testHtmlSimple()
        throws DefinitionException
    {
        build("HelloWorld", "showdeps:html");
        //TODO: check output
        checkOutput(env.expand("Generating: $showdeps.dir/hello-world.html"));
        assertExists("$showdeps.dir/hello-world.html");
    }

    public void testHtmlProject()
        throws DefinitionException
    {
        build("Samples", "showdeps:html");
        checkOutput(env.expand("Generating: $showdeps.dir/samples.html"));
        assertExists("$showdeps.dir/samples.html");
    }

    public void testGraphVizSimple()
        throws DefinitionException
    {
        build("HelloWorld", "showdeps:graphviz");
        //TODO: check output
        checkOutput(
                env.expand("Generating: $showdeps.dir/hello-world.gv"),
                env.expand("Generating: $showdeps.dir/hello-world.tred"),
                env.expand("Generating: $showdeps.dir/hello-world.pdf")
        );
        assertExists("$showdeps.dir/hello-world.gv");
        assertExists("$showdeps.dir/hello-world.tred");
        assertExists("$showdeps.dir/hello-world.pdf");
    }

    private void assertExists(String f) {
        assertTrue(new File(env.expand(f)).exists());
    }

    public void testGraphVizProject()
        throws DefinitionException
    {
        build("Samples", "showdeps:graphviz");
        checkOutput(
                env.expand("Generating: $showdeps.dir/samples.gv"),
                env.expand("Generating: $showdeps.dir/samples.tred"),
                env.expand("Generating: $showdeps.dir/samples.pdf")
        );
        assertExists("$showdeps.dir/samples.gv");
        assertExists("$showdeps.dir/samples.tred");
        assertExists("$showdeps.dir/samples.pdf");
    }

    @Override protected void createEnv(Map<String, String> properties)
    {
        properties.put("showdeps.dir", new File(tmpdir, "showdeps").getAbsolutePath());
        properties.put("graphviz.dot", "cat");
        properties.put("graphviz.tred", "cat");
        super.createEnv(properties);
    }


}