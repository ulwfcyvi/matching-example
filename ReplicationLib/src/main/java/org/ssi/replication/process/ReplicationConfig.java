package org.ssi.replication.process;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationConfig {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReplicationConfig.class);
	private static Properties configs;
	private static boolean isConfigMaster = true;
	private static boolean isStandAlone = true;
	
	public static synchronized void loadConfigFromClassPath() {
		 try (InputStream input = ReplicationConfig.class.getClassLoader().getResourceAsStream("ha.properties")) {

			 	configs = new Properties();

	            if (input == null) {
	                LOG.error("Unable to find ha.properties");
	                return;
	            }

	            //load a properties file from class path, inside static method
	            configs.load(input);
	            isConfigMaster = Boolean.parseBoolean(configs.getProperty("isMaster","true"));
	            //是独立的
	            isStandAlone = Boolean.parseBoolean(configs.getProperty("isStandAlone","true"));
	            ReplicationProcessor.BATCH_SIZE = Integer.valueOf(configs.getProperty("batchSize"));
//	            System.setProperty("replicated.queuePath", configs.getProperty("replicated.queuePath","./replicated"));
	        } catch (IOException ex) {
	        	LOG.error("Load config ha.properties error",ex);
	        }
	}
	
	public static synchronized void loadConfigFromDisk(String path) {
		 try (InputStream input = new FileInputStream(path)) {

			 	configs = new Properties();	          
	            //load a properties file from class path, inside static method
	            configs.load(input);
	            isConfigMaster = Boolean.parseBoolean(configs.getProperty("isMaster","true"));
	            isStandAlone = Boolean.parseBoolean(configs.getProperty("isStandAlone","true"));
	            ReplicationProcessor.BATCH_SIZE = Integer.valueOf(configs.getProperty("batchSize"));
//	            System.setProperty("replicated.queuePath", configs.getProperty("replicated.queuePath","./replicated"));
	        } catch (IOException ex) {
	        	LOG.error("Load config ha.properties error",ex);
	        }
	}
	
	public static synchronized void loadConfig() {
		 try (InputStream input = new FileInputStream("ha.properties")) {

			 	configs = new Properties();	          
	            //load a properties file from class path, inside static method
	            configs.load(input);
	            isConfigMaster = Boolean.parseBoolean(configs.getProperty("isMaster","true"));
	            isStandAlone = Boolean.parseBoolean(configs.getProperty("isStandAlone","true"));
	            ReplicationProcessor.BATCH_SIZE = Integer.valueOf(configs.getProperty("batchSize"));
//	            System.setProperty("replicated.queuePath", configs.getProperty("replicated.queuePath","./replicated"));
	        } catch (IOException ex) {
	        	LOG.error("Load config ha.properties error",ex);
//	        	loadConfigFromClassPath();
	        }
	}
	
	public static String getConfigValue(String key) {
	    return configs.getProperty(key);
	}
	
	public static String getConfigValue(String key,String defaultVal) {
	    String s =configs.getProperty(key);
	    if (s == null) {
	    	return defaultVal;
	    }
	    
	    return s;
	}
	
	public static boolean isConfigMaster() {
		return isConfigMaster;
	}
	
	public static boolean isStandAlone() {
		return isStandAlone;
	}
	
	public static boolean isReplay() {
		return Boolean.parseBoolean(configs.getProperty("isReplay","false"));
	}
}
