package org.ssi.replication.process;

import net.openhft.chronicle.bytes.Bytes;

public interface IPublishData {
	public void publishData(Bytes<?> data);
}
