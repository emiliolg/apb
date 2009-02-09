
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

package apb.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
//
// User: emilio
// Date: Nov 6, 2008
// Time: 11:59:40 AM

//
public class IsolatedClassLoader
    extends URLClassLoader
{
    //~ Instance fields ......................................................................................

    private ClassLoader parent = ClassLoader.getSystemClassLoader();

    //~ Constructors .........................................................................................

    public IsolatedClassLoader(Iterable<File> urls)
        throws MalformedURLException
    {
        super(FileUtils.toURLArray(urls));
    }

    //    public synchronized Class loadClass(String name)
    //        throws ClassNotFoundException
    //    {
    //        Class c = findLoadedClass(name);
    //
    //        ClassNotFoundException ex = null;
    //
    //        if (c == null) {
    //            try {
    //                c = findClass(name);
    //            }
    //            catch (ClassNotFoundException e) {
    //                ex = e;
    //
    //                if (parent != null) {
    //                    c = parent.loadClass(name);
    //                }
    //            }
    //        }
    //
    //        if (c == null) {
    //            throw ex;
    //        }
    //
    //        return c;
    //    }

}
