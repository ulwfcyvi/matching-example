package org.ssi.model;

public class OrderType {
	public static final byte LIMIT = 0;
	public static final byte MARKET = 1;
	public static final byte STOP_LIMIT = 2;
	public static final byte STOP_MARKET = 3;
	public static final byte IMMEDIATE_OR_CANCEL = 4;
	public static final byte FILL_OR_KILL = 5;
	
	/** to display history **/
	public static final byte OCO_LIMIT = 6; // temporarily map client OCO order to server's LIMIT Order + tradeoption
	public static final byte OCO_STOP_MARKET = 7;
	public static final byte ICE_BERG = 8;
}
