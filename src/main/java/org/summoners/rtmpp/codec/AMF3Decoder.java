package org.summoners.rtmpp.codec;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;

import org.summoners.function.*;
import org.summoners.rtmp.encoding.*;
import org.summoners.rtmpp.data.*;
import org.summoners.util.*;

import io.netty.buffer.*;
import io.netty.channel.*;

public class AMF3Decoder extends ChannelHandlerAdapter {
	
	public AMF3Decoder() {
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
    }
	
    private Logger logger = Logger.getLogger(AMF3Decoder.class.getName());
	HashMap<Integer, Packet> packets = new HashMap<>();
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		logger.log(Level.INFO, "AMF3 Packet being decoded. is: (" + buf.readerIndex() + "," + buf.writerIndex() + "), readable: " + buf.readableBytes());
		
		if (!buf.isReadable()) {
			ctx.read();
			return;
		}
		
		int header = buf.readByte();
		int channel = header & 0x2F, 
				type = header & 0xC0;
		
		int size = 0;
		switch (type) {
			case 0x00:
				size = 12;
				break;
			case 0x40:
				size = 8;
				break;
			case 0x80:
				size = 4;
				break;
		}
		
		Packet packet = packets.computeIfAbsent(channel, c -> new Packet());
		if (size != 0) {
			int[] data = new int[size - 1];
			for (int i = 0; i != data.length; ++i)
				data[i] = buf.readByte();
			
			System.out.println("Data: " + Arrays.toString(data));
			
			if (size >= 8) {
				int packetSize = 0;
				for (int i = 3; i != 6; ++i)
					packetSize = packetSize * 256 + (data[i] & 0xFF);
				
				packet.setSize(packetSize);
				packet.setType(data[6]);
			}
            System.out.println("Type: " + packet.getType() + ", Size: "+ packet.getDataBuffer().limit());
		}
            System.out.println("S: " + size + ", H: " + header + ", C: " + channel + ", T: " + type);
		
		System.out.println("P " + packet.getDataBuffer().position() + ", R " + packet.getDataBuffer().remaining() + ", L " + packet.getDataBuffer().limit() + ", C " + packet.getDataBuffer().capacity());
		for (int i = 0; i != 128; ++i) {
			byte b = buf.readByte();
			System.out.print(b + ", ");
			packet.getDataBuffer().put(b);
			if (packet.isComplete())
				break;
		}
		
		if (!packet.isComplete()) {
			channelRead(ctx, msg);
			return;
		}
		
		packet.getDataBuffer().flip();
		packets.remove(channel);
		
		TypedObject object;
		switch (packet.getType()) {
			case 0x03:
				packet.getDataBuffer().getInt(); //unknown
				return;
				
			case 0x06:
				System.out.println(packet.getDataBuffer().getInt()); //windowSize
				System.out.println(packet.getDataBuffer().get()); //windowType
				System.out.println(packet.getDataBuffer().remaining());
				System.out.println(buf.readableBytes());
				return;
				
			case 0x11:
				object = decodeInvoke(packet.getDataBuffer());
				break;
				
			/*
			 * The "connection call" packet type.
			 */
			case 0x14:
				object = decodeConnect(packet.getDataBuffer());
				break;
			
			default:
				Logger.getLogger(AMF3Decoder.class.getName()).log(Level.SEVERE, "Unrecognized packet type: " + packet.getType());
				return;
		}
		
		Logger.getLogger(AMF3Decoder.class.getName()).log(Level.SEVERE, "Object decoded: " + object);
		
