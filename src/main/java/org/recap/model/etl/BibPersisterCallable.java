package org.recap.model.etl;

import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;
import org.recap.model.jaxb.marc.LeaderFieldType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.*;
import org.recap.util.DBReportUtil;
import org.recap.util.MarcUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * Created by pvsubrah on 6/24/16.
 */
public class BibPersisterCallable implements Callable {

    private MarcUtil marcUtil;
    private BibRecord bibRecord;
    private XmlRecordEntity xmlRecordEntity;
    private Map institutionEntitiesMap;
    private String institutionName;

    private Map itemStatusMap;
    private Map collectionGroupMap;

    private DBReportUtil DBReportUtil;

    @Override
    public Object call() {
        Map<String, Object> map = new HashMap<>();
        boolean processBib = false;

        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();
        List<ReportEntity> reportEntities = new ArrayList<>();

        getDBReportUtil().setInstitutionEntitiesMap(institutionEntitiesMap);
        getDBReportUtil().setCollectionGroupMap(collectionGroupMap);

        Integer owningInstitutionId = (Integer) institutionEntitiesMap.get(bibRecord.getBib().getOwningInstitutionId());
        Map<String, Object> bibMap = processAndValidateBibliographicEntity(owningInstitutionId);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) bibMap.get("bibliographicEntity");
        ReportEntity bibReportEntity = (ReportEntity) bibMap.get("bibReportEntity");
        if (bibReportEntity != null) {
            reportEntities.add(bibReportEntity);
        } else {
            processBib = true;
        }

        List<Holdings> holdings = bibRecord.getHoldings();
        for (Iterator<Holdings> iterator = holdings.iterator(); iterator.hasNext(); ) {
            Holdings holdingsList = iterator.next();
            List<Holding> holding = holdingsList.getHolding();
            for (Iterator<Holding> holdingIterator = holding.iterator(); holdingIterator.hasNext(); ) {
                boolean processHoldings = false;
                Holding holdingEnt = holdingIterator.next();
                if (holdingEnt.getContent() != null) {
                    CollectionType holdingContentCollection = holdingEnt.getContent().getCollection();
                    List<RecordType> holdingRecordTypes = holdingContentCollection.getRecord();
                    RecordType holdingsRecordType = holdingRecordTypes.get(0);

                    Map<String, Object> holdingsMap = processAndValidateHoldingsEntity(bibliographicEntity, holdingEnt, holdingContentCollection);
                    HoldingsEntity holdingsEntity = (HoldingsEntity) holdingsMap.get("holdingsEntity");
                    ReportEntity holdingsReportEntity = (ReportEntity) holdingsMap.get("holdingsReportEntity");
                    if (holdingsReportEntity != null) {
                        reportEntities.add(holdingsReportEntity);
                    } else {
                        processHoldings = true;
                        holdingsEntities.add(holdingsEntity);
                    }

                    String holdingsCallNumber = getMarcUtil().getDataFieldValue(holdingsRecordType, "852", null, null, "h");
                    String holdingsCallNumberType = getMarcUtil().getInd1(holdingsRecordType, "852", "h");

                    List<Items> items = holdingEnt.getItems();
                    for (Items item : items) {
                        ContentType itemContent = item.getContent();
                        CollectionType itemContentCollection = itemContent.getCollection();

                        List<RecordType> itemRecordTypes = itemContentCollection.getRecord();
                        for (RecordType itemRecordType : itemRecordTypes) {
                            Map<String, Object> itemMap = processAndValidateItemEntity(bibliographicEntity, holdingsEntity, owningInstitutionId, holdingsCallNumber, holdingsCallNumberType, itemRecordType);
                            ItemEntity itemEntity = (ItemEntity) itemMap.get("itemEntity");
                            ReportEntity itemReportEntity = (ReportEntity) itemMap.get("itemReportEntity");
                            if (itemReportEntity != null) {
                                reportEntities.add(itemReportEntity);
                            } else if (processHoldings) {
                                if (holdingsEntity.getItemEntities() == null) {
                                    holdingsEntity.setItemEntities(new ArrayList<>());
                                }
                                holdingsEntity.getItemEntities().add(itemEntity);
                                itemEntities.add(itemEntity);
                            }
                        }
                    }
                }
            }
        }
        bibliographicEntity.setHoldingsEntities(holdingsEntities);
        bibliographicEntity.setItemEntities(itemEntities);

