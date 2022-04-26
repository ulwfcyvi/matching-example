package org.ssi.io;

import org.ssi.model.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BalanceDataWriter extends QWriter{
	private static final Logger LOG = LoggerFactory.getLogger(BalanceDataWriter.class);
	public void initialize(long epocIndex) {
		initQueue(epocIndex, "/balance");
	}

	@Override
	public void writeEvent(BaseEvent event) {		
	}

	public void writeSingleEvent(long userId, long userCurrencyId, long userAmount, long userReserveAmount, long sequence, boolean endOfBatch) {
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId, userCurrencyId, userAmount, userReserveAmount);
		buffer.writeLong(userId);
		buffer.writeLong(userCurrencyId);
		buffer.writeLong(userAmount);
		buffer.writeLong(userReserveAmount);
		if (endOfBatch || buffer.writePosition() >= MAX_BUFFER_SIZE) {
        	appender.writeBytes(b -> b
        			.write(buffer, 0, buffer.writePosition()));
            buffer.clear();
        }
	}

	public void write2Event(
			long userId1, long currId1, long amount1, long reserve1,
			long userId2, long currId2, long amount2, long reserve2,
			long sequence, boolean endOfBatch
			) {
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId1, currId1, amount1, reserve1);
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId2, currId2, amount2, reserve2);
		buffer.writeLong(userId1);
		buffer.writeLong(currId1);
		buffer.writeLong(amount1);
		buffer.writeLong(reserve1);
		buffer.writeLong(userId2);
		buffer.writeLong(currId2);
		buffer.writeLong(amount2);
		buffer.writeLong(reserve2);
		if (endOfBatch || buffer.writePosition() >= MAX_BUFFER_SIZE) {
        	appender.writeBytes(b -> b
        			.write(buffer, 0, buffer.writePosition()));
            buffer.clear();
        }
	}

	public void write4Event(
			long userId1, long currId1, long amount1, long reserve1,
			long userId2, long currId2, long amount2, long reserve2,
			long userId3, long currId3, long amount3, long reserve3,
			long userId4, long currId4, long amount4, long reserve4,
			long sequence, boolean endOfBatch
			) {
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId1, currId1, amount1, reserve1);
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId2, currId2, amount2, reserve2);
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId3, currId3, amount3, reserve3);
//		LOG.info("___BalanceXX: {}, {}, {}, {}", userId4, currId4, amount4, reserve4);
		buffer.writeLong(userId1);
		buffer.writeLong(currId1);
		buffer.writeLong(amount1);
		buffer.writeLong(reserve1);
		buffer.writeLong(userId2);
		buffer.writeLong(currId2);
		buffer.writeLong(amount2);
		buffer.writeLong(reserve2);
		buffer.writeLong(userId3);
		buffer.writeLong(currId3);
		buffer.writeLong(amount3);
		buffer.writeLong(reserve3);
		buffer.writeLong(userId4);
		buffer.writeLong(currId4);
		buffer.writeLong(amount4);
		buffer.writeLong(reserve4);
		if (endOfBatch || buffer.writePosition() >= MAX_BUFFER_SIZE) {
			appender.writeBytes(b -> b
					.write(buffer, 0, buffer.writePosition()));
			buffer.clear();
		}
	}
}
