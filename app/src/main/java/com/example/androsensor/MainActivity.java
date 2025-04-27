package com.example.androsensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private LinearLayout sensorContainer;
    private Map<Integer, View> sensorViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        sensorContainer = findViewById(R.id.sensorContainer);
        
        // Initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        // Set up refresh button
        ImageButton refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> refreshSensors());
        
        // Initial load of sensors
        loadSensors();
    }
    
    private void loadSensors() {
        // Clear existing views
        sensorContainer.removeAllViews();
        sensorViews.clear();
        
        // Get list of all sensors
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        
        for (Sensor sensor : sensorList) {
            View sensorView = createSensorView(sensor);
            sensorContainer.addView(sensorView);
            sensorViews.put(sensor.getType(), sensorView);
            
            // Register listener for this sensor
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    private View createSensorView(Sensor sensor) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_sensor_card, null);
        
        TextView nameTextView = view.findViewById(R.id.sensorNameTextView);
        TextView typeTextView = view.findViewById(R.id.sensorTypeTextView);
        TextView valueTextView = view.findViewById(R.id.sensorValueTextView);
        
        nameTextView.setText(sensor.getName());
        
        // Set sensor type category
        String typeCategory = getSensorCategory(sensor.getType());
        typeTextView.setText(typeCategory);
        
        // Initialize with empty value
        valueTextView.setText("Waiting for data...");
        
        return view;
    }
    
    private String getSensorCategory(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_PROXIMITY:
                return "Motion Sensor";
                
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
            case Sensor.TYPE_TEMPERATURE:
                return "Environmental Sensor";
                
            default:
                return "Other Sensor";
        }
    }
    
    private void refreshSensors() {
        // Unregister all listeners
        sensorManager.unregisterListener(this);
        
        // Reload all sensors
        loadSensors();
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get the view for this sensor type
        View sensorView = sensorViews.get(event.sensor.getType());
        if (sensorView != null) {
            TextView valueTextView = sensorView.findViewById(R.id.sensorValueTextView);
            
            // Format and display sensor values based on sensor type
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                case Sensor.TYPE_GYROSCOPE:
                case Sensor.TYPE_MAGNETIC_FIELD:
                    valueTextView.setText(String.format("X: %.2f, Y: %.2f, Z: %.2f", 
                            event.values[0], event.values[1], event.values[2]));
                    break;
                    
                case Sensor.TYPE_LIGHT:
                    valueTextView.setText(String.format("%.0f lux", event.values[0]));
                    break;
                    
                case Sensor.TYPE_PROXIMITY:
                    valueTextView.setText(String.format("%.0f cm", event.values[0]));
                    break;
                    
                default:
                    // For other sensors with variable number of values
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < event.values.length; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(String.format("%.2f", event.values[i]));
                    }
                    valueTextView.setText(sb.toString());
                    break;
            }
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this example
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Re-register listeners when activity resumes
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listeners when activity is paused to save battery
        sensorManager.unregisterListener(this);
    }
}