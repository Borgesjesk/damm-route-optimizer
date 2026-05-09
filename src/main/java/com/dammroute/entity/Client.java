package com.dammroute.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "clients")
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String address;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    private String deliveryWindowStart;
    private String deliveryWindowEnd;
    @Column(nullable = false) private String parkingDifficulty = "MEDIUM";
    private String nearestLoadingBay;
    private boolean active = true;
    public Client() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDeliveryWindowStart() { return deliveryWindowStart; }
    public String getDeliveryWindowEnd() { return deliveryWindowEnd; }
    public String getParkingDifficulty() { return parkingDifficulty; }
    public String getNearestLoadingBay() { return nearestLoadingBay; }
    public boolean isActive() { return active; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setDeliveryWindowStart(String s) { this.deliveryWindowStart = s; }
    public void setDeliveryWindowEnd(String s) { this.deliveryWindowEnd = s; }
    public void setParkingDifficulty(String s) { this.parkingDifficulty = s; }
    public void setNearestLoadingBay(String s) { this.nearestLoadingBay = s; }
    public void setActive(boolean active) { this.active = active; }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Client c = new Client();
        public Builder name(String v) { c.name = v; return this; }
        public Builder address(String v) { c.address = v; return this; }
        public Builder latitude(double v) { c.latitude = v; return this; }
        public Builder longitude(double v) { c.longitude = v; return this; }
        public Builder deliveryWindowStart(String v) { c.deliveryWindowStart = v; return this; }
        public Builder deliveryWindowEnd(String v) { c.deliveryWindowEnd = v; return this; }
        public Builder parkingDifficulty(String v) { c.parkingDifficulty = v; return this; }
        public Builder nearestLoadingBay(String v) { c.nearestLoadingBay = v; return this; }
        public Builder active(boolean v) { c.active = v; return this; }
        public Client build() { return c; }
    }
}
