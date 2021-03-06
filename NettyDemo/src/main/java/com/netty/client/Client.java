package com.netty.client;

import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;

import com.netty.util.MarshallingCodeCFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class Client {
	
	private static class SingletonHolder {
			static final Client instance = new Client();
	}
	
	public static Client getInstance () {
		return SingletonHolder.instance;
	}
	
	private EventLoopGroup group;
	private Bootstrap b;
	private ChannelFuture cf;
	
	private Client() {
		
		group = new NioEventLoopGroup();
		b = new Bootstrap();
		b.group(group)
		 .channel(NioSocketChannel.class)
		 .handler(new LoggingHandler(LogLevel.INFO))
		 .handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// TODO Auto-generated method stub
				ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
				ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
				
				ch.pipeline().addLast(new ReadTimeoutHandler(5));
				ch.pipeline().addLast(new ClientHandler());
			}
		});
	}
	
	public void connect() {
		try {
			 
			this.cf = b.connect("127.0.0.1",8765).sync();
			System.out.println("远程服务器已经连接，可以进行数据交换。。。");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public ChannelFuture getChannelFutrue() {
		if(this.cf == null) {
			this .connect();
		}
		if(!this.cf.channel().isActive()) {
			this.connect();
		}
		return this.cf;
	}
	
	public static void main(String[] args) throws Exception {
		final Client c = Client.getInstance();
		
		ChannelFuture  cf = c.getChannelFutrue();
		
		for(int i=1;i<=3;i++) {
			UserParam  request = new UserParam(); 
			request.setId("" + i);
			request.setName("pro"+i);
			request.setRequestMessage("数据信息"+i);
			
//			当SS没有交互，就会异步关闭channel
			cf.channel().closeFuture().sync();
			
//			再模拟一次传输
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						
						ChannelFuture cf = c.getChannelFutrue();
						UserParam request = new UserParam();
						request.setId(" " + 4);
						request.setName("pro" + 4);
						request.setRequestMessage("数据信息" + 4);
						
						cf.channel().writeAndFlush(request);
						cf.channel().closeFuture().sync();
						System.out.println("子线程结束。。");
						
					} catch (Exception e) {
						// TODO: handle exception
						
						e.printStackTrace();
					}
				}
				
			}).start();
			System.out.println("断开连接，主线程借宿..");
		}
	}
}
