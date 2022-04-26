package org.ssi.balance;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ssi.consumer.OrdersConsumer;
import org.ssi.io.BalanceDataWriter;
import org.ssi.matching.MatchingEngine;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventType;
import org.ssi.model.MoneyTransaction;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.model.SymbolConfig;
import org.ssi.model.TradeOption;
import org.ssi.model.UserBalance;
import org.ssi.service.BalanceService;
import org.ssi.service.MetadataService;
import org.ssi.util.BitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties
@SpringBootTest(classes = { BalanceService.class, BalanceDataWriter.class, MetadataService.class, OrdersConsumer.class})
@RunWith(SpringRunner.class)
public class BalanceTest {
	private static final int BTC = 1;
	private static final int USD = 2;
	private static final int ETH = 3;
	private static final int EOS = 4;
	private static final int XRP = 5;
	private static final int LTC = 6;
	private static final int ETC = 7;
	private static final int XTZ = 8;
	private static final int ZRX = 9;
	private static final int ENJ = 10;
	
	
	
	
	private static final int XRPBTC = BitUtil.pack(XRP, BTC);
	private static final int LTCBTC = BitUtil.pack(LTC, BTC);
	private static final int ETCBTC = BitUtil.pack(ETC, BTC);
	private static final int XTZBTC = BitUtil.pack(XTZ, BTC);
	private static final int ZRXETH = BitUtil.pack(ZRX, ETH);
	private static final int ENJETH = BitUtil.pack(ENJ, ETH);
	
	
	
	
	
	private static final int BTCUSD = BitUtil.pack(BTC, USD);
	private static final int ETHUSD = BitUtil.pack(ETH, USD);
	private static final int EOSUSD = BitUtil.pack(EOS, USD);
	private static final int ETHBTC = BitUtil.pack(ETH, BTC);
	
	
	private static final int maxOpenOrderNum = 1000000;
	private static final int maxSymbolNum = 100;
	
