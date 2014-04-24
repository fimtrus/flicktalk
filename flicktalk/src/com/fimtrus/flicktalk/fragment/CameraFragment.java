package com.fimtrus.flicktalk.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fimtrus.flicktalk.R;
import com.fimtrus.flicktalk.model.Constant;
import com.fimtrus.flicktalk.view.MyCameraSurface;
import com.jhlibrary.util.CommonDialogs;
import com.jhlibrary.util.ImageHelper;
import com.jhlibrary.util.Util;

public class CameraFragment extends Fragment implements View.OnTouchListener {

	private int INTENT_CALL_GALLERY = 3001;

	private int ROUND_VALUE = 7;
	private int mPreviewFormat;
	private Size mPreviewSize;

	private MyCameraSurface mSurface;
	private SurfaceHolder mHolder;
	private ViewGroup mRootLayout;
	private Button mShutterButton;
	private ImageButton mGalleryButton;
	private Button mSendKatalkButton;

	private String mCurrentFilePath = null;

	private boolean isPaused = false;
	private boolean isFocusSuccess = false;
	private boolean isAutoFocusing = false;
	private boolean isTake = false;

	private int mOpenedCamera = Camera.CameraInfo.CAMERA_FACING_BACK;

	AutoFocusCallback mAutoFocus = new AutoFocusCallback() {

		public void onAutoFocus(boolean success, Camera camera) {
			isFocusSuccess = success;
			isAutoFocusing = false;
		}
	};
	protected byte[] mPictureData;
	private Button mChangeCameraButton;

	private CommonDialogs mCommonDialog;

	private TextView mTextView;

	@Override
	public void onStop() {
		super.onPause();
		Camera camera = mSurface.getCamera();
		// if (camera != null) {
		// try {
		// Log.i("MyCamera", "onPause");
		// camera.stopPreview();
		// camera.release();
		// } catch (Exception e) {
		// }
		// }
		if (camera != null) {
			try {
				Log.i("MyCamera", "onPause");
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				// camera.startPreview();
				// camera = null;
				// camera = null;
				isPaused = true;
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!isPaused) {
			return;
		}
		isPaused = false;
		initializeCamera();
	}

	private void initializeCamera() {

		Camera camera = mSurface.getCamera();
		// if (camera != null) {
		// try {
		Log.i("MyCamera", "onResume");
		// camera = Camera.open();
		// camera.setPreviewTexture(mSurface.getSurfaceTexture());
		// camera.startPreview();
		// } catch (Exception e) {
		// }
		// }
		try {

			camera = Camera.open();
			mSurface.setCamera(camera);
			camera.setPreviewCallback(mSurface);
			camera.setDisplayOrientation(90);
			camera.setPreviewTexture(mSurface.getSurfaceTexture());

			Log.i("MyCamera", "surfaceChanged");
			Camera.Parameters params = camera.getParameters();

			params.setPreviewFormat(ImageFormat.NV21);

			mPreviewFormat = params.getPreviewFormat();
			mPreviewSize = params.getPreviewSize();
			// params.setPictureSize(mPreviewSize.width, mPreviewSize.height);

			params.setPreviewFpsRange(30000, 30000);

			// List<Size> arSize = params.getSupportedPreviewSizes();
			// List<Size> arSize = params.getSupportedPictureSizes();
			// params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			try {
				camera.setParameters(params);
			} catch (Exception e) {
//				Toast.makeText(getActivity(), "Camera Param failed", Toast.LENGTH_LONG).show();
			}
			camera.startPreview();
			Camera.Parameters p = camera.getParameters();
			List<String> focusModes = p.getSupportedFocusModes();

			if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				// Phone supports autofocus!
				camera.autoFocus(null);
			} else {
				// Phone does not support autofocus!
			}

			// mShutterButton.setVisibility(View.VISIBLE);
			// mRestartButton.setVisibility(View.GONE);

		} catch (Exception e) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootLayout = (ViewGroup) inflater.inflate(R.layout.fragment_camera, container, false);

