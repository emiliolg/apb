

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


package apb;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import apb.metadata.BuildProperty;

import apb.utils.DebugOption;
import apb.utils.FileUtils;
import apb.utils.NameUtils;
import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import static apb.utils.CollectionUtils.listToString;
import static apb.utils.CollectionUtils.stringToList;
import static apb.utils.StringUtils.isNotEmpty;
//
// User: emilio
// Date: Mar 11, 2009
// Time: 12:14:16 PM

//
class PropertyExpansor
{
    //~ Methods ..............................................................................................

    /**
     * Expand the property attributes
     * The expansion order is done using Bread First Sequence.
     * So first the primitive fields are expanded and then the fields
     * with complex objects.
     *
     * @param helper
     * @param parent
     * @param object
     */
    static void expandProperties(ProjectElementHelper helper, String parent, Object object)
    {
        // Expand the top level
        // add other fields to the map

        Map<FieldHelper, Object> innerMap = new HashMap<FieldHelper, Object>();

        for (FieldHelper field : publicAndDeclaredfields(helper, parent, object)) {
            field.expandProperty(object, innerMap);
        }

        // Expand the inner level
        for (Map.Entry<FieldHelper, Object> entry : innerMap.entrySet()) {
            final FieldHelper field = entry.getKey();
            expandProperties(helper, field.name, entry.getValue());
        }

        if (StringUtils.isNotEmpty(parent)) {
            putInfoObject(helper, parent, object);
        }
    }

    @NotNull static <T> T retrieveInfoObject(ProjectElementHelper helper, String name, Class<T> type)
    {
        Object o = helper.infoMap.get(name);

        if (o != null && !type.isInstance(o)) {
            helper.logWarning("Info object '%s' is of type '%s' instead of '%s'", name,
                              o.getClass().getName(), type.getName());
            o = null;
        }

        if (o == null) {
            o = createDefault(helper, name, type);
        }

        return type.cast(o);
    }

