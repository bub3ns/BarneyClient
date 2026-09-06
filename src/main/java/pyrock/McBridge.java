/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyObject
 *  net.fabricmc.loader.api.FabricLoader
 */
package pyrock;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import jep.python.PyObject;
import moscow.rockstar.core.RockstarClient;
import net.fabricmc.loader.api.FabricLoader;

public final class McBridge {
    private static final String RESOURCE = "/rockstar/mc-mappings.txt.gz";
    private static volatile boolean loaded;
    private static boolean identity;
    private static final Map<String, String> classN2I;
    private static final Map<String, Map<String, String>> fieldsByClass;
    private static final Map<String, Map<String, List<M>>> methodsByClass;
    private static final Map<String, Class<?>> classCache;
    private static final Map<MethodKey, Object> methodCache;
    private static final Map<FieldKey, Object> fieldCache;
    private static final Object MISS;

    private McBridge() {
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            String string = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
            identity = "named".equals(string);
            if (!identity) {
                McBridge.parse();
                RockstarClient.LOGGER.info("[McBridge] \u043c\u0430\u043f\u043f\u0438\u043d\u0433\u0438 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b: {} \u043a\u043b\u0430\u0441\u0441\u043e\u0432", (Object)classN2I.size());
            } else {
                RockstarClient.LOGGER.info("[McBridge] dev-\u0440\u0435\u0436\u0438\u043c (named) \u2014 \u043f\u0435\u0440\u0435\u0432\u043e\u0434 \u043d\u0435 \u043d\u0443\u0436\u0435\u043d");
            }
        }
        catch (Throwable throwable) {
            RockstarClient.LOGGER.error("[McBridge] \u043e\u0448\u0438\u0431\u043a\u0430 \u0438\u043d\u0438\u0446\u0438\u0430\u043b\u0438\u0437\u0430\u0446\u0438\u0438", throwable);
            identity = true;
        }
        loaded = true;
    }

    private static void parse() throws Exception {
        try (InputStream inputStream = McBridge.class.getResourceAsStream(RESOURCE);){
            if (inputStream == null) {
                throw new IllegalStateException("\u0440\u0435\u0441\u0443\u0440\u0441 /rockstar/mc-mappings.txt.gz \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 jar");
            }
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((InputStream)new GZIPInputStream(inputStream), StandardCharsets.UTF_8));){
                String string2;
                String string3 = null;
                HashMap<String, String> hashMap = null;
                HashMap<String, List<M>> hashMap2 = null;
                while ((string2 = bufferedReader.readLine()) != null) {
                    if (string2.isEmpty()) continue;
                    char c = string2.charAt(0);
                    String[] stringArray = string2.split("\t");
                    if (c == 'C') {
                        string3 = stringArray[1];
                        classN2I.put(stringArray[2], stringArray[1]);
                        hashMap = new HashMap<String, String>();
                        hashMap2 = new HashMap<String, List<M>>();
                        fieldsByClass.put(string3, hashMap);
                        methodsByClass.put(string3, hashMap2);
                        continue;
                    }
                    if (c == 'F' && hashMap != null) {
                        hashMap.put(stringArray[1], stringArray[2]);
                        continue;
                    }
                    if (c != 'M' || hashMap2 == null) continue;
                    hashMap2.computeIfAbsent(stringArray[2], string -> new ArrayList<>()).add(new M(stringArray[3], Integer.parseInt(stringArray[1])));
                }
            }
        }
    }

    public static Class<?> findClass(String string2) {
        McBridge.ensureLoaded();
        return classCache.computeIfAbsent(string2, string -> {
            String mappedClassName = identity ? string : classN2I.getOrDefault(string, string);
            try {
                return Class.forName(mappedClassName, false, McBridge.class.getClassLoader());
            }
            catch (ClassNotFoundException classNotFoundException) {
                throw new RuntimeException("\u043a\u043b\u0430\u0441\u0441 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + string + " (" + mappedClassName + ")", classNotFoundException);
            }
        });
    }

    public static boolean isClass(Object object) {
        return object instanceof Class;
    }

    public static boolean isInstance(Object object, String string) {
        Object object2 = McBridge.unwrap(object);
        return object2 != null && McBridge.findClass(string).isInstance(object2);
    }

    public static Object unwrap(Object object) {
        if (object instanceof PyObject) {
            PyObject pyObject = (PyObject)object;
            try {
                Object object2 = pyObject.getAttr("_obj");
                if (object2 != null) {
                    return object2;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return object;
    }

    public static boolean hasField(Object object, String string) {
        Class clazz;
        McBridge.ensureLoaded();
        Object object2 = McBridge.unwrap(object);
        Class clazz2 = object2 instanceof Class ? (clazz = (Class)object2) : object2.getClass();
        return McBridge.lookupField(clazz2, string) != null;
    }

    public static Object getField(Object object, String string) {
        McBridge.ensureLoaded();
        Object target = McBridge.unwrap(object);
        Class<?> owner = target instanceof Class<?> ? (Class<?>)target : target.getClass();
        Field field = McBridge.lookupField(owner, string);
        if (field == null) {
            throw new RuntimeException("\u043d\u0435\u0442 \u043f\u043e\u043b\u044f '" + string + "' \u0443 " + owner.getName());
        }
        try {
            return field.get(target instanceof Class<?> ? null : target);
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new RuntimeException(illegalAccessException);
        }
    }

    public static void setField(Object object, String string, Object object2) {
        McBridge.ensureLoaded();
        Object target = McBridge.unwrap(object);
        Class<?> owner = target instanceof Class<?> ? (Class<?>)target : target.getClass();
        Field field = McBridge.lookupField(owner, string);
        if (field == null) {
            throw new RuntimeException("\u043d\u0435\u0442 \u043f\u043e\u043b\u044f '" + string + "' \u0443 " + owner.getName());
        }
        try {
            field.set(target instanceof Class<?> ? null : target, McBridge.coerce(object2, field.getType()));
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new RuntimeException(illegalAccessException);
        }
    }

    public static Object invoke(Object object, String string, Object[] objectArray) {
        McBridge.ensureLoaded();
        Object object2 = McBridge.unwrap(object);
        boolean bl = object2 instanceof Class;
        Class<?> clazz = bl ? (Class<?>)object2 : object2.getClass();
        int n = objectArray == null ? 0 : objectArray.length;
        Method method = McBridge.lookupMethod(clazz, string, n);
        if (method == null) {
            throw new RuntimeException("\u043d\u0435\u0442 \u043c\u0435\u0442\u043e\u0434\u0430 '" + string + "'(" + n + " \u0430\u0440\u0433.) \u0443 " + clazz.getName());
        }
        try {
            return method.invoke(bl ? null : object2, McBridge.coerceAll(objectArray, method.getParameterTypes()));
        }
        catch (Exception exception) {
            throw new RuntimeException("\u043e\u0448\u0438\u0431\u043a\u0430 \u0432\u044b\u0437\u043e\u0432\u0430 " + string + ": " + exception.getMessage(), exception);
        }
    }

    public static Object construct(String string, Object[] objectArray) {
        return McBridge.constructClass(McBridge.findClass(string), objectArray);
    }

    public static Object constructClass(Class<?> clazz, Object[] objectArray) {
        McBridge.ensureLoaded();
        int n = objectArray == null ? 0 : objectArray.length;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() != n) continue;
            try {
                constructor.setAccessible(true);
                return constructor.newInstance(McBridge.coerceAll(objectArray, constructor.getParameterTypes()));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        throw new RuntimeException("\u043d\u0435\u0442 \u043a\u043e\u043d\u0441\u0442\u0440\u0443\u043a\u0442\u043e\u0440\u0430 " + clazz.getName() + " \u0441 " + n + " \u0430\u0440\u0433.");
    }

    public static Iterator<?> iterator(Object object) {
        Object object2 = McBridge.unwrap(object);
        if (object2 instanceof Iterable) {
            Iterable iterable = (Iterable)object2;
            return iterable.iterator();
        }
        return null;
    }

    private static Field lookupField(Class<?> clazz, String string) {
        FieldKey fieldKey = new FieldKey(clazz, string);
        Object object = fieldCache.get(fieldKey);
        if (object != null) {
            return object == MISS ? null : (Field)object;
        }
        Field field = McBridge.resolveField(clazz, string);
        if (field != null) {
            field.setAccessible(true);
        }
        fieldCache.put(fieldKey, field == null ? MISS : field);
        return field;
    }

    private static Method lookupMethod(Class<?> clazz, String string, int n) {
        MethodKey methodKey = new MethodKey(clazz, string, n);
        Object object = methodCache.get(methodKey);
        if (object != null) {
            return object == MISS ? null : (Method)object;
        }
        Method method = McBridge.resolveMethod(clazz, string, n);
        if (method != null) {
            method.setAccessible(true);
        }
        methodCache.put(methodKey, method == null ? MISS : method);
        return method;
    }

    private static Field resolveField(Class<?> clazz, String string) {
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            String string2 = McBridge.mapFieldName(clazz2, string);
            Field field = McBridge.declaredField(clazz2, string2);
            if (field == null && !string.equals(string2)) {
                field = McBridge.declaredField(clazz2, string);
            }
            if (field == null) continue;
            return field;
        }
        return null;
    }

    private static Field declaredField(Class<?> clazz, String string) {
        if (string == null) {
            return null;
        }
        try {
            return clazz.getDeclaredField(string);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            return null;
        }
    }

    private static String mapFieldName(Class<?> clazz, String string) {
        if (identity) {
            return string;
        }
        Map<String, String> map = fieldsByClass.get(clazz.getName());
        return map == null ? null : map.get(string);
    }

    private static Method resolveMethod(Class<?> clazz, String string, int n) {
        for (Class clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            Method genericDeclaration = McBridge.findOnClass(clazz2, string, n);
            if (genericDeclaration == null) continue;
            return genericDeclaration;
        }
        for (Class clazz2 : McBridge.allInterfaces(clazz)) {
            Method method = McBridge.findOnClass(clazz2, string, n);
            if (method == null) continue;
            return method;
        }
        return null;
    }

    private static Method findOnClass(Class<?> clazz, String string, int n) {
        List<String> list = McBridge.mapMethodNames(clazz, string, n);
        Method method = null;
        for (Method method2 : clazz.getDeclaredMethods()) {
            if (method2.getParameterCount() != n) continue;
            if (list.contains(method2.getName())) {
                return method2;
            }
            if (method != null || !method2.getName().equals(string)) continue;
            method = method2;
        }
        return method;
    }

    private static List<String> mapMethodNames(Class<?> clazz, String string, int n) {
        if (identity) {
            return List.of(string);
        }
        Map<String, List<M>> map = methodsByClass.get(clazz.getName());
        if (map == null) {
            return List.of();
        }
        List<M> list = map.get(string);
        if (list == null) {
            return List.of();
        }
        ArrayList<String> arrayList = new ArrayList<String>(2);
        for (M m : list) {
            if (m.argc != n) continue;
            arrayList.add(m.inter);
        }
        return arrayList;
    }

    private static List<Class<?>> allInterfaces(Class<?> clazz) {
        ArrayList arrayList = new ArrayList();
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            McBridge.collectInterfaces(clazz2, arrayList);
        }
        return arrayList;
    }

    private static void collectInterfaces(Class<?> clazz, List<Class<?>> list) {
        for (Class<?> clazz2 : clazz.getInterfaces()) {
            if (list.contains(clazz2)) continue;
            list.add(clazz2);
            McBridge.collectInterfaces(clazz2, list);
        }
    }

    private static Object[] coerceAll(Object[] objectArray, Class<?>[] classArray) {
        if (objectArray == null) {
            return new Object[0];
        }
        Object[] objectArray2 = new Object[objectArray.length];
        for (int i = 0; i < objectArray.length; ++i) {
            objectArray2[i] = McBridge.coerce(objectArray[i], i < classArray.length ? classArray[i] : Object.class);
        }
        return objectArray2;
    }

    private static Object coerce(Object object, Class<?> clazz) {
        Object object2 = McBridge.unwrap(object);
        if (object2 == null) {
            return null;
        }
        if (object2 instanceof Number) {
            Number number = (Number)object2;
            if (clazz == Integer.TYPE || clazz == Integer.class) {
                return number.intValue();
            }
            if (clazz == Long.TYPE || clazz == Long.class) {
                return number.longValue();
            }
            if (clazz == Float.TYPE || clazz == Float.class) {
                return Float.valueOf(number.floatValue());
            }
            if (clazz == Double.TYPE || clazz == Double.class) {
                return number.doubleValue();
            }
            if (clazz == Short.TYPE || clazz == Short.class) {
                return number.shortValue();
            }
            if (clazz == Byte.TYPE || clazz == Byte.class) {
                return number.byteValue();
            }
        }
        return object2;
    }

    static {
        classN2I = new HashMap<String, String>();
        fieldsByClass = new HashMap<String, Map<String, String>>();
        methodsByClass = new HashMap<String, Map<String, List<M>>>();
        classCache = new ConcurrentHashMap();
        methodCache = new ConcurrentHashMap<MethodKey, Object>();
        fieldCache = new ConcurrentHashMap<FieldKey, Object>();
        MISS = new Object();
    }

    static final class M {
        final String inter;
        final int argc;

        M(String string, int n) {
            this.inter = string;
            this.argc = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "inter", "argc");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "inter", "argc");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "inter", "argc");
        }

        public String inter() {
            return this.inter;
        }

        public int argc() {
            return this.argc;
        }
    }

    record FieldKey(Class<?> owner, String name) {
    }

    record MethodKey(Class<?> owner, String name, int argc) {
    }
}