	private static final int[] symbols = new int[] {BTCUSD, ETHUSD, EOSUSD, ETHBTC};
//	private static final byte[] sytmbolDecimalPrice = new byte[] {4, 4, 4, 8};
	private static final int[][] currency = {
			{BTC, USD},
			{ETH, USD},
			{EOS, USD},
			{ETH, BTC},
	};
	private static final long[] POW = new long[] {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
	private static byte[] currencyDecimal;
	private static byte[] priceDecimal;
	
	
	private static long[] currencyDecimalPow;
	private static long[] priceDecimalPow;
	private static long[] amountDecimalPow;
	static {
		System.out.println(BitUtil.pack(ETH, BTC));
		currencyDecimal = new byte[1000];
		currencyDecimal[BTC] = 9;
		currencyDecimal[USD] = 8;
		currencyDecimal[ETH] = 9;
		currencyDecimal[EOS] = 8;
		
		int size = 1 << 20;
		priceDecimal = new byte[size];
		priceDecimal[BTCUSD] = 2;
		priceDecimal[ETHUSD] = 2;
		priceDecimal[EOSUSD] = 4;
		priceDecimal[ETHBTC] = 6;

		currencyDecimalPow = new long[1000];
		Arrays.fill(currencyDecimalPow, 1);
		currencyDecimalPow[BTC] = POW[9];
		currencyDecimalPow[USD] = POW[8];
		currencyDecimalPow[ETH] = POW[9];
		currencyDecimalPow[EOS] = POW[8];

		amountDecimalPow = new long[size];
		Arrays.fill(amountDecimalPow, 1);
		amountDecimalPow[BTCUSD] = POW[6];
		amountDecimalPow[ETHUSD] = POW[5];
		amountDecimalPow[EOSUSD] = POW[3];
		amountDecimalPow[ETHBTC] = POW[3];

		priceDecimalPow = new long[size];
		Arrays.fill(priceDecimalPow, 1);
		priceDecimalPow[BTCUSD] = POW[2];
		priceDecimalPow[ETHUSD] = POW[2];
		priceDecimalPow[EOSUSD] = POW[4];
		priceDecimalPow[ETHBTC] = POW[6];

		
	}
	
	static {
		File index = new File("D:\\tmp");
		if (index.exists()) {
			String[]entries = index.list();
			for(String s: entries){
			    File currentFile = new File(index.getAbsolutePath(),s);
			    try {
					FileUtils.forceDelete(currentFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Autowired
	private BalanceService balanceService;

	@Autowired
	private OrdersConsumer ordersConsumer;
	
	
	private static MatchingEngine matchingEngine = null;
	
	@Before
	public void before() throws Exception {
		//System.out.println("before");
		balanceService.clear();
		matchingEngine = new MatchingEngine(maxOpenOrderNum, maxSymbolNum, getConfigArr());
		initSymbolAndCurrency();
		initUser();
	}
	@After
	public void after() {
		//System.out.println("after");
		clearSymbolAndCurrency();
		clearUser();
	}

	private void initUser() throws Exception {
		int userNo = 5;
		int[] currency = new int[] {BTC, USD, ETH, EOS};
		for (int i = 0; i < currency.length; i++) {
			int curr = currency[i];
			for (int userId = 1; userId < userNo; userId++) {
				long balance = 10000 * currencyDecimalPow[curr]; 
//				if (curr == currency[1]) {
//					balance = (long) (1000 * currencyDecimalPow[curr]);
//				}
				BaseEvent event = createEventChangeBalance(userId, curr, balance);
				processEvent(event);
			}
		}
	}
	
	/** test add user **/
	// @Test
	public void testADD_NEW_USER () throws Exception {
		//System.out.println("BalanceTest.testADD_NEW_USER()");
		// matchingEngine = new MatchingEngine(maxOpenOrderNum, getConfigArr());
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.ADD_NEW_USER;
		event.userId = 1000;
		
		// 
		processEvent(event);
		// assert
		assertTrue(balanceService.getBalance(1000) != null 
				&& balanceService.getBalance(1000).getBalance(BTC)[0] == 0 
				&& balanceService.getBalance(1000).getBalance(BTC)[1] == 0);
	}

	/** test remove user **/
	// @Test
	public void testREMOVE_USER () throws Exception {
		//System.out.println("BalanceTest.testREMOVE_USER()");
		// matchingEngine = new MatchingEngine(maxOpenOrderNum, getConfigArr());
		testADD_NEW_USER();

		BaseEvent event = new BaseEvent();
		event.eventType = EventType.REMOVE_USER;
		event.userId = 1000;
		
		// 
		processEvent(event);
		// assert
		assertTrue(balanceService.getBalance(1000) == null);
	}
	/** test add balance **/
	// @Test
	public void testCHANGE_BALANCE1 () throws Exception {
		BaseEvent event = createEventChangeBalance(1, BTC, 1000);
		processEvent(event);
		assertTrueBalance(event.userId, BTC, USD, 10001000, 0, 1000, 0);
	}

	/** test add balance cho user moi **/
	// @Test
	public void testCHANGE_BALANCE2 () throws Exception {
		BaseEvent event = createEventChangeBalance(1234, BTC, 1000);
		processEvent(event);
		assertTrueBalance(event.userId, BTC, USD, 1000, 0, 0, 0);
	}
	
	/**
	 * test truong hop BUY order that bai
	 */
	// @Test
	public void testPLACE_ORDER1 () throws Exception {
		BaseEvent event = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 3, 2, 0);
		processEvent(event);

		assertTrueBalance(event.userId, BTC, USD, 1000, 0, 994, 6);
	}

	/** test truong hop BUY success **/
	// @Test
	public void testPLACE_ORDER2 () throws Exception {
		BaseEvent event = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 3, 2, 0);
		processEvent(event);
		assertTrueBalance(event.userId, BTC, USD, 1000, 0, 994, 6);
	}

	/** test truong hop SELL success **/
	// @Test
	public void testPLACE_ORDER3 () throws Exception {
		BaseEvent event = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 3, 2, 0);
		processEvent(event);
		assertTrueBalance(event.userId, BTC, USD, 998, 2, 1000, 0);
	}
	
	// test order market buy: sell 2, 3, 4, 5/ market buy 10.
	// @Test
	public void testPLACE_ORDER4 () throws Exception {
		// tao balance trc
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 3, 2, 0);
		BaseEvent event2 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 4, 3, 0);
		BaseEvent event3 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 5, 4, 0);
		BaseEvent event4 = createEventOrder(4, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 6, 5, 0);
		BaseEvent event5 = createEventOrder(5, BTCUSD, OrderType.MARKET, OrderSide.BID, 14, 10, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		processEvent(event4);
		processEvent(event5);
		
		// assert
		assertTrueBalance(event1.userId, BTC, USD, 998, 0, 10000006, 0);
		assertTrueBalance(event2.userId, BTC, USD, 997, 0, 10000012, 0);
		assertTrueBalance(event3.userId, BTC, USD, 996, 0, 10000020, 0);
		assertTrueBalance(event4.userId, BTC, USD, 995, 4, 10000006, 0);
		assertTrueBalance(event5.userId, BTC, USD, 10000010, 0, 9999956, 0);
		
	}

	// test order market buy: sell 2, 3, 4, 5/ market buy 14.
	// @Test
	public void testPLACE_ORDER5 () throws Exception {
		// tao balance trc
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 3, 2, 0);
		BaseEvent event2 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 4, 3, 0);
		BaseEvent event3 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 5, 4, 0);
		BaseEvent event4 = createEventOrder(4, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 6, 5, 0);
		BaseEvent event5 = createEventOrder(5, BTCUSD, OrderType.MARKET, OrderSide.BID, 6, 14, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		processEvent(event4);
		processEvent(event5);
		
		// assert
		assertTrueBalance(event1.userId, BTC, USD, 998, 0, 10000006, 0);
		assertTrueBalance(event2.userId, BTC, USD, 997, 0, 10000012, 0);
		assertTrueBalance(event3.userId, BTC, USD, 996, 0, 10000020, 0);
		assertTrueBalance(event4.userId, BTC, USD, 995, 0, 10000030, 0);
		assertTrueBalance(event5.userId, BTC, USD, 10000014, 0, 9999932, 0);
		
	}

	// test order market buy: sell 2, 3, 4, 5/ market buy 16.
	// @Test
	public void testPLACE_ORDER6 () throws Exception {
		// tao balance trc
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 3, 2, 0);
		BaseEvent event2 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 4, 3, 0);
		BaseEvent event3 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 5, 4, 0);
		BaseEvent event4 = createEventOrder(4, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 6, 5, 0);
		BaseEvent event5 = createEventOrder(5, BTCUSD, OrderType.MARKET, OrderSide.BID, 6, 16, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		processEvent(event4);
		processEvent(event5);
		
		// assert
		assertTrueBalance(event1.userId, BTC, USD, 998, 0, 10000006, 0);
		assertTrueBalance(event2.userId, BTC, USD, 997, 0, 10000012, 0);
		assertTrueBalance(event3.userId, BTC, USD, 996, 0, 10000020, 0);
		assertTrueBalance(event4.userId, BTC, USD, 995, 0, 10000030, 0);
		assertTrueBalance(event5.userId, BTC, USD, 10000014, 0, 9999932, 0);
		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	// @Test
	public void testPLACE_ORDER7 () throws Exception {
		System.out.println("BalanceTest.testPLACE_ORDER7()");
		// tao balance trc
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 10, 8, 0);
		BaseEvent event2 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 10, 2, 0);
		BaseEvent event3 = createEventOrder(3, BTCUSD, OrderType.MARKET, OrderSide.ASK, 10, 6, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);

		// assert
		assertTrueBalance(event1.userId, BTC, USD, 10000008, 0, 9999920, 0);
		assertTrueBalance(event2.userId, BTC, USD, 998, 0, 10000020, 0);
		assertTrueBalance(event3.userId, BTC, USD, 994, 0, 10000060, 0);
		
	}
	// @Test
	public void testPLACE_ORDER8 () throws Exception {
		System.out.println("BalanceTest.testPLACE_ORDER8()");
		// tao balance trc
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 5, 2, 0);
		BaseEvent event2 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 8, 1, 0);
		BaseEvent event3 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.BID, 9, 4, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		
		BaseEvent cancelSell = createEventCancel(3, event3.orderId, BTCUSD);
		processEvent(cancelSell);
		// assert
		
		int a = 0;
		assertTrue(a == 0);
	}
	// @Test
	public void testPLACE_ORDER9 () throws Exception {
		System.out.println("BalanceTest.testPLACE_ORDER9()");
		// tao balance trc
		BaseEvent event01 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 1, 1, 0);
		BaseEvent event02 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 1, 1, 0);
		processEvent(event01);
		processEvent(event02);
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.STOP_LIMIT, OrderSide.BID, 3, 1, 0);
		event1.stopPrice = 2;
		BaseEvent event2 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.BID, 2, 2, 0);
		BaseEvent event3 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 2, 4, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		
		BaseEvent cancelSell = createEventCancel(3, event3.orderId, BTCUSD);
		processEvent(cancelSell);
		// assert
		
		int a = 0;
		assertTrue(a == 0);
	}
	
