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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 28/9/16.
 */
@Service
@Scope("prototype")
public class MarcXmlFormatterService implements DataDumpFormatterInterface{

    private Logger logger = org.slf4j.LoggerFactory.getLogger(MarcXmlFormatterService.class);

    @Value("${datadump.marc.pul}")
    private String holdingPUL;

    @Value ("${datadump.marc.cul}")
    private String holdingCUL;

    @Value ("${datadump.marc.nypl}")
    private String holdingNYPL;

    @Override
    public boolean isInterested(String formatType) {
        return formatType.equals(ReCAPConstants.DATADUMP_XML_FORMAT_MARC) ? true:false;
    }

    @Override
    public Object getFormattedOutput(List<BibliographicEntity> bibliographicEntityList) {
        Map<String,Object> successAndFailureFormattedList= new HashMap<>();
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        String formattedString = null;
        String formatError = null;
        List<Record> recordList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity : bibliographicEntityList){
            prepareBibEntity(successList, failureList, recordList, bibliographicEntity);
        }
        try {
            formattedString = covertToMarcXmlString(recordList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            formatError = e.getMessage();
        }
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_SUCCESSLIST,successList);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FAILURELIST,failureList);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FORMATTEDSTRING,formattedString);
        successAndFailureFormattedList.put(ReCAPConstants.DATADUMP_FORMATERROR,formatError);
        return successAndFailureFormattedList;
    }

    public void prepareBibEntity(List<BibliographicEntity> successList, List<BibliographicEntity> failureList, List<Record> recordList, BibliographicEntity bibliographicEntity) {
        try {
            Record record = getRecordFromContent(bibliographicEntity.getContent());
            update001Field(record,bibliographicEntity);
            record = addHoldingInfo(record,bibliographicEntity.getHoldingsEntities());
            record = addItemInfo(record,bibliographicEntity.getItemEntities());
            recordList.add(record);
            successList.add(bibliographicEntity);
            failureList.add(bibliographicEntity);
        } catch (Exception e) {
            logger.error(e.getMessage());
            failureList.add(bibliographicEntity);
        }
    }

    private Record getRecordFromContent(byte[] content){
        MarcReader reader;
        Record record=null;
        InputStream inputStream = new ByteArrayInputStream(content);
        reader = new MarcXmlReader(inputStream);
        while(reader.hasNext()){
            record = reader.next();
        }
        return record;
    }

    private void update001Field(Record record,BibliographicEntity bibliographicEntity){
        for(ControlField controlField:record.getControlFields()){
            if(controlField.getTag().equals("001")){
                controlField.setData(ReCAPConstants.SCSB+"-"+bibliographicEntity.getBibliographicId());
            }
        }
    }

    private Record addHoldingInfo(Record record,List<HoldingsEntity> holdingsEntityList){
        Record holdingRecord=null;
        boolean is852Added = false;
        boolean is866Added = false;
        boolean is876Added = false;
        for(HoldingsEntity holdingsEntity : holdingsEntityList){
            holdingRecord = getRecordFromContent(holdingsEntity.getContent());
            for(DataField dataField:holdingRecord.getDataFields()){
                if(dataField.getTag().equals("852")){
                    add852aField(dataField,holdingsEntity);
                    is852Added = true;
                }
                if(dataField.getTag().equals("866")){
                    add8660Field(dataField,holdingsEntity);
                    is866Added = true;
                }
                if(dataField.getTag().equals("876")){
                    add8760Field(dataField,holdingsEntity);
                    is876Added = true;
                }
                record.addVariableField(dataField);
            }
            if(!is852Added){
                record = add852aField(record,holdingsEntity);
            }
            if(!is866Added){
                record = add8660Field(record,holdingsEntity);
            }
            if(!is876Added){
                record = add8760Field(record,holdingsEntity);
            }
        }
        return record;
    }

    private Record add852aField(Record record,HoldingsEntity holdingEntity){
        MarcFactory factory = MarcFactory.newInstance();
        DataField dataField = factory.newDataField("852",' ',' ');
        if(holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.PRINCETON)){
            dataField.addSubfield(factory.newSubfield('a',holdingPUL));
        }else if(holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.COLUMBIA)){
            dataField.addSubfield(factory.newSubfield('a',holdingCUL));
        }else if(holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.NYPL)){
            dataField.addSubfield(factory.newSubfield('a',holdingNYPL));
        }
        record.addVariableField(dataField);
        return record;
    }

    private void add852aField(DataField dataField,HoldingsEntity holdingEntity){
        MarcFactory factory = MarcFactory.newInstance();
        if(holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.PRINCETON)){
            dataField.addSubfield(factory.newSubfield('a',holdingPUL));
        }else if(holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.COLUMBIA)){
            dataField.addSubfield(factory.newSubfield('a',holdingCUL));
        }else if(holdingEntity.getInstitutionEntity().getInstitutionCode().equals(ReCAPConstants.NYPL)){
            dataField.addSubfield(factory.newSubfield('a',holdingNYPL));
        }
    }

    private Record add8660Field(Record record,HoldingsEntity holdingsEntity){
        MarcFactory factory = MarcFactory.newInstance();
        DataField dataField = factory.newDataField("866",' ',' ');
        dataField.addSubfield(factory.newSubfield('0',holdingsEntity.getHoldingsId().toString()));
        record.addVariableField(dataField);
        return record;
    }

    private void add8660Field(DataField dataField,HoldingsEntity holdingEntity){
        MarcFactory factory = MarcFactory.newInstance();
        dataField.addSubfield(factory.newSubfield('0',holdingEntity.getHoldingsId().toString()));
    }

    private Record add8760Field(Record record,HoldingsEntity holdingEntity){
        MarcFactory factory = MarcFactory.newInstance();
        DataField dataField = factory.newDataField("876",' ',' ');
        dataField.addSubfield(factory.newSubfield('0',holdingEntity.getHoldingsId().toString()));
        record.addVariableField(dataField);
        return record;
    }

    private void add8760Field(DataField dataField,HoldingsEntity holdingEntity){
        MarcFactory factory = MarcFactory.newInstance();
        dataField.addSubfield(factory.newSubfield('0',holdingEntity.getHoldingsId().toString()));
    }

    private void add876xField(Record record,String collectionGroupCode){
        MarcFactory factory = MarcFactory.newInstance();
        DataField dataField = factory.newDataField("876",' ',' ');
        dataField.addSubfield(factory.newSubfield('x', collectionGroupCode));
        record.addVariableField(dataField);
    }

    private Record addItemInfo(Record record,List<ItemEntity> itemEntityList){
        MarcFactory factory = MarcFactory.newInstance();
        boolean is876Added = false;
        for(ItemEntity itemEntity:itemEntityList){
            for(DataField dataField:record.getDataFields()){
                if(dataField.getTag().equals("876")){
                    dataField.addSubfield(factory.newSubfield('x', itemEntity.getCollectionGroupEntity().getCollectionGroupCode()));
                    is876Added = true;
                }
            }
            if(!is876Added){
                add876xField(record,itemEntity.getCollectionGroupEntity().getCollectionGroupCode());
            }
        }
        return record;
    }

    public String covertToMarcXmlString(List<Record> recordList){
        OutputStream out = new ByteArrayOutputStream();
        MarcWriter writer = new MarcXmlWriter(out,"UTF-8", true);
        try {
            recordList.forEach(writer::write);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return out.toString();
    }
}
