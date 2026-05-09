package com.dammroute.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "route_stops")
public class RouteStop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @Column(nullable = false) private int stopOrder;
    @Column(nullable = false) private String status = "PENDING";
    private String estimatedArrival;
    @Column(length = 300) private String parkingInstruction;
    public RouteStop() {}
    public Long getId() { return id; }
    public Client getClient() { return client; }
    public int getStopOrder() { return stopOrder; }
    public String getStatus() { return status; }
    public String getEstimatedArrival() { return estimatedArrival; }
    public String getParkingInstruction() { return parkingInstruction; }
    public void setId(Long id) { this.id = id; }
    public void setClient(Client client) { this.client = client; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }
    public void setStatus(String status) { this.status = status; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public void setParkingInstruction(String parkingInstruction) { this.parkingInstruction = parkingInstruction; }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final RouteStop s = new RouteStop();
        public Builder client(Client v) { s.client = v; return this; }
        public Builder stopOrder(int v) { s.stopOrder = v; return this; }
        public Builder status(String v) { s.status = v; return this; }
        public Builder estimatedArrival(String v) { s.estimatedArrival = v; return this; }
        public Builder parkingInstruction(String v) { s.parkingInstruction = v; return this; }
        public RouteStop build() { return s; }
    }
}
