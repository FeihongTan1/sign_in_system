package team.a9043.sign_in_system.util;

import lombok.Data;

@Data
public class Gps {
    private double wgLat;
    private double wgLon;

    public Gps(double wgLat, double wgLon) {
        this.wgLat = wgLat;
        this.wgLon = wgLon;
    }
}
