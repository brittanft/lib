package org.summoners.math;

/**
 * @author Joseph Robert Melsha (jrmelsha@olivet.edu)
 * @link http://www.joemelsha.com
 * @date Oct 10, 2015
 *
 * Copyright 2015 Joseph Robert Melsha
 */
public class Matrix4f {
	public static final Matrix4f ID = new Matrix4f(1, 0, 0, 0,
	                                               0, 1, 0, 0,
	                                               0, 0, 1, 0,
	                                               0, 0, 0, 1);

	public static final float EPSILON = 1E-6F;

	public final float matrix0_0, matrix0_1, matrix0_2, matrix0_3,
					   matrix1_0, matrix1_1, matrix1_2, matrix1_3,
					   matrix2_0, matrix2_1, matrix2_2, matrix2_3,
					   matrix3_0, matrix3_1, matrix3_2, matrix3_3;

	public Matrix4f(float v11, float v12, float v13, float v14,
	                float v21, float v22, float v23, float v24,
	                float v31, float v32, float v33, float v34,
	                float v41, float v42, float v43, float v44) {
		matrix0_0 = v11; matrix0_1 = v12; matrix0_2 = v13; matrix0_3 = v14;
		matrix1_0 = v21; matrix1_1 = v22; matrix1_2 = v23; matrix1_3 = v24;
		matrix2_0 = v31; matrix2_1 = v32; matrix2_2 = v33; matrix2_3 = v34;
		matrix3_0 = v41; matrix3_1 = v42; matrix3_2 = v43; matrix3_3 = v44;
	}

	public Matrix4f(float[] array) {
		if (array.length >= 16) {
			matrix0_0 = array[ 0]; matrix0_1 = array[ 1]; matrix0_2 = array[ 2]; matrix0_3 = array[ 3];
			matrix1_0 = array[ 4]; matrix1_1 = array[ 5]; matrix1_2 = array[ 6]; matrix1_3 = array[ 7];
			matrix2_0 = array[ 8]; matrix2_1 = array[ 9]; matrix2_2 = array[10]; matrix2_3 = array[11];
			matrix3_0 = array[12]; matrix3_1 = array[13]; matrix3_2 = array[14]; matrix3_3 = array[15];
		} else if (array.length >= 9) {
			matrix0_0 = array[ 0]; matrix0_1 = array[ 1]; matrix0_2 = array[ 2]; matrix0_3 = 0;
			matrix1_0 = array[ 3]; matrix1_1 = array[ 4]; matrix1_2 = array[ 5]; matrix1_3 = 0;
			matrix2_0 = array[ 6]; matrix2_1 = array[ 7]; matrix2_2 = array[ 8]; matrix2_3 = 0;
			matrix3_0 = array[ 9]; matrix3_1 = array[10]; matrix3_2 = array[11]; matrix3_3 = 1;
		} else
			throw new IllegalArgumentException("length must be 9 or 16");
	}

	public float get(int i, int j) {
		if (i < 0 || i > 3 || j < 0 || j > 3)
			throw new IndexOutOfBoundsException();
		switch (i * 4 + j) {
			case 4 * 0 + 0: return matrix0_0;
			case 4 * 0 + 1: return matrix0_1;
			case 4 * 0 + 2: return matrix0_2;
			case 4 * 0 + 3: return matrix0_3;

			case 4 * 1 + 0: return matrix1_0;
			case 4 * 1 + 1: return matrix1_1;
			case 4 * 1 + 2: return matrix1_2;
			case 4 * 1 + 3: return matrix1_3;

			case 4 * 2 + 0: return matrix2_0;
			case 4 * 2 + 1: return matrix2_1;
			case 4 * 2 + 2: return matrix2_2;
			case 4 * 2 + 3: return matrix2_3;

			case 4 * 3 + 0: return matrix3_0;
			case 4 * 3 + 1: return matrix3_1;
			case 4 * 3 + 2: return matrix3_2;
			case 4 * 3 + 3: return matrix3_3;

			default: throw new RuntimeException();
		}
	}

