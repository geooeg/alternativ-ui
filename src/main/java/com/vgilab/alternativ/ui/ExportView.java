package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.ShapefileService;
import com.vgilab.alternativ.business.geo.SubTrajectory;
import com.vgilab.alternativ.export.ReportItem;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
            for (final AlterNativ curAlterNativ : alterNativs) {
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
                System.out.println("test");
            }
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
            final Map<String, String> map = new HashMap<>();
            final List<ReportItem> filteredReportItems = reportItems.stream().distinct().collect(Collectors.toList());
            filteredReportItems.stream().forEach((curReportItem) -> {
                List<SubTrajectory> subTrajectories = this.shapefileService.getCoordinatesFromFeatureCollection(this.projectedCoordinateReferenceSystem, curReportItem.getTripId(), this.importedFeatures);
                StringBuilder stringBuilder = new StringBuilder();
                subTrajectories.stream().forEach((curSubTrajectory) -> {
                    stringBuilder.append(curSubTrajectory.getTravelMode().name()).append(" ,");
                });
                map.put(curReportItem.getTripId(), stringBuilder.toString());
            });
            reportItems.stream().forEach((curReportItem) -> {
                curReportItem.setPrimaryModeActual(map.get(curReportItem.getTripId()));
            });
            
        }
    }
}
