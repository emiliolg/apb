

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


package apb.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import apb.BuildException;
import apb.Environment;
import apb.PropertyException;

import apb.metadata.BuildProperty;
//
// User: emilio
// Date: Mar 11, 2009
// Time: 12:14:16 PM

//
public class PropertyExpansor
{
    //~ Methods ..............................................................................................

    /**
     * Expand the property attributes
     * The expansion order is done using Bread First Sequence.
     * So first the primitive fields are expanded and then the fields
     * with complex objects.
     *
     * @param env
     * @param parent
     * @param object
     */
    public static void expandProperties(Environment env, String parent, Object object)
    {
        // Expand the top level
        // add other fields to the map

        Map<FieldHelper, Object> innerMap = new HashMap<FieldHelper, Object>();

        for (FieldHelper field : publicAndDeclaredfields(env, parent, object)) {
            field.expandProperty(object, innerMap);
        }

        // Expand the inner level
        for (Map.Entry<FieldHelper, Object> entry : innerMap.entrySet()) {
            final FieldHelper field = entry.getKey();
            expandProperties(env, field.getCompoundName(), entry.getValue());
            //field.setFieldValue(object, inner);
        }
    }

    private static Iterable<FieldHelper> publicAndDeclaredfields(Environment env, String parent,
                                                                 Object object)
    {
        Set<FieldHelper> result = new TreeSet<FieldHelper>();

        Class<?> cl = object.getClass();

        for (Field field : cl.getFields()) {
            addToSet(env, result, parent, field);
        }

        for (; cl != null; cl = cl.getSuperclass()) {
            for (Field field : cl.getDeclaredFields()) {
                addToSet(env, result, parent, field);
            }
        }

        return result;
    }

    private static void addToSet(Environment env, Set<FieldHelper> result, String parent, Field field)
    {
        if (!Modifier.isStatic(field.getModifiers())) {
            result.add(new FieldHelper(env, parent, field));
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String BASEDIR = "basedir";

    //~ Inner Classes ........................................................................................

    static class FieldHelper
        implements Comparable<FieldHelper>
    {
        private final BuildProperty annotation;
        private Environment         env;
        private final Field         field;
        private final int           order;
        private final String        parent;

        FieldHelper(Environment env, String parent, Field field)
        {
            this.field = field;
            this.parent = parent;
            annotation = field.getAnnotation(BuildProperty.class);
            order = annotation == null ? Integer.MAX_VALUE : annotation.order();
            this.env = env;
        }

        public Class<?> getType()
        {
            return field.getType();
        }

        public int compareTo(FieldHelper o)
        {
            int result = order - o.order;
            return result != 0 ? result : field.getName().compareTo(o.field.getName());
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
            final String id = NameUtils.idFromMember(field);
            return parent.isEmpty() ? id : parent + "." + id;
        }

        boolean isProperty()
        {
            return annotation != null;
        }

        String expand(String propertyName, Object fieldValue)
        {
            final String value;

            try {
                value = canonize(propertyName, env.expand(String.valueOf(fieldValue)));
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

        <T> void expandProperty(T object, Map<FieldHelper, Object> innerMap)
        {
            Object fieldValue = getFieldValue(object);
            setFieldValue(object, fieldValue);

            if (isProperty()) {
                final Class<?> type = getType();

                if (type.equals(String.class) || type.isPrimitive() || type.isEnum()) {
                    final String propertyName = getCompoundName();
                    final String v = env.getOptionalProperty(propertyName);
                    final String value = v != null ? v : expand(propertyName, fieldValue);

                    setFieldValue(object, convert(value, type));
                    env.putProperty(propertyName, value);
                }
                else if (fieldValue != null) {
                    innerMap.put(this, fieldValue);
                }
            }
        }

        private Object convert(String value, Class<?> type)
        {
            Object result = value;

            if (type != String.class) {
                if (type == Boolean.TYPE) {
                    result = Boolean.parseBoolean(value);
                }
                else if (type == Integer.TYPE) {
                    result = Integer.parseInt(value);
                }
                else {
                    throw new BuildException("Unsuported conversion to " + type.getName());
                }
            }

            return result;
        }

        private String canonize(String propertyName, String value)
        {
            // Special handling to canonize basedir
            return propertyName.equals(BASEDIR) ? FileUtils.normalizePath(new File(value)) : value;
        }
    }
}
