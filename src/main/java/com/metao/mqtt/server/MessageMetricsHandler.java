package com.metao.mqtt.server;

import com.metao.mqtt.models.MessageMetrics;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class MessageMetricsHandler extends ChannelDuplexHandler {

    private static final AttributeKey<MessageMetrics> ATTR_KEY_METRICS = AttributeKey.valueOf("MessageMetrics");

    private MessageMetricsCollector collector;

    public MessageMetricsHandler(MessageMetricsCollector collector) {
        this.collector = collector;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Attribute<MessageMetrics> attr = ctx.attr(ATTR_KEY_METRICS);
        attr.set(new MessageMetrics());

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageMetrics metrics = ctx.attr(ATTR_KEY_METRICS).get();
        metrics.incrementRead(1);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        MessageMetrics metrics = ctx.attr(ATTR_KEY_METRICS).get();
        metrics.incrementWrote(1);
        ctx.write(msg, promise);
    }


    @Override
    public void close(ChannelHandlerContext ctx,
                      ChannelPromise promise) throws Exception {
        MessageMetrics metrics = ctx.attr(ATTR_KEY_METRICS).get();
        collector.sumReadMessages(metrics.messagesRead());
        collector.sumWroteMessages(metrics.messagesWrote());
        super.close(ctx, promise);
    }
}
