package org.ssi.model;

import org.openjdk.jol.info.ClassLayout;

public class BaseEvent{
	long p000, p001, p0002, p003=1;
	int p004 =1;
	byte p005=1;
	/** **/
	public byte eventType;
	/**
	 * Case eventType = EventType.CHANGE_BALANCE(4):
	 * DEPOSIT: orderSide = 1, amount = money deposit > 0
	 * Request withdraw: orderSide = -1, amount = money withdraw < 0
	 * Confirm withdraw: orderSide = -2, amount = money withdraw < 0
	 * Cancel withdraw: orderSide = -3, amount = money withdraw < 0
	 * Change balance: orderSide = 0.
	 */
	public byte orderSide; // case add symbol: base decimal

	public byte orderType; // case add symbol: counter decimal

	public byte tradeType = TradeType.EXCHANGE; // case add symbol: price decimal
	public byte tradeOption = TradeOption.NONE;
	public byte eventResult;

	public boolean ignoreCheckBalance = false;
	public int symbol;// case ADD_NEW_USER brokerId = symbol
	int  p007=7;

	public long orderId;
    public long clientOrderId;
    public long timestamp;
    /** incase event CHANGE_BALANCE, symbol = currency **/
    /** incase event ADD_SYMBOL, EDIT_SYMBOL, REMOVE_SYMBOL, symbol = symbol, price = base, amount = counter **/

    public long price; // Case CHANGE_FEE (14) makerFee   . case ADD_NEW_USER: makerFee
    public long stopPrice; // case CHANGE_BALANCE, add brokerId // // Case CHANGE_FEE (14) takerFee. //case ADD_NEW_USER: takerFee
    public long amount;
    // case when RESET_FEE, userId = brokerId
    public long userId;

    
    // passed to the next consumer along the chain
    public MatchingResult matchingResult; // a linked list of results, each result object must be pre-allocated and managed by an object pool
    
    public BaseEvent next;

    // [START] case RESET_FEE_BY_BROKER
    // orderId: makerFee
    // clientOrderId: takerFee
    // symbol: brokerId
    // [END] RESET_FEE_BY_BROKER
    
    // [START] ADD_NEW_USER
    // brokerId = event.symbol
    // userId = event.userId
    // makerFee = event.price
    // takerFee = event.stopPrice
    // referUserId : event.clientOrderId
    // [START] ADD_NEW_USER
    
    // [START] CHANGE_BALANCE
    // event.userId: user id
    // event.symbol: currency id
    // event.stopPrice: brokerId
    // event.amount: amount change balance
    // [START] CHANGE_BALANCE
    
    //[START] CHANGE_FEE
    // event.price: makerFee 
    // event.stopPrice: takerFee 
    //[END] CHANGE_FEE
    
    public void copy(BaseEvent event) {
    	eventType = event.eventType;
    	
    	orderId = event.orderId;
    	clientOrderId = event.clientOrderId;
    	timestamp = event.timestamp;
    	
    	symbol = event.symbol;
    	price = event.price;    	
    	stopPrice = event.stopPrice;
    	amount = event.amount;
    	
    	orderSide = event.orderSide;
    	orderType = event.orderType;
    	
    	tradeType = event.tradeType;
    	tradeOption = event.tradeOption;
    	
    	userId = event.userId;
    	ignoreCheckBalance = event.ignoreCheckBalance;
    	eventResult = event.eventResult;
    }
    
    public boolean isStopOrderEvent() {
    	return orderType == OrderType.STOP_LIMIT || orderType == OrderType.STOP_MARKET;
    }

	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer("BaseEvent [eventType=").append(eventType).append(", orderId=").append(orderId).append(", clientOrderId=").append(clientOrderId
				+ ", timestamp=").append(timestamp).append(", symbol=").append(symbol).append(", price=").append(price).append(", stopPrice=").append(stopPrice
				+ ", amount=").append(amount).append(", orderSide=").append(orderSide).append(", orderType=").append(orderType).append(", tradeType="
				+ tradeType).append(", tradeOption=").append(tradeOption).append(", userId=").append(userId).append(", eventResult=").append(eventResult
				+ ", matchingResult=").append(matchingResult).append(", next=").append(next).append("]");
		return bf.toString();
	}
//
	public static void main(String args[]){
		System.out.println(ClassLayout.parseClass(BaseEvent.class).toPrintable());
	}
    
}
