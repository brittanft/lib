package org.summoners.rtmpp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.summoners.rtmpp.pipeline.*;

public class RTMPSNettyClient {

	private final String host;
	private final int port;

	public RTMPSNettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new RTMPSClientInitializer());

			Channel ch = b.connect(host, port).sync().channel();
			while (true)
				ch.closeFuture().awaitUninterruptibly();
		} finally {
			// The connection is closed automatically on shutdown.
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception 
	{
		
		String host = "prod.na2.lol.riotgames.com";
		int port = 2099;
		new RTMPSNettyClient(host, port).run();
	}
}