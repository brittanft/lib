package org.summoners.rtmpp;

import java.nio.*;

/**
 * Representation of data sent between the client and server.
 * @author Brittan Thomas
 */
public class Packet {
	
	/**
	 * Instantiates a new packet with default values.<br>
	 * 	Implies non-direct, big-endianness.
	 */
	public Packet() {
		this(-1);
	}
	
	/**
	 * Instantiates a new packet.
	 *
	 * @param type
	 *            the type of packet
	 * @param dataBuffer
	 *            the buffer of data
	 */
	public Packet(int type) {
		this.type = type;
	}
	
	/**
	 * The type of packet.
	 */
	private int type;

	/**
	 * Gets the type of packet.
	 *
	 * @return the type of packet
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type of packet.
	 *
	 * @param type
	 *            the new type of packet
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * The buffer of data captured in this packet.
	 */
	private ByteBuffer dataBuffer;

	/**
	 * Gets the buffer of data captured in this packet.
	 *
	 * @return the buffer of data captured in this packet
	 */
	public ByteBuffer getDataBuffer() {
		return dataBuffer;
	}
	
	/**
	 * Sets the size of this packet's data buffer.
	 *
	 * @param size
	 *            the new size of this packet's data buffer
	 */
	public void setSize(int size) {
		dataBuffer = ByteBuffer.allocate(size);
	}
	
	/**
	 * Checks if this packet has completed reading.
	 *
	 * @return true, if the packet has no remaining data to read
	 */
	public boolean isComplete() {
		return dataBuffer.remaining() == 0;
	}
}
