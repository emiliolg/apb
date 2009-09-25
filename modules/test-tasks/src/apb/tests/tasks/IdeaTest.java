

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

import apb.metadata.PackageType;

import apb.tests.testutils.FileAssert;

import static java.util.Arrays.asList;
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
        final long ts = currentTime();

        final List<File> sources = asList(env.fileFromBase("$module-src"), env.fileFromBase("empty"));

        genIml(sources);
        genIml(sources);

        generateModule(SIMPLE).on(basedir).usingSources(sources).ifOlder(ts).execute();
    }

    public void testSimpleIpr()
        throws IOException
    {
        final long ts = currentTime();

        genIpr();

        genIpr();

        generateProject(SIMPLE, basedir).on(basedir)  //
                                        .usingModules(singletonList(SIMPLE))  //
                                        .ifOlder(ts)  //
                                        .useJdk("1.6")  //
                                        .execute();
    }

    public void testTemplates()
        throws IOException
    {
        final List<File> sources = singletonList(env.fileFromBase("$module-src"));

        generateModule(MOD1).on(basedir)  //
                            .usingTemplate("$datadir/template.iml").usingSources(sources).execute();

        verify(MOD1 + IML);
        generateModule(MOD2).on(basedir)  //
                            .usingModules(MOD1)  //
                            .usingSources(sources)  //
                            .execute();
        verify(MOD2 + IML);

        generateProject(PROJ1, basedir).on(basedir)  //
                                       .usingTemplate("$datadir/template.ipr")
                                       .usingModules(asList(MOD1, MOD2))  //
                                       .useJdk("1.5")  //
                                       .execute();
        verify(PROJ1 + IPR);
    }

    private void genIml(List<File> sources)
    {
        generateModule(SIMPLE).on(basedir)  //
                              .usingSources(sources).usingOutput(env.fileFromBase("output"))  //
                              .withPackageType(PackageType.JAR)  //
                              .execute();
        verify(SIMPLE + IML);
    }

    private void genIpr()
    {
        generateProject(SIMPLE, basedir).on(basedir)  //
                                        .usingModules(singletonList(SIMPLE))  //
                                        .useJdk("1.6")  //
                                        .execute();
        verify(SIMPLE + IPR);
    }

    private void verify(String file)
    {
        final File iml = new File(basedir, file);
        FileAssert.assertFileEquals(iml, dataFile(file));
    }

    //~ Static fields/initializers ...........................................................................

    private static final String SIMPLE = "simple";
    private static final String IML = ".iml";
    private static final String IPR = ".ipr";
    private static final String MOD1 = "template.module1";
    private static final String MOD2 = "template.module2";
    private static final String PROJ1 = "template.project";
}
