package org.recap.service.formatter.datadump;

import org.apache.commons.collections.CollectionUtils;
import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.recap.RecapConstants;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by premkb on 28/9/16.
 */
@Service
@Scope("prototype")
public class MarcXmlFormatterService implements DataDumpFormatterInterface {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MarcXmlFormatterService.class);

    @Value("${datadump.marc.pul}")
    private String holdingPUL;

    @Value("${datadump.marc.cul}")
    private String holdingCUL;

    @Value("${datadump.marc.nypl}")
    private String holdingNYPL;

    private MarcFactory factory;

    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(RecapConstants.DATADUMP_XML_FORMAT_MARC) ? true : false;
    }


    public Map<String, Object> prepareMarcRecords(List<BibliographicEntity> bibliographicEntities) {
        Map resultsMap = new HashMap();
        List<Record> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Iterator<BibliographicEntity> iterator = bibliographicEntities.iterator(); iterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = iterator.next();
            if(CollectionUtils.isNotEmpty(bibliographicEntity.getItemEntities())) {
                Map<String, Object> stringObjectMap = prepareMarcRecord(bibliographicEntity);
                Record record = (Record) stringObjectMap.get(RecapConstants.SUCCESS);
                if (null != record) {
                    records.add(record);
                }
                String failureMsg = (String) stringObjectMap.get(RecapConstants.FAILURE);
                if (null != failureMsg) {
                    errors.add(failureMsg);
                }
            }
        }

        resultsMap.put(RecapConstants.SUCCESS, records);
        resultsMap.put(RecapConstants.FAILURE, errors);

        return resultsMap;
    }

    public Map<String, Object> prepareMarcRecord(BibliographicEntity bibliographicEntity) {
        Record record = null;
        Map results = new HashMap();
        try {
            record = getRecordFromContent(bibliographicEntity.getContent());
            update001Field(record, bibliographicEntity);
            stripTagsFromBib(record,Arrays.asList(RecapConstants.MarcFields.DF_852,RecapConstants.MarcFields.DF_876));
            add009Field(record, bibliographicEntity);
            List<Integer> itemIds = getItemIds(bibliographicEntity);
            record = addHoldingInfo(record, bibliographicEntity.getHoldingsEntities(),itemIds);
            results.put(RecapConstants.SUCCESS, record);
        } catch (Exception e) {
            logger.error(RecapConstants.ERROR,e);
            results.put(RecapConstants.FAILURE, String.valueOf(e.getCause()));

        }
        return results;
    }

    private void stripTagsFromBib(Record record,List<String> tagList){
        for(Iterator<DataField> dataFieldIterator = record.getDataFields().iterator();dataFieldIterator.hasNext();) {
            DataField dataField = dataFieldIterator.next();
            for (String tag : tagList) {
                if (tag.equals(dataField.getTag())) {
                    dataFieldIterator.remove();
                }
            }
        }
    }

    private List<Integer> getItemIds(BibliographicEntity bibliographicEntity){
        List<Integer> itemIds = new ArrayList<>();
        List<ItemEntity> itemEntityList = bibliographicEntity.getItemEntities();
        for(ItemEntity itemEntity : itemEntityList){
            itemIds.add(itemEntity.getItemId());
        }
        return itemIds;
    }

    private Record getRecordFromContent(byte[] content) {
        MarcReader reader;
        Record record = null;
        InputStream inputStream = new ByteArrayInputStream(content);
        reader = new MarcXmlReader(inputStream);
        while (reader.hasNext()) {
            record = reader.next();
        }
        return record;
    }

    private void update001Field(Record record, BibliographicEntity bibliographicEntity) {
        boolean is001Available = false;
        for (ControlField controlField : record.getControlFields()) {
            if (RecapConstants.MarcFields.CF_001.equals(controlField.getTag())) {
                controlField.setData(RecapConstants.SCSB + "-" + bibliographicEntity.getBibliographicId());
                is001Available = true;
            }
        }
        if(!is001Available) {
            ControlField controlField = getFactory().newControlField(RecapConstants.MarcFields.CF_001);
            controlField.setData(RecapConstants.SCSB + "-" + bibliographicEntity.getBibliographicId());
            record.addVariableField(controlField);
        }
    }

    private void add009Field(Record record, BibliographicEntity bibliographicEntity){
        ControlField controlField = getFactory().newControlField(RecapConstants.MarcFields.CF_009);
        controlField.setData(bibliographicEntity.getOwningInstitutionBibId());
        record.addVariableField(controlField);
    }

    private Record addHoldingInfo(Record record, List<HoldingsEntity> holdingsEntityList,List<Integer> itemIds) {
        Record holdingRecord;
        for (HoldingsEntity holdingsEntity : holdingsEntityList) {
            holdingRecord = getRecordFromContent(holdingsEntity.getContent());
            for (DataField dataField : holdingRecord.getDataFields()) {
                if (RecapConstants.MarcFields.DF_852.equals(dataField.getTag())) {
                    add0SubField(dataField, holdingsEntity);
                    add852Subfield1(dataField, holdingsEntity);
                    update852bField(dataField, holdingsEntity);
                    record.addVariableField(dataField);
                }
                if (RecapConstants.MarcFields.DF_866.equals(dataField.getTag())) {
                    if(dataField.getSubfield('a')!=null && (dataField.getSubfield('a').getData()==null || "".equals(dataField.getSubfield('a').getData()))){
                        continue;
                    }else {
                        add0SubField(dataField, holdingsEntity);
                        record.addVariableField(dataField);
                    }
                }
            }
            for(ItemEntity itemEntity : holdingsEntity.getItemEntities()){
                if(itemIds.contains(itemEntity.getItemId())) {
                    record = addItemInfo(record, itemEntity, holdingsEntity);
                }
            }
        }
        return record;
    }

    private void add0SubField(DataField dataField, HoldingsEntity holdingEntity) {
        dataField.addSubfield(getFactory().newSubfield('0', holdingEntity.getHoldingsId().toString()));
    }

    private void add852Subfield1(DataField dataField, HoldingsEntity holdingEntity){
        dataField.addSubfield(getFactory().newSubfield('1', holdingEntity.getOwningInstitutionHoldingsId()));
    }

    private void update852bField(DataField dataField, HoldingsEntity holdingEntity){
        if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(RecapConstants.PRINCETON)) {
            dataField.getSubfield('b').setData(holdingPUL);
        } else if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(RecapConstants.COLUMBIA)) {
            dataField.getSubfield('b').setData(holdingCUL);
        } else if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(RecapConstants.NYPL)) {
            dataField.getSubfield('b').setData(holdingNYPL);
        }
    }

    private Record addItemInfo(Record record, ItemEntity itemEntity,HoldingsEntity holdingsEntity) {
        DataField dataField = getFactory().newDataField(RecapConstants.MarcFields.DF_876, ' ', ' ');
        dataField.addSubfield(getFactory().newSubfield('0', String.valueOf(holdingsEntity.getHoldingsId())));
        dataField.addSubfield(getFactory().newSubfield('1', holdingsEntity.getOwningInstitutionHoldingsId()));
        dataField.addSubfield(getFactory().newSubfield('2', itemEntity.getOwningInstitutionItemId()));
        dataField.addSubfield(getFactory().newSubfield('3', itemEntity.getVolumePartYear() != null ? itemEntity.getVolumePartYear() : ""));
        dataField.addSubfield(getFactory().newSubfield('a', String.valueOf(itemEntity.getItemId())));
        dataField.addSubfield(getFactory().newSubfield('h', itemEntity.getUseRestrictions() != null ? itemEntity.getUseRestrictions() : ""));
        dataField.addSubfield(getFactory().newSubfield('j', itemEntity.getItemStatusEntity().getStatusCode()));
        dataField.addSubfield(getFactory().newSubfield('p', itemEntity.getBarcode()));
        dataField.addSubfield(getFactory().newSubfield('t', itemEntity.getCopyNumber() != null ? String.valueOf(itemEntity.getCopyNumber()) : ""));
        dataField.addSubfield(getFactory().newSubfield('x', itemEntity.getCollectionGroupEntity().getCollectionGroupCode()));
        dataField.addSubfield(getFactory().newSubfield('z', itemEntity.getCustomerCode()));
        record.addVariableField(dataField);

        return record;
    }

    public String covertToMarcXmlString(List<Record> recordList) throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        MarcWriter writer = new MarcXmlWriter(out, "UTF-8", true);

        recordList.forEach(writer::write);
        writer.close();

        return out.toString();
    }

    public MarcFactory getFactory() {
        if (null == factory) {
            factory = MarcFactory.newInstance();
        }
        return factory;
    }
}
