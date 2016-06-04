package com.vgilab.alternativ.business.geo;

import com.vividsolutions.jts.geom.LineString;

/**
 *
 * @author smuellner
 */
public class DeviationSegment {

    private final LineString segmentLineX;
    private final LineString segmentLineY;

    public DeviationSegment(final LineString segmentLineX, final LineString segmentLineY) {
        this.segmentLineX = segmentLineX;
        this.segmentLineY = segmentLineY;
    }

    /**
     * @return the segmentLineX
     */
    public LineString getSegmentLineX() {
        return segmentLineX;
    }

    /**
     * @return the segmentLineY
     */
    public LineString getSegmentLineY() {
        return segmentLineY;
    }

}
