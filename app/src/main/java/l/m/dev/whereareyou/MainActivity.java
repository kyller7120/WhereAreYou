package l.m.dev.whereareyou;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView image, arrow;
    private TextView tvHeading, tvUserLocation;
    private float currentDegree = 0f;
    private float currentDegreeNeedle = 0f;

    private SensorManager mSensorManager;
    private Location userLoc = new Location("service Provider");

    private LocationManager locationManager;

    // Coordenadas del destino
    private final double DEST_LAT = 13.722141127699249; // Latitud destino
    private final double DEST_LON = -88.93846870277245; // Longitud destino

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Actualizar ubicación del usuario
            userLoc.setLongitude(location.getLongitude());
            userLoc.setLatitude(location.getLatitude());
            userLoc.setAltitude(location.getAltitude());

            // Mostrar las coordenadas del usuario en pantalla
            String userCoordinates = "Lat: " + location.getLatitude() +
                    "\nLon: " + location.getLongitude();
            tvUserLocation.setText(userCoordinates);

            // Sincronizar con Firebase (opcional)
            saveToFirebase(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // No se requiere acción
        }

        @Override
        public void onProviderEnabled(String provider) {
            // No se requiere acción
        }

        @Override
        public void onProviderDisabled(String provider) {
            // No se requiere acción
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        image = findViewById(R.id.imageCompass);
        arrow = findViewById(R.id.needle);
        tvHeading = findViewById(R.id.heading);
        tvUserLocation = findViewById(R.id.tvUserLocation); // Mostrar ubicación

        // Inicializar sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Inicializar LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            obtenerUbicacionUsuario();
        }
    }

    // Método para obtener la ubicación del usuario
    private void obtenerUbicacionUsuario() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

                // Obtener última ubicación conocida
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    userLoc.setLongitude(lastKnownLocation.getLongitude());
                    userLoc.setLatitude(lastKnownLocation.getLatitude());
                    userLoc.setAltitude(lastKnownLocation.getAltitude());

                    String userCoordinates = "Lat: " + lastKnownLocation.getLatitude() +
                            "\nLon: " + lastKnownLocation.getLongitude();
                    tvUserLocation.setText(userCoordinates);
                }
            } else {
                tvUserLocation.setText("GPS no está activado.");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            tvUserLocation.setText("No se puede obtener la ubicación.");
        }
    }

    // Manejo del resultado de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionUsuario();
            } else {
                tvUserLocation.setText("Permiso de ubicación denegado.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Obtener el valor de la brújula
        float degree = event.values[0];

        // Definir ubicación del destino
        Location destinationLoc = new Location("service Provider");
        destinationLoc.setLatitude(DEST_LAT);
        destinationLoc.setLongitude(DEST_LON);

        // Obtener la dirección hacia el destino
        float bearTo = userLoc.bearingTo(destinationLoc);
        if (bearTo < 0) {
            bearTo += 360;
        }

        // Calcular la dirección final
        float direction = bearTo - degree;
        if (direction < 0) {
            direction += 360;
        }

        // Mostrar el ángulo de la brújula
        tvHeading.setText("Heading: " + degree + "°");

        // Animación de la aguja de la brújula
        RotateAnimation raQibla = new RotateAnimation(
                currentDegreeNeedle, direction,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        raQibla.setDuration(210);
        raQibla.setFillAfter(true);
        arrow.startAnimation(raQibla);
        currentDegreeNeedle = direction;

        // Animación del fondo de la brújula
        RotateAnimation ra = new RotateAnimation(
                currentDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se requiere acción
    }

    // Método para guardar la ubicación en Firebase
    private void saveToFirebase(double latitude, double longitude) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("UserLocation");

        // Guardar coordenadas
        ref.setValue(new UserLocation(latitude, longitude));
    }

    // Clase para modelar la ubicación del usuario
    public static class UserLocation {
        public double latitude;
        public double longitude;

        public UserLocation() {
        }

        public UserLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
