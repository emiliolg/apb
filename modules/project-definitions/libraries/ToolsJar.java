
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

package libraries;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jetbrains.annotations.Nullable;

import apb.metadata.Library;
import apb.metadata.LocalLibrary;

public class ToolsJar
    extends LocalLibrary
{
    //~ Constructors .........................................................................................

    private ToolsJar()
    {
        super(getToolsFile().getPath());
    }

    //~ Methods ..............................................................................................

    private static boolean isNecessary()
    {
        final ClassLoader extLoader = ClassLoader.getSystemClassLoader().getParent();

        try {
            Class.forName("com.sun.javadoc.Doc", false, extLoader);
        }
        catch (ClassNotFoundException ignore) {
            return true;
        }

        return false;
    }

    private static File getToolsFile()
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Class<?>     clazz = compiler.getClass();
        Class<?>     topClazz = clazz.getDeclaringClass();

        if (topClazz != null) {
            clazz = topClazz;
        }

        String classResource = clazz.getSimpleName() + ".class";
        URL    url = clazz.getResource(classResource);

        assert url != null;
        assert "jar".equals(url.getProtocol());

        String path = url.getPath();
        int    p = path.indexOf('!');

        assert p != -1;

        path = path.substring(0, p);
        URI uri = URI.create(path);
        return new File(uri);
    }

    //~ Static fields/initializers ...........................................................................

    @Nullable public static final Library LIB = isNecessary() ? new ToolsJar() : null;
}
