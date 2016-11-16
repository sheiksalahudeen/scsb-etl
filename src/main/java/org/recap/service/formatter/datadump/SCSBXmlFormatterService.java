package org.recap.service.formatter.datadump;

import org.recap.ReCAPConstants;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.*;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBException;
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
    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(ReCAPConstants.DATADUMP_XML_FORMAT_SCSB) ? true:false;
    }

    public String getSCSBXmlForBibRecords(List<BibRecord> bibRecords){
        String formattedString = null;
        try {
            BibRecords bibRecords1 = new BibRecords();
            bibRecords1.setBibRecords(bibRecords);
            formattedString = convertToXml(bibRecords1);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return formattedString;
    }

    private String convertToXml(BibRecords bibRecords){
        StringWriter stringWriter = new StringWriter();
        try {
            Marshaller jaxbMarshaller = JAXBContextHandler.getInstance().getJAXBContextForClass(BibRecords.class).createMarshaller();
            synchronized (jaxbMarshaller) {
                jaxbMarshaller.marshal(bibRecords, stringWriter);
            }
        } catch (JAXBException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

    public Map<String, Object> prepareBibRecords(List<BibliographicEntity> bibliographicEntities) {
        Map resultsMap = new HashMap();
        List<BibRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Iterator<BibliographicEntity> bibliographicEntityIterator = bibliographicEntities.iterator(); bibliographicEntityIterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = bibliographicEntityIterator.next();
            Map<String, Object> stringObjectMap = prepareBibRecord(bibliographicEntity);
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

    private Map<String, Object> prepareBibRecord(BibliographicEntity bibliographicEntity) {
        BibRecord bibRecord = null;
        Map results = new HashMap();
        try {
            Bib bib = getBib(bibliographicEntity);
            List<Holdings> holdings = getHoldings(bibliographicEntity.getHoldingsEntities());
            bibRecord = new BibRecord();
            bibRecord.setBib(bib);
            bibRecord.setHoldings(holdings);
            results.put(ReCAPConstants.SUCCESS, bibRecord);
        } catch (Exception e) {
            logger.error(e.getMessage());
            results.put(ReCAPConstants.FAILURE, e.getMessage());
        }
        return results;
    }

    private Bib getBib(BibliographicEntity bibliographicEntity) {
        Bib bib = new Bib();
        bib.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        bib.setOwningInstitutionId(bibliographicEntity.getInstitutionEntity().getInstitutionCode());
        ContentType contentType = getContentType(bibliographicEntity.getContent());
        List<RecordType> record = contentType.getCollection().getRecord();
        RecordType recordType = record.get(0);
        String value = recordType.getControlfield().get(0).getValue();
        value = ReCAPConstants.SCSB+"-"+value;
        recordType.getControlfield().get(0).setValue(value);
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
                if(holdingsEntity.getItemEntities()!=null && !isHoldingSingleItemPrivate(holdingsEntity.getItemEntities())) {
                    Items items = getItems(holdingsEntity.getItemEntities());
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