//	 @Test
	public void testCANCEL_ORDER () throws Exception {
		// dat order
		BaseEvent eventSell = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 3, 2, 0);
		processEvent(eventSell);

		assertTrueBalance(eventSell.userId, BTC, USD, 998, 2, 1000, 0);
		
		BaseEvent cancelSell = createEventCancel(1, eventSell.orderId, BTCUSD);
		processEvent(cancelSell);
		assertTrueBalance(eventSell.userId, BTC, USD, 1000, 0, 1000, 0);
	}

	@Test
	public void mainTest() throws Exception {
		testBUGReportFee();

		assertTrue(true);
	}

	public void testPlaceOCO_1() {
//		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0068, 1, 0);
//		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.0068, 1, 0);
		BaseEvent event3 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0066, 1, 0.0080);
		event3.tradeOption = TradeOption.OCO;
//		processEvent(event1, event2);
		processEvent(event3);
	}
	
	public void testBUGReportFee () throws Exception {
		BaseEvent event01 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 0.0002079,10.14, 0);
		BaseEvent event02 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 0.0002080,6.55, 0);
		BaseEvent event03 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 0.0002082,5.54, 0);
		processEvent(event01, event02, event03);
		
		BaseEvent event04 = createEventOrder(2, BTCUSD, OrderType.MARKET, OrderSide.BID, 0,10.14, 0);
		processEvent(event04);
		BaseEvent event05 = createEventOrder(2, BTCUSD, OrderType.MARKET, OrderSide.BID, 0,3.21, 0);
		processEvent(event05);
		BaseEvent event06 = createEventOrder(2, BTCUSD, OrderType.MARKET, OrderSide.BID, 0,10.24, 0);
		processEvent(event06);
		
		BaseEvent event07 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 0.0002073, 4.87, 0);
		BaseEvent event08 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 0.0002075, 12.54, 0);
		processEvent(event07, event08);
		BaseEvent event09 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 0.0002073, 2.67, 0);
		processEvent(event09);
	}

	public void testBUG462_STOP_LIMIT () throws Exception {
		System.out.println("BalanceTest.testPLACE_ORDER9()");
		// tao balance trc
		BaseEvent event01 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 154.87, 0.1, 0);
		BaseEvent event02 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 154.87, 0.1, 0);
		processEvent(event01);
		processEvent(event02);
		
		// 3
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.STOP_LIMIT, OrderSide.BID, 155.01, 0.64645, 154.96);
		
		// 4
		BaseEvent event2 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.BID, 154.97, 0.12222, 0);
		// 5
		BaseEvent event3 = createEventOrder(4, BTCUSD, OrderType.LIMIT, OrderSide.BID, 154.98, 0.13333, 0);
		// 6
		BaseEvent event4 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 154.96, 0.51255, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		processEvent(event4);
		
		// 4 <> 5
		// 4 <> 3
