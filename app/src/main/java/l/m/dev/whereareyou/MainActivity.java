package l.m.dev.whereareyou;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.Manifest;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;
    private DatabaseReference database;

    private TextInputEditText latitudeEditText;
    private TextInputEditText longitudeEditText;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeEditText = findViewById(R.id.latitude_value);
        longitudeEditText = findViewById(R.id.longitude_value);

        //Iniciando objetos para firebase y localizacion
        database = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        //Solicitar permisos al usuario
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getCurrentLocation();
        }

    }

    private void updateLocationInDatabase(String userId, double latitude, double longitude) {
        // Crea un objeto UserLocation y lo guarda en la base de datos
        UserLocation userLocation = new UserLocation(latitude, longitude);
        database.child("users").child(userId).setValue(userLocation);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Aquí puedes enviar la ubicación a la base de datos o usarla directamente.
                            updateLocationInDatabase("user1", latitude, longitude);
                            latitudeEditText.setText(String.valueOf(latitude));
                            longitudeEditText.setText(String.valueOf(longitude));

                        }
                    }
                });
    }

    private float calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double deltaLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(deltaLon) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(deltaLon);
        return (float) (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimut = (float) Math.toDegrees(orientation[0]);
                // Usa azimut con la dirección calculada para rotar la aguja
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    //funcionalidad para recibir la ubicación del otro usuario
    private void fetchOtherUserLocation(String userId) {
        database.child("users").child(userId).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                if (userLocation != null) {
                    double latitude = userLocation.latitude;
                    double longitude = userLocation.longitude;

                    // Aquí puedes usar la ubicación obtenida (por ejemplo, mostrarla en un TextView)
                    // Ejemplo: Actualizar un TextView
                    System.out.println("Ubicación del usuario: Latitud=" + latitude + ", Longitud=" + longitude);
                }
            }
        });
    }


    private class TextView {
    }
}