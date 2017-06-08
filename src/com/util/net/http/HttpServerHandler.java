package com.util.net.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

public class HttpServerHandler extends ChannelInboundHandlerAdapter{

	private static final String FAVICON_ICO = "/favicon.ico";
    private static final String ERROR = "error";
    
    private ServletManager servletManager;
    
	public HttpServerHandler(ServletManager servletManager) {
		this.servletManager = servletManager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg){
		if(msg instanceof HttpRequest){
			HttpRequest request = (HttpRequest) msg;
			SelfRequest request1 = new SelfRequest(request);
			SelfResponse resopnse1 = new SelfResponse(ctx, request);
			try{
				String uri = request.uri();
				System.out.println(">>uri=" + uri);
				if(uri.equals(FAVICON_ICO)){
					return;
				}
				Servlet servlet = servletManager.getHandler(uri);
				if(servlet == null) {
					throw new Exception("uri not match servlet.");
				}else {
					try{
						servlet.handler(request1, resopnse1);
					} finally {
						request1.waitComplate();
						if(false == resopnse1.isWrite()) {
							resopnse1.writeResponse(HttpResponseStatus.OK, ERROR, true);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				resopnse1.writeResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, ERROR, true);
				
			}finally{
				ReferenceCountUtil.release(msg);
			}
		}else{
			ReferenceCountUtil.release(msg);
		}
	}
    
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
