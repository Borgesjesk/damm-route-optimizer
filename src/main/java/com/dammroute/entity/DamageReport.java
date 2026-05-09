package com.dammroute.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "damage_reports")
public class DamageReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_stop_id", nullable = false)
    private RouteStop routeStop;

    @Column(nullable = false) private String productReference;
    @Column(nullable = false) private String productName;
    @Column(nullable = false) private int quantityAffected;

    // DELIVERY_DAMAGED / RETURN_REJECTED / MISSING
    @Column(nullable = false) private String damageType;

    @Column(length = 500) private String notes;
    @Column(nullable = false) private LocalDateTime reportedAt;

    // SUPERVISOR / WAREHOUSE / COMMERCIAL
    private String assignedTo;

    // OPEN / RESOLVED
    private String status = "OPEN";

    public DamageReport() {}

    public Long getId() { return id; }
    public RouteStop getRouteStop() { return routeStop; }
    public String getProductReference() { return productReference; }
    public String getProductName() { return productName; }
    public int getQuantityAffected() { return quantityAffected; }
    public String getDamageType() { return damageType; }
    public String getNotes() { return notes; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public String getAssignedTo() { return assignedTo; }
    public String getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setRouteStop(RouteStop routeStop) { this.routeStop = routeStop; }
    public void setProductReference(String productReference) { this.productReference = productReference; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantityAffected(int quantityAffected) { this.quantityAffected = quantityAffected; }
    public void setDamageType(String damageType) { this.damageType = damageType; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setStatus(String status) { this.status = status; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final DamageReport r = new DamageReport();
        public Builder routeStop(RouteStop v) { r.routeStop = v; return this; }
        public Builder productReference(String v) { r.productReference = v; return this; }
        public Builder productName(String v) { r.productName = v; return this; }
        public Builder quantityAffected(int v) { r.quantityAffected = v; return this; }
        public Builder damageType(String v) { r.damageType = v; return this; }
        public Builder notes(String v) { r.notes = v; return this; }
        public Builder reportedAt(LocalDateTime v) { r.reportedAt = v; return this; }
        public Builder assignedTo(String v) { r.assignedTo = v; return this; }
        public Builder status(String v) { r.status = v; return this; }
        public DamageReport build() { return r; }
    }
}
