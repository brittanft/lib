package org.summoners.netty;

import java.util.*;

import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.socket.*;

public class NettyContext extends ChannelHandlerAdapter {

	public NettyContext() {
	}
	
	protected Bootstrap bootstrap;
	private final Collection<Channel> channelSet = new LinkedHashSet<>();

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		synchronized (channelSet) {
			channelSet.add(ctx.channel());
		}
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		synchronized (channelSet) {
			channelSet.remove(ctx.channel());
		}
	}

	protected void close(Channel[] channels) {
		for (Channel channel : channels)
			channel.close();
	}

	public void stop() {
		Channel[] channels = channels();
		for (Channel channel : channels)
			if (channel instanceof ServerSocketChannel)
				channel.close();
		close(channels);
		
		if (bootstrap != null)
			bootstrap.group().shutdownGracefully();
	}

	public void awaitUntilClosed() {
		while (true)
			Arrays.stream(channels()).forEach(c -> c.closeFuture().awaitUninterruptibly());
	}

	public Channel[] channels() {
		Channel[] channels;
		synchronized (channelSet) {
			channels = channelSet.toArray(new Channel[channelSet.size()]);
		}
		return channels;
	}

	public void closeListeners() {
		for (Channel channel : channels())
			if (channel instanceof ServerSocketChannel)
				channel.close();
	}
}