		initialize();
		return mRootLayout;
	}

	private void initialize() {

		initializeFields();
		initializeListeners();
		initializeView();

	}

	private void initializeFields() {
		mSurface = (MyCameraSurface) mRootLayout.findViewById(R.id.preview);
		mShutterButton = (Button) mRootLayout.findViewById(R.id.button_capture);
		mGalleryButton = (ImageButton) mRootLayout.findViewById(R.id.button_gallery);
		mSendKatalkButton = (Button) mRootLayout.findViewById(R.id.button_katalk);
		mChangeCameraButton = (Button) mRootLayout.findViewById(R.id.button_change_camera);
		mTextView = (TextView) mRootLayout.findViewById(R.id.outlineTextView1);
		
		mCommonDialog = new CommonDialogs(getActivity());
	}

	private void initializeListeners() {

		mSurface.setOnTouchListener(this);

		mShutterButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (!isTake) {
					// 캡쳐 버튼이 여러번 눌려지는 걸 방지함.
					isTake = true;
					mSurface.capture();
				}
			}
		});
		mGalleryButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				callGallery();
			}
		});
		mSendKatalkButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				sendImageToKakao(mCurrentFilePath);
			}
		});
		mChangeCameraButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				changeCamera();

			}
		});

	}

	/**
	 * 카카오톡으로 이미지를 보낸다.
	 * 
	 * @param filePath
	 *            : 파일 경로
	 */
	private void sendImageToKakao(String filePath) {

		if (filePath == null) {
			Toast.makeText(getActivity(), R.string.must_take_picture, Toast.LENGTH_SHORT).show();
			return;
		}

		File directoryFile = Environment.getExternalStoragePublicDirectory("flicktalk");
		File imageFile = new File(directoryFile, filePath);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");

		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFile.getAbsolutePath()));
		intent.setPackage("com.kakao.talk");

		startActivity(intent);
	}

	/**
	 * 카카오톡으로 이미지를 보낸다.
	 * 
	 * @param filePath
	 *            : 파일 경로
	 */
	private void sendImageToKakao(Uri uri) {

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");

		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setPackage("com.kakao.talk");

		startActivity(intent);
	}

	private void initializeView() {
		mSurface.setPictureCallback(mPictureCallback);

		Object obj = Util.getPreference(getActivity(), Constant.PREFERENCE_CURRENT_FILE);

		if (obj != null) {
			mCurrentFilePath = (String) obj;
			try {

				Bitmap thumbnail = getThumbnail(Environment.getExternalStoragePublicDirectory("flicktalk")
						.getAbsolutePath() + mCurrentFilePath);

				// 메모리 해제하고...
				// Canvas 를 통해 radius를 그린다..
				mGalleryButton.setImageBitmap(ImageHelper.getRoundedCornerBitmap(thumbnail,
						Util.getPxFromDp(ROUND_VALUE)));
				thumbnail.recycle();
			} catch (Exception e) {
				mCurrentFilePath = null;
			}
		}
	}

	public MyCameraSurface getSurface() {
		return mSurface;
	}

	Camera.PictureCallback mPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(final byte[] data, final Camera camera) {

			TextWatcher watcher = new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					mTextView.setText(s);
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					
				}
			};
			
			mCommonDialog.showInputDialog(R.string.input_title, new CommonDialogs.OnClickListener() {
				
				@Override
				public void onClick(boolean isPositive, String text, AlertDialog dialog, View button, EditText editText) {
					if ( isPositive ) {
						mTextView.setText(text);
						savePicture(data, camera);
						dialog.dismiss();
					} else {
						isTake = false;
						mSurface.getCamera().startPreview();
						dialog.dismiss();
//						mTextView.setText("");
					}
					mTextView.setText("");
					
				}
			}, watcher);
			
		}
	};

	/**
	 * 사진을 저장한다.
	 */
	public void savePicture(byte[] data, Camera camera) {

		// mSurface.getCamera().stopPreview();
		mPictureData = data;
		File directoryFile = Environment.getExternalStoragePublicDirectory("flicktalk");
		SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		mCurrentFilePath = "/flicktalk_" + timeStampFormat.format(new Date()) + ".png";

		Util.setPreference(getActivity(), Constant.PREFERENCE_CURRENT_FILE, mCurrentFilePath);

		File imageFile = new File(directoryFile, mCurrentFilePath);
		FileOutputStream fos = null;
		try {

			if (!directoryFile.exists()) {
				directoryFile.mkdir();
			}
			fos = new FileOutputStream(imageFile);

			Bitmap bmp = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length);
			
			// Getting width & height of the given image.
			if (bmp != null) {
				int w = bmp.getWidth();
				int h = bmp.getHeight();
				// Setting post rotate to 90
				Matrix mtx = new Matrix();
				mtx.postRotate(90);
				// Rotating Bitmap
				Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
				Bitmap textBitmap = loadBitmapFromView(mTextView);
				
				rotatedBMP = overlayMark(rotatedBMP, textBitmap, 0, 0);
				
				rotatedBMP.compress(Bitmap.CompressFormat.PNG, 100, fos);
				
				Bitmap thumbnail = getThumbnail(rotatedBMP);
				
				// 메모리 해제하고...
				rotatedBMP.recycle();
				// Canvas 를 통해 radius를 그린다..
				mGalleryButton.setImageBitmap(ImageHelper.getRoundedCornerBitmap(thumbnail,
						Util.getPxFromDp(ROUND_VALUE)));

				// 썸네일도 해제
				fos.write(mPictureData);
				thumbnail.recycle();
				mPictureData = null;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			isTake = false;
			try {
				mSurface.getCamera().startPreview();
				fos.close();
				getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
//				mTextView.setText("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// if ( mShutterButton.isShown() ) {
		// mShutterButton.setVisibility(View.GONE);
		// mRestartButton.setVisibility(View.VISIBLE);
		// } else {
		//
		// }

	}

	/**
	 * 갤러리를 호출한다.
	 */
	public void callGallery() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK); // 이거랑 아래꺼 소스가 갤러리 접근 소스
		intent.setType("image/*");
		startActivityForResult(intent, INTENT_CALL_GALLERY);
	}

	/**
	 * 카메라 화면을 전환한다.
	 */
	private void changeCamera() {

		if (isAutoFocusing) {
			return;
		}

		if (mOpenedCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {

			mOpenedCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;

		} else {
			mOpenedCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		mSurface.initializeCamera(mOpenedCamera, mSurface.getSurfaceTexture());
	}

	/**
	 * 오토 포커스를 잡는다.
	 */
	private void autoFocus() {

		if (mSurface == null || mSurface.getCamera() == null)
			return;

		if (mSurface.isFocusable) {
			// Phone supports autofocus!
			isAutoFocusing = true;
			mSurface.getCamera().autoFocus(mAutoFocus);
		} else {
			// Phone does not support autofocus!
		}
	}

	/**
	 * 썸네일을 얻어온다.
	 */
	private Bitmap getThumbnail(Bitmap bitmap) {
		return Util.resizeBitmap(getActivity(), bitmap, Util.getPxFromDp(51), Util.getPxFromDp(51));
	}

	/**
	 * 썸네일을 얻어온다.
	 */
	private Bitmap getThumbnail(String url) {
		return Util.resizeBitmap(getActivity(), url, Util.getPxFromDp(51), Util.getPxFromDp(51));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			if (v.getId() == R.id.preview) {
				autoFocus();
				return true;
			}

			break;
		}

		return false;
	}

	/**
	 * 뷰로부터 비트맵을 추출한다.
	 */
	public static Bitmap loadBitmapFromView(View v) {
		v.setDrawingCacheEnabled(true);
		v.buildDrawingCache();
		Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);
//		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
//		Canvas c = new Canvas(b);
//		v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
//		v.draw(c);
		return b;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == INTENT_CALL_GALLERY && resultCode == Activity.RESULT_OK) {

			Uri uri = data.getData();
			sendImageToKakao(uri);

		}
	}
	
	/**
	 * 두 이미지를 합성한다.
	 * @param baseBmp
	 * @param overlayBmp
	 * @param distanceLeft
	 * @param distanceTop
	 * @return
	 */
	public static Bitmap overlayMark(Bitmap baseBmp, Bitmap overlayBmp, int distanceLeft, int distanceTop) {
		
		if ( overlayBmp.getWidth() > baseBmp.getWidth() ) {
			float scale = ( baseBmp.getWidth() / overlayBmp.getWidth() ) * 100;
			Matrix matrix = new Matrix(); 
			matrix.postScale(scale, scale); 
			overlayBmp = Bitmap.createBitmap(overlayBmp, 0, 0, overlayBmp.getWidth(), overlayBmp.getHeight(), matrix, true); 
		}
//		
		WeakReference<Bitmap> resultBitmapReference = new WeakReference<Bitmap>(Bitmap.createBitmap(baseBmp.getWidth() + distanceLeft, baseBmp.getHeight() + distanceTop,
				baseBmp.getConfig()));
		
		WeakReference<Canvas> canvasReference = new WeakReference<Canvas>(new Canvas(resultBitmapReference.get()));
		
		canvasReference.get().drawBitmap(baseBmp, distanceLeft, distanceTop, null);
		canvasReference.get().drawBitmap(overlayBmp, ( baseBmp.getWidth() - overlayBmp.getWidth() ) / 2, ( baseBmp.getHeight() - overlayBmp.getHeight() ), null);
		return resultBitmapReference.get();
	}
}
