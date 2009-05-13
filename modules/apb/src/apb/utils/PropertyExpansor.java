

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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import apb.BuildException;
import apb.Environment;
import apb.PropertyException;

import apb.metadata.BuildProperty;
import apb.metadata.Project;
import apb.metadata.ProjectElement;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Mar 11, 2009
// Time: 12:14:16 PM

//
public class PropertyExpansor
{
    //~ Instance fields ......................................................................................

    @NotNull private final Environment env;

    //~ Constructors .........................................................................................

    public PropertyExpansor(Environment env)
    {
        this.env = env;
    }

    //~ Methods ..............................................................................................

    public ProjectElement expand(ProjectElement element)
    {
        initMagicProperties(element);
        return expandProperties("", element);
    }

    /**
     * Init the 3 'magic' properties that are based on the ProjectElement classname:
     * For Modules: module, moduleid & moduledir
     * For Projects: project, projectid & projectdir
     * @param element The ProjectElement to process
     */
    private void initMagicProperties(ProjectElement element)
    {
        final String prop = element instanceof Project ? PROJECT_PROP_KEY : MODULE_PROP_KEY;
        env.putProperty(prop, element.getName());
        env.putProperty(prop + ID_SUFFIX, element.getId());
        env.putProperty(prop + DIR_SUFFIX, element.getDir());
    }

    private Iterable<FieldHelper> publicAndDeclaredfields(String parent, Object object)
    {
        Set<FieldHelper> result = new TreeSet<FieldHelper>();

        Class<?> cl = object.getClass();

        for (Field field : cl.getFields()) {
            addToSet(result, parent, field);
        }

        for (; cl != null; cl = cl.getSuperclass()) {
            for (Field field : cl.getDeclaredFields()) {
                addToSet(result, parent, field);
            }
        }

        return result;
    }

    private void addToSet(Set<FieldHelper> result, String parent, Field field)
    {
        if (!Modifier.isStatic(field.getModifiers())) {
            result.add(new FieldHelper(parent, field));
        }
    }

    /**
     * Expand the property attributes
     * The expansion order is done using Bread First Sequence.
     * So first the primitive fields are expanded and then the fields
     * with complex objects.
     *
     * @param parent
     * @param object
     */
    private <T> T expandProperties(String parent, T object)
    {
        T result = newInstance(object);

        // Expand the top level
        // add other fields to the map

        Map<FieldHelper, Object> innerMap = new HashMap<FieldHelper, Object>();

        for (FieldHelper field : publicAndDeclaredfields(parent, object)) {
            field.expandProperty(object, result, innerMap);
        }

        // Expand the inner level
        for (Map.Entry<FieldHelper, Object> entry : innerMap.entrySet()) {
            final FieldHelper field = entry.getKey();
            Object            inner = expandProperties(field.getCompoundName(), entry.getValue());
            field.setFieldValue(result, inner);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T newInstance(T object)
    {
        final Class<? extends T> c = (Class<? extends T>) object.getClass();

        try {
            Constructor<? extends T> cons = c.getDeclaredConstructor();
            cons.setAccessible(true);
            return cons.newInstance();
        }
        catch (InstantiationException e) {
            env.handle(e);
        }
        catch (IllegalAccessException e) {
            env.handle(e);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return object;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String ID_SUFFIX = "id";
    private static final String DIR_SUFFIX = "dir";
    private static final String PROJECT_PROP_KEY = "project";
    private static final String MODULE_PROP_KEY = "module";
    private static final String BASEDIR = "basedir";

    //~ Inner Classes ........................................................................................

    class FieldHelper
        implements Comparable<FieldHelper>
    {
        private BuildProperty annotation;
        private Field         field;
        private int           order;
        private String        parent;

        FieldHelper(String parent, Field field)
        {
            this.field = field;
            this.parent = parent;
            annotation = field.getAnnotation(BuildProperty.class);
            order = annotation == null ? Integer.MAX_VALUE : annotation.order();
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

        <T> void expandProperty(T object, T result, Map<FieldHelper, Object> innerMap)
        {
            Object fieldValue = getFieldValue(object);
            setFieldValue(result, fieldValue);

            if (isProperty()) {
                final Class<?> type = getType();

                if (type.equals(String.class) || type.isPrimitive() || type.isEnum()) {
                    final String propertyName = getCompoundName();
                    final String v = env.getBaseProperty(propertyName);
                    final String value = v != null ? v : expand(propertyName, fieldValue);

                    setFieldValue(result, convert(value, type));
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
            try {
                return propertyName.equals(BASEDIR) ? new File(value).getCanonicalPath() : value;
            }
            catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }
}
