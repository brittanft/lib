package com.asksunny.ssl.client;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

import javax.net.ssl.*;

import org.summoners.netty.pipeline.*;
import org.summoners.rtmp.encoding.*;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.socket.*;

public class HandShake extends ChannelHandlerAdapter {
	
	public final SocketChannel channel;

	public HandShake(SocketChannel channel) {
		this.channel = channel;
	}
	
	HandShakeState state = HandShakeState.getFirst();
	
    private static final Logger logger = Logger.getLogger(HandShake.class.getName());

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		logger.log(Level.INFO, "Handshake active!");
		
		ByteBuf buf = ctx.alloc().ioBuffer(1537).order(ByteOrder.BIG_ENDIAN);
		buf.writeByte(0x03);
		
		//ch.write(buf);
		//buf.clear();
		
		long timeStamp = System.currentTimeMillis();
		buf.writeInt((int) timeStamp);
		buf.writeInt(0);
		
		//ch.write(buf);
		//buf.clear();
		
		data = new byte[1528];
		new Random().nextBytes(data);
		buf.writeBytes(data);

		logger.log(Level.INFO, "Buf finished!");
		ctx.writeAndFlush(buf);
		logger.log(Level.INFO, "buf: " + buf.capacity() + ", " + (buf.capacity() - buf.writableBytes()));
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		logger.log(Level.INFO, "Handshake removed.");
		// channel.pipeline().addAfter("bufferer", "decoder", new AMF3Decoder());
		//channel.pipeline().addBefore("handler", "decoder", new AMF3Decoder());
		
		//channel.pipeline().addBefore("decoder", "timeout", new ReadTimeoutHandler(5 * 60))
		//	.addAfter("decoder", "write_timeout", new WriteTimeoutHandler(5 * 60));
		
		channel.pipeline().addLast("encoder", new AMF3ConnectEncoder());
		ObjectMap params = new ObjectMap();
		params.put("app", "");
		params.put("flashVer", "WIN 10,1,85,3");
		params.put("swfUrl", "app:/mod_ser.dat");
		params.put("tcUrl", "rtmps://prod.na2.lol.riotgames.com:2099");
		params.put("fpad", false);
		params.put("capabilities", 239);
		params.put("audioCodecs", 3191);
		params.put("videoCodecs", 252);
		params.put("videoFunction", 1);
		params.put("pageUrl", null);
		params.put("objectEncoding", 3);
	
		ByteBuf out = ctx.alloc().ioBuffer();
		((AMF3ConnectEncoder) channel.pipeline().get("encoder")).encode(ctx, params, out);
	
		ctx.writeAndFlush(out);
		//ChannelFuture future = ctx.writeAndFlush(out);
		//future.syncUninterruptibly().await();
		
		SslHandler2 handler = channel.pipeline().get(SslHandler2.class);
		System.out.println("Donezo?: " + handler.engine().isInboundDone());
		
		ctx.pipeline().channel().config().setAutoRead(true);
		/*
		 * for (int i = 0; i != 100; ++i) { ctx.read(); Thread.sleep(1000L); }
		 */
	}
	
	public byte[] data;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
		logger.log(Level.INFO, "state: " + state.name());
		state = state.progress(ctx, in, this);
	}
	
	private int timestamp;
	
	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

}
