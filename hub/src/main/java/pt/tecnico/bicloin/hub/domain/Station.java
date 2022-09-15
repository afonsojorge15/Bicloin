package pt.tecnico.bicloin.hub.domain;

public class Station {
    private final String name;
    private final String id;
    private final double latitude;
    private final double longitude;
    private final int totalDocks;
    private final int totalPrizes;


    public Station(String name, String id, double latitude, double longitude, int totalDocks, int totalPrizes) {
        this.name = name;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalDocks = totalDocks;
        this.totalPrizes = totalPrizes;
    }

    String getName() {
        return name;
    }

    String getId() {
        return id;
    }

    double getLatitude() {
        return latitude;
    }

    double getLongitude() {
        return longitude;
    }

    int getTotalDocks() {
        return totalDocks;
    }

    int getTotalPrizes() {
        return totalPrizes;
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", totalDocks=" + totalDocks +
                ", totalPrices=" + totalPrizes +
                '}';
    }
}
