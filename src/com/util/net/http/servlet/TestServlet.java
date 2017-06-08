package com.util.net.http.servlet;

import java.util.ArrayList;
import java.util.List;

import com.HelloWorld;
import com.Main;
import com.util.JavaAgent;
import com.util.SwapJarConfig;
import com.util.net.http.SelfRequest;
import com.util.net.http.SelfResponse;
import com.util.net.http.Servlet;


public class TestServlet extends Servlet{

	@Override
	public void handler(final SelfRequest request, SelfResponse resopnse) {
		List<SwapJarConfig> jarConfigs = new ArrayList<SwapJarConfig>();
		
//		List<String> clzss = new ArrayList<String>();
//		clzss.add("com.HelloWorld");
//		SwapJarConfig config = new SwapJarConfig("lib/swapjar.jar", clzss);
		
		SwapJarConfig config = new SwapJarConfig("lib/swapjar.jar", null);
		
		jarConfigs.add(config);
		
		System.out.println("swap before>>>>>>>>>>>>>>>");
		HelloWorld.print();
		JavaAgent.getInst().swap(Main.pid, jarConfigs);
		System.out.println("swap after>>>>>>>>>>>>>>>");
		HelloWorld.print();
	}
}
