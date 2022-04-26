package org.ssi.matching;


import org.junit.Test;
import org.ssi.collection.ConcurrentObjectPool;
import org.ssi.collection.ObjectPool;
import org.ssi.collection.TwoWayLongArray;
import org.ssi.event.TradeEventManager;
import org.ssi.model.*;
import org.ssi.type.MutualLong;
import org.ssi.util.SearchUtil;

import static org.junit.Assert.assertTrue;


public class OrderBookTest {
	
	private OrderMap oMap;
	private MutualLong oId;
	private ObjectPool<Order> oPool;
	private ObjectPool<OrderBucket> bPool;
	private ConcurrentObjectPool<BaseEvent> ePool;
	private OrderBook ob;
	
	public OrderBookTest() {
		oMap = new OrderMap(1 << 4);
		oId = new MutualLong(0);
		
		oPool = new ObjectPool<Order>(1 << 3, Order::new);
		oPool.fillPool(1 << 3);
		
		bPool = new ObjectPool<OrderBucket>(1 << 3, OrderBucket::new);
		bPool.fillPool(1 << 3);
		
		ePool = new ConcurrentObjectPool<BaseEvent>(1 << 3, BaseEvent::new);
		ePool.fillPool(1 << 3);
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, null);
		ob.tradeDecimals(0, 0, 0);
	}

	@Test
	public void testNewAskLimitOrder() throws Exception {
		
		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.askLadder().size() == 3 && ob.bidLadder().size() == 0);
		assertTrue(ob.askLadder().size() == 3 && ob.bidLadder().size() == 0);
		assertTrue(oMap.size() == 4);
		assertTrue(!ob.isMatchingPrice(OrderSide.ASK, 996));
		assertTrue(ob.isMatchingPrice(OrderSide.BID, 996));
		
		OrderBucket bucket = ob.askLadder().bucketMap().get(999);
		assertTrue(bucket.length == 2 && bucket.amount == 1000);
		
		TwoWayLongArray priceLevel = ob.askLadder().priceArr();
		assertTrue(priceLevel.at(0) == 996 && priceLevel.at(1) == 998 && priceLevel.at(2) == 999);
				
		assertTrue(oPool.size() == 4 && bPool.size() == 5);
	}
	
	@Test
	public void testNewBidLimitOrder() throws Exception {
		
		BaseEvent event = createTradeEvent(1, 999, 999, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(2, 999, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(3, 998, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(4, 996, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.bidLadder().size() == 3 && ob.askLadder().size() == 0);
		assertTrue(ob.bidLadder().size() == 3 && ob.askLadder().size() == 0);
		assertTrue(ob.isMatchingPrice(OrderSide.ASK, 996));
		assertTrue(!ob.isMatchingPrice(OrderSide.BID, 996));
		
		OrderBucket bucket = ob.bidLadder().getBucket(999);
		assertTrue(bucket.length == 2 && bucket.amount == 1000);
		
		TwoWayLongArray priceLevel = ob.bidLadder().priceArr();
		assertTrue(priceLevel.at(0) == 999 && priceLevel.at(1) == 998 && priceLevel.at(2) == 996);
				
		assertTrue(oPool.size() == 4 && bPool.size() == 5);
	}
	
	@Test
	public void testMatchBidLimitOrder() throws Exception {
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);
		
		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);

		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);

		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);

		event = createTradeEvent(0, 996, 10, OrderSide.ASK, OrderType.LIMIT);
		event.tradeOption = TradeOption.HIDDEN;
		ob.placeOrder(event, true);
		
		/*
		 * 		order book [Ask] :  Price -> (filled - amount)
		 * 			999 -> ( 0 - 999, 0 - 1)
		 * 			998 -> 0 - 1
		 * 			996 -> 0 - 10
		 */
		
		event = createTradeEvent(0, 998, 2, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		after match [Ask]
		 * 			999 -> (0 - 999, 0 - 1)
		 * 			998 -> 0 - 1
		 * 			996 -> 2 - 10			
		 */
		
		assertTrue(event.orderId == 5);
		assertTrue(event.matchingResult.makerOrderId == 4 && event.matchingResult.price == 996 
				&& event.matchingResult.makerFilled == 2 && event.matchingResult.makerTradeOption == TradeOption.HIDDEN);
		
		assertTrue(ob.askLadder().size() == 3 && ob.bidLadder().size() == 0);
		assertTrue(oMap.size() == 4);
		
		event = createTradeEvent(0, 998, 20, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		after match [Ask]
		 * 			999 -> (0 - 999, 0 - 1)
		 * 			//remove 998 -> 1 - 1
		 * 			//remove 996 -> 10 - 10
		 * 
		 * 		[Bid]
		 * 		998 -> (9, 20)			
		 */
		
		assertTrue(ob.askLadder().size() == 1 && ob.bidLadder().size() == 1);
		assertTrue(event.matchingResult.makerOrderId == 4 && event.matchingResult.price == 996 
				&& event.matchingResult.makerFilled == 10 && event.matchingResult.amount == 8 && event.matchingResult.makerTradeOption == TradeOption.HIDDEN
				&& event.matchingResult.next.makerOrderId == 3 && event.matchingResult.next.price == 998 
				&& event.matchingResult.next.amount == 1 && event.matchingResult.next.makerFilled == 1);

				
		assertTrue(ob.bidLadder().priceArr().at(0) == 998);
		
		OrderBucket bidBucket = ob.bidLadder().getBucket(998);
		assertTrue(bidBucket.head.amount == 20 && bidBucket.head.filled == 9);	
		
		assertTrue(oMap.size() == 3);
	}
	
	@Test
	public void testMatchAskLimitOrder() throws Exception {
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);
	
		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 10, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		order book [Bid] :  Price -> (filled - amount)
		 * 			999 -> ( 0 - 999, 0 - 1)
		 * 			998 -> 0 - 1
		 * 			996 -> 0 - 10
		 */
		
		event = createTradeEvent(0, 997, 2, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		after match [Bid]
		 * 			999 -> (2 - 999, 0 - 1)
		 * 			998 -> 0 - 1
		 * 			996 -> 0 - 10			
		 */
		
		assertTrue(oMap.size() == 4);		
		assertTrue(ob.bidLadder().size() == 3 && ob.askLadder().size() == 0);
		
		event = createTradeEvent(0, 997, 1000, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		after match [Bid]
		 * 			//removed 999 -> (999 - 999, 1 - 1)
		 * 			//remove 998 -> 1 - 1
		 * 			//remove 996 -> 10 - 10
		 * 		[Ask]
		 * 			997 -> (999 - 1000)		
		 */
		
		long orderId = event.orderId;
		
		assertTrue(ob.bidLadder().size() == 1 && ob.askLadder().size() == 1);				
		assertTrue(ob.askLadder().priceArr().at(0) == 997);
		
		OrderBucket bidBucket = ob.askLadder().getBucket(997);
		assertTrue(bidBucket.head.amount == 1000 && bidBucket.head.filled == 999);		
		assertTrue(oMap.size() == 2);
		
		event = createTradeEvent(orderId, 0, 0, OrderSide.ASK, OrderType.LIMIT);
		ob.cancelOrder(event);
		
		assertTrue(event.matchingResult != null && event.matchingResult.amount == 1 
				&& event.matchingResult.makerFilled == 999 && event.matchingResult.makerFilledCost == 998000); //999 * 998 + 998
		assertTrue(ob.askLadder().size() == 0);
	}
	
	@Test
	public void testMatchBidMarketOrder() throws Exception {
		
		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 10, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		order book [Ask] :  Price -> (filled - amount)
		 * 			999 -> ( 0 - 999, 0 - 1)
		 * 			998 -> 0 - 1
		 * 			996 -> 0 - 10
		 */
		
		event = createTradeEvent(0, 997, 20, OrderSide.BID, OrderType.MARKET);
		event.price = 999 * 10000;
		ob.placeOrder(event, true);		
		
		/*
		 * 		after match [Ask]
		 * 			999 -> (9 - 999, 0 - 1)
		 * 		//remove	998 -> 1 - 1
		 * 		//remove	996 -> 10 - 10			
		 */
		
		assertTrue(ob.askLadder().size() == 1 && ob.bidLadder().size() == 0);		
		OrderBucket bucket = ob.askLadder().getBucket(999);
		assertTrue(bucket.amount == 991 && bucket.length == 2);		
		assertTrue(oMap.size() == 2);
		
		event = createTradeEvent(0, 999, 20, OrderSide.BID, OrderType.MARKET);
		event.price = 999 * 3;
		ob.placeOrder(event, true);
		
		/*
		 * 		after match [Ask]
		 * 			999 -> (12 - 999, 0 - 1)
		 * 
		 */
		
		bucket = ob.askLadder().getBucket(999);
		assertTrue(bucket.head.filled == 12 && bucket.head.amount == 999);		
		assertTrue(oMap.size() == 2);		
	}
	
	@Test
	public void testMatchBidMarketOrderWithTradeDigit() throws Exception {
		
		ob.tradeDecimals(2, 0, 0);
		assertTrue((long)ob.symbolScale() == 100);
		
		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 10, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		/*
		 * 		order book [Ask] :  Price -> (filled - amount)
		 * 			999 -> ( 0 - 999, 0 - 1)
		 * 			998 -> 0 - 1
		 * 			996 -> 0 - 10
		 */
		
		//place market bid order with amount of 20
		event = createTradeEvent(0, 997, 20, OrderSide.BID, OrderType.MARKET);
		event.price = (long)((20 / ob.symbolScale()) * 1000);
		ob.placeOrder(event, true);		
		
		/*
		 * 		after match [Ask]
		 * 			999 -> (9 - 999, 0 - 1)
		 * 		//remove	998 -> 1 - 1
		 * 		//remove	996 -> 10 - 10			
		 */
		
		assertTrue(ob.askLadder().size() == 1 && ob.bidLadder().size() == 0);		
		OrderBucket bucket = ob.askLadder().getBucket(999);
		assertTrue(bucket.amount == 991 && bucket.length == 2);		
		assertTrue(oMap.size() == 2);
		
		event = createTradeEvent(0, 999, 20, OrderSide.BID, OrderType.MARKET);
		event.price = (long)((3 / ob.symbolScale()) * 1000);
		ob.placeOrder(event, true);
		
		/*
		 * 		after match [Ask]
		 * 			999 -> (12 - 999, 0 - 1)
		 * 
		 */
		
		bucket = ob.askLadder().getBucket(999);
		assertTrue(bucket.head.filled == 12 && bucket.head.amount == 999);		
		assertTrue(oMap.size() == 2);		
	}
	
	@Test
	public void testMatchAskMarketOrder() throws Exception {
		
		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 10, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 997, 1002, OrderSide.ASK, OrderType.MARKET);
		ob.placeOrder(event, true);
		
		assertTrue(ob.bidLadder().size() == 1 && ob.askLadder().size() == 0);
		
		OrderBucket bucket = ob.bidLadder().getBucket(996);
		assertTrue(bucket.amount == 9 && bucket.length == 1);
		
		assertTrue(oMap.size() == 1);
	}
	
	@Test
	public void testMatchAskMarketOrderUncompleted() throws Exception {
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);
		
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
		event.userId = 1;
		ob.placeOrder(event, true);		
		event = createTradeEvent(0, 998, 1, OrderSide.BID, OrderType.LIMIT);
		event.userId = 2;
		ob.placeOrder(event, true);		
		
		event = createTradeEvent(0, 997, 4, OrderSide.ASK, OrderType.MARKET);
		ob.placeOrder(event, true);
		
		assertTrue(ob.bidLadder().size() == 0 && ob.askLadder().size() == 0);		
		assertTrue(event.matchingResult.amount == 1 && event.matchingResult.makerId == 1 
				&& event.matchingResult.next.amount == 1 && event.matchingResult.next.makerId == 2
				&& event.matchingResult.next.next.amount == 2 && event.matchingResult.next.next.next == null);
	}


	//IOC completed
    @Test
    public void testMatchAskIOCompleted() throws Exception {
        ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
        ob.tradeDecimals(0, 0, 0);

        BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
        event.userId = 1;
        ob.placeOrder(event, true);
        event = createTradeEvent(0, 998, 1, OrderSide.BID, OrderType.LIMIT);
        event.userId = 2;
        ob.placeOrder(event, true);

        event = createTradeEvent(0, 997, 1, OrderSide.BID, OrderType.LIMIT);
        event.userId = 3;
        ob.placeOrder(event, true);


        event = createTradeEvent(0, 998, 2, OrderSide.ASK, OrderType.IMMEDIATE_OR_CANCEL);
        ob.placeOrder(event, true);

//        System.out.println(ob.bidLadder().size() + " " + ob.askLadder().size());
//        System.out.println(event.matchingResult.toString());


        assertTrue(ob.bidLadder().size() == 1);
        assertTrue( ob.askLadder().size() == 0);
//

        //check filled 1
        assertTrue( event.matchingResult.amount == 1
                && event.matchingResult.makerId == 1
                && event.matchingResult.type == MatchingType.TRADE);

        //check filled 2
        assertTrue( event.matchingResult.next.amount == 1
                && event.matchingResult.next.makerId == 2
                && event.matchingResult.next.type == MatchingType.TRADE);

        assertTrue(event.matchingResult.next.next == null);

    }



    //ICO uncompleted
	@Test
	public void testMatchAskIOCUncompleted() throws Exception {
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);

		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
		event.userId = 1;
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 998, 1, OrderSide.BID, OrderType.LIMIT);
		event.userId = 2;
		ob.placeOrder(event, true);

		event = createTradeEvent(0, 999, 4, OrderSide.ASK, OrderType.IMMEDIATE_OR_CANCEL);
		ob.placeOrder(event, true);

