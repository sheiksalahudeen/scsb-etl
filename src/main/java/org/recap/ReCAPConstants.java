package org.recap;

/**
 * Created by premkb on 19/8/16.
 */
public class ReCAPConstants {

    public static final String DATA_DUMP_FILE_NAME = "ReCAPCollectionFor";
    public static final String FILENAME = "fileName";
    public static final String DATETIME_FOLDER = "dateTimeFolder";
    public static final String REQUESTING_INST_CODE = "requestingInstitutionCode";
    public static final String INSTITUTION_CODES = "institutionCodes";
    public static final String TRANSMISSION_TYPE = "transmissionType";
    public static final String EXPORT_FORMAT = "exportFormat";
    public static final String TO_EMAIL_ID = "toEmailId";
    public static final String XML_FILE_FORMAT = ".xml";
    public static final String ZIP_FILE_FORMAT = ".zip";
    public static final String JSON_FILE_FORMAT = ".json";
    public static final String FILE_FORMAT = "fileFormat";

    public static final String COLUMBIA = "CUL";
    public static final String PRINCETON = "PUL";
    public static final String NYPL = "NYPL";
    public static final String SCSB = "SCSB";

    //General Constants
    public static final String INST_NAME= "institutionName";


    //Report
    public static final String FILE_LOAD_STATUS= "FileLoadStatus";
    public static final String FILE_LOADED= "Loaded";
    public static final String FILE_LOAD_EXCEPTION= "Exception";
    public static final String XML_LOAD= "XMLLoad";
    public static final String CAMEL_EXCHANGE_FILE = "CamelFileExchangeFile";


    //Camel Queue Constants
    public static final String REPORT_Q= "scsbactivemq:queue:reportQ";
    public static final String CSV_SUCCESS_Q = "scsbactivemq:queue:csvSuccessQ";
    public static final String CSV_FAILURE_Q = "scsbactivemq:queue:csvFailureQ";
    public static final String FTP_SUCCESS_Q = "scsbactivemq:queue:ftpFailureQ";
    public static final String FTP_FAILURE_Q = "scsbactivemq:queue:ftpSuccessQ";
    public static final String DATADUMP_SUCCESS_REPORT_Q = "scsbactivemq:queue:dataDumpSuccessReportQ";
    public static final String DATADUMP_SUCCESS_REPORT_CSV_Q = "scsbactivemq:queue:dataDumpSuccessReportCsvQ";
    public static final String DATADUMP_FAILURE_REPORT_Q = "scsbactivemq:queue:dataDumpFailureReportQ";
    public static final String DATADUMP_FAILURE_REPORT_CSV_Q = "scsbactivemq:queue:dataDumpFailureReportCsvQ";
    public static final String DATADUMP_SUCCESS_REPORT_FTP_Q = "scsbactivemq:queue:dataDumpSuccessReportFtpQ";
    public static final String DATADUMP_FAILURE_REPORT_FTP_Q = "scsbactivemq:queue:dataDumpFailureReportFtpQ";
    public static final String DATADUMP_FILE_SYSTEM_Q = "scsbactivemq:queue:dataDumpFileSystemQ";
    public static final String EMAIL_Q = "scsbactivemq:queue:emailQ";
    public static final String DATADUMP_ZIPALLFILE_FILESYSTEM_Q = "direct:zipAllFileSystemQ";
    public static final String DATADUMP_ZIPFILE_FTP_Q = "direct:zipFileFtpQ";
    public static final String DATADUMP_HTTP_Q = "scsbactivemq:queue:dataExportHttpQ";

    //Camel Route Ids
    public static final String REPORT_ROUTE_ID = "reportQRoute";
    public static final String CSV_SUCCESS_ROUTE_ID = "csvSuccessQ";
    public static final String CSV_FAILURE_ROUTE_ID = "csvFailureQ";
    public static final String FTP_SUCCESS_ROUTE_ID = "ftpFailureQ";
    public static final String FTP_FAILURE_ROUTE_ID = "ftpSuccessQ";
    public static final String EMAIL_ROUTE_ID = "emailQ";
    public static final String DATADUMP_ZIP_FILESYSTEM_ROUTE_ID = "zipDataDumpQ";
    public static final String DATADUMP_ZIPFTP_ROUTE_ID = "zipDataDumpRoute";


    public static final String DATE_FORMAT_FOR_FILE_NAME = "ddMMMyyyy";
    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";

