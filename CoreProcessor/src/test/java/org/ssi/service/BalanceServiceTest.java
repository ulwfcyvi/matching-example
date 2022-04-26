package org.ssi.service;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.ssi.collection.Int2DoubleMap;
import org.ssi.io.BalanceDataWriter;
import org.ssi.matching.MatchingEngine;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventType;
import org.ssi.model.MoneyTransaction;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.util.BitUtil;

//Thai: automated test for BalanceService
public class BalanceServiceTest {
	private static final int BTC = 1;
	private static final int USD = 2;
	private static final int ETH = 3;
	
	private static final long USER_1 = 1;
	private static final long USER_2 = 2;
	
	private static final int BTC_USD = BitUtil.pack(BTC, USD);
	
	private BalanceService balanceService;
	private MatchingEngine matchingEngine;
	private BalanceDataWriter writer;
	
	public BalanceServiceTest() {
		initBalanceService();
		matchingEngine = new MatchingEngine(1 << 10, 1 << 4);
	}
	
	private void initBalanceService() {
		balanceService = new BalanceService();
		
		writer = mock(BalanceDataWriter.class);
		balanceService.balanceDataWriter(writer);
		
		balanceService.rateCounterDecimal(new Int2DoubleMap(1 << 10));
	}

	@Test
	public void test() {
		testPlaceStopLimitOrder();
		assertTrue(true);
	}
	
