package com.fimtrus.flicktalk.view;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.fimtrus.flicktalk.activity.MainActivity;

//미리보기 표면 클래스
public class MyCameraSurface extends TextureView implements Camera.PreviewCallback, TextureView.SurfaceTextureListener {

	// private SurfaceHolder mHolder;
	private Camera mCamera = null;

	private int mCount = 0;
	private Context mContext;

	private TakeCallback mTakeCallback;
	private Camera.PictureCallback mPictureCallback;
	private boolean isTaken;
	private Size mPreviewSize;
	private int mPreviewFormat;
	
	private boolean isInit = false;
	
	public boolean isFocusable = false;

	public MyCameraSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		this.setSurfaceTextureListener(this);
		// mHolder = getHolder();
		// mHolder.addCallback(this);
	}

	public Camera getCamera() {
		return mCamera;
	}
	
	public void setCamera(Camera camera) {
		mCamera = camera;
	}

	public void capture() {


//		Camera.Parameters params = mCamera.getParameters();
//		int time = 200;
//		if (MainActivity.volume == MainActivity.VOLUME_MUTE) {
//			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//			time = 3000;
//		}
//		mCamera.setParameters(params);
//		mDelayHandler.sendEmptyMessageDelayed(0, 200);
//		mTakeEndHandler.sendEmptyMessageDelayed(0, time);
		mCamera.takePicture(new ShutterCallback() {
			
			@Override
			public void onShutter() {
				 AudioManager mgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
				 mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
			}
		}, null, mPictureCallback);
	}
	
	private Handler mTakeEndHandler = new Handler () {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (mTakeCallback != null) {
				mTakeCallback.takeEnd();
			}
			if (MainActivity.volume == MainActivity.VOLUME_MUTE) {
				Camera.Parameters params = mCamera.getParameters();
				params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(params);
			}
			
		}
		
	};
	
	public void setTakeCallback(TakeCallback callback) {
		this.mTakeCallback = callback;
	}

	public void setPictureCallback(Camera.PictureCallback callback) {
		this.mPictureCallback = callback;
	}

	public interface TakeCallback {
		void takeEnd();
	}

	ArrayList<byte[]> mDatas = new ArrayList<byte[]>();

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		// camera.startPreview();
//		if (isTaken) {
//
//			if (mCount < 3) {
//				mDatas.add(data);
//				isTaken = false;
//				mDelayHandler.sendEmptyMessageDelayed(0, 800);
//				// 이미지를 먼저 뽑아서....만들고
//				if (mCount == 0) {
//
//					// Yuv이미지를 만들고...
//					YuvImage img = null;
//					img = new YuvImage(data, ImageFormat.NV21, mPreviewSize.width, mPreviewSize.height, null);
//					Matrix matrix = new Matrix();
//					matrix.setRotate(90);
//
//					ByteArrayOutputStream out = new ByteArrayOutputStream();
//					int widthCenter = mPreviewSize.width / 2;
//					int heightPixel = mPreviewSize.height / 2;
//					Rect rect = new Rect(widthCenter - 100, heightPixel - 100, widthCenter + 100, heightPixel + 100);
//					img.compressToJpeg(rect, 100, out);
//					byte[] imgBytes = out.toByteArray();
//
//					Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
//					Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
//							true);
//
////					mPictureCallback.getFirstPicture(resized);
//
//				}
//
//				mCount++;
//				// mCamera.takePicture(null, null, this);
//			} else {
//				mCount = 0;
//				isTaken = false;
//
//				// RefreshCanvas();
//				// Camera.Parameters params = mCamera.getParameters();
//				// params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//				// mCamera.setParameters(params);
//				// MyCameraSurface.this.setVisibility(View.INVISIBLE);
//				mStopPreviewHandler.sendEmptyMessageDelayed(0, 3000);
//				// mCamera.stopPreview();
//				// mCamera.release();
//				// mCamera = null;
//				// Log.i("MyCamera", "Exception");
//				new SaveDataTask().execute();
//				// new SaveDataTask().execute(mDatas);
//				// mCamera = null;
//			}
//		}
	}

	Handler mStopPreviewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MyCameraSurface.this.setVisibility(View.INVISIBLE);
			// if (mCamera != null) {
			// mCamera.stopPreview();
			// mCamera.release();
			// mCamera = null;
			// }
		}
	};

	public class SaveDataTask extends AsyncTask<Void, Bitmap, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			for (int i = 0; i < mDatas.size(); i++) {

				// Yuv이미지를 만들고...
				YuvImage img = null;
				img = new YuvImage(mDatas.get(i), ImageFormat.NV21, mPreviewSize.width, mPreviewSize.height, null);
				Matrix matrix = new Matrix();
				matrix.setRotate(90);

				// 파일로 저장.
				if (MainActivity.volume == MainActivity.VOLUME_MUTE) {

					FileOutputStream fos = null;
					SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
					String fileName = "iris_" + timeStampFormat.format(new Date()) + ".jpg";
//					File sd = Environment.getExternalStoragePublicDirectory(".iris_security");
					//TODO : 
//					File sd = new File(mContext.getFilesDir() + "/.iris_security");
//					sd.mkdirs();

					Rect rect = new Rect(0, 0, mPreviewSize.width, mPreviewSize.height);

					OutputStream outStream = null;
//					File file = new File(sd, fileName);
					try {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						img.compressToJpeg(rect, 100, out);
//						outStream = new FileOutputStream(file);
						outStream = mContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
						byte[] imgBytes = out.toByteArray();
						Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
						Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
								matrix, true);
						
//						if (Util.isLite(mContext)) {
//							Bitmap overlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
//							Bitmap watermarkedBitmap = overlayMark(resized, overlayBitmap, 0, 0);
//							watermarkedBitmap.compress(CompressFormat.JPEG, 100, outStream);
//						} else {
//							Bitmap overlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
//							Bitmap watermarkedBitmap = overlayMarkFullVer(resized, overlayBitmap, 0, 0);
//							watermarkedBitmap.compress(CompressFormat.JPEG, 100, outStream);
//						}
						
						
						outStream.flush();
						outStream.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// if (mTakeCallback != null) {
			// mTakeCallback.takeEnd();
			// }
		}

	}

	Handler mDelayHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			isTaken = true;
		}

	};

	
	/**
	 * 카메라 init
	 * @param surface
	 */
	public void initializeCamera( int cameraId, SurfaceTexture surface ) {
		
	try {
	
		if ( mCamera != null ) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		
		mCamera = Camera.open( cameraId );
		mCamera.setPreviewCallback(this);
		mCamera.setDisplayOrientation(90);
		mCamera.setPreviewTexture(surface);
		
		Log.i("MyCamera", "surfaceChanged");
		Camera.Parameters params = mCamera.getParameters();

		params.setPreviewFormat(ImageFormat.NV21);

		mPreviewFormat = params.getPreviewFormat();
		mPreviewSize = params.getPreviewSize();
		params.setPictureSize(mPreviewSize.width, mPreviewSize.height);

		params.setPreviewFpsRange(30000, 30000);
		List<Size> arSize = params.getSupportedPreviewSizes();
		
//		params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		params.setPreviewSize(arSize.get(0).width, arSize.get(0).height);
		try {
			mCamera.setParameters(params);
			
		} catch (Exception e) {
//			Toast.makeText(mContext, "Camera Param failed", Toast.LENGTH_LONG).show();
		}
		mCamera.startPreview();
		Camera.Parameters p = mCamera.getParameters();
		List<String> focusModes = p.getSupportedFocusModes();

		if (focusModes != null && ( isFocusable = focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) ) ) {
			// Phone supports autofocus!
//			mCamera.autoFocus(null);
		} else {
			// Phone does not support autofocus!
		}
	} catch (Exception e) {
		Log.i("MyCamera", "Exception");
		try {
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
				// mCamera = null;
			}	
		}catch (Exception e1) {}
		 
		// mCamera = null;
	}
	}
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		Log.i("MyCamera", "onSurfaceTextureAvailable");
		initializeCamera( Camera.CameraInfo.CAMERA_FACING_BACK, surface );
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//		if (mCamera != null) {
//			try {
//				Log.i("MyCamera", "onSurfaceTextureDestroyed");
//				mCamera.setPreviewCallback(null);
//				mCamera.stopPreview();
//				mCamera.release();
//				// mCamera = null;
//				// mCamera = null;
//			} catch (Exception e) {
//			}
//		}
		return false;
	}
	
	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		Log.i("MyCamera", "onSurfaceTextureSizeChanged");
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

}