package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import java.util.List;

/**
 *
 * @author smuellner
 */
public class AnalysedTrip {
    private AlterNativ alterNativ;
    private List<Position> positions;
    private List<Coordinate3D> snapedToRoad;
    private Double deviationArea;
    private Integer deviationsFromTrip;

    public AnalysedTrip(final AlterNativ curAlterNativ, final List<Position> positions) {
        this.alterNativ = curAlterNativ;
        this.positions = positions;
    }

    /**
     * @return the alterNativ
     */
    public AlterNativ getAlterNativ() {
        return alterNativ;
    }

    /**
     * @param alterNativ the alterNativ to set
     */
    public void setAlterNativ(AlterNativ alterNativ) {
        this.alterNativ = alterNativ;
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
     * @return the deviationArea
     */
    public Double getDeviationArea() {
        return deviationArea;
    }

    /**
     * @param deviationArea the deviationArea to set
     */
    public void setDeviationArea(Double deviationArea) {
        this.deviationArea = deviationArea;
    }

    /**
     * @return the deviationsFromTrip
     */
    public Integer getDeviationsFromTrip() {
        return deviationsFromTrip;
    }

    /**
     * @param deviationsFromTrip the deviationsFromTrip to set
     */
    public void setDeviationsFromTrip(Integer deviationsFromTrip) {
        this.deviationsFromTrip = deviationsFromTrip;
    }
}
