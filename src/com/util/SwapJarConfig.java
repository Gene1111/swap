package com.util;

import java.util.List;

public class SwapJarConfig {

	private String jarPath;
	private List<String> clzss;
	
	public SwapJarConfig(String jarPath, List<String> clzss) {
		this.jarPath = jarPath;
		this.clzss = clzss;
	}
	
	public String getJarPath() {
		return jarPath;
	}
	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}
	public List<String> getClzss() {
		return clzss;
	}
	public void setClzss(List<String> clzss) {
		this.clzss = clzss;
	}
	
	
}
