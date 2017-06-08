package com.util.net.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

public class SelfRequest {
	
	private final Logger logger = LoggerFactory.getLogger(SelfRequest.class);
	
	private static final Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:+(\\d+)");  
	
	private HttpRequest request;
	private HttpMethod method;
	private HttpHeaders headers;
	
	private String contentType;
	private String ip;
	private int port = 0;
	private Map<String, String> params;
	private String content;
	private String uri;
	
	private CountDownLatch latch;  
	
	public SelfRequest(HttpRequest request) {
		this.request = request;
	}

	public HttpMethod getMethod() {
		if(method == null) {
			method = request.method();
			System.out.println("method=" + method);
		}
		return method;
	}
	
	public HttpHeaders getHeaders() {
		if(headers == null) {
			headers = request.headers();
		}
		return headers;
	}
	
	public String getIp(){
		if(ip == null) {
			parseHost();
		}
		return ip;
	}
	
	public int getPort(){
		if(port == 0) {
			parseHost();
		}
		return port;
	}
	
	public String getContentType(){
		if(contentType == null) {
			HttpHeaders headers = getHeaders();
			String typeStr = headers.get("Content-Type");
			if(typeStr != null && false == typeStr.isEmpty()) {
				String[] list = typeStr.split(";");
				contentType = list[0];
				System.out.println("contentType=" + contentType);
			}
		}
		return contentType;
	}
	
	public String getUri() {
		if(uri == null) {
			uri = request.uri();
		}
		return uri;
	}
	
	public String getParam(String key) {
		try {
			Map<String, String> pm = getParams();
			if(pm != null) {
				return pm.get(key);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getParam error. mes:" + e.getMessage());
		}
		return null;
	}
	
	public Map<String, String> getParams() throws IOException {
		if(params == null) {
			params = new HashMap<String, String>();
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
			Map<String, List<String>> requestParams = queryStringDecoder.parameters();
			if (!requestParams.isEmpty()) {
				for (Entry<String, List<String>> p: requestParams.entrySet()) {
					String key = p.getKey();
					List<String> vals = p.getValue();
					for (String val : vals) {
						params.put(key, val);
					}
				}
			}
		}
		return params;
	}
	
	public String getConetnt() {
		if(content == null) {
			if (request instanceof HttpContent) {
				HttpContent httpContent = (HttpContent) request;
				ByteBuf content1 = httpContent.content();
				if (content1.isReadable()) {
					content = content1.toString(CharsetUtil.UTF_8);
				}
			}
		}
		return content;
	}
	
	public void sync() {
		latch = new CountDownLatch(1);  
	}
	
	public void complate() {
		latch.countDown();
	}
	
	public void waitComplate() throws InterruptedException {
		if(latch != null) {
			latch.await();
		}
	}
	
	private void parseHost() {
		HttpHeaders headers = getHeaders();
		String page = headers.get("Host").toString();
		Matcher m = p.matcher(page);  
		this.ip = m.group(1);
		this.port = Integer.parseInt(m.group(2));
	}
	
}
