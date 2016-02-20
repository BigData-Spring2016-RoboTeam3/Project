package com.example.kranthi.lab3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.kranthi.lab3.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    SensorManager mSensorManager;
    Sensor mAcc, mLight, mProximity, mMagnetic, mHeartActivity;
    TextView textView, Acc, Lig, Pro, Mag, LocationText,heart;
    Button cameraClick;
    ImageView displayImage;
    LocationManager locationManager;
    String provider;
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_CAMERA=200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mHeartActivity = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);


        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeartActivity, SensorManager.SENSOR_DELAY_NORMAL);

        textView = (TextView) findViewById(R.id.text);
        Acc = (TextView) findViewById(R.id.text1);
        Lig = (TextView) findViewById(R.id.text3);
        Pro = (TextView) findViewById(R.id.text5);
        Mag = (TextView) findViewById(R.id.text7);
        LocationText = (TextView) findViewById(R.id.text9);
        heart = (TextView) findViewById(R.id.text11);

        displayImage = (ImageView) findViewById(R.id.imageView);
        cameraClick = (Button) findViewById(R.id.button);

        cameraClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQ_CODE_CAMERA);
            }
        });

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationText.setText("Permission not set");
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            LocationText.setText("Location not available");
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAcc) {
            Acc.setText(event.values[0] + "," + event.values[1] + "," + event.values[2]);

            if(event.values[0]<-8)
            {
                sendNotification("Turn Right");
            }
            else if(event.values[0]>8){

                sendNotification("Turn Left");
            }
            else if(event.values[1]>8)
            {
                sendNotification("Turn clockwise");
            }
            else if(event.values[1]<-8){
                sendNotification("Turn Anti-clockwise");
            }

        }
        if (event.sensor == mLight) {
            Lig.setText("" + event.values[0]);
            if(event.values[0]<20)
                sendNotification("The place is very dark");
            else if(event.values[0]>20&&event.values[0]<40)
                sendNotification("meduimly bright");
            else if(event.values[0]>40)
                sendNotification("The place is bright");
        }
        if (event.sensor == mMagnetic)
            Mag.setText(event.values[0] + "");
        if (event.sensor == mProximity) {
            Pro.setText(event.values[0] + "");
            int notificationId = 200;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this)
                    .setSmallIcon(R.drawable.notificationicon)
                    .setContentTitle("Proximity sensing")
                    .setContentText("Object detected at " + event.values[0]+" cms");
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);

            notificationManagerCompat.notify(notificationId, notificationBuilder.build());

        }
        if (event.sensor == mHeartActivity){

            heart.setText(event.values[0] + "");
            String notify=heart.getText().toString();
            int notificationId = 300;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this)
                    .setSmallIcon(R.drawable.notificationicon)
                    .setContentTitle("Heart rate sensing")
                    .setContentText(notify);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);

            notificationManagerCompat.notify(notificationId, notificationBuilder.build());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendNotification(String text){
        int notificationId = 100;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle("movement intstructions from phone to robo notifications")
                .setContentText(text);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);

        notificationManagerCompat.notify(notificationId,notificationBuilder.build());
    }
    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationText.setText("Permission not set");
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationText.setText("Permission not set");
            return;
        }
        locationManager.removeUpdates(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                }
                break;
            }
            case REQ_CODE_CAMERA:{
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                displayImage.setImageBitmap(bp);
                break;
            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        LocationText.setText("Lat :" + lat + " , Long :" + lng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
}


