package com.util.net.http;

import java.util.HashMap;
import java.util.Map;

public class ServletManager {

	private Map<String, String> handerMap = new HashMap<String, String>();
	
	public void addHandler(String path, String clazz) {
		handerMap.put(path, clazz);
	}
	
	public Servlet getHandler(String path) {
		if(path == null || path.isEmpty() || path.equals("/")) {
			return null;
		}
		String[] parts = path.split("/");
		String handlerClass = handerMap.get(parts[1].split("\\?")[0]);
		if(handlerClass == null) {
			return null;
		}
		try {
			return (Servlet) Class.forName(handlerClass).newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
