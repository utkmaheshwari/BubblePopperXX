package com.example.bubblepopperxx;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	public TextView tv[];
	public TextView tvScore, tvTime, tvBack, tvWord;
	public Toast toast;
	public Handler mainHandler;
	public LinearLayout lLayout;
	public volatile List<Letter> wordList;
	public volatile Letter letter;
	public static final int numberOfAlphabets = 26;
	public volatile int maxErrors = 7;
	public volatile boolean isRunning = true;
	public volatile boolean shutDown = false;
	public volatile int checkResult = -1;
	public volatile int errors;
	public volatile int total;
	public volatile int score;
	public SecureRandom random;
	public NotificationManager nm;
	public Thread t;
	public static final String[] charset = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	public volatile ArrayList<Integer> xDistance;
	public volatile ArrayList<Integer> yDistance;
	public volatile ArrayList<Integer> distance;
	public volatile static int width, height;
	public volatile SoundPool beepSound;
	public int beepSoundId;
	public volatile String ans = "";
	public volatile CountDownTimer cdTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUIComponents();
		setUpExtraPeripherals();
		createAndDisplaySurface();
	}

	public void setUIComponents() {
		lLayout = (LinearLayout) findViewById(R.id.lLayout);

		tv = new TextView[numberOfAlphabets];
		final int[] textViewId = { R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4,
				R.id.tv5, R.id.tv6, R.id.tv7, R.id.tv8, R.id.tv9, R.id.tv10,
				R.id.tv11, R.id.tv12, R.id.tv13, R.id.tv14, R.id.tv15,
				R.id.tv16, R.id.tv17, R.id.tv18, R.id.tv19, R.id.tv20,
				R.id.tv21, R.id.tv22, R.id.tv23, R.id.tv24, R.id.tv25,
				R.id.tv26 };

		for (int i = 0; i < numberOfAlphabets; ++i) {
			tv[i] = (TextView) findViewById(textViewId[i]);
			tv[i].setOnClickListener(this);
		}

		tvWord = (TextView) findViewById(R.id.tvWord);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvBack = (TextView) findViewById(R.id.tvBack);
		tvScore = (TextView) findViewById(R.id.tvScore);

		tvBack.setOnClickListener(this);

		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(123);
	}

	public void setUpExtraPeripherals() {
		beepSound = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		beepSoundId = beepSound.load(getApplicationContext(), R.raw.beep, 1);
		mainHandler = new Handler();
		cdTimer = new CountDownTimer(180000, 1000) {

			public void onTick(long millisUntilFinished) {
				long secs = millisUntilFinished / 1000;
				tvTime.setText((secs / 60) + " : " + (secs % 60));
			}

			public void onFinish() {
				shutDown = true;
			}
		};
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v.getId() == R.id.tvBack) {
			int length = ans.length();
			if (length == 0)
				return;
			ans = ans.substring(0, length - 1);
		} else {
			TextView tvX = (TextView) v;
			ans = ans + tvX.getText().toString();
		}
		tvWord.setText(ans);
		actionAfterChecking(check(tvWord.getText().toString()));

		keyboardRandomizer();
	}

	private void actionAfterChecking(int index) {
		if (index == (-1)) {
		} else {
			score++;
			tvWord.setText("");
			ans = "";
			displayToast(wordList.get(index).item.toString().trim()
					+ " removed...");
			updateSurface(index);
		}
	}

	public int check(String character) {
		for (int i = 0; i < wordList.size(); ++i) {
			if (wordList.get(i).item.equals(character))
				return i;
		}
		return (-1);
	}

	public void updateSurface(int index) {
		isRunning = false;
		{
			wordList.remove(index);
		}
		isRunning = true;

	}

	public void createAndDisplaySurface() {
		wordList = new ArrayList<Letter>();
		xDistance = new ArrayList<Integer>();
		yDistance = new ArrayList<Integer>();

		random = new SecureRandom();
		displayToast("starting....");
		score = total = errors = 0;
		lLayout.addView(new AnimationSurface(getApplicationContext()), 0,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

	}

	class AnimationSurface extends SurfaceView implements
			SurfaceHolder.Callback {

		public SurfaceHolder sfh;
		public Canvas canvas;
		public Paint paint0, paint1, paint, paint2;
		// public int yToMove;
		public ReentrantLock lock;

		public AnimationSurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paint0 = new Paint();
			paint0.setColor(Color.WHITE);
			paint0.setAntiAlias(true);
			paint0.setTextSize(40);

			paint1 = new Paint();
			paint1.setColor(Color.RED);
			paint1.setAntiAlias(true);
			paint1.setTextSize(40);

			sfh.addCallback(this);
		}

		public AnimationSurface(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paint0 = new Paint();
			paint0.setColor(Color.BLUE);
			paint0.setAntiAlias(true);
			paint0.setTextSize(40);

			paint1 = new Paint();
			paint1.setColor(Color.RED);
			paint1.setAntiAlias(true);
			paint1.setTextSize(40);

			sfh.addCallback(this);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			// TODO Auto-generated method stub
			super.onSizeChanged(w, h, oldw, oldh);
			width = w / 10;
			height = h / 10;

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub

		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@SuppressLint("NewApi")
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			for (int i = 100; i <= width * 2; i += 10)
				xDistance.add(i);
			for (int i = -(height * 15); i <= (height * 15); i += 10)
				yDistance.add(i);

			t = new Thread(new ThreadWorker());
			t.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			shutDown = true;
			sfh.removeCallback(this);
		//	beepSound.stop(beepSoundId);
			beepSound.release();
		}

		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		class ThreadWorker implements Runnable {

			@Override
			protected void finalize() throws Throwable {
				// TODO Auto-generated method stub
				super.finalize();
				sfh.getSurface().release();
			}

			@SuppressWarnings("finally")
			@Override
			public void run() {
				// TODO Auto-generated method stub
				cdTimer.start();
				while (true) {
					if (shutDown) {
						canvas.drawText("you did " + maxErrors
								+ " errors you lose........" + "\n"
								+ "your score is " + score + " / " + total, 0,
								canvas.getHeight() / 2, paint);

						sfh.unlockCanvasAndPost(canvas);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						finish();
						break;
					}

					while (!isRunning)
						;

					letter = new Letter(produceRandomCharacter(), (height * 5));
					wordList.add(letter);

					++total;
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							tvScore.setText(score + " / " + total);

						}
					});
					canvas = sfh.lockCanvas();
					canvas.drawRGB(0, 0, 0);

					if (errors >= maxErrors) {

						try {
							canvas.drawText("you did " + maxErrors
									+ " errors you lose........", 0,
									canvas.getHeight() / 2, paint);
							shutDown = true;

						} finally {
							sfh.unlockCanvasAndPost(canvas);
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							finish();
							continue;
						}
					}
					sfh.unlockCanvasAndPost(canvas);
					for (int i = 0; i < wordList.size(); ++i) {
						if (wordList.get(i).colorCode == 1) {
							wordList.remove(i);
							--i;
							score--;
							continue;
						} else
							setPosition(i);

						if ((wordList.get(i).xPos) > width * 7) {
							wordList.get(i).colorCode = 1;
						}

					}

					for (int a = 0; a < 100; ++a) {
						canvas = sfh.lockCanvas();
						canvas.drawRGB(0, 0, 0);
						for (int i = 0; i < wordList.size(); ++i) {

							final int dx = wordList.get(i).xToMove / 100;
							final int dy = wordList.get(i).yToMove / 100;
							wordList.get(i).xPos += dx;
							wordList.get(i).yPos += dy;

							if (wordList.get(i).yPos > getHeight()) {
								wordList.get(i).yPos = getHeight();
								wordList.get(i).yToMove = -(wordList.get(i).yToMove - a
										* dy);
							}

							if (wordList.get(i).yPos < 30) {
								wordList.get(i).yPos = 30;
								wordList.get(i).yToMove = -(wordList.get(i).yToMove - a
										* dy);
							}

							if (wordList.get(i).colorCode == 0)
								paint = new Paint(paint0);
							else if (wordList.get(i).colorCode == 1)
								paint = new Paint(paint1);

							canvas.drawText(wordList.get(i).item,
									wordList.get(i).xPos, wordList.get(i).yPos,
									paint);

						}
						sfh.unlockCanvasAndPost(canvas);
						try {
						//	beepSound
							//		.play(beepSoundId, 5, 5, 0, 0, (float) 1.5);
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//beepSound.stop(beepSoundId);
				}
			}
		}

		public void setPosition(int i) {
			wordList.get(i).yToMove = yDistance.get(random.nextInt(yDistance
					.size()));
			wordList.get(i).xToMove = xDistance.get(random.nextInt(xDistance
					.size()));
		}
	}

	public void displayToast(final String result) {
		toast = Toast.makeText(getApplicationContext(), result,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
		toast.show();
		mainHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				toast.cancel();
			}
		}, 200);

	}

	public String produceRandomCharacter() {
		int n = random.nextInt(2) + 3;
		String s = "";
		for (int i = 0; i < n; ++i) {
			s = s + charset[random.nextInt(charset.length)];
		}
		return s;
	}

	public void keyboardRandomizer() {
		for (int i = 1; i < numberOfAlphabets; ++i) {
			int j = random.nextInt(i);
			String letter = (tv[i].getText().toString());
			tv[i].setText(tv[j].getText().toString());
			tv[j].setText(letter);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	public void createNotification() {
		Notification.Builder nb = new Notification.Builder(
				getApplicationContext());
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, i, 0);
		nb.setContentIntent(pi)
				.setContentText("press to restart game")
				.setContentTitle("BUBBLEPOPPER")
				.setSmallIcon(R.drawable.qaz)
				.setWhen(0)
				.setTicker(
						"BubblePopper game has been paused, press to restart game");
		nm.notify(123, nb.build());
	}
}
