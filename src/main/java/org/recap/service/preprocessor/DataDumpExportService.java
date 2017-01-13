package org.recap.service.preprocessor;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.commons.io.FileUtils;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.service.email.datadump.DataDumpEmailService;
import org.recap.service.executor.datadump.DataDumpExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by premkb on 27/9/16.
 */
@Service
public class DataDumpExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpExportService.class);

    @Autowired
    ApplicationContext appContext;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private DataDumpExecutorService dataDumpExecutorService;

    @Autowired
    DataDumpEmailService dataDumpEmailService;

    @Autowired
    ConsumerTemplate consumerTemplate;

    @Value("${datadump.status.file.name}")
    String dataDumpStatusFileName;

    public String startDataDumpProcess(DataDumpRequest dataDumpRequest) {
        String outputString = null;
        String responseMessage = null;
        try {
            new Thread(() -> {
                try {
                    dataDumpExecutorService.generateDataDump(dataDumpRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP)){
                String message = getMessageFromIsRecordAvailableQ();
                if (message.equals(ReCAPConstants.DATADUMP_RECORDS_AVAILABLE_FOR_PROCESS)) {
                    outputString = getMessageFromHttpQ();
                } else{
                    outputString = message;
                }
            }else{
                outputString = getMessageFromIsRecordAvailableQ();
                if(!outputString.equals(ReCAPConstants.DATADUMP_RECORDS_AVAILABLE_FOR_PROCESS)){
                    dataDumpEmailService.sendEmail(dataDumpRequest.getInstitutionCodes(),
                            Integer.valueOf(0),
                            Integer.valueOf(0),
                            dataDumpRequest.getTransmissionType(),
                            null,
                            dataDumpRequest.getToEmailAddress(),
                            ReCAPConstants.DATADUMP_NO_DATA_AVAILABLE
                    );
                }
            }
            responseMessage = getResponseMessage(outputString, dataDumpRequest);
        } catch (Exception e) {
            logger.error(e.getMessage());
            responseMessage = ReCAPConstants.DATADUMP_EXPORT_FAILURE;
        }
        return responseMessage;
    }

    private String getMessageFromHttpQ(){
        String outputString;
        Exchange receive = consumerTemplate.receive(ReCAPConstants.DATADUMP_HTTP_Q);
        Object body = receive.getIn().getBody();
        while (null == body) {
            receive = consumerTemplate.receive(ReCAPConstants.DATADUMP_HTTP_Q);
            body = receive.getIn().getBody();
        }
        outputString = (String) receive.getIn().getBody();
        return outputString;
    }

    private String getMessageFromIsRecordAvailableQ(){
        String outputString;
        Exchange receive = consumerTemplate.receive(ReCAPConstants.DATADUMP_IS_RECORD_AVAILABLE_Q);
        Object body = receive.getIn().getBody();
        while (null == body) {
            receive = consumerTemplate.receive(ReCAPConstants.DATADUMP_IS_RECORD_AVAILABLE_Q);
            body = receive.getIn().getBody();
        }
        outputString = (String) receive.getIn().getBody();
        return outputString;
    }

    private List<String> splitStringAndGetList(String inputString) {
        String[] splittedString = inputString.split(",");
        List<String> stringList = Arrays.asList(splittedString);
        return stringList;
    }

    private List<Integer> getIntegerListFromStringList(List<String> stringList) {
        List<Integer> integerList = new ArrayList<>();
        for (String stringValue : stringList) {
            integerList.add(Integer.parseInt(stringValue));
        }
        return integerList;
    }

    private List<Integer> splitStringAndGetIntegerList(String inputString) {
        List<Integer> integerList = getIntegerListFromStringList(splitStringAndGetList(inputString));
        return integerList;
    }

    public void setDataDumpRequest(DataDumpRequest dataDumpRequest, String fetchType, String institutionCodes, String date, String collectionGroupIds,
                                   String transmissionType, String requestingInstitutionCode, String toEmailAddress, String outputFormat) {
        if (fetchType != null) {
            dataDumpRequest.setFetchType(fetchType);
        }
        if (institutionCodes != null) {
            List<String> institutionCodeList = splitStringAndGetList(institutionCodes);
            dataDumpRequest.setInstitutionCodes(institutionCodeList);
        }
        if (date != null && !date.equals("")) {
            dataDumpRequest.setDate(date);
        }

        if (collectionGroupIds != null && !collectionGroupIds.equals("")) {
            List<Integer> collectionGroupIdList = splitStringAndGetIntegerList(collectionGroupIds);
            dataDumpRequest.setCollectionGroupIds(collectionGroupIdList);
        } else {
            List<Integer> collectionGroupIdList = new ArrayList<>();
            CollectionGroupEntity collectionGroupEntityShared = collectionGroupDetailsRepository.findByCollectionGroupCode(ReCAPConstants.COLLECTION_GROUP_SHARED);
            collectionGroupIdList.add(collectionGroupEntityShared.getCollectionGroupId());
            CollectionGroupEntity collectionGroupEntityOpen = collectionGroupDetailsRepository.findByCollectionGroupCode(ReCAPConstants.COLLECTION_GROUP_OPEN);
            collectionGroupIdList.add(collectionGroupEntityOpen.getCollectionGroupId());
            dataDumpRequest.setCollectionGroupIds(collectionGroupIdList);
        }
        if (transmissionType != null && !transmissionType.equals("")) {
            dataDumpRequest.setTransmissionType(transmissionType);
        } else {
            dataDumpRequest.setTransmissionType(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP);
        }
        if (requestingInstitutionCode != null) {
            dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        }
        if (!StringUtils.isEmpty(toEmailAddress)) {
            dataDumpRequest.setToEmailAddress(toEmailAddress);
        }

        if (!StringUtils.isEmpty(outputFormat)) {
            dataDumpRequest.setOutputFileFormat(outputFormat);
        }

        dataDumpRequest.setDateTimeString(getDateTimeString());

        dataDumpRequest.setRequestId(new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_YYYYMMDDHHMM).format(new Date()));
    }

    public String validateIncomingRequest(DataDumpRequest dataDumpRequest) {
        String validationMessage = null;
        Map<Integer, String> errorMessageMap = new HashMap<>();
        Integer errorcount = 1;
        if (dataDumpRequest.getInstitutionCodes().size() > 0) {
            for (String institutionCode : dataDumpRequest.getInstitutionCodes()) {
                if (!institutionCode.equals(ReCAPConstants.COLUMBIA) && !institutionCode.equals(ReCAPConstants.PRINCETON)
                        && !institutionCode.equals(ReCAPConstants.NYPL)) {
                    errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_INST_CODES_ERR_MSG);
                    errorcount++;
                }
            }
            if(dataDumpRequest.getInstitutionCodes().size() != 1 && dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL)) {
                errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_MULTIPLE_INST_CODES_ERR_MSG);
                errorcount++;
            }
        }
        if (dataDumpRequest.getRequestingInstitutionCode() != null) {
            if (!dataDumpRequest.getRequestingInstitutionCode().equals(ReCAPConstants.COLUMBIA) && !dataDumpRequest.getRequestingInstitutionCode().equals(ReCAPConstants.PRINCETON)
                    && !dataDumpRequest.getRequestingInstitutionCode().equals(ReCAPConstants.NYPL)) {
                errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_REQ_INST_CODE_ERR_MSG);
                errorcount++;
            }
        }
        if (!dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL) &&
                !dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL)
                && !dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_DELETED)) {
            errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG);
            errorcount++;
        }
        if (!dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                && !dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP)
                ) {
            errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_TRANS_TYPE_ERR_MSG);
            errorcount++;
        }
        if (dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL)) {
            if (dataDumpRequest.getInstitutionCodes() == null) {
                errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_INSTITUTIONCODE_ERR_MSG);
                errorcount++;
            }
        }
        if (dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL)) {
            if (dataDumpRequest.getDate() == null || dataDumpRequest.getDate().equals("")) {
                errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_DATE_ERR_MSG);
                errorcount++;
            }
        }
        if (dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)) {
            if (StringUtils.isEmpty(dataDumpRequest.getToEmailAddress())) {
                errorMessageMap.put(errorcount, ReCAPConstants.DATADUMP_EMAIL_TO_ADDRESS_REQUIRED);
                errorcount++;
            } else {
                boolean isValid = validateEmailAddress(dataDumpRequest.getToEmailAddress());
                if (!isValid) {
                    errorMessageMap.put(errorcount, ReCAPConstants.INVALID_EMAIL_ADDRESS);
                    errorcount++;
                }
            }
        }

        if(dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL) && dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)) {
            String dataExportStatus = getDataExportCurrentStatus();
            if(dataExportStatus != null && dataExportStatus.equals(ReCAPConstants.IN_PROGRESS)){
                errorMessageMap.put(errorcount, ReCAPConstants.FULLDUMP_INPROGRESS_ERR_MSG);
                errorcount++;
            }
        }

        if (errorMessageMap.size() > 0) {
            validationMessage = buildErrorMessage(errorMessageMap);
        }
        return validationMessage;
    }

    private String getDataExportCurrentStatus(){
        File file = new File(dataDumpStatusFileName);
        String dataDumpStatus = null;
        try {
            if (file.exists()) {
                dataDumpStatus = FileUtils.readFileToString(file, Charset.defaultCharset());
            }
        } catch (IOException e) {
            logger.error("Exception while creating or updating the file : " + e.getMessage());
        }
        return dataDumpStatus;
    }


    private void setDataExportCurrentStatus(){
        File file = new File(dataDumpStatusFileName);
        File parentFile = file.getParentFile();
        try {
            if (file.exists()) {
                String dataDumpStatus = FileUtils.readFileToString(file, Charset.defaultCharset());
                if (dataDumpStatus.contains(ReCAPConstants.COMPLETED)) {
                    writeStatusToFile(file, ReCAPConstants.IN_PROGRESS);
                }
            } else {
                parentFile.mkdirs();
                file.createNewFile();
                writeStatusToFile(file, ReCAPConstants.IN_PROGRESS);
            }
        } catch (IOException e) {
            logger.error("Exception while creating or updating the file : " + e.getMessage());
        }
    }

    private void writeStatusToFile(File file, String status) throws IOException {
        FileWriter fileWriter = new FileWriter(file, false);
        fileWriter.append(status);
        fileWriter.flush();
        fileWriter.close();
    }

    private String buildErrorMessage(Map<Integer, String> erroMessageMap) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        erroMessageMap.entrySet().forEach(entry -> {
            errorMessageBuilder.append(entry.getKey()).append(". ").append(entry.getValue()).append("\n");
        });
        return errorMessageBuilder.toString();
    }

    private boolean validateEmailAddress(String toEmailAddress) {
        String regex = ReCAPConstants.REGEX_FOR_EMAIL_ADDRESS;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toEmailAddress);
        return matcher.matches();
    }

    private String getResponseMessage(String outputString, DataDumpRequest dataDumpRequest) throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        String date = new Date().toString();
        if (dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)) {
            if (outputString.equals(ReCAPConstants.DATADUMP_RECORDS_AVAILABLE_FOR_PROCESS)) {
                setDataExportCurrentStatus();
                outputString = ReCAPConstants.DATADUMP_PROCESS_STARTED;
            }
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE, date);
            return outputString;
        }else if (dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP) && outputString != null) {
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE, date);
            return outputString;
        } else {
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE, date);
            return ReCAPConstants.DATADUMP_EXPORT_FAILURE;
        }
    }

    private String getDateTimeString() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }

}