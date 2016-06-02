package com.asksunny.ssl.client;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.*;

import javax.net.ssl.SSLEngine;

import com.asksunny.ssl.SecureSocketSslContextFactory;


public class SecureSocketClientInitializer extends ChannelInitializer<SocketChannel> {
	
	static final int MIN_BUFFER_SIZE = 0x100;
	static final int MAX_BUFFER_SIZE = 0x100000;
	static final int BUFFER_SIZE = 0x100;
	static final RecvByteBufAllocator ALLOCATOR = new AdaptiveRecvByteBufAllocator(MIN_BUFFER_SIZE, BUFFER_SIZE, MAX_BUFFER_SIZE);

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //ch.config().setAutoRead(true).setConnectTimeoutMillis(30000).setTcpNoDelay(true).setAllocator(PooledByteBufAllocator.DEFAULT);
        
		ch.config().setAutoRead(true).setConnectTimeoutMillis(30000).setPerformancePreferences(0, 2, 1).setReceiveBufferSize(BUFFER_SIZE)
			.setRecvByteBufAllocator(ALLOCATOR).setReuseAddress(true).setSendBufferSize(BUFFER_SIZE).setTcpNoDelay(false).setTrafficClass(0x2 | 0x4 | 0x8 | 0x10)
			.setWriteBufferHighWaterMark(MAX_BUFFER_SIZE).setWriteBufferLowWaterMark(MIN_BUFFER_SIZE).setAllocator(PooledByteBufAllocator.DEFAULT);
        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.

        SSLEngine engine = SecureSocketSslContextFactory.getClientContext().createSSLEngine();
        engine.setUseClientMode(true);
        SslHandler handler = new SslHandler(engine);
		handler.setSingleDecode(false);
		
		handler.setCumulator(ByteToMessageDecoder.COMPOSITE_CUMULATOR);

        pipeline.addLast("ssl", handler);
        //pipeline.addLast("bufferer", new InputBufferer()).addAfter("bufferer", "handshake", new HandShake(ch));
		pipeline.addLast("handshake", new HandShake(ch));
        pipeline.addFirst("timeout", new ReadTimeoutHandler(30)).addLast("write_timeout", new WriteTimeoutHandler(30));
    }
}
