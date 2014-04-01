package net.audio.send;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import android.media.MediaCodec;

public class AudioSendManage {

	public AACStream mAACStream;

	public MediaCodec mMediaCodec;

	public MediaCodecInputStream mMediaCodecInputStream;

	public ByteBuffer[] mMediaCodecInputBuffers;
	public ByteBuffer[] mMediaCodecOutputBuffers = null;

	public AACLATMPacketizer mAACLATMPacketizer;

	public SendSocket sendSocket;

	InetSocketAddress dest;
	int port;

	public void initialize() {
		mAACStream = new AACStream();
		mAACStream.initializeMediaCodec();
		mMediaCodec = mAACStream.mMediaCodec;

		mMediaCodecInputBuffers = mAACStream.mMediaCodecInputBuffers;
		mMediaCodecInputStream = new MediaCodecInputStream(mMediaCodec);
		mMediaCodecOutputBuffers = mMediaCodecInputStream.mMediaCodecOutputBuffers;

		mAACLATMPacketizer = new AACLATMPacketizer();
		mAACLATMPacketizer.mMediaCodecInputStream = mMediaCodecInputStream;

		sendSocket = new SendSocket();
		dest = new InetSocketAddress("192.168.1.7", 8051);
		port = 8051;
		sendSocket.setDestination(dest, port, 8080);
		sendSocket.setCacheSize(0);

		mAACLATMPacketizer.sendSocket = sendSocket;

	}

	public void start() {
		try {
			mAACStream.encodeWithMediaCodec();

			mAACLATMPacketizer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
