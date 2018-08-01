package com.netty.client;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.netty.server.UserData;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelHandlerAdapter {
	
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	public void channelRead(ChannelHandlerContext ctx,Object msg) {
		try {
			UserData user = (UserData)msg;
			System.out.println("服务器返回的消息： "+ user.getId() + "," + user.getName() + "," + user.getResponseMessage() );
		} finally {
			// TODO: handle finally clause
			ReferenceCountUtil.release(msg);
		}
	}
	
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	}
 
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
}
