package org.summoners.util;

/**
 * @author Joseph Robert Melsha (jrmelsha@olivet.edu)
 * @link http://www.joemelsha.com
 * @date Jan 30, 2015
 *
 * Copyright 2015 Joseph Robert Melsha
 */
public class LookupHash {
	public static int combine(int iv, int... k) {
		return combine(iv, k, 0, k.length);
	}

	public static int combine(int iv, int[] k, int kOff, int kLen) {
		int a, b, c;
		a = b = c = 0xdeadbeef + (kLen << 2) + iv;
		int i = 0;
		for (int end = kLen - 2; i < end;) {
			a += k[i++];
			b += k[i++];
			c += k[i++];
			/* doMix(a, b, c) */
			a -= c; a ^= rot(c,  4); c += b;
			b -= a; b ^= rot(a,  6); a += c;
			c -= b; c ^= rot(b,  8); b += a;
			a -= c; a ^= rot(c, 16); c += b;
			b -= a; b ^= rot(a, 19); a += c;
			c -= b; c ^= rot(b,  4); b += a;
			/* end */
		}
		switch (kLen - i) {
			case 3:
				c += k[i + 2];
			case 2:
				b += k[i + 1];
			case 1:
				a += k[i];

				/* doFinal(a, b, c) */
				c ^= b; c -= rot(b, 14);
				a ^= c; a -= rot(c, 11);
				b ^= a; b -= rot(a, 25);
				c ^= b; c -= rot(b, 16);
				a ^= c; a -= rot(c,  4);
				b ^= a; b -= rot(a, 14);
				c ^= b; c -= rot(b, 24);
				/* end */
			case 0:
				break;
		}
		return c;
	}

	public static int combine(int iv, CharSequence k) {
		int kLen = k.length();
		int a, b, c;
		a = b = c = 0xdeadbeef + (kLen << 2) + iv;
		int i = 0;
		for (int end = kLen - 2; i < end;) {
			a += k.charAt(i++);
			b += k.charAt(i++);
			c += k.charAt(i++);
			/* doMix(a, b, c) */
			a -= c; a ^= rot(c,  4); c += b;
			b -= a; b ^= rot(a,  6); a += c;
			c -= b; c ^= rot(b,  8); b += a;
			a -= c; a ^= rot(c, 16); c += b;
			b -= a; b ^= rot(a, 19); a += c;
			c -= b; c ^= rot(b,  4); b += a;
			/* end */
		}
		switch (kLen - i) {
			case 3:
				c += k.charAt(i + 2);
			case 2:
				b += k.charAt(i + 1);
			case 1:
				a += k.charAt(i);

				/* doFinal(a, b, c) */
				c ^= b; c -= rot(b, 14);
				a ^= c; a -= rot(c, 11);
				b ^= a; b -= rot(a, 25);
				c ^= b; c -= rot(b, 16);
				a ^= c; a -= rot(c,  4);
				b ^= a; b -= rot(a, 14);
				c ^= b; c -= rot(b, 24);
				/* end */
			case 0:
				break;
		}
		return c;
	}

	static int rot(int x, int k) {
		return (x << k) | (x >>> (32 - k));
	}

	//for reference only
	static void doMix(int a, int b, int c) {
		a -= c; a ^= rot(c,  4); c += b;
		b -= a; b ^= rot(a,  6); a += c;
		c -= b; c ^= rot(b,  8); b += a;
		a -= c; a ^= rot(c, 16); c += b;
		b -= a; b ^= rot(a, 19); a += c;
		c -= b; c ^= rot(b,  4); b += a;
	}

	//for reference only
	static void doFinal(int a, int b, int c) {
		c ^= b; c -= rot(b, 14);
		a ^= c; a -= rot(c, 11);
		b ^= a; b -= rot(a, 25);
		c ^= b; c -= rot(b, 16);
		a ^= c; a -= rot(c,  4);
		b ^= a; b -= rot(a, 14);
		c ^= b; c -= rot(b, 24);
	}

	//hardcoded
	public static int combine(int iv, int k0) {
		return combine(iv, k0, 0, 0);
	}

	public static int combine(int iv, int k0, int k1) {
		return combine(iv, k0, k1, 0);
	}

	public static int combine(int iv, int k0, int k1, int k2) {
		int a, b, c;
		a = b = c = 0xdeadbeef + (3 << 2) + iv;

		a += k0;
		b += k1;
		c += k2;

		/* doMix(a, b, c) */
		a -= c; a ^= rot(c,  4); c += b;
		b -= a; b ^= rot(a,  6); a += c;
		c -= b; c ^= rot(b,  8); b += a;
		a -= c; a ^= rot(c, 16); c += b;
		b -= a; b ^= rot(a, 19); a += c;
		c -= b; c ^= rot(b,  4); b += a;
		/* end */

		return c;
	}

	public static int combine(int iv, int k0, int k1, int k2, int k3) {
		return combine(iv, k0, k1, k2, k3, 0, 0);
	}

	public static int combine(int iv, int k0, int k1, int k2, int k3, int k4) {
		return combine(iv, k0, k1, k2, k3, k4, 0);
	}

	public static int combine(int iv, int k0, int k1, int k2, int k3, int k4, int k5) {
		int a, b, c;
		a = b = c = 0xdeadbeef + (3 << 2) + iv;

		a += k0;
		b += k1;
		c += k2;

		/* doMix(a, b, c) */
		a -= c; a ^= rot(c,  4); c += b;
		b -= a; b ^= rot(a,  6); a += c;
		c -= b; c ^= rot(b,  8); b += a;
		a -= c; a ^= rot(c, 16); c += b;
		b -= a; b ^= rot(a, 19); a += c;
		c -= b; c ^= rot(b,  4); b += a;
		/* end */

		a += k3;
		b += k4;
		c += k5;

		/* doMix(a, b, c) */
		a -= c; a ^= rot(c,  4); c += b;
		b -= a; b ^= rot(a,  6); a += c;
		c -= b; c ^= rot(b,  8); b += a;
		a -= c; a ^= rot(c, 16); c += b;
		b -= a; b ^= rot(a, 19); a += c;
		c -= b; c ^= rot(b,  4); b += a;
		/* end */

		return c;
	}
}
