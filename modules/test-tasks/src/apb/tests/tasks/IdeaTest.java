

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


package apb.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import static java.util.Arrays.asList;

import apb.tests.utils.FileAssert;

import static java.util.Collections.singletonList;

import static apb.idegen.IdegenTask.generateModule;
import static apb.idegen.IdegenTask.generateProject;

// User: emilio
// Date: Sep 12, 2009
// Time: 6:33:37 PM

public class IdeaTest
    extends TaskTestCase
{
    //~ Methods ..............................................................................................

    public void testSimpleIml()
        throws IOException
    {
        final long       ts = System.currentTimeMillis();
        final List<File> sources = asList(env.fileFromBase("$source"), env.fileFromBase("empty"));
        generateModule("simple").on(basedir).usingSources(sources).execute();
        final File iml = new File(basedir, SIMPLE_IML);
        FileAssert.assertFileEquals(iml, dataFile(SIMPLE_IML));

        generateModule("simple").on(basedir).usingSources(sources).ifOlder(ts).execute();
    }

    public void testSimpleIpr()
        throws IOException
    {
        final long         ts = System.currentTimeMillis();
        final List<String> modules = singletonList("simple");
        generateProject("simple", basedir).on(basedir).usingModules(modules).execute();
        final File ipr = new File(basedir, SIMPLE_IPR);
        FileAssert.assertExists(ipr);
        FileAssert.assertFileEquals(ipr, dataFile(SIMPLE_IPR));

        generateProject("simple", basedir).on(basedir)  //
                                          .usingModules(modules)  //
                                          .ifOlder(ts)  //
                                          .useJdk("1.6")  //
                                          .execute();
    }

    public void testTemplates()
        throws IOException
    {
        final long         ts = System.currentTimeMillis();
        final List<File> sources = singletonList(env.fileFromBase("$source"));

        generateModule("template.module1").on(basedir).usingSources(sources).execute();
        //final File iml = new File(basedir, "template.module1.iml");
        generateModule("template.module2").on(basedir).usingSources(sources).execute();
        //final File iml = new File(basedir, "template.module2.iml");

        final List<String> modules = singletonList("simple");
        generateProject("template.project", basedir).on(basedir)      //
                .usingModules(asList("template.module1")) //
                .execute();

    }

    //~ Static fields/initializers ...........................................................................

    private static final String SIMPLE_IML = "simple.iml";
    private static final String SIMPLE_IPR = "simple.ipr";
}
