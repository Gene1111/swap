package com.util.net.http;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.util.net.http.util.HttpClassConfig;
import com.util.net.http.util.HttpClazzConfigLoader;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class HttpServer {
	
	private final Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private final int port;
	private final String configPath;
	private int bossNum = 1;
	private int workNum = Runtime.getRuntime().availableProcessors() * 2 + 1;
	private boolean ssl = false;
	private SslContext sslCtx;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workGroup;
	
	public HttpServer(int port, String configPath) {
		this.port = port;
		this.configPath = configPath;
	}
	
	public boolean start() {
		ServletManager servletManager = loadServlet();
		if(servletManager == null) {
			return false;
		}
		
		try {
			if(ssl) {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx =  SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			}
			bossGroup = new NioEventLoopGroup(bossNum);
			workGroup = new NioEventLoopGroup(workNum);
			
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new HttpChannleInitializer(sslCtx, servletManager));
			serverBootstrap.bind(port).sync();
			logger.error("init http server scuess. port:" + port);
			return true;
		} catch (Exception e) {
			logger.error("init http server fail. message:" + e.getMessage());
		}
		return false;
	}
	
	private ServletManager loadServlet() {
		if(configPath == null || configPath.isEmpty()) {
			logger.error("init HttpClassConfig config file fialed., configPath is null");
			return null;
		}
		List<HttpClassConfig> configs = HttpClazzConfigLoader.load(configPath);
		if(configs == null) {
			return null;
		}
		ServletManager servletManager = new ServletManager();
		for(HttpClassConfig c : configs) {
			servletManager.addHandler(c.getPath(), c.getClazz());
			System.out.println("load servlet:" + c.toString());
		}
		return servletManager;
	}
	
	public void stop() {
		bossGroup.shutdownGracefully();
		workGroup.shutdownGracefully();
	}
}
