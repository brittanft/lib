package org.summoners.rtmpp.pipeline;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;

import org.summoners.function.*;
import org.summoners.rtmpp.*;
import org.summoners.util.*;

public class PacketDecoder {
	
	private ByteBuffer buf;
	
	public PacketDecoder(ByteBuffer buf) {
		this.buf = buf;
	}
	
	@SuppressWarnings("unused")
	private void parse() throws IOException {
		HashMap<Integer, Packet> packets = new HashMap<>();
		
		while (true) {
			int header = buf.get();
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
					data[i] = buf.get();
				
				if (size >= 8) {
					int packetSize = 0;
					for (int i = 3; i != 6; ++i)
						packetSize = packetSize * 256 + (data[i] & 0xFF);
					
					packet.setSize(packetSize);
					packet.setType(data[6]);
				}
			}
			
			for (int i = 0; i != 128; ++i) {
				packet.getDataBuffer().put(buf.get());
				if (packet.isComplete())
					break;
			}
			
			if (!packet.isComplete())
				continue;
			
			packets.remove(channel);
			
			SerializedObject object;
			switch (packet.getType()) {
				case 0x03:
					int unknown = packet.getDataBuffer().getInt();
					continue;
					
				case 0x06:
					int windowSize = packet.getDataBuffer().getInt();
					int windowType = packet.getDataBuffer().get();
					continue;
					
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
					Logger.getLogger(PacketDecoder.class.getName()).log(Level.SEVERE, "Unrecognized packet type: " + packet.getType());
					continue;
			}
			
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
	}

    /**
     * Resets all the reference lists
     */
    public void reset() {
        references.clear();
        objects.clear();
        classDefs.clear();
    }
	
	public SerializedObject decodeConnect(ByteBuffer buf) throws IOException {
		reset();
		
		this.buf = buf;
		
		SerializedObject object = new SerializedObject("Invoke");
		object.put("result", decodeAMF0());
		object.put("invokeId", decodeAMF0());
		object.put("serviceCall", decodeAMF0());
		object.put("data", decodeAMF0());
		
		Validate.require(buf.remaining() == 0, "Did not consume entire buffer.", IOException.class);
		return object;
	}
	
	public SerializedObject decodeInvoke(ByteBuffer buf) throws IOException {
		reset();
		
		this.buf = buf;
		
		SerializedObject object = new SerializedObject("Invoke");
		if (buf.get(0) == 0x00) {
			object.put("version", 0x00);
			buf.get();
		}
		object.put("result", decodeAMF0());
		object.put("invokeId", decodeAMF0());
		object.put("serviceCall", decodeAMF0());
		object.put("data", decodeAMF0());
		
		Validate.require(buf.remaining() == 0, "Did not consume entire buffer.", IOException.class);
		return object;
	}
	
