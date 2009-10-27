

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


package apb.processors;
//
// User: emilio
// Date: Oct 27, 2009
// Time: 4:01:57 PM

class ClassWriter
    extends org.objectweb.asm.ClassWriter
{
    //~ Constructors .........................................................................................

    public ClassWriter(int flags)
    {
        super(flags);
    }

    //~ Methods ..............................................................................................

    protected String getCommonSuperClass(String type1, String type2)
    {
        Class<?> c;
        Class<?> d;

        try {
            c = Thread.currentThread().getContextClassLoader().loadClass(type1.replace('/', '.'));
            d = Thread.currentThread().getContextClassLoader().loadClass(type2.replace('/', '.'));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (c.isAssignableFrom(d)) {
            return type1;
        }

        if (d.isAssignableFrom(c)) {
            return type2;
        }

        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        }

        do {
            c = c.getSuperclass();
        }
        while (!c.isAssignableFrom(d));

        return c.getName().replace('.', '/');
    }
}
