package com.dammroute.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "commercial_alerts")
public class CommercialAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @Column(nullable = false) private String alertType;
    @Column(nullable = false) private String priority;
    @Column(nullable = false, length = 300) private String message;
    @Column(nullable = false, length = 300) private String recommendation;
    @Column(nullable = false) private String assignedChannel;
    private double expectedRevenueEur;
    private double conversionProbability;
    private int urgencyDays;
    @Column(nullable = false) private LocalDateTime createdAt;
    private String status = "OPEN";
    public CommercialAlert() {}
    public Long getId() { return id; }
    public Client getClient() { return client; }
    public String getAlertType() { return alertType; }
    public String getPriority() { return priority; }
    public String getMessage() { return message; }
    public String getRecommendation() { return recommendation; }
    public String getAssignedChannel() { return assignedChannel; }
    public double getExpectedRevenueEur() { return expectedRevenueEur; }
    public double getConversionProbability() { return conversionProbability; }
    public int getUrgencyDays() { return urgencyDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public void setId(Long id) { this.id = id; }
    public void setClient(Client client) { this.client = client; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setMessage(String message) { this.message = message; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public void setAssignedChannel(String assignedChannel) { this.assignedChannel = assignedChannel; }
    public void setExpectedRevenueEur(double expectedRevenueEur) { this.expectedRevenueEur = expectedRevenueEur; }
    public void setConversionProbability(double conversionProbability) { this.conversionProbability = conversionProbability; }
    public void setUrgencyDays(int urgencyDays) { this.urgencyDays = urgencyDays; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final CommercialAlert a = new CommercialAlert();
        public Builder client(Client v) { a.client = v; return this; }
        public Builder alertType(String v) { a.alertType = v; return this; }
        public Builder priority(String v) { a.priority = v; return this; }
        public Builder message(String v) { a.message = v; return this; }
        public Builder recommendation(String v) { a.recommendation = v; return this; }
        public Builder assignedChannel(String v) { a.assignedChannel = v; return this; }
        public Builder expectedRevenueEur(double v) { a.expectedRevenueEur = v; return this; }
        public Builder conversionProbability(double v) { a.conversionProbability = v; return this; }
        public Builder urgencyDays(int v) { a.urgencyDays = v; return this; }
        public Builder createdAt(LocalDateTime v) { a.createdAt = v; return this; }
        public Builder status(String v) { a.status = v; return this; }
        public CommercialAlert build() { return a; }
    }
}
