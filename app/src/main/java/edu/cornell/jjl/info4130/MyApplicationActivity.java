package edu.cornell.jjl.info4130;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.  
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 * 
 * @author SteveyO
 *
 */
public class MyApplicationActivity extends Activity {
    private PHHueSDK phHueSDK;
    private int brightness = 255;
    private static final int MAX_HUE=65535;
    public static final String TAG = "QuickStart";
    private Handler handler = new Handler();

    Runnable running = new Runnable() {
        public void run() {
            PHBridge bridge = phHueSDK.getSelectedBridge();
            List<PHLight> allLights = bridge.getResourceCache().getAllLights();
            for (PHLight light : allLights) {
                PHLightState lightState = new PHLightState();
                Log.w("BRI: ",Integer.toString(lightState.getBrightness()));
                Log.w("SAT: ",Integer.toString(lightState.getSaturation()));
                Log.w("HUE: ", Integer.toString(lightState.getHue()));
            }

            handler.postDelayed(this,100);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main2);
        phHueSDK = PHHueSDK.create();

        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        List  <String> lightIdentifiers = new ArrayList<String>();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        for (PHLight light : allLights) {
            lightIdentifiers.add(light.getIdentifier());
        }

        bridge.createGroup("Test", lightIdentifiers, null);
        PHSchedule wakeSchedule = new PHSchedule("Wake Up");
        PHLightState lightState = new PHLightState();
        lightState.setOn(false);


        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        wakeSchedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_ALL_DAY.getValue());
        wakeSchedule.setLightState(lightState);
        wakeSchedule.setGroupIdentifier("Test");
        wakeSchedule.setLocalTime(true);
        wakeSchedule.setDate(cal.getTime());

        bridge.createSchedule(wakeSchedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule schedule) {
                        Log.w("Created:", "Ran");
                    }

                    @Override
                    public void onSuccess() {
                        Log.w("SUCCESS:", "Ran");

                    }

                    @Override
                    public void onStateUpdate(Map<String,String> successAttribute, List<PHHueError> errorAttribute) {
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });

        Button randomButton;
        randomButton = (Button) findViewById(R.id.buttonRand);
        randomButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                randomLights();
            }
        });
    }

    public void randomLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        PHLightState lightState1 = new PHLightState();
        lightState1.setHue(46920);
        bridge.setLightStateForDefaultGroup(lightState1);

        PHLightState lightState = new PHLightState();
        lightState.setOn(false);
        lightState.setTransitionTime(12000);
        bridge.setLightStateForDefaultGroup(lightState);


    }

    public void dimLights() {
    }

    // If you want to handle the response from the bridge, create a PHLightListener object.
    PHLightListener listener = new PHLightListener() {
        
        @Override
        public void onSuccess() {

        }
        
        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
           Log.w(TAG, "Light has updated");
        }
        
        @Override
        public void onError(int arg0, String arg1) {}

        @Override
        public void onReceivingLightDetails(PHLight arg0) {}

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {}

        @Override
        public void onSearchComplete() {}
    };
    
    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {
            
            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }
            
            phHueSDK.disconnect(bridge);
            handler.removeCallbacks(running);
            super.onDestroy();
        }
    }
}
