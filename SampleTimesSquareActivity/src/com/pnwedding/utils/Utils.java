package com.pnwedding.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {
	
	public static Properties getReminderProperties(){
		Properties properties = new Properties();
		InputStream in = Utils.class.getResourceAsStream("/reminders.properties");
		try {
			properties.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}
