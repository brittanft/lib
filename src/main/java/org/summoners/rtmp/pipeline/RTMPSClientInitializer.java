package org.summoners.rtmp.pipeline;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.*;

import javax.net.ssl.SSLEngine;

import org.summoners.rtmp.crypto.*;

public class RTMPSClientInitializer extends ChannelInitializer<SocketChannel> {
	
	static final int MIN_BUFFER_SIZE = 0x100;
	static final int MAX_BUFFER_SIZE = 0x100000;
	static final int BUFFER_SIZE = 0x100;
	static final RecvByteBufAllocator ALLOCATOR = new AdaptiveRecvByteBufAllocator(MIN_BUFFER_SIZE, BUFFER_SIZE, MAX_BUFFER_SIZE);

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
		ch.config().setAutoRead(true).setConnectTimeoutMillis(30000).setPerformancePreferences(0, 2, 1).setReceiveBufferSize(BUFFER_SIZE)
			.setRecvByteBufAllocator(ALLOCATOR).setReuseAddress(true).setSendBufferSize(BUFFER_SIZE).setTcpNoDelay(false).setTrafficClass(0x2 | 0x4 | 0x8 | 0x10)
			.setWriteBufferHighWaterMark(MAX_BUFFER_SIZE).setWriteBufferLowWaterMark(MIN_BUFFER_SIZE).setAllocator(PooledByteBufAllocator.DEFAULT);

        SSLEngine engine = SslContextFactory.getClientContext().createSSLEngine();
        engine.setUseClientMode(true);
        
        pipeline.addLast("ssl", new SslHandler(engine))
				.addLast("handshake", new HandshakeHandler(ch))
				.addFirst("timeout", new ReadTimeoutHandler(30))
				.addLast("write_timeout", new WriteTimeoutHandler(30));
    }
}