    //CSV Generator Constants
    public static final String FILE_SYSTEM = "FileSystem";
    public static final String OPERATION_TYPE_ETL = "ETL";

    //FTP Generator Constants
    public static final String FTP = "FTP";

    //Failure Report Record Constants
    public static final String OWNING_INSTITUTION = "OwningInstitution";
    public static final String OWNING_INSTITUTION_BIB_ID = "OwningInstitutionBibId";
    public static final String OWNING_INSTITUTION_HOLDINGS_ID = "OwningInstitutionHoldingsId";
    public static final String LOCAL_ITEM_ID = "LocalItemId";
    public static final String ITEM_BARCODE = "ItemBarcode";
    public static final String CUSTOMER_CODE = "CustomerCode";
    public static final String TITLE = "Title";
    public static final String COLLECTION_GROUP_DESIGNATION = "CollectionGroupDesignation";
    public static final String CREATE_DATE_ITEM = "CreateDateItem";
    public static final String LAST_UPDATED_DATE_ITEM = "LastUpdatedDateItem";
    public static final String EXCEPTION_MESSAGE = "ExceptionMessage";
    public static final String ERROR_DESCRIPTION = "ErrorDescription";

    //Success Report Record Constants
    public static final String FILE_NAME = "FileName";
    public static final String TOTAL_RECORDS_IN_FILE = "TotalRecordsInFile";
    public static final String TOTAL_BIBS_LOADED = "TotalBibsLoaded";
    public static final String TOTAL_HOLDINGS_LOADED = "TotalHoldingsLoaded";
    public static final String TOTAL_BIB_HOLDINGS_LOADED = "TotalBibHoldingsLoaded";
    public static final String TOTAL_ITEMS_LOADED = "TotalItemsLoaded";
    public static final String TOTAL_BIB_ITEMS_LOADED = "TotalBibItemsLoaded";

    //File Name Processor Constants
    public static final String REPORT_FILE_NAME = "fileName";
    public static final String REPORT_TYPE = "reportType";
    public static final String DIRECTORY_NAME = "directoryName";

    //Date Util
    public static final String DATE_FORMAT_MMDDYYY = "MM-dd-yyyy";
    public static final String DATE_FORMAT_YYYYMMDDHHMM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_DDMMMYYYYHHMM = "ddMMMyyyyHHmm";
    public static final String UTC = "UTC";
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String SOLR_DATE_RANGE_TO_NOW = " TO NOW";

    //Data Dump
    public static final String DATADUMP_EXPORT_FAILURE = "Data dump export failed, please check with the support team.";
    public static final String DATADUMP_NO_RECORD = "There is no data to export.";
    public static final String DATADUMP_PROCESS_STARTED = "Export process has started and we will send an email notification upon completion";
    public static final String DATADUMP_INSTITUTIONCODE_ERR_MSG = "Please enter the value for institutioncode parameter.";
    public static final String DATADUMP_VALID_INST_CODES_ERR_MSG = "Please enter valid institution code CUL or PUL or NYP for institutionCodes .";
    public static final String DATADUMP_DATE_ERR_MSG = "Please enter the value for date .";
    public static final String DATADUMP_TRANS_TYPE_ERR_MSG = "Please enter valid transmission type.";
    public static final String DATADUMP_VALID_FETCHTYPE_ERR_MSG = "Please enter valid fetchType either 0 or 1.";
    public static final String DATADUMP_VALID_REQ_INST_CODE_ERR_MSG = "Please enter valid institution code CUL or PUL or NYP for requestingInstitutionCode .";
    public static final String DATADUMP_FULL_VALID_TRANS_TYPE = "Transmission type 1 is not valid for full dump. Use transmission type 0 or 2";
    public static final String DATADUMP_HTTP_REPONSE_RECORD_LIMIT_ERR_MSG = "There are more than 100 records. Use transmission type ftp or file system to dump the data";
    public static final String DATADUMP_EMAIL_TO_ADDRESS_REQUIRED = "Please enter a valid e-mail address.";
    public static final String INVALID_EMAIL_ADDRESS = "Email address is invalid.";
    public static final String REGEX_FOR_EMAIL_ADDRESS = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String DATADUMP_FETCHTYPE_FULL = "0";
    public static final String DATADUMP_FETCHTYPE_INCREMENTAL = "1";
    public static final String DATADUMP_FETCHTYPE_DELETED = "2";
    public static final String DATADUMP_TRANSMISSION_TYPE_FTP = "0";
    public static final String DATADUMP_TRANSMISSION_TYPE_HTTP = "1";
    public static final String DATADUMP_TRANSMISSION_TYPE_FILESYSTEM = "2";
    public static final String DATADUMP_XML_FORMAT_MARC = "0";
    public static final String DATADUMP_XML_FORMAT_SCSB = "1";
    public static final String DATADUMP_DELETED_JSON_FORMAT = "2";
    public static final Integer IS_NOT_DELETED=0;
    public static final String DATADUMP_SUCCESSLIST = "successList";
    public static final String DATADUMP_FAILURELIST = "failureList";
    public static final String DATADUMP_FORMATTEDSTRING = "formattedString";
    public static final String DATADUMP_FORMATERROR = "formatError";

