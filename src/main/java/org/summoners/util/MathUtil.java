package org.summoners.util;

/**
 * @author Joseph Robert Melsha (jrmelsha@olivet.edu)
 * @link http://www.joemelsha.com
 * @date Jan 30, 2015
 *
 * Copyright 2015 Joseph Robert Melsha
 */
public final class MathUtil {
	public static final double PHI = 1.618033988749895D;
	public static final double SQRT_2 = Math.sqrt(2);
	public static final float SQRT_2_F = sqrtf(2);
	public static final float PI_F = (float) Math.PI;

	private MathUtil() {
	}

	public static float toRadiansf(float angdeg) {
		return angdeg / 180.0F * PI_F;
	}

	public static float toDegreesf(float angrad) {
		return angrad * 180.0F / PI_F;
	}

	public static float atan2f(float a, float b) {
		return (float) Math.atan2(a, b);
	}

	public static float atanf(float a) {
		return (float) Math.atan(a);
	}

	public static float tanf(float a) {
		return (float) Math.tan(a);
	}

	public static float sinf(float a) {
		return (float) Math.sin(a);
	}

	public static float cosf(float a) {
		return (float) Math.cos(a);
	}

	public static float acosf(float a) {
		return (float) Math.acos(a);
	}

	public static float powf(float a, float b) {
		return (float) Math.pow(a, b);
	}

	public static float floorf(float f) {
		return (float) Math.floor(f);
	}

	public static float ceilf(float f) {
		return (float) Math.ceil(f);
	}

	public static float sqrtf(float f) {
		return (float) Math.sqrt(f);
	}

	public static int bitsf(float v, float epsilon) {
		if (epsilon != 0)
			v -= v % epsilon;
		return Float.floatToRawIntBits(v);
	}

	public static float angleDiff(float a, float b) {
		float diff = b - a;
		if (diff > 180)
			diff = 360 - diff;
		else if (diff < -180)
			diff = 360 + diff;
		return diff;
	}

	public static float normalizeAngle(float aa) {
		double a = aa;
		if (a < 0)
			a += (Math.floor(-a / 360) + 1) * 360;
		return (float) (a % 360);
	}

	public static long safelyMultiply(long a, long b) {
		if (a == 0)
			return 0;
		long c = a * b;
		if (c / a == b)
			return c;
		if (((a>>>63) ^ (b>>>63)) == 0)
			return Long.MAX_VALUE;
		return Long.MIN_VALUE;
	}

