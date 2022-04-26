package org.ssi.serialization;

import org.ssi.collection.Int2DoubleMap;
import org.ssi.collection.Int2ObjectMap;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.io.BufferedChannel;
import org.ssi.model.UserBalance;
import org.ssi.service.BalanceService;
import org.ssi.util.SerializeHelper;

import java.io.IOException;

public class BalanceSerializer {
	public static void encode(BalanceService balance, BufferedChannel channel) throws IOException {
		Long2ObjectMap<UserBalance> balances = balance.getBalances();

		int userNum = balances.size();

		//write balance size
		channel.putInt(userNum);

		long[] keys = balances.keys();
		Object[] values = balances.values();

		for (int i = 0; i < keys.length; i++) {
			long userId = keys[i];
			UserBalance userBalance = (UserBalance)values[i];

			if(userBalance != null) {
				int currencySize = userBalance.size;
				channel.putLong(userId);
				channel.putInt(userBalance.makerFee);
				channel.putInt(userBalance.takerFee);
				channel.putInt(userBalance.brokerId);

				channel.putInt(currencySize);
				for (int j = 0; j < currencySize; j++) {
					int index = j * 3;
					channel.putInt((int) userBalance.balance[index]);
					channel.putLong(userBalance.balance[index + 1]);
					channel.putLong(userBalance.balance[index + 2]);
				}
			}
		}
		
		// write fee default of broker
		Int2ObjectMap<int[]> feeDefault = balance.getMapFeeDefault();
		int brokerNum = feeDefault.size();
		channel.putInt(brokerNum);

		int[] brokerKeys = feeDefault.keys();
		Object[] feeValues = feeDefault.values();

		for (int i = 0; i < brokerKeys.length; i++) {
			int brokerId = brokerKeys[i];
			int[] fee = (int[]) feeValues[i];
			if (fee != null) {
				channel.putInt(brokerId);
				channel.putInt(fee[0]);
				channel.putInt(fee[1]);
			}
		}
		
		// write rate
		Int2DoubleMap rates = balance.getRates();
		
		SerializeHelper.serializeInt2DoubleMap(channel, rates);
	}

	public static void decode(BalanceService balanceService, BufferedChannel channel) throws Exception {

		//read user num
		int userNum = channel.getInt();
		for (int i = 0; i < userNum; i++) {
			long userId = channel.getLong();
			int makerFee = channel.getInt();
			int takerFee = channel.getInt();
			int brokerId = channel.getInt();
			UserBalance userBalance = new UserBalance(userId, makerFee, takerFee, brokerId);
			int currencySize = channel.getInt();
			for (int j = 0; j < currencySize; j++) {
				int currencyId = channel.getInt();
				long amount = channel.getLong();
				long reserve = channel.getLong();
				userBalance.setBalance(currencyId, amount, reserve);
			}

			balanceService.getBalances().put(userId, userBalance);
		}

		// read fee default
		int brokerNum = channel.getInt();
		for (int i = 0; i < brokerNum; i++) {
			int brokerId = channel.getInt();
			int makerFee = channel.getInt();
			int takerFee = channel.getInt();
			
			balanceService.getMapFeeDefault().put(brokerId, new int[] {makerFee, takerFee});
		}

		// read rate
		Int2DoubleMap rates = balanceService.getRates();
		SerializeHelper.deserializeInt2DoubleMap(channel, rates);
	}
}
