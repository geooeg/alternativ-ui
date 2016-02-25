package com.vgilab.alternativ.business.geo;

import com.google.common.io.Files;
import com.vgilab.alternativ.business.busstop.BusStop;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Feature;
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

/**
 * http://mapstarter.com http://www.mapshaper.org
 *
 * @author smuellner
 */
@Service
public class ShapefileService {

    @Autowired
    private FeatureService featureService;

    public File exportToShapefile(List<AlterNativ> alterNativs, List<BusStop> busStops, List<Feature> telofuns) {
        final File shapeDir = Files.createTempDir();
        if (null != alterNativs) {
            final List<SimpleFeature> pointsForTracks = new LinkedList<>();
            final List<SimpleFeature> linesForTracks = new LinkedList<>();
            final List<SimpleFeature> pointsForChosenRoute = new LinkedList<>();
            for (final AlterNativ curAlterNativ : alterNativs) {
                for (final ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                    pointsForChosenRoute.addAll(this.featureService.createFeaturesFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
                }
                pointsForTracks.addAll(this.featureService.createPointsFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
                linesForTracks.add(this.featureService.createLineFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
            }
            this.addChoosenRoute(shapeDir, pointsForChosenRoute);
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

    private void addChoosenRoute(File shapeDir, List<SimpleFeature> featuresForChosenRoute) {
        if (null != featuresForChosenRoute) {
            final File shapeFile = new File(shapeDir, "chosenroute.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForChosenRoute(), featuresForChosenRoute);
        }
    }

    private void addTracksAsPoints(File shapeDir, List<SimpleFeature> featuresForTracks) {
        if (null != featuresForTracks) {
            final File shapeFile = new File(shapeDir, "tracks.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForTracks(), featuresForTracks);
        }
    }

    private void addTracksAsLine(File shapeDir, List<SimpleFeature> featuresForTracks) {
        if (null != featuresForTracks) {
            final File shapeFile = new File(shapeDir, "trackline.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getLineTypeForTracks(), featuresForTracks);
        }
    }

    private void addBusStopsAsPoints(File shapeDir, List<SimpleFeature> pointsForBusStops) {
        if (null != pointsForBusStops) {
            final File shapeFile = new File(shapeDir, "busstops.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForBusStops(), pointsForBusStops);
        }
    }

    private void addTelofunsAsPoints(File shapeDir, List<SimpleFeature> pointsForTelofuns) {
        if (null != pointsForTelofuns) {
            final File shapeFile = new File(shapeDir, "telofuns.shp");
            this.writeToShapeFile(shapeFile, this.featureService.getPointTypeForTelofuns(), pointsForTelofuns);
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
                    ex.printStackTrace();
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
