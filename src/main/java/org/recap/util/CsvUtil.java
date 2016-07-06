package org.recap.util;

import com.csvreader.CsvWriter;
import org.recap.model.etl.LoadReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
@Component
public class CsvUtil {

    @Value("${etl.report.directory}")
    private String reportDirectoryPath;

    Logger logger = LoggerFactory.getLogger(CsvUtil.class);

    public void writeToCsv(List<LoadReportEntity> loadReportEntities) {
        if (!CollectionUtils.isEmpty(loadReportEntities)) {
            LoadReportEntity loadReport = loadReportEntities.get(0);
            String fileName = loadReport.getOwningInstitution() != null ? loadReport.getOwningInstitution() : "Failure_Report";
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            File file = new File(reportDirectoryPath + File.separator + fileName + "_" + df.format(new Date()) + ".csv");
            try {
                boolean fileExists = file.exists();
                if (!fileExists) {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdir();
                    }
                    file.createNewFile();
                }

                // Use FileWriter constructor that specifies open for appending
                CsvWriter csvOutput = new CsvWriter(new FileWriter(file, true), ',');

                //Create Header for CSV
                if (!fileExists) {
                    csvOutput.write("Owning Institution");
                    csvOutput.write("Owning Institution Bib ID");
                    csvOutput.write("Owning Institution Holdings ID");
                    csvOutput.write("Local Item ID");
                    csvOutput.write("Item Barcode");
                    csvOutput.write("Customer Code");
                    csvOutput.write("Title");
                    csvOutput.write("Collection Group Designation");
                    csvOutput.write("Create Date Item");
                    csvOutput.write("Last Updated Date Item");
                    csvOutput.write("Exception Message");
                    csvOutput.write("Error Description");
                    csvOutput.endRecord();
                }
                for (LoadReportEntity loadReportEntity : loadReportEntities) {
                    csvOutput.write(loadReportEntity.getOwningInstitution());
                    csvOutput.write(loadReportEntity.getOwningInstitutionBibId());
                    csvOutput.write(loadReportEntity.getOwningInstitutionHoldingsId());
                    csvOutput.write(loadReportEntity.getLocalItemId());
                    csvOutput.write(loadReportEntity.getItemBarcode());
                    csvOutput.write(loadReportEntity.getCustomerCode());
                    csvOutput.write(loadReportEntity.getTitle());
                    csvOutput.write(loadReportEntity.getCollectionGroupDesignation());
                    String createDateItem = loadReportEntity.getCreateDateItem() != null ? loadReportEntity.getCreateDateItem().toString() : null;
                    csvOutput.write(createDateItem);
                    String lastUpdatedDateItem = loadReportEntity.getLastUpdatedDateItem() != null ? loadReportEntity.getLastUpdatedDateItem().toString() : null;
                    csvOutput.write(lastUpdatedDateItem);
                    csvOutput.write(loadReportEntity.getExceptionMessage());
                    csvOutput.write(loadReportEntity.getErrorDescription());
                    csvOutput.endRecord();
                }
                csvOutput.flush();
                csvOutput.close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }
    }
}
