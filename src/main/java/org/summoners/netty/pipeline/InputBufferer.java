package org.summoners.netty.pipeline;

import java.util.function.*;
import java.util.logging.*;

import io.netty.buffer.*;
import io.netty.channel.*;

public class InputBufferer extends ChannelHandlerAdapter {
	
    private static final Logger logger = Logger.getLogger(InputBufferer.class.getName());
	ByteBuf buffer;
	boolean first;
	volatile boolean disabled = true, singleRead;

	public InputBufferer() {
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		setDisabled(false);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		setDisabled(true);
		if (buffer != null) {
			if (buffer.refCnt() > 0) {
				ctx.fireChannelRead(buffer);
				if (buffer != null && buffer.refCnt() > 0)
					buffer.release();
			}
			buffer = null;
		}
	}

	public void setDisabled(boolean disabled) {
		if (this.disabled == disabled)
			return;
		this.disabled = disabled;
	}

	public void setSingleRead(boolean singleRead) {
		this.singleRead = singleRead;
	}

	public boolean getSingleRead() {
		return singleRead;
	}

	private void read(ChannelHandlerContext ctx, Supplier<ByteBuf> sup) {
		do {
			if (disabled)
				break;
			ByteBuf b = sup.get();
			if (b == null)
				break;
			int n = b.readableBytes();
			ctx.fireChannelRead(b);
			if (b.readableBytes() >= n)
				break;
		} while (!singleRead);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
		logger.log(Level.INFO, "Message being read!: " + msg);
		if (buffer == null) {
			read(ctx, () -> in);
			if (!in.isReadable()) {
				if (in != Unpooled.EMPTY_BUFFER)// && in.refCnt() > 0)
					in.release();
				return;
			}
			in.discardSomeReadBytes();
			buffer = in;
			first = true;
			return;
		}
		if (in.isReadable()) {
			if (first) {
				first = false;
				ByteBuf tmp = buffer;
				buffer = ctx.alloc().ioBuffer(tmp.readableBytes() + in.readableBytes());
				buffer.writeBytes(tmp);
				tmp.release();
			}
			buffer.ensureWritable(in.readableBytes());
			buffer.writeBytes(in);
		}
		if (in != Unpooled.EMPTY_BUFFER)// && in.refCnt() > 0)
			in.release();
		read(ctx, () -> buffer);
		if (buffer != null)
			if (!buffer.isReadable()) {
				buffer.release();
				buffer = null;
			} else
				buffer.discardSomeReadBytes();
	}
}
