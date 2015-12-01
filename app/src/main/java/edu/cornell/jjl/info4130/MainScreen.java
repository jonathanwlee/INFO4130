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
import android.widget.ImageView;
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
 *
 */
public class MainScreen extends Activity{
    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private static final int MAX_HUE=65535;
    public static final String TAG = "Health and Computation";
    private ImageView lightIcon,sleepIcon,dietIcon,activityIcon,showerIcon,groupIcon;
    private Typeface ralewayFont;
    private boolean activityRunning;
    private boolean sleepOn =false,dietOn=false,activityOn=false,showerOn=false,groupOn=false;
    private int score = 0;
    private int brightness = 255;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main_screen);
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        SharedPreferences sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE);
        //boolean firstRun = sharedPref.getBoolean("firstRun", false);
        boolean firstRun = false;
        if (firstRun) {
            Intent intent = new Intent(getApplicationContext(), StartScreen.class);
            startActivity(intent);
        }
        ralewayFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");

        initViews();
        initListeners();
    }

    public void onResume() {
        super.onResume();
        activityRunning=true;
    }

    public void dimLights() {
    }

    public void setLights(int hue,boolean on) {
        if (on) {
            score=score+5000;

        }
        else {
            score=score-5000;
        }
        PHLightState lightState = new PHLightState();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        for (PHLight light : allLights) {
            lightState.setHue(score+5000);
            lightState.setBrightness(100);
            lightState.setSaturation(100);
            bridge.updateLightState(light, lightState);
            Log.w("Hue Score: ", Integer.toString(score));
        }
    }

    protected void onPause() {
        super.onPause();
        activityRunning=false;
        finish();
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

    public void initViews() {
        sleepIcon = (ImageView) findViewById(R.id.sleepIcon);
        dietIcon = (ImageView) findViewById(R.id.dietIcon);
        groupIcon = (ImageView) findViewById(R.id.groupIcon);
        activityIcon = (ImageView) findViewById(R.id.activeIcon);
        lightIcon = (ImageView) findViewById(R.id.lightIcon);
        showerIcon = (ImageView) findViewById(R.id.showerIcon);
    }

    public void initListeners() {
        lightIcon.setOnClickListener(new OnClickListener() {
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

        sleepIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sleepOn) {
                sleepIcon.setImageResource(R.mipmap.sleepgray);
                    sleepOn=false;
                    setLights(score,false);

                }
                else {
                    sleepIcon.setImageResource(R.mipmap.sleepcolor);
                    sleepOn=true;
                    setLights(score,true);

                }
                }
        });

        groupIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupOn) {
                    groupIcon.setImageResource(R.mipmap.groupgray);
                    groupOn=false;
                    setLights(score,false);
                }
                else {
                    groupIcon.setImageResource(R.mipmap.groupcolor);
                    groupOn=true;
                    setLights(score,true);

                }
            }
        });
        activityIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activityOn) {
                    activityIcon.setImageResource(R.mipmap.activegray);
                    activityOn=false;
                    setLights(score,false);

                }
                else {
                    activityIcon.setImageResource(R.mipmap.activecolor);
                    activityOn=true;
                    //setLights(score,true);
                    PHLightState lightState = new PHLightState();
                    List<PHLight> allLights = bridge.getResourceCache().getAllLights();
                    for (PHLight light : allLights) {
                        lightState.setHue(56100);
                        lightState.setBrightness(90);
                        lightState.setSaturation(100);
                        bridge.updateLightState(light,lightState);
                    }


                }
            }
        });
        showerIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showerOn) {
                    showerIcon.setImageResource(R.mipmap.showergray);
                    showerOn=false;
                    setLights(score,false);

                }
                else {
                    showerIcon.setImageResource(R.mipmap.showercolor);
                    showerOn=true;
                    setLights(score,true);

                }
            }
        });

        dietIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dietOn) {
                    dietIcon.setImageResource(R.mipmap.dietgray);
                    dietOn=false;
                    setLights(score,false);

                }
                else {
                    dietIcon.setImageResource(R.mipmap.dietcolor);
                    dietOn=true;
                    setLights(score,true);

                }
            }
        });

    }
}