//		BaseEvent cancelSell = createEventCancel(3, event3.orderId, BTCUSD);
//		processEvent(cancelSell);
	}
	public void testBUG462_STOP_LIMIT_2 () throws Exception {
		System.out.println("BalanceTest.testPLACE_ORDER9()");
		// tao balance trc
		BaseEvent event01 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.BID, 4.0, 0.1, 0);
		BaseEvent event02 = createEventOrder(1, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 4.0, 0.1, 0);
		processEvent(event01);
		processEvent(event02);
		
		// 3
		BaseEvent event1 = createEventOrder(1, BTCUSD, OrderType.STOP_LIMIT, OrderSide.BID, 4.3795, 0.5, 4.3792);
		
		// 4
		BaseEvent event2 = createEventOrder(3, BTCUSD, OrderType.LIMIT, OrderSide.BID, 4.3793, 0.1, 0);
		// 5
		BaseEvent event3 = createEventOrder(4, BTCUSD, OrderType.LIMIT, OrderSide.BID, 4.3792, 0.1, 0);
		// 6
		BaseEvent event4 = createEventOrder(2, BTCUSD, OrderType.LIMIT, OrderSide.ASK, 4.3792, 0.5, 0);
		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		System.out.println("--------------------------");
		processEvent(event4);
		
		// 4 <> 5
		// 4 <> 3
//		BaseEvent cancelSell = createEventCancel(3, event3.orderId, BTCUSD);
//		processEvent(cancelSell);
	}
	
	public void testFixbugMatching2Broker() {
		BaseEvent lastA = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 147.09, 1, 0);
		BaseEvent lastB = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 147.09, 1, 0);
		processEvent(lastA, lastB);
	}
	public void testFixbug449OCO() {
		BaseEvent lastB = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 147.09, 1, 0);
		BaseEvent lastS = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 147.09, 1, 0);
		processEvent(lastB, lastS);
		
		BaseEvent event2 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.BID, 146.98, 0.21679, 148.45);
		event2.tradeOption = TradeOption.OCO;
		processEvent(event2);

