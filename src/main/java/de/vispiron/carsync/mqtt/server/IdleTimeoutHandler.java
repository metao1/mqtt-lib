package de.vispiron.carsync.mqtt.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/
@ChannelHandler.Sharable
public class IdleTimeoutHandler extends ChannelDuplexHandler {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.ALL_IDLE) {
				ctx.fireChannelInactive();
				ctx.close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
