package org.ssi.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;

import org.ssi.collection.Int2DoubleMap;
import org.ssi.collection.Int2ObjectMap;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.constant.ScaleConstants;
import org.ssi.io.BalanceDataWriter;
import org.ssi.io.BufferedChannel;
import org.ssi.io.MetadataManager;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventResult;
import org.ssi.model.EventType;
import org.ssi.model.MatchingResult;
import org.ssi.model.MatchingType;
import org.ssi.model.MoneyTransaction;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.model.UserBalance;
import org.ssi.serialization.BalanceSerializer;
import org.ssi.util.BitUtil;
import org.ssi.util.MathUtils;
import org.ssi.util.SystemPropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
	private static final Logger LOG = LoggerFactory.getLogger(BalanceService.class);
	private Int2DoubleMap rateCounterDecimal;
	private Long2ObjectMap<UserBalance> balances = new Long2ObjectMap<>();
	private static final double[] POWERS_OF_10 = {1d, 10d, 100d, 1000d, 10000d, 100000d, 1000000d, 10000000d, 100000000d, 1000000000d};

	// map <brokerId, ing []Fee default of broker
	private Int2ObjectMap<int[]> mapFeeDefault = new Int2ObjectMap<>();

	private int defaultMakerFee = 10;
	private int defaultTakerFee = 20;

	@Value("${snapshot.balance:/path/snapshot/balance/}")
	private String path;
	
	@Autowired
	private BalanceDataWriter balanceDataWriter;

	@Autowired
	private MetadataService metaService;

	@PostConstruct
	public void init() {
		balanceDataWriter.initialize(metaService.getEpocIndex());
		rateCounterDecimal = new Int2DoubleMap();
		if (SystemPropsUtil.getLoadSnapshot()) {
			long index = SystemPropsUtil.getEpocIndex();
			if (index == -1) {
				index = metaService.getEpocIndex() - 1;
			}
			if (index < 0) return;
			loadSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BAlANCE_SNAPSHOT, index));
			LOG.info("Loading balance snapshot @epoc {}... done.", index);
		}
	}

	public Long2ObjectMap<UserBalance> getBalances() {
		return balances;
	}
	
	//utility method for mocking
	public void balanceDataWriter(BalanceDataWriter writer) {
		balanceDataWriter = writer;
	}
	
	public void rateCounterDecimal(Int2DoubleMap rateMap) {
		rateCounterDecimal = rateMap;
		
	}
	public Int2DoubleMap getRates() {
		return rateCounterDecimal;
	}
	
	public byte check(BaseEvent event, long sequence, boolean endOfBatch) {
		// check event
		switch (event.eventType) {
		case EventType.PLACE_ORDER:
			placeOrder(event, sequence, endOfBatch);
			break;
		case EventType.ADD_NEW_USER:
			addUser(event);
			break;
		case EventType.EDIT_USER:
			break;
		case EventType.REMOVE_USER:
			balances.remove(event.userId);
			break;
		case EventType.CHANGE_BALANCE:
			changeBalance(event);
			break;

//		case CHANGE_ORDER:
//			break;
//		case CANCEL_ORDER:
//			break;
		case EventType.ADD_SYMBOL:
			addSymbol(event, sequence, endOfBatch);
			break;
		case EventType.EDIT_SYMBOL:
			break;
		case EventType.REMOVE_SYMBOL:
			break;
		case EventType.TAKE_SNAPSHOT:
			break;
		case EventType.REPLAY_EVENTS:
			break;
		case EventType.SHUTDOWN:
			break;
		case EventType.CHANGE_FEE:
			changeFee(event);
			break;		
		case EventType.RESET_FEE_BY_BROKER:
			resetFee(event);
			break;
		case EventType.APPLYING_RESULTS:
			stopApplyingResults(event);
			break;
		default:
			break;
		}

		return EventResult.OK;
	}

	private void stopApplyingResults(BaseEvent event) {
		balanceDataWriter.writeSingleEvent(-1, event.eventResult, -1, 0, 0, true);		
	}

	private void addSymbol(BaseEvent event, long sequence, boolean endOfBatch) {
		byte baseDecimal = event.orderSide;
		byte counterDecimal = event.orderType;
		byte priceDecimal = event.tradeType;
		int decimal = counterDecimal - baseDecimal - priceDecimal;
		double val = 0;
		if (decimal > 0) {
			val = POWERS_OF_10[decimal];
		} else {
			val = 1/ POWERS_OF_10[-decimal];
		}
		rateCounterDecimal.put(event.symbol, val);
		LOG.info("SYMBOL_____" + event.symbol + "    " + (counterDecimal - baseDecimal - priceDecimal));
	}

	private void changeBalance(BaseEvent event) {
		if (event.amount == 0) {
			event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			return;
		}

		// case CHANGE_BALANCE, brokerId = stopPrice
		int brokerId = (int) event.stopPrice;
		UserBalance userBalance = balances.get(event.userId);
		if (userBalance == null) {
			int[] brokerFee = mapFeeDefault.get(brokerId);
			int makerFee = defaultMakerFee;
			int takerFee = defaultTakerFee;
			if (brokerFee != null) {
				makerFee = brokerFee[0];
				takerFee = brokerFee[1];
			}
			userBalance = new UserBalance(event.userId, makerFee, takerFee, brokerId);
			userBalance.setBalance(event.symbol, 0, 0);
			balances.put(event.userId, userBalance);
		}

		int index = 0;
		switch (event.orderSide) {
		case MoneyTransaction.CHANGE_BALANCE:
			if (event.amount <= 0) {
				event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			} else {
				index = userBalance.findIndexBalance(event.symbol);
				if (userBalance.balance[index + 1] + event.amount < 0) {
					event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
				} else {
					userBalance.balance[index + 1] += event.amount;
				}
			}
			break;
		case MoneyTransaction.AFFILIATE_CHANGE_BALANCE:
			index = userBalance.findIndexBalance(event.symbol);
			if (userBalance.balance[index + 1] + event.amount < 0) {
				event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			} else {
				userBalance.balance[index + 1] += event.amount;
			}

			break;
		case MoneyTransaction.DEPOSIT:
			// in case deposit: AMOUNT > 0
			if (event.amount <= 0) {
				event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
				return;
			}
			index = userBalance.findIndexBalance(event.symbol);
			userBalance.balance[index + 1] += event.amount;
			break;
		case MoneyTransaction.REQUEST_WITHDRAW:
			// in case WITHDRAW: AMOUNT < 0
			index = userBalance.findIndexBalance(event.symbol);
			if (userBalance.balance[index + 1] + event.amount < 0 || userBalance.balance[index + 2] - event.amount < 0) {
				event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			} else {
				userBalance.balance[index + 1] += event.amount;
				userBalance.balance[index + 2] -= event.amount;
			}
			break;
		case MoneyTransaction.CONFIRM_WITHDRAW:
			// in case WITHDRAW: AMOUNT < 0
			index = userBalance.findIndexBalance(event.symbol);
			if (userBalance.balance[index + 2] + event.amount < 0) {
				event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			} else {
				userBalance.balance[index + 2] += event.amount;
			}
			break;
		case MoneyTransaction.CANCEL_WITHDRAW:
			index = userBalance.findIndexBalance(event.symbol);
			if (userBalance.balance[index + 2] + event.amount < 0 || userBalance.balance[index + 1] - event.amount < 0) {
				event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			} else {
				userBalance.balance[index + 2] += event.amount;
				userBalance.balance[index + 1] -= event.amount;
			}
			break;
		default:
			break;
		}
	}

	private void changeFee(BaseEvent event) {
		LOG.info("changeFee {}", event);
		UserBalance userBalance = balances.get(event.userId);

		if (userBalance != null) {
			userBalance.makerFee = (int) event.price;
			userBalance.takerFee = (int) event.stopPrice;
		}
	}

	private void resetFee(BaseEvent event) {
		LOG.info("resetFee {}", event);
		int brokerId = event.symbol;
		Object[] users = balances.values();
		for(Object obj : users) {
			UserBalance user = (UserBalance) obj;
			if (user != null) {
				if (user.brokerId == brokerId) {
					user.makerFee = (int) event.orderId;
					user.takerFee = (int) event.clientOrderId;
				}
			}
		}

		// set fee default
		int[] feeArr = mapFeeDefault.get(brokerId);
		if (feeArr == null) {
			feeArr = new int[] {(int) event.orderId, (int) event.clientOrderId};
			mapFeeDefault.put(brokerId, feeArr);
		} else {
			feeArr[0] = (int) event.orderId;
			feeArr[1] = (int) event.clientOrderId;
		}
	}

	private void writeChangeBalance(long userId, int currencyId, long sequence, boolean endOfBatch, int brokerId) {
		UserBalance userBalance = balances.get(userId);
		if (userBalance != null) {
			// Write broker to ResultBalance
			balanceDataWriter.writeSingleEvent(userId, 0, brokerId, 0, sequence, endOfBatch);

			int index = userBalance.findIndexBalance(currencyId);
			balanceDataWriter.writeSingleEvent(userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);

		}
	}

	public byte processBalance(BaseEvent event, long sequence, boolean endOfBatch) {
		// check event
		switch (event.eventType) {
//		case ADD_NEW_USER:
//			break;
//		case EDIT_USER:
//			break;
//		case REMOVE_USER:
//			break;
		case EventType.PLACE_ORDER:
			matching(event, sequence, endOfBatch);
			break;
		case EventType.CHANGE_BALANCE:
			writeChangeBalance(event.userId, event.symbol, sequence, endOfBatch, (int) event.stopPrice);
			break;


//		case CHANGE_ORDER:
//			break;
		case EventType.CANCEL_ORDER:
			cancelOrder(event, sequence, endOfBatch);
			break;
//		case EventType.ADD_SYMBOL:
//			break;
//		case EventType.EDIT_SYMBOL:
//			break;
//		case EventType.REMOVE_SYMBOL:
//			break;
		case EventType.TAKE_SNAPSHOT:
			createSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BAlANCE_SNAPSHOT));
			break;
		case EventType.REPLAY_EVENTS:
//			replayEvents();
			break;
		case EventType.SHUTDOWN:
			createSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BAlANCE_SNAPSHOT));
			break;

		default:
			break;
		}
		return EventResult.OK;
	}

	private void addUser(BaseEvent event) {
		int brokerId = event.symbol;
		int makerFee = (int) event.price;
		int takerFee = (int) event.stopPrice;

		LOG.info("addUser {} {} {} {} ", event.userId, brokerId, makerFee, takerFee);
		UserBalance user = new UserBalance(event.userId, makerFee, takerFee, brokerId);
		balances.put(event.userId, user);
	}

	private void writePlaceOrder(BaseEvent event, long sequence, boolean endOfBatch) {

		int base = BitUtil.getHigh(event.symbol);
		int counter = BitUtil.getLow(event.symbol);
		
		if (event.eventResult != EventResult.OK) {
			return;
		}

		UserBalance userBalance = balances.get(event.userId);
		if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET) {
			if (event.orderSide == OrderSide.BID) {
				// buy: hold counter = all balance counter
				// find index counter
				int index = userBalance.findIndexBalance(counter);
				balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
			} else {
				// sell: hold base = amount
				// find index base
				int index = userBalance.findIndexBalance(base);
				balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
			}
		} else {
			// LIMIT
			if (event.orderSide == OrderSide.BID) {
				// buy: hold counter = price * amount.
				// find index counter
				int index = userBalance.findIndexBalance(counter);

				balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
			} else {
				// sell: hold base = amount
				// find index base
				int index = userBalance.findIndexBalance(base);
				balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
			}
		}
	}

	private void placeOrder(BaseEvent event, long sequence, boolean endOfBatch) {
		int base = BitUtil.getHigh(event.symbol);
		int counter = BitUtil.getLow(event.symbol);
//		LOG.info("placeOrderXXX {}", event);
		UserBalance userBalance = balances.get(event.userId);
		if (userBalance == null) {
			LOG.error("NOT EXIST USER {}", event.userId);
			event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
			return;
		}
		if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET) {
			if (event.orderSide == OrderSide.BID) {
				// buy: hold counter = all balance counter
				// find index counter
				int index = userBalance.findIndexBalance(counter);
				
				// hold balance
				// hold balance when market using 

				// - balance
				if (event.orderType == OrderType.STOP_MARKET) {
					long cost = MathUtils.ceil(event.stopPrice * rateCounterDecimal.get(event.symbol) * event.amount);
					if (userBalance.balance[index + 1] >= cost) {
						// hold balance
						event.price = cost;
						userBalance.balance[index + 1] -= cost;
						userBalance.balance[index + 2] += cost;
						event.eventResult = EventResult.OK;
					} else {
						LOG.info("placeOrderXXX MARKET cost = {} balance = {}", cost, userBalance.balance[index + 1]);
						event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
					}
				} else {
					// MARKET
					event.price = userBalance.balance[index + 1];
					userBalance.balance[index + 1] = 0;
					userBalance.balance[index + 2] += event.price;// hold balance
					event.eventResult = EventResult.OK;
				}
			} else {
				// sell: hold base = amount
				// find index base
				int index = userBalance.findIndexBalance(base);
				if (userBalance.balance[index + 1] < event.amount) {
					event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
					return;
				}
				userBalance.balance[index + 1] -= event.amount; 
				userBalance.balance[index + 2] += event.amount; 
				event.eventResult = EventResult.OK;
			}
		} else {
			// LIMIT
			if (event.orderSide == OrderSide.BID) {
				// buy: hold counter = price * amount.
//				long amount = event.price * event.amount;
				
				long cost = MathUtils.ceil(event.price * rateCounterDecimal.get(event.symbol) * event.amount);
//				long cost = event.price * event.amount;
				// find index counter
				int index = userBalance.findIndexBalance(counter);
				
				if (userBalance.balance[index + 1] >= cost) {
					userBalance.balance[index + 1] -= cost;
					userBalance.balance[index + 2] += cost;
					
					event.eventResult = EventResult.OK;
				} else {
					LOG.info("placeOrderXXX BID LIMIT cost = {} balance = {}", cost, userBalance.balance[index + 1]);
					event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
				}
			} else {
				// sell: hold base = amount
				// find index base
				int index = userBalance.findIndexBalance(base);
				if (userBalance.balance[index + 1] >= event.amount) {
					userBalance.balance[index + 1] -= event.amount;
					userBalance.balance[index + 2] += event.amount;
					event.eventResult = EventResult.OK;
				} else {
					LOG.info("placeOrderXXX ASK LIMIT cost = {} balance = {}", event.amount, userBalance.balance[index + 1]);
					event.eventResult = EventResult.NOT_ENOUGH_BALANCE;
				}
			}
		}
	}

	private void matching(BaseEvent event, long sequence, boolean endOfBatch) {
//		LOG.info("matchingXXX {}", event);
		if (event.eventResult != EventResult.OK) {
			LOG.info("ERROR_MATCHING {}", event.toString());
			return;
		}
		MatchingResult result = event.matchingResult;
		if (result == null) {
			writePlaceOrder(event, sequence, endOfBatch);
			return;
		}

		long filledQuantity = 0;
		while (result != null) {
			filledQuantity += matchingOneResult(event, result, filledQuantity, sequence, endOfBatch);
			result = result.next;
		}

		BaseEvent next = event.next;
		while (next != null) {
			result = next.matchingResult;
			filledQuantity = 0;
			while (result != null) {
				filledQuantity += matchingOneResult(next, result, filledQuantity, sequence, endOfBatch);
				result = result.next;
			}

			next = next.next;
		}
	}

	private void cancelOrder(BaseEvent event, long sequence, boolean endOfBatch) {
		if (!(event.eventResult == EventResult.OK)) {
			return;
		}

		int base = BitUtil.getHigh(event.symbol);
		int counter = BitUtil.getLow(event.symbol);

		if (event.ignoreCheckBalance) {
			return;
		}

		UserBalance userBalance = balances.get(event.userId);
		if (userBalance != null) {
			if (event.orderSide == OrderSide.BID) {
				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET) {

					int index = userBalance.findIndexBalance(counter);
					userBalance.balance[index + 1] += event.price; // hold balance
					userBalance.balance[index + 2] -= event.price; // hold balance
					balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
				} else {

					int index = userBalance.findIndexBalance(counter);
					long cost = MathUtils.ceil(((event.price * rateCounterDecimal.get(event.symbol)) * event.amount));
					userBalance.balance[index + 1] += cost;
					userBalance.balance[index + 2] -= cost;

					if (userBalance.balance[index + 2] < 0) {
						userBalance.balance[index + 1] += userBalance.balance[index + 2];
						userBalance.balance[index + 2] = 0;
					} else if (userBalance.balance[index + 2] <= 1) {
						userBalance.balance[index + 2] = 0;
					}

					balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
				}
			} else {
				int index = userBalance.findIndexBalance(base);
				userBalance.balance[index + 1] += event.amount;
				userBalance.balance[index + 2] -= event.amount;
				balanceDataWriter.writeSingleEvent(event.userId, userBalance.balance[index], userBalance.balance[index + 1], userBalance.balance[index + 2], sequence, endOfBatch);
			}
		}
	}

	private long matchingOneResult(BaseEvent event, MatchingResult result, long filledAmount, long sequence, boolean endOfBatch) {
		int base = BitUtil.getHigh(event.symbol);
		int counter = BitUtil.getLow(event.symbol);

		if (result.type == MatchingType.TRADE) {
			UserBalance makerBalance = balances.get(result.makerId);
			UserBalance takerBalance = balances.get(event.userId);

//			LOG.info("matchingOneResult {} XXXXX {} ", makerBalance.toString(), takerBalance.toString());
			int indexMakerBase = makerBalance.findIndexBalance(base);
			int indexTakerBase = takerBalance.findIndexBalance(base);
			int indexMakerCounter = makerBalance.findIndexBalance(counter);
			int indexTakerCounter = takerBalance.findIndexBalance(counter);
			// maker sell, taker buy.
//			long cost = result.price * result.amount;
//			long cost = MathUtils.ceil((result.price * rateCounterDecimal.get(event.symbol)) * result.amount);
			if (event.orderSide == OrderSide.BID) {
				// maker sell: cong counter(rate * amount), tru phi counter (fee * (rate * amount)). tru reserve base(amount)
//				// taker buy: cong base (amount), tr reserve counter, tru phi (fee * (amount))
				
				// - tru reserve base(amount)

				makerBalance.balance[indexMakerBase + 2] -= result.amount;

				// + cong counter(rate * amount), tru phi counter (fee * (rate * amount)) 
//				int indexMakerCounter = makerBalance.findIndexBalance(counter);
				makerBalance.balance[indexMakerCounter + 1] += Math.floor(result.price * rateCounterDecimal.get(event.symbol) * result.amount * (1 - makerBalance.makerFee / ScaleConstants.FEE_SCALE));
//				makerBalance.balance[indexMakerCounter + 1] += Math.floor(result.price * result.amount * (1 - makerBalance.makerFee / ScaleConstants.FEE_SCALE));

				// taker buy: cong base (amount), tru phi (fee * (amount)). Tru reserve counter, 
				// cong base (amount), tru phi (fee * (amount))

				takerBalance.balance[indexTakerBase + 1] += Math.floor(result.amount * (1 - takerBalance.takerFee / ScaleConstants.FEE_SCALE));
				// Tru reserve counter, 

				long matchingPrice = event.price;
				double decimalSymbol = rateCounterDecimal.get(event.symbol);
				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET) {
					matchingPrice = result.price;
					// tru hold balance
					event.price -= MathUtils.ceil ((result.price * decimalSymbol) * result.amount);
//					event.price -= result.price * result.amount;
				}

				takerBalance.balance[indexTakerCounter + 1] += (long) Math.floor((matchingPrice - result.price) * decimalSymbol * result.amount);
				takerBalance.balance[indexTakerCounter + 2] -= MathUtils.ceil(matchingPrice * result.amount * decimalSymbol);

				if (takerBalance.balance[indexTakerCounter + 2] <= 1) {
					takerBalance.balance[indexTakerCounter + 2] = 0;
				}

				if ((event.orderType == OrderType.MARKET  || event.orderType == OrderType.STOP_MARKET)&& ((filledAmount + result.amount) == event.amount)) {
					takerBalance.balance[indexTakerCounter + 1] += event.price;
					takerBalance.balance[indexTakerCounter + 2] -= event.price;
					// TODO
					if (takerBalance.balance[indexTakerCounter + 2] <= 1) {
						takerBalance.balance[indexTakerCounter + 2] = 0;
					}
				}

				// write balance
//				balanceDataWriter.writeSingleEvent(result.makerId, makerBalance.balance[indexMakerCounter], makerBalance.balance[indexMakerCounter + 1], makerBalance.balance[indexMakerCounter + 2], event.userId, takerBalance.balance[indexTakerBase], takerBalance.balance[indexTakerBase + 1], takerBalance.balance[indexTakerBase + 2], sequence, endOfBatch);
			} else {
				// maker buy: cong base (amount) - fee; fee = amount * fee. 
				// release counter reserve (rate * amount)
				// cong base (amount) - fee; fee = amount * fee
//				int indexMakerBase = makerBalance.findIndexBalance(base);
				makerBalance.balance[indexMakerBase + 1] += Math.floor(result.amount * (1 - makerBalance.makerFee / ScaleConstants.FEE_SCALE));
				makerBalance.balance[indexMakerCounter + 2] -= MathUtils.ceil((result.price * rateCounterDecimal.get(event.symbol)) * result.amount);

				if (makerBalance.balance[indexMakerCounter + 2] <= 1) {
					makerBalance.balance[indexMakerCounter + 2] = 0;
				}
				// takersell: release hold base. cong counter (rate * amount), - fee = amount * rate * fee.
				// release reserve base
				takerBalance.balance[indexTakerBase + 2] -= result.amount;

				// cong counter (rate * amount), - fee = amount * rate * fee.
//				int indexTakerCounter = takerBalance.findIndexBalance(counter);
				takerBalance.balance[indexTakerCounter + 1] += Math.floor(result.price * rateCounterDecimal.get(event.symbol) * result.amount * (1 - takerBalance.takerFee / ScaleConstants.FEE_SCALE));

//				balanceDataWriter.writeSingleEvent(result.makerId, makerBalance.balance[indexMakerCounter], makerBalance.balance[indexMakerCounter + 1], makerBalance.balance[indexMakerCounter + 2], event.userId, takerBalance.balance[indexTakerBase], takerBalance.balance[indexTakerBase + 1], takerBalance.balance[indexTakerBase + 2], sequence, endOfBatch);
			}

			if (result.makerId == event.userId) {
				balanceDataWriter.write2Event(
						event.userId, takerBalance.balance[indexTakerBase], takerBalance.balance[indexTakerBase + 1], takerBalance.balance[indexTakerBase + 2],
						result.makerId, makerBalance.balance[indexMakerCounter], makerBalance.balance[indexMakerCounter + 1], makerBalance.balance[indexMakerCounter + 2],
						sequence, endOfBatch);
			} else {
				balanceDataWriter.write4Event(
						takerBalance.userId, takerBalance.balance[indexTakerBase], takerBalance.balance[indexTakerBase + 1], takerBalance.balance[indexTakerBase + 2],
						takerBalance.userId, takerBalance.balance[indexTakerCounter], takerBalance.balance[indexTakerCounter + 1], takerBalance.balance[indexTakerCounter + 2],
						makerBalance.userId, makerBalance.balance[indexMakerBase], makerBalance.balance[indexMakerBase + 1], makerBalance.balance[indexMakerBase + 2],
						makerBalance.userId, makerBalance.balance[indexMakerCounter], makerBalance.balance[indexMakerCounter + 1], makerBalance.balance[indexMakerCounter + 2],
						sequence, endOfBatch
						);
			}

			return result.amount;
		} else if (result.type == MatchingType.CANCEL) {
			UserBalance takerBalance = balances.get(result.makerId);
			if (event.orderSide == OrderSide.BID) {
				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET) {
					
					int indexTakerCounter = takerBalance.findIndexBalance(counter);
					takerBalance.balance[indexTakerCounter + 1] += event.price; // hold balance
					takerBalance.balance[indexTakerCounter + 2] -= event.price; // hold balance
					if (takerBalance.balance[indexTakerCounter + 2] <= 1) {
						takerBalance.balance[indexTakerCounter + 2] = 0;
					}
					balanceDataWriter.writeSingleEvent(event.userId, takerBalance.balance[indexTakerCounter], takerBalance.balance[indexTakerCounter + 1], takerBalance.balance[indexTakerCounter + 2], sequence, endOfBatch);
				} else {
					int indexTakerCounter = takerBalance.findIndexBalance(counter);
					double cost = ((event.price * rateCounterDecimal.get(event.symbol)) * result.amount);
//					long cost = event.price * result.amount;
					takerBalance.balance[indexTakerCounter + 1] += Math.floor(cost);
					takerBalance.balance[indexTakerCounter + 2] -= MathUtils.ceil(cost);

					if (takerBalance.balance[indexTakerCounter + 2] <= 1) {
						takerBalance.balance[indexTakerCounter + 2] = 0;
					}
					balanceDataWriter.writeSingleEvent(event.userId, takerBalance.balance[indexTakerCounter], takerBalance.balance[indexTakerCounter + 1], takerBalance.balance[indexTakerCounter + 2], sequence, endOfBatch);
				}
			} else {
				int indexTakeBase = takerBalance.findIndexBalance(base);
				takerBalance.balance[indexTakeBase + 1] += result.amount;
				takerBalance.balance[indexTakeBase + 2] -= result.amount;
				balanceDataWriter.writeSingleEvent(event.userId, takerBalance.balance[indexTakeBase], takerBalance.balance[indexTakeBase + 1], takerBalance.balance[indexTakeBase + 2], sequence, endOfBatch);
			}
			return 0;
		}

		return 0;
	}

	public UserBalance getBalance(long userId) {
		return balances.get(userId);
	}

	public void createSnapshot() {
		createSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BAlANCE_SNAPSHOT));
	}

	public void createSnapshot(String filePath) {
		RandomAccessFile file = null;
		try {
//			StringBuffer filePath = new StringBuffer(path).append(DateUtil.toString(DateUtil.PATTERN_YYYYMMDD_SLASH)).append("/"); 

			int size = 4096;

			ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			BalanceSerializer.encode(this, channel);
			channel.flush();			
		} catch (IOException e) {
			LOG.error("ERROR", e);
		} finally {
			if (file != null) {
				try {
					file.close();
					balanceDataWriter.writeSingleEvent(-2, metaService.getEpocIndex(), -1, 0, 0, true);
				} catch (IOException e) {
					LOG.error("ERROR", e);
				}
			}
		}
	}

	public void loadSnapshot(String filePath) {
		int size = 4096;
		RandomAccessFile file = null;
		try {

		    ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			BalanceSerializer.decode(this, channel);
			channel.flush();
		} catch (Exception e) {
			LOG.error("ERROR", e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					LOG.error("ERROR", e);
				}
			}
		}
	}

	public Int2ObjectMap<int[]> getMapFeeDefault() {
		return mapFeeDefault;
	}
	public void clear() {
		balances.clear();		
	}	
}