//		BaseEvent event3 = createEventOrder(2, ETHUSD, OrderType.IMMEDIATE_OR_CANCEL, OrderSide.ASK, 146.98, 0.1, 0);
//		processEvent(event3);
		BaseEvent event3 = createEventOrder(2, ETHUSD, OrderType.FILL_OR_KILL, OrderSide.ASK, 146.98, 0.1, 0);
		processEvent(event3);
		
	}
	public void testFixbug448OCO() {
		BaseEvent lastB = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 147.09, 1, 0);
		BaseEvent lastS = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 147.09, 1, 0);
		processEvent(lastB, lastS);
		
		BaseEvent event2 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.BID, 146.98, 0.21679, 148.45);
		event2.tradeOption = TradeOption.OCO;
		processEvent(event2);
		
		BaseEvent cancel1 = createEventCancel(2, 3, ETHUSD);
		processEvent(cancel1);
		BaseEvent cancel2 = createEventCancel(2, 4, ETHUSD);
		processEvent(cancel2);
		
	}

	public void testFixbugHangVTBalance() {
		BaseEvent balance1B = createEventChangeBalance(10, BTC, (long) (10 * currencyDecimalPow[BTC]));
		processEvent(balance1B);
		BaseEvent balance1E = createEventChangeBalance(10, ETH, (long) (100 * currencyDecimalPow[ETH]));
		processEvent(balance1E);
		
		BaseEvent balance2B = createEventChangeBalance(11, BTC, (long) (10 * currencyDecimalPow[BTC]));
		processEvent(balance2B);
		BaseEvent balance2E = createEventChangeBalance(11, ETH, (long) (100 * currencyDecimalPow[ETH]));
		processEvent(balance2E);
		
		BaseEvent event11 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017124, 2.354, 0);
		processEvent(event11);
		
		BaseEvent event12 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017583, 1.537, 0);
		processEvent(event12);
		
		BaseEvent event13 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017972, 1.972, 0);
		processEvent(event13);
		
		BaseEvent event14 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.018423, 1.707, 0);
		processEvent(event14);
		
		BaseEvent event21 = createEventOrder(11, ETHBTC, OrderType.IMMEDIATE_OR_CANCEL, OrderSide.BID, 0.018423, 7.570, 0);
		processEvent(event21);

		System.out.println("----------------------------------");
	}

	public void testFixbugAnhDTBalance() {
		BaseEvent balance1B = createEventChangeBalance(10, BTC, (long) (10 * currencyDecimalPow[BTC]));
		processEvent(balance1B);
		BaseEvent balance1E = createEventChangeBalance(10, ETH, (long) (100 * currencyDecimalPow[ETH]));
		processEvent(balance1E);

		BaseEvent balance2B = createEventChangeBalance(11, BTC, (long) (10 * currencyDecimalPow[BTC]));
		processEvent(balance2B);
		BaseEvent balance2E = createEventChangeBalance(11, ETH, (long) (100 * currencyDecimalPow[ETH]));
		processEvent(balance2E);
		
		BaseEvent event11 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017124, 2.354, 0);
		processEvent(event11);
		
		BaseEvent event12 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017583, 1.537, 0);
		processEvent(event12);
		
		BaseEvent event13 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017972, 1.972, 0);
		processEvent(event13);
		
		BaseEvent event14 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.018423, 2.103, 0);
		processEvent(event14);

		BaseEvent event21 = createEventOrder(11, ETHBTC, OrderType.IMMEDIATE_OR_CANCEL, OrderSide.BID, 0.018423, 7.570, 0);
		processEvent(event21);

		System.out.println("----------------------------------");
		BaseEvent event15 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.018723, 2.103, 0);
		processEvent(event15);
		
		BaseEvent event22 = createEventOrder(11, ETHBTC, OrderType.IMMEDIATE_OR_CANCEL, OrderSide.BID, 0.018723, 3.000, 0);
		processEvent(event22);
		
	}
	public void testFixbug442() {
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 148.54, 1, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 148.54, 1, 0);
		processEvent(event1);
		processEvent(event2);
		
		BaseEvent event3 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 149.36, 0.12546, 0);
		processEvent(event3);
		BaseEvent event4 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 150.46, 0.64312, 0);
		processEvent(event4);
		BaseEvent event5 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.15, 0.73345, 0);
		processEvent(event5);
		
		BaseEvent event6 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.BID, 147.21, 1.48464, 149.36);
		event6.tradeOption = TradeOption.OCO;
		processEvent(event6);
		
		BaseEvent event7 = createEventOrder(3, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 149.36, 0.31234, 0);
		processEvent(event7);
		
		
		
	}
	public void testFixbug421() {
		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.000100, 3.4, 0);
		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.000100, 1.1, 0);
		processEvent(event1);
		processEvent(event2);
	}
	public void testFixbug336() {
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 144.12, 0.1, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 144.12, 0.1, 0);
		processEvent(event1, event2);

		BaseEvent event3 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 143.04, 0.5, 144.85);
		event3.tradeOption = TradeOption.OCO;
		processEvent(event3);

		BaseEvent event4 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 143.04, 0.6, 0);
		processEvent(event4);
		
	}

	public void testFixbug420() {
		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.000100, 3.264, 0);
		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.000100, 3.264, 0);
		processEvent(event1, event2);
		BaseEvent event3 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.000100, 3.264, 0);
		event3.tradeOption = TradeOption.OCO;
		processEvent(event3);
		BaseEvent cancel = createEventCancel(1, event3.orderId, ETHBTC);
		processEvent(cancel);
	}

	public void testFixbug418() {
		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.000100, 3.264, 0);
		processEvent(event1);
		
