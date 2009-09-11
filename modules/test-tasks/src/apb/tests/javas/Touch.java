

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


package apb.tests.javas;

import java.io.File;
import java.io.IOException;

import apb.utils.FileUtils;

/**
 * A simple main to test the java task
 */
//
// User: emilio
// Date: Sep 7, 2009
// Time: 3:13:04 PM

//
public class Touch
{
    //~ Methods ..............................................................................................

    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.exit(1);
        }

        final File f = new File(args[0]);
        final long ts = Long.parseLong(System.getProperty("timestamp"));

        try {
            FileUtils.touch(f, ts);
        }
        catch (IOException e) {
            System.exit(1);
        }
    }
}
