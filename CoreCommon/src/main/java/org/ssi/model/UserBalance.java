package org.ssi.model;

import java.util.Arrays;

public class UserBalance {
	private static final int INIT_CURRENCY = 30;
	// [currency -> balance -> reserve]
	public long[] balance;
	public int makerFee = 10;
	public int takerFee = 20;
	public int brokerId = 0;
	public long userId;
	public int size;
	public long lastCurrencyId = 0;
	public int lastCurrencyIndex = 0;


	public UserBalance() {
		balance = new long[INIT_CURRENCY];
		size = INIT_CURRENCY / 3;
		lastCurrencyIndex = 0;
	}

	public UserBalance(long userId, int makerFee, int takerFee, int brokerId) {
		this();

		this.userId = userId;
		this.makerFee = makerFee;
		this.takerFee = takerFee;
		this.brokerId = brokerId;
	}

//	public void reset(long userId, int makerFee, int takerFee) {
//		this.userId = userId;
//		this.makerFee = makerFee;
//		this.takerFee = takerFee;
//		Arrays.fill(balance, 0);
//		size = balance.length / 3;
//		lastCurrencyId = 0;
//		lastCurrencyIndex = 0;
//	}
//	public void addFirstBalance(long userId, int makerFee, int takerFee, int currencyId, long amount, long reserve) {
//		this.userId = userId;
//		this.makerFee = makerFee;
//		this.takerFee = takerFee;
//
//		balance[lastCurrencyIndex] = currencyId;
//		balance[lastCurrencyIndex + 1] = amount;
//		balance[lastCurrencyIndex + 2] = reserve;
//		
//		lastCurrencyId = currencyId;
////		maxCurrencyIndex += 3;
//	}

	public int findIndexBalance(long currencyId) {
		if (lastCurrencyId == currencyId) {
			return lastCurrencyIndex;
		}

		lastCurrencyId = currencyId;
		for (int i = 0; i < size; i++) {
			if (balance[i * 3] == 0) {
				lastCurrencyIndex = i * 3;
				balance[lastCurrencyIndex] = currencyId;
				return lastCurrencyIndex;
			}
			if (balance[i * 3] == currencyId) {
				lastCurrencyIndex = i * 3;
				return lastCurrencyIndex;
			}
		}
		// truong hop ko co
		int newLen = (size + 1) * 3;
		long[] newBalance = new long[newLen];
		
		System.arraycopy(balance, 0, newBalance, 0, size * 3);
		newBalance[size * 3] = currencyId;
		balance = newBalance;
		size = size + 1;

		lastCurrencyIndex = newLen;
		return newLen;
	}

	/**
	 * return false if not exist balance index
	 * @param currencyId
	 * @param amount
	 * @param reserveAmount
	 * @return
	 */
	public boolean setBalance(long currencyId, long amount, long reserveAmount ) {
		if (lastCurrencyId == currencyId) {
			balance[lastCurrencyIndex + 1] = amount;
			balance[lastCurrencyIndex + 2] = reserveAmount;
			return true;
		}
		
		lastCurrencyId = currencyId;
		for (int i = 0; i < size; i++) {
			if (balance[i * 3] == 0) {
				lastCurrencyIndex = i * 3;
				balance[lastCurrencyIndex] = currencyId;
				balance[lastCurrencyIndex + 1] = amount;
				balance[lastCurrencyIndex + 2] = reserveAmount;
				return false;
			}
			if (balance[i * 3] == currencyId) {
				lastCurrencyIndex = i * 3;
				
				balance[lastCurrencyIndex + 1] = amount;
				balance[lastCurrencyIndex + 2] = reserveAmount;
				return true;
			}
		}
		// truong hop ko co
		lastCurrencyIndex = size * 3;
		int newLen = lastCurrencyIndex + 3;
		long[] newBalance = new long[newLen];
		
		System.arraycopy(balance, 0, newBalance, 0, lastCurrencyIndex);
		newBalance[lastCurrencyIndex] = currencyId;
		balance = newBalance;
		size = size + 1;
		
		lastCurrencyIndex = newLen;
		balance[lastCurrencyIndex + 1] = amount;
		balance[lastCurrencyIndex + 2] = reserveAmount;
		return false;
	}
	
	// for test
	public long[] getBalance(long currencyId) {
		int index = findIndexBalance(currencyId);
		return new long[] {balance[index + 1], balance[index + 2]};
	}
	@Override
	public String toString() {
		return "UserBalance [userId =" + userId +  ",balance=" + Arrays.toString(balance) + ", makerFee=" + makerFee + ", takerFee=" + takerFee + "]";
	}
}
