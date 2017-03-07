package org.recap.service.formatter.datadump;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.model.export.DeletedRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by premkb on 29/9/16.
 */
public class DeletedJsonFormatterServiceUT extends BaseTestCase{

    @Autowired
    private DeletedJsonFormatterService deletedJsonFormatterService;

    @Test
    public void getFormattedOutput() throws Exception {
        Map<String,Object> successAndFailureFormattedList = deletedJsonFormatterService.prepareDeletedRecords(getBibliographicEntityList());
        List<DeletedRecord> deletedRecordList = (List<DeletedRecord>)successAndFailureFormattedList.get(RecapConstants.SUCCESS);
        String outputString = (String) deletedJsonFormatterService.getJsonForDeletedRecords(deletedRecordList);
        assertEquals("[{\"bibId\":\"100\",\"itemBarcodes\":[\"3456\",\"1234\"]}]",outputString);
    }

    private List<BibliographicEntity> getBibliographicEntityList() throws URISyntaxException, IOException {
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setBibliographicId(100);
        bibliographicEntity.setContent("bib content".getBytes());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId("2");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setLastUpdatedBy("tst");

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("holding content".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId("3");

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId("4");
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("tst");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("tst");
        itemEntity.setBarcode("1234");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("1");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setCopyNumber(123);
        itemEntity.setDeleted(true);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setOwningInstitutionItemId("4");
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("tst");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("tst");
        itemEntity1.setBarcode("3456");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setCopyNumber(123);
        itemEntity1.setDeleted(true);
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1,itemEntity));
        return Arrays.asList(bibliographicEntity);
    }

}
