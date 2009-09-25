

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

import apb.metadata.UpdatePolicy;

import apb.tests.testutils.FileAssert;

import static apb.tasks.CoreTasks.download;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class DownloadTest
    extends TaskTestCase
{
    //~ Methods ..............................................................................................

    public void testLocal()
        throws IOException
    {
        final File from = dataFile(SOURCE);
        final File to = env.fileFromBase(SOURCE);

        download(from.toURI().toString()).to(to)  //
                                         .execute();

        FileAssert.assertFileEquals(from, to);

        long ts = to.lastModified();

        download(from.toURI().toString()).to(to)  //
                                         .execute();

        assertEquals(ts, to.lastModified());

        download(from.toURI().toURL()).to(to.getPath())  //
                                      .withUpdatePolicy(UpdatePolicy.ALWAYS)  //
                                      .withUser("")  //
                                      .withPassword("")  //
                                      .execute();

        assertTrue(ts <= to.lastModified());
    }

    public void testRemote()
        throws IOException
    {
        //        final File to = env.fileFromBase("commons-util-final.jar");
        //
        //        download("http://www.ibiblio.org/maven/commons-util/jars/commons-util-final.jar").to(to)  //
        //                                         .execute();
        //
        //        FileAssert.assertFileEquals(dataFile("commons-util-final.jar"), to);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String SOURCE = "simple.iml";
}
