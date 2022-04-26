package org.ssi.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.ssi.model.BaseEvent;
import org.ssi.model.EventResult;
import org.ssi.model.EventType;
import org.ssi.model.FeeSide;
import org.ssi.model.MatchingResult;
import org.ssi.model.MatchingType;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderStatus;
import org.ssi.model.OrderType;
import org.ssi.model.TradeOption;
import org.ssi.util.BitUtil;
import org.ssi.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;

public class OrdersDataWriter extends QWriter{    
	private static final Logger LOG = LoggerFactory.getLogger(OrdersDataWriter.class);

	public static int ARR_SIZE = 1 << 16;
	public long[] baseDecimal = new long[ARR_SIZE];
	@SuppressWarnings("rawtypes")
	Bytes tmp = Bytes.elasticByteBuffer(1024 * 128);
	
	@Override
	public void writeEvent(BaseEvent event) {
		int base = BitUtil.getHigh(event.symbol);
		long prevId = 0;
		prevId = writeOneEvent(event, base, prevId);
		
		BaseEvent next = event.next;
		while(next != null) {
			prevId = writeOneEvent(next, base, prevId);

			next = next.next;
		}
	}
	StringBuffer tmpLog = new StringBuffer();
	private long writeOneEvent(BaseEvent event, int base, long prevId) {
		tmp.clear();
		long totalFilled = 0;
		double totalFilledCost = 0;
		
//		StringBuffer log = new StringBuffer();
		if (event.eventType == EventType.PLACE_ORDER) {
			// write order maker
			boolean isMarketCancel = false;
			MatchingResult result = event.matchingResult;
			long filled = 0;
			
			byte orderType = event.orderType;
			// case  OCO
			if (event.tradeOption == TradeOption.OCO) {
				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
					orderType = OrderType.OCO_STOP_MARKET;
				} else {
					orderType = OrderType.OCO_LIMIT;
				}
			}

			long price = event.price;
			if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
				price = 0;
			}

			long matchingPrice = 0;
			if (result != null) {
				while (result != null) {
					if (result.type != MatchingType.CANCEL) {
						byte orderTypeMaker = result.makerOrderType;
						if (result.makerTradeOption == TradeOption.OCO) {
							if (result.makerOrderType == OrderType.MARKET || result.makerOrderType == OrderType.STOP_MARKET || result.makerOrderType == OrderType.OCO_STOP_MARKET) {
								orderTypeMaker = OrderType.OCO_STOP_MARKET;
							} else {
								orderTypeMaker = OrderType.OCO_LIMIT;
							}
						}
						tmp.writeLong(result.makerOrderId);
						tmp.writeLong(result.makerId);
						tmp.writeInt(event.symbol);
						tmp.writeLong(result.timestamp);
						tmp.writeByte(orderTypeMaker);
						tmp.writeByte(event.orderSide == OrderSide.ASK ? OrderSide.BID : OrderSide.ASK);
						
						tmp.writeByte(result.makerFilled == result.makerAmount ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);
						tmp.writeLong(result.price);
						tmp.writeLong(result.amount); // filled
						tmp.writeLong(result.makerFilled); // totalFilled
						tmp.writeLong(result.makerAmount); // place order amount
						tmp.writeLong(result.makerFilled == 0 ? 0 : MathUtils.ceil(result.makerFilledCost / result.makerFilled * baseDecimal[base])); // averagePrice
						tmp.writeLong(result.makerStopPrice);
						tmp.writeLong(0); // clientOrderId
						tmp.writeLong(0); // referId
						tmp.writeLong(result.price); // matchingPrice
						tmp.writeByte(FeeSide.MAKER);// feeSide
						filled = result.amount;
						totalFilledCost += (double) result.amount / baseDecimal[base] * result.price;
						totalFilled += result.amount;
						matchingPrice = result.price;
//						writeLog(event, orderTypeMaker, base, tmpLog, result, prevId, result.price, matchingPrice, FeeSide.MAKER);
					} else {
						isMarketCancel = true;
					}
					
					writeTaker(event, base, prevId, isMarketCancel, price, matchingPrice, filled, totalFilled, totalFilledCost, orderType);
					buffer.write(tmp);
					tmp.clear();
//					LOG.info(tmpLog.toString());
					tmpLog.setLength(0);

					result = result.next;
				}
			} else {
				writeTaker(event, base, prevId, isMarketCancel, price, matchingPrice, filled, totalFilled, totalFilledCost, orderType);
			}

		} else if (event.eventType == EventType.CANCEL_ORDER) {
			MatchingResult result = event.matchingResult;
				
			if (result != null) {
				totalFilled = result.makerFilled;
				totalFilledCost = result.makerFilledCost;
			}
			long average = totalFilled == 0 ? 0 : MathUtils.ceil(totalFilledCost / totalFilled * baseDecimal[base]);

			byte orderType = event.orderType;
			long price = event.price;
			// case  OCO
			if (event.tradeOption == TradeOption.OCO) {
				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
					orderType = OrderType.OCO_STOP_MARKET;
				} else {
					orderType = OrderType.OCO_LIMIT;
				}
			}
			if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
				price = 0;
			}
			buffer.writeLong(event.orderId);
			buffer.writeLong(event.userId);
			buffer.writeInt(event.symbol);
			buffer.writeLong(event.timestamp);
			buffer.writeByte(orderType);
			buffer.writeByte(event.orderSide);
			buffer.writeByte(event.eventResult == EventResult.OK ? OrderStatus.CANCELLED : OrderStatus.REJECT);
			buffer.writeLong(price);// price
			buffer.writeLong(0); // filled
			buffer.writeLong(totalFilled); // totalFilled
			buffer.writeLong(event.amount + totalFilled); // place order amount
			buffer.writeLong(totalFilled == 0 ? 0 : average); // averagePrice
			buffer.writeLong(event.stopPrice);
			buffer.writeLong(event.clientOrderId);
			buffer.writeLong(0); // referId
			buffer.writeLong(0); // matchingPrice
			buffer.writeByte(FeeSide.TAKER);// feeSide
