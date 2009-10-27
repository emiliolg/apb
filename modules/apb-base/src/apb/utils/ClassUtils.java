

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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.tools.ToolProvider;

import apb.BuildException;

import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 6, 2008
// Time: 4:40:56 PM

//
public class ClassUtils
{
    //~ Methods ..............................................................................................

    public static Object newInstance(String className, Object... params)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
               IllegalAccessException, InstantiationException
    {
        return newInstance(ClassUtils.class.getClassLoader(), className, params);
    }

    public static Object newInstance(ClassLoader classLoader, String className, Object... params)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
               IllegalAccessException, InstantiationException
    {
        Class<?> clazz = classLoader.loadClass(className);

        Object result;

        if (params == null) {
            result = clazz.newInstance();
        }
        else {
            result = findConstructor(clazz, params).newInstance(params);
        }

        return result;
    }

    public static Object invoke(Object targetObject, String methodName, Object... params)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        return findMethod(false, targetObject.getClass(), methodName, params).invoke(targetObject, params);
    }

    public static Object invokeNonPublic(Object targetObject, String methodName, Object... params)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        return findMethod(true, targetObject.getClass(), methodName, params).invoke(targetObject, params);
    }

    public static Object fieldValue(Object targetObject, String fieldName)
        throws NoSuchFieldException, IllegalAccessException
    {
        Field fld = targetObject.getClass().getDeclaredField(fieldName);
        fld.setAccessible(true);
        return fld.get(targetObject);
    }

    public static Object invokeStatic(Class clazz, String methodName, Object... params)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        return findMethod(false, clazz, methodName, params).invoke(null, params);
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Object... params)
        throws NoSuchMethodException
    {
        for (Constructor<?> c : clazz.getConstructors()) {
            if (match(c.getParameterTypes(), params)) {
                c.setAccessible(true);
                return c;
            }
        }

        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (match(c.getParameterTypes(), params)) {
                c.setAccessible(true);
                return c;
            }
        }

        throw new NoSuchMethodException("new " + clazz.getName() + argumentTypesToString(params));
    }

    public static File jarFromClass(Class<?> aClass)
    {
        String url = aClass.getResource(NameUtils.simpleName(aClass.getName()) + ".class").toExternalForm();
        int    ind = url.lastIndexOf('!');

        if (ind == -1 || !url.startsWith(JAR_FILE_URL_PREFIX)) {
            throw new BuildException("Can't not find jar " + url);
        }

        return new File(url.substring(JAR_FILE_URL_PREFIX.length(), ind));
    }

    /**
     * Return the jar that contains the tools if different from the standard jar file
     * Null otherwise
     */
    @Nullable public static File toolsJar()
    {
        Class<?> javaCompiler = ToolProvider.getSystemJavaCompiler().getClass();

        Class<?> topClazz = javaCompiler.getDeclaringClass();
        File     result = null;

        try {
            File toolsJar = jarFromClass(topClazz == null ? javaCompiler : topClazz);

            if (!toolsJar.equals(jarFromClass(Class.class))) {
                result = toolsJar;
            }
        }
        catch (Throwable e) {
            result = null;
        }

        return result;
    }

    private static Method findMethod(boolean nonPublic, Class<?> clazz, String methodName, Object... params)
        throws NoSuchMethodException
    {
        final Method[] methods = nonPublic ? clazz.getDeclaredMethods() : clazz.getMethods();

        for (Method c : methods) {
            if (c.getName().equals(methodName) && match(c.getParameterTypes(), params)) {
                c.setAccessible(true);
                return c;
            }
        }

        throw new NoSuchMethodException(clazz.getName() + "." + methodName + argumentTypesToString(params));
    }

    private static boolean match(Class<?>[] paramTypes, Object[] params)
    {
        if (params.length != paramTypes.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];

            if (type.isPrimitive()) {
                type = wrappers.get(type);
            }

            if (params[i] != null && !type.isInstance(params[i])) {
                System.out.println("params = " + params[i]);
                System.out.println("type = " + type);
                return false;
            }
        }

        return true;
    }

    private static String argumentTypesToString(Object[] args)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("(");

        if (args != null) {
            for (Object arg : args) {
                if (buf.length() > 1) {
                    buf.append(", ");
                }

                buf.append(arg == null ? "null" : arg.getClass().getName());
            }
        }

        buf.append(")");
        return buf.toString();
    }

    //~ Static fields/initializers ...........................................................................

    public static final String JAR_FILE_URL_PREFIX = "jar:file:";

    private static final Map<Class, Class> wrappers = new HashMap<Class, Class>();

    static {
        wrappers.put(Boolean.TYPE, Boolean.class);
        wrappers.put(Byte.TYPE, Byte.class);
        wrappers.put(Short.TYPE, Short.class);
        wrappers.put(Integer.TYPE, Integer.class);
        wrappers.put(Long.TYPE, Long.class);
        wrappers.put(Float.TYPE, Float.class);
        wrappers.put(Double.TYPE, Double.class);
        wrappers.put(Character.TYPE, Character.class);
        wrappers.put(Void.TYPE, Void.class);
    }
}
