package org.summoners.math;

public class Quaternion {
	
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public float x, y, z, w;

	public static Quaternion ID = new Quaternion(0F, 0F, 0F, 0F);
}
