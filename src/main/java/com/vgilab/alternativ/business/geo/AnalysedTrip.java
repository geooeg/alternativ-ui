package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import java.util.List;

/**
 *
 * @author smuellner
 */
public class AnalysedTrip {
    private final AlterNativ alterNativ;
    private final Double deviationInMeters;
    private final Integer minimumTracks;
    private List<Coordinate3D> snapedToRoad;
    private boolean snapedToRoadError = false;
    private double deviationArea;
    private List<DeviationSegment> deviationsFromTrip;
    private List<Position> positions;

    public AnalysedTrip(final AlterNativ curAlterNativ, Double deviationInMeters, Integer minimumTracks) {
        this.alterNativ = curAlterNativ;
        this.deviationInMeters = deviationInMeters;
        this.minimumTracks = minimumTracks;
    }

    /**
     * @return the alterNativ
     */
    public AlterNativ getAlterNativ() {
        return alterNativ;
    }


    /**
     * @return the positions
     */
    public List<Position> getPositions() {
        return positions;
    }

    /**
     * @param positions the positions to set
     */
    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    /**
     * @return the snapedToRoad
     */
    public List<Coordinate3D> getSnapedToRoad() {
        return snapedToRoad;
    }

    /**
     * @param snapedToRoad the snapedToRoad to set
     */
    public void setSnapedToRoad(List<Coordinate3D> snapedToRoad) {
        this.snapedToRoad = snapedToRoad;
    }

    /**
     * @return the snapedToRoadEror
     */
    public boolean isSnapedToRoadError() {
        return snapedToRoadError;
    }

    /**
     * @param snapedToRoadEror the snapedToRoadEror to set
     */
    public void setSnapedToRoadError(boolean snapedToRoadError) {
        this.snapedToRoadError = snapedToRoadError;
    }

    /**
     * @return the deviationArea
     */
    public double getDeviationArea() {
        return deviationArea;
    }

    /**
     * @param deviationArea the deviationArea to set
     */
    public void setDeviationArea(double deviationArea) {
        this.deviationArea = deviationArea;
    }

    /**
     * @return the deviationsFromTrip
     */
    public List<DeviationSegment> getDeviationsFromTrip() {
        return deviationsFromTrip;
    }

    /**
     * @param deviationsFromTrip the deviationsFromTrip to set
     */
    public void setDeviationsFromTrip(List<DeviationSegment> deviationsFromTrip) {
        this.deviationsFromTrip = deviationsFromTrip;
    }
}
