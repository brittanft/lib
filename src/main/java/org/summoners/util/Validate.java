package org.summoners.util;

import java.lang.reflect.InvocationTargetException;
import java.util.function.*;

/**
 * @author Joseph Robert Melsha (jrmelsha@olivet.edu)
 * @link http://www.joemelsha.com
 * @date Jan 30, 2015
 *
 * Copyright 2015 Joseph Robert Melsha
 */
public final class Validate {
	static {
		System.err.println("Commons - Copyright 2015 Joseph Robert Melsha");
	}

	private Validate() {
	}

	// bounds
	private static boolean badBounds(int size, int off, int len) {
		return (off | len | (off + len) | (size - (off + len))) < 0;
	}

	public static void checkBounds(int size, int off, int len) {
		if (badBounds(size, off, len))
			throw offsetException(new IllegalArgumentException(), 1);
	}

	public static void checkBounds(int size, int off, int len, String message) {
		if (badBounds(size, off, len))
			throw offsetException(new IllegalArgumentException(message), 1);
	}

	public static void checkBounds(int size, int off, int len, Supplier<String> messageSupplier) {
		if (badBounds(size, off, len))
			throw offsetException(new IllegalArgumentException(messageSupplier.get()), 1);
	}

	public static <T extends Throwable> void checkBounds(int size, int off, int len, Class<? extends T> type) throws T {
		if (badBounds(size, off, len))
			throw createException(type, 1);
	}

	public static <T extends Throwable> void checkBounds(int size, int off, int len, String message, Class<? extends T> type) throws T {
		if (badBounds(size, off, len))
			throw createException(type, message, 1);
	}

	public static <T extends Throwable> void checkBounds(int size, int off, int len, Supplier<String> messageSupplier, Class<? extends T> type) throws T {
		if (badBounds(size, off, len))
			throw createException(type, messageSupplier.get(), 1);
	}

	// != null
	public static boolean requireNonNulls(Object... values) {
		for (Object value : values)
			if (value == null)
				throw offsetException(new NullPointerException(), 1);
		
		return true;
	}

	// != null
	public static <B> B requireNonNull(B value) {
		if (value == null)
			throw offsetException(new NullPointerException(), 1);
		return value;
	}

	public static <B> B requireNonNull(B value, String message) {
		if (value == null)
			throw offsetException(new NullPointerException(message), 1);
		return value;
	}

	public static <B> B requireNonNull(B value, Supplier<String> messageSupplier) {
		if (value == null)
			throw offsetException(new NullPointerException(messageSupplier.get()), 1);
		return value;
	}

	public static <B, T extends Throwable> B requireNonNull(B value, Class<? extends T> type) throws T {
		if (value == null)
			throw createException(type, 1);
		return value;
	}

	public static <B, T extends Throwable> B requireNonNull(B value, String message, Class<? extends T> type) throws T {
		if (value == null)
			throw createException(type, message, 1);
		return value;
	}

	public static <B, T extends Throwable> B requireNonNull(B value, Supplier<String> messageSupplier, Class<? extends T> type) throws T {
		if (value == null)
			throw createException(type, messageSupplier.get(), 1);
		return value;
	}

	// TODO
	public static <B, T extends Throwable> B throwIfNull(B value, Supplier<? extends T> exceptionSupplier) throws T {
		if (value == null)
			throw offsetSuppliedException(exceptionSupplier.get(), 2);
		return value;
	}

	// == null
	public static <B> B requireNull(B value) {
		if (value != null)
			throw offsetException(new IllegalArgumentException(), 1);
		return null;
	}

	public static <B> B requireNull(B value, String message) {
		if (value != null)
			throw offsetException(new IllegalArgumentException(message), 1);
		return null;
	}

	public static <B> B requireNull(B value, Supplier<String> messageSupplier) {
		if (value != null)
			throw offsetException(new IllegalArgumentException(messageSupplier.get()), 1);
		return null;
	}

	public static <B, T extends Throwable> B requireNull(B value, Class<? extends T> type) throws T {
		if (value != null)
			throw createException(type, 1);
		return null;
	}

	public static <B, T extends Throwable> B requireNull(B value, String message, Class<? extends T> type) throws T {
		if (value != null)
			throw createException(type, message, 1);
		return null;
	}

