package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MQTTDecoder extends ByteToMessageDecoder {

    //3 = 3.1, 4 = 3.1.1
    public static final AttributeKey<Integer> PROTOCOL_VERSION = AttributeKey.valueOf("version");

    private final Map<Byte, Decoder> decoderMap = new HashMap<>();

    public MQTTDecoder() {
        decoderMap.put(PacketTypeMessage.CONNECT, new ConnectDecoder());
        decoderMap.put(PacketTypeMessage.CONNACK, new ConnAckDecoder());
        decoderMap.put(PacketTypeMessage.PUBLISH, new PublishDecoder());
        decoderMap.put(PacketTypeMessage.PUBACK, new PubAckDecoder());
        decoderMap.put(PacketTypeMessage.SUBSCRIBE, new SubscribeDecoder());
        decoderMap.put(PacketTypeMessage.SUBACK, new SubAckDecoder());
        decoderMap.put(PacketTypeMessage.UNSUBSCRIBE, new UnsubscribeDecoder());
        decoderMap.put(PacketTypeMessage.DISCONNECT, new DisconnectDecoder());
        decoderMap.put(PacketTypeMessage.PINGREQ, new PingReqDecoder());
        decoderMap.put(PacketTypeMessage.PINGRESP, new PingRespDecoder());
        decoderMap.put(PacketTypeMessage.UNSUBACK, new UnsubAckDecoder());
        decoderMap.put(PacketTypeMessage.PUBCOMP, new PubCompDecoder());
        decoderMap.put(PacketTypeMessage.PUBREC, new PubRecDecoder());
        decoderMap.put(PacketTypeMessage.PUBREL, new PubRelDecoder());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (!Utils.checkValidHeader(in)) {
            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();

        byte messageType = Utils.readMessageType(in);

        Decoder decoder = decoderMap.get(messageType);
        if (decoder == null) {
            throw new CorruptedFrameException("Can't find any suitable decoder for message type: " + messageType);
        }
        decoder.decode(ctx, in, out);
    }
}
