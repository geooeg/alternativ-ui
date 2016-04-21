package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.export.ReportItem;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import java.util.LinkedList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
}
