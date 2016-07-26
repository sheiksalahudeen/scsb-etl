package org.recap.model.etl;

import org.apache.commons.lang3.StringUtils;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;
import org.recap.model.jaxb.marc.LeaderFieldType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.util.LoadReportUtil;
import org.recap.util.MarcUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * Created by pvsubrah on 6/24/16.
 */
public class BibPersisterCallable implements Callable {

    private MarcUtil marcUtil;
    private LoadReportUtil loadReportUtil;
    private BibRecord bibRecord;
    private XmlRecordEntity xmlRecordEntity;

    private Map institutionEntitiesMap;
    private Map itemStatusMap;
    private Map collectionGroupMap;

    @Override
    public Object call() {
        Map<String, Object> map = new HashMap<>();

        List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = new ArrayList<>();
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();

        Integer owningInstitutionId = (Integer) institutionEntitiesMap.get(bibRecord.getBib().getOwningInstitutionId());
        Map<String, Object> bibMap = processAndValidateBibliographicEntity(owningInstitutionId);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) bibMap.get("bibliographicEntity");
        FailureReportReCAPCSVRecord bibFailureReportReCAPCSVRecord= (FailureReportReCAPCSVRecord) bibMap.get("failureReportReCAPCSVRecord");
        if (bibFailureReportReCAPCSVRecord != null) {
            bibFailureReportReCAPCSVRecord.setFileName(xmlRecordEntity.getXmlFileName());
            failureReportReCAPCSVRecords.add(bibFailureReportReCAPCSVRecord);
        }

