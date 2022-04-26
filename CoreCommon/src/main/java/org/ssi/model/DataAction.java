package org.ssi.model;

public class DataAction {
	public static final byte STOP_APPLYING_RESULTS = -1;
	public static final byte CREATE = 0;
	public static final byte UPDATE = 1;
	public static final byte DELETE = 2;
	public static final byte TAKE_SNAPSHOT = 3;
	public static final byte REPLAY = 4;
	public static final byte SEND = 5;	
	public static final byte INIT_SYMBOL = 6;
	public static final byte INIT_CURRENCY = 7;
}
