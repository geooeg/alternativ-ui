package com.vgilab.alternativ.business.geo;

import com.google.common.io.Files;
import com.vgilab.alternativ.business.busstop.BusStop;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Feature;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * http://mapstarter.com http://www.mapshaper.org
 *
 * @author smuellner
 */
@Service
public class ShapefileService {

    private final static Logger LOGGER = Logger.getGlobal();

    @Autowired
    private FeatureService featureService;

    public File exportToShapefile(final List<AlterNativ> alterNativs, final List<BusStop> busStops, final List<Feature> telofuns, final boolean snapToRoad) {
        final File shapeDir = Files.createTempDir();
        if (null != alterNativs) {
            final List<SimpleFeature> pointsForTracks = new LinkedList<>();
            final List<SimpleFeature> linesForTracks = new LinkedList<>();
            final List<SimpleFeature> pointsForSnapedToRoad = new LinkedList<>();
            final List<SimpleFeature> linesForSnapedToRoad = new LinkedList<>();
            final List<SimpleFeature> pointsForChosenRoute = new LinkedList<>();
            final List<SimpleFeature> linesForChosenRoute = new LinkedList<>();
            for (final AlterNativ curAlterNativ : alterNativs) {
                for (final ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                    pointsForChosenRoute.addAll(this.featureService.createPointsFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
                    linesForChosenRoute.add(this.featureService.createLineFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
                }
                pointsForTracks.addAll(this.featureService.createPointsFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
                linesForTracks.add(this.featureService.createLineFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
                if (snapToRoad) {
                    final List<Coordinate3D> coordinates = AlterNativUtil.getCoordinatesFromTrack(curAlterNativ);
                    if (coordinates.size() > 2) {
                        try {
                            final List<Coordinate3D> snapedToRoad = GoogleMapsRoadsApi.snapToRoadsUsingBatches(coordinates, true);
                            pointsForSnapedToRoad.addAll(this.featureService.createPointsForCoordinates(snapedToRoad));
                            linesForSnapedToRoad.addAll(this.featureService.createLinesForCoordinates(snapedToRoad));
                        } catch (final SecurityException ex) {
                            LOGGER.severe(ex.getLocalizedMessage());
                        }
                    }
                }
            }
            this.addChoosenRouteAsPoints(shapeDir, pointsForChosenRoute);
            this.addChoosenRouteAsLine(shapeDir, linesForChosenRoute);
            this.addSnappedToRoadAsPoints(shapeDir, pointsForSnapedToRoad);
            this.addSnappedToRoadAsLines(shapeDir, linesForSnapedToRoad);
            this.addTracksAsPoints(shapeDir, pointsForTracks);
            this.addTracksAsLine(shapeDir, linesForTracks);
        }
        if (null != busStops) {
            final List<SimpleFeature> pointsForBusStops = this.featureService.createPointsFromBusStops(busStops);
            this.addBusStopsAsPoints(shapeDir, pointsForBusStops);
        }
        if (null != telofuns) {
            final List<SimpleFeature> pointsForTelofuns = this.featureService.createPointsFromTelofuns(telofuns);
            this.addTelofunsAsPoints(shapeDir, pointsForTelofuns);
        }
        try {
            shapeDir.deleteOnExit();
            final File zipFile = new File(shapeDir + File.pathSeparator + "alternativ-shp.zip");
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (File file : shapeDir.listFiles()) {
                    file.deleteOnExit();
                    final ZipEntry entry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(entry);
                    final FileInputStream in = new FileInputStream(file);
                    IOUtils.copy(in, zipOutputStream);
                    IOUtils.closeQuietly(in);
                }
                zipOutputStream.flush();
            }
            return zipFile;
        } catch (IOException ex) {
            Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public File exportToShapefile(final AlterNativ alterNativ, final List<Coordinate3D> snapedToRoad) {
        final File shapeDir = Files.createTempDir();
        final List<SimpleFeature> pointsForTracks = new LinkedList<>();
        final List<SimpleFeature> linesForTracks = new LinkedList<>();
        final List<SimpleFeature> pointsForChosenRoute = new LinkedList<>();
        final List<SimpleFeature> linesForChosenRoute = new LinkedList<>();
        for (final ChosenRoute curChosenRoute : alterNativ.getChosenRoute()) {
            pointsForChosenRoute.addAll(this.featureService.createPointsFromChosenRoute(curChosenRoute, alterNativ.getId()));
            linesForChosenRoute.add(this.featureService.createLineFromChosenRoute(curChosenRoute, alterNativ.getId()));
        }
        pointsForTracks.addAll(this.featureService.createPointsFromTracks(alterNativ.getTracks(), alterNativ.getId()));
        linesForTracks.add(this.featureService.createLineFromTracks(alterNativ.getTracks(), alterNativ.getId()));
        this.addChoosenRouteAsPoints(shapeDir, pointsForChosenRoute);
        this.addChoosenRouteAsLine(shapeDir, linesForChosenRoute);
        this.addTracksAsPoints(shapeDir, pointsForTracks);
        this.addTracksAsLine(shapeDir, linesForTracks);
        if (null != snapedToRoad) {
            final List<SimpleFeature> pointsForSnapedToRoad = this.featureService.createPointsForCoordinates(snapedToRoad);
            this.addSnappedToRoadAsPoints(shapeDir, pointsForSnapedToRoad);
            final List<SimpleFeature> linesForSnapedToRoad = this.featureService.createLinesForCoordinates(snapedToRoad);
            this.addSnappedToRoadAsLines(shapeDir, linesForSnapedToRoad);
        }
        try {
            shapeDir.deleteOnExit();
            final File zipFile = new File(shapeDir + File.pathSeparator + alterNativ.getId() + "-shp.zip");
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (File file : shapeDir.listFiles()) {
                    file.deleteOnExit();
                    final ZipEntry entry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(entry);
                    final FileInputStream in = new FileInputStream(file);
                    IOUtils.copy(in, zipOutputStream);
                    IOUtils.closeQuietly(in);
                }
                zipOutputStream.flush();
            }
            return zipFile;
        } catch (IOException ex) {
            Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void addChoosenRouteAsPoints(File shapeDir, List<SimpleFeature> featuresForChosenRoute) {
        if (!CollectionUtils.isEmpty(featuresForChosenRoute)) {
            final File shapeFile = new File(shapeDir, "chosenroute-points.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForChosenRoute(), featuresForChosenRoute);
        }
    }

    private void addChoosenRouteAsLine(File shapeDir, List<SimpleFeature> featuresForChosenRoute) {
        if (!CollectionUtils.isEmpty(featuresForChosenRoute)) {
            final File shapeFile = new File(shapeDir, "chosenroute-lines.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getLineTypeForChosenRoute(), featuresForChosenRoute);
        }
    }

    private void addTracksAsPoints(File shapeDir, List<SimpleFeature> featuresForTracks) {
        if (!CollectionUtils.isEmpty(featuresForTracks)) {
            final File shapeFile = new File(shapeDir, "tracks-points.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForTracks(), featuresForTracks);
        }
    }

    private void addTracksAsLine(File shapeDir, List<SimpleFeature> featuresForTracks) {
        if (!CollectionUtils.isEmpty(featuresForTracks)) {
            final File shapeFile = new File(shapeDir, "tracks-lines.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getLineTypeForTracks(), featuresForTracks);
        }
    }

    private void addBusStopsAsPoints(File shapeDir, List<SimpleFeature> pointsForBusStops) {
        if (!CollectionUtils.isEmpty(pointsForBusStops)) {
            final File shapeFile = new File(shapeDir, "busstops-points.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForBusStops(), pointsForBusStops);
        }
    }

    private void addTelofunsAsPoints(File shapeDir, List<SimpleFeature> pointsForTelofuns) {
        if (!CollectionUtils.isEmpty(pointsForTelofuns)) {
            final File shapeFile = new File(shapeDir, "telofuns-points.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForTelofuns(), pointsForTelofuns);
        }
    }

    private void addSnappedToRoadAsPoints(File shapeDir, List<SimpleFeature> features) {
        if (!CollectionUtils.isEmpty(features)) {
            final File shapeFile = new File(shapeDir, "snapped-track-points.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForCoordinates(), features);
        }
    }

    private void addSnappedToRoadAsLines(File shapeDir, List<SimpleFeature> features) {
        if (!CollectionUtils.isEmpty(features)) {
            final File shapeFile = new File(shapeDir, "snapped-track-lines.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getLineTypeForCoordinates(), features);
        }
    }

    public void writeToShapeFile(File shapeFile, SimpleFeatureType simpleFeatureType, List<SimpleFeature> features) {
        try {
            final ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            final Map<String, Serializable> params = new HashMap<>();
            params.put("url", shapeFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            final ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            shapefileDataStore.createSchema(simpleFeatureType);
            final Transaction transaction = new DefaultTransaction("create");
            final String typeName = shapefileDataStore.getTypeNames()[0];
            final SimpleFeatureSource featureSource = shapefileDataStore.getFeatureSource(typeName);
            if (featureSource instanceof SimpleFeatureStore) {
                final SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                final SimpleFeatureCollection collection = new ListFeatureCollection(simpleFeatureType, features);
                featureStore.setTransaction(transaction);
                try {
                    featureStore.addFeatures(collection);
                    transaction.commit();
                } catch (Exception ex) {
                    Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
                    transaction.rollback();
                } finally {
                    transaction.close();
                }
            } else {
                System.out.println(typeName + " does not support read/write access");
            }
        } catch (IOException ex) {
            Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
