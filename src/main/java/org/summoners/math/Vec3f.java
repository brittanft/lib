package org.summoners.math;

import java.security.*;
import java.util.*;

/**
 * @author Joseph Robert Melsha (jrmelsha@olivet.edu)
 * @link http://www.joemelsha.com
 * @date Oct 10, 2015
 *
 * Copyright 2015 Joseph Robert Melsha
 */
public class Vec3f {
	public static final Vec3f ID = new Vec3f(0, 0, 0);
	public static final Vec3f ONE = new Vec3f(1, 1, 1);

	public static final float EPSILON = 1E-6F;

	public final float x, y, z;

	public Vec3f(float x, float y, float z) {
		if (x == -0) x = 0;
		if (y == -0) y = 0;
		if (z == -0) z = 0;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3f(float[] array) {
		float x = 0, y = 0, z = 0;
		if (array != null)
			switch (array.length) {
				case 0: break;
				case 1:
					x = array[0]; //???
					break;
				case 2:
					x = array[0];
					y = array[1];
					break;
				default:
					x = array[0];
					y = array[1];
					z = array[2];
					break;
			}
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec3f random() {
		return random(RAND.get());
	}

	public static Vec3f random(Random r) {
		return random(r, 1, 0);
	}

	public static Vec3f random(float d, float m) {
		return random(RAND.get(), d, m);
	}

	public static Vec3f random(Random r, float d, float m) {
		return new Vec3f(m+(float) r.nextGaussian()*d, m+(float) r.nextGaussian()*d, m+(float) r.nextGaussian()*d).norm();
	}

	public static Vec3f random(Vec3f d, Vec3f m) {
		if (d == null) d = ONE;
		if (m == null) m = ID;
		return random(RAND.get(), d, m);
	}
	
	public static final ThreadLocal<Random> RAND = ThreadLocal.withInitial(() -> new SecureRandom());

	public static Vec3f random(Random r, Vec3f d, Vec3f m) {
		if (d == null) d = ONE;
		if (m == null) m = ID;
		return random(r, d.x, d.y, d.z, m.x, m.y, m.z);
	}

	public static Vec3f random(float dx, float dy, float dz, float mx, float my, float mz) {
		return random(RAND.get(), dx, dy, dz, mx, my, mz);
	}

	public static Vec3f random(Random r, float dx, float dy, float dz, float mx, float my, float mz) {
		return new Vec3f(mx+(float) r.nextGaussian()*dx, my+(float) r.nextGaussian()*dy, mz+(float) r.nextGaussian()*dz).norm();
	}

	public static Vec3f anglesToDir(float yaw, float pitch) {
        float pitchTheta = MathUtil.toRadiansf(pitch);
        float yawTheta = MathUtil.toRadiansf(yaw);

        float xz = MathUtil.cosf(pitchTheta);

        float x = -xz * MathUtil.sinf(yawTheta);
        float y = -MathUtil.sinf(pitchTheta);
        float z = xz * MathUtil.cosf(yawTheta);
        return new Vec3f(x, y, z);
	}

	public int intX() {
		return MathUtil.floor(x);
	}

	public int intY() {
		return MathUtil.floor(y);
	}

	public int intZ() {
		return MathUtil.floor(z);
	}

	public Vec3f dir() {
		//TODO: roll
		return anglesToDir(z, y);
	}

	//roll, pitch, yaw
	public Vec3f angles() {
		float yaw, pitch;
		if (x == 0 && y == 0) {
			yaw = 0;
			if (z > 0)
				pitch = -90;
			else if (z < 0)
				pitch = 90;
			else
				pitch = 0;
		} else {
			yaw = MathUtil.toDegreesf(MathUtil.atan2f(-x, y));
			pitch = MathUtil.toDegreesf(MathUtil.atanf(-z / MathUtil.sqrtf(y * y + x * x)));
		}
		return new Vec3f(0, pitch, yaw); //roll will always be 0 because it is underderminable
	}

	//TODO: add function to change roll.

	public float pitch() {
		float pitch;
		if (x == 0 && y == 0) {
			if (z > 0)
				pitch = -90;
			else if (z < 0)
				pitch = 90;
			else
				pitch = 0;
		} else
			pitch = MathUtil.toDegreesf(MathUtil.atanf(-z / MathUtil.sqrtf(y * y + x * x)));
		return pitch;
	}

	public float yaw() {
		float yaw;
		if (x == 0 && y == 0)
			yaw = 0;
		else
			yaw = MathUtil.toDegreesf(MathUtil.atan2f(-x, y));
		return yaw;
	}

	public Vec3f apply(Matrix4f m) {
		return m.apply(this);
	}

	//unary ops
	public float[] array() {
		return new float[] { x, y, z };
	}

	public Vec3f floor() {
		return new Vec3f(MathUtil.floorf(x), MathUtil.floorf(y), MathUtil.floorf(z));
	}

	public Vec3f ceil() {
		return new Vec3f(MathUtil.ceilf(x), MathUtil.ceilf(y), MathUtil.ceilf(z));
	}

	public Vec3f sgn() {
		return new Vec3f(Math.signum(x), Math.signum(y), Math.signum(z));
	}

	public Vec3f neg() {
		return new Vec3f(-x, -y, -z);
	}

	public Vec3f abs() {
		return new Vec3f(Math.abs(x), Math.abs(y), Math.abs(z));
	}

	public Vec3f norm() {
		float len = len();
		if (len <= EPSILON)
			return ID;
		return div(len);
	}

	public float lenSq() {
		return lenSq(x, y, z);
	}

	public static float lenSq(float x, float y, float z) {
		return x * x + y * y + z * z;
	}

	public static float len(float x, float y, float z) {
		return MathUtil.sqrtf(lenSq(x, y, z));
	}

	public float len() {
		return len(x, y, z);
	}

	public float avg() {
		return avg(x, y, z);
	}

	public static float avg(float x, float y, float z) {
		return (x + y + z) / 3;
	}

	public float min() {
		return singleMin(x, y, z);
	}

	public static float singleMin(float x, float y, float z) {
		if (x > y)
			x = y;
		if (x > z)
			x = z;
		return x;
	}

	public float max() {
		return singleMax(x, y, z);
	}

	public static float singleMax(float x, float y, float z) {
		if (x < y)
			x = y;
		if (x < z)
			x = z;
		return x;
	}

	public Vec3f ordered() {
		float ox = x, oy = y, oz = z;
		if (oz < ox) {
			float tmp = oz;
			oz = ox;
			ox = tmp;
		}
		if (oz < oy) {
			float tmp = oz;
			oz = oy;
			oy = tmp;
		}
		if (oy < ox) {
			float tmp = oy;
			oy = ox;
			ox = tmp;
		}
		return new Vec3f(ox, oy, oz);
	}

	public float range() {
		return max() - min();
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float z() {
		return z;
	}

	//binary ops

	public Vec3f x(float x) {
		return new Vec3f(x, y, z);
	}

	public Vec3f y(float y) {
		return new Vec3f(x, y, z);
	}

	public Vec3f z(float z) {
		return new Vec3f(x, y, z);
	}

	public Vec3f add(float n) {
		return new Vec3f(x + n, y + n, z + n);
	}

	public Vec3f addX(float dx) {
		return new Vec3f(x + dx, y, z);
	}

	public Vec3f addY(float dy) {
		return new Vec3f(x, y + dy, z);
	}

	public Vec3f addZ(float dz) {
		return new Vec3f(x, y, z + dz);
	}

	public Vec3f sub(float n) {
		return new Vec3f(x - n, y - n, z - n);
	}

	public Vec3f mul(float n) {
		return new Vec3f(x * n, y * n, z * n);
	}

	public Vec3f div(float n) {
		return new Vec3f(x / n, y / n, z / n);
	}

	public Vec3f mod(float n) {
		return new Vec3f(x % n, y % n, z % n);
	}

	public Vec3f pow(float n) {
		return new Vec3f(MathUtil.powf(x, n), MathUtil.powf(y, n), MathUtil.powf(z, n));
	}

	public Vec3f exp(float n) {
		return new Vec3f(MathUtil.powf(n, x), MathUtil.powf(n, y), MathUtil.powf(n, z));
	}

	//pitch first!
    public Vec3f pitch(float pitch) {
    	float pt = MathUtil.toRadiansf(-pitch);
    	float pc = MathUtil.cosf(pt);
        float ps = MathUtil.sinf(pt);
        float x, y, z;
        x = this.x;
        y = this.y * pc + this.z * ps;
        z = this.z * pc - this.y * ps;
        return new Vec3f(x, y, z);
    }

    public Vec3f yaw(float yaw) {
    	float yt = MathUtil.toRadiansf(-yaw);
    	float yc = MathUtil.cosf(yt);
    	float ys = MathUtil.sinf(yt);
        float x, y, z;
        x = this.x * yc + this.z * ys;
        y = this.y;
        z = this.z * yc - this.x * ys;
        return new Vec3f(x, y, z);
    }

    public Vec3f rot(Vec3f dir) {
    	return rot(dir.z, dir.y);
    }

    public Vec3f rot(float yaw, float pitch) {
    	float pt = MathUtil.toRadiansf(pitch);
    	float pc = MathUtil.cosf(pt);
        float ps = MathUtil.sinf(pt);
        float x, y, z;
        x = this.x;
        y = this.y * pc + this.z * ps;
        z = this.z * pc - this.y * ps;

    	float yt = MathUtil.toRadiansf(-yaw);
    	float yc = MathUtil.cosf(yt);
    	float ys = MathUtil.sinf(yt);
        x = x * yc + z * ys;
        z = z * yc - x * ys;
        return new Vec3f(x, y, z);
    }

	//vec ops
    public float dot(Vec3f o) {
    	return dot(o.x, o.y, o.z);
    }

    public float dot(float ox, float oy, float oz) {
    	return x * ox + y * oy + z * oz;
    }

    public Vec3f cross(Vec3f o) {
    	return cross(o.x, o.y, o.z);
    }

    public Vec3f cross(float ox, float oy, float oz) {
        return new Vec3f(y * oz - oy * z, z * ox - oz * x, x * oy - ox * y);
    }

    public float angle(Vec3f o) {
    	return angle(o.x, o.y, o.z);
    }

    public float angle(float ox, float oy, float oz) {
        float dot = dot(ox, oy, oz) / (len() * len(ox, oy, oz));
        return MathUtil.acosf(dot);
    }

    public Vec3f mid(Vec3f o) {
    	return mid(o.x, o.y, o.z);
    }

    public Vec3f mid(float ox, float oy, float oz) {
    	return new Vec3f((x + ox) / 2, (y + oy) / 2, (z + oz) / 2);
    }

	public Vec3f add(Vec3f o) {
		return add(o.x, o.y, o.z);
	}

	public Vec3f add(float ox, float oy, float oz) {
		return new Vec3f(x + ox, y + oy, z + oz);
	}

	public Vec3f sub(Vec3f o) {
		return sub(o.x, o.y, o.z);
	}

	public Vec3f sub(float ox, float oy, float oz) {
		return new Vec3f(x - ox, y - oy, z - oz);
	}

	public Vec3f subX(float ox) {
		return new Vec3f(x - ox, y, z);
	}

	public Vec3f subY(float oy) {
		return new Vec3f(x, y - oy, z);
	}

	public Vec3f subZ(float oz) {
		return new Vec3f(x, y, z - oz);
	}

	public Vec3f mul(Vec3f o) {
		return mul(o.x, o.y, o.z);
	}

	public Vec3f mul(float ox, float oy, float oz) {
		return new Vec3f(x * ox, y * oy, z * oz);
	}

	public Vec3f mulX(float ox) {
		return new Vec3f(x * ox, y, z);
	}

	public Vec3f mulY(float oy) {
		return new Vec3f(x, y * oy, z);
	}

	public Vec3f mulZ(float oz) {
		return new Vec3f(x, y, z * oz);
	}

	public Vec3f div(Vec3f o) {
		return div(o.x, o.y, o.z);
	}

	public Vec3f div(float ox, float oy, float oz) {
		return new Vec3f(x / ox, y / oy, z / oz);
	}

	public Vec3f divX(float ox) {
		return new Vec3f(x / ox, y, z);
	}

	public Vec3f divY(float oy) {
		return new Vec3f(x, y / oy, z);
	}

	public Vec3f divZ(float oz) {
		return new Vec3f(x, y, z / oz);
	}

	public Vec3f pow(Vec3f o) {
		return pow(o.x, o.y, o.z);
	}

	public Vec3f pow(float ox, float oy, float oz) {
		return new Vec3f(MathUtil.powf(x, ox), MathUtil.powf(y, oy), MathUtil.powf(z, oz));
	}

	public Vec3f powX(float ox) {
		return new Vec3f(MathUtil.powf(x, ox), y, z);
	}

	public Vec3f powY(float oy) {
		return new Vec3f(x, MathUtil.powf(y, oy), z);
	}

	public Vec3f powZ(float oz) {
		return new Vec3f(x, y, MathUtil.powf(z, oz));
	}

	public Vec3f exp(Vec3f o) {
		return exp(o.x, o.y, o.z);
	}

	public Vec3f exp(float ox, float oy, float oz) {
		return new Vec3f(MathUtil.powf(ox, x), MathUtil.powf(oy, y), MathUtil.powf(oz, z));
	}

	public Vec3f expX(float ox) {
		return new Vec3f(MathUtil.powf(ox, x), y, z);
	}

	public Vec3f expY(float oy) {
		return new Vec3f(x, MathUtil.powf(oy, y), z);
	}

	public Vec3f expZ(float oz) {
		return new Vec3f(x, y, MathUtil.powf(oz, z));
	}

	public Vec3f mod(Vec3f o) {
		return mod(o.x, o.y, o.z);
	}

	public Vec3f mod(float ox, float oy, float oz) {
		return new Vec3f(x % ox, y % oy, z % oz);
	}

	public Vec3f modX(float ox) {
		return new Vec3f(x % ox, y, z);
	}

	public Vec3f modY(float oy) {
		return new Vec3f(x, y % oy, z);
	}

	public Vec3f modZ(float oz) {
		return new Vec3f(x, y, z % oz);
	}

	public Vec3f min(Vec3f o) {
		return min(o.x, o.y, o.z);
	}

	public Vec3f min(float ox, float oy, float oz) {
		return new Vec3f(Math.min(x, ox), Math.min(y, oy), Math.min(z, oz));
	}

	public Vec3f max(Vec3f o) {
		return max(o.x, o.y, o.z);
	}

	public Vec3f max(float ox, float oy, float oz) {
		return new Vec3f(Math.max(x, ox), Math.max(y, oy), Math.max(z, oz));
	}

	public float distSq(Vec3f o) {
		return distSq(o.x, o.y, o.z);
	}

	public float distSq(float ox, float oy, float oz) {
		ox -= x;
		oy -= y;
		oz -= z;
		return ox * ox + oy * oy + oz * oz;
	}

	public float dist(Vec3f o) {
		return dist(o.x, o.y, o.z);
	}

	public float dist(float ox, float oy, float oz) {
		return MathUtil.sqrtf(distSq(ox, oy, oz));
	}

	//equals
	public boolean equals(Vec3f o, float epsilon) {
		return equals(o.x, o.y, o.z, epsilon);
	}

	public boolean equals(float ox, float oy, float oz, float epsilon) {
		int lx1 = MathUtil.bitsf(x, epsilon);
		int lx2 = MathUtil.bitsf(ox, epsilon);
		if (lx1 != lx2)
			return false;
		int ly1 = MathUtil.bitsf(y, epsilon);
		int ly2 = MathUtil.bitsf(oy, epsilon);
		if (ly1 != ly2)
			return false;
		int lz1 = MathUtil.bitsf(z, epsilon);
		int lz2 = MathUtil.bitsf(oz, epsilon);
		if (lz1 != lz2)
			return false;
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Vec3f))
			return false;
		Vec3f o = (Vec3f) other;
		if (hashCode != 0 && o.hashCode != 0 && hashCode != o.hashCode)
			return false;
		return equals(o, EPSILON);
	}

	private int hashCode;

	@Override
	public int hashCode() {
		if (hashCode == 0)
			hashCode = hashCode(EPSILON);
		return hashCode;
	}

	public int hashCode(float epsilon) {
		int lx = MathUtil.bitsf(x, epsilon);
		int ly = MathUtil.bitsf(y, epsilon);
		int lz = MathUtil.bitsf(z, epsilon);
		return LookupHash.combine(0x67a21e63, lx, ly, lz);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
