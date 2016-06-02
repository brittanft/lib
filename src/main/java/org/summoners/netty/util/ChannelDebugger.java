package org.summoners.netty.util;

import java.util.logging.*;

import io.netty.channel.*;

public class ChannelDebugger extends ChannelHandlerAdapter {
	public static ChannelDebugger create(Logger logger, Level level, String activePrefix, String inactivePrefix, boolean local, boolean logAdd, boolean logRemove) {
		if (logger == null || !logger.isLoggable(level))
			return null;
		return new ChannelDebugger(logger, level, activePrefix, inactivePrefix, local, logAdd, logRemove);
	}

	private ChannelDebugger(Logger logger, Level level, String activePrefix, String inactivePrefix, boolean local, boolean logAdd, boolean logRemove) {
		this.logger = logger;
		this.level = level;
		this.activePrefix = activePrefix + ' ';
		this.inactivePrefix = inactivePrefix + ' ';
		this.local = local;
		this.logAdd = logAdd;
		this.logRemove = logRemove;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		if (logAdd)
			logger.log(level, activePrefix + (local ? ctx.channel().localAddress() : ctx.channel().remoteAddress()));
		super.handlerAdded(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.log(level, activePrefix + (local ? ctx.channel().localAddress() : ctx.channel().remoteAddress()));
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.log(level, inactivePrefix + (local ? ctx.channel().localAddress() : ctx.channel().remoteAddress()));
		super.channelInactive(ctx);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		if (logRemove)
			logger.log(level, inactivePrefix + (local ? ctx.channel().localAddress() : ctx.channel().remoteAddress()));
		super.handlerRemoved(ctx);
	}

	public final Logger logger;
	public final Level level;
	public final String activePrefix, inactivePrefix;
	public final boolean local, logAdd, logRemove;
}