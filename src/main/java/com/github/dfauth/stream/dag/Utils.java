package com.github.dfauth.stream.dag;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Utils {
    @FunctionalInterface
    private interface SilentInvoker extends Function<Callable<?>, Object> {
        MethodType SIGNATURE = MethodType.methodType(Object.class, Callable.class);//signature after type erasure

        <V> V invoke(final Callable<V> callable);

        @Override
        default Object apply(final Callable<?> callable) {
            return invoke(callable);
        }

        static <V> V call(final Callable<V> callable) throws Exception {
            return callable.call();
        }
    }

    private static final SilentInvoker SILENT_INVOKER;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "invoke",
                    MethodType.methodType(SilentInvoker.class),
                    SilentInvoker.SIGNATURE,
                    lookup.findStatic(SilentInvoker.class, "call", SilentInvoker.SIGNATURE),
                    SilentInvoker.SIGNATURE);
            SILENT_INVOKER = (SilentInvoker) site.getTarget().invokeExact();
        } catch (final Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Utils(){
        throw new InstantiationError();
    }

    /**
     * Calls code with checked exception as a code without checked exception.
     * <p>
     *     This method should be used instead of wrapping some code into try-catch block with ignored exception.
     *     Don't use this method to hide checked exception that can be actually happened at runtime in some conditions.
     * @param callable Portion of code to execute.
     * @param <V> Type of result.
     * @return An object returned by portion of code.
     */
    public static <V> V callUnchecked(final Callable<V> callable) {
        return SILENT_INVOKER.invoke(callable);
    }

    public static Optional<Method> checkAndExtractLambdaMethod(
            Function function) throws ClassNotFoundException,
            InvocationTargetException, IllegalAccessException,
            NoSuchMethodException {
        Object serializedLambda = null;
        for (Class<?> clazz = function.getClass(); clazz != null; clazz = clazz
                .getSuperclass()) {//w  w w. java  2s . c  o  m
            Method replaceMethod = clazz.getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);
            Object serialVersion = replaceMethod.invoke(function);
            if (serialVersion.getClass().getName()
                    .equals("java.lang.invoke.SerializedLambda")) {
                Class.forName("java.lang.invoke.SerializedLambda");
                serializedLambda = serialVersion;
                break;
            }
        }
        if (serializedLambda == null) {
            return Optional.empty();
        }
        Method implClassMethod = serializedLambda.getClass()
                .getDeclaredMethod("getImplClass");
        Method implMethodNameMethod = serializedLambda.getClass()
                .getDeclaredMethod("getImplMethodName");
        String className = (String) implClassMethod
                .invoke(serializedLambda);
        String methodName = (String) implMethodNameMethod
                .invoke(serializedLambda);
        Class<?> implClass = Class.forName(className.replace('/', '.'),
                true, Thread.currentThread().getContextClassLoader());
        Method[] methods = implClass.getDeclaredMethods();
        Method parameterizedMethod = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                parameterizedMethod = method;
            }
        }
        return Optional.of(parameterizedMethod);
    }

}