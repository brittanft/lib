package com.asksunny.ssl.client;

import java.io.*;
import java.util.logging.*;

import org.summoners.netty.pipeline.*;
import org.summoners.util.*;

import io.netty.buffer.*;
import io.netty.channel.*;

public enum HandShakeState {
	VERSION() {
		@Override
		protected boolean process(ChannelHandlerContext ctx, ByteBuf in, HandShake h) throws Exception {
			Logger.getLogger(HandShakeState.class.getName()).log(Level.INFO, "state: " + name() + ", r:" + in.readableBytes());
			return in.readByte() == 0x03;
		}
	},
	CERT(1536) {
		@Override
		protected boolean process(ChannelHandlerContext ctx, ByteBuf in, HandShake h) throws Exception {
			Logger.getLogger(HandShakeState.class.getName()).log(Level.INFO, "state: " + name() + ", r:" + in.readableBytes());
			h.setTimestamp(in.readInt());
			int unknown = in.readInt();
			System.out.println("Unk: " + unknown);
			byte[] certification = new byte[1528];
			in.readBytes(certification);
			
			ByteBuf buf = ctx.alloc().ioBuffer(4 + 4 + 1528);
			buf.writeInt(h.getTimestamp());
			buf.writeInt((int) System.currentTimeMillis());
			buf.writeBytes(certification, 0, 1520);
			ctx.writeAndFlush(buf);
			
			return true;
		}
	},
	IDK(1536) {
		@Override
		protected boolean process(ChannelHandlerContext ctx, ByteBuf in, HandShake h) throws Exception {
			Logger.getLogger(HandShakeState.class.getName()).log(Level.INFO, "state: " + name() + ", r:" + in.readableBytes());
	        byte[] data = new byte[1536];
	        for (int i = 0; i < data.length; i++)
	        	data[i] = in.readByte();
	        
	        boolean valid = true;
	        for (int i = 8; i < 1536; i++) {
	            if (h.data[i - 8] != data[i]) {
	                valid = false;
	                break;
	            }
	        }
	        
	        Validate.require(valid, "Server-Client Certification mismatch!", IOException.class);
	        ctx.pipeline().channel().config().setAutoRead(false);
	        ctx.pipeline().addAfter("handshake", "decoder", new AMF3Decoder());
			ctx.pipeline().remove("write_timeout");
			ctx.pipeline().remove("timeout");
			ctx.pipeline().remove(h);
			return true;
		}
	};

	protected boolean process(ChannelHandlerContext ctx, ByteBuf in, HandShake h) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * The number of bytes required for the decoder to resume to the next stage.
	 */
	private final int required;

	/**
	 * Creates a new type of handshake state.
	 * @param predicate - the triple predicate to consume the context, buffer, and session.
	 */
	private HandShakeState() {
		this(0);
	}

	/**
	 * Creates a new type of handshake state.
	 * @param required - the number of bytes required for the decoder to resume.
	 * @param predicate - the triple predicate to consume the context, buffer, and session.
	 */
	private HandShakeState(int required) {
		this.required = required;
	}

	/**
	 * Progresses through the handshake states and returns the next state.
	 * @param context - the channel handler context being passed.
	 * @param buf - the byte buffer being passed.
	 * @param handshake - the handshake decoder instance being passed.
	 * @return the next state in the progression of the decoder.
	 * @throws Exception
	 */
	public HandShakeState progress(ChannelHandlerContext context, ByteBuf in, HandShake handshake) throws Exception {
		return progress(this, context, in, handshake);
	}

	/**
	 * The static final array of values.
	 */
	private static HandShakeState[] VALUES = values();

	/**
	 * Gets the first value in this enum.
	 * @return the first value in this enum.
	 */
	public static HandShakeState getFirst() {
		return VALUES[0];
	}

	/**
	 * Progresses through the handshake states and returns the next state.
	 * @param cur - the current state the decoder is on.
	 * @param context - the channel handler context being passed.
	 * @param buf - the byte buffer being passed.
	 * @param handshake - the handshake decoder instance being passed.
	 * @return the next state in the progression of the decoder.
	 * @throws Exception
	 */
	private static HandShakeState progress(HandShakeState cur, ChannelHandlerContext context, ByteBuf in, HandShake handshake) throws Exception {
		Logger.getLogger(HandShakeState.class.getName()).log(Level.INFO, "readable: " + in.readableBytes());
		for (int pos = cur.ordinal(); cur != null; cur = ++pos < VALUES.length ? VALUES[pos] : null) {
			if (!in.isReadable(cur.required))
				break;
			in.markReaderIndex();
			if (!cur.process(context, in, handshake)) {
				in.resetReaderIndex();
				break;
			}
		}
		return cur;
	}
}
