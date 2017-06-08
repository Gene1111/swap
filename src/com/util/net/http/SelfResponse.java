package com.util.net.http;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

public class SelfResponse {

	private ChannelHandlerContext ctx;
	private HttpRequest request;
	private boolean write = false;
	
	private String content_type = "text/plain; charset=UTF-8";
	
	public SelfResponse(ChannelHandlerContext ctx, HttpRequest request) {
		this.ctx = ctx;
		this.request = request;
	}
	
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public void write(String msg) {
		writeResponse(HttpResponseStatus.OK, msg, false);
	}

	public boolean isWrite() {
		return write;
	}
	
	public void writeResponse(HttpResponseStatus status, String msg, boolean forceClose){
		boolean keepAlive = HttpUtil.isKeepAlive(request);
		FullHttpResponse response = new DefaultFullHttpResponse(
		        HTTP_1_1, status, Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, content_type);
		if (keepAlive) {
		    response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		ctx.write(response);
        if(keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        write = true;
	}
}
