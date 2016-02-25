package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Step;
import com.vgilab.alternativ.generated.Track;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author smuellner
 */
public class SpatialAnalysisService {

    @Autowired
    private FeatureService featureService;

    public Map<String, List<Position>> analyseRoutes(List<AlterNativ> alterNativs) {
        final Map<String, List<Position>> trips = new HashMap<>();
        for (final AlterNativ curAlterNativ : alterNativs) {
            final Map<Track, SimpleFeature> trackFeatureMap = this.featureService.createTrackPointMapFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId());
            for (final ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                final Map<Step, List<SimpleFeature>> stepFeatureMap = this.featureService.createStepFeatureMapFromChosenRoute(curChosenRoute, curAlterNativ.getId());
                // curChosenRoute.
            }
        }

        return trips;
    }
}
