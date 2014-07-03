package com.example.bubblepopperxx;

import java.security.SecureRandom;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	public Button[] bt;
	public Button start, pause;
	public TextView[] tv;
	public TextView score, time;
	public Toast toast;
	public Handler handler;
	public Game game;
	public boolean chronometerStarted = false;
	public Chronometer chronometer;
	public int s;
	public String character;
	public String[] set;

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
		s = 0;
	}

	public void setUIComponents() {
		start = (Button) findViewById(R.id.start);
		pause = (Button) findViewById(R.id.pause);
		start.setOnClickListener(this);
		pause.setOnClickListener(this);

		bt = new Button[26];
		final int[] buttonId = { R.id.bt1, R.id.bt2, R.id.bt3, R.id.bt4,
				R.id.bt5, R.id.bt6, R.id.bt7, R.id.bt8, R.id.bt9, R.id.bt10,
				R.id.bt11, R.id.bt12, R.id.bt13, R.id.bt14, R.id.bt15,
				R.id.bt16, R.id.bt17, R.id.bt18, R.id.bt19, R.id.bt20,
				R.id.bt21, R.id.bt22, R.id.bt23, R.id.bt24, R.id.bt25,
				R.id.bt26 };
		final int[] textViewId = { R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4,
				R.id.tv5, R.id.tv6, R.id.tv7, R.id.tv8, R.id.tv9 };
		for (int i = 0; i < 26; ++i) {
			bt[i] = (Button) findViewById(buttonId[i]);
			bt[i].setOnClickListener(this);
		}

		time = (TextView) findViewById(R.id.time);
		score = (TextView) findViewById(R.id.score);

		tv = new TextView[9];
		for (int i = 0; i < 9; ++i)
			tv[i] = (TextView) findViewById(textViewId[i]);

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
		if (v.getId() == R.id.start) {
			game = new Game();
			game.execute("qwerty");

		} else if (v.getId() == R.id.pause) {
			displayToast("paused....");
			createNotification();
			// stopChronometer();
			try {

				game.cancel(true);
				// stopChronometer();

			} catch (Exception e) {
				displayToast("execption...");
			}

		} else {
			character = ((Button) v).getText().toString();
			displayScore(check(character));
			keyboardRandomizer();

		}

	}

	public boolean check(String character) {
		for (int i = 0; i < 9; ++i) {
			if (character.trim().equalsIgnoreCase(
					tv[i].getText().toString().trim())) {
				return true;
			}
		}
		return false;

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	public void createNotification() {

		Notification.Builder nb = new Notification.Builder(
				getApplicationContext());
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, i, 0);
		// RemoteViews rv=new RemoteViews("com.example.bubblepopper",
		// android.R.layout.)
		nb.setContentIntent(pi)
				.setContentText("press to restart game")
				.setContentTitle("BUBBLEPOPPER")
				.setSmallIcon(R.drawable.qaz)
				.setWhen(0)
				.setTicker(
						"BubblePopper game has been paused, press to restart game");
		nm.notify(123, nb.build());
		// finish();
	}

	public void showLettersOnTextViews(String[] code) {
		for (int i = 0; i < 9; ++i) {
			tv[i].setText(code[i]);
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

	public String[] produceRandomString() {
		SecureRandom r = new SecureRandom();
		// int i = r.nextInt(5) + 1;
		String s[] = new String[9];
		for (int i = 0; i < 9; ++i) {
			s[i] = charset[r.nextInt(charset.length)];
		}
		return s;
	}

	public void keyboardRandomizer() {
		SecureRandom r = new SecureRandom();
		for (int i = 1; i < 26; ++i) {
			int j = r.nextInt(i);
			String letter = ((Button) bt[i]).getText().toString();
			bt[i].setText(((Button) bt[j]).getText().toString());
			bt[j].setText(letter);
		}
	}

	public void displayScore(boolean result) {
		if (result) {
			++s;
			displayToast("correct....");
		} else
			displayToast("wrong...");
		score.setText(s + " / 9");

	}

	class Game extends AsyncTask<String, Integer, String[]> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("starting....");
			s = 0;
			keyboardRandomizer();
			displayScore(false);
			// startStopwatch();

		}

		@Override
		protected String[] doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			set = produceRandomString();
			return set;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			showLettersOnTextViews(result);

		}

		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(String[] result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);

		}

	}

	/*
	 * public static void hideSoftKeyboard(Activity activity) {
	 * InputMethodManager inputMethodManager = (InputMethodManager) activity
	 * .getSystemService(Activity.INPUT_METHOD_SERVICE);
	 * inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
	 * .getWindowToken(), 0); }
	 */

	/*
	 * class TimeManager extends TimerTask {
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub /*
	 * Chronometer stopWatch = new Chronometer(getApplicationContext()); long
	 * startTime = SystemClock.elapsedRealtime();
	 * 
	 * 
	 * stopWatch.setOnChronometerTickListener(new OnChronometerTickListener(){
	 * 
	 * @Override public void onChronometerTick(Chronometer arg0) { long countUp
	 * = (SystemClock.elapsedRealtime() - arg0.getBase()) / 1000; String asText
	 * = (countUp / 60) + ":" + (countUp % 60); time.setText(asText); } });
	 * stopWatch.start(); //time.setText("");
	 * 
	 * 
	 * // TODO Auto-generated method stub long elapsedTime =
	 * SystemClock.elapsedRealtime() - initialTime; final String timeDisplay =
	 * elapsedTime / 60 + " : " + elapsedTime % 60; runOnUiThread(new Runnable()
	 * {
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub
	 * time.setText(timeDisplay); } }); } }
	 */

}
