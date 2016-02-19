package com.vgilab.alternativ.business.geo;

import com.google.common.io.Files;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
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
 *
 * @author smuellner
 */
@Service
public class ShapefileService {

    @Autowired
    private FeatureService featureService;

    public File exportToShapefile(List<AlterNativ> alterNativs) {
        final List<SimpleFeature> featuresForChosenRoute = new LinkedList<>();
        final List<SimpleFeature> featuresForTracks = new LinkedList<>();
        for (final AlterNativ curAlterNativ : alterNativs) {
            for (final ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                featuresForChosenRoute.addAll(this.featureService.createFeaturesFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
            }
            featuresForTracks.addAll(this.featureService.createFeaturesFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
        }
        final File shapeDir = Files.createTempDir();
        try {
            final File shapeFile = new File(shapeDir, "chosenroute.shp");
            final ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            final Map<String, Serializable> params = new HashMap<>();
            params.put("url", shapeFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            final SimpleFeatureType typeForChosenRoute = this.featureService.getTypeForChosenRoute();
            final ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            shapefileDataStore.createSchema(typeForChosenRoute);
            final Transaction transaction = new DefaultTransaction("create");
            final String typeName = shapefileDataStore.getTypeNames()[0];
            final SimpleFeatureSource featureSource = shapefileDataStore.getFeatureSource(typeName);
            if (featureSource instanceof SimpleFeatureStore) {
                final SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                final SimpleFeatureCollection collection = new ListFeatureCollection(typeForChosenRoute, featuresForChosenRoute);
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
        try {
            final File shapeFile = new File(shapeDir, "tracks.shp");
            final ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            final Map<String, Serializable> params = new HashMap<>();
            params.put("url", shapeFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            final SimpleFeatureType typeForTracks = this.featureService.getTypeForTracks();
            final ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            shapefileDataStore.createSchema(typeForTracks);
            final Transaction transaction = new DefaultTransaction("create");
            final String typeName = shapefileDataStore.getTypeNames()[0];
            final SimpleFeatureSource featureSource = shapefileDataStore.getFeatureSource(typeName);
            if (featureSource instanceof SimpleFeatureStore) {
                final SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                final SimpleFeatureCollection collection = new ListFeatureCollection(typeForTracks, featuresForTracks);
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
}
