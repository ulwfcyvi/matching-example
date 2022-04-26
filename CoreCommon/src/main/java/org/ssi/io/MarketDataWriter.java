package org.ssi.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.ssi.collection.Int2ObjectMap;
import org.ssi.collection.Long2LongMap;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.constant.ScaleConstants;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventType;
import org.ssi.model.MatchingResult;
import org.ssi.model.MatchingType;
import org.ssi.util.SerializeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketDataWriter extends QWriter {
	private static final Logger LOG = LoggerFactory.getLogger(MarketDataWriter.class);
//	static final long[] POWERS_OF_10 = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };
//	public long[] currencyDecimal = new long[1 << 16];
	private Long2ObjectMap<double[]> userFeeMap = new Long2ObjectMap<>();
	private Int2ObjectMap<double[]> brokerFeeMap = new Int2ObjectMap<>();
	// <broker <Map<UserId, usserId>>>
	private Int2ObjectMap<Long2LongMap> brokerToUserIDMaps = new Int2ObjectMap<Long2LongMap>();
	private final int defaultMakerFee = 10;
	private final int defaultTakerFee = 20;
	private MetadataManager metadata;
	
	public MarketDataWriter() {
		// TODO Auto-generated constructor stub
	}
	
	public MarketDataWriter(MetadataManager meta) {
		metadata = meta;
	}
	
	@Override
	public void writeEvent(BaseEvent event) {
		BaseEvent evt = event;
//		int base = BitUtil.getHigh(event.symbol);
		while (evt != null) {
			// only interested in place_order and ADD_SYMBOL event
			if (evt.eventType == EventType.PLACE_ORDER) {
//				LOG.info("MarketDataWriter PLACE_ORDER {}", evt);
				MatchingResult tmp = evt.matchingResult;
				while (tmp != null) {
					if (tmp.type == MatchingType.TRADE) {
//						buffer.writeByte(evt.eventType);
						buffer.writeInt(evt.symbol);
						buffer.writeByte(evt.orderSide);
						buffer.writeLong(evt.userId);
						buffer.writeLong(tmp.makerId);
						buffer.writeLong(tmp.amount);
						buffer.writeLong(tmp.price);
//						buffer.writeLong(tmp.timestamp);
						buffer.writeLong(System.currentTimeMillis());
						
						double[] takerFees = userFeeMap.get(evt.userId);
						double takerFee = takerFees == null? defaultTakerFee / ScaleConstants.FEE_SCALE : takerFees[1];
						buffer.writeDouble(takerFee);
						
						double[] makerFees = userFeeMap.get(tmp.makerId);
						double makerFee = makerFees == null? defaultMakerFee / ScaleConstants.FEE_SCALE : makerFees[0];
						buffer.writeDouble(makerFee);
					}
					tmp = tmp.next;
				}
			} else if (evt.eventType == EventType.ADD_SYMBOL) {
//				buffer.writeByte(evt.eventType);
				buffer.writeInt(evt.symbol);
				buffer.writeByte((byte) evt.eventResult);
				buffer.writeLong(-1);
				buffer.writeLong((evt.orderSide << 16) | (evt.orderType << 8) | (evt.tradeType));
				buffer.writeLong(evt.amount);
				buffer.writeLong(evt.price);
				buffer.writeLong(evt.clientOrderId);
			} else if (evt.eventType == EventType.CHANGE_BALANCE) {
				Long2LongMap userIDs = brokerToUserIDMaps.get((int) event.stopPrice);
				int brokerID = (int) evt.stopPrice;
				if (userIDs == null) {
					userIDs = new Long2LongMap();
					brokerToUserIDMaps.put((int) event.stopPrice, userIDs);

					// set fee default to user
					double[] defaultFeeBroker = brokerFeeMap.get(brokerID);
					if (defaultFeeBroker == null) {
						userFeeMap.put(evt.userId, new double[] {defaultMakerFee, defaultTakerFee});
					} else {
						userFeeMap.put(evt.userId, new double[] {defaultFeeBroker[0], defaultFeeBroker[1]});
					}
				}
				userIDs.put(evt.userId, evt.userId);
//				buffer.writeByte(evt.eventType);
				buffer.writeInt(evt.symbol);
				buffer.writeByte((byte) evt.eventResult);
				buffer.writeLong(evt.userId);
				buffer.writeLong(-1);
				buffer.writeLong(evt.amount);
				buffer.writeInt(brokerID); //brokerId
				buffer.writeInt(evt.orderSide); // type change balance
				buffer.writeLong(evt.price); // withdraw fee
//				buffer.writeLong(evt.timestamp);
				buffer.writeLong(evt.clientOrderId);
			} else if (event.eventType == EventType.CHANGE_FEE) {
				double fees[] = userFeeMap.get(event.userId);
				if (fees == null) {
					fees = new double[2];
					userFeeMap.put(event.userId, fees);
				}
				fees[0] = event.price / ScaleConstants.FEE_SCALE;  //maker_fee
				fees[1] = event.stopPrice / ScaleConstants.FEE_SCALE; //taker_fee
			} else if(evt.eventType == EventType.CHANGE_AFFILIATE_COMMISION) {
//				buffer.writeByte(evt.eventType);
				buffer.writeInt(evt.symbol); // brokerId
				buffer.writeByte((byte) -1);  // order side
				buffer.writeLong(evt.userId); // commision
				buffer.writeLong(-1);
				buffer.writeLong(evt.amount);  // commision
				buffer.writeLong(evt.price);// commision
			} else if(evt.eventType == EventType.ADD_NEW_USER) {
			    // ADD_NEW_USER
			    // brokerId = event.symbol
			    // userId = event.userId
			    // makerFee = event.price
			    // takerFee = event.stopPrice
				LOG.info("MarketDataWriter ADD_NEW_USER {}", evt);
				double fees[] = userFeeMap.get(event.userId);
				
				if (fees == null) {
					fees = new double[2];
					userFeeMap.put(event.userId, fees);
				}
				
				fees[0] = evt.price / ScaleConstants.FEE_SCALE; // makerFee
				fees[1] = evt.stopPrice / ScaleConstants.FEE_SCALE; // takerFee
				
				Long2LongMap userIDs = brokerToUserIDMaps.get(evt.symbol);
				if (userIDs == null) {
					userIDs = new Long2LongMap();
					brokerToUserIDMaps.put(evt.symbol, userIDs);
				}
				userIDs.put(event.userId, event.userId);

//				buffer.writeByte(evt.eventType);
				buffer.writeInt(evt.symbol); // brokerId
				buffer.writeByte((byte)-1);  // order side
				buffer.writeLong(-1); 
				buffer.writeLong(event.userId);
				buffer.writeLong(evt.clientOrderId);  // refUserId				
				buffer.writeLong(evt.timestamp);
			} else if(evt.eventType == EventType.RESET_FEE_BY_BROKER) {
				LOG.info("MarketDataWriter RESET_FEE_BY_BROKER {}", evt);
				Long2LongMap userIDs = brokerToUserIDMaps.get(evt.symbol);
				if (userIDs != null) {
					long[] values = userIDs.values();
					
					for (int i = 0; i < values.length; i++) {
						if (values[i] != Long2LongMap.MISSING_VALUE) {
							double fees[] = userFeeMap.get(values[i]);
							if (fees != null) {
								fees[0] = evt.orderId / ScaleConstants.FEE_SCALE; // makerFee
								fees[1] = evt.clientOrderId / ScaleConstants.FEE_SCALE; // takerFee
							}
						}
					}
				}
				
				// PUT default fee
				if(brokerFeeMap.containsKey(evt.symbol)) {
					double fees[] = brokerFeeMap.get(evt.symbol);
					fees[0] = evt.orderId / ScaleConstants.FEE_SCALE; // makerFee
					fees[1] = evt.clientOrderId / ScaleConstants.FEE_SCALE; // takerFee
				} else {
					double fees[] = new double[2];
					fees[0] = evt.orderId / ScaleConstants.FEE_SCALE; // makerFee
					fees[1] = evt.clientOrderId / ScaleConstants.FEE_SCALE; // takerFee
					brokerFeeMap.put(evt.symbol, fees);
				}
				
//				buffer.writeByte(evt.eventType);
//				buffer.writeInt(evt.symbol); // brokerId
//				buffer.writeByte((byte)-1);  // -1
//				buffer.writeLong(-1);        // -1
//				buffer.writeLong(-1);        // -1
//				buffer.writeLong(evt.orderId);  // makerFee
//				buffer.writeLong(evt.clientOrderId); // takerFee
			} else if(evt.eventType == EventType.APPLYING_RESULTS) {
				buffer.writeInt(-1); //symbol
				buffer.writeByte(evt.eventResult); //orderside
				buffer.writeLong(-1); //taker
				buffer.writeLong(0);//maker
				buffer.writeLong(0); //amount
				
				buffer.writeLong(0); // price
				buffer.writeLong(0); // time
				appender.writeBytes(b -> b
						.write(buffer, 0, buffer.writePosition()));
				buffer.clear();
				
			}else if (evt.eventType == EventType.TAKE_SNAPSHOT) {
				buffer.writeInt(0); //symbol
				buffer.writeByte((byte)-2); //orderside
				buffer.writeLong(0); //taker
				buffer.writeLong(-1);//maker
				buffer.writeLong(metadata.getCurrentIndex()); //amount
				
				buffer.writeLong(0); // price
				buffer.writeLong(0); // time
				appender.writeBytes(b -> b
						.write(buffer, 0, buffer.writePosition()));
			}
			evt = evt.next;
		}
	}
	
	public void loadSnapshot(String filePath) {
		int size = 4096;
		RandomAccessFile file = null;
		try {
		    ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			int nUsers = channel.getInt();
			
			for(int i = 0; i < nUsers; i++) {
				long userId = channel.getLong();
				double[] fees = new double[2];
				
				fees[0] = channel.getDouble();
				fees[1] = channel.getDouble();
				
				userFeeMap.put(userId, fees);
			}
			
			int nBrokers = channel.getInt();
			
			for(int i = 0; i < nBrokers; i++) {
				int brokerId = channel.getInt();
				double[] fees = new double[2];
				
				fees[0] = channel.getDouble();
				fees[1] = channel.getDouble();
				
				brokerFeeMap.put(brokerId, fees);
			}
			
			nBrokers = channel.getInt();
			for (int i = 0; i < nBrokers; i++) {
				int bId = channel.getInt();
				Long2LongMap uIDs = new Long2LongMap();
				brokerToUserIDMaps.put(bId, uIDs);
				SerializeHelper.deserializeLong2LongMap(channel, uIDs);
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

			int nUsers = userFeeMap.size();
			channel.putInt(nUsers);
			long[] rKeys = userFeeMap.keys();
			Object[] values = userFeeMap.values();
			
			for(int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					long k = rKeys[i];
					double[] v = (double[])values[i];
					channel.putLong(k);
					channel.putDouble(v[0]);
					channel.putDouble(v[1]);
				}
			}
			
			int nBrokers = brokerFeeMap.size();
			channel.putInt(nBrokers);
			int[] rBrokerKeys = brokerFeeMap.keys();
			Object[] valueBrokers = brokerFeeMap.values();
			
			for(int i = 0; i < valueBrokers.length; i++) {
				if (valueBrokers[i] != null) {
					int k = rBrokerKeys[i];
					double[] v = (double[])valueBrokers[i];
					channel.putInt(k);
					channel.putDouble(v[0]);
					channel.putDouble(v[1]);
				}
			}
			
			nBrokers = brokerToUserIDMaps.size();
			channel.putInt(nBrokers);
			int[] bKeys = brokerToUserIDMaps.keys();
			Object[] bValues = brokerToUserIDMaps.values();
			
			for (int i = 0; i < bValues.length; i++) {
				if (bValues[i] != null) {
					int bId = bKeys[i];
					channel.putInt(bId);
					Long2LongMap uIDs = (Long2LongMap) bValues[i];
					SerializeHelper.serializeLong2LongMap(channel, uIDs);
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
