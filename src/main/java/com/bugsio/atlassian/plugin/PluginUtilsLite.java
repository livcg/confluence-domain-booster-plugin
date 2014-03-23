package com.bugsio.atlassian.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PluginUtilsLite {
			
	public static Object invokeMethodWithKnownParamTypes(
			Object object, String methodName, Class[] methodParamTypes, Object... args) {
		Throwable t = null;
		try {
			Class clazz = object.getClass();
			Method setter = clazz.getMethod(methodName, methodParamTypes);
			return setter.invoke(object, args);
		} catch (NoSuchMethodException e) {
			t = e;
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			t = e;
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			t = e;
			e.printStackTrace();
		}
		throw new RuntimeException("Invoke failed", t);
	}
}
