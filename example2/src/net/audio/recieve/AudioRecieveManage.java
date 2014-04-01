package net.audio.recieve;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public class AudioRecieveManage {

	public RecieveSocket recieveSocket;
	public AACLATMunPacketizer mAACLATMunPacketizer;

	public void initialize() {

		recieveSocket = new RecieveSocket();
		
		initializeMediaCodec();
		
		mAACLATMunPacketizer= new AACLATMunPacketizer();
		mAACLATMunPacketizer.recieveSocket = recieveSocket;
		mAACLATMunPacketizer.decoder = decoder;

	}

	public void start() {
		recieveSocket.start();
		mAACLATMunPacketizer.start();
	}

	protected AudioQuality mRequestedQuality = AudioQuality.DEFAULT_AUDIO_QUALITY.clone();
	protected AudioQuality mQuality = mRequestedQuality.clone();

	public MediaCodecInputStream inputStream;

	public MediaCodec decoder;
	public ByteBuffer[] inputDecoderBuffers;
	public ByteBuffer[] outputDecoderBuffers;

	public int bufferSize;

	@SuppressLint({ "InlinedApi", "NewApi" })
	public void initializeMediaCodec() {

		bufferSize = AudioRecord.getMinBufferSize(mQuality.samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;

		decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
		MediaFormat format = new MediaFormat();
		format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
		format.setInteger(MediaFormat.KEY_BIT_RATE, mQuality.bitRate);
		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
		format.setInteger(MediaFormat.KEY_SAMPLE_RATE, mQuality.samplingRate);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
		decoder.configure(format, null, null, 0);

		decoder.start();

		inputDecoderBuffers = decoder.getInputBuffers();
		outputDecoderBuffers = decoder.getOutputBuffers();

	}

}
