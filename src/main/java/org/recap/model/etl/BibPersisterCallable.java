package org.recap.model.etl;

import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.ContentType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.util.MarcUtil;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * Created by pvsubrah on 6/24/16.
 */
public class BibPersisterCallable implements Callable {

    private MarcUtil marcUtil;
    private BibRecord bibRecord;

    private final Map institutionEntitiesMap;
    private final Map itemStatusMap;

    public BibPersisterCallable(BibRecord bibRecord, Map institutionEntitiesMap, Map itemStatusMap) {
        this.bibRecord = bibRecord;
        this.institutionEntitiesMap = institutionEntitiesMap;
        this.itemStatusMap = itemStatusMap;

    }

    @Override
    public Object call() throws Exception {

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();

        bibliographicEntity.setCreatedDate(new Date());
        Bib bib = bibRecord.getBib();
        String owningInstitutionBibId = getOwningInstitutionBibId(bibRecord, bib);
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        Integer owningInstitutionId = (Integer) institutionEntitiesMap.get(bib.getOwningInstitutionId());
        bibliographicEntity.setOwningInstitutionId(owningInstitutionId);//TODO need to update
        bibliographicEntity.setCreatedDate(new Date());
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
                CollectionType holdingContentCollection = holdingEnt.getContent().getCollection();
                List<RecordType> holdingRecordTypes = holdingContentCollection.getRecord();
                RecordType holdingsRecordType = holdingRecordTypes.get(0);
                holdingsEntity.setContent(holdingContentCollection.serialize(holdingContentCollection));
                holdingsEntity.setCreatedDate(new Date());
                holdingsEntities.add(holdingsEntity);

                String holdingsCallNumber = getMarcUtil().getDataFieldValue(holdingsRecordType, "852", null, null, "h");
                String holdingsCallNumberType = getMarcUtil().getInd1(holdingsRecordType, "852", "h");

                List<Items> items = holdingEnt.getItems();
                for (Items item : items) {
                    ContentType itemContent = item.getContent();
                    CollectionType itemContentCollection = itemContent.getCollection();

                    List<RecordType> itemRrecordTypes = itemContentCollection.getRecord();
                    for (RecordType itemRecordType : itemRrecordTypes) {
                        ItemEntity itemEntity = new ItemEntity();
                        itemEntity.setBarcode(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "p"));
                        itemEntity.setCustomerCode(getMarcUtil().getDataFieldValue(itemRecordType, "900", null, null, "b"));
                        itemEntity.setCallNumber(holdingsCallNumber);
                        itemEntity.setCallNumberType(holdingsCallNumberType);
                        String itemStatusValue = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "j");
                        itemEntity.setItemAvailabilityStatusId("-".equals(itemStatusValue) ? 1 : (Integer) itemStatusMap.get(itemStatusValue));//TODO need to update
                        String copyNumber = getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "t");
                        if (org.apache.commons.lang3.StringUtils.isNoneBlank(copyNumber) && org.apache.commons.lang3.math.NumberUtils.isNumber(copyNumber)) {
                            itemEntity.setCopyNumber(Integer.valueOf(copyNumber));
                        }
                        itemEntity.setOwningInstitutionId(owningInstitutionId);//TODO need to update
                        String collectionGroupId = getMarcUtil().getDataFieldValue(itemRecordType, "900", null, null, "a");
                        if (org.apache.commons.lang3.StringUtils.isNoneBlank(collectionGroupId) && org.apache.commons.lang3.math.NumberUtils.isNumber(collectionGroupId)) {
                            itemEntity.setCollectionGroupId(Integer.valueOf(collectionGroupId));
                        } else {
                            itemEntity.setCollectionGroupId(2);
                        }
                        itemEntity.setCreatedDate(new Date());
                        itemEntity.setUseRestrictions(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "h"));
                        itemEntity.setVolumePartYear(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "3"));
                        itemEntity.setOwningInstitutionItemId(getMarcUtil().getDataFieldValue(itemRecordType, "876", null, null, "a"));

                        itemEntity.setHoldingsEntity(holdingsEntity);
                        itemEntities.add(itemEntity);
                    }
                }
            }
        }
        bibliographicEntity.setHoldingsEntities(holdingsEntities);
        bibliographicEntity.setItemEntities(itemEntities);

        return bibliographicEntity;
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
}
