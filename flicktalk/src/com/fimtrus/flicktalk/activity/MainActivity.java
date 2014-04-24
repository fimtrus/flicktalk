package com.fimtrus.flicktalk.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.fimtrus.flicktalk.R;
import com.fimtrus.flicktalk.fragment.CameraFragment;
import com.fimtrus.flicktalk.fragment.SplashFragment;
import com.fimtrus.flicktalk.view.MyCameraSurface;

public class MainActivity extends Activity {

	public static final int VOLUME_MUTE = 0;
	public static final int VOLUME_MIDDLE = 1;
	public static final int VOLUME_MAX = 2;
	
	public static int volume = 0;
	
	private FragmentManager mFragmentManager;
	private CameraFragment mCameraFragment;
	protected boolean flag;
	private Handler mSplashHandler;

	private MyCameraSurface mSurface;
	private SplashFragment mSplashFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		initialize();
	}

	private void initialize() {

		initializeFragments();
		initializeFields();
		initializeListeners();
		initializeView();
	}

	private void initializeFields() {
		mSplashHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0) {
					mFragmentManager.beginTransaction().remove(mSplashFragment).commit();
				}
			}
		};
		
	}

	private void initializeView() {
//		mSplashHandler.sendEmptyMessageDelayed(0, 2000);
	}

	private void initializeFragments() {
		// Utils.setPreference(Key.INTIALIZED_HELP, false);

		mFragmentManager = getFragmentManager();
		
		mCameraFragment = new CameraFragment();
		mSplashFragment = new SplashFragment();
		
		
		mFragmentManager.beginTransaction().add(R.id.fragment_camera, mCameraFragment, "camera").commit();
//		mFragmentManager.beginTransaction().add(R.id.fragment_splash, mSplashFragment, "splash").commit();
	}
	
	private void initializeListeners() {
		
	}
	
	public CameraFragment getCameraFragment() {
		return (CameraFragment) mCameraFragment;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.activity_main, menu);
	// return true;
	// }
}
