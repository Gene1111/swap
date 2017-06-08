package com;

import com.util.net.http.HttpServer;
import sun.management.ManagementFactoryHelper;

public class Main {

	public static String pid;
	
	public static void main(String[] args) {
		HttpServer http = new HttpServer(8009, "config/http_class.xml");
		http.start();
		String name = ManagementFactoryHelper.getRuntimeMXBean().getName(); 
		pid = name.split("@")[0];
	}
}
 