        List<Holdings> holdings = bibRecord.getHoldings();
        for (Iterator<Holdings> iterator = holdings.iterator(); iterator.hasNext(); ) {
            Holdings holdingsList = iterator.next();
            List<Holding> holding = holdingsList.getHolding();
            for (Iterator<Holding> holdingIterator = holding.iterator(); holdingIterator.hasNext(); ) {
                Holding holdingEnt = holdingIterator.next();
                if (holdingEnt.getContent() != null) {
                    CollectionType holdingContentCollection = holdingEnt.getContent().getCollection();
                    List<RecordType> holdingRecordTypes = holdingContentCollection.getRecord();
                    RecordType holdingsRecordType = holdingRecordTypes.get(0);

                    Map<String, Object> holdingsMap = processAndValidateHoldingsEntity(bibliographicEntity, holdingEnt, holdingContentCollection);
                    HoldingsEntity holdingsEntity = (HoldingsEntity) holdingsMap.get("holdingsEntity");
                    FailureReportReCAPCSVRecord holdingsFailureReportReCAPCSVRecord = (FailureReportReCAPCSVRecord) holdingsMap.get("failureReportReCAPCSVRecord");
                    if (holdingsFailureReportReCAPCSVRecord != null) {
                        failureReportReCAPCSVRecords.add(holdingsFailureReportReCAPCSVRecord);
                    }

                    holdingsEntities.add(holdingsEntity);
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
                            FailureReportReCAPCSVRecord itemFailureReportReCAPCSVRecord = (FailureReportReCAPCSVRecord) itemMap.get("failureReportReCAPCSVRecord");
                            if (itemFailureReportReCAPCSVRecord != null) {
                                failureReportReCAPCSVRecords.add(itemFailureReportReCAPCSVRecord);
                            }

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
        bibliographicEntity.setHoldingsEntities(holdingsEntities);
        bibliographicEntity.setItemEntities(itemEntities);

        if (CollectionUtils.isEmpty(failureReportReCAPCSVRecords)) {
            map.put("bibliographicEntity", bibliographicEntity);
        } else {
            map.put("failureReportReCAPCSVRecord", failureReportReCAPCSVRecords);
        }
        return map;
    }

    private Map<String, Object> processAndValidateBibliographicEntity(Integer owningInstitutionId) {
        Map<String, Object> map = new HashMap<>();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = null;
        StringBuffer errorMessage = new StringBuffer();

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

        LeaderFieldType leader = bibContentCollection.getRecord().get(0).getLeader();
        if (!(leader != null && StringUtils.isNotBlank(leader.getValue()) && leader.getValue().length() == 24)) {
            errorMessage.append("\n");
            errorMessage.append("Leader Field value should be 24 characters");
        }

        if (errorMessage.toString().length() > 1) {
            failureReportReCAPCSVRecord = getLoadReportUtil().populateBibInfo(bibliographicEntity);
            failureReportReCAPCSVRecord.setErrorDescription(errorMessage.toString());
        }
        map.put("bibliographicEntity", bibliographicEntity);
        map.put("failureReportReCAPCSVRecord", failureReportReCAPCSVRecord);
        return map;
    }

    private Map<String, Object> processAndValidateHoldingsEntity(BibliographicEntity bibliographicEntity, Holding holdingEnt, CollectionType holdingContentCollection) {
        StringBuffer errorMessage = new StringBuffer();
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = null;
        Map<String, Object> map = new HashMap<>();
        HoldingsEntity holdingsEntity = new HoldingsEntity();

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
        String owningInstituionHoldingsId = null;
        if (StringUtils.isNotBlank(holdingEnt.getOwningInstitutionHoldingsId())) {
            if (holdingEnt.getOwningInstitutionHoldingsId().length() > 45) {
                owningInstituionHoldingsId = bibRecord.getBib().getOwningInstitutionId() + "-" + bibRecord.getBib().getOwningInstitutionBibId() + "-" + UUID.randomUUID().toString();
            } else {
                owningInstituionHoldingsId = bibRecord.getBib().getOwningInstitutionId() + bibRecord.getBib().getOwningInstitutionBibId() + "-" + holdingEnt.getOwningInstitutionHoldingsId();
            }
        } else {
            owningInstituionHoldingsId = bibRecord.getBib().getOwningInstitutionId() + "-" + bibRecord.getBib().getOwningInstitutionBibId() + "-" + UUID.randomUUID().toString();
        }

        holdingsEntity.setOwningInstitutionHoldingsId(owningInstituionHoldingsId);
        if (errorMessage.toString().length() > 1) {
            failureReportReCAPCSVRecord = getLoadReportUtil().populateBibHoldingsInfo(bibliographicEntity, holdingsEntity);
            failureReportReCAPCSVRecord.setErrorDescription(errorMessage.toString());
        }
        map.put("holdingsEntity", holdingsEntity);
        map.put("failureReportReCAPCSVRecord", failureReportReCAPCSVRecord);
        return map;
    }

    private Map<String, Object> processAndValidateItemEntity(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity, Integer owningInstitutionId, String holdingsCallNumber, String holdingsCallNumberType, RecordType itemRecordType) {
        StringBuffer errorMessage = new StringBuffer();
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = null;
        Map<String, Object> map = new HashMap<>();
        ItemEntity itemEntity = new ItemEntity();

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
        itemEntity.setUseRestrictions(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "h"));
        itemEntity.setVolumePartYear(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "3"));
        String owningInstitutionItemId = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "a");
        if (StringUtils.isNotBlank(owningInstitutionItemId)) {
            itemEntity.setOwningInstitutionItemId(owningInstitutionItemId);
        } else {
            errorMessage.append("\n");
            errorMessage.append("Item Owning Institution Id cannot be null");
        }
        itemEntity.setHoldingsEntity(holdingsEntity);

        if (errorMessage.toString().length() > 1) {
            failureReportReCAPCSVRecord = getLoadReportUtil().populateBibHoldingsItemInfo(bibliographicEntity, itemEntity.getHoldingsEntity(), itemEntity);
            failureReportReCAPCSVRecord.setErrorDescription(errorMessage.toString());
        }
        map.put("itemEntity", itemEntity);
        map.put("failureReportReCAPCSVRecord", failureReportReCAPCSVRecord);
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

    public LoadReportUtil getLoadReportUtil() {
        if (null == loadReportUtil) {
            loadReportUtil = new LoadReportUtil(institutionEntitiesMap, collectionGroupMap);
        }
        return loadReportUtil;
    }
}