    /**
     * Create a default Info object for that type
     */
    @NotNull private static <T> T createDefault(ProjectElementHelper helper, String name, Class<T> type)
    {
        try {
            T result = type.newInstance();
            expandProperties(helper, name, result);
            return result;
        }
        catch (InstantiationException e) {
            throw new BuildException(e);
        }
        catch (IllegalAccessException e) {
            throw new BuildException(e);
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

    private static void putInfoObject(ProjectElementHelper helper, String name, Object value)
    {
        if (helper.mustShow(DebugOption.PROPERTIES)) {
            helper.logVerbose("Setting info object structure %s:%s\n", helper.getId(), name);
        }

        helper.infoMap.put(name, value);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String BASEDIR = "basedir";

    //~ Inner Classes ........................................................................................

    static class FieldHelper
        implements Comparable<FieldHelper>
    {
        private final boolean              property;
        @NotNull private final Class<?>    elementType;
        @NotNull private final Environment env;
        @NotNull private final Field       field;
        private final int                  order;
        @NotNull private final String      name;

        FieldHelper(Environment env, String parent, Field field)
        {
            this.env = env;
            this.field = field;
            BuildProperty annotation = field.getAnnotation(BuildProperty.class);

            property = annotation != null;

            if (property) {
                order = annotation.order();
                elementType = annotation.elementType();
            }
            else {
                order = Integer.MAX_VALUE;
                elementType = Object.class;
            }

            // Assign name
            StringBuilder result = new StringBuilder();

            if (isNotEmpty(parent)) {
                result.append(parent).append('.');
            }

            result.append(NameUtils.idFromMember(field));
            name = result.toString();
        }

        @NotNull public Class<?> getType()
        {
            return field.getType();
        }

        @NotNull public Class<?> getElementType()
        {
            return elementType;
        }

        public int compareTo(FieldHelper o)
        {
            int result = order - o.order;
            return result != 0 ? result : field.getName().compareTo(o.field.getName());
        }

        @Override public String toString()
        {
            return name;
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

        void setFieldValue(Object result, Object fieldValue)
        {
            try {
                field.set(result, fieldValue);
            }
            catch (IllegalAccessException e) {
                env.handle("Can not set field: " + field.getName());
            }
        }

        boolean isProperty()
        {
            return property;
        }

        @NotNull String setExpandedValue(Object object, String fieldValue)
        {
            String value = "";

            if (isNotEmpty(fieldValue)) {
                value = expand(fieldValue);

                // Special handling to canonize basedir
                if (BASEDIR.equals(name)) {
                    value = FileUtils.normalizePath(new File(value));
                }

                if (!value.equals(fieldValue)) {
                    setFieldValue(object, value);
                }
            }

            return value;
        }

        String setExpandedList(List<String> list)
        {
            String value = "";

            if (list != null && !list.isEmpty()) {
                for (ListIterator<String> it = list.listIterator(); it.hasNext();) {
                    String       s = it.next();
                    final String v = expand(s);

                    if (!v.equals(s)) {
                        it.set(v);
                    }
                }

                value = listToString(list);
            }

            return value;
        }

        void expandProperty(Object object, Map<FieldHelper, Object> innerMap)
        {
            Object fieldValue = getFieldValue(object);

            if (isProperty()) {
                final Class<?> type = getType();

                if (isBasicType(type) || isList() && isBasicType(getElementType())) {
                    final String v = env.getOptionalProperty(name);

                    if (v != null) {
                        if (isList()) {
                            setListValue((List) fieldValue, v);
                        }
                        else {
                            setFieldValue(object, convert(v, type));
                        }
                    }
                    else {
                        String value;

                        if (isString(type)) {
                            value = setExpandedValue(object, (String) fieldValue);
                        }
                        else if (isList() && isString(getElementType())) {
                            value = setExpandedList(asStringList(fieldValue));
                        }
                        else {
                            value = toString(fieldValue);
                        }

                        env.putProperty(name, value);
                    }
                }
                else if (fieldValue != null) {
                    innerMap.put(this, fieldValue);
                }
            }
        }

        @SuppressWarnings("unchecked")
        void setListValue(List list, String vs)
        {
            list.clear();

            for (String v : stringToList(vs)) {
                list.add(convert(v, getElementType()));
            }
        }

        @SuppressWarnings("unchecked")
        private List<String> asStringList(Object o)
        {
            return (List<String>) o;
        }

        private String expand(String fieldValue)
        {
            try {
                return env.expand(fieldValue);
            }
            catch (BuildException e) {
                if (e.getCause() instanceof PropertyException) {
                    PropertyException p = (PropertyException) e.getCause();
                    p.setSource("When initializing field: " + name);
                }

                throw e;
            }
        }

        private String toString(Object fieldValue)
        {
            return fieldValue == null
                   ? "" : isList() ? listToString((List) fieldValue) : String.valueOf(fieldValue);
        }

        private boolean isBasicType(Class<?> type)
        {
            return isString(type) || type.isPrimitive() || type.isEnum() || isPrimitiveWrapper(type);
        }

        private boolean isString(Class<?> type)
        {
            return type.equals(String.class);
        }

        private boolean isList()
        {
            return List.class.isAssignableFrom(getType());
        }

        private boolean isPrimitiveWrapper(Class<?> type)
        {
            return type.equals(Boolean.class) || type.equals(Character.class) || type.equals(Byte.class) ||
                   type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class) ||
                   type.equals(Float.class) || type.equals(Double.class);
        }

        // todo process wrappers also
        private Object convert(String value, Class<?> type)
        {
            Object result = value;

            if (type != String.class) {
                if (type == Boolean.TYPE || type.equals(Boolean.class)) {
                    result = Boolean.parseBoolean(value);
                }
                else if (type == Character.TYPE || type.equals(Character.class)) {
                    result = value.length() == 0 ? 0 : value.charAt(0);
                }
                else if (type == Integer.TYPE || type.equals(Integer.class)) {
                    result = (int) toInt(value);
                }
                else if (type == Byte.TYPE || type.equals(Byte.class)) {
                    result = (byte) toInt(value);
                }
                else if (type == Short.TYPE || type.equals(Short.class)) {
                    result = (short) toInt(value);
                }
                else if (type == Long.TYPE || type.equals(Long.class)) {
                    result = toInt(value);
                }
                else if (type == Float.TYPE || type.equals(Float.class)) {
                    result = (float) toDouble(value);
                }
                else if (type == Double.TYPE || type.equals(Double.class)) {
                    result = toDouble(value);
                }
                else if (type.isEnum()) {
                    final Object[] enums = type.getEnumConstants();
                    result = enums[0];

                    for (Object o : enums) {
                        if (o.toString().equalsIgnoreCase(value)) {
                            result = o;
                            break;
                        }
                    }
                }
                else {
                    throw new BuildException("Unsuported conversion to " + type.getName());
                }
            }

            return result;
        }

        private long toInt(String value)
        {
            long result;

            try {
                result = Long.parseLong(value);
            }
            catch (NumberFormatException e) {
                result = 0;
            }

            return result;
        }

        private double toDouble(String value)
        {
            double result;

            try {
                result = Double.parseDouble(value);
            }
            catch (NumberFormatException e) {
                result = 0;
            }

            return result;
        }
    }
}
