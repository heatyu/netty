package com.netty.util;


import java.io.InputStream;
import java.util.Properties;

public class ReadFromProperties {
	private static final String GLOBAL_CONFIG_FILE = "netty.properties";
	private static Properties globalConf;
	
	public static Properties getProperties() {
		try {
			globalConf = new Properties();
			InputStream inputStream = ReadFromProperties.class.getClassLoader().getResourceAsStream(GLOBAL_CONFIG_FILE);
			globalConf.load(inputStream);
			return globalConf;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
