package com.asksunny.ssl.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SecureSocketNettyClient {

	private final String host;
	private final int port;

	public SecureSocketNettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new SecureSocketClientInitializer());

			Channel ch = b.connect(host, port).sync().channel();
			while (true) {
				ch.closeFuture().awaitUninterruptibly();
			}
			

			// Read commands from the stdin.
			/*ChannelFuture lastWriteFuture = null;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			for (;;) {
				String line = in.readLine();
				if (line == null) {
					break;
				}

				// Sends the received line to the server.
				lastWriteFuture = ch.writeAndFlush(line + "\n");
				System.out.println(line + " sent.");
				// If user typed the 'bye' command, wait until the server closes
				// the connection.
				if ("bye".equals(line.toLowerCase())) {
					ch.closeFuture().sync();
					break;
				}
			}

			// Wait until all messages are flushed before closing the channel.
			if (lastWriteFuture != null) {
				lastWriteFuture.sync();
			}*/
		} finally {
			// The connection is closed automatically on shutdown.
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception 
	{
		
		String host = "prod.na2.lol.riotgames.com";
		int port = 2099;
		new SecureSocketNettyClient(host, port).run();
	}
}