package com.example.bubblepopperxx;

import java.security.SecureRandom;
import java.util.ArrayList;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bubblepopperxx.MainActivity.AnimationSurface.ThreadWorker;

public class MainActivity extends Activity implements OnClickListener {

	public Button[] bt;
	public TextView tvScore, tvTime, tvLevel;
	public Toast toast;
	public Handler mainHandler;
	public String character;
	public LinearLayout lLayout;
	public volatile ArrayList<String> rList;
	public volatile ArrayList<Integer> xPos;
	public ThreadWorker tworker;
	public static final int numberOfAlphabets = 26;
	public volatile int maxErrors = 10;
	public volatile int errors;
	public volatile int total;
	public volatile int score;
	public SecureRandom random;
	public NotificationManager nm;
	public static final String[] charset = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

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

		bt = new Button[26];
		final int[] buttonId = { R.id.bt1, R.id.bt2, R.id.bt3, R.id.bt4,
				R.id.bt5, R.id.bt6, R.id.bt7, R.id.bt8, R.id.bt9, R.id.bt10,
				R.id.bt11, R.id.bt12, R.id.bt13, R.id.bt14, R.id.bt15,
				R.id.bt16, R.id.bt17, R.id.bt18, R.id.bt19, R.id.bt20,
				R.id.bt21, R.id.bt22, R.id.bt23, R.id.bt24, R.id.bt25,
				R.id.bt26 };

		for (int i = 0; i < 26; ++i) {
			bt[i] = (Button) findViewById(buttonId[i]);
			bt[i].setOnClickListener(this);
		}

		tvLevel = (TextView) findViewById(R.id.level);
		// tvTime = (TextView) findViewById(R.id.time);
		tvScore = (TextView) findViewById(R.id.score);

		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(123);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		character = ((Button) v).getText().toString();
		actionAfterChecking(check(character));
		keyboardRandomizer();

	}

	private void actionAfterChecking(int index) {
		if (index == (-1)) {
			++errors;
			displayToast("wrong...");
		} else {
			score++;
			displayToast(rList.get(index).toString().trim() + " removed...");
			updateAndDisplaySurface(index);
		}
	}

	public int check(String character) {
		return (rList.indexOf(character));
	}

	public void updateAndDisplaySurface(int index) {
		tworker.setRunningState(false);
		rList.remove(index);
		lLayout.removeAllViews();
		lLayout.addView(new AnimationSurface(getApplicationContext()), 0,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
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

		public AnimationSurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);
			paint.setTextSize(15);
			sfh.addCallback(this);
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
			tworker = new ThreadWorker();
			new Thread(tworker).start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			tworker.setRunningState(false);
			sfh.removeCallback(this);
		}

		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		class ThreadWorker implements Runnable {
			private volatile boolean isRunning = true;

			@Override
			protected void finalize() throws Throwable {
				// TODO Auto-generated method stub
				super.finalize();
				sfh.getSurface().release();
			}
			
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			@SuppressLint("NewApi")
			@SuppressWarnings("finally")
			@Override
			
			

			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					if (!isRunning)
						break;

					rList.add(produceRandomCharacter());
					++total;
					mainHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							keyboardRandomizer();
							tvScore.setText(score + " / " + total);
						}
					});

					xPos.add(0);
					yPos = 0;
					canvas = sfh.lockCanvas();
					canvas.drawRGB(0, 0, 0);
					if (errors >= maxErrors) {

						try {
							canvas.drawText(
									"you did 10 errors you lose........", 0,
									canvas.getHeight() / 2, paint);
							setRunningState(false);
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
					} finally {
						// canvas.drawRGB(255, 0, 0);
						
						if (!isRunning)
							break;
					}
				}
			}

			
			public void setRunningState(boolean state) {
				isRunning = state;
			}

			/*
			 * public void setLimits() { if (xLimit >= (canvas.getWidth() - 20))
			 * { xLimit = (canvas.getWidth() - 20); } else { xLimit += 20; }
			 * yLimit = canvas.getHeight() - 20; }
			 */

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
		}, 1000);
	}

	public String produceRandomCharacter() {
		return charset[random.nextInt(charset.length)];

	}

	public void keyboardRandomizer() {
		for (int i = 1; i < numberOfAlphabets; ++i) {
			int j = random.nextInt(i);
			String letter = ((Button) bt[i]).getText().toString();
			bt[i].setText(((Button) bt[j]).getText().toString());
			bt[j].setText(letter);
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

	/*
	 * public static void hideSoftKeyboard(Activity activity) {
	 * InputMethodManager inputMethodManager = (InputMethodManager) activity
	 * .getSystemService(Activity.INPUT_METHOD_SERVICE);
	 * inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
	 * .getWindowToken(), 0); }
	 */
}
