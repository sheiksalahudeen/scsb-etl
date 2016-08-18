package org.recap.report;

import org.recap.model.jpa.ReportEntity;

import java.util.List;

/**
 * Created by peris on 8/17/16.
 */
public interface ReportGeneratorInterface {

    boolean isInterested(String reportType);

    boolean isTransmitted(String transmissionType);

    String generateReport(List<ReportEntity> reportEntities);
}