	public static int clip32(long v) {
		if (v < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		if (v > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int) v;
	}

	public static long bits(double v, double epsilon) {
		if (epsilon != 0)
			//v -= Math.IEEEremainder(v, epsilon);
			v -= v % epsilon;
		return Double.doubleToRawLongBits(v);
	}

	public static boolean compareDoubles(double a, double b) {
		return isZero(Double.doubleToRawLongBits(b - a));
	}

	public static boolean compareFloats(float a, float b) {
		return isZero(Float.floatToRawIntBits(b - a));
	}

	public static strictfp boolean isZero(double value) {
		return value == 0.0D;// || Double.doubleToRawLongBits(value) == ZERO_DOUBLE_BITS;
	}

	public static strictfp boolean isZero(float value) {
		return value == 0.0D;// || Float.floatToRawIntBits(value) == ZERO_FLOAT_BITS;
	}

	public static double fixDouble(double value) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(value));
	}

	public static float fixFloat(float value) {
		return Float.intBitsToFloat(Float.floatToRawIntBits(value));
	}

	public static final long ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(0.0D);
	public static final int ZERO_FLOAT_BITS = Float.floatToRawIntBits(0.0F);

	public static int safelySum(int a, int b) {
		if (a > b) {
			int tmp = a;
			a = b;
			b = tmp;
		}
		if (a < 0) {
			if (b >= 0)
				return a + b;
			if (Integer.MIN_VALUE - b <= a)
				return a + b;
			return Integer.MIN_VALUE;
		}
		if (a <= Integer.MAX_VALUE - b)
			return a + b;
		return Integer.MAX_VALUE;
	}

	public static long safelySum(long a, long b) {
		if (a > b) {
			long tmp = a;
			a = b;
			b = tmp;
		}
		if (a < 0) {
			if (b >= 0)
				return a + b;
			if (Long.MIN_VALUE - b <= a)
				return a + b;
			return Long.MIN_VALUE;
		}
		if (a <= Long.MAX_VALUE - b)
			return a + b;
		return Long.MAX_VALUE;
	}

	public static double squareSafe(double value) {
		return isZero(value) ? 0.0D : value * value;
	}

	public static double lengthSafe(double dx, double dy, double dz) {
		return squareSafe(dx) + squareSafe(dy) + squareSafe(dz);
	}

	public static double length(double dx, double dy, double dz) {
		return dx * dx + dy * dy + dz * dz;
	}

	public static int minimum(int a, int b, int c) {
		return a < b ? a < c ? a : c : b < c ? b : c;
	}

	public static int clip(int val, int min, int max) {
		if (val < min)
			val = min;
		if (val > max)
			val = max;
		return val;
	}

	public static int floor(float val) {
		int n = (int) val;
		return val < n ? n - 1 : n;
	}

	public static int floor(double val) {
		int n = (int) val;
		return val < n ? n - 1 : n;
	}

	public static int ceil(float val) {
		int n = (int) val;
		return val > n ? n + 1 : n;
	}

	public static int ceil(double val) {
		int n = (int) val;
		return val > n ? n + 1 : n;
	}

	public static double min(double v1, double v2, double v3, double v4) {
		if (v1 > v2)
			v1 = v2;
		if (v1 > v3)
			v1 = v3;
		if (v1 > v4)
			v1 = v4;
		return v1;
	}

	public static double max(double v1, double v2, double v3, double v4) {
		if (v1 < v2)
			v1 = v2;
		if (v1 < v3)
			v1 = v3;
		if (v1 < v4)
			v1 = v4;
		return v1;
	}

	public static int random(int start, int end) {
		if (end > start) {
			int tmp = start;
			start = end;
			end = tmp;
		}
		return start + (int) (Math.random() * (end - start + 1));
	}

	public static long gcd(long a, long b) {
		long t;
		if (a < 0)
			a = -a;
		if (b == 0)
			return a;
		if (b < 0)
			b = -b;
		if (a == 0)
			return b;
		t = 0;
		while (((a | b) & 0x1) == 0) {
			a >>>= 1;
			b >>>= 1;
			++t;
		}
		while ((a & 0x1) == 0)
			a >>>= 1;
		while ((b & 0x1) == 0)
			b >>>= 1;
		while (a != b)
			if (a > b) {
				a -= b;
				do
					a >>>= 1;
				while ((a & 0x1) == 0);
			} else {
				b -= a;
				do
					b >>>= 1;
				while ((b & 0x1) == 0);
			}
		return a << t;
	}

	public static int gcd(int a, int b) {
		int t;
		if (a < 0)
			a = -a;
		if (b == 0)
			return a;
		if (b < 0)
			b = -b;
		if (a == 0)
			return b;
		t = 0;
		while (((a | b) & 0x1) == 0) {
			a >>>= 1;
			b >>>= 1;
			++t;
		}
		while ((a & 0x1) == 0)
			a >>>= 1;
		while ((b & 0x1) == 0)
			b >>>= 1;
		while (a != b)
			if (a > b) {
				a -= b;
				do
					a >>>= 1;
				while ((a & 0x1) == 0);
			} else {
				b -= a;
				do
					b >>>= 1;
				while ((b & 0x1) == 0);
			}
		return a << t;
	}

	public static boolean isPrime(long n) {
		if (n < 2)
			return false;
		if (n == 2 || n == 3)
			return true;
		if ((n & 0x1) == 0 || n % 3 == 0)
			return false;
		long max = (long) Math.sqrt(n) + 1L; // does this need +1?
		for (long i = 6; i <= max; i += 6)
			if (n % (i - 1) == 0 || n % (i + 1) == 0)
				return false;
		return true;
	}

	public static int swapBits(int v) {
		return (((v) & 0xff) << 24) | (((v >>> 8) & 0xff) << 16) | (((v >>> 16) & 0xff) << 8) | (((v >>> 24)));
	}

	public static long modPow(long a, long b, long mod) {
		long product = 1, pseq = a % mod;
		while (b > 0) {
			if ((b & 0x1) != 0)
				product = modMult(product, pseq, mod);
			pseq = modMult(pseq, pseq, mod);
			b >>>= 1;
		}
		return product;
	}

	public static long modMult(long a, long b, long mod) {
		if (a == 0 || b < mod / a)
			return (a * b) % mod;
		long sum = 0;
		while (b > 0) {
			if ((b & 0x1) != 0)
				sum = (sum + a) % mod;
			a = (a << 1) % mod;
			b >>>= 1;
		}
		return sum;
	}

	public static long modInverse(long a, long n) {
		long i = n, v = 0, d = 1;
		while (a > 0) {
			long t = i / a, x = a;
			a = i % x;
			i = x;
			x = d;
			d = v - t * x;
			v = x;
		}
		v %= n;
		if (v < 0)
			v = (v + n) % n;
		return v;
	}
}
