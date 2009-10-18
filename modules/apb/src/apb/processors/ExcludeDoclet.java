

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


package apb.processors;  // Copyright 2008-2009 Emilio Lopez-Gabeiras

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

// User: emilio
// Date: Oct 10, 2009
// Time: 8:39:08 PM

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;

public class ExcludeDoclet
{
    //~ Methods ..............................................................................................

    public static void main(String[] args)
    {
        String name = ExcludeDoclet.class.getName();
        Main.execute(name, name, args);
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter)
        throws java.io.IOException
    {
        return Standard.validOptions(options, reporter);
    }

    public static int optionLength(String option)
    {
        return Standard.optionLength(option);
    }

    public static boolean start(RootDoc root)
        throws java.io.IOException
    {
        return Standard.start((RootDoc) process(root, RootDoc.class));
    }

    private static boolean exclude(Doc doc)
    {
        if (doc instanceof ProgramElementDoc) {
            if (((ProgramElementDoc) doc).containingPackage().tags("exclude").length > 0) {
                return true;
            }
        }

        return doc.tags("exclude").length > 0;
    }

    private static Object process(Object obj, Class expect)
    {
        if (obj == null) {
            return null;
        }

        Class cls = obj.getClass();

        if (cls.getName().startsWith("com.sun.")) {
            return Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new ExcludeHandler(obj));
        }
        else if (obj instanceof Object[] && expect.isArray()) {
            Class        componentType = expect.getComponentType();
            Object[]     array = (Object[]) obj;
            List<Object> list = new ArrayList<Object>(array.length);

            for (Object entry : array) {
                if (!(entry instanceof Doc) || !exclude((Doc) entry)) {
                    list.add(process(entry, componentType));
                }
            }

            return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
        }
        else {
            return obj;
        }
    }

    //~ Inner Classes ........................................................................................

    private static class ExcludeHandler
        implements InvocationHandler
    {
        private Object target;

        public ExcludeHandler(Object target)
        {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            if (args != null) {
                String methodName = method.getName();

                if ("compareTo".equals(methodName) || "equals".equals(methodName) ||
                        "overrides".equals(methodName) || "subclassOf".equals(methodName)) {
                    args[0] = unwrap(args[0]);
                }
            }

            try {
                return process(method.invoke(target, args), method.getReturnType());
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private Object unwrap(Object proxy)
        {
            if (proxy instanceof Proxy) {
                return ((ExcludeHandler) Proxy.getInvocationHandler(proxy)).target;
            }

            return proxy;
        }
    }
}
