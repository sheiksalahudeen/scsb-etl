package org.recap;

/**
 * Created by premkb on 19/8/16.
 */
public class ReCAPConstants {

    public static final String DATA_DUMP_FILE_NAME = "ReCAPCollectionFor";
    public static final String FILENAME = "fileName";
    public static final String CAMELFILENAME = "CamelFileName";
    public static final String REQUESTING_INST_CODE = "requestingInstitutionCode";
    public static final String XML_FILE_FORMAT = ".xml";
    public static final String ZIP_FILE_FORMAT = ".zip";

    public static final String COLUMBIA = "CUL";
    public static final String PRINCETON = "PUL";
    public static final String NYPL = "NYPL";

    //General Constants
    public static final String INST_NAME= "institutionName";


    //Report
    public static final String FILE_LOAD_STATUS= "FileLoadStatus";
    public static final String FILE_LOADED= "Loaded";
    public static final String FILE_LOAD_EXCEPTION= "Exception";
    public static final String XML_LOAD= "XMLLoad";
    public static final String CAMEL_EXCHANGE_FILE = "CamelFileExchangeFile";


    //Camel Queue Constants
    public static final String REPORT_Q= "seda:reportQ";
    public static final String CSV_SUCCESS_Q = "seda:csvSuccessQ";
    public static final String CSV_FAILURE_Q = "seda:csvFailureQ";
    public static final String FTP_SUCCESS_Q = "seda:ftpFailureQ";
    public static final String FTP_FAILURE_Q = "seda:ftpSuccessQ";
    public static final String DATA_DUMP_FTP_Q = "seda:dataDumpQ";
    public static final String DATA_DUMP_FILE_SYSTEM_Q = "seda:dataDumpFileSystemQ";
    public static final String DATA_DUMP_REPORT_FTP_Q = "seda:dataDumpReportFtpQ";
    public static final String DATA_DUMP_REPORT_FILE_SYSTEM_Q = "seda:dataDumpReportFileSystemQ";
    public static final String EMAIL_Q = "seda:emailQ";
    public static final String DATA_DUMP_ZIP_FILE_Q = "direct:zipToFileQ";
    public static final String DATA_DUMP_ZIP_FILE_TO_FTP_Q = "direct:zipFileToFtpQ";

    //Camel Route Ids
    public static final String REPORT_ROUTE_ID = "reportQRoute";
    public static final String CSV_SUCCESS_ROUTE_ID = "csvSuccessQ";
    public static final String CSV_FAILURE_ROUTE_ID = "csvFailureQ";
    public static final String FTP_SUCCESS_ROUTE_ID = "ftpFailureQ";
    public static final String FTP_FAILURE_ROUTE_ID = "ftpSuccessQ";
    public static final String EMAIL_ROUTE_ID = "emailQ";

    public static final String DATE_FORMAT_FOR_FILE_NAME = "ddMMMyyyy";
    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";

    //CSV Generator Constants
    public static final String FILE_SYSTEM = "FileSystem";

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
    public static final String DATE_FORMAT_MMDDYYYHHMM = "yyyy-MM-dd HH:mm";

    //Data Dump
    public static final String DATADUMP_LIMIT_PAGE = "datadump.limit.page";;
    public static final String DATADUMP_THREADS = "datadump.threads";;
    public static final String DATADUMP_BATCHSIZE = "datadump.batchsize";;
    public static final String DATADUMP_EXPORT_SUCCESS = "Data dump exported successfully.";
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
    public static final int DATADUMP_FETCHTYPE_FULL = 0;
    public static final int DATADUMP_FETCHTYPE_INCREMENTAL = 1;
    public static final int DATADUMP_TRANSMISSION_TYPE_FTP = 0;
    public static final int DATADUMP_TRANSMISSION_TYPE_HTTP = 1;
    public static final int DATADUMP_TRANSMISSION_TYPE_FILESYSTEM = 2;

    public static final String COLLECTION_GROUP_SHARED = "Shared";
    public static final String COLLECTION_GROUP_OPEN = "Open";
    public static final String COLLECTION_GROUP_PRIVATE = "Private";

    public static final String RESPONSE_DATE = "Date";

}
