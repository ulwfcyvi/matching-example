package org.ssi.io;

import java.io.File;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

public class QReader {
	static final String BASE_DIR = "/tmp/coreprocessor/";
    static final int BUFFER_SIZE = 1024 * 2048;
    static final int MAX_BUFFER_SIZE = BUFFER_SIZE - 10000; //must be smaller than BUFFER_SIZE
    
	ChronicleQueue queue;
	ExcerptTailer tailer;
	StringBuilder sb = new StringBuilder(256);
	
	@SuppressWarnings("rawtypes")
	Bytes buffer;
    
    // topic naming convention: / + topicName
    public QReader(long epocIndex, String topic) {
    	generateFolderName(epocIndex);
        sb.append(topic);
        File directory = new File(sb.toString());
        if (!directory.exists()){
            directory.mkdirs();
        }

        SingleChronicleQueueBuilder builder = SingleChronicleQueueBuilder
                .fieldlessBinary(directory)
                .blockSize(128 << 20);
        queue = builder.build();
//        queue = ChronicleQueue.singleBuilder(sb.toString()).build();
        tailer = queue.createTailer();
        buffer = Bytes.elasticByteBuffer(BUFFER_SIZE);
    }
    
    public void generateFolderName(long index) {
        sb.delete(0, sb.length());
        sb.append(BASE_DIR);
        sb.append(index);
        sb.append("/results");
    }
    
    public void close() {
    	if (queue != null) {
    		queue.close();
    	}
    }
    
	@SuppressWarnings("rawtypes")
	public Bytes readBytes() {
		buffer.clear();
		if (!tailer.readBytes(buffer)) {
			return null;
		}
		return buffer;
	}	
}
