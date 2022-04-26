package org.ssi.io;

import java.io.File;

import net.openhft.chronicle.bytes.MappedFile;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.ssi.model.BaseEvent;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;

public abstract class QWriter {
	static final String BASE_DIR = "/tmp/coreprocessor/";
    static final int BUFFER_SIZE = 1024 * 2048;
    static final int MAX_BUFFER_SIZE = BUFFER_SIZE - 30000; //must be smaller than BUFFER_SIZE
    
	ChronicleQueue queue;
	ExcerptAppender appender;
	StringBuilder sb = new StringBuilder(256);
	
	@SuppressWarnings("rawtypes")
	Bytes buffer;
	Thread pretoucher;

	// topic naming convention: / + topicName
    public void initQueue(long epocIndex, String topic) {
		MappedFile.warmup();
    	generateFolderName(epocIndex);
		sb.append(topic);

		File directory = new File(sb.toString());
        if (!directory.exists()){
            directory.mkdirs();
        }


		SingleChronicleQueueBuilder builder = SingleChronicleQueueBuilder
				.fieldlessBinary(directory)
//				.writeBufferMode(BufferMode.Asynchronous)
				.blockSize(128 << 20);
		queue = builder.build();
//        queue = ChronicleQueue.singleBuilder(sb.toString()).blockSize(128 << 20).build();
        appender = queue.acquireAppender();
        buffer = Bytes.elasticByteBuffer(BUFFER_SIZE);

		appender.pretouch();
//		pretoucher = new Thread(() -> {
//			try{
//				while (true) {
//					Thread.sleep(50);
//					appender.pretouch();
//				}
//			} catch (InterruptedException ie) {
//
//			}
//		});
//		pretoucher.setDaemon(true);
//		pretoucher.start();
    }
    
    private void generateFolderName(long index) {
        sb.delete(0, sb.length());
        sb.append(BASE_DIR);
        sb.append(index);
        sb.append("/results");
    }
    
    public static String removeData(long epocIndex, String topic) { 
    	StringBuilder sb = new StringBuilder(1 << 6);
    	sb.append(BASE_DIR).append(epocIndex).append("/results").append(topic);
    	
    	String dirPath = sb.toString();
    	File directory = new File(dirPath);
    	if(directory.exists()) {
    		File[] files = directory.listFiles();
    		
    		for(File file : files) {
//    			try {
    				file.delete();
//    			} catch (IllegalAccessError ex) {
//    				ex.printStackTrace();
//    			}
    		}
    	}
    	
    	return dirPath;
    }
    
    public void close() {
    	if (queue != null) {
    		queue.close();
    	}
    }
    
    public abstract void writeEvent(BaseEvent event);
    
	public void write(BaseEvent event, long sequence, Boolean endOfBatch) {
		writeEvent(event);
        if (endOfBatch || buffer.writePosition() >= MAX_BUFFER_SIZE) {
        	appender.writeBytes(b -> b
        			.write(buffer, 0, buffer.writePosition()));
            buffer.clear();
			appender.pretouch();
        }
	}	
}
