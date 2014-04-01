package net.audio.example2;

import net.audio.recieve.AudioRecieveManage;
import net.audio.send.AudioSendManage;
import net.audio.example2.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * A straightforward example of how to stream AMR and H.263 to some public IP
 * using libstreaming. Note that this example may not be using the latest
 * version of libstreaming !
 */
public class testActivity extends Activity implements OnClickListener {

	private final static String TAG = "MainActivity";

	private Button mButton1, mButton2, mButton3, mButton4;
	private EditText mEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mEditText = (EditText) findViewById(R.id.editText1);
		mButton1 = (Button) findViewById(R.id.button1);
		mButton2 = (Button) findViewById(R.id.button2);
		mButton1.setOnClickListener(this);
		mButton2.setOnClickListener(this);

		mButton3 = (Button) findViewById(R.id.button3);
		mButton3.setOnClickListener(this);

		mButton4 = (Button) findViewById(R.id.button4);
		mButton4.setOnClickListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	AudioSendManage audioSendManage = new AudioSendManage();
	AudioRecieveManage audioRecieveManage = new AudioRecieveManage();

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button1) {
			audioSendManage.initialize();
		} else if (v.getId() == R.id.button2) {
			audioSendManage.start();
		} else if (v.getId() == R.id.button3) {
			audioRecieveManage.initialize();
		} else if (v.getId() == R.id.button4) {
			audioRecieveManage.start();
		}
	}

	/** Displays a popup to report the eror to the user */
	private void logError(final String msg) {
		final String error = (msg == null) ? "Error unknown" : msg;
		AlertDialog.Builder builder = new AlertDialog.Builder(testActivity.this);
		builder.setMessage(error).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