	private Object decode() throws IOException {
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
				return getSmart();
			case 5:
				return buf.getDouble();
			case 6:
				return getString();
			case 7:
				//return buf.
			case 8:
				return getDate();
			case 9:
				return getArray();
			case 10:
			case 11:
			case 12:
				break;
		}
		return null;
	}
	
	private Object decodeAMF0() throws IOException {
		int type = buf.get();
		switch (type) {
			case 0:
				return (int) buf.getDouble();
			case 2:
				return getAMF0String();
			case 3:
				return getAMF0Object();
			case 5:
				return null;
			case 11:
				return decode();
		}
		
		throw new IOException("AMF0 type not supported!");
	}
	
	public Tuple<Integer, Boolean> handle() {
		int length = getSmart();
		boolean stored = (length & 1) != 0;
		length >>= 1;
		return new Tuple<>(length, stored);
	}
	
	public int getSmart() {
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
	
	public String getAMF0String() throws IllegalStateException {
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
	
	private SerializedObject getAMF0Object() {
		SerializedObject body = new SerializedObject("Body");
		String key;
		while (!(key = getAMF0String()).equals("")) {
			byte b = buf.get();
			if (b == 0x00)
				body.put(key, buf.getDouble());
			else if (b == 0x02)
				body.put(key, getAMF0String());
			else if (b == 0x05)
				body.put(key, null);
			else
				throw new IllegalStateException("AMF0 type not supported: " + b);
		}
		buf.get();
		return body;
	}
	
	public byte[] getByteArray() {
		Tuple<Integer, Boolean> handle = handle();
		if (handle.getB()) {
			byte[] buffer = new byte[handle.getA()];
			buf.get(buffer);
			return buffer;
		}
		return (byte[]) objects.get(handle.getA());
	}
	
	public String getString() throws IllegalStateException {
		Tuple<Integer, Boolean> handle = handle();
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
	
	public Date getDate() {
		Tuple<Integer, Boolean> handle = handle();
		if (handle.getB()) {
			Date date = new Date(buf.getLong());
			objects.add(date);
			return date;
		}
		return (Date) objects.get(handle.getA());
	}
	
	public Object[] getArray() throws IllegalStateException, IOException {
		Tuple<Integer, Boolean> handle = handle();
		if (handle.getB()) {
			String key = getString();
			Validate.requireFalse(key != null && !key.equals(""), "Array cannot be associative.", IllegalStateException.class);
			
			Object[] value = new Object[handle.getA()];
			objects.add(value);
			
			for (int i = 0; i != handle.getA(); ++i)
				value[i] = getObject();
			
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
	
	public Object getObject() throws IOException {
		int handle = getSmart();
		boolean exists = ((handle & 1) != 0);
		handle = handle >> 1;
		if (exists) {
			boolean defExists = ((handle & 1) != 0);
			handle = handle >> 1;
			
			ClassDef def;
			if (defExists) {
				def = new ClassDef();
				def.type = getString();
				def.externalizable = ((handle & 1) != 0);
				handle = handle >> 1;
				def.dynamic = ((handle & 1) != 0);
				handle = handle >> 1;
				for (int i = 0; i < handle; i++)
					def.members.add(getString());
				classDefs.add(def);
			} else
				def = classDefs.get(handle);
			
			SerializedObject ret = new SerializedObject(def.type);
			objects.add(ret);
			if (def.externalizable) {
				if (def.type.equals("DSK"))
					ret = getDSK();
				else if (def.type.equals("DSA"))
					ret = getDSA();
				else if (def.type.equals("flex.messaging.io.ArrayCollection")) {
					Object obj = decode();
					ret = SerializedObject.makeArrayCollection((Object[]) obj);
				} else if (def.type.equals("com.riotgames.platform.systemstate.ClientSystemStatesNotification") || def.type.equals("com.riotgames.platform.broadcast.BroadcastNotification")) {
					/*int size = 0;
					for (int i = 0; i < 4; i++)
						size = size * 256 + (buf.get() & 0xFF);*/
					
					byte[] buffer = new byte[buf.getInt()];
					buf.get(buffer);
					
					ret = new SerializedObject(JsonIO.load(new String(buffer, "UTF-8"), Map.class));
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
					Object value = decode();
					ret.put(key, value);
				}
				if (def.dynamic) {
					String key;
					while ((key = getString()).length() != 0) {
						Object value = decode();
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
	private SerializedObject getDSA() throws IOException {
		SerializedObject ret = new SerializedObject("DSA");
		
		List<Integer> flags = getFlags();
		for (int i = 0; i != flags.size(); ++i) {
			int flag = flags.get(i);
			int bits = 0;
			if (i == 0) {
				if ((flag & 0x01) != 0)
					ret.put("body", decode());
				if ((flag & 0x02) != 0)
					ret.put("clientId", decode());
				if ((flag & 0x04) != 0)
					ret.put("destination", decode());
				if ((flag & 0x08) != 0)
					ret.put("headers", decode());
				if ((flag & 0x10) != 0)
					ret.put("messageId", decode());
				if ((flag & 0x20) != 0)
					ret.put("timeStamp", decode());
				if ((flag & 0x40) != 0)
					ret.put("timeToLive", decode());
				bits = 7;
			} else if (i == 1) {
				if ((flag & 0x01) != 0) {
					buf.get();
					byte[] temp = getByteArray();
					ret.put("clientIdBytes", temp);
					ret.put("clientId", getId(temp));
				}
				if ((flag & 0x02) != 0) {
					buf.get();
					byte[] temp = getByteArray();
					ret.put("messageIdBytes", temp);
					ret.put("messageId", getId(temp));
				}
				bits = 2;
			}
			parseRemaining(flag, bits);
		}
		
		flags = getFlags();
		for (int i = 0; i != flags.size(); ++i) {
			int flag = flags.get(i);
			
			int bits = 0;
			if (i == 0) {
				if ((flag & 0x01) != 0)
					ret.put("correlationId", decode());
				if ((flag & 0x02) != 0) {
					buf.get();
					byte[] temp = getByteArray();
					ret.put("correlationIdBytes", temp);
					ret.put("correlationId", getId(temp));
				}
				bits = 2;
			}
			parseRemaining(flag, bits);
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

	private SerializedObject getDSK() throws IOException {
		SerializedObject object = getDSA();
		object.setType("DSK");
		List<Integer> flags = getFlags();
		for (int i = 0; i != flags.size(); ++i)
			parseRemaining(flags.get(i), 0);
		return object;
	}

	private List<Integer> getFlags() {
		List<Integer> flags = new LinkedList<>();
		int flag;
		do {
			flag = buf.get() & 0xFF;
			flags.add(flag);
		} while ((flag & 0x80) != 0);
		return flags;
	}

	private void parseRemaining(int flag, int bits) throws IOException {
		// For forwards compatibility, read in any other flagged objects to
		// preserve the integrity of the input stream...
		if ((flag >> bits) != 0) {
			for (int o = bits; o < 6; o++) {
				if (((flag >> o) & 1) != 0)
					decode();
			}
		}
	}
	
	private ArrayList<String> references = new ArrayList<>();
	private ArrayList<ClassDef> classDefs = new ArrayList<>();
	private ArrayList<Object> objects = new ArrayList<>();
}
