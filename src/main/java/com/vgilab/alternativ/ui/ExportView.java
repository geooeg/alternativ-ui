package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.Coordinate3DUtil;
import com.vgilab.alternativ.business.geo.DistanceCalculation;
import com.vgilab.alternativ.business.geo.ShapefileService;
import com.vgilab.alternativ.business.geo.SubTrajectory;
import com.vgilab.alternativ.business.geo.TravelMode;
import com.vgilab.alternativ.export.ReportItem;
import com.vgilab.alternativ.export.ReportItemTrajectory;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Zhang <3
 */
@Component
@ManagedBean(name = "exportView")
@SessionScoped
public class ExportView {

    private final String coordinateReferenceSystem = "EPSG:2039";
    private FeatureCollection<SimpleFeatureType, SimpleFeature> importedFeatures;
    private CoordinateReferenceSystem projectedCoordinateReferenceSystem;

    @Autowired
    private ShapefileService shapefileService;

    private final List<ReportItem> reportItems = new LinkedList<>();

    public void setAlterNativs(final List<AlterNativ> alterNativs) {
        if (!CollectionUtils.isEmpty(alterNativs)) {
            alterNativs.stream().forEach((curAlterNativ) -> {
                final ReportItem report = new ReportItem();
                report.setTripId(curAlterNativ.getId());
                report.setUserId(curAlterNativ.getUserId());
                final DateTime createdAt = new DateTime(curAlterNativ.getCreatedAt());
                final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyyHH:mm:ss");
                report.setTripStartTime(dateTimeFormatter.print(createdAt));
                report.setPrimaryModeChoosen(curAlterNativ.getChosenType());
                report.setTripStartLocation(curAlterNativ.getOrigin().getAddress());
                report.setTripEndLocation(curAlterNativ.getDestination().getAddress());
                // Chosen Routes
                for (final ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                }
                this.getReportItems().add(report);
            });
        }
    }

    /**
     * @return the reportItems
     */
    public List<ReportItem> getReportItems() {
        return reportItems;
    }

    public void handleFileUpload(FileUploadEvent event) {
        event.getComponent().setTransient(false);
        if (event.getFile() != null && event.getFile().getSize() > 0) {
            final byte[] contents = event.getFile().getContents();
            try {
                this.importedFeatures = this.shapefileService.importFeaturesFromArchive(contents);
                final FacesMessage message = new FacesMessage("Successful", "Imported features from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (FactoryException | IOException ex) {
                Logger.getLogger(PositionDetailView.class.getName()).log(Level.SEVERE, null, ex);
                final FacesMessage message = new FacesMessage("Error", "Could not read features from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
            try {
                this.projectedCoordinateReferenceSystem = this.shapefileService.importCRSFromArchive(this.coordinateReferenceSystem, contents);
                final FacesMessage message = new FacesMessage("Successful", "Imported CRS from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (FactoryException | IOException ex) {
                Logger.getLogger(PositionDetailView.class.getName()).log(Level.SEVERE, null, ex);
                final FacesMessage message = new FacesMessage("Error", "Could not read CRS from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
            final DistanceCalculation distanceCalculation = new DistanceCalculation();
            this.reportItems.stream().forEach((curReportItem) -> {
                final List<SubTrajectory> subTrajectories = this.shapefileService.getCoordinatesFromFeatureCollection(this.projectedCoordinateReferenceSystem, curReportItem.getTripId(), this.importedFeatures);
                final List<TravelMode> travelModes = new LinkedList<>();
                subTrajectories.stream().forEach((curSubTrajectory) -> {
                    travelModes.add(curSubTrajectory.getTravelMode());
                    final List<Coordinate> coordinates = Coordinate3DUtil.convert(curSubTrajectory.getCoordinates());
                    final ReportItemTrajectory reportItemTrajectory = new ReportItemTrajectory();
                    reportItemTrajectory.setTravelMode(curSubTrajectory.getTravelMode());
                    try {
                        final Double distance = distanceCalculation.calculate(coordinates, projectedCoordinateReferenceSystem);
                        reportItemTrajectory.setDistance(distance);
                    } catch (TransformException ex) {
                        Logger.getLogger(ExportView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    curReportItem.getTrajectories().add(reportItemTrajectory);
                });
                final StringJoiner combinedTravelModes = new StringJoiner(",");
                travelModes.stream().distinct().forEachOrdered(curTravelMode -> {
                    combinedTravelModes.add(curTravelMode.name());
                });
                curReportItem.setPrimaryModeActual(combinedTravelModes.toString());
                curReportItem.setDistance(curReportItem.getTrajectories().stream().mapToDouble(t -> t.getDistance()).sum());
            });
            /*
            final Map<String, String> map = new HashMap<>();
            final List<ReportItem> filteredReportItems = reportItems.stream().distinct().collect(Collectors.toList());
            filteredReportItems.stream().forEach((ReportItem curReportItem) -> {
                final List<SubTrajectory> subTrajectories = this.shapefileService.getCoordinatesFromFeatureCollection(this.projectedCoordinateReferenceSystem, curReportItem.getTripId(), this.importedFeatures);
                final List travelModes = new LinkedList<>();
                subTrajectories.stream().forEach((curSubTrajectory) -> {
                    travelModes.add(curSubTrajectory.getTravelMode());
                });
                map.put(curReportItem.getTripId(), combinedTravelModes.toString());
            });
            reportItems.stream().forEach((curReportItem) -> {
                curReportItem.setPrimaryModeActual(map.get(curReportItem.getTripId()));
            });
                // full lambda 
                // final String combinedTravelModes = (String) travelModes.stream().distinct().map(t -> t.toString()).collect(Collectors.joining(", "));
                // mixed lambda
                final StringJoiner combinedTravelModes = new StringJoiner(",");
                travelModes.stream().distinct().forEachOrdered(t -> {
                    combinedTravelModes.add(t.toString());
                });
             */
        }
    }
}
