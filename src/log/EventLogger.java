package log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class EventLogger {
	
	public static final Logger LOGGER = Logger.getLogger(Logger.class.getName());
	private static FileHandler handler;
	
	public static void initLog(String path) {
		try {
			System.out.println(path);
			System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
			handler = new FileHandler(path);
			LOGGER.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		} catch (SecurityException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		} 
	}

}
