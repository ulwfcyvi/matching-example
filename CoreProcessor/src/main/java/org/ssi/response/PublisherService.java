package org.ssi.response;

import io.aeron.Aeron;
import io.aeron.Aeron.Context;
import io.aeron.ExclusivePublication;
import io.aeron.Publication;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssi.config.BaseEventConfig;
import org.ssi.response.model.OrderHistoryModel;
import org.ssi.sbe.*;


/*
 * Service request object to aeron data (byte[])
 *  
 */
public class PublisherService {
	private static final Logger LOG = LoggerFactory.getLogger(PublisherService.class);

	public static final int BALANCE_OFFER_MODE = 1;
	public static final int BOOK_OFFER_MODE = 2;
	public static final int TRADE_OFFER_MODE = 3;
	public static final int ORDER_OFFER_MODE = 4;
	public static final int MARKET_OFFER_MODE = 5;
	public static final int RESULT_OFFER_MODE = 6;

	private static final String AERON_MODE_MDC = "MDC";
	private static final String AERON_MODE_MULTICAST = "MULTICAST";
	private static final String AERON_CHANNEL_START = "aeron:udp?";
	private static final String AERON_CHANNEL_CONTROL_MODE = "control-mode=";
	private static final String AERON_CHANNEL_CONTROL = "control=";
	private static final String AERON_CHANNEL_ENPOINT = "endpoint=";
	private static final String AERON_CHANNEL_INTERFACE = "interface=";

	private static final int MAX_RETRY_TIMES = 1000;
	private static final int RETRY_TIMES = 5;

	private int operatingMode = 0;
	private final BusySpinIdleStrategy OFFER_IDLE_STRATEGY = new BusySpinIdleStrategy();

	private Aeron PUBLISHER_AERON;
	private ExclusivePublication BALANCE_MODEL_PUBLICATION;
	private ExclusivePublication OBJECT_IMAGE_PUBLICATION;
	private ExclusivePublication TRADE_EVENT_PUBLICATION;
	private ExclusivePublication ORDER_HISTORY_MODEL_PUBLICATION;
	private ExclusivePublication MARKET_STATS_PUBLICATION;
	private Publication RESULT_MODEL_PUBLICATION;

	private static int messageLimit = 100000000;




	private static OrderHistoryModelEnDeCoder ORDER_HISTORY_MODEL_ENDE_CODER = new OrderHistoryModelEnDeCoder();



	public PublisherService(int mode) {
		this.operatingMode = mode;
		messageLimit = Integer.valueOf(BaseEventConfig.getEnvironment().getProperty("server.stream.aeron.message.limit"));
			connectToAeron();


	}



	// write order history model event to client (vertx)
	public void writeOrderHistoryModel(OrderHistoryModel orderHistoryModel) {
//		if (ResultGlobalValue.IS_APPLY_RESULTS) {
//			LOG.info("writeOrderHistoryModel {}", orderHistoryModel.toString());
			DirectBuffer buffer = ORDER_HISTORY_MODEL_ENDE_CODER.encodeByte(orderHistoryModel);
			int counter = 0;
			while (counter++ < MAX_RETRY_TIMES) {
				long result = ORDER_HISTORY_MODEL_PUBLICATION.offer(buffer, 0, OrderHistoryModelEncoder.BLOCK_LENGTH);
				if (result == Publication.BACK_PRESSURED) {
					OFFER_IDLE_STRATEGY.idle();
				} else {
//					LOG.info("Time send to client {}", System.nanoTime() - orderHistoryModel.clientOrderId);
					break;
				}
			}
//		}
	}


	private static String getMDCChannelOrderHistoryModelPub() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(AERON_CHANNEL_START);
		strBuffer.append(AERON_CHANNEL_CONTROL);
		strBuffer.append(BaseEventConfig.getEnvironment().getProperty("server.stream.aeron.pub.mdc.control.ip"));
		strBuffer.append(":");
		strBuffer.append(BaseEventConfig.getEnvironment().getProperty("server.stream.aeron.pub.mdc.order.history.model.channel.port"));
		strBuffer.append("|");
		strBuffer.append(AERON_CHANNEL_CONTROL_MODE);
		strBuffer.append(BaseEventConfig.getEnvironment().getProperty("server.stream.aeron.pub.mdc.control.mode"));
		return strBuffer.toString();
	}


	private void connectToAeron() {
		int counter = 0;
		boolean keepTrying = true;
		while (counter < RETRY_TIMES && keepTrying) {
				PUBLISHER_AERON = Aeron.connect(new Context());
				switch (this.operatingMode) {

					case ORDER_OFFER_MODE:
						ORDER_HISTORY_MODEL_PUBLICATION = PUBLISHER_AERON.addExclusivePublication(
								getMDCChannelOrderHistoryModelPub(),
								Integer.valueOf(BaseEventConfig.getEnvironment().
										getProperty("server.stream.aeron.pub.mdc.order.history.model.streamid")));
						break;

					default:
						break;
				}
				keepTrying = false;

			counter++;
		}
		if (counter == RETRY_TIMES) {
			LOG.error("Aeron driver is not running!");
		}

	}
}