//		System.out.println(ob.bidLadder().size() + " " + ob.askLadder().size());
//		System.out.println(event.matchingResult.toString());


		assertTrue(ob.bidLadder().size() == 1 && ob.askLadder().size() == 0);
//

		//check filled 1
		assertTrue( event.matchingResult.amount == 1
				&& event.matchingResult.makerId == 1
				&& event.matchingResult.type == MatchingType.TRADE);

		assertTrue( event.matchingResult.next.type == MatchingType.CANCEL
				&& event.matchingResult.next.amount == 3
				&& event.matchingResult.next.price == 0);

		assertTrue(event.matchingResult.next.next == null);

	}

	@Test
	public void testCancelOrder() throws Exception {

		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 995, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 994, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 993, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(oPool.size() == 1 && bPool.size() == 2);

		event = new BaseEvent();
		event.orderId = 1;
		ob.cancelOrder(event);
		
		Order order = oMap.get(event.orderId);
		assertTrue(order == null && event.matchingResult == null);
		assertTrue(oPool.size() == 2 && bPool.size() == 2);

		event.orderId = 3;
		ob.cancelOrder(event);
		order = oMap.get(event.orderId);
		assertTrue(oPool.size() == 3 && bPool.size() == 3);
		assertTrue(order == null);

		event.orderId = 4;
		ob.cancelOrder(event);

		PriceLadder askLadder = ob.askLadder();
		assertTrue(SearchUtil.findElementByAscending(askLadder.priceArr(), 998) == -1 && askLadder.bucketMap().get(998) == null);
		assertTrue(SearchUtil.findElementByAscending(askLadder.priceArr(), 996) == -1 && askLadder.bucketMap().get(996) == null);
		assertTrue(SearchUtil.findElementByAscending(askLadder.priceArr(), 999) >= 0 && askLadder.bucketMap().get(999) != null && askLadder.bucketMap().get(999).amount == 1);
	}
	
	@Test
	public void testChangeBidOrder() throws Exception {

		BaseEvent event = createTradeEvent(0, 999, 999, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 10, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 997, 12, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.askLadder().size() == 2 && ob.bidLadder().size() == 1);
		
		OrderBucket bidBucket = ob.bidLadder().getBucket(997);
		assertTrue(bidBucket.head.amount == 12);
		assertTrue(bidBucket.head.filled == 10);
		
		//change available amount from 2 -> 3
		event = createTradeEvent(5, 997, 3, OrderSide.BID, OrderType.LIMIT);
		ob.changeOrder(event);
		
		bidBucket = ob.bidLadder().getBucket(997);
		assertTrue(bidBucket.head.amount == 13 && bidBucket.head.filled == 10);
		
		//chang price 997 -> 998 lead to matching and change available amount
		event = createTradeEvent(5, 998, 2, OrderSide.BID, OrderType.LIMIT);
		ob.changeOrder(event);
		
		bidBucket = ob.bidLadder().getBucket(998);
//		assertTrue(bidBucket.head.amount == 12 && bidBucket.head.filled == 11);
	}
	
	@Test
	public void testChangeAskOrder() throws Exception {
		
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 10, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 999, 10, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.bidLadder().priceArr().size() == 2 && ob.askLadder().priceArr().size() == 1);
		
		OrderBucket askBucket = ob.askLadder().getBucket(999);
		assertTrue(askBucket.head.amount == 10 && askBucket.head.filled == 2);
		
		//change available amount from 8 -> 10
		event = createTradeEvent(5, 999, 10, OrderSide.ASK, OrderType.LIMIT);
		ob.changeOrder(event);
		askBucket = ob.askLadder().getBucket(999);
		assertTrue(askBucket.head.amount == 12 && askBucket.head.filled == 2);
		
		//chang price 998 -> 999 lead to matching and change available amount
		event = createTradeEvent(5, 998, 8, OrderSide.ASK, OrderType.LIMIT);
		ob.changeOrder(event);
		askBucket = ob.askLadder().getBucket(998);
