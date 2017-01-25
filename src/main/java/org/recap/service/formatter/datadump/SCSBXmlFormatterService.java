package org.recap.service.formatter.datadump;

import org.recap.ReCAPConstants;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.*;
import org.recap.model.jpa.*;
import org.recap.repository.MatchingInstitutionBibRepository;
import org.recap.repository.ReportDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by premkb on 28/9/16.
 */
@Service
@Scope("prototype")
public class SCSBXmlFormatterService implements DataDumpFormatterInterface {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SCSBXmlFormatterService.class);

    @Autowired
    private MatchingInstitutionBibRepository matchingInstitutionBibIdRepository;

    @Autowired
    private ReportDataRepository reportDataRepository;

    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(ReCAPConstants.DATADUMP_XML_FORMAT_SCSB) ? true:false;
    }

    public String getSCSBXmlForBibRecords(List<BibRecord> bibRecords) throws Exception{
        String formattedString = null;
        BibRecords bibRecords1 = new BibRecords();
        bibRecords1.setBibRecords(bibRecords);
        formattedString = convertToXml(bibRecords1);

        return formattedString;
    }

    private String convertToXml(BibRecords bibRecords) throws Exception {
        StringWriter stringWriter = new StringWriter();
            Marshaller jaxbMarshaller = JAXBContextHandler.getInstance().getJAXBContextForClass(BibRecords.class).createMarshaller();
            synchronized (jaxbMarshaller) {
                jaxbMarshaller.marshal(bibRecords, stringWriter);
            }
        return stringWriter.toString();
    }

    public Map<String, Object> prepareBibRecords(List<BibliographicEntity> bibliographicEntities) {
        Map resultsMap = new HashMap();
        List<BibRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> bibIdList = getBibIdList(bibliographicEntities);
        List<MatchingInstitutionBibViewEntity> matchingInstitutionBibIdViewEntityList = matchingInstitutionBibIdRepository.findByBibIdList(bibIdList);
        Map<String,String> bibIdRownumMap = getBibIdRowNumMap(matchingInstitutionBibIdViewEntityList);
        List<ReportDataEntity> reportDataEntityList = getReportDataEntitieList(matchingInstitutionBibIdViewEntityList);
        Map<String,List<ReportDataEntity>> reportDataEntityMap = getRownumReportDataEntityMap(reportDataEntityList);
        for (Iterator<BibliographicEntity> bibliographicEntityIterator = bibliographicEntities.iterator(); bibliographicEntityIterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = bibliographicEntityIterator.next();
            Map<String, Object> stringObjectMap = prepareBibRecord(bibliographicEntity,reportDataEntityMap.get(bibIdRownumMap.get(String.valueOf(bibliographicEntity.getBibliographicId()))));
            BibRecord bibRecord = (BibRecord) stringObjectMap.get(ReCAPConstants.SUCCESS);
            if (null != bibRecord) {
                records.add(bibRecord);
            }
            String failureMsg = (String) stringObjectMap.get(ReCAPConstants.FAILURE);
            if (null != failureMsg) {
                errors.add(failureMsg);
            }
        }
        resultsMap.put(ReCAPConstants.SUCCESS, records);
        resultsMap.put(ReCAPConstants.FAILURE, errors);
        return resultsMap;
    }

    private List<ReportDataEntity> getReportDataEntitieList(List<MatchingInstitutionBibViewEntity> matchingInstitutionBibIdViewEntityList) {
        List<String> recordNumList = getRecordNumList(matchingInstitutionBibIdViewEntityList);
        List<String> headerNameList = getHeaderNameList();
        return reportDataRepository.getReportDataForMatchingInstitutionBib(recordNumList,headerNameList);
    }

    private List<String> getHeaderNameList() {
        List<String> headerNameList = new ArrayList<>();
        headerNameList.add(ReCAPConstants.BIB_ID);
        headerNameList.add(ReCAPConstants.OWNING_INSTITUTION);
        headerNameList.add(ReCAPConstants.OWNING_INSTITUTION_BIB_ID);
        return headerNameList;
    }

    private List<String> getBibIdList(List<BibliographicEntity> bibliographicEntityList){
        List<String> bibIdList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity : bibliographicEntityList){
            bibIdList.add(String.valueOf(bibliographicEntity.getBibliographicId()));
        }
        return bibIdList;
    }

    private List<String> getRecordNumList(List<MatchingInstitutionBibViewEntity> matchingInstitutionBibIdViewEntityList){
        List<String> recordNumList = new ArrayList<>();
        for(MatchingInstitutionBibViewEntity matchingInstitutionBibIdViewEntity:matchingInstitutionBibIdViewEntityList){
            recordNumList.add(matchingInstitutionBibIdViewEntity.getId());
        }
        return recordNumList;
    }

    private Map<String,String> getBibIdRowNumMap(List<MatchingInstitutionBibViewEntity> matchingInstitutionBibIdViewEntityList){
        Map<String,String> bibIdRownumMap = new HashMap<>();
        for(MatchingInstitutionBibViewEntity matchingInstitutionBibIdViewEntity: matchingInstitutionBibIdViewEntityList){
            bibIdRownumMap.put(matchingInstitutionBibIdViewEntity.getBibId(),matchingInstitutionBibIdViewEntity.getId());
        }
        return bibIdRownumMap;
    }

    private  Map<String,List<ReportDataEntity>> getRownumReportDataEntityMap(List<ReportDataEntity> reportDataEntityList){
        Map<String,List<ReportDataEntity>> reportDataEntityMap = new HashMap<>();
        for(ReportDataEntity reportDataEntity:reportDataEntityList){
            if(reportDataEntityMap.containsKey(reportDataEntity.getRecordNum())){
                reportDataEntityMap.get(reportDataEntity.getRecordNum()).add(reportDataEntity);
            }else{
                List<ReportDataEntity> reportDataEntityListForRowNum = new ArrayList<>();
                reportDataEntityListForRowNum.add(reportDataEntity);
                reportDataEntityMap.put(reportDataEntity.getRecordNum(),reportDataEntityListForRowNum);
            }
        }
        return reportDataEntityMap;
    }

    private Map<String, Object> prepareBibRecord(BibliographicEntity bibliographicEntity,List<ReportDataEntity> reportDataEntityList) {
        BibRecord bibRecord = null;
        Map results = new HashMap();
        try {
            Bib bib = getBib(bibliographicEntity,reportDataEntityList);
            List<Integer> itemIds = getItemIds(bibliographicEntity);
            List<Holdings> holdings = getHoldings(bibliographicEntity.getHoldingsEntities(),itemIds);
            bibRecord = new BibRecord();
            bibRecord.setBib(bib);
            bibRecord.setHoldings(holdings);
            results.put(ReCAPConstants.SUCCESS, bibRecord);
        } catch (Exception e) {
            logger.error(e.getMessage());
            results.put(ReCAPConstants.FAILURE, String.valueOf(e.getCause()));
        }
        return results;
    }

    private List<Integer> getItemIds(BibliographicEntity bibliographicEntity){
        List<Integer> itemIds = new ArrayList<>();
        List<ItemEntity> itemEntityList = bibliographicEntity.getItemEntities();
        for(ItemEntity itemEntity : itemEntityList){
            itemIds.add(itemEntity.getItemId());
        }
        return itemIds;
    }

    private Bib getBib(BibliographicEntity bibliographicEntity,List<ReportDataEntity> reportDataEntityList) throws Exception{
        Bib bib = new Bib();
        bib.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        bib.setOwningInstitutionId(bibliographicEntity.getInstitutionEntity().getInstitutionCode());
        bib.setMatchingInstitutionBibId(getMatchingInstitutionBibId(bibliographicEntity.getBibliographicId(),reportDataEntityList));
        ContentType contentType = getContentType(bibliographicEntity.getContent());
        List<RecordType> record = contentType.getCollection().getRecord();
        RecordType recordType = record.get(0);
        String value = ReCAPConstants.SCSB+"-"+bibliographicEntity.getBibliographicId();
        recordType.getControlfield().get(0).setValue(value);
        bib.setContent(contentType);
        return bib;
    }

    private List<MatchingInstitutionBibIdType> getMatchingInstitutionBibId(Integer bibId,List<ReportDataEntity> reportDataEntityList){
        List<MatchingInstitutionBibIdType> matchingInstitutionBibIdTypeList = new ArrayList<>();
        String[] bibidArray = null;
        String[] institutionArray = null;
        String[] owningInstitutionBibIdArray = null;
        if (reportDataEntityList != null) {
            for(ReportDataEntity reportDataEntity:reportDataEntityList){
                if(reportDataEntity.getHeaderName().equals(ReCAPConstants.BIB_ID)){
                    bibidArray = reportDataEntity.getHeaderValue().split(",");
                }else if(reportDataEntity.getHeaderName().equals(ReCAPConstants.OWNING_INSTITUTION)){
                    institutionArray = reportDataEntity.getHeaderValue().split(",");
                }else if(reportDataEntity.getHeaderName().equals(ReCAPConstants.OWNING_INSTITUTION_BIB_ID)){
                    owningInstitutionBibIdArray = reportDataEntity.getHeaderValue().split(",");
                }
            }
            for(int count=0;count<bibidArray.length;count++){
                if(!bibidArray[count].equals(String.valueOf(bibId))){
                    MatchingInstitutionBibIdType matchingInstitutionBibIdType = new MatchingInstitutionBibIdType();
                    matchingInstitutionBibIdType.setSource(institutionArray[count]);
                    matchingInstitutionBibIdType.setValue(owningInstitutionBibIdArray[count]);
                    matchingInstitutionBibIdTypeList.add(matchingInstitutionBibIdType);
                }
            }
        }
        return matchingInstitutionBibIdTypeList;
    }

    private List<Holdings> getHoldings(List<HoldingsEntity> holdingsEntityList,List<Integer> itemIds) throws Exception{
        List<Holdings> holdingsList = new ArrayList<>();
        if (holdingsEntityList!=null && !CollectionUtils.isEmpty(holdingsEntityList)) {
            for (HoldingsEntity holdingsEntity : holdingsEntityList) {
                Holdings holdings = new Holdings();
                Holding holding = new Holding();
                holding.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());
                ContentType contentType = getContentType(holdingsEntity.getContent());
                holding.setContent(contentType);
                List<ItemEntity> itemEntityList = new ArrayList<>();
                if(holdingsEntity.getItemEntities()!=null && !isHoldingSingleItemPrivate(holdingsEntity.getItemEntities())) {
                    for(ItemEntity itemEntity:holdingsEntity.getItemEntities()){
                        if(itemIds.contains(itemEntity.getItemId())) {
                            itemEntityList.add(itemEntity);
                        }
                    }
                    Items items = getItems(itemEntityList);
                    holding.setItems(Arrays.asList(items));
                    holdings.setHolding(Arrays.asList(holding));
                    holdingsList.add(holdings);
                }
            }
        }
        return holdingsList;
    }

    private boolean isHoldingSingleItemPrivate(List<ItemEntity> itemEntities){
        if(itemEntities.size()==1 && itemEntities.get(0).getCollectionGroupEntity().getCollectionGroupCode().equals(ReCAPConstants.COLLECTION_GROUP_PRIVATE)){
            return true;
        }else{
            for(ItemEntity itemEntity : itemEntities) {
                if (itemEntity.getCollectionGroupEntity().getCollectionGroupCode().equals(ReCAPConstants.COLLECTION_GROUP_PRIVATE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Items getItems(List<ItemEntity> itemEntities) {
        Items items = new Items();
        ContentType itemContentType = new ContentType();
        CollectionType collectionType = new CollectionType();
        collectionType.setRecord(buildRecordTypes(itemEntities));
        itemContentType.setCollection(collectionType);
        items.setContent(itemContentType);
        return items;
    }

    private List<RecordType> buildRecordTypes(List<ItemEntity> itemEntities) {
        List<RecordType> recordTypes = new ArrayList<>();
        if (itemEntities!=null) {
            for (ItemEntity itemEntity : itemEntities) {
                if(!itemEntity.getCollectionGroupEntity().getCollectionGroupCode().equals(ReCAPConstants.COLLECTION_GROUP_PRIVATE)) {
                    RecordType recordType = new RecordType();
                    List<DataFieldType> dataFieldTypeList = new ArrayList<>();
                    dataFieldTypeList.add(build876DataField(itemEntity));
                    dataFieldTypeList.add(build900DataField(itemEntity));
                    recordType.setDatafield(dataFieldTypeList);
                    recordTypes.add(recordType);
                }
            }
        }
        return recordTypes;
    }

    private DataFieldType build900DataField(ItemEntity itemEntity) {
        DataFieldType dataFieldType = new DataFieldType();
        List<SubfieldatafieldType> subfieldatafieldTypes = new ArrayList<>();
        dataFieldType.setTag("900");
        dataFieldType.setInd1(" ");
        dataFieldType.setInd2(" ");
        subfieldatafieldTypes.add(getSubfieldatafieldType("a", itemEntity.getCollectionGroupEntity().getCollectionGroupCode()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("b", itemEntity.getCustomerCode()));
        dataFieldType.setSubfield(subfieldatafieldTypes);
        return dataFieldType;
    }

    private DataFieldType build876DataField(ItemEntity itemEntity) {
        DataFieldType dataFieldType = new DataFieldType();
        List<SubfieldatafieldType> subfieldatafieldTypes = new ArrayList<>();
        dataFieldType.setTag("876");
        dataFieldType.setInd1(" ");
        dataFieldType.setInd2(" ");
        subfieldatafieldTypes.add(getSubfieldatafieldType("p", itemEntity.getBarcode()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("h", itemEntity.getUseRestrictions()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("a", itemEntity.getOwningInstitutionItemId()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("j", itemEntity.getItemStatusEntity().getStatusCode()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("t", itemEntity.getCopyNumber().toString()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("3", itemEntity.getVolumePartYear()));
        dataFieldType.setSubfield(subfieldatafieldTypes);
        return dataFieldType;
    }

    private SubfieldatafieldType getSubfieldatafieldType(String code, String value) {
        SubfieldatafieldType subfieldatafieldType = new SubfieldatafieldType();
        subfieldatafieldType.setCode(code);
        subfieldatafieldType.setValue(value);
        return subfieldatafieldType;
    }

    private ContentType getContentType(byte[] byteContent) throws Exception{
        String content = new String(byteContent, Charset.forName("UTF-8"));
        CollectionType collectionType = null;
        collectionType = (CollectionType) JAXBHandler.getInstance().unmarshal(content, CollectionType.class);
        ContentType contentType = new ContentType();
        contentType.setCollection(collectionType);
        return contentType;
    }


}
