package org.ssi.model;

public class MoneyTransaction {
	public static final byte DEPOSIT = 1;
	public static final byte REQUEST_WITHDRAW = -1;
	public static final byte CONFIRM_WITHDRAW = -2;
	public static final byte CANCEL_WITHDRAW = -3;
	public static final byte CHANGE_BALANCE = 0;
	public static final byte AFFILIATE_CHANGE_BALANCE = 2;
}
