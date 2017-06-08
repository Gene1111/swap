package com.util.net.http.util;

/**
 * servlet器配置
 **/
public class HttpClassConfig {
	private String path;
	private String clazz;

	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getClazz() {
		return clazz;
	}


	public void setClzss(String clzss) {
		this.clazz = clzss;
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("HttpClassConfig:");
		sb.append("path=").append(path).append("|");
		sb.append("clzss=").append(clazz);
		return sb.toString();
	}
}