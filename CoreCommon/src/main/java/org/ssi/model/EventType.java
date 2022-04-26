package org.ssi.model;

public class EventType {
	public static final byte ADD_NEW_USER = 1;
	public static final byte EDIT_USER = 2;
	public static final byte REMOVE_USER = 3;
	public static final byte CHANGE_BALANCE = 4;
	
	public static final byte PLACE_ORDER = 5;
	public static final byte CHANGE_ORDER = 6;
	public static final byte CANCEL_ORDER = 7;
	
	public static final byte ADD_SYMBOL = 8;
	public static final byte EDIT_SYMBOL = 9;
	public static final byte REMOVE_SYMBOL = 10;
	
	public static final byte TAKE_SNAPSHOT = 11;
	public static final byte REPLAY_EVENTS = 12;
	public static final byte SHUTDOWN = 13;

	public static final byte CHANGE_FEE = 14;
	public static final byte CHANGE_AFFILIATE_COMMISION = 15;
	public static final byte RESET_FEE_BY_BROKER = 16;
	public static final byte END_OF_DATE = 17;
	public static final byte END_HOUR = 18;
	
	//control event
	public static final byte APPLYING_RESULTS = 50;
}
