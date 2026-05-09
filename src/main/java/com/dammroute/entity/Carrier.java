package com.dammroute.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "carriers")
public class Carrier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String licenseNumber;
    @Column(nullable = false) private int trustScore;
    @Column(nullable = false) private String trustLevel;
    @Column(nullable = false) private boolean documentVerified;
    private int disputeCount;
    private boolean identityFlagged;
    private boolean active = true;
    public Carrier() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLicenseNumber() { return licenseNumber; }
    public int getTrustScore() { return trustScore; }
    public String getTrustLevel() { return trustLevel; }
    public boolean isDocumentVerified() { return documentVerified; }
    public int getDisputeCount() { return disputeCount; }
    public boolean isIdentityFlagged() { return identityFlagged; }
    public boolean isActive() { return active; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLicenseNumber(String s) { this.licenseNumber = s; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }
    public void setTrustLevel(String trustLevel) { this.trustLevel = trustLevel; }
    public void setDocumentVerified(boolean documentVerified) { this.documentVerified = documentVerified; }
    public void setDisputeCount(int disputeCount) { this.disputeCount = disputeCount; }
    public void setIdentityFlagged(boolean identityFlagged) { this.identityFlagged = identityFlagged; }
    public void setActive(boolean active) { this.active = active; }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Carrier c = new Carrier();
        public Builder name(String v) { c.name = v; return this; }
        public Builder licenseNumber(String v) { c.licenseNumber = v; return this; }
        public Builder trustScore(int v) { c.trustScore = v; return this; }
        public Builder trustLevel(String v) { c.trustLevel = v; return this; }
        public Builder documentVerified(boolean v) { c.documentVerified = v; return this; }
        public Builder disputeCount(int v) { c.disputeCount = v; return this; }
        public Builder identityFlagged(boolean v) { c.identityFlagged = v; return this; }
        public Builder active(boolean v) { c.active = v; return this; }
        public Carrier build() { return c; }
    }
}
