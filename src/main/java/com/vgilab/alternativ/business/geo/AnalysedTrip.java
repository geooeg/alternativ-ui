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

    public AnalysedTrip(AlterNativ curAlterNativ, List<Position> positions) {
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
}
