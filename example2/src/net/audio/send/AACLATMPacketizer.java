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

package net.audio.send;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.media.MediaCodec.BufferInfo;
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
public class AACLATMPacketizer implements Runnable {

	private final static String TAG = "AACLATMPacketizer";

	private Thread t;

	protected static final int rtphl = SendSocket.RTP_HEADER_LENGTH;

	// Maximum size of RTP packets
	protected final static int MAXPACKETSIZE = SendSocket.MTU - 28;

	public SendSocket sendSocket = null;
	public MediaCodecInputStream mMediaCodecInputStream = null;
	protected byte[] buffer;

	protected long ts = 0;

	public AACLATMPacketizer() {
		super();
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	public void stop() {
		if (t != null) {
			mMediaCodecInputStream.close();
			
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
			}
			t = null;
		}
	}

	public void setSamplingRate(int samplingRate) {
		sendSocket.setClockFrequency(samplingRate);
	}

	@SuppressLint("NewApi")
	public void run() {

		Log.d(TAG, "AAC LATM packetizer started !");

		int length = 0;
		long oldts;
		BufferInfo bufferInfo;

		try {
			while (!Thread.interrupted()) {
				buffer = sendSocket.requestBuffer();
				length = mMediaCodecInputStream.read(buffer, rtphl + 4, MAXPACKETSIZE - (rtphl + 4));

				if (length > 0) {

					bufferInfo = mMediaCodecInputStream.getLastBufferInfo();
					// Log.d(TAG,"length: "+length+" ts: "+bufferInfo.presentationTimeUs);
					oldts = ts;
					ts = bufferInfo.presentationTimeUs * 1000;

					// Seems to happen sometimes
					if (oldts > ts) {
						sendSocket.commitBuffer();
						continue;
					}

					sendSocket.markNextPacket();
					sendSocket.updateTimestamp(ts);

					// AU-headers-length field: contains the size in bits of a
					// AU-header
					// 13+3 = 16 bits -> 13bits for AU-size and 3bits for
					// AU-Index / AU-Index-delta
					// 13 bits will be enough because ADTS uses 13 bits for
					// frame length
					buffer[rtphl] = 0;
					buffer[rtphl + 1] = 0x10;

					// AU-size
					buffer[rtphl + 2] = (byte) (length >> 5);
					buffer[rtphl + 3] = (byte) (length << 3);

					// AU-Index
					buffer[rtphl + 3] &= 0xF8;
					buffer[rtphl + 3] |= 0x00;

					send(rtphl + length + 4);

				} else {
					sendSocket.commitBuffer();
				}

			}
		} catch (IOException e) {
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.e(TAG, "ArrayIndexOutOfBoundsException: " + (e.getMessage() != null ? e.getMessage() : "unknown error"));
			e.printStackTrace();
		} catch (InterruptedException ignore) {
		}

		Log.d(TAG, "AAC LATM packetizer stopped !");

	}

	/** Updates data for RTCP SR and sends the packet. */
	protected void send(int length) throws IOException {
		sendSocket.commitBuffer(length);
	}

	/** Used in packetizers to estimate timestamps in RTP packets. */
	protected static class Statistics {

		public final static String TAG = "Statistics";

		private int count = 700, c = 0;
		private float m = 0, q = 0;
		private long elapsed = 0;
		private long start = 0;
		private long duration = 0;
		private long period = 10000000000L;
		private boolean initoffset = false;

		public Statistics() {
		}

		public Statistics(int count, int period) {
			this.count = count;
			this.period = period;
		}

		public void reset() {
			initoffset = false;
			q = 0;
			m = 0;
			c = 0;
			elapsed = 0;
			start = 0;
			duration = 0;
		}

		public void push(long value) {
			elapsed += value;
			if (elapsed > period) {
				elapsed = 0;
				long now = System.nanoTime();
				if (!initoffset || (now - start < 0)) {
					start = now;
					duration = 0;
					initoffset = true;
				}
				// Prevents drifting issues by comparing the real duration of
				// the
				// stream with the sum of all temporal lengths of RTP packets.
				value += (now - start) - duration;
				// Log.d(TAG,
				// "sum1: "+duration/1000000+" sum2: "+(now-start)/1000000+" drift: "+((now-start)-duration)/1000000+" v: "+value/1000000);
			}
			if (c < 5) {
				// We ignore the first 20 measured values because they may not
				// be accurate
				c++;
				m = value;
			} else {
				m = (m * q + value) / (q + 1);
				if (q < count)
					q++;
			}
		}

		public long average() {
			long l = (long) m;
			duration += l;
			return l;
		}

	}

}
