package edu.cornell.jjl.info4130;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.  
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 * 
 * @author SteveyO
 *
 */
public class MainScreen extends Activity implements SensorEventListener{
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE=65535;
    public static final String TAG = "QuickStart";

    private TextView settings,score,scoreTitle;
    private Typeface ralewayFont;
    private SensorManager sensorManager;
    private boolean activityRunning;
   // private Sensor countSensor;
    private int brightness = 255;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();

        if (activityRunning) {
            score.setText(String.valueOf(event.values[0]));
        }
        }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main_screen);
        phHueSDK = PHHueSDK.create();

        SharedPreferences sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE);
        boolean firstRun = sharedPref.getBoolean("firstRun", false);
        if (firstRun) {
            Intent intent = new Intent(getApplicationContext(), StartScreen.class);
            startActivity(intent);
        }
        score = (TextView) findViewById(R.id.score);
        scoreTitle =(TextView) findViewById(R.id.scoreTitle);
        ralewayFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
       // countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);


        settings = (TextView) findViewById(R.id.settingsText);
        settings.setTypeface(ralewayFont);
        score.setTypeface(ralewayFont);
        scoreTitle.setTypeface(ralewayFont);

        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("firstRun",true);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), StartScreen.class);
                startActivity(intent);

            }
        });
    }

    public void onResume() {
        super.onResume();
        activityRunning=true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            Toast.makeText(this, "Count sensor registered!", Toast.LENGTH_LONG).show();

            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    public void dimLights() {
    }

    protected void onPause() {
        super.onPause();
        activityRunning=false;
    }

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {
            
            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }
            
            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }
}
