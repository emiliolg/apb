
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

package apb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeSet;

import apb.metadata.BuildProperty;

import apb.utils.NameUtils;
//
// User: emilio
// Date: Nov 5, 2008
// Time: 12:16:05 PM

class FieldHelper
    implements Comparable<FieldHelper>
{
    //~ Instance fields ......................................................................................

    private BuildProperty annotation;
    private Environment   env;
    private Field         field;
    private int           order;
    private String        parent;

    //~ Constructors .........................................................................................

    FieldHelper(Environment env, String parent, Field field)
    {
        this.field = field;
        this.env = env;
        this.parent = parent;
        annotation = field.getAnnotation(BuildProperty.class);
        order = annotation == null ? Integer.MAX_VALUE : annotation.order();
    }

    //~ Methods ..............................................................................................

    public Class<?> getType()
    {
        return field.getType();
    }

    public int compareTo(FieldHelper o)
    {
        int result = order - o.order;
        return result != 0 ? result : field.getName().compareTo(o.field.getName());
    }

    static Iterable<FieldHelper> publicAndDeclaredfields(Environment env, String parent, Object object)
    {
        Set<FieldHelper> result = new TreeSet<FieldHelper>();

        for (Field field : object.getClass().getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                result.add(new FieldHelper(env, parent, field));
            }
        }

        for (Field field : object.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                result.add(new FieldHelper(env, parent, field));
            }
        }

        return result;
    }

    Object getFieldValue(Object object)
    {
        Object fieldValue;

        try {
            field.setAccessible(true);
            fieldValue = field.get(object);
        }
        catch (IllegalAccessException e) {
            env.handle("Can not access field: " + field.getName());
            fieldValue = null;
        }

        return fieldValue;
    }

    <T> void setFieldValue(T result, Object fieldValue)
    {
        try {
            field.set(result, fieldValue);
        }
        catch (IllegalAccessException e) {
            env.handle("Can not set field: " + field.getName());
        }
    }

    String getCompoundName()
    {
        final String id = NameUtils.idFromJavaId(field.getName());
        return parent.isEmpty() ? id : parent + "." + id;
    }

    boolean isProperty()
    {
        return annotation != null;
    }

    String expand(Object fieldValue)
    {
        final String value;

        try {
            value = env.expand(String.valueOf(fieldValue));
        }
        catch (BuildException e) {
            if (e.getCause() instanceof PropertyException) {
                PropertyException p = (PropertyException) e.getCause();
                p.setSource("When initializing field: " + getCompoundName());
            }

            throw e;
        }

        return value;
    }
}
