package org.ssi.util;

public class MathUtils {
	private static final double EPSILON = 0.01;
	public static long ceil(double a) {
		long b = (long) a;
		if (EPSILON < a - b) {
			return b + 1;
		}
		return b;
	}
	public static long floor(double a) {
		return (long) a;
	}
	
	public static void main(String[] args) {
		double a1 = 1.12345;
		double a2 = 99.9999999999;
		
		System.out.println(floor(a1));
		System.out.println(floor(a2));
	}
}
