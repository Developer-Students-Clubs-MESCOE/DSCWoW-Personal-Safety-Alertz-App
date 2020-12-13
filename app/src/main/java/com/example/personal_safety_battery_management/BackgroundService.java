package com.example.personal_safety_battery_management;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.getIntent;
import static android.content.Intent.getIntentOld;

public class BackgroundService extends Service {


    static int battery_level=0;
    static int automatic_alert_battery=0;
    double currLat,currLong;
    LocationTrack locationTrack,lt;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Entered the background service...");
        batterylevel();
        System.out.println("Battery level hai "+BackgroundService.battery_level);

        lt=new LocationTrack(BackgroundService.this);
        locationTrack = new LocationTrack(BackgroundService.this);


        if (locationTrack.canGetLocation()) {


            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();
            this.currLat=latitude;
            this.currLong=longitude;
            System.out.println("lat:"+longitude+" - "+latitude);

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {

            locationTrack.showSettingsAlert();
        }

//        currLat=MainActivity.currLat;
//        currLong=MainActivity.currLong;
//        double homeLat=HomeLocation.homeLat;
//        double homeLong=HomeLocation.homeLong;
//        String diff = String.format("%.2f",distance(currLat,homeLat,currLong,homeLong));

//        DataBaseHelper dataBaseHelper = new DataBaseHelper(BackgroundService.this);
//        automatic_alert_battery = dataBaseHelper.getAutomatic_alert_battery();

        if(battery_level<=62) {
            automatic_alert();
            Toast.makeText(BackgroundService.this,"background service",Toast.LENGTH_SHORT).show();

        }
        return START_STICKY;
    }

    private void automatic_alert() {
    batterylevel();

        final ArrayList<String> phone= new ArrayList<>();
        final ArrayList<String> name= new ArrayList<>();

        DataBaseHelper dataBaseHelper = new DataBaseHelper(BackgroundService.this);
        List<ContactModel> everyone = dataBaseHelper.getEveryone();
        System.out.println(everyone.toString());
        if(!everyone.isEmpty()) {
            phone.add(everyone.get(0).getPhone());
            name.add(everyone.get(0).getName());
            phone.add(everyone.get(1).getPhone());
            name.add(everyone.get(1).getName());
            phone.add(everyone.get(2).getPhone());
            name.add(everyone.get(2).getName());
        }
        String loc = "https://maps.google.com/?q="+currLat+","+currLong;

        String msg_temp="Sent from SAFETY BATTERY ALERT.  My battery is about to die (Automatic alert).\n Battery: "+battery_level+"%.\n Current location: "+loc;
//        AlertModel alertModel = new AlertModel(-1,battery_level,loc,msg_temp,name.get(0),name.get(1),name.get(2),phone.get(0),phone.get(1),phone.get(2));
//        boolean success = dataBaseHelper.addOneAlert(alertModel);
//        String successMsg= success==true?"Added to database":"Error occurred";
//        SMS.sendSMS(phone,msg_temp);
        System.out.println("Sending "+msg_temp);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private int batterylevel(){
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int raw_level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
                int level =-1;
                if(raw_level>=0 && scale>0){
                    level = (raw_level*100)/scale;
                }
                battery_level = level;
                //battery.setText(String.valueOf(level) + "%");
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatInfoReceiver, batteryLevelFilter);
    }


    public static double distance(double lat1,
                                  double lat2, double lon1,
                                  double lon2)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c * r);
    }

}