    public static final String  BIB_LASTUPDATED_DATE = "BibLastUpdatedDate";

    public static final String COLLECTION_GROUP_SHARED = "Shared";
    public static final String COLLECTION_GROUP_OPEN = "Open";
    public static final String COLLECTION_GROUP_PRIVATE = "Private";

    public static final String RESPONSE_DATE = "Date";

    public static final String SOLR_INPUT_FOR_DATA_EXPORT_Q = "scsbactivemq:queue:SolrInputForDataExportQ";
    public static final String BIB_ENTITY_FOR_DATA_EXPORT_Q = "scsbactivemq:queue:BibEntityForDataExportQ";
    public static final String MARC_RECORD_FOR_DATA_EXPORT_Q = "scsbactivemq:queue:MarcRecordForDataExportQ";
    public static final String SCSB_RECORD_FOR_DATA_EXPORT_Q = "scsbactivemq:queue:SCSBRecordForDataExportQ";
    public static final String DELETED_JSON_RECORD_FOR_DATA_EXPORT_Q = "scsbactivemq:queue:DeletedJsonRecordForDataExportQ";
    public static final String DATADUMP_STAGING_Q = "scsbactivemq:queue:dataExportStagingQ";

    public static final String SOLR_INPUT_DATA_EXPORT_ROUTE_ID = "solrInputDataExportRouteId";
    public static final String BIB_ENTITY_DATA_EXPORT_ROUTE_ID = "bibEntityDataExportRouteId";
    public static final String MARC_RECORD_DATA_EXPORT_ROUTE_ID = "marcRecordDataExportRouteId";
    public static final String SCSB_RECORD_DATA_EXPORT_ROUTE_ID = "scsbRecordDataExportRouteId";
    public static final String DELETED_JSON_RECORD_DATA_EXPORT_ROUTE_ID = "deletedJsonRecordDataExportRouteId";
    public static final String DATADUMP_STAGING_ROUTE_ID = "dataExportStagingRouteId";

    public static final String NUM_RECORDS = "Num Records";
    public static final String NUM_BIBS_EXPORTED = "NoOfBibsExported";
    public static final String BATCH_EXPORT = "BatchExport";
    public static final String BATCH_EXPORT_SUCCESS = "BatchExportSuccess";
    public static final String BATCH_EXPORT_FAILURE = "BatchExportFailure";
    public static final String REQUEST_ID = "requestId";
    public static final String FAILURE_CAUSE = "FailureCause";
    public static final String FAILED_BIBS = "FailedBibs";
    public static final String COLLECTION_GROUP_IDS = "collectionGroupIds";
    public static final String FETCH_TYPE = "fetchType";
    public static final String EXPORT_FROM_DATE = "exportFromDate";

    public static final String DATADUMP_SUCCESS_REPORT_CSV_ROUTE_ID = "dataExportSucccessReportCsvRouteId";
    public static final String DATADUMP_FAILURE_REPORT_CSV_ROUTE_ID = "dataExportFailureReportCsvRouteId";
    public static final String DATADUMP_SUCCESS_REPORT_FTP_ROUTE_ID = "dataExportSuccessReportFtpRouteId";
    public static final String DATADUMP_FAILURE_REPORT_FTP_ROUTE_ID = "dataExportFailureReportFtpRouteId";
    public static final String DATADUMP_SUCCESS_REPORT_ROUTE_ID = "dataExportSuccessReportRouteId";
    public static final String DATADUMP_FAILURE_REPORT_ROUTE_ID = "dataExportFailureReportRouteId";
}
