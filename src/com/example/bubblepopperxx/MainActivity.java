package com.example.bubblepopperxx;

import java.security.SecureRandom;
import java.util.ArrayList;

import com.example.bubblepopperxx.MainActivity.AnimationSurface.ThreadWorker;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	public Button[] bt;
	public Toast toast;
	public Handler handler;
	public Game game;
	public String character;
	public String[] set;
	public LinearLayout lLayout;
	public int xLimit;
	public volatile ArrayList<String> rList;
	public Thread t;
	public ThreadWorker tworker;
	public static final int numberOfCharacters = 10;
	public static final int numberOfAlphabets = 26;
	public volatile int last;
	public volatile int score;

	public NotificationManager nm;
	public static final String[] charset = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUIComponents();
		handler = new Handler();
		new Game().execute(charset);
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
			displayToast("wrong...Score: " + score + " / " + numberOfCharacters);
		} else {
			score++;
			displayToast("" + rList.get(index).toString().trim()
					+ " removed.. Score: " + score + " / " + numberOfCharacters);
			updateAndDisplaySurface(index);
			// displayToast("correct....");
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
		xLimit = 40;
		last = 0;
		score = 0;
		lLayout.addView(new AnimationSurface(getApplicationContext()), 0,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

	}

	class Game extends AsyncTask<String, Integer, ArrayList<String>> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("starting....");
			keyboardRandomizer();
		}

		@Override
		protected ArrayList<String> doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			produceRandomString();
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			createAndDisplaySurface();
		}
	}

	class AnimationSurface extends SurfaceView implements
			SurfaceHolder.Callback {

		public SurfaceHolder sfh;
		public Canvas canvas;
		public SecureRandom scr;
		public Paint paint;
		public int yLimit, xPos, yPos;

		public AnimationSurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			sfh = getHolder();
			// c=sfh.lockCanvas();
			scr = new SecureRandom();
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

			scr = new SecureRandom();
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
			t = new Thread(tworker);
			t.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			tworker.setRunningState(false);
			sfh.removeCallback(this);
		}

		class ThreadWorker implements Runnable {
			private volatile boolean isRunning = true;

			@SuppressWarnings("finally")
			@Override
			public void run() {
				// TODO Auto-generated method stub

				while (true) {
					if (!isRunning)
						break;

					if (last >= rList.size())
						last = rList.size();

					else
						++last;
					xPos = yPos = yLimit = 0;
					canvas = sfh.lockCanvas();
					canvas.drawRGB(0, 0, 0);
					// setLimits();

					if (score == numberOfCharacters) {
						try {
							canvas.drawRGB(0, 0, 0);
							canvas.drawText("you Win....", 0,
									canvas.getHeight() / 2, paint);
							sfh.unlockCanvasAndPost(canvas);
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							setRunningState(false);
							finish();
							continue;
						}
					}

					// setLimits();

					if (!setLimits()) {

						try {
							canvas.drawRGB(0, 0, 0);
							canvas.drawText("time up....", 0,
									canvas.getHeight() / 2, paint);
							sfh.unlockCanvasAndPost(canvas);
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							setRunningState(false);
							finish();
							continue;
						}

					}

					for (String rItem : rList.subList(0, last)) {
						setPosition();
						canvas.drawText(rItem, xPos, yPos, paint);
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

			public boolean setLimits() {
				if (xLimit >= (canvas.getWidth() - 20))
					return false;

				xLimit += 20;
				yLimit = canvas.getHeight() - 20;
				return true;
			}

			public void setPosition() {
				xPos = scr.nextInt(xLimit);
				yPos = scr.nextInt(yLimit);
			}
		}
	}

	public void displayToast(final String result) {

		// TODO Auto-generated method stub
		toast = Toast.makeText(getApplicationContext(), result,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
		toast.show();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				toast.cancel();
			}
		}, 1000);
	}

	public void produceRandomString() {
		SecureRandom r = new SecureRandom();
		rList = new ArrayList<String>();
		for (int i = 0; i < numberOfCharacters; ++i) {
			rList.add(charset[r.nextInt(charset.length)]);
			Log.i("random", rList.get(i));
		}
	}

	public void keyboardRandomizer() {
		SecureRandom r = new SecureRandom();
		for (int i = 1; i < numberOfAlphabets; ++i) {
			int j = r.nextInt(i);
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
