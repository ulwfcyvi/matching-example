package org.ssi.model;

public class OrderStatus {
	public static final byte REJECT = -1;
	public static final byte OPEN = 1;
	public static final byte CANCELLED = 2;
	public static final byte FILLED = 3;
	public static final byte PARTIALLY_FILLED = 4;
//	public static final byte ADMIN_CANCEL = 5;
}
