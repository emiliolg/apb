

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


package apb.installer;

import java.io.File;
import java.net.URL;
//
// User: emilio
// Date: Feb 12, 2009
// Time: 6:14:15 PM

//
public class Main
{
    //~ Methods ..............................................................................................

    public static void main(String[] args)
    {
        File      jarFile = guessJarFile();
        Installer installer = new Installer(jarFile, args);

        if (args.length == 0) {
            installer.usage();
        }
        else {
            try {
                if (installer.parseArgs()) {
                    installer.run();
                }
                else {
                    installer.usage();
                }
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static File guessJarFile()
    {
        try {
            URL url = Main.class.getClassLoader().getResource("apb/");

            if ("jar".equals(url.getProtocol())) {
                String fileName = url.getFile();
                int    index = fileName.lastIndexOf('!');

                if (index != -1) {
                    URL file = new URL(fileName.substring(0, index));
                    return new File(file.getFile());
                }
            }
        }
        catch (Exception e) {
            // Fallthrough
        }

        System.err.println("Cannot guess jar file name. Aborting");
        System.exit(1);

        // NOT REACHABLE
        throw new RuntimeException();
    }
}