        if (!CollectionUtils.isEmpty(reportEntities)) {
            map.put("reportEntities", reportEntities);
        }
        if (processBib) {
            map.put("bibliographicEntity", bibliographicEntity);
        }
        return map;
    }

    private Map<String, Object> processAndValidateBibliographicEntity(Integer owningInstitutionId) {
        Map<String, Object> map = new HashMap<>();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        StringBuffer errorMessage = new StringBuffer();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(xmlRecordEntity.getXmlFileName());
        reportEntity.setInstitutionName(institutionName);
        reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
        reportEntity.setCreatedDate(new Date());

        Bib bib = bibRecord.getBib();
        String owningInstitutionBibId = getOwningInstitutionBibId(bibRecord, bib);
        if (StringUtils.isNotBlank(owningInstitutionBibId)) {
            bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        } else {
            errorMessage.append("Owning Institution Bib Id cannot be null");
        }
        if (owningInstitutionId != null) {
            bibliographicEntity.setOwningInstitutionId(owningInstitutionId);
        } else {
            errorMessage.append("\n");
            errorMessage.append("Owning Institution Id cannot be null");
        }
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setLastUpdatedBy("etl");

        ContentType bibContent = bib.getContent();
        CollectionType bibContentCollection = bibContent.getCollection();
        String bibXmlContent = bibContentCollection.serialize(bibContentCollection);
        if (StringUtils.isNotBlank(bibXmlContent)) {
            bibliographicEntity.setContent(bibXmlContent.getBytes());
        } else {
            errorMessage.append("\n");
            errorMessage.append("Bib Content cannot be empty");
        }

        boolean subFieldExistsFor245 = getMarcUtil().isSubFieldExists(bibContentCollection.getRecord().get(0), "245");
        if (!subFieldExistsFor245) {
            errorMessage.append("\n");
            errorMessage.append("Atleast one subfield should be there for 245 tag");
        }

        LeaderFieldType leader = bibContentCollection.getRecord().get(0).getLeader();
        if (!(leader != null && StringUtils.isNotBlank(leader.getValue()) && leader.getValue().length() == 24)) {
            errorMessage.append("\n");
            errorMessage.append("Leader Field value should be 24 characters");
        }

        List<ReportDataEntity> reportDataEntities = null;
        if (errorMessage.toString().length() > 1) {
            reportDataEntities = getDBReportUtil().generateBibFailureReportEntity(bibliographicEntity);
            ReportDataEntity errorReportDataEntity = new ReportDataEntity();
            errorReportDataEntity.setHeaderName(ReCAPConstants.ERROR_DESCRIPTION);
            errorReportDataEntity.setHeaderValue(errorMessage.toString());
            reportDataEntities.add(errorReportDataEntity);
        }
        if(!CollectionUtils.isEmpty(reportDataEntities)) {
            reportEntity.addAll(reportDataEntities);
            map.put("bibReportEntity", reportEntity);
        }
        map.put("bibliographicEntity", bibliographicEntity);
        return map;
    }

    private Map<String, Object> processAndValidateHoldingsEntity(BibliographicEntity bibliographicEntity, Holding holdingEnt, CollectionType holdingContentCollection) {
        StringBuffer errorMessage = new StringBuffer();
        Map<String, Object> map = new HashMap<>();
        HoldingsEntity holdingsEntity = new HoldingsEntity();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(xmlRecordEntity.getXmlFileName());
        reportEntity.setInstitutionName(institutionName);
        reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
        reportEntity.setCreatedDate(new Date());

        String holdingsContent = holdingContentCollection.serialize(holdingContentCollection);
        if (StringUtils.isNotBlank(holdingsContent)) {
            holdingsEntity.setContent(holdingsContent.getBytes());
        } else {
            errorMessage.append("Holdings Content cannot be empty");
        }
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        Integer owningInstitutionId = bibliographicEntity.getOwningInstitutionId();
        holdingsEntity.setOwningInstitutionId(owningInstitutionId);
        String owningInstitutionHoldingsId = holdingEnt.getOwningInstitutionHoldingsId();
        if (StringUtils.isBlank(owningInstitutionHoldingsId)) {
            owningInstitutionHoldingsId = UUID.randomUUID().toString();
        }
        holdingsEntity.setOwningInstitutionHoldingsId(owningInstitutionHoldingsId);
        List<ReportDataEntity> reportDataEntities = null;
        if (errorMessage.toString().length() > 1) {
            getDBReportUtil().generateBibHoldingsFailureReportEntity(bibliographicEntity, holdingsEntity);
            ReportDataEntity errorReportDataEntity = new ReportDataEntity();
            errorReportDataEntity.setHeaderName(ReCAPConstants.ERROR_DESCRIPTION);
            errorReportDataEntity.setHeaderValue(errorMessage.toString());
            reportDataEntities.add(errorReportDataEntity);
        }

        if(!CollectionUtils.isEmpty(reportDataEntities)) {
            reportEntity.addAll(reportDataEntities);
            map.put("holdingsReportEntity", reportEntity);
        }
        map.put("holdingsEntity", holdingsEntity);
        return map;
    }

    private Map<String, Object> processAndValidateItemEntity(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity, Integer owningInstitutionId, String holdingsCallNumber, String holdingsCallNumberType, RecordType itemRecordType) {
        StringBuffer errorMessage = new StringBuffer();
        Map<String, Object> map = new HashMap<>();
        ItemEntity itemEntity = new ItemEntity();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(xmlRecordEntity.getXmlFileName());
        reportEntity.setInstitutionName(institutionName);
        reportEntity.setType(org.recap.ReCAPConstants.FAILURE);
        reportEntity.setCreatedDate(new Date());

        String itemBarcode = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "p");
        if (StringUtils.isNotBlank(itemBarcode)) {
            itemEntity.setBarcode(itemBarcode);
        } else {
            errorMessage.append("Item Barcode cannot be null");
        }
        String customerCode = getMarcUtil().getDataFieldValue(itemRecordType, "900", null, null, "b");
        if (StringUtils.isNotBlank(customerCode)) {
            itemEntity.setCustomerCode(customerCode);
        } else {
            errorMessage.append("\n");
            errorMessage.append("Customer Code cannot be null");
        }
        itemEntity.setCallNumber(holdingsCallNumber);
        itemEntity.setCallNumberType(holdingsCallNumberType);
        itemEntity.setItemAvailabilityStatusId((Integer) itemStatusMap.get("Available"));
        String copyNumber = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "t");
        if (StringUtils.isNoneBlank(copyNumber) && org.apache.commons.lang3.math.NumberUtils.isNumber(copyNumber)) {
            itemEntity.setCopyNumber(Integer.valueOf(copyNumber));
        }
        if (owningInstitutionId != null) {
            itemEntity.setOwningInstitutionId(owningInstitutionId);
        } else {
            errorMessage.append("\n");
            errorMessage.append("Owning Institution Id cannot be null");
        }
        String collectionGroupCode = getMarcUtil().getDataFieldValue(itemRecordType, "900", null, null, "a");
        if (StringUtils.isNotBlank(collectionGroupCode) && collectionGroupMap.containsKey(collectionGroupCode)) {
            itemEntity.setCollectionGroupId((Integer) collectionGroupMap.get(collectionGroupCode));
        } else {
            itemEntity.setCollectionGroupId((Integer) collectionGroupMap.get("Open"));
        }
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");

        String useRestrictions = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "h");
        if (StringUtils.isNotBlank(useRestrictions) && (useRestrictions.equalsIgnoreCase("In Library Use") || useRestrictions.equalsIgnoreCase("Supervised Use"))) {
            itemEntity.setUseRestrictions(useRestrictions);
        }

        itemEntity.setVolumePartYear(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "3"));
        String owningInstitutionItemId = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "a");
        if (StringUtils.isNotBlank(owningInstitutionItemId)) {
            itemEntity.setOwningInstitutionItemId(owningInstitutionItemId);
        } else {
            errorMessage.append("\n");
            errorMessage.append("Item Owning Institution Id cannot be null");
        }
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        List<ReportDataEntity> reportDataEntities = null;
        if (errorMessage.toString().length() > 1) {
            reportDataEntities = getDBReportUtil().generateBibHoldingsAndItemsFailureReportEntities(bibliographicEntity, holdingsEntity, itemEntity);
            ReportDataEntity errorReportDataEntity = new ReportDataEntity();
            errorReportDataEntity.setHeaderName(ReCAPConstants.ERROR_DESCRIPTION);
            errorReportDataEntity.setHeaderValue(errorMessage.toString());
            reportDataEntities.add(errorReportDataEntity);
        }
        if(!CollectionUtils.isEmpty(reportDataEntities)) {
            reportEntity.addAll(reportDataEntities);
            map.put("itemReportEntity", reportEntity);
        }
        map.put("itemEntity", itemEntity);
        return map;
    }

    private String getOwningInstitutionBibId(BibRecord bibRecord, Bib bib) {
        return StringUtils.isBlank(bib.getOwningInstitutionBibId()) ? getControlFieldValue001(bibRecord) : bib.getOwningInstitutionBibId();
    }

    private String getControlFieldValue001(BibRecord bibRecord) {
        RecordType marcRecord = bibRecord.getBib().getContent().getCollection().getRecord().get(0);
        return getMarcUtil().getControlFieldValue(marcRecord, "001");
    }

    public BibRecord getBibRecord() {
        return bibRecord;
    }

    public void setBibRecord(BibRecord bibRecord) {
        this.bibRecord = bibRecord;
    }

    public Map getInstitutionEntitiesMap() {
        return institutionEntitiesMap;
    }

    public void setInstitutionEntitiesMap(Map institutionEntitiesMap) {
        this.institutionEntitiesMap = institutionEntitiesMap;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public Map getItemStatusMap() {
        return itemStatusMap;
    }

    public void setItemStatusMap(Map itemStatusMap) {
        this.itemStatusMap = itemStatusMap;
    }

    public Map getCollectionGroupMap() {
        return collectionGroupMap;
    }

    public void setCollectionGroupMap(Map collectionGroupMap) {
        this.collectionGroupMap = collectionGroupMap;
    }

    public XmlRecordEntity getXmlRecordEntity() {
        return xmlRecordEntity;
    }

    public void setXmlRecordEntity(XmlRecordEntity xmlRecordEntity) {
        this.xmlRecordEntity = xmlRecordEntity;
    }

    public MarcUtil getMarcUtil() {
        if (null == marcUtil) {
            marcUtil = new MarcUtil();
        }
        return marcUtil;
    }

    public DBReportUtil getDBReportUtil() {
        return DBReportUtil;
    }

    public void setDBReportUtil(DBReportUtil DBReportUtil) {
        this.DBReportUtil = DBReportUtil;
    }
}