//			writeLog(event, orderType, totalFilled, totalFilledCost, base, 0, event.eventResult == EventResult.OK ? OrderStatus.CANCELLED : OrderStatus.REJECT, prevId, price, 0, FeeSide.TAKER);
		} else if (event.eventType == EventType.CHANGE_BALANCE) {
			// case change balance, add broker
			buffer.writeLong(0);
			buffer.writeLong(event.userId);
			buffer.writeInt(0); // symbol
			buffer.writeLong(0);
			buffer.writeByte(EventType.CHANGE_BALANCE); // ORDER_TYPE: change balance
			buffer.writeByte(OrderSide.ASK);
			buffer.writeByte(OrderStatus.REJECT);
			buffer.writeLong(0); // price
			buffer.writeLong(event.stopPrice == 0 ? 1 : (int) event.stopPrice); // filled
			buffer.writeLong(0); // totalFilled
			buffer.writeLong(0); // place order amount
			buffer.writeLong(0); // averagePrice
			buffer.writeLong(0); // stopPrice
			buffer.writeLong(0); // clientOrderId
			buffer.writeLong(0); // referId
			buffer.writeLong(0); // matchingPrice
			buffer.writeByte(FeeSide.TAKER);// feeSide
		} else if (event.eventType == EventType.ADD_SYMBOL) {
			// case add symbol
			buffer.writeLong(0);
			buffer.writeLong(0);
			buffer.writeInt(event.symbol); // symbol
			buffer.writeLong(0);
			buffer.writeByte(EventType.ADD_SYMBOL); // ORDER_TYPE: ADD_SYMBOL
			buffer.writeByte(OrderSide.ASK);
			buffer.writeByte(OrderStatus.REJECT);
			buffer.writeLong(0); // price
			buffer.writeLong(0); // filled
			buffer.writeLong(0); // totalFilled
			buffer.writeLong(0); // place order amount
			buffer.writeLong(0); // averagePrice
			buffer.writeLong(0); // stopPrice
			buffer.writeLong(0); // clientOrderId
			buffer.writeLong(0); // referId
			buffer.writeLong(0); // matchingPrice
			buffer.writeByte(FeeSide.TAKER);// feeSide
		} else if (event.eventType == EventType.RESET_FEE_BY_BROKER) {
			// case add symbol
			buffer.writeLong(0);
			buffer.writeLong(0);
			buffer.writeInt(event.symbol); // broker
			buffer.writeLong(0);
			buffer.writeByte(EventType.RESET_FEE_BY_BROKER); // ORDER_TYPE: ADD_SYMBOL
			buffer.writeByte(OrderSide.ASK);
			buffer.writeByte(OrderStatus.REJECT);
			buffer.writeLong(0); // price
			buffer.writeLong(0); // filled    -->  
			buffer.writeLong(0); // totalFilled
			buffer.writeLong(0); // place order amount
			buffer.writeLong(0); // averagePrice
			buffer.writeLong(event.orderId); // stopPrice                    // maker RESET_FEE_BY_BROKER: maker 
			buffer.writeLong(event.clientOrderId); // clientOrderId          // taker RESET_FEE_BY_BROKER: taker 
			buffer.writeLong(0); // referId                
			buffer.writeLong(0); // matchingPrice
			buffer.writeByte(FeeSide.TAKER);// feeSide
		} else if (event.eventType == EventType.CHANGE_FEE) {
			// case add symbol
			buffer.writeLong(0);
			buffer.writeLong(event.userId);
			buffer.writeInt(0); // broker
			buffer.writeLong(0);
			buffer.writeByte(EventType.CHANGE_FEE); // ORDER_TYPE: ADD_SYMBOL
			buffer.writeByte(OrderSide.ASK);
			buffer.writeByte(OrderStatus.REJECT);
			buffer.writeLong(0); // price
			buffer.writeLong(0); // filled    -->  
			buffer.writeLong(0); // totalFilled
			buffer.writeLong(0); // place order amount
			buffer.writeLong(0); // averagePrice
			buffer.writeLong(event.price); // stopPrice                  // maker CHANGE_FEE: maker 
			buffer.writeLong(event.stopPrice); // clientOrderId          // taker CHANGE_FEE: taker 
			buffer.writeLong(0); // referId                
			buffer.writeLong(0); // matchingPrice
			buffer.writeByte(FeeSide.TAKER);// feeSide
		}else if(event.eventType == EventType.APPLYING_RESULTS) {
			
			buffer.writeLong(-1);
			buffer.writeLong(-1);
			buffer.writeInt(0); // broker
			buffer.writeLong(0);
			buffer.writeByte(EventType.APPLYING_RESULTS); // ORDER_TYPE: ADD_SYMBOL
			buffer.writeByte(event.eventResult);
			buffer.writeByte(OrderStatus.REJECT);
			buffer.writeLong(0); // price
			buffer.writeLong(0); // filled    -->  
			buffer.writeLong(0); // totalFilled
			buffer.writeLong(0); // place order amount
			buffer.writeLong(0); // averagePrice
			buffer.writeLong(0); // stopPrice                  // maker CHANGE_FEE: maker 
			buffer.writeLong(0); // clientOrderId          // taker CHANGE_FEE: taker 
			buffer.writeLong(0); // referId                
			buffer.writeLong(0); // matchingPrice
			buffer.writeByte(FeeSide.TAKER);
			
			appender.writeBytes(b -> b
					.write(buffer, 0, buffer.writePosition()));
			buffer.clear();	
		}

