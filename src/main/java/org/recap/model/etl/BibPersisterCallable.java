package org.recap.model.etl;

import org.apache.commons.lang3.StringUtils;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.util.LoadReportUtil;
import org.recap.util.MarcUtil;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * Created by pvsubrah on 6/24/16.
 */
public class BibPersisterCallable implements Callable {

    private MarcUtil marcUtil;
    private LoadReportUtil loadReportUtil;
    private BibRecord bibRecord;

    private final Map institutionEntitiesMap;
    private final Map itemStatusMap;
    private final Map collectionGroupMap;

    public BibPersisterCallable(BibRecord bibRecord, Map institutionEntitiesMap, Map itemStatusMap, Map collectionGroupMap) {
        this.bibRecord = bibRecord;
        this.institutionEntitiesMap = institutionEntitiesMap;
        this.itemStatusMap = itemStatusMap;
        this.collectionGroupMap = collectionGroupMap;
    }

    @Override
    public Object call() {
        Map<String, Object> map = new HashMap<>();

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();

        Bib bib = bibRecord.getBib();
        String owningInstitutionBibId = getOwningInstitutionBibId(bibRecord, bib);
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        Integer owningInstitutionId = (Integer) institutionEntitiesMap.get(bib.getOwningInstitutionId());
        bibliographicEntity.setOwningInstitutionId(owningInstitutionId);
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setLastUpdatedBy("etl");
        ContentType bibContent = bib.getContent();

        CollectionType bibContentCollection = bibContent.getCollection();
        String bibXmlContent = bibContentCollection.serialize(bibContentCollection);
        bibliographicEntity.setContent(bibXmlContent);

        List<Holdings> holdings = bibRecord.getHoldings();
        for (Iterator<Holdings> iterator = holdings.iterator(); iterator.hasNext(); ) {
            Holdings holdingsList = iterator.next();
            List<Holding> holding = holdingsList.getHolding();
            for (Iterator<Holding> holdingIterator = holding.iterator(); holdingIterator.hasNext(); ) {
                Holding holdingEnt = holdingIterator.next();
                HoldingsEntity holdingsEntity = new HoldingsEntity();
                if (holdingEnt.getContent() != null) {
                    CollectionType holdingContentCollection = holdingEnt.getContent().getCollection();
                    List<RecordType> holdingRecordTypes = holdingContentCollection.getRecord();
                    RecordType holdingsRecordType = holdingRecordTypes.get(0);
                    holdingsEntity.setContent(holdingContentCollection.serialize(holdingContentCollection));
                    holdingsEntity.setCreatedDate(new Date());
                    holdingsEntity.setCreatedBy("etl");
                    holdingsEntity.setLastUpdatedDate(new Date());
                    holdingsEntity.setLastUpdatedBy("etl");
                    String owningInstituionHoldingsId = null;
                    if (StringUtils.isNotBlank(holdingEnt.getOwningInstitutionHoldingsId())) {
                        if (holdingEnt.getOwningInstitutionHoldingsId().length() > 45) {
                            owningInstituionHoldingsId = bibRecord.getBib().getOwningInstitutionId() + "-" + bibRecord.getBib().getOwningInstitutionBibId() + "-" + UUID.randomUUID().toString();
                        } else {
                            owningInstituionHoldingsId= bibRecord.getBib().getOwningInstitutionId() + bibRecord.getBib().getOwningInstitutionBibId() + "-" + holdingEnt.getOwningInstitutionHoldingsId();
                        }
                    } else {
                        owningInstituionHoldingsId = bibRecord.getBib().getOwningInstitutionId() + "-" + bibRecord.getBib().getOwningInstitutionBibId() + "-" + UUID.randomUUID().toString();
                    }

                    holdingsEntity.setOwningInstitutionHoldingsId(owningInstituionHoldingsId);
                    holdingsEntities.add(holdingsEntity);

                    String holdingsCallNumber = getMarcUtil().getDataFieldValue(holdingsRecordType, "852", null, null, "h");
                    String holdingsCallNumberType = getMarcUtil().getInd1(holdingsRecordType, "852", "h");

                    List<Items> items = holdingEnt.getItems();
                    for (Items item : items) {
                        ContentType itemContent = item.getContent();
                        CollectionType itemContentCollection = itemContent.getCollection();

                        List<RecordType> itemRecordTypes = itemContentCollection.getRecord();
                        for (RecordType itemRecordType : itemRecordTypes) {
                            ItemEntity itemEntity = new ItemEntity();
                            itemEntity.setBarcode(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "p"));
                            itemEntity.setCustomerCode(getMarcUtil().getDataFieldValue(itemRecordType, "900", null, null, "b"));
                            itemEntity.setCallNumber(holdingsCallNumber);
                            itemEntity.setCallNumberType(holdingsCallNumberType);
                            String itemStatusValue = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "j");
                            if (StringUtils.isNotBlank(itemStatusValue) && itemStatusMap.containsKey(itemStatusValue)) {
                                itemEntity.setItemAvailabilityStatusId((Integer) itemStatusMap.get(itemStatusValue));
                            } else {
                                itemEntity.setItemAvailabilityStatusId((Integer) itemStatusMap.get("Available"));
                            }
                            String copyNumber = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "t");
                            if (org.apache.commons.lang3.StringUtils.isNoneBlank(copyNumber) && org.apache.commons.lang3.math.NumberUtils.isNumber(copyNumber)) {
                                itemEntity.setCopyNumber(Integer.valueOf(copyNumber));
                            }
                            itemEntity.setOwningInstitutionId(owningInstitutionId);
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
                            itemEntity.setOwningInstitutionItemId(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "a"));
                            itemEntity.setHoldingsEntity(holdingsEntity);
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

        LoadReportEntity loadReportEntity = validateEntity(bibliographicEntity);
        if (loadReportEntity != null) {
            map.put("loadReportEntity", loadReportEntity);
        } else {
            map.put("bibliographicEntity", bibliographicEntity);
        }

        return map;
    }

    private LoadReportEntity validateEntity(BibliographicEntity bibliographicEntity) {
        LoadReportEntity loadReportEntity = getLoadReportUtil().validateBibliographicEntity(bibliographicEntity);
        if (loadReportEntity == null) {
            loadReportEntity = getLoadReportUtil().validateHoldingsEntities(bibliographicEntity);
            if (loadReportEntity == null) {
                loadReportEntity = getLoadReportUtil().validateItemEntities(bibliographicEntity);
            }
        }
        return loadReportEntity;
    }

    private String getOwningInstitutionBibId(BibRecord bibRecord, Bib bib) {
        return null == bib.getOwningInstitutionBibId() ? getControlFieldValue001(bibRecord) : bib.getOwningInstitutionBibId();
    }

    private String getControlFieldValue001(BibRecord bibRecord) {
        RecordType marcRecord = bibRecord.getBib().getContent().getCollection().getRecord().get(0);
        return getMarcUtil().getControlFieldValue(marcRecord, "001");
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
