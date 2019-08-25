package com.metao.mqtt.server;

import com.metao.mqtt.models.*;
import com.metao.mqtt.models.*;
import com.metao.mqtt.utils.Utils;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.metao.mqtt.models.PacketTypeMessage.*;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

@Component
@ChannelHandler.Sharable
public class TcpHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LoggerFactory.getILoggerFactory().getLogger(TcpHandler.class.getName());

    @Autowired
    public ProtocolHandler protocolAnalyser;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        PacketTypeMessage msg = (PacketTypeMessage) message;
        log.info("Received a message of type {}", Utils.msgTypeToString(msg.getMessageType()));
        try {
            switch (msg.getMessageType()) {
                case CONNECT:
                    protocolAnalyser.processConnect(ctx.channel(), (ConnectPacket) message);
                    break;
                case DISCONNECT:
                    protocolAnalyser.processDisconnect(ctx.channel());
                    break;
                case SUBSCRIBE:
                    protocolAnalyser.processSubscribe(ctx.channel(), (SubscribePacket) message);
                    break;
                case UNSUBSCRIBE:
                    protocolAnalyser.processUnsubscribe(ctx.channel(), (UnsubscribeAckPacket) message);
                    break;
                case PUBACK:
                    protocolAnalyser.processPublishAck(ctx.channel(), (PublishAckPacket) message);
                    break;
                case PUBLISH:
                    protocolAnalyser.processPublish(ctx.channel(), (PublishPacket) message);
                    break;
                case PINGREQ:
                    protocolAnalyser.processPingRequest(ctx.channel());
                    break;
                case PUBREC:
                    protocolAnalyser.processPublishReceived(ctx.channel(), (PublishReceivePacket) message);
                    break;
                case PUBCOMP:
                    protocolAnalyser.processPublishComplete(ctx.channel(), (PubCompPacket) message);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported command!");
            }
        } catch (Exception e) {
            throw new ChannelException(e.getCause());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CorruptedFrameException) {
            //decoding problem
            log.info("Error in processing the packet: bad formatted {}", cause.getMessage());
        } else if (cause instanceof ClassCastException) {
            log.info("Error in processing the packet: cast exception {}", cause.getMessage());
        } else if (cause instanceof ChannelException) {
            log.info("Error in processing the packet: Channel corrupted {}", cause.getMessage());
        } else {
            log.info("Error in processing the packet: general error {}", cause.getMessage());
            cause.printStackTrace();
        }
    }
}
