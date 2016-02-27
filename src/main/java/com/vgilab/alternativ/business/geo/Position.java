package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.Route;
import com.vgilab.alternativ.generated.Step;
import com.vgilab.alternativ.generated.Track;

/**
 *
 * @author smuellner
 */
public class Position {
    private Track track;
    private Route route;
    private Step step;
    private Coordinate3D coordinateForTrack;
    private Coordinate3D coordinateForStep;
    private Double distance;

    /**
     * @return the track
     */
    public Track getTrack() {
        return track;
    }

    /**
     * @param track the track to set
     */
    public void setTrack(Track track) {
        this.track = track;
    }

    /**
     * @return the route
     */
    public Route getRoute() {
        return route;
    }

    /**
     * @param route the route to set
     */
    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * @return the step
     */
    public Step getStep() {
        return step;
    }

    /**
     * @param step the step to set
     */
    public void setStep(Step step) {
        this.step = step;
    }

    /**
     * @return the coordinateForTrack
     */
    public Coordinate3D getCoordinateForTrack() {
        return coordinateForTrack;
    }

    /**
     * @param coordinateForTrack the coordinateForTrack to set
     */
    public void setCoordinateForTrack(Coordinate3D coordinateForTrack) {
        this.coordinateForTrack = coordinateForTrack;
    }

    /**
     * @return the coordinateForStep
     */
    public Coordinate3D getCoordinateForStep() {
        return coordinateForStep;
    }

    /**
     * @param coordinateForStep the coordinateForStep to set
     */
    public void setCoordinateForStep(Coordinate3D coordinateForStep) {
        this.coordinateForStep = coordinateForStep;
    }

    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

}
                            