	public void testPlaceOrder1() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.LIMIT, OrderSide.BID, 8_000, 100_000_000, 0);
		processEvent(event);
	}
	public void testAddAndRemoveUser() {
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.ADD_NEW_USER;
		event.userId = USER_1;
		
		processEvent(event);
		
		assertTrue(balanceService.getBalance(USER_1) != null 
				&& balanceService.getBalance(USER_1).getBalance(BTC)[0] == 0 
				&& balanceService.getBalance(USER_1).getBalance(BTC)[1] == 0);
		
		event = new BaseEvent();
		event.eventType = EventType.REMOVE_USER;
		event.userId = USER_1;
		
		processEvent(event);
		
		assertTrue(balanceService.getBalance(USER_1) == null); 
	}
	
	public void testChangeBalance() {
		BaseEvent event = createChangeBalanceEvent(USER_1, BTC, 1_000);
		processEvent(event);
		
		assertTrue(balanceService.getBalance(USER_1).getBalance(BTC)[0] == 1_000);
		
		event = createChangeBalanceEvent(USER_1, BTC, 999);
		event.orderSide = MoneyTransaction.DEPOSIT;
		processEvent(event);
		
		assertTrue(balanceService.getBalance(USER_1).getBalance(BTC)[0] == 1_999);
		
		event = createChangeBalanceEvent(USER_1, BTC, -999);
		event.orderSide = MoneyTransaction.REQUEST_WITHDRAW;
		processEvent(event);
		
		assertTrue(balanceService.getBalance(USER_1).getBalance(BTC)[0] == 1_000 && balanceService.getBalance(USER_1).getBalance(BTC)[1] == 999);
		
		event = createChangeBalanceEvent(USER_1, BTC, -999);
		event.orderSide = MoneyTransaction.CONFIRM_WITHDRAW;
		processEvent(event);
		
		assertTrue(balanceService.getBalance(USER_1).getBalance(BTC)[0] == 1_000 && balanceService.getBalance(USER_1).getBalance(BTC)[1] == 0);
	}
	
	public void testPlaceLimitOrder() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createChangeBalanceEvent(USER_2, BTC, 200_000_000);
		balanceService.check(event, 1, true);
		
		event = createAddSymbolEvent(BTC_USD, 8, 0, 0);
		balanceService.check(event, 1, true);

		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.LIMIT, OrderSide.BID, 8_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		long[] balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 2_000 && balance[1] == 8_000);
		
		event = createPlaceOrderEvent(USER_2, BTC_USD, OrderType.LIMIT, OrderSide.ASK, 9_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		balance = balanceService.getBalance(USER_2).getBalance(BTC);
		assertTrue(balance[0] == 100_000_000 && balance[1] == 100_000_000);
	}
	
	public void testPlaceMarketOrder() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createChangeBalanceEvent(USER_2, BTC, 200_000_000);
		balanceService.check(event, 1, true);
		
		event = createAddSymbolEvent(BTC_USD, 8, 0, 0);
		balanceService.check(event, 1, true);

		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.MARKET, OrderSide.BID, 8_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		long[] balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 0 && balance[1] == 10_000);
		
		event = createPlaceOrderEvent(USER_2, BTC_USD, OrderType.MARKET, OrderSide.ASK, 9_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		balance = balanceService.getBalance(USER_2).getBalance(BTC);
		assertTrue(balance[0] == 100_000_000 && balance[1] == 100_000_000);
	}
	
	public void testPlaceStopLimitOrder() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createChangeBalanceEvent(USER_2, BTC, 200_000_000);
		balanceService.check(event, 1, true);
		
		event = createAddSymbolEvent(BTC_USD, 8, 0, 0);
		balanceService.check(event, 1, true);

		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.STOP_LIMIT, OrderSide.BID, 8_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		long[] balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 2_000 && balance[1] == 8_000);
		
		event = createPlaceOrderEvent(USER_2, BTC_USD, OrderType.STOP_LIMIT, OrderSide.ASK, 9_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		balance = balanceService.getBalance(USER_2).getBalance(BTC);
		assertTrue(balance[0] == 100_000_000 && balance[1] == 100_000_000);
	}
	
	public void testPlaceStopMarketOrder() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createChangeBalanceEvent(USER_2, BTC, 200_000_000);
		balanceService.check(event, 1, true);
		
		event = createAddSymbolEvent(BTC_USD, 8, 0, 0);
		balanceService.check(event, 1, true);

		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.STOP_LIMIT, OrderSide.BID, 8_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		long[] balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 2_000 && balance[1] == 8_000);
		
		event = createPlaceOrderEvent(USER_2, BTC_USD, OrderType.STOP_LIMIT, OrderSide.ASK, 9_000, 100_000_000, 0);
		balanceService.check(event, 1, true);
		
		balance = balanceService.getBalance(USER_2).getBalance(BTC);
		assertTrue(balance[0] == 100_000_000 && balance[1] == 100_000_000);
	}
	
	public void testMatchLimitOrder() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createChangeBalanceEvent(USER_2, BTC, 200_000_000);
		balanceService.check(event, 1, true);
		
		event = createAddSymbolEvent(BTC_USD, 8, 0, 0);
		processEvent(event);
		
		event = createChangeFeeEvent(USER_1, 20, 30);
		balanceService.check(event, 1, true);
		
		event = createChangeFeeEvent(USER_2, 20, 30);
		balanceService.check(event, 1, true);

		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.LIMIT, OrderSide.BID, 8_000, 100_000_000, 0);
		processEvent(event);
		
		long[] balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 2_000 && balance[1] == 8_000);
		
		event = createPlaceOrderEvent(USER_2, BTC_USD, OrderType.LIMIT, OrderSide.ASK, 8_000, 50_000_000, 0);
		processEvent(event);
		
		balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 2_000 && balance[1] == 4_000);
		//BTC of user 1 = 50_000_000 * (1 - 20 / 10000) = 49_900_000
		balance = balanceService.getBalance(USER_1).getBalance(BTC);
		assertTrue(balance[0] == 49_900_000);
		
		balance = balanceService.getBalance(USER_2).getBalance(BTC);
		assertTrue(balance[0] == 150_000_000);
		//BTC of user 1 = 4_000 * (1 - 30 / 10000) = 3_988
		balance = balanceService.getBalance(USER_2).getBalance(USD);
		assertTrue(balance[0] == 3_988);
	}
	
	public void testMultiMatchLimitOrder() {
		BaseEvent event = createChangeBalanceEvent(USER_1, USD, 10_000);
		balanceService.check(event, 1, true);
		
		event = createChangeBalanceEvent(USER_2, BTC, 200_000_000);
		balanceService.check(event, 1, true);
		
		event = createAddSymbolEvent(BTC_USD, 8, 0, 0);
		processEvent(event);
		
		event = createChangeFeeEvent(USER_1, 20, 30);
		balanceService.check(event, 1, true);
		
		event = createChangeFeeEvent(USER_2, 20, 30);
		balanceService.check(event, 1, true);

		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.LIMIT, OrderSide.BID, 8_000, 15_500_000, 0);
		processEvent(event);
		
		event = createPlaceOrderEvent(USER_1, BTC_USD, OrderType.LIMIT, OrderSide.BID, 8_000, 25_500_000, 0);
		processEvent(event);
		
		long[] balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 6_720 && balance[1] == 3_280);
		
		event = createPlaceOrderEvent(USER_2, BTC_USD, OrderType.LIMIT, OrderSide.ASK, 8_000, 41_000_000, 0);
		processEvent(event);
		
		balance = balanceService.getBalance(USER_1).getBalance(USD);
		assertTrue(balance[0] == 6_720);
		//BTC of user 1 = 15_500_000 * (1 - 20 / 10000) + 25_500_000 * (1 - 20 / 10000)= 40_918_000
		balance = balanceService.getBalance(USER_1).getBalance(BTC);
		assertTrue(balance[0] == 40_918_000);
		
		balance = balanceService.getBalance(USER_2).getBalance(BTC);
		assertTrue(balance[0] == 159_000_000);
		//BTC of user 1 = 8_000 * 0.155 * (1 - 30 / 10000) + 8_000 * 0.255 * (1 - 30 / 10000) = 1_236 + 2033 = 3269
		balance = balanceService.getBalance(USER_2).getBalance(USD);
		assertTrue(balance[0] == 3_269);
	}

	private void processEvent(BaseEvent event) {
		try {
			balanceService.check(event, 1, true);
			matchingEngine.handleEvent(event);
			balanceService.processBalance(event, 1, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private BaseEvent createChangeBalanceEvent(long userId, int currency, long amount) {
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.CHANGE_BALANCE;
		event.userId = userId;
		event.symbol = currency;
		event.amount = amount;
		event.timestamp = System.currentTimeMillis();
		return event;
	}
	
	private BaseEvent createAddSymbolEvent(int symbol, int baseDec, int counterDec, int priceDec) {
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.ADD_SYMBOL;
		event.userId = 0;
		event.symbol = symbol;
		event.orderSide = (byte)baseDec;
		event.orderType = (byte)counterDec;
		event.tradeType = (byte)priceDec;
		event.timestamp = System.currentTimeMillis();
		return event;
	}
	
	private BaseEvent createChangeFeeEvent(long userId, int makerFee, int takerFee) {
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.CHANGE_FEE;
		event.userId = userId;
		event.price = makerFee;
		event.stopPrice = takerFee;
		return event;
	}
	
	private BaseEvent createPlaceOrderEvent(long userId, int symbol, byte orderType, byte orderSide, long price, long amount, long stopPrice) {
		BaseEvent event = new BaseEvent();
		
		event.timestamp = System.currentTimeMillis();
		event.eventType = EventType.PLACE_ORDER;
		event.userId = userId;
		event.symbol = symbol;
		event.orderType = orderType;
		event.orderSide = orderSide;
		event.price = price;
		event.amount = amount;
		event.stopPrice = stopPrice;
		return event;
	}
}
