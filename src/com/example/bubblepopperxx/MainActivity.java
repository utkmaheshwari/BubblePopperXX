package com.example.bubblepopperxx;

import java.security.SecureRandom;
import java.util.ArrayList;
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
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
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
	public TextView tvScore, tvTime, tvLevel;
	public Toast toast;
	public Handler mainHandler;
	public LinearLayout lLayout;
	public volatile ArrayList<String> rList;
	public volatile ArrayList<Integer> xPos;
	public static final int numberOfAlphabets = 26;
	public volatile int maxErrors = 10;
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
	public SoundPool soundpool;
	public ReentrantLock globalLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUIComponents();
		mainHandler = new Handler();
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

		tvLevel = (TextView) findViewById(R.id.level);
		// tvTime = (TextView) findViewById(R.id.time);
		tvScore = (TextView) findViewById(R.id.score);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(123);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// soundpool.play(soundID, leftVolume, rightVolume, priority, loop,
		// rate);
		TextView tvX = (TextView) v;
		actionAfterChecking(check(tvX.getText().toString()));
	}

	private void actionAfterChecking(int index) {
		if (index == (-1)) {
			++errors;
			displayToast("wrong...");
		} else {
			score++;
			displayToast(rList.get(index).toString().trim() + " removed...");
			updateSurface(index);
		}
	}

	public int check(String character) {
		return (rList.indexOf(character));
	}

	public void updateSurface(int index) {
		isRunning = false;
		synchronized (rList) {
			rList.remove(index);
		}
		synchronized (xPos) {
			xPos.remove(index);
		}
		isRunning = true;
	}

	public void createAndDisplaySurface() {
		rList = new ArrayList<String>();
		xPos = new ArrayList<Integer>();
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
		public Paint paint;
		public int yPos;
		public ReentrantLock lock;

		public AnimationSurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);
			paint.setTextSize(15);
			sfh.addCallback(this);
			lock = new ReentrantLock();
		}

		public AnimationSurface(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);
			paint.setTextSize(30);
			sfh.addCallback(this);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			t = new Thread(new ThreadWorker());
			t.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			shutDown = true;
			sfh.removeCallback(this);
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
				while (true) {
					if (shutDown)
						break;

					while (!isRunning)
						;

					rList.add(produceRandomCharacter());
					xPos.add(0);
					++total;
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							keyboardRandomizer();
							tvScore.setText(score + " / " + total);
						}
					});

					yPos = 0;
					canvas = sfh.lockCanvas();
					canvas.drawRGB(0, 0, 0);
					if (errors >= maxErrors) {

						try {
							canvas.drawText(
									"you did 10 errors you lose........", 0,
									canvas.getHeight() / 2, paint);
							shutDown = true;
							sfh.unlockCanvasAndPost(canvas);
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							finish();
							continue;
						}
					}
					for (int i = 0; i < rList.size(); ++i) {
						if (!setPosition(i)) {
							rList.remove(i);
							xPos.remove(i);
						}
						canvas.drawText(rList.get(i), xPos.get(i), yPos, paint);
					}
					sfh.unlockCanvasAndPost(canvas);

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			public boolean setPosition(int i) {
				if (xPos.get(i) >= (canvas.getWidth() - 20))
					return false;

				xPos.set(i, xPos.get(i) + 20);
				yPos = random.nextInt(canvas.getHeight() - 40) + 20;
				return true;
			}
		}
	}

	public void displayToast(final String result) {
		// TODO Auto-generated method stub
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
		}, 800);
	}

	public String produceRandomCharacter() {
		return charset[random.nextInt(charset.length)];
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
