package de.vispiron.carsync.mqtt.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Auto-flush data on channel after read timeout
 * It is used to avoid aggressively flushing from the ProtocolProcessor
 *
 * @author Mehrdad A.Karami at 3/7/19
 **/
public class AutoFlusherHandler extends ChannelDuplexHandler {
	private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
	private final long writerIdleTimeNanos;
	private volatile int state;// 0 = none, 1 = initialized, 2 = destroyed
	private volatile long lastWriterTime;
	private ScheduledFuture<?> writerIdleTimeout;

	public AutoFlusherHandler(int flushInterval, TimeUnit unit) {
		if (unit == null) {
			throw new NullPointerException("unit");
		}
		writerIdleTimeNanos = Math.max(unit.toNanos(flushInterval), MIN_TIMEOUT_NANOS);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
			initialize(ctx);
		}
	}

	private void initialize(ChannelHandlerContext ctx) {
		// 0 = none, 1 = initialized, 2 = destroyed
		if (state == 2) {
			return;
		}
		state = 1;
		EventExecutor eventExecutor = ctx.executor();
		lastWriterTime = System.nanoTime();
		writerIdleTimeout = eventExecutor
				.schedule(new WriterIdleTimeoutHandler(ctx), writerIdleTimeNanos, TimeUnit.NANOSECONDS);

	}

	private void destroy() {
		state = 2;
		if (writerIdleTimeout != null) {
			writerIdleTimeout.cancel(false);
			writerIdleTimeout = null;
		}
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		destroy();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().isActive()) {
			initialize(ctx);
		}
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		initialize(ctx);
		super.channelActive(ctx);
	}

	private class WriterIdleTimeoutHandler implements Runnable {

		private final ChannelHandlerContext ctx;

		WriterIdleTimeoutHandler(ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			if (!ctx.channel().isOpen()) {
				return;
			}
			long nextDelay = writerIdleTimeNanos - (System.nanoTime() - lastWriterTime);
			if (nextDelay <= 0) {
				writerIdleTimeout = ctx.executor().schedule(this, writerIdleTimeNanos, TimeUnit.NANOSECONDS);
				try {
					channelIdle(ctx);
				} catch (Exception e) {
					ctx.fireExceptionCaught(e);
				}
			} else {
				writerIdleTimeout = ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
			}
		}

		private void channelIdle(ChannelHandlerContext ctx) {
			ctx.channel().flush();
		}
	}
}
