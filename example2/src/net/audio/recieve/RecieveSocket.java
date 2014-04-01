/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.audio.recieve;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.Semaphore;

import android.util.Log;

/**
 * A basic implementation of an RTP socket. It implements a buffering mechanism,
 * relying on a FIFO of buffers and a Thread. That way, if a packetizer tries to
 * send many packets too quickly, the FIFO will grow and packets will be sent
 * one by one smoothly.
 */
public class RecieveSocket implements Runnable {

	public static final String TAG = "RecieveSocket";

	public static final int RTP_HEADER_LENGTH = 12;
	public static final int MTU = 1300;

	private MulticastSocket mSocket;
	public DatagramPacket[] mPackets;
	private byte[][] mBuffers;
	private long[] mTimestamps;

	private Semaphore mBufferRequested, mBufferCommitted;
	private Thread mThread;

	private long mCacheSize;
	private long mOldTimestamp = 0;
	private int mBufferCount, mBufferIn, mBufferOut;
	private int mCount = 0;

	public void start() {
		if (mThread == null) {
			isListenning = true;
			mThread = new Thread(this);
			mThread.start();
		}
	}

	/**
	 * This RTP socket implements a buffering mechanism relying on a FIFO of
	 * buffers and a Thread.
	 * 
	 * @throws IOException
	 */
	public RecieveSocket() {

		mCacheSize = 00;
		mBufferCount = 300; // TODO: reajust that when the FIFO is full
		mBuffers = new byte[mBufferCount][];
		mPackets = new DatagramPacket[mBufferCount];

		for (int i = 0; i < mBufferCount; i++) {

			mBuffers[i] = new byte[MTU];
			mPackets[i] = new DatagramPacket(mBuffers[i], 1);

			/* Version(2) Padding(0) */
			/* ^ ^ Extension(0) */
			/* | | ^ */
			/* | -------- | */
			/* | |--------------------- */
			/* | || -----------------------> Source Identifier(0) */
			/* | || | */
			mBuffers[i][0] = (byte) Integer.parseInt("10000000", 2);

			/* Payload Type */
			mBuffers[i][1] = (byte) 96;

			/* Byte 2,3 -> Sequence Number */
			/* Byte 4,5,6,7 -> Timestamp */
			/* Byte 8,9,10,11 -> Sync Source Identifier */

		}

		try {
			mSocket = new MulticastSocket(8051);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	boolean isListenning = false;
	public int packetIndex = 0;

	/** The Thread sends the packets in the FIFO one by one at a constant rate. */
	@Override
	public void run() {
		try {
			while (isListenning == true) {
				mSocket.receive(mPackets[packetIndex % mBufferCount]);
				packetIndex = packetIndex + 1;
				Log.e(TAG, "A packet recieved. packetIndex=" + packetIndex);
			}
		} catch (Exception e) {
			isListenning = false;
			e.printStackTrace();
		}
		mThread = null;
	}
}
