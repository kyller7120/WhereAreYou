package l.m.dev.whereareyou;

public class UserLocation {
    public double latitude;
    public double longitude;

    public UserLocation() {
        // Constructor vacío requerido para Firebase
    }

    public UserLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
