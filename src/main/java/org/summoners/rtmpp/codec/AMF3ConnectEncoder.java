package org.summoners.rtmpp.codec;

import io.netty.buffer.*;
import io.netty.channel.*;

import java.util.*;
import java.util.logging.*;

import org.summoners.rtmpp.data.*;
import org.summoners.rtmpp.pipeline.*;

public class AMF3ConnectEncoder extends AMF3Encoder {
	
    private static final Logger logger = Logger.getLogger(HandshakeHandler.class.getName());
    
    public AMF3ConnectEncoder() {
    	super(ObjectMap.class);
    }

	@Override
	public void encode(ChannelHandlerContext ctx, ObjectMap obj, ByteBuf out) throws Exception {
		Map<String, Object> map = obj;
		logger.log(Level.INFO, "Connection packet being encoded.");
		ByteBuf buf = ctx.alloc().ioBuffer();
		
		writeStringAMF0(buf, "connect");
		writeIntAMF0(buf, 1); //invokeId

        // Write params
		buf.writeByte(0x11); // AMF3 object
		buf.writeByte(0x09); // Array
        writeAssociativeArray(buf, map);

        // Write service call args
        buf.writeByte(0x01);
        buf.writeByte(0x00); // false
        writeStringAMF0(buf, "nil"); // "nil"
        writeStringAMF0(buf, ""); // ""

        // Set up CommandMessage
        TypedObject cm = new TypedObject("flex.messaging.messages.CommandMessage");
        cm.put("messageRefType", null);
        cm.put("operation", 5);
        cm.put("correlationId", "");
        cm.put("clientId", null);
        cm.put("destination", "");
        cm.put("messageId", randomUID());
        cm.put("timestamp", 0d);
        cm.put("timeToLive", 0d);
        cm.put("body", new TypedObject());
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("DSMessagingVersion", 1d);
        headers.put("DSId", "my-rtmps");
        cm.put("headers", headers);

        // Write CommandMessage
        buf.writeByte(0x11); // AMF3 object
        writeObject(buf, cm);

        writeHeaders(out, buf);
        out.setByte(7, 0x14); //Change message type
        
        ctx.fireChannelRead(out);
	}
	
	
}