	public float[] array() {
		return new float[] { matrix0_0, matrix0_1, matrix0_2, matrix0_3,
		                     matrix1_0, matrix1_1, matrix1_2, matrix1_3,
		                     matrix2_0, matrix2_1, matrix2_2, matrix2_3,
		                     matrix3_0, matrix3_1, matrix3_2, matrix3_3 };
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Matrix4f))
			return false;
		Matrix4f p = (Matrix4f) obj;
		if (hashCode != 0 && p.hashCode != 0 && hashCode != p.hashCode)
			return false;
		return equals(p, EPSILON);
	}

	private int hashCode;

	@Override
	public int hashCode() {
		if (hashCode == 0)
			hashCode = hashCode(EPSILON);
		return hashCode;
	}

	public int hashCode(float epsilon) {
		int h0_0 = MathUtil.bitsf(matrix0_0, epsilon), h0_1 = MathUtil.bitsf(matrix0_1, epsilon), h0_2 = MathUtil.bitsf(matrix0_2, epsilon), h0_3 = MathUtil.bitsf(matrix0_3, epsilon),
			h1_0 = MathUtil.bitsf(matrix1_0, epsilon), h1_1 = MathUtil.bitsf(matrix1_1, epsilon), h1_2 = MathUtil.bitsf(matrix1_2, epsilon), h1_3 = MathUtil.bitsf(matrix1_3, epsilon),
			h2_0 = MathUtil.bitsf(matrix2_0, epsilon), h2_1 = MathUtil.bitsf(matrix2_1, epsilon), h2_2 = MathUtil.bitsf(matrix2_2, epsilon), h2_3 = MathUtil.bitsf(matrix2_3, epsilon),
			h3_0 = MathUtil.bitsf(matrix3_0, epsilon), h3_1 = MathUtil.bitsf(matrix3_1, epsilon), h3_2 = MathUtil.bitsf(matrix3_2, epsilon), h3_3 = MathUtil.bitsf(matrix3_3, epsilon);
		return LookupHash.combine(0x3728cec4, h0_0, h0_1, h0_2, h0_3,
		                          			  h1_0, h1_1, h1_2, h1_3,
		                             		  h2_0, h2_1, h2_2, h2_3,
		                             		  h3_0, h3_1, h3_2, h3_3);
	}

	public boolean equals(Matrix4f rhs, float epsilon) {
		return deq(matrix0_0, rhs.matrix0_0, epsilon) && deq(matrix0_1, rhs.matrix0_1, epsilon) && deq(matrix0_2, rhs.matrix0_2, epsilon) && deq(matrix0_3, rhs.matrix0_3, epsilon) &&
			   deq(matrix1_0, rhs.matrix1_0, epsilon) && deq(matrix1_1, rhs.matrix1_1, epsilon) && deq(matrix1_2, rhs.matrix1_2, epsilon) && deq(matrix1_3, rhs.matrix1_3, epsilon) &&
			   deq(matrix2_0, rhs.matrix2_0, epsilon) && deq(matrix2_1, rhs.matrix2_1, epsilon) && deq(matrix2_2, rhs.matrix2_2, epsilon) && deq(matrix2_3, rhs.matrix2_3, epsilon) &&
			   deq(matrix3_0, rhs.matrix3_0, epsilon) && deq(matrix3_1, rhs.matrix3_1, epsilon) && deq(matrix3_2, rhs.matrix3_2, epsilon) && deq(matrix3_3, rhs.matrix3_3, epsilon);
	}

	private static boolean deq(float d1, float d2, float epsilon) {
		int l1 = MathUtil.bitsf(d1, epsilon);
		int l2 = MathUtil.bitsf(d2, epsilon);
		return l1 == l2;
	}

	@Override
	public String toString() {
		String s0_0 = String.valueOf(matrix0_0), s0_1 = String.valueOf(matrix0_1), s0_2 = String.valueOf(matrix0_2), s0_3 = String.valueOf(matrix0_3),
			   s1_0 = String.valueOf(matrix1_0), s1_1 = String.valueOf(matrix1_1), s1_2 = String.valueOf(matrix1_2), s1_3 = String.valueOf(matrix1_3),
			   s2_0 = String.valueOf(matrix2_0), s2_1 = String.valueOf(matrix2_1), s2_2 = String.valueOf(matrix2_2), s2_3 = String.valueOf(matrix2_3),
			   s3_0 = String.valueOf(matrix3_0), s3_1 = String.valueOf(matrix3_1), s3_2 = String.valueOf(matrix3_2), s3_3 = String.valueOf(matrix3_3);

		int l0 = s0_0.length(); l0 = Math.max(l0, s1_0.length()); l0 = Math.max(l0, s2_0.length()); l0 = Math.max(l0, s3_0.length());
		int l1 = s0_1.length(); l1 = Math.max(l1, s1_1.length()); l1 = Math.max(l1, s2_1.length()); l1 = Math.max(l1, s3_1.length());
		int l2 = s0_2.length(); l2 = Math.max(l2, s1_2.length()); l2 = Math.max(l2, s2_2.length()); l2 = Math.max(l2, s3_2.length());
		int l3 = s0_3.length(); l3 = Math.max(l3, s1_3.length()); l3 = Math.max(l3, s2_3.length()); l3 = Math.max(l3, s3_3.length());

		s0_0 = fillString(s0_0, l0); s0_1 = fillString(s0_1, l1); s0_2 = fillString(s0_2, l2); s0_3 = fillString(s0_3, l3);
		s1_0 = fillString(s1_0, l0); s1_1 = fillString(s1_1, l1); s1_2 = fillString(s1_2, l2); s1_3 = fillString(s1_3, l3);
		s2_0 = fillString(s2_0, l0); s2_1 = fillString(s2_1, l1); s2_2 = fillString(s2_2, l2); s2_3 = fillString(s2_3, l3);
		s3_0 = fillString(s3_0, l0); s3_1 = fillString(s3_1, l1); s3_2 = fillString(s3_2, l2); s3_3 = fillString(s3_3, l3);

		return "/ " + s0_0 + "  " + s0_1 + "  " + s0_2 + "  " + s0_3 +" \\\n" +
			   "| " + s1_0 + "  " + s1_1 + "  " + s1_2 + "  " + s1_3 + " |\n" +
			   "| " + s2_0 + "  " + s2_1 + "  " + s2_2 + "  " + s2_3 + " |\n" +
			  "\\ " + s3_0 + "  " + s3_1 + "  " + s3_2 + "  " + s3_3 + " /";
	}
	
	public static String fillString(String s, int n) {
		if (s.length() < n) {
			StringBuilder b = new StringBuilder(n);
			n -= s.length();
			for (int i = 0; i < n; ++i)
				b.append(' ');
			b.append(s);
			s = b.toString();
		}
		return s;
	}

	public Matrix4f add(Matrix4f rhs) {
		return new Matrix4f(matrix0_0 + rhs.matrix0_0, matrix0_1 + rhs.matrix0_1, matrix0_2 + rhs.matrix0_2, matrix0_3 + rhs.matrix0_3,
		                    matrix1_0 + rhs.matrix1_0, matrix1_1 + rhs.matrix1_1, matrix1_2 + rhs.matrix1_2, matrix1_3 + rhs.matrix1_3,
		                    matrix2_0 + rhs.matrix2_0, matrix2_1 + rhs.matrix2_1, matrix2_2 + rhs.matrix2_2, matrix2_3 + rhs.matrix2_3,
		                    matrix3_0 + rhs.matrix3_0, matrix3_1 + rhs.matrix3_1, matrix3_2 + rhs.matrix3_2, matrix3_3 + rhs.matrix3_3);
	}

	public Matrix4f sub(Matrix4f rhs) {
		return new Matrix4f(matrix0_0 * rhs.matrix0_0, matrix0_1 * rhs.matrix0_1, matrix0_2 * rhs.matrix0_2, matrix0_3 * rhs.matrix0_3,
		                    matrix1_0 * rhs.matrix1_0, matrix1_1 * rhs.matrix1_1, matrix1_2 * rhs.matrix1_2, matrix1_3 * rhs.matrix1_3,
		                    matrix2_0 * rhs.matrix2_0, matrix2_1 * rhs.matrix2_1, matrix2_2 * rhs.matrix2_2, matrix2_3 * rhs.matrix2_3,
		                    matrix3_0 * rhs.matrix3_0, matrix3_1 * rhs.matrix3_1, matrix3_2 * rhs.matrix3_2, matrix3_3 * rhs.matrix3_3);
	}

	public Matrix4f mul(float c) {
		return new Matrix4f(matrix0_0 * c, matrix0_1 * c, matrix0_2 * c, matrix0_3 * c,
		                    matrix1_0 * c, matrix1_1 * c, matrix1_2 * c, matrix1_3 * c,
		                    matrix2_0 * c, matrix2_1 * c, matrix2_2 * c, matrix2_3 * c,
		                    matrix3_0 * c, matrix3_1 * c, matrix3_2 * c, matrix3_3 * c);
	}

	public Matrix4f div(float c) {
		return new Matrix4f(matrix0_0 / c, matrix0_1 / c, matrix0_2 / c, matrix0_3 / c,
		                    matrix1_0 / c, matrix1_1 / c, matrix1_2 / c, matrix1_3 / c,
		                    matrix2_0 / c, matrix2_1 / c, matrix2_2 / c, matrix2_3 / c,
		                    matrix3_0 / c, matrix3_1 / c, matrix3_2 / c, matrix3_3 / c);
	}

	public Vec3f apply(Vec3f v) {
		return apply(v.x, v.y, v.z);
	}

	public Vec3f apply(float x, float y, float z) {
		float hx = matrix0_0 * x + matrix0_1 * y + matrix0_2 * z + matrix0_3;
		float hy = matrix1_0 * x + matrix1_1 * y + matrix1_2 * z + matrix1_3;
		float hz = matrix2_3;
		float hi = 1;
		return new Vec3f(hx / hi, hy / hi, hz / hi);

		//float hx = x * matrix0_0 + y * matrix0_1 + z * matrix0_2 + matrix0_3;
		//float hy = x * matrix1_0 + y * matrix1_1 + z * matrix1_2 + matrix1_3;
		//float hz = x * matrix2_0 + y * matrix2_1 + z * matrix2_2 + matrix2_3;
		//float hi = x * matrix3_0 + y * matrix3_1 + z * matrix3_2 + matrix3_3;
		//return new Vec3f(hx / hi, hy / hi, hz / hi);
	}

	public Matrix4f mul(Matrix4f o) {
		return new Matrix4f(matrix0_0 * o.matrix0_0 + matrix0_1 * o.matrix1_0 + matrix0_2 * o.matrix2_0 + matrix0_3 * o.matrix3_0, matrix0_0 * o.matrix0_1 + matrix0_1 * o.matrix1_1 + matrix0_2 * o.matrix2_1 + matrix0_3 * o.matrix3_1, matrix0_0 * o.matrix0_2 + matrix0_1 * o.matrix1_2 + matrix0_2 * o.matrix2_2 + matrix0_3 * o.matrix3_2, matrix0_0 * o.matrix0_3 + matrix0_1 * o.matrix1_3 + matrix0_2 * o.matrix2_3 + matrix0_3 * o.matrix3_3,
		                    matrix1_0 * o.matrix0_0 + matrix1_1 * o.matrix1_0 + matrix1_2 * o.matrix2_0 + matrix1_3 * o.matrix3_0, matrix1_0 * o.matrix0_1 + matrix1_1 * o.matrix1_1 + matrix1_2 * o.matrix2_1 + matrix1_3 * o.matrix3_1, matrix1_0 * o.matrix0_2 + matrix1_1 * o.matrix1_2 + matrix1_2 * o.matrix2_2 + matrix1_3 * o.matrix3_2, matrix1_0 * o.matrix0_3 + matrix1_1 * o.matrix1_3 + matrix1_2 * o.matrix2_3 + matrix1_3 * o.matrix3_3,
		                    matrix2_0 * o.matrix0_0 + matrix2_1 * o.matrix1_0 + matrix2_2 * o.matrix2_0 + matrix2_3 * o.matrix3_0, matrix2_0 * o.matrix0_1 + matrix2_1 * o.matrix1_1 + matrix2_2 * o.matrix2_1 + matrix2_3 * o.matrix3_1, matrix2_0 * o.matrix0_2 + matrix2_1 * o.matrix1_2 + matrix2_2 * o.matrix2_2 + matrix2_3 * o.matrix3_2, matrix2_0 * o.matrix0_3 + matrix2_1 * o.matrix1_3 + matrix2_2 * o.matrix2_3 + matrix2_3 * o.matrix3_3,
		                    matrix3_0 * o.matrix0_0 + matrix3_1 * o.matrix1_0 + matrix3_2 * o.matrix2_0 + matrix3_3 * o.matrix3_0, matrix3_0 * o.matrix0_1 + matrix3_1 * o.matrix1_1 + matrix3_2 * o.matrix2_1 + matrix3_3 * o.matrix3_1, matrix3_0 * o.matrix0_2 + matrix3_1 * o.matrix1_2 + matrix3_2 * o.matrix2_2 + matrix3_3 * o.matrix3_2, matrix3_0 * o.matrix0_3 + matrix3_1 * o.matrix1_3 + matrix3_2 * o.matrix2_3 + matrix3_3 * o.matrix3_3);
	}

	public Matrix4f translate(Vec3f v) {
		return translate(v.x, v.y, v.z);
	}

	public Matrix4f translate(float dx, float dy, float dz) {
		Matrix4f trn = new Matrix4f(1, 	0, 	0, 	dx,
		                            0, 	1, 	0, 	dy,
		                            0, 	0, 	1, 	dz,
		                            0, 	0, 	0, 	 1);
		return trn.mul(this);
	}

	public Matrix4f scale(float v) {
		return scale(v, v, v);
	}

	public Matrix4f scale(Vec3f v) {
		return scale(v.x, v.y, v.z);
	}

	public Matrix4f scale(float sx, float sy, float sz) {
		Matrix4f scl = new Matrix4f(sx, 	 0, 	 0, 	0,
		                             0, 	sy, 	 0, 	0,
		                             0, 	 0, 	sz, 	0,
		                             0, 	 0, 	 0, 	1);
		return scl.mul(this);
	}

	public Matrix4f rot(Vec3f angles) {
		return rot(angles.z, angles.y, angles.x);
	}

	public Matrix4f pitchRoll(float pitch, float roll) {
		if (pitch == 0)
			return roll(roll);
		if (roll == 0)
			return pitch(pitch);
		float t = MathUtil.toRadiansf(pitch);
		float cb = MathUtil.cosf(t), sb = MathUtil.sinf(t);
			  t = MathUtil.toRadiansf(roll);
		float cc = MathUtil.cosf(t), sc = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(cb, 	sb * sc, 	 sb * cc, 	0,
		                             0, 	cc, 		-sc, 		0,
		                           -sb, 	cb * sc, 	 cb * cc, 	0,
		                             0, 	 0, 		  0, 		1);
		return rot.mul(this);
	}

	public Matrix4f yawPitch(float yaw, float pitch) {
		if (yaw == 0)
			return pitch(pitch);
		if (pitch == 0)
			return yaw(yaw);
		float t = MathUtil.toRadiansf(-yaw);
		float ca = MathUtil.cosf(t), sa = MathUtil.sinf(t);
			  t = MathUtil.toRadiansf(pitch);
		float cb = MathUtil.cosf(t), sb = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(ca * cb, 	-sa, 	ca * sb, 	0,
		                            sa * cb, 	 ca, 	sa * sb, 	0,
		                           -sb, 		  0, 		 cb, 	0,
		                             0, 		  0, 		  0, 	1);
		return rot.mul(this);
	}

	public Matrix4f yawRoll(float yaw, float roll) {
		if (yaw == 0)
			return roll(roll);
		if (roll == 0)
			return yaw(yaw);
		float t = MathUtil.toRadiansf(-yaw);
		float ca = MathUtil.cosf(t), sa = MathUtil.sinf(t);
			  t = MathUtil.toRadiansf(roll);
		float cc = MathUtil.cosf(t), sc = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(ca, 	-sa * cc, 	sa * sc, 	0,
		                            sa, 	 ca * cc, 	ca * -sc, 	0,
		                             0, 	 sc, 		cc, 		0,
		                             0, 	  0, 		 0, 		1);
		return rot.mul(this);
	}

	public Matrix4f rot(float yaw, float pitch, float roll) {
		if (yaw == 0)
			return pitchRoll(pitch, roll);
		if (pitch == 0)
			return yawRoll(yaw, roll);
		if (roll == 0)
			return yawPitch(yaw, pitch);
		float t = MathUtil.toRadiansf(-yaw);
		float ca = MathUtil.cosf(t), sa = MathUtil.sinf(t);
			  t = MathUtil.toRadiansf(pitch);
		float cb = MathUtil.cosf(t), sb = MathUtil.sinf(t);
			   t = MathUtil.toRadiansf(roll);
		float cc = MathUtil.cosf(t), sc = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(ca * cb, 	ca * sb * sc - sa * cc, 	ca * sb * cc + sa * sc, 	0,
		                            sa * cb, 	sa * sb * sc + ca * cc, 	sa * sb * cc - ca * sc, 	0,
		                           -sb, 		cb * sc, 					cb * cc, 					0,
		                             0, 		 0, 						 0, 						1);
		return rot.mul(this);
	}

	//x
	public Matrix4f roll(float roll) {
		if (roll == 0)
			return this;
		float t = MathUtil.toRadiansf(roll);
		float cc = MathUtil.cosf(t), sc = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(1, 	 0, 	  0, 	0,
		                            0, 	cc, 	-sc, 	0,
		                            0, 	sc, 	 cc, 	0,
		                            0, 	 0, 	  0, 	1);
		return rot.mul(this);
	}

	//z
	public Matrix4f yaw(float yaw) {
		if (yaw == 0)
			return this;
		float t = MathUtil.toRadiansf(-yaw);
		float ca = MathUtil.cosf(t), sa = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(ca, 	-sa, 	0, 		0,
		                            sa, 	 ca, 	0, 		0,
		                             0, 	  0, 	1, 		0,
		                             0, 	  0, 	0, 		1);
		return rot.mul(this);
	}

	//y
	public Matrix4f pitch(float pitch) {
		if (pitch == 0)
			return this;
		float t = MathUtil.toRadiansf(pitch);
		float cb = MathUtil.cosf(t), sb = MathUtil.sinf(t);
		Matrix4f rot = new Matrix4f(cb, 	0, 		sb, 	0,
		                             0, 	1, 		 0, 	0,
		                           -sb, 	0, 		cb, 	0,
		                             0, 	0, 		 0, 	1);
		return rot.mul(this);
	}

	public Matrix4f transpose() {
		return new Matrix4f(matrix0_0, matrix1_0, matrix2_0, matrix3_0,
		                    matrix0_1, matrix1_1, matrix2_1, matrix3_1,
		                    matrix0_2, matrix1_2, matrix2_2, matrix3_2,
		                    matrix0_3, matrix1_3, matrix2_3, matrix3_3);
	}
}
