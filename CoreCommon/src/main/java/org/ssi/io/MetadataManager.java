package org.ssi.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.ssi.exception.InvalidMetadataException;
import org.ssi.exception.UnknowMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataManager {
	private static final Logger LOG = LoggerFactory.getLogger(MetadataManager.class); 
	static final String BASE_DIR = "/tmp/coreprocessor/";
	static final String SNAPSHOT_FOLDER = "/snapshots/";
	// core snapshots
	static final String BALANCE_SNAPSHOT_FILENAME = "balances";
	static final String MATCHING_ENGINE_SNAPSHOT_FILENAME = "matching_engine";
	static final String BASE_DECIMALS_SNAPSHOT_FILENAME = "base_decimals";
	static final String FEES_MAP_SNAPSHOT_FILENAME = "fee_map";
	// result snapshots
	static final String MARKET_DATA_SNAPSHOT_FILENAME = "market_data";
	static final String MARKET_STATS_SNAPSHOT_FILENAME = "market_stats";
	static final String BALANCE_DB_SNAPSHOT_FILENAME = "balance_db";
	static final String ORDER_REALTIME_SNAPSHOT_FILENAME = "order_realtime";
	static final String ORDER_DB_SNAPSHOT_FILENAME = "order_db";
	static final String BOOK_IMAGE_SNAPSHOT_FILENAME = "book_image";
	static final String REPLICATION_FILENAME = "replicated";

	public static final int BAlANCE_SNAPSHOT = 1;
	public static final int MATCHING_ENGINE_SNAPSHOT = 2;
	public static final int BASE_DECIMALS_SNAPSHOT = 3;
	public static final int FEES_MAP_SNAPSHOT = 4;
	public static final int MARKET_DATA_SNAPSHOT = 5;
	public static final int MARKET_STATS_SNAPSHOT = 6;
	public static final int BALANCE_DB_SNAPSHOT = 7;
	public static final int ORDER_REALTIME_SNAPSHOT = 8;
	public static final int ORDER_DB_SNAPSHOT = 9;
	public static final int BOOK_IMAGE_SNAPSHOT = 10;
	public static final int REPLICATION_SNAPSHOT = 11;

	static final String METADATA_FILENAME = "metadata";
	static final byte FILE_VERSION = 0x01;
	static final int BYTE_SIZE = 1;
	static final int LONG_SIZE = 8;
	static final int HEADER_OFFSET = BYTE_SIZE * 2 + LONG_SIZE * 2;
	static final int SNAPSHOT_ENTRY_SIZE = LONG_SIZE * 3 + BYTE_SIZE; // started at, finished at, total events, status
	private static final StringBuilder sb = new StringBuilder(256);

	private RandomAccessFile raf;

	private long curIndex = 0;
	private long totalEpocs = 0;
	private byte status = 0; // 0 means OK, 1 means unfinished/running

	public MetadataManager(boolean prepNextEntry) {
		sb.delete(0, sb.length());
		sb.append(BASE_DIR);

		File directory = new File(sb.toString());
		if (!directory.exists()) {
			directory.mkdirs();
		}

		sb.append(METADATA_FILENAME);
		try {
			prepareMetadataFile(prepNextEntry);
		} catch (IOException e) {
			e.printStackTrace();
		}
		createFolders();
	}

	public void openFile() throws IOException {
		if (raf != null) {
			raf.close();
		}
		raf = new RandomAccessFile(sb.toString(), "rw");
	}

	public void openMetadataFile() throws IOException {
		sb.delete(0, sb.length());
		sb.append(BASE_DIR);
		sb.append(METADATA_FILENAME);
		openFile();
	}

	public String getSnapshotFolder(int type, long index) {
		sb.delete(0, sb.length());
		sb.append(BASE_DIR);
		sb.append(index);
		sb.append(SNAPSHOT_FOLDER);
		
		switch (type) {
		case BAlANCE_SNAPSHOT:
			sb.append(BALANCE_SNAPSHOT_FILENAME);
			break;
		case MATCHING_ENGINE_SNAPSHOT:
			sb.append(MATCHING_ENGINE_SNAPSHOT_FILENAME);
			break;
		case BASE_DECIMALS_SNAPSHOT:
			sb.append(BASE_DECIMALS_SNAPSHOT_FILENAME);
			break;
		case FEES_MAP_SNAPSHOT:
			sb.append(FEES_MAP_SNAPSHOT_FILENAME);
			break;
		case MARKET_DATA_SNAPSHOT:
			sb.append(MARKET_DATA_SNAPSHOT_FILENAME);
			break;
		case MARKET_STATS_SNAPSHOT:
			sb.append(MARKET_STATS_SNAPSHOT_FILENAME);
			break;
		case BALANCE_DB_SNAPSHOT:
			sb.append(BALANCE_DB_SNAPSHOT_FILENAME);
			break;
		case ORDER_REALTIME_SNAPSHOT:
			sb.append(ORDER_REALTIME_SNAPSHOT_FILENAME);
			break;
		case ORDER_DB_SNAPSHOT:
			sb.append(ORDER_DB_SNAPSHOT_FILENAME);
			break;
		case BOOK_IMAGE_SNAPSHOT:
			sb.append(BOOK_IMAGE_SNAPSHOT_FILENAME);
			break;
		case REPLICATION_SNAPSHOT:
			sb.append(REPLICATION_FILENAME);
			break;
		default:
			break;
		}
		return sb.toString();
	}

	public String getSnapshotFolder(int type) {
		return getSnapshotFolder(type, curIndex);
	}
	public void createFolders() {
		sb.delete(0, sb.length());
		sb.append(BASE_DIR);
		sb.append(curIndex);
		sb.append(SNAPSHOT_FOLDER);

		File directory = new File(sb.toString());
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	public void prepareMetadataFile(boolean prepNextEntry) throws IOException{
		File f = new File(sb.toString());
		boolean existed = f.exists();

		openFile();

		if (existed && raf.length() >= HEADER_OFFSET) {
			// validate the metadata file
			byte fileVersion = raf.readByte();

			if (fileVersion > 0x01) {
				throw new IOException("Unknown file version: " + fileVersion);
			}

			curIndex = raf.readLong();
			totalEpocs = raf.readLong();

			if (curIndex >= totalEpocs) {
				throw new IOException("Current epoc index is bigger than total number of epocs");
			}

			status = raf.readByte();

			if (status == 1 && prepNextEntry) {
//				throw new UnfinishedStateException("Unfinished state detected!");
				LOG.error("Metadata: unfinished state detected!");
			}

			if (prepNextEntry) {				
				moveToTheNextEntry();
				writeEmptyEntry();
				LOG.info("Metadata: moved to next entry {}",curIndex);
			}

		} else {
			// first time running
			if (prepNextEntry) {
				raf.writeByte(FILE_VERSION);
				raf.writeLong(0);
				totalEpocs = 1;
				raf.writeLong(1);
				raf.writeByte(1); // unfinished
				writeEmptyEntry();
			}
		}

		raf.close();
	}

	public long getCurrentIndex() {
		return curIndex;
	}

	public void finish(long finishedAt, long totalEvents) throws IOException {

		openMetadataFile();

		long offset = HEADER_OFFSET + curIndex * SNAPSHOT_ENTRY_SIZE;

		// edit the metadata
		raf.seek(BYTE_SIZE + LONG_SIZE * 2);
		raf.writeByte(0);

		// fill the entry
		raf.seek(offset + LONG_SIZE);
		raf.writeLong(finishedAt);
		raf.writeLong(totalEvents);
		raf.writeByte(0);

		raf.close();
	}

	public void moveToTheNextEntry() throws IOException {
		long nextEntryOffset = HEADER_OFFSET + totalEpocs * SNAPSHOT_ENTRY_SIZE;
		// increase the current index
		raf.seek(BYTE_SIZE);
		raf.writeLong(++curIndex);
		raf.writeLong(++totalEpocs);
		raf.writeByte(1);
		// move offset to the new entry
		raf.seek(nextEntryOffset);
	}

	public void writeEmptyEntry() throws IOException {
		// write empty entry with startedAt timestamp only
		raf.writeLong(System.currentTimeMillis());
		raf.writeLong(0);
		raf.writeLong(0);
		raf.writeByte(1); // unfinished
	}

	public void rollback() throws IOException {
		openMetadataFile();
		raf.seek(BYTE_SIZE);
		raf.writeLong(--curIndex);
		raf.writeLong(totalEpocs);
		raf.writeByte(1);
		raf.close();
	}

	public void traverse() throws IOException {
		openMetadataFile();

		byte version = raf.readByte();
		long curIndex = raf.readLong();
		long nEpocs = raf.readLong();
		byte status = raf.readByte();
		System.out.println("File version: " + String.format("0x%02X", version));
		System.out.println("Current index: " + curIndex);
		System.out.println("Total epocs: " + nEpocs);
		System.out.println("Current Status: " + (status == 0 ? "OK" : "Unfinished"));
		System.out.println("-------------------------------------");
		for (int i = 0; i < nEpocs; i++) {
			long startedAt = raf.readLong();
			long finishedAt = raf.readLong();
			long nEvents = raf.readLong();
			byte entryStatus = raf.readByte();
			System.out.println("Epoc " + i + ":");
			System.out.println("Start time: " + startedAt);
			System.out.println("Finish time: " + finishedAt);
			System.out.println("Total events: " + nEvents);
			System.out.println("Status: " + (entryStatus == 0 ? "OK" : "Unfinished"));
			System.out.println("-------------------------------------");
		}
		raf.close();
	}
}
