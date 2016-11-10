package org.recap.camel.datadump;

import org.recap.model.export.DataDumpRequest;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by peris on 11/5/16.
 */

@Component
public class DataExportHeaderUtil {
    public String getValueFor(String batchHeaderString, String key) {
        StringTokenizer stringTokenizer = new StringTokenizer(batchHeaderString, ";");
        while(stringTokenizer.hasMoreTokens()){
            StringTokenizer stringTokenizer1 = new StringTokenizer(stringTokenizer.nextToken(), "#");
            if(stringTokenizer1.nextToken().equals(key)){
                return stringTokenizer1.nextToken();
            }
        }
        return null;
    }

    public String getBatchHeaderString(Integer totalPageCount, Integer currentPageCount, String folderName, String fileName, DataDumpRequest dataDumpRequest) {
        StringBuilder headerString = new StringBuilder();
        headerString.append("totalPageCount")
                .append("#")
                .append(totalPageCount)
                .append(";")
                .append("currentPageCount")
                .append("#")
                .append(currentPageCount)
                .append(";")
                .append("folderName")
                .append("#")
                .append(folderName)
                .append(";")
                .append("fileName")
                .append("#")
                .append(fileName)
                .append(";")
                .append("institutionCodes")
                .append("#")
                .append(getInstitutionCodes(dataDumpRequest))
                .append(";")
                .append("fileFormat")
                .append("#")
                .append(dataDumpRequest.getOutputFileFormat())
                .append(";")
                .append("transmissionType")
                .append("#")
                .append(dataDumpRequest.getTransmissionType())
                .append(";")
                .append("toEmailId")
                .append("#")
                .append(dataDumpRequest.getToEmailAddress())
                .append(";")
                .append("dateTimeString")
                .append("#")
                .append(dataDumpRequest.getDateTimeString())
                .append(";")
                .append("requestingInstitutionCode")
                .append("#")
                .append(dataDumpRequest.getRequestingInstitutionCode())
                .append(";")
                .append("exportFormat")
                .append("#")
                .append(dataDumpRequest.getOutputFileFormat())
                .append(";")
                .append("requestId")
                .append("#")
                .append(dataDumpRequest.getRequestId());

        return headerString.toString();
    }

    private String getInstitutionCodes(DataDumpRequest dataDumpRequest) {
        List<String> institutionCodes = dataDumpRequest.getInstitutionCodes();
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<String> iterator = institutionCodes.iterator(); iterator.hasNext(); ) {
            String code = iterator.next();
            stringBuilder.append(code);
            if(iterator.hasNext()){
                stringBuilder.append("*");
            }
        }
        return stringBuilder.toString();
    }

}
