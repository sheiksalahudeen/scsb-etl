package org.recap.util;

import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.*;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenchulakshmig on 5/8/16.
 */
public class DataDumpUtil {

    public BibRecords getBibRecords(List<BibliographicEntity> bibliographicEntities) {
        BibRecords bibRecords = new BibRecords();
        List<BibRecord> bibRecordList = getBibRecordList(bibliographicEntities);
        bibRecords.setBibRecords(bibRecordList);
        return bibRecords;
    }

    public List<BibRecord> getBibRecordList(List<BibliographicEntity> bibliographicEntities) {
        List<BibRecord> bibRecordList = new ArrayList<>();
        BibRecord bibRecord = null;
        if (!CollectionUtils.isEmpty(bibliographicEntities)) {
            for (BibliographicEntity bibliographicEntity : bibliographicEntities) {
                bibRecord = getBibRecord(bibliographicEntity);
                bibRecordList.add(bibRecord);
            }
        }
        return bibRecordList;
    }

    public BibRecord getBibRecord(BibliographicEntity bibliographicEntity) {
        BibRecord bibRecord = new BibRecord();
        Bib bib = getBib(bibliographicEntity);
        bibRecord.setBib(bib);
        List<Holdings> holdings = getHoldings(bibliographicEntity.getHoldingsEntities());
        bibRecord.setHoldings(holdings);
        return bibRecord;
    }

    public Bib getBib(BibliographicEntity bibliographicEntity) {
        Bib bib = new Bib();
        bib.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        bib.setOwningInstitutionId(bibliographicEntity.getInstitutionEntity().getInstitutionCode());
        ContentType contentType = getContentType(bibliographicEntity.getContent());
        bib.setContent(contentType);
        return bib;
    }

    public List<Holdings> getHoldings(List<HoldingsEntity> holdingsEntityList) {
        List<Holdings> holdingsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(holdingsEntityList)) {
            for (HoldingsEntity holdingsEntity : holdingsEntityList) {
                Holdings holdings = new Holdings();
                Holding holding = new Holding();
                holding.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());
                ContentType contentType = getContentType(holdingsEntity.getContent());
                holding.setContent(contentType);
                Items items = getItems(holdingsEntity.getItemEntities());
                holding.setItems(Arrays.asList(items));
                holdings.setHolding(Arrays.asList(holding));
                holdingsList.add(holdings);
            }
        }
        return holdingsList;
    }

    public Items getItems(List<ItemEntity> itemEntities) {
        Items items = new Items();
        ContentType itemContentType = new ContentType();
        CollectionType collectionType = new CollectionType();
        collectionType.setRecord(buildRecordTypes(itemEntities));
        itemContentType.setCollection(collectionType);
        items.setContent(itemContentType);
        return items;
    }

    public List<RecordType> buildRecordTypes(List<ItemEntity> itemEntities) {
        List<RecordType> recordTypes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(itemEntities)) {
            for (ItemEntity itemEntity : itemEntities) {
                RecordType recordType = new RecordType();
                List<DataFieldType> dataFieldTypeList = new ArrayList<>();
                dataFieldTypeList.add(build876DataField(itemEntity));
                dataFieldTypeList.add(build900DataField(itemEntity));
                recordType.setDatafield(dataFieldTypeList);
                recordTypes.add(recordType);
            }
        }
        return recordTypes;
    }

    public DataFieldType build900DataField(ItemEntity itemEntity) {
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

    public DataFieldType build876DataField(ItemEntity itemEntity) {
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

    public SubfieldatafieldType getSubfieldatafieldType(String code, String value) {
        SubfieldatafieldType subfieldatafieldType = new SubfieldatafieldType();
        subfieldatafieldType.setCode(code);
        subfieldatafieldType.setValue(value);
        return subfieldatafieldType;
    }

    public ContentType getContentType(byte[] byteContent) {
        String content = new String(byteContent, Charset.forName("UTF-8"));
        CollectionType collectionType = (CollectionType) JAXBHandler.getInstance().unmarshal(content, CollectionType.class);
        ContentType contentType = new ContentType();
        contentType.setCollection(collectionType);
        return contentType;
    }

}
