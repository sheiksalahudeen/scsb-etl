package org.recap.service.formatter.datadump;

import org.recap.ReCAPConstants;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.*;
import org.recap.model.jpa.*;
import org.recap.repository.MatchingBibInfoDetailRepository;
import org.recap.repository.MatchingInstitutionBibRepository;
import org.recap.repository.ReportDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

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

    @Autowired
    private MatchingBibInfoDetailRepository matchingBibInfoDetailRepository;

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
        Map<String,Integer> bibIdRecordNumMap = null;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Integer> recordNumList = matchingBibInfoDetailRepository.getRecordNum(bibIdList);
        stopWatch.stop();
        logger.info("Time taken to fetch record num---->"+stopWatch.getTotalTimeSeconds());
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        List<MatchingBibInfoDetail> matchingBibInfoDetailList = matchingBibInfoDetailRepository.findByRecordNum(recordNumList);
        stopWatch1.stop();
        logger.info("Time taken to fetch MatchingBibInfoDetail---->"+stopWatch1.getTotalTimeSeconds());
        Map<Integer,List<MatchingBibInfoDetail>> recordNumMatchingBibInfoDetailMap = null;
        if(recordNumList != null && recordNumList.size() > 0){
            logger.info("recordNumList size --->"+recordNumList.size());
            bibIdRecordNumMap = getBibIdRowNumMap(matchingBibInfoDetailList);// put bib id and record num from report table in a map
            recordNumMatchingBibInfoDetailMap = getRecordNumReportDataEntityMap(matchingBibInfoDetailList);
        }

        for (Iterator<BibliographicEntity> bibliographicEntityIterator = bibliographicEntities.iterator(); bibliographicEntityIterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = bibliographicEntityIterator.next();
            List<MatchingBibInfoDetail> matchingBibInfoDetailListForSingleBib = null;
            if(bibIdRecordNumMap!=null){
                Integer rowNum = bibIdRecordNumMap.get(String.valueOf(bibliographicEntity.getBibliographicId()));
                matchingBibInfoDetailListForSingleBib = recordNumMatchingBibInfoDetailMap.get(rowNum);
            }
            Map<String, Object> stringObjectMap = prepareBibRecord(bibliographicEntity,matchingBibInfoDetailListForSingleBib);
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

    private Map<Integer, List<MatchingBibInfoDetail>> getRecordNumReportDataEntityMap(List<MatchingBibInfoDetail> matchingBibInfoDetailList) {
        Map<Integer, List<MatchingBibInfoDetail>> recordNumMatchingBibInfoDetailMap = new HashMap<>();
        for (MatchingBibInfoDetail matchingBibInfoDetail : matchingBibInfoDetailList) {
            if (recordNumMatchingBibInfoDetailMap.containsKey(matchingBibInfoDetail.getRecordNum())) {
                recordNumMatchingBibInfoDetailMap.get(matchingBibInfoDetail.getRecordNum()).add(matchingBibInfoDetail);
            } else {
                List<MatchingBibInfoDetail> reportDataEntityListForRowNum = new ArrayList<>();
                reportDataEntityListForRowNum.add(matchingBibInfoDetail);
                recordNumMatchingBibInfoDetailMap.put(matchingBibInfoDetail.getRecordNum(), reportDataEntityListForRowNum);
            }
        }
        return recordNumMatchingBibInfoDetailMap;
    }

    private List<String> getBibIdList(List<BibliographicEntity> bibliographicEntityList){
        List<String> bibIdList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity : bibliographicEntityList){
            bibIdList.add(String.valueOf(bibliographicEntity.getBibliographicId()));
        }
        return bibIdList;
    }

    private Map<String,Integer> getBibIdRowNumMap(List<MatchingBibInfoDetail> matchingBibInfoDetailList){
        Map<String,Integer> bibIdRownumMap = new HashMap<>();
        for(MatchingBibInfoDetail matchingBibInfoDetail: matchingBibInfoDetailList){
            bibIdRownumMap.put(matchingBibInfoDetail.getBibId(),matchingBibInfoDetail.getRecordNum());
        }
        return bibIdRownumMap;
    }

    private Map<String, Object> prepareBibRecord(BibliographicEntity bibliographicEntity,List<MatchingBibInfoDetail> matchingBibInfoDetailList) {
        BibRecord bibRecord = null;
        Map results = new HashMap();
        try {
            Bib bib = getBib(bibliographicEntity,matchingBibInfoDetailList);
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

    private Bib getBib(BibliographicEntity bibliographicEntity,List<MatchingBibInfoDetail> matchingBibInfoDetailList) throws Exception{
        Bib bib = new Bib();
        bib.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        bib.setOwningInstitutionId(bibliographicEntity.getInstitutionEntity().getInstitutionCode());
        if(matchingBibInfoDetailList!=null){
            bib.setMatchingInstitutionBibId(getMatchingInstitutionBibId(String.valueOf(bibliographicEntity.getBibliographicId()),matchingBibInfoDetailList));
        }
        ContentType contentType = getContentType(bibliographicEntity.getContent());
        List<RecordType> record = contentType.getCollection().getRecord();
        RecordType recordType = record.get(0);
        String value = ReCAPConstants.SCSB+"-"+bibliographicEntity.getBibliographicId();
        recordType.getControlfield().get(0).setValue(value);
        bib.setContent(contentType);
        return bib;
    }

    private List<MatchingInstitutionBibIdType> getMatchingInstitutionBibId(String bibId,List<MatchingBibInfoDetail> matchingBibInfoDetailList) {
        List<MatchingInstitutionBibIdType> matchingInstitutionBibIdTypeList = new ArrayList<>();
        for(MatchingBibInfoDetail matchingBibInfoDetail:matchingBibInfoDetailList){
            if(!bibId.equals(matchingBibInfoDetail.getBibId())){
                MatchingInstitutionBibIdType matchingInstitutionBibIdType = new MatchingInstitutionBibIdType();
                matchingInstitutionBibIdType.setSource(matchingBibInfoDetail.getOwningInstitution());
                matchingInstitutionBibIdType.setValue(matchingBibInfoDetail.getOwningInstitutionBibId());
                matchingInstitutionBibIdTypeList.add(matchingInstitutionBibIdType);
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