	public static <B, T extends Throwable> B requireNull(B value, Supplier<String> messageSupplier, Class<? extends T> type) throws T {
		if (value != null)
			throw createException(type, messageSupplier.get(), 1);
		return null;
	}

	// TODO
	public static <B, T extends Throwable> B throwIfNonNull(B value, Supplier<? extends T> exceptionSupplier) throws T {
		if (value != null)
			throw offsetSuppliedException(exceptionSupplier.get(), 2);
		return null;
	}
	
	public static <E, T extends Throwable> boolean check(E item, Predicate<E> checker, Function<E, String> message, Class<? extends T> type) throws T {
		if (checker.test(item))
			throw createException(type, message.apply(item), 1);
		
		return true;
	}

	// == true
	public static boolean require(boolean flag) {
		if (!flag)
			throw offsetException(new IllegalArgumentException(), 1);
		return true;
	}

	public static boolean require(boolean flag, String message) {
		if (!flag)
			throw offsetException(new IllegalArgumentException(message), 1);
		return true;
	}

	public static boolean require(boolean flag, Supplier<String> messageSupplier) {
		if (!flag)
			throw offsetException(new IllegalArgumentException(messageSupplier.get()), 1);
		return true;
	}

	public static <T extends Throwable> boolean require(boolean flag, Class<? extends T> type) throws T {
		if (!flag)
			throw createException(type, 1);
		return true;
	}

	public static <T extends Throwable> boolean require(boolean flag, String message, Class<? extends T> type) throws T {
		if (!flag)
			throw createException(type, message, 1);
		return true;
	}

	public static <T extends Throwable> boolean require(boolean flag, Supplier<String> messageSupplier, Class<? extends T> type) throws T {
		if (!flag)
			throw createException(type, messageSupplier.get(), 1);
		return true;
	}

	// TODO
	public static <T extends Throwable> boolean throwIfFalse(boolean flag, Supplier<? extends T> exceptionSupplier) throws T {
		if (!flag)
			throw offsetSuppliedException(exceptionSupplier.get(), 2);
		return true;
	}

	// == false
	public static boolean requireFalse(boolean flag) {
		if (flag)
			throw offsetException(new IllegalArgumentException(), 1);
		return false;
	}

	public static boolean requireFalse(boolean flag, String message) {
		if (flag)
			throw offsetException(new IllegalArgumentException(message), 1);
		return false;
	}

	public static boolean requireFalse(boolean flag, Supplier<String> messageSupplier) {
		if (flag)
			throw offsetException(new IllegalArgumentException(messageSupplier.get()), 1);
		return false;
	}

	public static <T extends Throwable> boolean requireFalse(boolean flag, Class<? extends T> type) throws T {
		if (flag)
			throw createException(type, 1);
		return false;
	}

	public static <T extends Throwable> boolean requireFalse(boolean flag, String message, Class<? extends T> type) throws T {
		if (flag)
			throw createException(type, message, 1);
		return false;
	}

	public static <T extends Throwable> boolean requireFalse(boolean flag, Supplier<String> messageSupplier, Class<? extends T> type) throws T {
		if (flag)
			throw createException(type, messageSupplier.get(), 1);
		return false;
	}

	// TODO
	public static <T extends Throwable> boolean throwIf(boolean flag, Supplier<? extends T> exceptionSupplier) throws T {
		if (flag)
			throw offsetSuppliedException(exceptionSupplier.get(), 2);
		return false;
	}

	// UTILITIES
	private static <T extends Throwable> T offsetSuppliedException(T exception, int offset) {
		exception.fillInStackTrace();
		return offsetException(exception, offset);
	}

	private static <T extends Throwable> T offsetException(T exception, int offset) {
		StackTraceElement[] stackTrace = exception.getStackTrace();
		if (offset > stackTrace.length)
			offset = stackTrace.length;
		if (offset > 0) {
			StackTraceElement[] tmp = new StackTraceElement[stackTrace.length - offset];
			System.arraycopy(stackTrace, offset, tmp, 0, tmp.length);
			exception.setStackTrace(tmp);
		}
		return exception;
	}

	private static <T extends Throwable> T createException(Class<? extends T> type, int offset) {
		try {
			return offsetException(type.getConstructor().newInstance(), offset + 5);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static <T extends Throwable> T createException(Class<? extends T> type, String message, int offset) {
		try {
			return offsetException(type.getConstructor(String.class).newInstance(message), offset + 5);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}
}