//		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.IMMEDIATE_OR_CANCEL, OrderSide.BID, 0.000100, 20, 0);
		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.MARKET, OrderSide.BID, 0.000100, 20, 0);
		processEvent(event2);
	}

	public void testFixbug300() {
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 150.87, 0.54464, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.06, 1.12678, 0);
		processEvent(event1, event2);
		BaseEvent event3 = createEventOrder(2, ETHUSD, OrderType.MARKET, OrderSide.BID, 0, 3, 0);
		processEvent(event3);
	}
	public void testFixbug396() {
		BaseEvent balance1 = createEventChangeBalance(10, ETH, (long) (104.84973842 * currencyDecimalPow[ETH]));
		BaseEvent balance2 = createEventChangeBalance(10, USD, (long) (12875.82673285 * currencyDecimalPow[USD]));
		
		processEvent(balance1, balance2);
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 150.87, 0.54464, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.06, 1.12678, 0);
		processEvent(event1, event2);
		
		
		
		BaseEvent event3 = createEventOrder(10, ETHUSD, OrderType.LIMIT, OrderSide.BID, 152.06, 1.67142, 0);
		processEvent(event3);
	}

	public void testFixbug341Cancel() {
		BaseEvent balance1 = createEventChangeBalance(10, BTC, (long) (10.102336799 * currencyDecimalPow[BTC]));
		processEvent(balance1);
		BaseEvent balance2 = createEventChangeBalance(10, ETH, (long) (94.21853600 * currencyDecimalPow[ETH]));
		processEvent(balance2);
		BaseEvent event1 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.017125, 2.326, 0);
		processEvent(event1);
		
		BaseEvent cancelLimit = createEventCancel(10, event1.orderId, ETHBTC);
		processEvent(cancelLimit);
	}

	public void testFixbug341() {
		long balanceBtc1 = (long) Math.floor(10 * currencyDecimalPow[BTC]);
		BaseEvent balance1 = createEventChangeBalance(10, BTC, balanceBtc1);
		
		long balanceEth1 = (long) Math.floor(100 * currencyDecimalPow[ETH]);
		BaseEvent balance2 = createEventChangeBalance(10, ETH, balanceEth1);
		
		long balanceBtc2 = (long) Math.floor(10 * currencyDecimalPow[BTC]);
		BaseEvent balance3 = createEventChangeBalance(11, BTC, balanceBtc2);
		
		long balanceEth2 = (long) Math.floor(100 * currencyDecimalPow[ETH]);
		BaseEvent balance4 = createEventChangeBalance(11, ETH, balanceEth2);
		
		processEvent(balance1, balance2, balance3, balance4);
		
		// last rate
		BaseEvent eventa = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.01712, 1, 0);
		BaseEvent eventb = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.01712, 1, 0);
		processEvent(eventa, eventb);
		
		BaseEvent event1 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017125, 2.326, 0);
		processEvent(event1);
		BaseEvent event2 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.017975, 1.978, 0);
		processEvent(event2);
		BaseEvent event3 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.018236, 0.972, 0);
		processEvent(event3);
		BaseEvent event4 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.018532, 1.236, 0);
		processEvent(event4);
		
		BaseEvent event5 = createEventOrder(11, ETHBTC, OrderType.IMMEDIATE_OR_CANCEL, OrderSide.BID, 0.018532, 5.78, 0);
		processEvent(event5);
	}

	public void testFixbug324() {
		BaseEvent balance1 = createEventChangeBalance(10, ETH, (long) Math.floor(99.99546200 * currencyDecimalPow[ETH]));
		BaseEvent balance2 = createEventChangeBalance(10, BTC, (long) Math.floor(9.99995703 * currencyDecimalPow[BTC]));
		BaseEvent balance3 = createEventChangeBalance(11, ETH, (long) Math.floor(99.99900000 * currencyDecimalPow[ETH]));
		BaseEvent balance4 = createEventChangeBalance(11, BTC, (long) Math.floor(9.99996213 * currencyDecimalPow[BTC]));
		
		processEvent(balance1, balance2, balance3, balance4);
		
		BaseEvent event1 = createEventOrder(10, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.019235, 2.3556, 0);
		BaseEvent event2 = createEventOrder(11, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.019352, 2.3556, 0);
		processEvent(event1, event2);
		
	}
	public void testFixbug288() {
		BaseEvent lastB = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 148.5464, 1, 0);
		BaseEvent lastS = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 148.5464, 1, 0);
		processEvent(lastB, lastS);
		
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 149.369, 1, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 150.4646, 0.64312313, 0);
		BaseEvent event3 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.1578, 0.73345645, 0);
		processEvent(event1, event2, event3);
		
		BaseEvent event4 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 147.2137, 1.48464564, 149.3690);
		event4.tradeOption = TradeOption.OCO;
		processEvent(event4);
		BaseEvent event5 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 149.369, 0.13464564, 0);
		processEvent(event5);
		
	}
	public void testFixbug315() {
		BaseEvent lastB = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 161.0679, 1, 0);
		BaseEvent lastS = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 161.0679, 1, 0);
		processEvent(lastB, lastS);
		
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 161.2546, 0.1713, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 158.0664, 0.4564, 0);
		processEvent(event1, event2);
		
		BaseEvent event3 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 161.2546, 0.2577, 157.0134);
		event3.tradeOption = TradeOption.OCO;
		processEvent(event3);
		
	}
	public void testPlaceOCO() {
		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0068, 1, 0);
		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.0068, 1, 0);
		BaseEvent event3 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0066, 1, 0.0080);
		event3.tradeOption = TradeOption.OCO;
		processEvent(event1, event2);
		processEvent(event3);
	}
	public void test185() {
		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0069, 1, 0);
		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0068, 1, 0);
		BaseEvent event3 = createEventOrder(1, ETHBTC, OrderType.MARKET, OrderSide.ASK, 0, 1.5, 0);
		processEvent(event1, event2);
		processEvent(event3);
	}
	public void test286() {
		BaseEvent event1 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.BID, 152.2134, 1, 0);
		BaseEvent event2 = createEventOrder(2, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.2134, 1, 0);
		
		BaseEvent eventOcoBuy = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 150, 70.462406, 163.1478);
		eventOcoBuy.tradeOption = TradeOption.OCO;
		
		processEvent(event1);
		processEvent(event2);
		processEvent(eventOcoBuy);
	}
	public void test271() {
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 168.4656, 0.12366456, 185.2316);
		event1.tradeOption = TradeOption.OCO;
		
		processEvent(event1);
		
		BaseEvent cancelLimit = createEventCancel(1, event1.orderId, ETHUSD);
		BaseEvent cancelStop = createEventCancel(1, event1.next.orderId, ETHUSD);
		processEvent(cancelLimit, cancelStop);
	}
	public void testCancelFillOrKill() {
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 151.0134, 0.46646464, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 151.2453, 0.12546645, 0);
		BaseEvent event3 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 151.9979, 0.73454679, 0);
		BaseEvent event4 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.2134, 0.59193109, 0);
		processEvent(event1, event2, event3, event4);
		BaseEvent event5 = createEventOrder(1, ETHUSD, OrderType.FILL_OR_KILL, OrderSide.BID, 151.2453, 0.59193109, 0);
		processEvent(event5);
	}
	
	public void testCancelOCO() {
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 152.1254, 1, 0);
		BaseEvent event2 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 152.1254, 1, 0);
		
		BaseEvent eventOcoBuy = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 150.4566, 1.04567979, 153.0646);
		eventOcoBuy.tradeOption = TradeOption.OCO;
		
		processEvent(event1);
		processEvent(event2);
		processEvent(eventOcoBuy);
		
		BaseEvent cancelLimit = createEventCancel(1, eventOcoBuy.orderId, ETHUSD);
		BaseEvent cancelStop = createEventCancel(1, eventOcoBuy.next.orderId, ETHUSD);
		processEvent(cancelLimit);
		processEvent(cancelStop);
	}
	public void testBuyOCO() {
		
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.BID, 150, 70.462406, 163.1478);
		event1.tradeOption = TradeOption.OCO;
		processEvent(event1);
		
	}
	public void testChangeBalance() {
		BaseEvent event1 = createEventWithdraw(1, BTC, -1000, MoneyTransaction.REQUEST_WITHDRAW);
		processEvent(event1);
		
	}
	public void testBug245() {
//		BaseEvent event1 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 11000,  0.70844317, 0);
		BaseEvent event2 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 11000, 0.06536801, 0);
		BaseEvent event3 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 9800,  0.10000000, 0);
		BaseEvent event4 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 9600,   0.02000000, 0);
		BaseEvent event5 = createEventOrder(1, ETHBTC, OrderType.MARKET, OrderSide.BID, 0, 0.88035664, 0);
