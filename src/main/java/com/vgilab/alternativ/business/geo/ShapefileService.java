package com.vgilab.alternativ.business.geo;

import com.google.common.io.Files;
import com.vgilab.alternativ.business.busstop.BusStop;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Feature;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
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
    private final static String REFERENCE_ID = "Tripid";
    private final static String TRAVEL_MODE_ID = "Type";
    public final static String DEFAULT_CRS = "EPSG:2039";
    

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
            alterNativs.stream().map((AlterNativ curAlterNativ) -> {
                curAlterNativ.getChosenRoute().stream().map((ChosenRoute curChosenRoute) -> {
                    pointsForChosenRoute.addAll(this.featureService.createPointsFromChosenRoute(curChosenRoute, curAlterNativ.getId(), curAlterNativ.getUserId(), curAlterNativ.getChosenType(), curAlterNativ.getCreatedAt()));
                    return curChosenRoute;
                }).forEach((curChosenRoute) -> {
                    linesForChosenRoute.add(this.featureService.createLineFromChosenRoute(curChosenRoute, curAlterNativ.getId(), curAlterNativ.getUserId(), curAlterNativ.getChosenType()));
                });
                return curAlterNativ;
            }).map((AlterNativ curAlterNativ) -> {
                pointsForTracks.addAll(this.featureService.createPointsFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
                return curAlterNativ;
            }).map((curAlterNativ) -> {
                linesForTracks.add(this.featureService.createLineFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId()));
                return curAlterNativ;
            }).filter((curAlterNativ) -> (snapToRoad)).map((curAlterNativ) -> AlterNativUtil.getCoordinatesFromTrack(curAlterNativ)).filter((coordinates) -> (coordinates.size() > 2)).forEach((coordinates) -> {
                try {
                    final List<Coordinate3D> snapedToRoad = GoogleMapsRoadsApi.snapToRoadsUsingBatches(coordinates, true);
                    pointsForSnapedToRoad.addAll(this.featureService.createPointsForCoordinates(snapedToRoad));
                    linesForSnapedToRoad.addAll(this.featureService.createLinesForCoordinates(snapedToRoad));
                } catch (final SecurityException ex) {
                    LOGGER.severe(ex.getLocalizedMessage());
                }
            });
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
        alterNativ.getChosenRoute().stream().map((curChosenRoute) -> {
            pointsForChosenRoute.addAll(this.featureService.createPointsFromChosenRoute(curChosenRoute, alterNativ.getId(), alterNativ.getUserId(), alterNativ.getChosenType(), alterNativ.getCreatedAt()));
            return curChosenRoute;
        }).forEach((curChosenRoute) -> {
            linesForChosenRoute.add(this.featureService.createLineFromChosenRoute(curChosenRoute, alterNativ.getId(), alterNativ.getUserId(), alterNativ.getChosenType()));
        });
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
                Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, "{0} does not support read/write access", typeName);
            }
        } catch (IOException ex) {
            Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> importFeaturesFromArchive(final byte[] contents) throws IOException, FactoryException {
        if (null != contents) {
            final String destinationPath = System.getProperty("java.io.tmpdir");
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new ByteArrayInputStream(contents));
                ZipEntry entry;
                final Map<String, URL> map = new HashMap<>();
                while ((entry = zis.getNextEntry()) != null) {
                    if (isValid(entry)) {
                        final File entryFile = new File(destinationPath, entry.getName());
                        entryFile.createNewFile();
                        entryFile.deleteOnExit();
                        if (entryFile.exists()) {
                            final String ext = Files.getFileExtension(entry.getName());
                            if (StringUtils.equalsIgnoreCase("shp", ext)) {
                                map.put("url", entryFile.toURI().toURL());
                            }
                            // and rewrite data from stream
                            OutputStream os = null;
                            try {
                                os = new FileOutputStream(entryFile);
                                IOUtils.copy(zis, os);
                            } finally {
                                IOUtils.closeQuietly(os);
                            }
                        }
                    }
                }
                final DataStore dataStore = DataStoreFinder.getDataStore(map);
                return dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures();
            } finally {
                IOUtils.closeQuietly(zis);
            }
        }
        return null;
    }

    public CoordinateReferenceSystem importCRSFromArchive(final String crs, final byte[] contents) throws IOException, FactoryException {
        if (null != contents) {
            final String destinationPath = System.getProperty("java.io.tmpdir");
            final List<File> files = new LinkedList<>();
            ZipInputStream zis = null;
            CoordinateReferenceSystem srcCrs = StringUtils.isNotEmpty(crs) ? CRS.decode(crs) : null;
            try {
                zis = new ZipInputStream(new ByteArrayInputStream(contents));
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    final String ext = Files.getFileExtension(entry.getName());
                    if (isValid(entry) && (null != srcCrs && StringUtils.equalsIgnoreCase("prj", ext))) {
                        final File entryFile = new File(destinationPath, entry.getName());
                        entryFile.createNewFile();
                        entryFile.deleteOnExit();
                        if (entryFile.exists()) {
                            files.add(entryFile);
                            // and rewrite data from stream
                            OutputStream os = null;
                            try {
                                os = new FileOutputStream(entryFile);
                                IOUtils.copy(zis, os);
                                try {
                                    final String wkt = IOUtils.toString(entryFile.toURI());
                                    if (StringUtils.isNotEmpty(wkt)) {
                                        srcCrs = CRS.parseWKT(wkt);
                                    }
                                } catch (FactoryException ex) {
                                    Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
                                    srcCrs = CRS.decode(DEFAULT_CRS);
                                }
                                return srcCrs;
                            } finally {
                                IOUtils.closeQuietly(os);
                            }
                        }
                        // and rewrite data from stream
                        OutputStream os = null;
                        try {
                            os = new FileOutputStream(entryFile);
                            IOUtils.copy(zis, os);
                        } finally {
                            IOUtils.closeQuietly(os);
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly(zis);
                files.stream().forEach((file) -> {
                    file.delete();
                });
            }
        }
        return null;
    }

    private boolean isValid(final ZipEntry entry) {
        return !entry.isDirectory()
                && StringUtils.isNotEmpty(entry.getName())
                && !entry.getName().startsWith(".")
                && !entry.getName().startsWith("__MACOSX");
    }

    public List<SubTrajectory> getCoordinatesFromFeatureCollection(final CoordinateReferenceSystem crs, final String referenceId, final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) {
        final List<SubTrajectory> subTrajectories = new LinkedList<>();
        if (null != featureCollection) {
            try (FeatureIterator iterator = featureCollection.features()) {
                while (iterator.hasNext()) {
                    final SimpleFeature feature = (SimpleFeature) iterator.next();
                    final String attribute = null != feature.getAttribute(REFERENCE_ID) ? feature.getAttribute(REFERENCE_ID).toString() : null;
                    final String travelModeId = null != feature.getAttribute(TRAVEL_MODE_ID) ? feature.getAttribute(TRAVEL_MODE_ID).toString() : null;
                    if (StringUtils.containsIgnoreCase(referenceId, attribute) || (null == referenceId)) {
                        final Geometry defaultGeometry = (Geometry) feature.getDefaultGeometry();
                        try {
                            final Geometry transformedGeometry = null != crs ? transformToGeo(crs, defaultGeometry, true) : defaultGeometry;
                            final List<Coordinate> coordinates = new LinkedList<>();
                            coordinates.addAll(Arrays.asList(transformedGeometry.getCoordinates()));
                            final List<Coordinate3D> coordinates3D = new LinkedList<>();
                            coordinates.stream().forEach((curCoordinate) -> {
                                coordinates3D.add(new Coordinate3D(curCoordinate.x, curCoordinate.y, curCoordinate.z));
                            });
                            subTrajectories.add(new SubTrajectory(feature.getID(), TravelMode.fromInt(Integer.valueOf(travelModeId)), coordinates3D));
                        } catch (FactoryException | TransformException ex) {
                            Logger.getLogger(ShapefileService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        return subTrajectories;
    }

    private static Geometry transformToGeo(CoordinateReferenceSystem srcCRS, Geometry source, boolean lenient) throws FactoryException, TransformException {
        final CoordinateReferenceSystem destCRS = DefaultGeographicCRS.WGS84;
        final MathTransform transform = CRS.findMathTransform(srcCRS, destCRS, lenient);
        return JTS.transform(source, transform);
    }
}