//		if (log.length() != 0) {
//			LOG.info(log.toString());
//		}
		return event.orderId;
	}

	private void writeTaker(BaseEvent event, int base, long prevId, boolean isMarketCancel, long price, long matchingPrice, long filled, long totalFilled, double totalFilledCost, byte orderType) {
		byte orderStatus = OrderStatus.REJECT;
		if (event.eventResult != EventResult.OK) {
			orderStatus = OrderStatus.REJECT;
			filled = 0;
		} else {
			if (isMarketCancel) {
				orderStatus = OrderStatus.CANCELLED;
				if (totalFilled == 0) {
					filled = 0;
				}
			} else {
				if (totalFilled == 0) {
					orderStatus = OrderStatus.OPEN;
					filled = 0;
				} else if (totalFilled == event.amount) {
					orderStatus = OrderStatus.FILLED;
				} else {
					orderStatus = OrderStatus.PARTIALLY_FILLED;
				}
			}
		}
		long average = totalFilled == 0 ? 0 : MathUtils.ceil((double) totalFilledCost / totalFilled * baseDecimal[base]);
		buffer.writeLong(event.orderId);
		buffer.writeLong(event.userId);
		buffer.writeInt(event.symbol);
		buffer.writeLong(event.timestamp);
		
		buffer.writeByte(orderType);
		buffer.writeByte(event.orderSide);
		buffer.writeByte(orderStatus);
		buffer.writeLong(price);// price
		buffer.writeLong(filled);
		buffer.writeLong(totalFilled); // totalFilled
		buffer.writeLong(event.amount); // place order amount
		buffer.writeLong(totalFilled == 0 ? 0 : average); // averagePrice
		buffer.writeLong(event.stopPrice);
		buffer.writeLong(event.clientOrderId);
		buffer.writeLong(prevId);
		buffer.writeLong(matchingPrice);
		buffer.writeByte(FeeSide.TAKER);// feeSide
		
//		writeLog(event, orderType, totalFilled, totalFilledCost, base, filled, orderStatus, prevId, price, matchingPrice, FeeSide.TAKER);
	}
	
