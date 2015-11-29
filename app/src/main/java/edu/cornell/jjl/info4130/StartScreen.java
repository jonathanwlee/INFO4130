package edu.cornell.jjl.info4130;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.philips.lighting.hue.listener.PHBridgeConfigurationListener;
import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.listener.PHTimeZoneListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class StartScreen extends AppCompatActivity {
    private PHHueSDK phHueSDK;
    private PHSchedule wakeSchedule; //Only one Wake Schedule is shown
    private PHSchedule sleepSchedule1; //First Sleep Schedule that is shown
    private PHSchedule sleepSchedule2; //Second Sleep Schedule that is shown subsequent
    private PHBridge bridge;
    private PHLightState wakeColor,transitionToSleepColor,transitiontoSleepDim;

    private PHBridgeConfigurationListener bridgeConfigListener = new PHBridgeConfigurationListener() {
        @Override
        public void onReceivingConfiguration(PHBridgeConfiguration phBridgeConfiguration) {

        }

        @Override
        public void onSuccess() {
            Log.w("SUCCESS: ", "on Success");
        }

        @Override
        public void onError(int i, String s) {
            Log.w("ERROR: ", "Bridge Config Error");
        }

        @Override
        public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

        }
    };

    private PHScheduleListener bridgeListener = new PHScheduleListener() {
        @Override
        public void onCreated(PHSchedule schedule) {
            Log.w("Created:", "Schedule Name:  " + schedule.getName());
            Log.w("Created:", "Schedule Identifier:  " + schedule.getIdentifier());

        }

        @Override
        public void onSuccess() {
            Log.w("On Success:", "on Success Ran");

        }

        @Override
        public void onStateUpdate(Map<String,String> successAttribute, List<PHHueError> errorAttribute) {
            Log.w("On Updated: ", "State has Been Updated.");

        }

        @Override
        public void onError(int i, String s) {
            Log.w("on Error Message: ", s);
        }
    };

    private PHTimeZoneListener timeZoneListener = new PHTimeZoneListener() {


        @Override
        public void onSuccess(List<String> list) {
            for (String t : list) {

            }
        }

        @Override
        public void onError(String s) {

        }
    };
        /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public static Typeface ralewayFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        phHueSDK = PHHueSDK.create();
        initBridgeSettings();
        initLightStates();
        ralewayFont = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
    }
    /*
    * Inits Bridge Settings: Lights, Timezone.
    * */
    public void initBridgeSettings() {
        bridge = PHHueSDK.getInstance().getSelectedBridge();
        List<String> lightIdentifiers = new ArrayList<String>();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
            lightIdentifiers.add(light.getIdentifier());
        }

        bridge.createGroup("Lights", lightIdentifiers, null);
        bridge.getSupportedTimeZones(timeZoneListener);

        PHBridgeConfiguration config = new PHBridgeConfiguration();
        config.setTimeZone("EST");
        bridge.updateBridgeConfigurations(config, bridgeConfigListener);
    }

    /*
    * WakeColor: Blue. Set Light On.
    * TransitionToSleepColor: Sets Transition Time to Red.
    * TransitionToSleepDim: Sets Transition to Brightness of 0.
    * */
    public void initLightStates() {
        wakeColor = new PHLightState();
        wakeColor.setHue(46920);
        wakeColor.setOn(true);

        transitionToSleepColor = new PHLightState();
        transitionToSleepColor.setTransitionTime(18000);
        transitionToSleepColor.setHue(300);

        transitiontoSleepDim = new PHLightState();
        transitiontoSleepDim.setTransitionTime(18000);
        transitiontoSleepDim.setBrightness(0);

    }

    public void scheduleAlarms(int wakeMinute, int wakeHour, int sleepMinute, int sleepHour) {

        wakeSchedule = new PHSchedule("Wake Alarm","WakeSchedule");
        sleepSchedule1 = new PHSchedule("Sleep Alarm 1st","SleepSchedule1");
        sleepSchedule2 = new PHSchedule("Sleep Alarm 2nd","SleepSchedule2");

        //Init Alarm Times.
        Calendar wakeColorTime = Calendar.getInstance();
        wakeColorTime.set(Calendar.SECOND, 1);
        wakeColorTime.set(Calendar.MINUTE, wakeMinute);
        wakeColorTime.set(Calendar.HOUR_OF_DAY, wakeHour);

        Calendar sleepCalColorTransition = Calendar.getInstance();
        sleepCalColorTransition.set(Calendar.SECOND, 1);
        sleepCalColorTransition.set(Calendar.MINUTE, sleepMinute);
        sleepCalColorTransition.set(Calendar.HOUR_OF_DAY, sleepHour);

        Calendar sleepCalDimTransition = Calendar.getInstance();
        sleepCalDimTransition.set(Calendar.SECOND, 30);

        int sleepMinuteMod;
        int sleepHourMod;
        //Fixes any errors with 24 hour time.
        if (sleepMinute +35 > 59) {
            sleepMinuteMod = (sleepMinute + 35) % 60;
            if (sleepHour+1 < 24) {
                sleepHourMod = sleepHour+1;
            }
            else {
                sleepHourMod= 0;
            }
        }
        else {
            sleepHourMod = sleepHour;
            sleepMinuteMod = sleepMinute + 35;
        }

        sleepCalDimTransition.set(Calendar.MINUTE, sleepMinuteMod);
        sleepCalDimTransition.set(Calendar.HOUR_OF_DAY, sleepHourMod);

        /*
        * This section of code initializes the wake and sleep schedules.
        * WakeSchedule: Sets to Blue Light upon alarm in the morning.
        * SleepSchedule1: Transitions Light towards Red Progrssively.
        * SleepSchedule2: Transitions Light towards Brightness of 0.
        * */
        wakeSchedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_ALL_DAY.getValue());
        wakeSchedule.setLightState(wakeColor);
        wakeSchedule.setGroupIdentifier("Lights");
        wakeSchedule.setLocalTime(true);
        wakeSchedule.setDate(wakeColorTime.getTime());

        sleepSchedule1.setRecurringDays(PHSchedule.RecurringDay.RECURRING_ALL_DAY.getValue());
        sleepSchedule1.setLightState(transitionToSleepColor);
        sleepSchedule1.setGroupIdentifier("Lights");
        sleepSchedule1.setLocalTime(true);
        sleepSchedule1.setDate(sleepCalColorTransition.getTime());

        sleepSchedule2.setRecurringDays(PHSchedule.RecurringDay.RECURRING_ALL_DAY.getValue());
        sleepSchedule2.setLightState(transitiontoSleepDim);
        sleepSchedule2.setGroupIdentifier("Lights");
        sleepSchedule2.setLocalTime(true);
        sleepSchedule2.setTimer(700);
        sleepSchedule2.setDate(sleepCalDimTransition.getTime());

        Map<String,PHSchedule> bridgeSchedules = bridge.getResourceCache().getSchedules();

        if(bridgeSchedules.isEmpty()) {
            bridge.createSchedule(wakeSchedule, bridgeListener);
            bridge.createSchedule(sleepSchedule1, bridgeListener);
            bridge.createSchedule(sleepSchedule2, bridgeListener);
        }

        else {
            bridge.updateSchedule(wakeSchedule, bridgeListener);
            bridge.updateSchedule(sleepSchedule1, bridgeListener);
            bridge.updateSchedule(sleepSchedule2, bridgeListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a StartScreenFragment (defined as a static inner class below).
            return StartScreenFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        }

    /**
     * A placeholder fragment containing our view.
     */
    public static class StartScreenFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public static TimePicker wake;
        public static TimePicker sleep;
        public static TextView wakeText,sleepText,continueText,startScreenText;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static StartScreenFragment newInstance(int sectionNumber) {
            StartScreenFragment fragment = new StartScreenFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public StartScreenFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                View rootView = inflater.inflate(R.layout.fragment_start_screen0, container, false);
                startScreenText = (TextView) rootView.findViewById(R.id.startScreenText);
                startScreenText.setTypeface(((StartScreen) getActivity()).ralewayFont);
                return rootView;
            }

            else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                View rootView = inflater.inflate(R.layout.fragment_start_screen, container, false);
                wake = (TimePicker) rootView.findViewById(R.id.timePicker1);
                wakeText = (TextView) rootView.findViewById(R.id.wakeText);
                wakeText.setTypeface(((StartScreen) getActivity()).ralewayFont);
                return rootView;
            }
            else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                View rootView = inflater.inflate(R.layout.fragment_start_screen2, container, false);
                sleep = (TimePicker) rootView.findViewById(R.id.timePicker2);
                sleepText = (TextView) rootView.findViewById(R.id.sleepText);
                sleepText.setTypeface(((StartScreen) getActivity()).ralewayFont);

            return rootView;
            }

            else {
                View rootView = inflater.inflate(R.layout.fragment_start_screen3, container, false);

                Button startButton = (Button) rootView.findViewById(R.id.startButton);

                startButton.setTypeface(((StartScreen) getActivity()).ralewayFont);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int wakeHour = wake.getCurrentHour();
                        int wakeMinute = wake.getCurrentMinute();
                        int sleepHour = sleep.getCurrentHour();
                        int sleepMinute = sleep.getCurrentMinute();

                        //Store Preferences.
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("test", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("wakeHour", wakeHour);
                        editor.putInt("wakeMinute", wakeMinute);
                        editor.putInt("sleepHour", sleepHour);
                        editor.putInt("sleepMinute", sleepMinute);
                        editor.putBoolean("firstRun", false);

                        editor.commit();

                        ((StartScreen) getActivity()).scheduleAlarms(wakeMinute, wakeHour, sleepMinute, sleepHour);
                        Intent intent = new Intent(getActivity().getApplicationContext(), MainScreen.class);
                        startActivity(intent);
                    }
                });

                return rootView;
            }
        }
    }
}
