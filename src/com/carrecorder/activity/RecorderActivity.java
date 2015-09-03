package com.carrecorder.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.carrecorder.conf.ActivityConf;
import com.carrecorder.conf.EnvConf;
import myjob.carrecorder.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RecorderActivity extends Activity {
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
	protected Camera camera;
	protected boolean isPreview;
	private boolean isRecording = true; // true-->no record,click to start；
	// false-->recording,click to pause;
	private TextToSpeech text2speech;
	private TextView myTextView1;
	private TextView myTextView2;
	private TextView myTextView3;
	private SensorManager mySensorManager;
	private int clo = 0;
	private int range;// the flag for notice text
	private int k = 0;
	private int m = 0;
	private LocationManager lm;
	private SensorEventListener sensorEventListener;
	float speed;
	double lat;
	double lng;
	boolean symbol1;
	boolean symbol2;
	private int or;
	private float f = 1.0f;
	private int rank1;
	private int rank2;
	private int rank3;

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
		myTextView1 = (TextView) findViewById(R.id.textview1);
		myTextView2 = (TextView) findViewById(R.id.textview2);
		myTextView3 = (TextView) findViewById(R.id.textview3);
	}

	private void initSensor() {
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	private void initListener() {
		mVideoStartBtn.setOnClickListener(new StartBtnLisener());
	}

	private void initIntentData() {
		Intent intent = getIntent();
		rank1 = intent.getIntExtra(ActivityConf.intent_range1, 0);
		rank2 = intent.getIntExtra(ActivityConf.intent_range2, 0);
		rank3 = intent.getIntExtra(ActivityConf.intent_range3, 0);
		parentId = getIntent().getIntExtra("parentId", 0);
	}

	private void initDynamicView() {
		if (rank1 == 1) {
			mVideoStartBtn.setVisibility(View.VISIBLE);
		} else {
			mVideoStartBtn.setVisibility(View.GONE);
		}
		if (rank2 != 1) {
			myTextView1.setVisibility(View.GONE);
			myTextView2.setVisibility(View.GONE);
		}
		// set timer invisible
		timer.setVisibility(View.GONE);

		// set buffer path
		mRecVedioPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/hfdatabase/video/temp/");
		if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdirs();
		}
		// bind the preview view , SurfaceHolder is a controller of SurfaceView
		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new CarmeraCallBack());
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void initOther() {
		text2speech = new TextToSpeech(getApplicationContext(),
				new MyText2SpeechListener());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initIntentData();
		initSensor();
		initDynamicView();
		initListener();
		initOther();
	}

	/*
	 * 消息提示
	 */

	/*
	 * 生成video文件名字
	 */
	@SuppressLint("SimpleDateFormat")
	protected void videoRename() {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/hfdatabase/video/"
				+ String.valueOf(parentId) + "/";
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date()) + ".3gp";
		File out = new File(path);
		if (!out.exists()) {
			out.mkdirs();
		}
		out = new File(path, fileName);
		if (mRecAudioFile.exists())
			mRecAudioFile.renameTo(out);
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
				if (rank2_1 == 1) {
					myTextView1.setText("光线较暗请开车灯");
					myTextView1.setVisibility(View.VISIBLE);
					myTextView1.setTextColor(Color.YELLOW);
					myTextView2.setVisibility(View.GONE);
				}
				range = 1;
				if (rank2_2 == 1) {
					voiceToast();
				}
			}
			if (50 * m >= k) {
				if (rank2_1 == 1) {
					myTextView2.setText("光线非常暗\n请立即开灯");
					myTextView2.setVisibility(View.VISIBLE);
					myTextView2.setTextColor(Color.YELLOW);
					myTextView1.setVisibility(View.GONE);
					spark();
				}
				range = 2;
				if (rank2_2 == 1) {
					voiceToast();
				}
			}
			if (100 * m <= k) {
				myTextView1.setVisibility(View.GONE);
				myTextView2.setVisibility(View.GONE);
				Toast.makeText(getApplicationContext(), "光线良好",
						Toast.LENGTH_LONG).show();
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
			camera.setOneShotPreviewCallback(new PreviewCallback() {
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
						if (rank2_1 == 1) {
							myTextView1.setText("光线较暗请开车灯");
							myTextView1.setVisibility(View.VISIBLE);
							myTextView1.setTextColor(Color.YELLOW);
							myTextView2.setVisibility(View.GONE);
						}
						range = 1;
						if (rank2_2 == 1) {
							voiceToast();
						}
					}
					if (50 * m >= k) {
						if (rank2_1 == 1) {
							myTextView2.setText("光线非常暗\n请立即开灯");
							myTextView2.setVisibility(View.VISIBLE);
							myTextView2.setTextColor(Color.YELLOW);
							myTextView1.setVisibility(View.GONE);
							spark();
						}
						range = 2;
						if (rank2_2 == 1) {
							voiceToast();
						}
					}
					if (100 * m <= k) {
						myTextView1.setVisibility(View.GONE);
						myTextView2.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(), "光线良好",
								Toast.LENGTH_LONG).show();
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
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = lm
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				lat = location.getLatitude();
				lng = location.getLongitude();
				speed = location.getSpeed();
				myTextView3.setText(speed + "m/s");
				myTextView3.setTextColor(Color.YELLOW);
			} else {
				myTextView3.setText("No Location Founded");
				myTextView3.setTextColor(Color.YELLOW);
			}
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					new LocationListener() {

						@Override
						public void onStatusChanged(String provider,
								int status, Bundle extras) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onProviderEnabled(String provider) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onProviderDisabled(String provider) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLocationChanged(Location location) {
							speed = location.getSpeed();
							lat = location.getLatitude();
							lng = location.getLongitude();
							if (speed >= 1) {
								range = 3;
								if (rank3_2 == 1) {
									voiceToast();
								}
								if (rank3_1 == 1) {
									myTextView3.setText("车速过快，请减速行驶");
									myTextView3.setTextColor(Color.YELLOW);
									spark();
								}
							} else {
								if (rank3_1 == 1) {
									Toast.makeText(getApplicationContext(),
											"车速正常" + speed + "m/s",
											Toast.LENGTH_LONG).show();
								}
							}
						}
					});

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
						if (rank2_1 == 1) {
							myTextView1.setText(EnvConf.NOTICE_WEAK_LIGHT_1);
							myTextView1.setVisibility(View.VISIBLE);
							myTextView1.setTextColor(Color.YELLOW);
							myTextView2.setVisibility(View.GONE);
						}
						range = 1;
						if (rank2_2 == 1) {

							voiceToast();
						}
					} else if (values[0] <= 10) {
						if (rank2_1 == 1) {
							myTextView2
									.setText(EnvConf.NOTICE_WEAK_LIGHT_MUTIL_LINE);
							myTextView2.setVisibility(View.VISIBLE);
							myTextView2.setTextColor(Color.YELLOW);
							myTextView1.setVisibility(View.GONE);
							spark();
						}
						range = 2;
						if (rank2_2 == 1) {
							voiceToast();
						}

					} else if (values[0] > 50) {
						myTextView1.setVisibility(View.GONE);
						myTextView2.setVisibility(View.GONE);
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
	 * 定时器设置，实现计时
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
	 * 格式化时间
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	/*
	 * 覆写返回键监听
	 */
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
									videoRename();
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
	}

	public void voiceToast() {
		final EditText noticeText = (EditText) findViewById(R.id.notice_text);
		if (range == 1) {
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
		if (or == 1) {
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

	public void spark() {
		Timer timer = new Timer();
		TimerTask taskcc = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {

					public void run() {
						if (clo == 0) {
							clo = 1;
							if (range == 2) {
								myTextView2.setBackgroundColor(Color.RED);
							}
							if (range == 3) {
								myTextView3.setBackgroundColor(Color.RED);
							}
						} else {
							clo = 0;
							if (range == 2) {
								myTextView2
										.setBackgroundColor(Color.TRANSPARENT);
							}
							if (range == 3) {
								myTextView3
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
			if (camera != null) {
				if (isPreview) {
					camera.stopPreview();
					isPreview = false;
				}
				camera.release();
				camera = null; // 记得释放
			}
			mSurfaceview = null;
			mSurfaceHolder = null;
			mMediaRecorder = null;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			try {
				camera = Camera.open();
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewFrameRate(5); // 每秒5帧
				parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
				parameters.set("jpeg-quality", 85);// 照片质量
				camera.setParameters(parameters);
				camera.setPreviewDisplay(holder);
				camera.startPreview();
				isPreview = true;
				if (rank2 == 1 && rank1 != 1) {
					handler4.postDelayed(task4, 2000);
				}
				if (rank3 == 1 && rank1 != 1) {
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
				 * 点击开始录像
				 */
				if (isPreview) {
					camera.stopPreview();
					camera.release();
					camera = null;
				}
				f = 0.01f;
				setBrightness(f);
				second = 0;
				minute = 0;
				hour = 0;
				bool = true;
				if (mMediaRecorder == null)
					mMediaRecorder = new MediaRecorder();
				else
					mMediaRecorder.reset();
				mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
				mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
				mMediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mMediaRecorder.setVideoSize(320, 240);
				mMediaRecorder.setVideoFrameRate(15);
				try {
					mRecAudioFile = File.createTempFile("Vedio", ".3gp",
							mRecVedioPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
				try {
					mMediaRecorder.prepare();
					timer.setVisibility(View.VISIBLE);
					handler.postDelayed(task, 1000);
					mMediaRecorder.start();
					if (rank2 == 1 && rank1 == 1) {
						handler2.postDelayed(task2, 1000);
					}
					if (rank3 == 1 && rank1 == 1) {
						handler3.postDelayed(task3, 1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mVideoStartBtn
						.setBackgroundResource(R.drawable.arc_hf_btn_video_stop);
				isRecording = !isRecording;
			} else {
				/*
				 * 点击停止
				 */
				try {
					bool = false;
					mMediaRecorder.stop();
					f = 100f;
					setBrightness(f);
					timer.setText(format(hour) + ":" + format(minute) + ":"
							+ format(second));
					mMediaRecorder.release();
					mMediaRecorder = null;
					videoRename();
					text2speech.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRecording = !isRecording;
				mVideoStartBtn
						.setBackgroundResource(R.drawable.arc_hf_btn_video_start);

				try {
					camera = Camera.open();
					Camera.Parameters parameters = camera.getParameters();
					parameters.setPreviewFrameRate(5); // 每秒5帧
					parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
					parameters.set("jpeg-quality", 85);// 照片质量
					camera.setParameters(parameters);
					camera.setPreviewDisplay(mSurfaceHolder);
					camera.startPreview();
					isPreview = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}
}