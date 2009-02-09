package apb.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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

    public static Object invokeStatic(ClassLoader classLoader, String className, String method,
                                      Object... params)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
               IllegalAccessException, InstantiationException
    {
        Class<?> clazz = classLoader.loadClass(className);
        Object   a = ClassUtils.newInstance(classLoader, "org.apache.commons.lang.BitField", 10);
        System.out.println("a.getClass() = " + a.getClass());

        return findMethod(clazz, method, params).invoke(null, params);
    }

    public static Object invoke(Object targetObject, String methodName, Object... params)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        return findMethod(targetObject.getClass(), methodName, params).invoke(targetObject, params);
    }

    private static Constructor<?> findConstructor(Class<?> clazz, Object... params)
        throws NoSuchMethodException
    {
        for (Constructor<?> c : clazz.getConstructors()) {
            if (match(c.getParameterTypes(), params)) {
                return c;
            }
        }

        throw new NoSuchMethodException("new " + clazz.getName() + argumentTypesToString(params));
    }

    private static Method findMethod(Class<?> clazz, String methodName, Object... params)
        throws NoSuchMethodException
    {
        for (Method c : clazz.getMethods()) {
            if (c.getName().equals(methodName) && match(c.getParameterTypes(), params)) {
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

    private static Map<Class, Class> wrappers = new HashMap<Class, Class>();

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
