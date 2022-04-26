package org.ssi.service;

import java.io.IOException;

import org.ssi.io.MetadataManager;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {
    
	private static MetadataManager metaManager = new MetadataManager(true);
	
	public MetadataManager getManager() {
		return metaManager;
	}
	
	public long getEpocIndex() {
		return metaManager.getCurrentIndex();
	}
	
	public void finish(long timestamp, long nEvents) throws IOException {
		metaManager.finish(timestamp, nEvents);
	}
	public static void main(String[] args) throws Exception {
//		metaManager = new MetadataManager();
		

//		
//		metaManager.finish(System.currentTimeMillis(), 234234);
		metaManager.traverse();
	}
}