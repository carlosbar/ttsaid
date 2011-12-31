/*
 * This file is part of the TTSAid project.
 *
 * Copyright (C) 2011-2012 Carlos Barcellos <carlosbar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.ttsaid;

import com.ttsaid.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

public class TTSActivity extends Activity {
	private int MY_DATA_CHECK_CODE = 0x0001;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private int interval = 0;
	private int languageId;
	private String language = "en";
	private boolean screenEvent = false;
	private boolean phoneNumber = false;

	private SharedPreferences prefs;

	/* get result from activities */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	/* list activity creation */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		//
		// preferences database
		//
		prefs = getSharedPreferences(LocalService.PREFS_DB, 0);

		// set current view

		setContentView(R.layout.config);
		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.version));

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		/* get parameters */

		interval = prefs.getInt("SET_INTERVAL", interval);
		screenEvent = prefs.getBoolean("SET_SCREEN_EVENT", screenEvent);
		phoneNumber = prefs.getBoolean("SET_PHONE_NUMBER", phoneNumber);
		language = prefs.getString("SET_LANGUAGE", language);
		if (language == "es") {
			languageId = R.id.es;
		} else {
			languageId = R.id.en;
		}

		// get widget id

		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// subclass seekbar events

		((SeekBar) findViewById(R.id.interval)).setMax(8);
		((SeekBar) findViewById(R.id.interval)).setKeyProgressIncrement(1);
		((SeekBar) findViewById(R.id.interval))
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						String hour = "";
						int m;

						interval = progress;
						if (progress == 0) {
							((TextView) findViewById(R.id.intervalValue))
									.setText("Off");
						}
						if (progress > 3) {
							hour = new Integer(progress / 4).toString();
						}
						m = new Integer(progress % 4 * 15);
						if (hour.length() > 0) {
							hour = hour + "h:" + String.format("%02d", m) + "m";
						} else {
							hour = m + " min";
						}
						((TextView) findViewById(R.id.intervalValue))
								.setText(hour);
					}
				});

		((RadioGroup) findViewById(R.id.radioGroup1))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(RadioGroup group, int checkedId) {
						languageId = checkedId;
					}
				});

		((ToggleButton) findViewById(R.id.screenEvent))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						screenEvent = isChecked;
					}
				});

		((ToggleButton) findViewById(R.id.phoneNumber))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						phoneNumber = isChecked;
					}
				});

		/* set current values */

		((SeekBar) findViewById(R.id.interval)).setProgress(interval);
		((RadioButton) findViewById(languageId)).setChecked(true);
		((ToggleButton) findViewById(R.id.screenEvent)).setChecked(screenEvent);
		((ToggleButton) findViewById(R.id.phoneNumber)).setChecked(phoneNumber);
		((EditText) findViewById(R.id.incomingMessage)).setText(prefs.getString("INCOMING_MESSAGE","Incoming Call!"));

		/* verify if TTS data is up to date */
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();

		RemoteViews views = new RemoteViews(TTSActivity.this.getPackageName(),
				R.layout.main);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(TTSActivity.this);

		/* set play action on widget canvas */

		Intent play = new Intent(LocalService.PLAY_SOUND);
		play.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				TTSActivity.this, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.play, pendingIntent);

		Intent config = new Intent(TTSActivity.this, TTSActivity.class);
		config.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		PendingIntent pendingConfig = PendingIntent.getActivity(
				TTSActivity.this, 0, config, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.config, pendingConfig);

		/* update view */

		appWidgetManager.updateAppWidget(mAppWidgetId, views);

		/* set new values on service */

		Intent intent = new Intent(TTSActivity.this, LocalService.class);
		startService(intent);

		/* set result */

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);

		/* save preferences */

		if (languageId == R.id.es) {
			language = "es";
		} else {
			language = "en";
		}

		SharedPreferences.Editor prefset = prefs.edit();
		prefset.putInt("SET_INTERVAL", interval);
		prefset.putBoolean("SET_SCREEN_EVENT", screenEvent);
		prefset.putBoolean("SET_PHONE_NUMBER", phoneNumber);
		prefset.putString("SET_LANGUAGE", language);
		prefset.putString("INCOMING_MESSAGE",((EditText) findViewById(R.id.incomingMessage)).getText().toString());		
		prefset.commit();

		/* start service */

		intent = new Intent(TTSActivity.this, LocalService.class);
		startService(intent);

		finish();
	}

}