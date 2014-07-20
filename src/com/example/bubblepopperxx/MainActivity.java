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
	public volatile List<Letter> letterList;
	public volatile Letter letter;/*
								 * public volatile ArrayList<String> rList;
								 * public volatile ArrayList<Integer> xPos;
								 * public volatile ArrayList<Integer> yPos;
								 * public volatile ArrayList<Integer> x; public
								 * volatile ArrayList<Integer> y;
								 */
	// public volatile ArrayList<String> delList;
	// public volatile ArrayList<Integer> delIndexes;
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
	public static final Integer[] xCords = { 0, 10, 20, 30, 40, 50, 60, 70, 80,
			90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210, 220 };
	public static volatile Integer[] yCords = { 0, 10, 20, 30, 40, 50, 60, 70,
			80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210,
			220, 230, 240, 250, 260, 270, 280 };
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
			displayToast(letterList.get(index).item.toString().trim()
					+ " removed...");
			updateSurface(index);
			// Alternate approach 1
			/*
			 * isRunning=false; delList.add(rList.get(index)); isRunning=true;
			 */

			// Alternate approach 2
			/*
			 * isRunning=false; delIndexes.add(index); isRunning=true;
			 */
		}
	}

	public int check(String character) {
		for (int i = 0; i < letterList.size(); ++i) {
			if (letterList.get(i).item.equals(character))
				return i;
		}
		return (-1);
	}

	public void updateSurface(int index) {
		isRunning = false;
		{/*
		 * rList.remove(index); xPos.remove(index); yPos.remove(index);
		 * x.remove(index); y.remove(index);
		 */
			letterList.remove(index);
		}
		isRunning = true;
	}

	public void createAndDisplaySurface() {
		/*
		 * rList = new ArrayList<String>(); xPos = new ArrayList<Integer>();
		 * yPos = new ArrayList<Integer>(); x = new ArrayList<Integer>(); y =
		 * new ArrayList<Integer>();
		 */
		letterList = new ArrayList<Letter>();
		/*
		 * Alternate approach 1 delList=new ArrayList<String>();
		 */

		/*
		 * Alternate approach 2 delIndexes=new ArrayList<Integer>();
		 */

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
		public Paint paintTrue, paintFalse, paint;
		// public int yPos;
		public ReentrantLock lock;

		public AnimationSurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paintTrue = new Paint();
			paintTrue.setColor(Color.WHITE);
			paintTrue.setAntiAlias(true);
			paintTrue.setTextSize(40);

			paintFalse = new Paint();
			paintFalse.setColor(Color.RED);
			paintFalse.setAntiAlias(true);
			paintFalse.setTextSize(60);
			sfh.addCallback(this);

		}

		public AnimationSurface(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paintTrue = new Paint();
			paintTrue.setColor(Color.WHITE);
			paintTrue.setAntiAlias(true);
			paintTrue.setTextSize(40);

			paintFalse = new Paint();
			paintFalse.setColor(Color.RED);
			paintFalse.setAntiAlias(true);
			paintFalse.setTextSize(60);

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
					/*
					 * Alternate approach 1 for(Integer index:delIndexes) {
					 * rList.remove(index); xPos.remove(index);
					 * delIndexes.remove(index); }
					 */
					/*
					 * Alternate approach 2 for(String item:delList) {
					 * xPos.remove(rList.indexOf(item)); rList.remove(item);
					 * delList.remove(item); }
					 */
					letter = new Letter(produceRandomCharacter());
					letterList.add(letter);

					++total;
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							keyboardRandomizer();
							tvScore.setText(score + " / " + total);
						}
					});

					canvas = sfh.lockCanvas();
					canvas.drawRGB(0, 0, 0);
					if (errors >= maxErrors) {

						try {
							canvas.drawText(
									"you did 10 errors you lose........", 0,
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

					for (int i = 0; i < letterList.size(); ++i) {
						if (letterList.get(i).colorBit == false) {
							letterList.remove(i);
							--i;
							continue;
						} else
							setPosition(i);
						
						if ((letterList.get(i).x+letterList.get(i).xPos) > xCords[xCords.length-1])
						 {
							paint = new Paint(paintFalse);
							letterList.get(i).colorBit = false;
						} else
							paint = new Paint(paintTrue);
					}

					for (int a = 0; a < 10; ++a) {
						canvas = sfh.lockCanvas();
						canvas.drawRGB(0, 0, 0);
						for (int i = 0; i < letterList.size(); ++i) {

							final int dx = 4;
							final int dy = letterList.get(i).yPos / 10;
							letterList.get(i).x += dx;
							letterList.get(i).y += dy;

							canvas.drawText(letterList.get(i).item,
									letterList.get(i).x, letterList.get(i).y,
									paint);
						}
						sfh.unlockCanvasAndPost(canvas);
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		public void setPosition(int i) {
		//	int newX = xCords[random.nextInt(xCords.length)];
			int newY = yCords[random.nextInt(yCords.length)];
		//	int newY=random.nextInt(canvas.getHeight());
		//	int oldX = letterList.get(i).xPos;
			int oldY = letterList.get(i).yPos;

	//		letterList.get(i).xPos = newX - oldX;
			letterList.get(i).yPos = newY - oldY;
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
