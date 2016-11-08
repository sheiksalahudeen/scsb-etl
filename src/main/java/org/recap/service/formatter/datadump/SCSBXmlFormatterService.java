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

    @Override
    public Object getFormattedOutput(List<BibliographicEntity> bibliographicEntityList){
        Map<String,Object> successAndFailureFormattedList= new HashMap<>();
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        String formatError = null;
        BibRecords bibRecords = new BibRecords();
        List<BibRecord> bibRecordList = new ArrayList<>();
        try {
            if (!CollectionUtils.isEmpty(bibliographicEntityList)) {
                for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
                    BibRecord bibRecord = getBibRecord(bibliographicEntity);
                    bibRecordList.add(bibRecord);
                    successList.add(bibliographicEntity);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            formatError = e.getMessage();
        }
        bibRecords.setBibRecords(bibRecordList);
        String formattedString = null;
        try {
            formattedString = convertToXml(bibRecords);
        } catch (Exception e) {
            e.printStackTrace();
            formatError = e.getMessage();
        }
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_SUCCESSLIST,successList);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FAILURELIST,failureList);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FORMATTEDSTRING,formattedString);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FORMATERROR,formatError);
        return successAndFailureFormattedList;
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

    public void prepareBibRecords(List<BibliographicEntity> successList, List<BibliographicEntity> failureList, List<BibRecord> records, List<BibliographicEntity> bibliographicEntities) {
        for (Iterator<BibliographicEntity> bibliographicEntityIterator = bibliographicEntities.iterator(); bibliographicEntityIterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = bibliographicEntityIterator.next();
            BibRecord bibRecord = getBibRecord(bibliographicEntity);
            records.add(bibRecord);
        }
    }
}
