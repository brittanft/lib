package org.summoners.function;

public class Tuple<A, B> {
	
	public Tuple(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	private A a;

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}
	
	private B b;

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}
}
