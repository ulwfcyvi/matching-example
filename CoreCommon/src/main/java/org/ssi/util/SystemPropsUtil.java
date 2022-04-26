package org.ssi.util;

public class SystemPropsUtil {
	static String strLoadSnapshot = System.getProperty("image.loadSnapshot", "false");
	static String strEpochIndex = System.getProperty("image.epochIndex", "-1");
	static String strReplayToEvent = System.getProperty("image.replayToEvent", "-1");
	static String strShardsNumber = System.getProperty("image.shardsNumber", "2");
	static boolean loadSnapshot = Boolean.parseBoolean(strLoadSnapshot);
	static boolean metadataPrepareNextEntry = Boolean.parseBoolean(System.getProperty("metadata.moveToNextEntry", "false"));
	static long replayToEvent = Long.parseLong(strReplayToEvent);
	static long epochIndex = Long.parseLong(strEpochIndex);
	static int shardsNumber = Integer.parseInt(strShardsNumber);
	
	// true means loading from snapshot, false means starting in clean state
	public static boolean getLoadSnapshot() {
		return loadSnapshot;
	}
	
	// -1 means loading from the latest epoc, >=0 means epoc index
	public static long getEpocIndex() {
		return epochIndex;
	}
	
	// event sequence number that the system will be replayed to (events of epoch @epochIndex+1 if loadSnapshot = true, else @epochIndex)
	public static long getReplayToEvent() {
		return replayToEvent;
	}
	
	// shards number of BookResultProcessor
	public static int getShardsNumber() {
		return shardsNumber;
	}
	
	public static boolean metadataPrepareNextEntry() {
		return metadataPrepareNextEntry;
	}
}
