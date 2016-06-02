package org.summoners.rtmp.codec;

import java.io.*;
import java.util.*;

import org.summoners.rtmp.data.*;

import io.netty.buffer.*;
import io.netty.handler.codec.*;

public abstract class AMF3Encoder extends MessageToByteEncoder<ObjectMap> {
	
	private long startTime = System.currentTimeMillis();

	public AMF3Encoder(Class<ObjectMap> class1) {
		super(class1);
	}

	protected void writeHeaders(ByteBuf out, ByteBuf buf) {
		int size = buf.writerIndex();
		out.writeByte((byte) VERSION);
		
		long timediff = System.currentTimeMillis() - startTime;
		out.writeByte((int) (timediff & 0xFF0000) >> 16);
		out.writeByte((int) (timediff & 0x00FF00) >> 8);
		out.writeByte((int) timediff & 0x0000FF);
		// Body size
		out.writeByte((size & 0xFF0000) >> 16);
		out.writeByte((size & 0x00FF00) >> 8);
		out.writeByte(size & 0x0000FF);
		// Content type
		out.writeByte(0x11);
		// Source ID
		for (int i = 0; i != 4; ++i)
			out.writeByte(0);
		
		for (int i = 0; buf.readableBytes() != 0; ++i) {
			out.writeByte(buf.readByte());
			if (i % 128 == 127 && i != size - 1)
				out.writeByte(0xC3);
		}
	}

	@SuppressWarnings("unchecked")
	public static void writeObject(ByteBuf out, Object obj) { //453 - 487
		if (obj == null)
			out.writeByte(0x01);
		else if (obj instanceof Boolean)
			out.writeByte(((Boolean) obj) ? 0x03 : 0x02);
		else if (obj instanceof Integer) {
			out.writeByte(0x04);
			writeInt(out, (Integer) obj);
		} else if (obj instanceof Double) {
			out.writeByte(0x05);
			out.writeDouble((Double) obj);
		} else if (obj instanceof String) {
			out.writeByte(0x06);
			writeString(out, (String) obj);
		} else if (obj instanceof Date) {
			out.writeByte(0x08);
			writeDate(out, (Date) obj);
		} else if (obj instanceof Byte[]) {
			out.writeByte(0x0C);
			writeByteArray(out, (byte[]) obj);
		} else if (obj instanceof Object[]) {
			out.writeByte(0x09);
			writeArray(out, (Object[]) obj);
		} else if (obj instanceof TypedObject) {
			out.writeByte(0x0A);
			writeSerializedObject(out, (TypedObject) obj);
		} else if (obj instanceof Map) {
			out.writeByte(0x09);
			writeAssociativeArray(out, (Map<String, Object>) obj);
		} else
			throw new IllegalStateException("Unexpected object type: " + obj.getClass().getName());
	}

	/**
	 * Encodes an integer as AMF3 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The integer to encode
	 */
	public static void writeInt(ByteBuf out, int val) {
		if (val < 0 || val >= 0x200000) {
			out.writeByte((byte) (((val >> 22) & 0x7f) | 0x80));
			out.writeByte((byte) (((val >> 15) & 0x7f) | 0x80));
			out.writeByte((byte) (((val >> 8) & 0x7f) | 0x80));
			out.writeByte((byte) (val & 0xff));
		} else {
			if (val >= 0x4000) {
				out.writeByte((byte) (((val >> 14) & 0x7f) | 0x80));
			}
			if (val >= 0x80) {
				out.writeByte((byte) (((val >> 7) & 0x7f) | 0x80));
			}
			out.writeByte((byte) (val & 0x7f));
		}
	}

	/**
	 * Encodes a string as AMF3 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The string to encode
	 * @throws EncodingException
	 */
	public static void writeString(ByteBuf out, String val) {
		byte[] temp = null;
		try {
			temp = val.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unable to encode string as UTF-8: " + val);
		}
		writeInt(out, (temp.length << 1) | 1);
		for (byte b : temp)
			out.writeByte(b);
	}

	/**
	 * Encodes a date as AMF3 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The date to encode
	 */
	public static void writeDate(ByteBuf out, Date val) {
		out.writeByte(0x01);
		out.writeDouble(val.getTime());
	}

	/**
	 * Encodes an array as AMF3 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The array to encode
	 * @throws EncodingException
	 * @throws NotImplementedException
	 */
	public static void writeArray(ByteBuf out, Object[] val) {
		writeInt(out, (val.length << 1) | 1);
		out.writeByte(0x01);
		for (Object obj : val)
			writeObject(out, obj);
	}

	/**
	 * Encodes an associative array as AMF3 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The associative array to encode
	 * @throws EncodingException
	 * @throws NotImplementedException
	 */
	public static void writeAssociativeArray(ByteBuf out, Map<String, Object> val) {
		out.writeByte(0x01);
		for (String key : val.keySet()) {
			writeString(out, key);
			writeObject(out, val.get(key));
		}
		out.writeByte(0x01);
	}

	/**
	 * Encodes an object as AMF3 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The object to encode
	 * @throws EncodingException
	 * @throws NotImplementedException
	 */
	public static void writeSerializedObject(ByteBuf out, TypedObject val) {
		if (val.getType() == null || val.getType().equals("")) {
			out.writeByte(0x0B); // Dynamic class
			out.writeByte(0x01); // No class name
			for (String key : val.keySet()) {
				writeString(out, key);
				writeObject(out, val.get(key));
			}
			out.writeByte(0x01); // End of dynamic
		} else if (val.getType().equals("flex.messaging.io.ArrayCollection")) {
			out.writeByte(0x07); // Externalizable
			writeString(out, val.getType());
			writeObject(out, val.get("array"));
		} else {
			writeInt(out, (val.size() << 4) | 3); // Inline + member count
			writeString(out, val.getType());
			List<String> keyOrder = new ArrayList<>();
			for (String key : val.keySet()) {
				writeString(out, key);
				keyOrder.add(key);
			}
			for (String key : keyOrder)
				writeObject(out, val.get(key));
		}
	}

	/**
	 * Not implemented
	 * 
	 * @param out
	 * @param val
	 * @throws NotImplementedException
	 */
	public static void writeByteArray(ByteBuf out, byte[] val) {
		throw new IllegalStateException("Encoding byte arrays is not implemented");
	}

	/**
	 * Encodes an integer as AMF0 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The integer to encode
	 */
	public static void writeIntAMF0(ByteBuf out, int val) {
		out.writeByte(0x00);
		out.writeDouble(val);
	}

	/**
	 * Encodes a string as AMF0 to the given buffer
	 * 
	 * @param out
	 *            The buffer to encode to
	 * @param val
	 *            The string to encode
	 * @throws EncodingException
	 */
	public static void writeStringAMF0(ByteBuf out, String val) {
		byte[] temp = null;
		try {
			temp = val.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unable to encode string as UTF-8: " + val);
		}
		out.writeByte(0x02);
		out.writeByte((temp.length & 0xFF00) >> 8);
		out.writeByte(temp.length & 0x00FF);
		out.writeBytes(temp);
	}

	public static final int VERSION = 0x03;
	protected static final Random rand = new Random();

	/**
	 * Generates a random UID, used for messageIDs
	 * 
	 * @return A random UID
	 */
	public static String randomUID() {
		byte[] bytes = new byte[16];
		rand.nextBytes(bytes);
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (i == 4 || i == 6 || i == 8 || i == 10)
				out.append('-');
			out.append(String.format("%02X", bytes[i]));
		}
		return out.toString();
	}
}
