package com.dammroute.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    // Delivery confirmation fields
    private LocalDateTime deliveredAt;
    @Column(length = 2000) private String signatureBase64;
    private String deliveryNotes;
    private int returnablesCollected;
    private boolean hasReturnables;

    public RouteStop() {}

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public int getStopOrder() { return stopOrder; }
    public String getStatus() { return status; }
    public String getEstimatedArrival() { return estimatedArrival; }
    public String getParkingInstruction() { return parkingInstruction; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public String getSignatureBase64() { return signatureBase64; }
    public String getDeliveryNotes() { return deliveryNotes; }
    public int getReturnablesCollected() { return returnablesCollected; }
    public boolean isHasReturnables() { return hasReturnables; }

    public void setId(Long id) { this.id = id; }
    public void setClient(Client client) { this.client = client; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }
    public void setStatus(String status) { this.status = status; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public void setParkingInstruction(String parkingInstruction) { this.parkingInstruction = parkingInstruction; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setSignatureBase64(String signatureBase64) { this.signatureBase64 = signatureBase64; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    public void setReturnablesCollected(int returnablesCollected) { this.returnablesCollected = returnablesCollected; }
    public void setHasReturnables(boolean hasReturnables) { this.hasReturnables = hasReturnables; }

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
