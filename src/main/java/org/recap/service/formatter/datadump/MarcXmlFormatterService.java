package org.recap.service.formatter.datadump;

import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
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

    private Logger logger = org.slf4j.LoggerFactory.getLogger(MarcXmlFormatterService.class);

    @Value("${datadump.marc.pul}")
    private String holdingPUL;

    @Value("${datadump.marc.cul}")
    private String holdingCUL;

    @Value("${datadump.marc.nypl}")
    private String holdingNYPL;
    private MarcFactory factory;

    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(ReCAPConstants.DATADUMP_XML_FORMAT_MARC) ? true : false;
    }


    public Map<String, Object> prepareMarcRecords(List<BibliographicEntity> bibliographicEntities) {
        Map resultsMap = new HashMap();
        List<Record> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Iterator<BibliographicEntity> iterator = bibliographicEntities.iterator(); iterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = iterator.next();
            Map<String, Object> stringObjectMap = prepareMarcRecord(bibliographicEntity);

            Record record = (Record) stringObjectMap.get(ReCAPConstants.SUCCESS);

            if (null != record) {
                records.add(record);
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

    public Map<String, Object> prepareMarcRecord(BibliographicEntity bibliographicEntity) {
        Record record = null;
        Map results = new HashMap();
        try {
            record = getRecordFromContent(bibliographicEntity.getContent());
            update001Field(record, bibliographicEntity);
            record = addHoldingInfo(record, bibliographicEntity.getHoldingsEntities());
            record = addItemInfo(record, bibliographicEntity.getItemEntities());
            results.put(ReCAPConstants.SUCCESS, record);
        } catch (Exception e) {
            logger.error(e.getMessage());
            results.put(ReCAPConstants.FAILURE, String.valueOf(e.getCause()));

        }
        return results;
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
        for (ControlField controlField : record.getControlFields()) {
            if (controlField.getTag().equals("001")) {
                controlField.setData(ReCAPConstants.SCSB + "-" + bibliographicEntity.getBibliographicId());
            }
        }
    }

    private Record addHoldingInfo(Record record, List<HoldingsEntity> holdingsEntityList) {
        Record holdingRecord = null;
        boolean is852Added = false;
        boolean is866Added = false;
        boolean is876Added = false;
        for (HoldingsEntity holdingsEntity : holdingsEntityList) {
            holdingRecord = getRecordFromContent(holdingsEntity.getContent());
            for (DataField dataField : holdingRecord.getDataFields()) {
                if (dataField.getTag().equals("852")) {
                    add852aField(dataField, holdingsEntity);
                    is852Added = true;
                }
                if (dataField.getTag().equals("866")) {
                    add8660Field(dataField, holdingsEntity);
                    is866Added = true;
                }
                if (dataField.getTag().equals("876")) {
                    add8760Field(dataField, holdingsEntity);
                    is876Added = true;
                }
                record.addVariableField(dataField);
            }
            if (!is852Added) {
                record = add852aField(record, holdingsEntity);
            }
            if (!is866Added) {
                record = add8660Field(record, holdingsEntity);
            }
            if (!is876Added) {
                record = add8760Field(record, holdingsEntity);
            }
        }
        return record;
    }

    private Record add852aField(Record record, HoldingsEntity holdingEntity) {
        DataField dataField = getFactory().newDataField("852", ' ', ' ');
        if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.PRINCETON)) {
            dataField.addSubfield(getFactory().newSubfield('a', holdingPUL));
        } else if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.COLUMBIA)) {
            dataField.addSubfield(getFactory().newSubfield('a', holdingCUL));
        } else if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.NYPL)) {
            dataField.addSubfield(getFactory().newSubfield('a', holdingNYPL));
        }
        record.addVariableField(dataField);
        return record;
    }

    private void add852aField(DataField dataField, HoldingsEntity holdingEntity) {
        if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.PRINCETON)) {
            dataField.addSubfield(getFactory().newSubfield('a', holdingPUL));
        } else if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.COLUMBIA)) {
            dataField.addSubfield(getFactory().newSubfield('a', holdingCUL));
        } else if (holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.NYPL)) {
            dataField.addSubfield(getFactory().newSubfield('a', holdingNYPL));
        }
    }

    private Record add8660Field(Record record, HoldingsEntity holdingsEntity) {
        DataField dataField = getFactory().newDataField("866", ' ', ' ');
        dataField.addSubfield(getFactory().newSubfield('0', holdingsEntity.getHoldingsId().toString()));
        record.addVariableField(dataField);
        return record;
    }

    private void add8660Field(DataField dataField, HoldingsEntity holdingEntity) {
        dataField.addSubfield(getFactory().newSubfield('0', holdingEntity.getHoldingsId().toString()));
    }

    private Record add8760Field(Record record, HoldingsEntity holdingEntity) {
        DataField dataField = getFactory().newDataField("876", ' ', ' ');
        dataField.addSubfield(getFactory().newSubfield('0', holdingEntity.getHoldingsId().toString()));
        record.addVariableField(dataField);
        return record;
    }

    private void add8760Field(DataField dataField, HoldingsEntity holdingEntity) {
        dataField.addSubfield(getFactory().newSubfield('0', holdingEntity.getHoldingsId().toString()));
    }

    private void add876xField(Record record, String collectionGroupCode) {
        DataField dataField = getFactory().newDataField("876", ' ', ' ');
        dataField.addSubfield(getFactory().newSubfield('x', collectionGroupCode));
        record.addVariableField(dataField);
    }

    private Record addItemInfo(Record record, List<ItemEntity> itemEntityList) {
        boolean is876Added = false;
        for (ItemEntity itemEntity : itemEntityList) {
            for (DataField dataField : record.getDataFields()) {
                if (dataField.getTag().equals("876")) {
                    dataField.addSubfield(getFactory().newSubfield('a', itemEntity.getOwningInstitutionItemId()));
                    dataField.addSubfield(getFactory().newSubfield('h', itemEntity.getUseRestrictions() != null ? itemEntity.getUseRestrictions() : ""));
                    dataField.addSubfield(getFactory().newSubfield('j', itemEntity.getItemStatusEntity().getStatusCode()));
                    dataField.addSubfield(getFactory().newSubfield('p', itemEntity.getBarcode()));
                    dataField.addSubfield(getFactory().newSubfield('t', itemEntity.getCopyNumber() != null ? String.valueOf(itemEntity.getCopyNumber()) : ""));
                    dataField.addSubfield(getFactory().newSubfield('x', itemEntity.getCollectionGroupEntity().getCollectionGroupCode()));
                    is876Added = true;
                }
            }
            if (!is876Added) {
                add876xField(record, itemEntity.getCollectionGroupEntity().getCollectionGroupCode());
            }
        }
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