		Integer id = (Integer) object.get("invokeId");
		if (id == null || id == 0) { //TODO event system
			//if (callback != null)
			//	callback.callback(object);
		}/* else if (callbacks.containsKey(id)) {
			final Callback cb = callbacks.remove(id);
			if (cb != null) {
				cb.callback(result); //TODO async thread
			}
		} else
			results.put(id, object);
		
		invokes.remove(id);*/
	}

    /**
     * Resets all the reference lists
     */
    public void reset() {
        references.clear();
        objects.clear();
        classDefs.clear();
    }
	
	public TypedObject decodeConnect(ByteBuffer buf) throws IOException {
		reset();
		
		byte[] data = buf.array();
        System.out.println(Arrays.toString(data));
		
		TypedObject object = new TypedObject("Invoke");
		object.put("result", decodeAMF0(buf));
		object.put("invokeId", decodeAMF0(buf));
		object.put("serviceCall", decodeAMF0(buf));
		object.put("data", decodeAMF0(buf));
		
		Validate.require(buf.remaining() == 0, "Did not consume entire buffer.", IOException.class);
		return object;
	}
	
	public TypedObject decodeInvoke(ByteBuffer buf) throws IOException {
		reset();
		
		TypedObject object = new TypedObject("Invoke");
		if (buf.get(0) == 0x00) {
			object.put("version", 0x00);
			buf.get();
		}
		object.put("result", decodeAMF0(buf));
		object.put("invokeId", decodeAMF0(buf));
		object.put("serviceCall", decodeAMF0(buf));
		object.put("data", decodeAMF0(buf));
		
		Validate.require(buf.remaining() == 0, "Did not consume entire buffer.", IOException.class);
		return object;
	}
	
	private Object decode(ByteBuffer buf) throws IOException {
		int type = buf.get();
		switch (type) {
			case 0:
				throw new IOException("Invalid data type.");
			case 1:
				return null;
			case 2:
				return Boolean.FALSE;
			case 3:
				return Boolean.TRUE;
			case 4:
				return getSmart(buf);
			case 5:
				return buf.getDouble();
			case 6:
				return getString(buf);
			case 7:
				//return buf.
			case 8:
				return getDate(buf);
			case 9:
				return getArray(buf);
			case 10:
			case 11:
			case 12:
				break;
		}
		return null;
	}
	
	private Object decodeAMF0(ByteBuffer buf) throws IOException {
		int type = buf.get();
		switch (type) {
	        case 0x00:
				return (int) buf.getDouble();
	        case 0x01:
	        	return buf.get() != 0;
	        case 0x02:
				return getAMF0String(buf);
	        case 0x03:
				return getAMF0Object(buf);
	        case 0x05:
				return null;
	        case 0x11: // AMF3
				return decode(buf);
		}
		throw new IOException("AMF0 type not supported: " + type);
	}
	
	public Tuple<Integer, Boolean> handle(ByteBuffer buf) {
		int length = getSmart(buf);
		boolean stored = (length & 1) != 0;
		length >>= 1;
		return new Tuple<>(length, stored);
	}
	
	public int getSmart(ByteBuffer buf) {
		int val = buf.get() & 0xFF;
		if (val < 128)
			return val;
		val = (val & 0x7f) << 7;
		int tmp = buf.get() & 0xFF;
		if (tmp < 128) {
			val = val | tmp;
		} else {
			val = (val | (tmp & 0x7F)) << 7;
			tmp = buf.get() & 0xFF;
			if (tmp < 128)
				val = val | tmp;
			else {
				val = (val | tmp & 0x7F) << 7;
				val = val | (buf.get() & 0xFF);
			}
		}
		
		return -(val & (1 << 28)) | val;
	}
	
	public String getAMF0String(ByteBuffer buf) throws IllegalStateException {
		int length = buf.getShort() & 0xFFFF;
		if (length == 0)
			return "";
		
		byte[] buffer = new byte[length];
		buf.get(buffer);
		
		try {
			return new String(buffer, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException("Encoding unsupported!");
		}
	}
	
	private TypedObject getAMF0Object(ByteBuffer buf) {
		TypedObject body = new TypedObject("Body");
		String key;
		while (!(key = getAMF0String(buf)).equals("")) {
			byte b = buf.get();
			if (b == 0x00)
				body.put(key, buf.getDouble());
			else if (b == 0x02)
				body.put(key, getAMF0String(buf));
			else if (b == 0x05)
				body.put(key, null);
			else
				throw new IllegalStateException("AMF0 type not supported: " + b);
		}
		buf.get();
		return body;
	}
	
	public byte[] getByteArray(ByteBuffer buf) {
		Tuple<Integer, Boolean> handle = handle(buf);
		if (handle.getB()) {
			byte[] buffer = new byte[handle.getA()];
			buf.get(buffer);
			return buffer;
		}
		return (byte[]) objects.get(handle.getA());
	}
	
	public String getString(ByteBuffer buf) throws IllegalStateException {
		Tuple<Integer, Boolean> handle = handle(buf);
		if (handle.getB()) {
			if (handle.getA() == 0)
				return "";
			
			byte[] buffer = new byte[handle.getA()];
			buf.get(buffer);
			
			try {
				String string = new String(buffer, "UTF-8");
				references.add(string);
				return string;
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("Encoding unsupported!");
			}
		}
		return references.get(handle.getA());
	}
	
	public Date getDate(ByteBuffer buf) {
		Tuple<Integer, Boolean> handle = handle(buf);
		if (handle.getB()) {
			Date date = new Date(buf.getLong());
			objects.add(date);
			return date;
		}
		return (Date) objects.get(handle.getA());
	}
	
	public Object[] getArray(ByteBuffer buf) throws IllegalStateException, IOException {
		Tuple<Integer, Boolean> handle = handle(buf);
		if (handle.getB()) {
			String key = getString(buf);
			Validate.requireFalse(key != null && !key.equals(""), "Array cannot be associative.", IllegalStateException.class);
			
			Object[] value = new Object[handle.getA()];
			objects.add(value);
			
			for (int i = 0; i != handle.getA(); ++i)
				value[i] = getObject(buf);
			
			return value;
		} 
		return (Object[]) objects.get(handle.getA());
	}
	
	private static class ClassDef {
		public ClassDef() {
		}
		String type;
		boolean externalizable, dynamic;
		LinkedList<String> members;
	}
	
	public Object getObject(ByteBuffer buf) throws IOException {
		int handle = getSmart(buf);
		boolean exists = ((handle & 1) != 0);
		handle = handle >> 1;
		if (exists) {
			boolean defExists = ((handle & 1) != 0);
			handle = handle >> 1;
			
			ClassDef def;
			if (defExists) {
				def = new ClassDef();
				def.type = getString(buf);
				def.externalizable = ((handle & 1) != 0);
				handle = handle >> 1;
				def.dynamic = ((handle & 1) != 0);
				handle = handle >> 1;
				for (int i = 0; i < handle; i++)
					def.members.add(getString(buf));
				classDefs.add(def);
			} else
				def = classDefs.get(handle);
			
			TypedObject ret = new TypedObject(def.type);
			objects.add(ret);
			if (def.externalizable) {
				if (def.type.equals("DSK"))
					ret = getDSK(buf);
				else if (def.type.equals("DSA"))
					ret = getDSA(buf);
				else if (def.type.equals("flex.messaging.io.ArrayCollection")) {
					Object obj = decode(buf);
					ret = TypedObject.makeArrayCollection((Object[]) obj);
				} else if (def.type.equals("com.riotgames.platform.systemstate.ClientSystemStatesNotification") || def.type.equals("com.riotgames.platform.broadcast.BroadcastNotification")) {
					/*int size = 0;
					for (int i = 0; i < 4; i++)
						size = size * 256 + (buf.get() & 0xFF);*/
					
					byte[] buffer = new byte[buf.getInt()];
					buf.get(buffer);
					
					ret = new TypedObject(JsonIO.load(new String(buffer, "UTF-8"), Map.class));
					ret.setType(def.type);
				} else {
					for (int i = buf.position(); i != buf.limit(); ++i)
						System.out.print(String.format("%02X", buf.get(i)));
					
					System.out.println();
					throw new IllegalStateException("Externalizable not handled for " + def.type);
				}
			} else {
				for (int i = 0; i < def.members.size(); i++) {
					String key = def.members.get(i);
					Object value = decode(buf);
					ret.put(key, value);
				}
				if (def.dynamic) {
					String key;
					while ((key = getString(buf)).length() != 0) {
						Object value = decode(buf);
						ret.put(key, value);
					}
				}
			}
			return ret;
		}
		return objects.get(handle);
	}

	/**
	 * Decodes a DSA
	 * 
	 * @return The decoded DSA
	 * @throws NotImplementedException
	 * @throws EncodingException
	 */
	private TypedObject getDSA(ByteBuffer buf) throws IOException {
		TypedObject ret = new TypedObject("DSA");
		
		List<Integer> flags = getFlags(buf);
		for (int i = 0; i != flags.size(); ++i) {
			int flag = flags.get(i);
			int bits = 0;
			if (i == 0) {
				if ((flag & 0x01) != 0)
					ret.put("body", decode(buf));
				if ((flag & 0x02) != 0)
					ret.put("clientId", decode(buf));
				if ((flag & 0x04) != 0)
					ret.put("destination", decode(buf));
				if ((flag & 0x08) != 0)
					ret.put("headers", decode(buf));
				if ((flag & 0x10) != 0)
					ret.put("messageId", decode(buf));
				if ((flag & 0x20) != 0)
					ret.put("timeStamp", decode(buf));
				if ((flag & 0x40) != 0)
					ret.put("timeToLive", decode(buf));
				bits = 7;
			} else if (i == 1) {
				if ((flag & 0x01) != 0) {
					buf.get();
					byte[] temp = getByteArray(buf);
					ret.put("clientIdBytes", temp);
					ret.put("clientId", getId(temp));
				}
				if ((flag & 0x02) != 0) {
					buf.get();
					byte[] temp = getByteArray(buf);
					ret.put("messageIdBytes", temp);
					ret.put("messageId", getId(temp));
				}
				bits = 2;
			}
			parseRemaining(buf, flag, bits);
		}
		
		flags = getFlags(buf);
		for (int i = 0; i != flags.size(); ++i) {
			int flag = flags.get(i);
			
			int bits = 0;
			if (i == 0) {
				if ((flag & 0x01) != 0)
					ret.put("correlationId", decode(buf));
				if ((flag & 0x02) != 0) {
					buf.get();
					byte[] temp = getByteArray(buf);
					ret.put("correlationIdBytes", temp);
					ret.put("correlationId", getId(temp));
				}
				bits = 2;
			}
			parseRemaining(buf, flag, bits);
		}
		return ret;
	}
	
	private static String getId(byte[] data) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i != data.length; ++i) {
			if (i == 4 || i == 6 || i == 8 || i == 10)
				builder.append('-');
			builder.append(String.format("%02x", data[i]));
		}
		return builder.toString();
	}

	private TypedObject getDSK(ByteBuffer buf) throws IOException {
		TypedObject object = getDSA(buf);
		object.setType("DSK");
		List<Integer> flags = getFlags(buf);
		for (int i = 0; i != flags.size(); ++i)
			parseRemaining(buf, flags.get(i), 0);
		return object;
	}

	private List<Integer> getFlags(ByteBuffer buf) {
		List<Integer> flags = new LinkedList<>();
		int flag;
		do {
			flag = buf.get() & 0xFF;
			flags.add(flag);
		} while ((flag & 0x80) != 0);
		return flags;
	}

	private void parseRemaining(ByteBuffer buf, int flag, int bits) throws IOException {
		// For forwards compatibility, read in any other flagged objects to
		// preserve the integrity of the input stream...
		if ((flag >> bits) != 0) {
			for (int o = bits; o < 6; o++) {
				if (((flag >> o) & 1) != 0)
					decode(buf);
			}
		}
	}
	
	private ArrayList<String> references = new ArrayList<>();
	private ArrayList<ClassDef> classDefs = new ArrayList<>();
	private ArrayList<Object> objects = new ArrayList<>();
}
