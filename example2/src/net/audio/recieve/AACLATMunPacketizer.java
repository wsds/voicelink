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

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.util.Log;

/**
 * RFC 3640.
 * 
 * Encapsulates AAC Access Units in RTP packets as specified in the RFC 3640.
 * This packetizer is used by the AACStream class in conjunction with the
 * MediaCodec API introduced in Android 4.1 (API Level 16).
 * 
 */
@SuppressLint("NewApi")
public class AACLATMunPacketizer implements Runnable {

	private final static String TAG = "AACLATMunPacketizer";

	private Thread thread;

	protected static final int rtphl = RecieveSocket.RTP_HEADER_LENGTH;

	// Maximum size of RTP packets
	protected final static int MAXPACKETSIZE = RecieveSocket.MTU - 28;

	public RecieveSocket recieveSocket = null;

	public MediaCodec decoder;
	public ByteBuffer[] inputDecoderBuffers;

	public AACLATMunPacketizer() {
		super();
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			// mMediaCodecInputStream.close();

			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
			thread = null;
		}
	}

	int unPackIndex = 0;

	@SuppressLint("NewApi")
	public void run() {
		Log.e(TAG, "AAC LATM unpacketizer started !");
		DatagramPacket packingPacket;
		while (!Thread.interrupted()) {
			Log.e(TAG, "unpacking loop running !");
			if (unPackIndex < recieveSocket.packetIndex) {
				if (recieveSocket.packetIndex - unPackIndex > 10) {
					unPackIndex = recieveSocket.packetIndex - 5;
				}
				packingPacket = recieveSocket.mPackets[unPackIndex];
				int inputBufferIndex = decoder.dequeueInputBuffer(-1);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = inputDecoderBuffers[inputBufferIndex];
					inputBuffer.clear();
//					byte[] data = packingPacket.getData();
//					inputBuffer.put(data);

					decoder.queueInputBuffer(inputBufferIndex, 0, 1024, 0, 0);
				}
				unPackIndex++;
				Log.e(TAG, "unPack A packet  unPackIndex=" + unPackIndex);
			}else{
				try {
					Log.e(TAG, "sleep");
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}
		Log.e(TAG, "unpacking loop exit!!!!!!!!!!!");
	}

}
