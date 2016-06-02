package org.summoners.netty;

import java.util.logging.*;

import org.summoners.netty.pipeline.*;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.socket.*;

public abstract class NettySession<C extends NettyContext> extends ChannelHandlerAdapter {
	private volatile boolean destroyed;
	private volatile boolean closing;
	public final C context;
	public final SocketChannel channel;

	public NettySession(C context, SocketChannel channel) {
		this.context = context;
		this.channel = channel;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		close();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		close();
	}

	public final ByteBufAllocator alloc() {
		return channel.alloc();
	}

	public final void setAutoRead(boolean autoRead) {
		channel.config().setAutoRead(autoRead);
		InputBufferer bufferer = (InputBufferer) channel.pipeline().get("bufferer");
		if (bufferer != null)
			bufferer.setDisabled(!autoRead);
		if (autoRead)
			channel.read();
	}

	public final ChannelFuture write(Object msg) {
		if (!isActive())
			throw new IllegalStateException();
		return channel.write(msg);
	}

	public final ChannelFuture writeAndFlush(Object msg) {
		if (!isActive())
			throw new IllegalStateException();
		return channel.writeAndFlush(msg);
	}

	public final ChannelFuture writeAndClose(Object msg) {
		if (!isActive())
			throw new IllegalStateException();
		closing = true;
		disableInput();
		return channel.writeAndFlush(msg, channel.newPromise().addListener(ChannelFutureListener.CLOSE));
	}

	public final ChannelFuture closeFuture(ChannelFuture f) {
		if (!isActive())
			throw new IllegalStateException();
		closing = true;
		disableInput();
		return f.addListener(ChannelFutureListener.CLOSE);
	}

	public final void flush() {
		if (!isActive())
			throw new IllegalStateException();
		channel.flush();
	}

	protected void disableInput() {
		setAutoRead(false);
	}

	protected void closeImpl() {
		disableInput();
	}

	public final void close() {
		if (destroyed)
			return;
		synchronized (this) {
			if (destroyed)
				return;
			destroyed = true;
		}
		try {
			try {
				closeImpl();
			} finally {
				channel.close();
			}
		} catch (Exception ex) {
			Logger.getLogger(NettySession.class.getName()).log(Level.SEVERE, "Error closing session.", ex);
		}
	}

	public final boolean isDestroyed() {
		return destroyed;
	}

	public final boolean isClosing() {
		return closing;
	}

	public final boolean isActive() {
		return !closing && !isClosed();
	}

	public final boolean isClosed() {
		return destroyed || !channel.isActive();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}
}