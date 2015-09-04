package com.carrecorder.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.carrecorder.conf.ActivityConf;
import com.carrecorder.conf.EnvConf;
import com.carrecorder.sensor.GPS;
import com.carrecorder.sensor.GPSListener;
import com.carrecorder.utils.camera.CameraUtils;
import com.carrecorder.utils.debug.Log;
import com.db.DBExecutor;
import com.db.sql.Sql;

import myjob.carrecorder.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RecorderActivity extends Activity implements GPSListener {
	private Button mVideoStartBtn;
	private SurfaceView mSurfaceview;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	private File mRecVedioPath;
	private File mRecAudioFile;
	private TextView timer;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;
	private int parentId;
	protected Camera cameraDevice;
	protected boolean isPreview;
	private boolean isRecording = true; // true-->no record,click to start；
	// false-->recording,click to pause;
	private TextToSpeech text2speech;
	private TextView timerTextView;
	private TextView lightTextView;
	private TextView gpsTextView;
	private SensorManager mySensorManager;
	private int clo = 0;
	private int range;// the flag for notice text
	private int k = 0;
	private int m = 0;
	private SensorEventListener sensorEventListener;
	float speed;
	double lat;
	double lng;
	boolean symbol1;
	boolean symbol2;
	private int or;
	private float f = 1.0f;
	private int recorderCheckBoxStatus;
	private int lightNoticeCheckBoxStatus;
	private int overspeedNoticeCheckBoxStatus;
	private final int isChoice = 1;
	private GPS gps;
	private BrightnessManager brightnessManager;
	private DBExecutor dbExecutor;
	private Sql sql;
	private void initView() {
		// to full screen show
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_recorder);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		timer = (TextView) findViewById(R.id.timer);
		mVideoStartBtn = (Button) findViewById(R.id.arc_hf_video_start);
		mSurfaceview = (SurfaceView) this.findViewById(R.id.surface_camera);
		timerTextView = (TextView) findViewById(R.id.timer_textview);
		lightTextView = (TextView) findViewById(R.id.light_textview);
		gpsTextView = (TextView) findViewById(R.id.gps_textview);
	}

	private void initSensor() {
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	private void initListener() {
		mVideoStartBtn.setOnClickListener(new StartBtnLisener());
	}

	private void initIntentData() {
		Intent intent = getIntent();
		recorderCheckBoxStatus = intent.getIntExtra(ActivityConf.intent_range1,
				0);
		lightNoticeCheckBoxStatus = intent.getIntExtra(
				ActivityConf.intent_range2, 0);
		overspeedNoticeCheckBoxStatus = intent.getIntExtra(
				ActivityConf.intent_range3, 0);
		parentId = getIntent().getIntExtra("parentId", 0);
	}

	private void initDynamicView() {
		if (recorderCheckBoxStatus == isChoice) {
			mVideoStartBtn.setVisibility(View.VISIBLE);
		} else {
			mVideoStartBtn.setVisibility(View.GONE);
		}
		if (lightNoticeCheckBoxStatus != isChoice) {
			timerTextView.setVisibility(View.GONE);
			lightTextView.setVisibility(View.GONE);
		}
		// set timer invisible
		timer.setVisibility(View.GONE);
		// set buffer path
		mRecVedioPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/hfdatabase/video/temp/");
		Toast.makeText(this,Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/hfdatabase/video/temp/" , Toast.LENGTH_LONG).show();
		if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdirs();
		}
	}

	private void initCamera() {
		// bind the preview view , SurfaceHolder is a controller of SurfaceView
		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new CarmeraCallBack());
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//this is
		// ignored, this value is set automatically when needed.
	}

	private void initOther() {
		text2speech = new TextToSpeech(getApplicationContext(),
				new MyText2SpeechListener());
		gps = new GPS(this);
		brightnessManager = new BrightnessManager();
		brightnessManager.keepAwake();
		dbExecutor = DBExecutor.getInstance(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initIntentData();
		initSensor();
		initDynamicView();
		initCamera();
		initListener();
		initOther();
	}

	private Handler handler5 = new Handler();
	private Runnable task5 = new Runnable() {
		@Override
		public void run() {
			Intent intent1 = getIntent();
			final int rank2_1 = intent1.getIntExtra("range2_1", 0);
			final int rank2_2 = intent1.getIntExtra("range2_2", 0);
			handler5.postDelayed(task5, 8000);
			mSurfaceHolder.getSurfaceFrame();
			View view = getWindow().getDecorView();
			view.buildDrawingCache(true);
			Display display = RecorderActivity.this.getWindowManager()
					.getDefaultDisplay();
			view.layout(0, 0, display.getWidth(), display.getHeight());
			view.setDrawingCacheEnabled(true);
			Bitmap scrBmp = Bitmap.createBitmap(view.getDrawingCache());
			for (int i = 0; i < scrBmp.getWidth(); i = i + 30) {
				for (int j = 0; j < scrBmp.getHeight(); j = j + 30) {
					int col = scrBmp.getPixel(i, j);
					int alpha = col & 0xFF000000;
					int red = (col & 0x00FF0000) >> 16;
					int green = (col & 0x0000FF00) >> 8;
					int blue = (col & 0x000000FF);
					int grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
					m = m + 1;
					k = k + grey;
					System.out.println("grey=" + grey);
				}
			}
			scrBmp.recycle();
			System.out.println("m=" + m);
			System.out.println("k=" + k);
			if (50 * m <= k && 100 * m >= k) {
				if (rank2_1 == isChoice) {
					timerTextView.setText(EnvConf.NOTICE_WEAK_LIGHT_1);
					timerTextView.setVisibility(View.VISIBLE);
					timerTextView.setTextColor(Color.YELLOW);
					lightTextView.setVisibility(View.GONE);
				}
				range = isChoice;
				if (rank2_2 == isChoice) {
					voiceToast();
				}
			}
			if (50 * m >= k) {
				if (rank2_1 == isChoice) {
					lightTextView
							.setText(EnvConf.NOTICE_WEAK_LIGHT_1_MUTIL_LINE);
					lightTextView.setVisibility(View.VISIBLE);
					lightTextView.setTextColor(Color.YELLOW);
					timerTextView.setVisibility(View.GONE);
					sparkBackground();
				}
				range = 2;
				if (rank2_2 == isChoice) {
					voiceToast();
				}
			}
			if (100 * m <= k) {
				timerTextView.setVisibility(View.GONE);
				lightTextView.setVisibility(View.GONE);
//				Toast.makeText(getApplicationContext(),
//						EnvConf.NOTICE_GOOD_LIGHT, Toast.LENGTH_LONG).show();
			}

			k = 0;
			m = 0;
		}
	};
	private Handler handler4 = new Handler();
	private Runnable task4 = new Runnable() {

		@Override
		public void run() {
			Intent intent1 = getIntent();
			final int rank2_1 = intent1.getIntExtra("range2_1", 0);
			final int rank2_2 = intent1.getIntExtra("range2_2", 0);

			handler4.postDelayed(this, 8000);
			cameraDevice.setOneShotPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
							camera.getParameters().getPreviewSize().width,
							camera.getParameters().getPreviewSize().height,
							null);
					int mWidth = camera.getParameters().getPreviewSize().width;
					int mHeight = camera.getParameters().getPreviewSize().height;
					ByteArrayOutputStream mJpegOutput = new ByteArrayOutputStream(
							data.length);
					yuvimage.compressToJpeg(new Rect(0, 0, mWidth, mHeight),
							100, mJpegOutput);
					Bitmap mBitmapIn = BitmapFactory.decodeByteArray(
							mJpegOutput.toByteArray(), 0, mJpegOutput.size());
					Bitmap bitmapNew = mBitmapIn.copy(Config.ARGB_8888, true);
					for (int i = 0; i < bitmapNew.getWidth(); i = i + 30) {
						for (int j = 0; j < bitmapNew.getHeight(); j = j + 30) {
							int col = bitmapNew.getPixel(i, j);
							int alpha = col & 0xFF000000;
							int red = (col & 0x00FF0000) >> 16;
							int green = (col & 0x0000FF00) >> 8;
							int blue = (col & 0x000000FF);
							int grey = (int) ((float) red * 0.3 + (float) green
									* 0.59 + (float) blue * 0.11);
							m = m + 1;
							k = k + grey;
						}
					}
					bitmapNew.recycle();
					System.out.println("m=" + m);
					System.out.println("k=" + k);
					if (50 * m <= k && 100 * m >= k) {
						if (rank2_1 == isChoice) {
							timerTextView.setText(EnvConf.NOTICE_WEAK_LIGHT_1);
							timerTextView.setVisibility(View.VISIBLE);
							timerTextView.setTextColor(Color.YELLOW);
							lightTextView.setVisibility(View.GONE);
						}
						range = 1;
						if (rank2_2 == isChoice) {
							voiceToast();
						}
					}
					if (50 * m >= k) {
						if (rank2_1 == isChoice) {
							lightTextView
									.setText(EnvConf.NOTICE_WEAK_LIGHT_2_MUTIL_LINE);
							lightTextView.setVisibility(View.VISIBLE);
							lightTextView.setTextColor(Color.YELLOW);
							timerTextView.setVisibility(View.GONE);
							sparkBackground();
						}
						range = 2;
						if (rank2_2 == isChoice) {
							voiceToast();
						}
					}
					if (100 * m <= k) {
						timerTextView.setVisibility(View.GONE);
						lightTextView.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(),
								EnvConf.NOTICE_GOOD_LIGHT, Toast.LENGTH_LONG)
								.show();
					}

					k = 0;
					m = 0;
				}
			});

		}

	};

	private Handler handler3 = new Handler();
	private Runnable task3 = new Runnable() {

		@Override
		public void run() {
			Intent intent2 = getIntent();
			final int rank3_1 = intent2.getIntExtra("range3_1", 0);
			final int rank3_2 = intent2.getIntExtra("range3_2", 0);
			// if (speed >= 1) {
			// range = 3;
			// if (rank3_2 == isChoice) {
			// voiceToast();
			// }
			// if (rank3_1 == isChoice) {
			// gpsTextView
			// .setText(EnvConf.NOTICE_SLOWDOWN_SEPPD);
			// gpsTextView.setTextColor(Color.YELLOW);
			// sparkBackground();
			// }
			// } else {
			// if (rank3_1 == isChoice) {
			// Toast.makeText(getApplicationContext(),
			// "车速正常" + speed + "m/s",
			// Toast.LENGTH_LONG).show();
			// }
			// }
			// }
			// });

			symbol2 = true;
		}
	};

	private Handler handler2 = new Handler();
	private Runnable task2 = new Runnable() {

		@Override
		public void run() {
			Intent intent1 = getIntent();
			final int rank2_1 = intent1.getIntExtra("range2_1", 0);
			final int rank2_2 = intent1.getIntExtra("range2_2", 0);
			Sensor s = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			sensorEventListener = new SensorEventListener() {

				@Override
				public void onSensorChanged(SensorEvent event) {
					// TODO Auto-generated method stub
					float[] values = event.values;
					if (values[0] <= 50 && values[0] > 10) {
						if (rank2_1 == isChoice) {
							timerTextView.setText(EnvConf.NOTICE_WEAK_LIGHT_1);
							timerTextView.setVisibility(View.VISIBLE);
							timerTextView.setTextColor(Color.YELLOW);
							lightTextView.setVisibility(View.GONE);
						}
						range = 1;
						if (rank2_2 == isChoice) {

							voiceToast();
						}
					} else if (values[0] <= 10) {
						if (rank2_1 == isChoice) {
							lightTextView
									.setText(EnvConf.NOTICE_WEAK_LIGHT_1_MUTIL_LINE);
							lightTextView.setVisibility(View.VISIBLE);
							lightTextView.setTextColor(Color.YELLOW);
							timerTextView.setVisibility(View.GONE);
							sparkBackground();
						}
						range = 2;
						if (rank2_2 == isChoice) {
							voiceToast();
						}

					} else if (values[0] > 50) {
						timerTextView.setVisibility(View.GONE);
						lightTextView.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(),
								EnvConf.NOTICE_GOOD_LIGHT, Toast.LENGTH_LONG)
								.show();
					}
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					// TODO Auto-generated method stub

				}
			};
			mySensorManager.registerListener(sensorEventListener, s,
					SensorManager.SENSOR_DELAY_NORMAL);
			symbol1 = true;

		}

	};

	/*
	 * Setting Timer to timing
	 */
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				timer.setText(format(hour) + ":" + format(minute) + ":"
						+ format(second));
			}
		}
	};

	/*
	 * format time
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == isChoice) {
			s = "0" + s;
		}
		return s;
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setMessage("请选择操作")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNeutralButton("后台运行",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								moveTaskToBack(true);
							}
						})
				.setPositiveButton("返回上一层",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (mMediaRecorder != null) {
									mMediaRecorder.stop();
									mMediaRecorder.release();
									mMediaRecorder = null;
									CameraUtils.videoRename(mRecAudioFile,
											whichButton);
								}
								if (symbol1 = true) {
									mySensorManager
											.unregisterListener(sensorEventListener);
								}
								finish();
							}
						}).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		text2speech.shutdown();
		onBackPressed();
		handler4.removeCallbacks(task4);
		brightnessManager.releaseAwake();
	}
	@Override
	protected void onResume() {
		super.onResume();
		brightnessManager.keepAwake();
	}
	public void voiceToast() {
		final EditText noticeText = (EditText) findViewById(R.id.notice_text);
		if (range == isChoice) {
			noticeText.setText(EnvConf.NOTICE_WEAK_LIGHT_1);
			noticeText.setVisibility(View.GONE);
			String str;
			str = noticeText.getText().toString();
			text2speech.setLanguage(Locale.UK);
			text2speech.addSpeech(EnvConf.NOTICE_WEAK_LIGHT_1,
					EnvConf.AUDIO_PATH_WEAK_LIGHT_1);
			text2speech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		} else if (range == 2) {

			noticeText.setText(EnvConf.NOTICE_WEAK_LIGHT_2);
			noticeText.setVisibility(View.GONE);
			String str;
			str = noticeText.getText().toString();
			text2speech.setLanguage(Locale.UK);
			text2speech.addSpeech(EnvConf.NOTICE_WEAK_LIGHT_2,
					EnvConf.AUDIO_PATH_WEAK_LIGHT_2);
			text2speech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		} else if (range == 3) {
			noticeText.setText(EnvConf.NOTICE_SLOWDOWN_SEPPD);
			noticeText.setVisibility(View.GONE);
			String str;
			str = noticeText.getText().toString();
			text2speech.setLanguage(Locale.UK);
			text2speech.addSpeech(EnvConf.NOTICE_SLOWDOWN_SEPPD,
					EnvConf.AUDIO_PATH_SLOW_SPEED);
			text2speech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		}
		if (or == isChoice) {
			noticeText.setText(EnvConf.NOTICE_TUTORIAL_1);
			noticeText.setVisibility(View.GONE);
			String str;
			str = noticeText.getText().toString();
			text2speech.setLanguage(Locale.UK);
			text2speech.addSpeech(EnvConf.NOTICE_TUTORIAL_1,
					EnvConf.AUDIO_PATH_NOTICE_1);
			text2speech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		}
		if (or == 2) {
			noticeText.setText(EnvConf.NOTICE_TUTORIAL_2);
			noticeText.setVisibility(View.GONE);
			String str;
			str = noticeText.getText().toString();
			text2speech.setLanguage(Locale.UK);
			text2speech.addSpeech(EnvConf.NOTICE_TUTORIAL_2,
					EnvConf.AUDIO_PATH_NOTICE_2);
			text2speech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	public void sparkBackground() {
		Timer timer = new Timer();
		TimerTask taskcc = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {

					public void run() {
						if (clo == 0) {
							clo = 1;
							if (range == 2) {
								lightTextView.setBackgroundColor(Color.RED);
							}
							if (range == 3) {
								gpsTextView.setBackgroundColor(Color.RED);
							}
						} else {
							clo = 0;
							if (range == 2) {
								lightTextView
										.setBackgroundColor(Color.TRANSPARENT);
							}
							if (range == 3) {
								gpsTextView
										.setBackgroundColor(Color.TRANSPARENT);
							}
						}
					}
				});
			}
		};
		timer.schedule(taskcc, 1, 100);
	}

	private void setBrightness(float f) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = f;
		getWindow().setAttributes(lp);
	}

	class MyText2SpeechListener implements TextToSpeech.OnInitListener {
		@Override
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
			} else {
			}
		}
	}

	class CarmeraCallBack implements Callback {
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (cameraDevice != null) {
				if (isPreview) {
					cameraDevice.stopPreview();
					isPreview = false;
				}
				cameraDevice.release();
				cameraDevice = null; // 记得释放
			}
			mSurfaceview = null;
			mSurfaceHolder = null;
			mMediaRecorder = null;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				cameraDevice = CameraUtils.openCamera(cameraDevice,
						mSurfaceHolder);
				isPreview = true;
				if (lightNoticeCheckBoxStatus == isChoice
						&& recorderCheckBoxStatus != 1) {
					handler4.postDelayed(task4, 2000);
				}
				if (overspeedNoticeCheckBoxStatus == isChoice
						&& recorderCheckBoxStatus != 1) {
					handler3.post(task3);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mSurfaceHolder = holder;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			mSurfaceHolder = holder;
		}
	}

	class StartBtnLisener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (isRecording) {
				/*
				 * Click to record
				 */
				if (isPreview) {
					cameraDevice = CameraUtils.stopCamera(cameraDevice);
				}
				Log.logAL("record start");
				f = 0.01f;
				setBrightness(f);
				second = 0;
				minute = 0;
				hour = 0;
				bool = true;
				mMediaRecorder = CameraUtils.initMediaRecorder(mMediaRecorder,
						mSurfaceHolder);
				try {
					CameraUtils.startRecord(mMediaRecorder, mRecVedioPath);
					timer.setVisibility(View.VISIBLE);
					handler.postDelayed(task, 1000);// view the timer
					if (lightNoticeCheckBoxStatus == isChoice
							&& recorderCheckBoxStatus == isChoice) {
						handler2.postDelayed(task2, 1000);
					}
					if (overspeedNoticeCheckBoxStatus == isChoice
							&& recorderCheckBoxStatus == isChoice) {
						handler3.postDelayed(task3, 1000);
					}
					mVideoStartBtn
							.setBackgroundResource(R.drawable.arc_hf_btn_video_stop);
					isRecording = !isRecording;
					gps.addListeners(RecorderActivity.this);
					brightnessManager.setBrightness(255);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				/*
				 * Click to stop recode
				 */
				Log.logAL("record pause");
				try {
					bool = false;
					f = 100f;
					setBrightness(f);
					timer.setText(format(hour) + ":" + format(minute) + ":"
							+ format(second));
					// text2speech.shutdown();
					mMediaRecorder = CameraUtils.stopRecord(mMediaRecorder);
					CameraUtils.videoRename(mRecAudioFile, parentId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRecording = !isRecording;
				gps.removeListeners(RecorderActivity.this);
				mVideoStartBtn
						.setBackgroundResource(R.drawable.arc_hf_btn_video_start);
				try {
					cameraDevice = CameraUtils.openCamera(cameraDevice,
							mSurfaceHolder);
					isPreview = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CameraUtils.stopCamera(cameraDevice);
		brightnessManager.releaseAwake();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		brightnessManager.releaseAwake();
	}
	@Override
	public void GPS_receiver(Location location) {
		gpsTextView.setText(location.getSpeed() + "m/s");
		gpsTextView.setTextColor(Color.YELLOW);
	}

	class BrightnessManager {
		private PowerManager.WakeLock mWakeLock;
		public BrightnessManager() {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(
					PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		}
		public void keepAwake()
		{
			// onResume() 中调用：
			mWakeLock.acquire(); 
		}
		public void releaseAwake()
		{
			//  onPause() 中调用释放WakeLock对象
			mWakeLock.release();
		}
		public void setBrightness(int level) {
			ContentResolver cr = getContentResolver();
			Settings.System.putInt(cr, "screen_brightness", level);
			Window window = getWindow();
			LayoutParams attributes = window.getAttributes();
			float flevel = level;
			attributes.screenBrightness = flevel / 255;
			getWindow().setAttributes(attributes);
		}
	}
}