//	private long writeOneEvent(BaseEvent event, int base, long prevId) {
//		tmp.clear();
//		long totalFilled = 0;
//		double totalFilledCost = 0;
//		
//		StringBuffer log = new StringBuffer();
//		if (event.eventType == EventType.PLACE_ORDER) {
//			// write order maker
//			boolean isMarketCancel = false;
//			MatchingResult result = event.matchingResult;
//			long filled = 0;
//			while (result != null) {
//				if (result.type != MatchingType.CANCEL) {
//					byte orderType = result.makerOrderType;
//					if (result.makerTradeOption == TradeOption.OCO) {
//						if (result.makerOrderType == OrderType.MARKET || result.makerOrderType == OrderType.STOP_MARKET || result.makerOrderType == OrderType.OCO_STOP_MARKET) {
//							orderType = OrderType.OCO_STOP_MARKET;
//						} else {
//							orderType = OrderType.OCO_LIMIT;
//						}
//					}
//					tmp.writeLong(result.makerOrderId);
//					tmp.writeLong(result.makerId);
//					tmp.writeInt(event.symbol);
//					tmp.writeLong(result.timestamp);
//					tmp.writeByte(orderType);
//					tmp.writeByte(event.orderSide == OrderSide.ASK ? OrderSide.BID : OrderSide.ASK);
//					
//					tmp.writeByte(result.makerFilled == result.makerAmount ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);
//					tmp.writeLong(result.price);
//					tmp.writeLong(result.amount); // filled
//					tmp.writeLong(result.makerFilled); // totalFilled
//					tmp.writeLong(result.makerAmount); // place order amount
//					tmp.writeLong(result.makerFilled == 0 ? 0 : MathUtils.ceil(result.makerFilledCost / result.makerFilled * baseDecimal[base])); // averagePrice
//					tmp.writeLong(result.makerStopPrice);
//					tmp.writeLong(0); // clientOrderId
//					tmp.writeLong(0); // referId
//					filled = result.amount;
//					totalFilledCost += (double) result.amount / baseDecimal[base] * result.price;
//					totalFilled += result.amount;
//					
//					writeLog(event, orderType, base, log, result, prevId, result.price);
//				} else {
//					isMarketCancel = true;
//				}
//				result = result.next;
//			}
//			
//			byte orderStatus;
//			if (event.eventResult != EventResult.OK) {
//				orderStatus = OrderStatus.REJECT;
//				filled = 0;
//			} else {
//				if (isMarketCancel) {
//					orderStatus = OrderStatus.CANCELLED;
//					if (totalFilled == 0) {
//						filled = 0;
//					}
//				} else {
//					if (totalFilled == 0) {
//						orderStatus = OrderStatus.OPEN;
//						filled = 0;
//					} else if (totalFilled == event.amount) {
//						orderStatus = OrderStatus.FILLED;
//					} else {
//						orderStatus = OrderStatus.PARTIALLY_FILLED;
//					}
//				}
//			}
//			long average = totalFilled == 0 ? 0 : MathUtils.ceil((double) totalFilledCost / totalFilled * baseDecimal[base]);
//			buffer.writeLong(event.orderId);
//			buffer.writeLong(event.userId);
//			buffer.writeInt(event.symbol);
//			buffer.writeLong(event.timestamp);
//			byte orderType = event.orderType;
//			long price = event.price;
//			// case  OCO
//			if (event.tradeOption == TradeOption.OCO) {
//				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
//					orderType = OrderType.OCO_STOP_MARKET;
//				} else {
//					orderType = OrderType.OCO_LIMIT;
//				}
//			}
//			if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
//				price = 0;
//			}
//			
//			buffer.writeByte(orderType);
//			buffer.writeByte(event.orderSide);
//			buffer.writeByte(orderStatus);
//			buffer.writeLong(price);// price
//			buffer.writeLong(filled);
//			buffer.writeLong(totalFilled); // totalFilled
//			buffer.writeLong(event.amount); // place order amount
//			buffer.writeLong(totalFilled == 0 ? 0 : average); // averagePrice
//			buffer.writeLong(event.stopPrice);
//			buffer.writeLong(event.clientOrderId);
//			buffer.writeLong(prevId);
//			buffer.write(tmp);
//			tmp.clear();
//			
//			writeLog(event, orderType, totalFilled, totalFilledCost, base, log, filled, orderStatus, prevId, price);
//		} else if (event.eventType == EventType.CANCEL_ORDER) {
//			MatchingResult result = event.matchingResult;
//			
//			if (result != null) {
//				totalFilled = result.makerFilled;
//				totalFilledCost = result.makerFilledCost;
//			}
//			long average = totalFilled == 0 ? 0 : MathUtils.ceil(totalFilledCost / totalFilled * baseDecimal[base]);
//			
//			byte orderType = event.orderType;
//			long price = event.price;
//			// case  OCO
//			if (event.tradeOption == TradeOption.OCO) {
//				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
//					orderType = OrderType.OCO_STOP_MARKET;
//				} else {
//					orderType = OrderType.OCO_LIMIT;
//				}
//			}
//			if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
//				price = 0;
//			}
//			buffer.writeLong(event.orderId);
//			buffer.writeLong(event.userId);
//			buffer.writeInt(event.symbol);
//			buffer.writeLong(event.timestamp);
//			buffer.writeByte(orderType);
//			buffer.writeByte(event.orderSide);
//			buffer.writeByte(event.eventResult == EventResult.OK ? OrderStatus.CANCELLED : OrderStatus.REJECT);
//			buffer.writeLong(price);// price
//			buffer.writeLong(0); // filled
//			buffer.writeLong(totalFilled); // totalFilled
//			buffer.writeLong(event.amount + totalFilled); // place order amount
//			buffer.writeLong(totalFilled == 0 ? 0 : average); // averagePrice
//			buffer.writeLong(event.stopPrice);
//			buffer.writeLong(event.clientOrderId);
//			buffer.writeLong(0); // referId
//			
//			writeLog(event, orderType, totalFilled, totalFilledCost, base, log, 0, event.eventResult == EventResult.OK ? OrderStatus.CANCELLED : OrderStatus.REJECT, prevId, price);
//		} else if (event.eventType == EventType.CHANGE_BALANCE) {
//			// case change balance, add broker
//			buffer.writeLong(0);
//			buffer.writeLong(event.userId);
//			buffer.writeInt(0); // symbol
//			buffer.writeLong(0);
//			buffer.writeByte(OrderType.LIMIT);
//			buffer.writeByte(OrderSide.ASK);
//			buffer.writeByte(OrderStatus.REJECT);
//			buffer.writeLong(0);
//			buffer.writeLong(event.stopPrice == 0 ? 1 : (int) event.stopPrice); // filled
//			buffer.writeLong(0); // totalFilled
//			buffer.writeLong(0); // place order amount
//			buffer.writeLong(0); // averagePrice
//			buffer.writeLong(0);// 
//			buffer.writeLong(0);
//			buffer.writeLong(0); // referId
//		} else if (event.eventType == EventType.ADD_SYMBOL) {
//			// case add symbol
//			buffer.writeLong(0);
//			buffer.writeLong(0);
//			buffer.writeInt(event.symbol); // symbol
//			buffer.writeLong(0);
//			buffer.writeByte(OrderType.LIMIT);
//			buffer.writeByte(OrderSide.ASK);
//			buffer.writeByte(OrderStatus.REJECT);
//			buffer.writeLong(0);
//			buffer.writeLong(0); // filled
//			buffer.writeLong(0); // totalFilled
//			buffer.writeLong(0); // place order amount
//			buffer.writeLong(0); // averagePrice
//			buffer.writeLong(0);// 
//			buffer.writeLong(0);
//			buffer.writeLong(0); // referId
//		}
//		
//		if (log.length() != 0) {
//			LOG.info(log.toString());
//		}
//		return event.orderId;
//	}

	private void writeLog(BaseEvent event, byte orderType, long totalFilled, double totalFilledCost, int base,
			long filled, byte orderStatus, long referId, long price, long matchingPrice, byte feeSide) {
		StringBuffer log = new StringBuffer();
		log.setLength(0);
		log.append("[");
		log.append(event.orderId).append(",");
		log.append(event.userId).append(",");
		log.append(event.symbol).append(",");
		log.append(event.timestamp).append(",");
		log.append(orderType).append(",");
		log.append(event.orderSide).append(",");
		log.append(orderStatus).append(",");
		log.append(price).append(",");
		log.append(filled).append(",");
		log.append(totalFilled).append(",");
		log.append(event.amount).append(",");
		long average = MathUtils.ceil((double) totalFilledCost / totalFilled * baseDecimal[base]);
		log.append((totalFilled == 0 ? 0 : average)).append(",");
		log.append(event.stopPrice).append(",");
		log.append(event.clientOrderId).append(",");
		log.append(referId).append(",");;
		log.append(matchingPrice).append(",");
		log.append(feeSide); // feeSide
		log.append("]");
		LOG.info(log.toString());
	}

	private void writeLog(BaseEvent event, byte orderType, int base, StringBuffer log, MatchingResult result, long refId, long price, long matchingPrice, byte feeSide) {
		log.setLength(0);
		log.append("[");
		log.append(result.makerOrderId).append(",");
		log.append(result.makerId).append(",");
		log.append(event.symbol).append(",");
		log.append(result.timestamp).append(",");
		log.append(orderType).append(",");
		log.append(event.orderSide == OrderSide.ASK ? OrderSide.BID : OrderSide.ASK).append(",");
		log.append(result.makerFilled == result.makerAmount ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED).append(",");
		log.append(price).append(",");
		log.append(result.amount).append(",");
		log.append(result.makerFilled).append(",");
		log.append(result.makerAmount).append(",");
		long average = MathUtils.ceil((double) result.makerFilledCost / result.makerFilled * baseDecimal[base]);
		log.append((result.makerFilled == 0 ? 0 : average)).append(",");
		log.append(result.makerStopPrice).append(",");
		log.append(0).append(","); // clientOrderId
		log.append(refId).append(","); // referId
		log.append(matchingPrice).append(",");; // matchingPrice
		log.append(feeSide); // feeSide
		log.append("]");
//		LOG.info(log.toString());
	}
	
	public void loadSnapshot(String filePath) {
		int size = 4096;
		RandomAccessFile file = null;
		try {
		    ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			int nCount = channel.getInt();
			for (int i = 0; i < nCount; i++) {
				int index = channel.getInt();
				baseDecimal[index] = channel.getLong();
			}
			
			channel.flush();
		} catch (IOException e) {
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
	
	public void takeSnapshot(String filePath) {
		RandomAccessFile file = null;
		try {
			int size = 4096;

			ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			int nCount = 0;
			for(int i = 0; i < ARR_SIZE; i++) {
				if (baseDecimal[i] != 0L) {
					nCount++;
				}
			}
			channel.putInt(nCount);
			for(int i = 0; i < ARR_SIZE; i++) {
				if (baseDecimal[i] != 0L) {
					channel.putInt(i);
					channel.putLong(baseDecimal[i]);
				}
			}
			
			channel.flush();

		} catch (IOException e) {
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

}