//		assertTrue(askBucket.head.amount == 10 && askBucket.head.filled == 3);
	}
	
	@Test
	public void testNewAndCancelStopOrder() throws Exception {
		
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 1		
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 2
		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 3
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 4

		event = createTradeEvent(0, 997, 5, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5

		PriceLadder bidLadder = ob.bidLadder();
		PriceLadder askLadder = ob.askLadder();
		assertTrue(bidLadder.size() == 1 && askLadder.size() == 2 && ob.marketPrice() == 997);

		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.stopPrice = 998;// id 6
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.stopPrice = 997;// id 7
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.stopPrice = 996;
		ob.placeOrder(event, true);// id 8

		event = createTradeEvent(0, 0, 1, OrderSide.ASK, OrderType.STOP_MARKET);
		event.stopPrice = 997;// id  9
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.ASK, OrderType.STOP_MARKET);
		event.stopPrice = 996;// id 10
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.ASK, OrderType.STOP_MARKET);
		event.stopPrice = 995;// id 11
		ob.placeOrder(event, true);

		PriceLadder stopBidLadder = ob.stopBidLadder();
		PriceLadder stopAskLadder = ob.stopAskLadder();

		assertTrue(stopBidLadder.size() == 1 && stopAskLadder.size() == 2);

		event = new BaseEvent();
		event.orderId = 10;
		ob.cancelOrder(event);

		event.orderId = 11;
		ob.cancelOrder(event);
		
		assertTrue(stopBidLadder.size() == 1 && stopAskLadder.size() == 0);

		event.orderId = 6;
		ob.cancelOrder(event);

		assertTrue(stopBidLadder.size() == 0 && stopAskLadder.size() == 0);
	}

	@Test
	public void testActivateStopMarketOrder() throws Exception {
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);
		
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);		
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 995, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 994, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 993, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 992, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 991, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 991, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 991, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 990, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 989, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);	
				
		assertTrue( ob.marketPrice() == 991);
		
		event = createTradeEvent(0, 0, 3, OrderSide.BID, OrderType.STOP_MARKET);
		event.price = 999 * 10000;
		event.stopPrice = 992;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.price = 999 * 10000;
		event.stopPrice = 993;
		ob.placeOrder(event, true);		
		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.price = 999 * 10000;
		event.stopPrice = 994;
		ob.placeOrder(event, true);
		
		PriceLadder stopBidLadder = ob.stopBidLadder();
		assertTrue(stopBidLadder.size() == 3);
	
		event = createTradeEvent(0, 992, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(event.matchingResult.price == 992);
		
		BaseEvent nextEvent = event.next;
		assertTrue(nextEvent.matchingResult.price == 993 && nextEvent.matchingResult.next.price == 994 
				&& nextEvent.matchingResult.next.next.price == 995);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.matchingResult.price == 996);		
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.matchingResult.price == 997);		
	}
	
	@Test
	public void testActivateStopLimitOrder() throws Exception {
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);
		
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);		
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 995, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 994, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 993, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 992, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 991, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 991, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 991, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 990, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 989, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);	
				
		assertTrue(ob.marketPrice() == 991);
		
		event = createTradeEvent(0, 0, 3, OrderSide.BID, OrderType.STOP_MARKET);
		event.price = 999 * 10000;
		event.stopPrice = 992;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 993, 1, OrderSide.BID, OrderType.STOP_LIMIT);
		event.stopPrice = 994;
		ob.placeOrder(event, true);		
		event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.STOP_LIMIT);
		event.stopPrice = 995;
		ob.placeOrder(event, true);
		
		
		PriceLadder stopBidLadder = ob.stopBidLadder();
		assertTrue(stopBidLadder.size() == 3);
	
		event = createTradeEvent(0, 992, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.marketPrice() == 995);
		
		BaseEvent nextEvent = event.next;
		assertTrue(nextEvent.matchingResult.price == 993 && nextEvent.matchingResult.next.price == 994 
				&& nextEvent.matchingResult.next.next.price == 995);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.price == 993 && nextEvent.orderType == OrderType.LIMIT && nextEvent.matchingResult == null);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.price == 994 && nextEvent.orderType == OrderType.LIMIT && nextEvent.matchingResult == null);

		assertTrue(oMap.size() == 9 && ob.bidLadder().size() == 5 && ob.askLadder().size() == 4 && ob.stopBidLadder().size() == 0);
	}

	@Test
	public void testNewAndCancelOcoOrder() throws Exception {

		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 1
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 2
		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 3
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 4

		event = createTradeEvent(0, 997, 5, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5

		PriceLadder bidLadder = ob.bidLadder();
		PriceLadder askLadder = ob.askLadder();
		assertTrue(bidLadder.size() == 1 && askLadder.size() == 2 && ob.marketPrice() == 997);

		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 998;// id 6
		ob.placeOrder(event, true);

		assertTrue(event.orderId == 6 && event.orderType == OrderType.LIMIT && !event.ignoreCheckBalance && event.eventResult == EventResult.OK
					&& event.next.orderId == 7 && event.next.orderType == OrderType.STOP_MARKET && event.next.ignoreCheckBalance && event.next.eventResult == EventResult.OK);


		PriceLadder stopBidLadder = ob.stopBidLadder();
		PriceLadder stopAskLadder = ob.stopAskLadder();

		assertTrue(bidLadder.size() == 2 && stopBidLadder.size() == 1);

		event = new BaseEvent();
		event.orderId = 6;
		ob.cancelOrder(event);
		
		assertTrue(event.ignoreCheckBalance && event.tradeOption == TradeOption.OCO);

		assertTrue(bidLadder.size() == 1 && stopBidLadder.size() == 1);
		
		event = new BaseEvent();
		event.orderId = 7;
		ob.cancelOrder(event);
		
		assertTrue(!event.ignoreCheckBalance && bidLadder.size() == 1 && stopBidLadder.size() == 0 && event.tradeOption == TradeOption.OCO);
		
		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 996;
		ob.placeOrder(event, true);
		
		assertTrue(event.orderId == 8 && event.orderType == OrderType.LIMIT && !event.ignoreCheckBalance && event.eventResult == EventResult.OK
				&& event.next.orderId == 9 && event.next.orderType == OrderType.STOP_MARKET && event.next.ignoreCheckBalance && event.next.eventResult != EventResult.OK);

	}


	//test activate OCO stop order -> remove OCO limit order
	@Test
	public void testNewAndActivateOcoOrder() throws Exception {
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);
		
		PriceLadder bidLadder = ob.bidLadder();
		PriceLadder askLadder = ob.askLadder();
		PriceLadder stopBidLadder = ob.stopBidLadder();
		PriceLadder stopAskLadder = ob.stopAskLadder();

		BaseEvent event = createTradeEvent(0, 999, 6, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 1

		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 2

		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 3

		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 4
		
		//filled
		event = createTradeEvent(0, 997, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5


		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 6
		
		assertTrue(bidLadder.size() == 1 && askLadder.size() == 3 && ob.marketPrice() == 996);

		//place OCO order with limit price of 996 and stop price of 998
		event = createTradeEvent(0, 996, 5, OrderSide.BID, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 998;
		ob.placeOrder(event, true);

		assertTrue(stopBidLadder.size() == 1);
		assertTrue(bidLadder.size() == 2 && askLadder.size() == 3);

		//place order that activate OCO stop order
		event = createTradeEvent(0, 999, 3, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5

		assertTrue(ob.marketPrice() == 999 && stopBidLadder.size() == 0);
		assertTrue(bidLadder.size() == 1 && askLadder.size() == 1);

		assertTrue(event.matchingResult.type == MatchingType.TRADE && event.matchingResult.amount == 1
					&& event.matchingResult.next.type == MatchingType.TRADE && event.matchingResult.next.amount == 1 
					&& event.matchingResult.next.next.type == MatchingType.TRADE && event.matchingResult.next.next.amount == 1);
		
		BaseEvent nextEvent = event.next;
		assertTrue(nextEvent.eventType == EventType.CANCEL_ORDER && nextEvent.amount == 5);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.matchingResult.type == MatchingType.TRADE && nextEvent.matchingResult.amount == 4
					&& nextEvent.matchingResult.makerFilled == 5 && nextEvent.matchingResult.makerAmount == 6
					&& nextEvent.matchingResult.next.type == MatchingType.CANCEL && nextEvent.matchingResult.next.amount == 1);
		
		
		
		//place OCO ask order with limit price of 999 and stop price of 996
		event = createTradeEvent(0, 999, 5, OrderSide.ASK, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 996;
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 996, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		//place order that activate OCO stop order
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.marketPrice() == 995 && stopAskLadder.size() == 0);
		assertTrue(bidLadder.size() == 0 && askLadder.size() == 1);
		
		assertTrue(event.matchingResult.type == MatchingType.TRADE && event.matchingResult.amount == 1);
		
		nextEvent = event.next;
		assertTrue(nextEvent.eventType == EventType.CANCEL_ORDER && nextEvent.amount == 5);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.matchingResult.type == MatchingType.TRADE && nextEvent.matchingResult.amount == 1
					&& nextEvent.matchingResult.next.type == MatchingType.CANCEL && nextEvent.matchingResult.next.amount == 4);
	}
	
	//test activate OCO stop order -> remove OCO limit order
	@Test
	public void testNewAndActivateOcoOrderWithTradeDigit() throws Exception {
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(2, 0, 0);
		assertTrue((long)ob.symbolScale() == 100);
		
		PriceLadder bidLadder = ob.bidLadder();
		PriceLadder askLadder = ob.askLadder();
		PriceLadder stopBidLadder = ob.stopBidLadder();
		PriceLadder stopAskLadder = ob.stopAskLadder();

		BaseEvent event = createTradeEvent(0, 999, 6, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 1

		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 2

		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 3

		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 4
		
		//filled
		event = createTradeEvent(0, 997, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5


		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 6
		
		assertTrue(bidLadder.size() == 1 && askLadder.size() == 3 && ob.marketPrice() == 996);

		//place OCO order with limit price of 996 and stop price of 998
		event = createTradeEvent(0, 980, 5, OrderSide.BID, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 998;
		ob.placeOrder(event, true);

		assertTrue(bidLadder.size() == 2 && askLadder.size() == 3 && stopBidLadder.size() == 1);
		assertTrue(!event.ignoreCheckBalance && event.next.ignoreCheckBalance && event.next.price == 49);

		//place order that activate OCO stop order
		event = createTradeEvent(0, 999, 3, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5

		assertTrue(ob.marketPrice() == 999 && stopBidLadder.size() == 0);
		assertTrue(bidLadder.size() == 1 && askLadder.size() == 1);

		assertTrue(event.matchingResult.type == MatchingType.TRADE && event.matchingResult.amount == 1
					&& event.matchingResult.next.type == MatchingType.TRADE && event.matchingResult.next.amount == 1 
					&& event.matchingResult.next.next.type == MatchingType.TRADE && event.matchingResult.next.next.amount == 1);
		
		assertTrue(event.matchingResult.type == MatchingType.TRADE && event.matchingResult.amount == 1
				&& event.matchingResult.next.type == MatchingType.TRADE && event.matchingResult.next.amount == 1 
				&& event.matchingResult.next.next.type == MatchingType.TRADE && event.matchingResult.next.next.amount == 1);
	
		BaseEvent nextEvent = event.next;
		assertTrue(nextEvent.eventType == EventType.CANCEL_ORDER && nextEvent.amount == 5);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.matchingResult.type == MatchingType.TRADE && nextEvent.matchingResult.amount == 4
				&& nextEvent.matchingResult.makerFilled == 5 && nextEvent.matchingResult.makerAmount == 6
				&& nextEvent.matchingResult.next.type == MatchingType.CANCEL && nextEvent.matchingResult.next.amount == 1);
		
		//place OCO ask order with limit price of 999 and stop price of 996
		event = createTradeEvent(0, 999, 5, OrderSide.ASK, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 996;
		ob.placeOrder(event, true);
		assertTrue(!event.ignoreCheckBalance && event.next.ignoreCheckBalance && event.next.price == 0);
		
		event = createTradeEvent(0, 996, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		//place order that activate OCO stop order
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.marketPrice() == 995 && stopAskLadder.size() == 0);
		assertTrue(bidLadder.size() == 0 && askLadder.size() == 1);
		
		assertTrue(event.matchingResult.type == MatchingType.TRADE && event.matchingResult.amount == 1);
		
		nextEvent = event.next;
		assertTrue(nextEvent.eventType == EventType.CANCEL_ORDER && nextEvent.amount == 5);
		
		nextEvent = nextEvent.next;
		assertTrue(nextEvent.matchingResult.type == MatchingType.TRADE && nextEvent.matchingResult.amount == 1
					&& nextEvent.matchingResult.next.type == MatchingType.CANCEL && nextEvent.matchingResult.next.amount == 4);
	}


	//test hit limit of OCO -> remove stop order
	@Test
	public void testNewAndHitLimitOcoOrder() throws Exception {
		
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.tradeDecimals(0, 0, 0);

		PriceLadder bidLadder = ob.bidLadder();
		PriceLadder askLadder = ob.askLadder();
		PriceLadder stopBidLadder = ob.stopBidLadder();
		PriceLadder stopAskLadder = ob.stopAskLadder();

		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 1

		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 2

		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 3

		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 4
		
		//filled
		event = createTradeEvent(0, 997, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true); // id 5

		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 993, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);

		assertTrue(bidLadder.size() == 3 && askLadder.size() == 3 && ob.marketPrice() == 996);

		//place oco bid order with limit price of 996, stop price of 998
		event = createTradeEvent(0, 996, 2, OrderSide.BID, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 998;
		ob.placeOrder(event, true);
		long orderId = event.orderId;
		
		assertTrue(stopBidLadder.size() == 1 && bidLadder.size() == 4);

		//place order that fills OCO limit order
		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(bidLadder.size() == 4 && askLadder.size() == 3 && ob.marketPrice() == 996);
		assertTrue(stopAskLadder.size() == 0 && stopBidLadder.size() == 0);
		assertTrue(bidLadder.getBucket(996).amount == 1);
		
		event = createTradeEvent(orderId, 996, 1, OrderSide.BID, OrderType.LIMIT);
		ob.cancelOrder(event);
		
		assertTrue(bidLadder.getBucket(996) == null);
		assertTrue(event.matchingResult.type == MatchingType.CANCEL && event.matchingResult.amount == 1 && event.matchingResult.makerFilledCost == 996);
		
		//place oco ask order with limit price of 996, stop price of 994
		event = createTradeEvent(0, 996, 2, OrderSide.ASK, OrderType.LIMIT, TradeOption.OCO);
		event.stopPrice = 994;
		ob.placeOrder(event, true);
		
		assertTrue(stopAskLadder.size() == 1 && askLadder.size() == 4);
		
		//place order that fills OCO limit order
		event = createTradeEvent(0, 996, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(askLadder.size() == 4 && bidLadder.size() == 3 && ob.marketPrice() == 996);
		assertTrue(stopAskLadder.size() == 0 && stopBidLadder.size() == 0);
		assertTrue(askLadder.getBucket(996).amount == 1);
	}

    //test reject bid post only
    @Test
    public void testRejectBidPostOnly() throws Exception {

        PriceLadder bidLadder = ob.bidLadder();
        PriceLadder askLadder = ob.askLadder();

        BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 1

        event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 2

        event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 3


        event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 4


        event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT);
        ob.placeOrder(event, true); // id 5


        event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
        ob.placeOrder(event, true); // id 5


//        PrintPriceLadder("askLadder------------", askLadder);
//        PrintPriceLadder("bidLadder------------", bidLadder);
//        PrintPriceLadder("stopBidLadder--------", stopBidLadder);
//        PrintPriceLadder("stopAskLadder--------", stopAskLadder);
//        System.out.println("marketPrice: " + ob.marketPrice());

        event = createTradeEvent(0, 996, 1, OrderSide.BID, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

        event = createTradeEvent(0, 997, 1, OrderSide.BID, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

//        PrintPriceLadder("askLadder------------", askLadder);
//        PrintPriceLadder("bidLadder------------", bidLadder);
//        PrintPriceLadder("stopBidLadder--------", stopBidLadder);
//        PrintPriceLadder("stopAskLadder--------", stopAskLadder);
//        System.out.println("marketPrice: " + ob.marketPrice());

        assertTrue(bidLadder.size() == 2);
        assertTrue(askLadder.size() == 4);
        assertTrue(ob.marketPrice() == -1);

    }

    //test reject bid post only
    @Test
    public void testRejectAskPostOnly() throws Exception {

        PriceLadder bidLadder = ob.bidLadder();
        PriceLadder askLadder = ob.askLadder();

        BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 1

        event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 2

        event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 3

        //filled
        event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 4
        //filled
        event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT);
        ob.placeOrder(event, true); // id 5


        event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
        ob.placeOrder(event, true); // id 5


//        PrintPriceLadder("askLadder------------", askLadder);
//        PrintPriceLadder("bidLadder------------", bidLadder);
//        PrintPriceLadder("stopBidLadder--------", stopBidLadder);
//        PrintPriceLadder("stopAskLadder--------", stopAskLadder);
//        System.out.println("marketPrice: " + ob.marketPrice());

        event = createTradeEvent(0, 995, 1, OrderSide.ASK, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

        event = createTradeEvent(0, 994, 1, OrderSide.ASK, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

        assertTrue(bidLadder.size() == 2);
        assertTrue(askLadder.size() == 4);
        assertTrue(ob.marketPrice() == -1);

    }

    //test testAcceptAskPostOnly
    @Test
    public void testAcceptPostOnly() throws Exception {

        PriceLadder bidLadder = ob.bidLadder();
        PriceLadder askLadder = ob.askLadder();

        BaseEvent event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
        ob.placeOrder(event, true);// id 3

        event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

        assertTrue(bidLadder.size() == 0);
        assertTrue(askLadder.size() == 2);

        //filled
        event = createTradeEvent(0, 993, 1, OrderSide.BID, OrderType.LIMIT);
        ob.placeOrder(event, true); // id 5


        event = createTradeEvent(0, 992, 1, OrderSide.BID, OrderType.LIMIT);
        ob.placeOrder(event, true); // id 5


//        PrintPriceLadder("askLadder------------", askLadder);
//        PrintPriceLadder("bidLadder------------", bidLadder);
//        PrintPriceLadder("stopBidLadder--------", stopBidLadder);
//        PrintPriceLadder("stopAskLadder--------", stopAskLadder);
//        System.out.println("marketPrice: " + ob.marketPrice());

        event = createTradeEvent(0, 995, 1, OrderSide.ASK, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

        event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT, TradeOption.POST_ONLY);
        ob.placeOrder(event, true); // id 5

//        PrintPriceLadder("askLadder------------", askLadder);
//        PrintPriceLadder("bidLadder------------", bidLadder);
//        PrintPriceLadder("stopBidLadder--------", stopBidLadder);
//        PrintPriceLadder("stopAskLadder--------", stopAskLadder);
//        System.out.println("marketPrice: " + ob.marketPrice());

        assertTrue(bidLadder.size() == 3);
        assertTrue(askLadder.size() == 3);
        assertTrue(ob.marketPrice() == -1);

    }


	@Test
	public void testFillOrKill() throws Exception {
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 1
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 2
		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 3
		event = createTradeEvent(0, 996, 3, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);// id 4

		event = createTradeEvent(0, 995, 2, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);


		PriceLadder bidLadder = ob.bidLadder();
		PriceLadder askLadder = ob.askLadder();

		assertTrue(bidLadder.size() == 2 && askLadder.size() == 4 );

		//reject fill or kill
		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.FILL_OR_KILL);
		ob.placeOrder(event, true);
		assertTrue(event.orderId != 0 && event.eventResult == EventResult.OK &&
				event.matchingResult != null && event.matchingResult.type == MatchingType.CANCEL && event.matchingResult.amount == 1);
		
		event = createTradeEvent(0, 997, 5, OrderSide.BID, OrderType.FILL_OR_KILL);
		ob.placeOrder(event, true);
		assertTrue(event.orderId != 0 && event.eventResult == EventResult.OK &&
				event.matchingResult != null && event.matchingResult.type == MatchingType.CANCEL && event.matchingResult.amount == 5);

		event = createTradeEvent(0, 996, 1, OrderSide.ASK, OrderType.FILL_OR_KILL);
		ob.placeOrder(event, true);
		assertTrue(event.orderId != 0 && event.eventResult == EventResult.OK &&
				event.matchingResult != null && event.matchingResult.type == MatchingType.CANCEL && event.matchingResult.amount == 1);
		
		event = createTradeEvent(0, 994, 5, OrderSide.ASK, OrderType.FILL_OR_KILL);
		ob.placeOrder(event, true);
		assertTrue(event.orderId != 0 && event.eventResult == EventResult.OK &&
				event.matchingResult != null && event.matchingResult.type == MatchingType.CANCEL && event.matchingResult.amount == 5);
		
		assertTrue(bidLadder.size() == 2 && askLadder.size() == 4);

		//accept fill or kill
		event = createTradeEvent(0, 997, 4, OrderSide.BID, OrderType.FILL_OR_KILL);
		ob.placeOrder(event, true);
		assertTrue(event.eventResult == EventResult.OK && event.matchingResult != null && 
				event.matchingResult.amount == 3 && event.matchingResult.next.amount == 1);
		
		assertTrue(ob.marketPrice() == 997);
		
		event = createTradeEvent(0, 994, 4, OrderSide.ASK, OrderType.FILL_OR_KILL);
		ob.placeOrder(event, true);
		assertTrue(event.eventResult == EventResult.OK && event.matchingResult != null && 
				event.matchingResult.amount == 2 && event.matchingResult.next.amount == 1 && event.matchingResult.next.next.amount == 1);
		
		assertTrue(ob.marketPrice() == 994);
	}
	
	@Test
	public void testCombinationOfManyOrderTypes() throws Exception {
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.marketPrice(6100);
		
		BaseEvent event = createTradeEvent(0, 6000, 6, OrderSide.BID, OrderType.LIMIT);
		event.stopPrice = 6200;
		event.tradeOption = TradeOption.OCO;
		ob.placeOrder(event, true);
		
		assertTrue(event.orderId == 1 && event.orderType == OrderType.LIMIT && event.next.orderId == 2 && event.next.orderType == OrderType.STOP_MARKET);
		assertTrue(ob.bidLadder().size() == 1 && ob.stopBidLadder().size() == 1);
		
		event = createTradeEvent(1, 6000, 6, OrderSide.BID, OrderType.LIMIT);
		ob.cancelOrder(event);
		
		assertTrue(ob.bidLadder().size() == 0 && ob.stopBidLadder().size() == 1);
		
		event = createTradeEvent(0, 6200, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.bidLadder().size() == 1 && ob.stopBidLadder().size() == 1);
		
		event = createTradeEvent(0, 6200, 4, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.bidLadder().size() == 0 && ob.stopBidLadder().size() == 0 && ob.askLadder().size() == 0);
		assertTrue(event.matchingResult.amount == 1 && event.next.matchingResult.amount == 3 && event.next.matchingResult.next.type == MatchingType.CANCEL);
		
	}
	
	@Test
	public void testCombinationOfManyOrderTypes2() throws Exception {
		ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, new TradeEventManager(1 << 10));
		ob.marketPrice(15723);
		
		BaseEvent event = createTradeEvent(0, 15536, 178900, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 158236, 253600, OrderSide.ASK, OrderType.LIMIT);
		event.stopPrice = 15536;
		event.tradeOption = TradeOption.OCO;
		ob.placeOrder(event, true);

		event = createTradeEvent(0, 15536, 73659, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(event.orderId == 4 && event.matchingResult.type == MatchingType.TRADE && event.matchingResult.amount == 73659
				&& event.next.orderId == 2 && event.next.eventType == EventType.CANCEL_ORDER
				&& event.next.next.orderId == 3 && event.next.next.matchingResult.type == MatchingType.TRADE && event.next.next.matchingResult.amount == 105241
				&& event.next.next.matchingResult.next.type == MatchingType.CANCEL && event.next.next.matchingResult.next.amount == 148359);
	}

	public static BaseEvent createTradeEvent(long id, long price, long amount, byte side, byte type) {
		BaseEvent event = new BaseEvent();
		event.orderId = id;
		event.price = price;
		event.amount = amount;
		event.orderSide = side;
		event.orderType = type;
		event.tradeOption = TradeOption.NONE;
		return event;
	}

	public static BaseEvent createTradeEvent(long id, long price, long amount, byte side, byte type, byte tradeOption) {
		BaseEvent event = new BaseEvent();
		event.orderId = id;
		event.price = price;
		event.amount = amount;
		event.orderSide = side;
		event.orderType = type;
		event.tradeOption = tradeOption;
		return event;
	}

	public static void PrintPriceLadder(String msg, PriceLadder priceLadder) {
		String book = "";

		for(int i =  0; i <  priceLadder.priceArr().size(); i++) {
			try {
				String price = String.valueOf(priceLadder.priceArr().at(i));
				book += "," + price;
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println(msg + book);
	}
	
}
