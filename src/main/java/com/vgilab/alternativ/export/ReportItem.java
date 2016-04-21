package com.vgilab.alternativ.export;

import java.util.Objects;

/**
 *
 * @author Zhang
 */
public class ReportItem {
    private String tripId;
    private String userId;
    private String tripStartTime;
    private String tripStartLocation;
    private String tripEndTime;
    private String tripEndLocation;
    private String primaryModeChoosen;
    private String primaryModeActual;
    private String currentPreferency;
    private String deviationFromChoosenRoute;
    private String distance;

    /**
     * @return the tripId
     */
    public String getTripId() {
        return tripId;
    }

    /**
     * @param tripId the tripId to set
     */
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the tripStartTime
     */
    public String getTripStartTime() {
        return tripStartTime;
    }

    /**
     * @param tripStartTime the tripStartTime to set
     */
    public void setTripStartTime(String tripStartTime) {
        this.tripStartTime = tripStartTime;
    }

    /**
     * @return the tripStartLocation
     */
    public String getTripStartLocation() {
        return tripStartLocation;
    }

    /**
     * @param tripStartLocation the tripStartLocation to set
     */
    public void setTripStartLocation(String tripStartLocation) {
        this.tripStartLocation = tripStartLocation;
    }

    /**
     * @return the tripEndTime
     */
    public String getTripEndTime() {
        return tripEndTime;
    }

    /**
     * @param tripEndTime the tripEndTime to set
     */
    public void setTripEndTime(String tripEndTime) {
        this.tripEndTime = tripEndTime;
    }

    /**
     * @return the tripEndLocation
     */
    public String getTripEndLocation() {
        return tripEndLocation;
    }

    /**
     * @param tripEndLocation the tripEndLocation to set
     */
    public void setTripEndLocation(String tripEndLocation) {
        this.tripEndLocation = tripEndLocation;
    }

    /**
     * @return the primaryModeChoosen
     */
    public String getPrimaryModeChoosen() {
        return primaryModeChoosen;
    }

    /**
     * @param primaryModeChoosen the primaryModeChoosen to set
     */
    public void setPrimaryModeChoosen(String primaryModeChoosen) {
        this.primaryModeChoosen = primaryModeChoosen;
    }

    /**
     * @return the primaryModeActual
     */
    public String getPrimaryModeActual() {
        return primaryModeActual;
    }

    /**
     * @param primaryModeActual the primaryModeActual to set
     */
    public void setPrimaryModeActual(String primaryModeActual) {
        this.primaryModeActual = primaryModeActual;
    }

    /**
     * @return the currentPreferency
     */
    public String getCurrentPreferency() {
        return currentPreferency;
    }

    /**
     * @param currentPreferency the currentPreferency to set
     */
    public void setCurrentPreferency(String currentPreferency) {
        this.currentPreferency = currentPreferency;
    }

    /**
     * @return the deviationFromChoosenRoute
     */
    public String getDeviationFromChoosenRoute() {
        return deviationFromChoosenRoute;
    }

    /**
     * @param deviationFromChoosenRoute the deviationFromChoosenRoute to set
     */
    public void setDeviationFromChoosenRoute(String deviationFromChoosenRoute) {
        this.deviationFromChoosenRoute = deviationFromChoosenRoute;
    }

    /**
     * @return the distance
     */
    public String getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(String distance) {
        this.distance = distance;
    }
    
    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.tripId);
        return hash;
    }
    
}
