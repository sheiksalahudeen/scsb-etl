package org.recap.util;

import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.*;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenchulakshmig on 5/8/16.
 */
public class DataDumpUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpUtil.class);
    public BibRecords getBibRecords(List<BibliographicEntity> bibliographicEntities) {
        BibRecords bibRecords = new BibRecords();
        List<BibRecord> bibRecordList = getBibRecordList(bibliographicEntities);
        bibRecords.setBibRecords(bibRecordList);
        return bibRecords;
    }

    private List<BibRecord> getBibRecordList(List<BibliographicEntity> bibliographicEntities) {
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

    private BibRecord getBibRecord(BibliographicEntity bibliographicEntity) {
        BibRecord bibRecord = new BibRecord();
        Bib bib = getBib(bibliographicEntity);
        bibRecord.setBib(bib);
        List<Holdings> holdings = getHoldings(bibliographicEntity.getHoldingsEntities());
        bibRecord.setHoldings(holdings);
        return bibRecord;
    }

    private Bib getBib(BibliographicEntity bibliographicEntity) {
        Bib bib = new Bib();
        bib.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        bib.setOwningInstitutionId(bibliographicEntity.getInstitutionEntity().getInstitutionCode());
        ContentType contentType = getContentType(bibliographicEntity.getContent());
        bib.setContent(contentType);
        return bib;
    }

    private List<Holdings> getHoldings(List<HoldingsEntity> holdingsEntityList) {
        List<Holdings> holdingsList = new ArrayList<>();
        if (holdingsEntityList!=null && !CollectionUtils.isEmpty(holdingsEntityList)) {
            for (HoldingsEntity holdingsEntity : holdingsEntityList) {
                Holdings holdings = new Holdings();
                Holding holding = new Holding();
                holding.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());
                ContentType contentType = getContentType(holdingsEntity.getContent());
                holding.setContent(contentType);
                if(holdingsEntity.getItemEntities()!=null) {
                    Items items = getItems(holdingsEntity.getItemEntities());
                    holding.setItems(Arrays.asList(items));
                }
                holdings.setHolding(Arrays.asList(holding));
                holdingsList.add(holdings);
            }
        }
        return holdingsList;
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

    private ContentType getContentType(byte[] byteContent) {
        String content = new String(byteContent, Charset.forName("UTF-8"));
        CollectionType collectionType = null;
        try {
            collectionType = (CollectionType) JAXBHandler.getInstance().unmarshal(content, CollectionType.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        ContentType contentType = new ContentType();
        contentType.setCollection(collectionType);
        return contentType;
    }

}