//		processEvent(event1);
		processEvent(event2);
		processEvent(event3);
		processEvent(event4);
		processEvent(event5);
	}
	public void testFee() {
		BaseEvent event21 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 0.0048, 2.33265423, 0);
		BaseEvent event22 = createEventOrder(2, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 0.0048,  2.33265423, 0);
		processEvent(event21);
		processEvent(event22);
	}
	public void testNegativeBalance() {
		BaseEvent event21 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.BID, 5.2106, 1, 0);
		BaseEvent event22 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 5.2106,  0.3, 0);
		BaseEvent event23 = createEventOrder(1, ETHBTC, OrderType.LIMIT, OrderSide.ASK, 5.2106,  0.3, 0);
		processEvent(event21);
		processEvent(event22);
		processEvent(event23);
		
		BaseEvent cancelBuy = createEventCancel(1, event21.orderId, BTCUSD);
		processEvent(cancelBuy);
		
	}
	public void testMarketBuy() {
		BaseEvent event21 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 185.89,  0.614564, 0);
		BaseEvent event22 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 185.91,  0.815478, 0);
		BaseEvent event23 = createEventOrder(1, ETHUSD, OrderType.LIMIT, OrderSide.ASK, 185.96,  0.154614, 0);
		processEvent(event21);
		processEvent(event22);
		processEvent(event23);
		
		BaseEvent marketBuy = createEventOrder(1, ETHUSD, OrderType.MARKET, OrderSide.BID, 0,  1.584656, 0);
		processEvent(marketBuy);
		showBalance(1);
