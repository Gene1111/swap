package com.util;

public class ClassConfig {
	
	private String jarPath;
	private String className;
	private byte[] bytes;
	
	public ClassConfig(String jarPath) {
		this.jarPath = jarPath;
	}
	
	public String getJarPath() {
		return jarPath;
	}

	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}
