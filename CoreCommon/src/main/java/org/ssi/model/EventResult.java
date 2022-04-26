package org.ssi.model;

public class EventResult {
	public static final byte OK = 0;
	public static final byte NOT_ENOUGH_BALANCE = 1;
	public static final byte INVALID_USER_ID = 2;
	public static final byte INVALID_ORDER_ID = 3;
	public static final byte INVALID_SYMBOL = 4;
	public static final byte USER_ALREADY_EXISTED = 5;
	public static final byte SYMBOL_ALREADY_EXISTED = 6;

	public static final byte ORDER_REJECT = 88;
}