//		showBalance(2);
	}

	public void testMarket() {
		long[] ids = new long[] {2, 3, 4, 5, 6};
		Random random = new Random();
		int count = 100;
		double sumAmt = 0;
		for (int i = 0; i < count; i++) {
			for (long id : ids) {
				double price = (500d + random.nextInt(100)) / 100;
				double amount = (1000000d + random.nextInt(1000000)) / 1000000;
				System.out.println("---------------------" + price + "   " + amount);
				sumAmt += amount;
				BaseEvent event = createEventOrder(id, ETHUSD, OrderType.LIMIT, OrderSide.ASK, price, amount, 0);
				processEvent(event);
			}
		}
		
//		double priceMk = sumAmt + ((double) random.nextInt(10) + ((1d + random.nextInt(10)) / 10d)) * ((1000d + random.nextInt(1000)) / 1000);
		double priceMk = sumAmt + 100;
		BaseEvent event1 = createEventOrder(1, ETHUSD, OrderType.MARKET, OrderSide.BID, 0, priceMk, 0);
		processEvent(event1);
		
		showBalance(1);
		for (long id : ids) {
			showBalance(id);
		}
	}
	// @Test
	public void testREPLAY_EVENTS () throws Exception {
		int a = 1;
		assertTrue(a == 1);
	}

	private void showBalance(long userId) {
		System.out.println(userId + " " + balanceService.getBalance(userId).toString());
	}
	private void processEvent(BaseEvent event) {
		try {
			balanceService.check(event, 1, true);
			matchingEngine.handleEvent(event);
			balanceService.processBalance(event, 1, true);

			ordersConsumer.onEvent(event, 1, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void processEvent(BaseEvent ...events) {
		for (BaseEvent event : events) {
			try {
				balanceService.check(event, 1, true);
				matchingEngine.handleEvent(event);
				balanceService.processBalance(event, 1, true);
				
				ordersConsumer.onEvent(event, 1, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private BaseEvent createEventChangeBalance(long userId, int currency, long amount) {
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.CHANGE_BALANCE;
		event.userId = userId;
		event.symbol = currency;
		event.amount = amount;
		event.timestamp = System.currentTimeMillis();
		return event;
	}
	private BaseEvent createEventWithdraw(long userId, int currency, long amount, byte transactionType) {
		BaseEvent event = new BaseEvent();
		event.eventType = EventType.CHANGE_BALANCE;
		event.userId = userId;
		event.symbol = currency;
		event.amount = amount;
		event.orderSide = transactionType;
		return event;
	}

	private BaseEvent createEventOrder(long userId, int symbol, byte orderType, byte orderSide, double price, double amount, double stopPrice) {
		BaseEvent event = new BaseEvent();
		int base = BitUtil.getHigh(symbol);
		
		event.timestamp = System.currentTimeMillis();
		event.eventType = EventType.PLACE_ORDER;
		event.userId = userId;
		event.symbol = symbol;
		event.orderType = orderType;
		event.orderSide = orderSide;
		event.price = Math.round(price * priceDecimalPow[symbol]);
		event.amount = Math.round(amount * currencyDecimalPow[base]);
		event.stopPrice = Math.round(stopPrice * priceDecimalPow[symbol]);
		return event;
	}

	private BaseEvent createEventCancel(long userId, long orderId, int symbol) {
		BaseEvent event = new BaseEvent();
		event.timestamp = System.currentTimeMillis();
		event.eventType = EventType.CANCEL_ORDER;
		event.userId = userId;
		event.orderId = orderId;
		event.symbol = symbol;
		
		return event;
	}

	private void initSymbolAndCurrency() throws Exception {
		int symbolLen = symbols.length;
		for (int i = 0; i < symbolLen; i++) {
			BaseEvent event = new BaseEvent();
			event.eventType = EventType.ADD_SYMBOL;
			event.symbol = symbols[i];
			event.price = currency[i][0];
			event.amount = currency[i][1];

			int base = BitUtil.getHigh(event.symbol);
			int counter = BitUtil.getLow(event.symbol);
			event.orderSide = currencyDecimal[base];
			event.orderType = currencyDecimal[counter];
			event.tradeType = priceDecimal[event.symbol];
			processEvent(event);
		}
	}

	private void clearUser() {
		
	}
	private void clearSymbolAndCurrency() {
	}

	private static SymbolConfig[] getConfigArr() {
		//TODO: Read symbol config from persistence store	
		SymbolConfig config = new SymbolConfig();
		config.symbol = 1;
		config.maxPriceLevel = 1 << 12;
		
		SymbolConfig[] configArr = new SymbolConfig[1];
		configArr[0] = config;
		
		return configArr;
	}

	private void assertTrueBalance(long userId, int base, int counter, long balanceBase, long reserveBase, long balanceCounter, long reserveCounter) {
		UserBalance userBalance = balanceService.getBalance(userId);
		int indexBase = userBalance.findIndexBalance(base);
		assertTrue(userBalance != null
				&& userBalance.balance[indexBase + 1] == balanceBase * currencyDecimalPow[base]
				&& userBalance.balance[indexBase + 2] == reserveBase * currencyDecimalPow[base]
				);
		int indexUsdt = userBalance.findIndexBalance(counter);
		assertTrue(userBalance != null
				&& userBalance.balance[indexUsdt + 1] == balanceCounter * currencyDecimalPow[counter]
				&& userBalance.balance[indexUsdt + 2] == reserveCounter * currencyDecimalPow[counter]
				);
	}
}
