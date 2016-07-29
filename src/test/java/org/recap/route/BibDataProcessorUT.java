package org.recap.route;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.util.LoadReportUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by angelind on 26/7/16.
 */
public class BibDataProcessorUT extends BaseTestCase{

    @Autowired
    BibDataProcessor bibDataProcessor;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Mock
    private Map<String, Integer> institutionMap;

    @Mock
    private Map<String, Integer> collectionGroupMap;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void persistDataToDB() throws  Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCallNumberType("0");
        itemEntity.setCallNumber("callNum");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");
        itemEntity.setBarcode("1231");
        itemEntity.setOwningInstitutionItemId(".i1231");
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCustomerCode("PA");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

        ETLExchange etlExchange = new ETLExchange();
        etlExchange.setBibliographicEntities(Arrays.asList(bibliographicEntity));
        etlExchange.setInstitutionEntityMap(etlExchange.getInstitutionEntityMap() == null ? new HashMap() : etlExchange.getInstitutionEntityMap());
        etlExchange.setCollectionGroupMap(etlExchange.getCollectionGroupMap() == null ? new HashMap() : etlExchange.getCollectionGroupMap());

        bibDataProcessor.processETLExchagneAndPersistToDB(etlExchange);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(1, owningInstitutionBibId);
        assertNotNull(savedBibliographicEntity);
    }

    @Test
    public void processFailuresForHoldingsAndItems() throws Exception {

        Mockito.when(institutionMap.get("PUL")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Shared")).thenReturn(1);

        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCallNumberType("0");
        itemEntity.setCallNumber("callNum");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");
        itemEntity.setBarcode("33433002853319334330028533193343300285331955555");
        itemEntity.setOwningInstitutionItemId(".i1231");
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCustomerCode("PA");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

        LoadReportUtil loadReportUtil = new LoadReportUtil(institutionMap, collectionGroupMap);
        bibDataProcessor.setXmlFileName("testFailureForItemsAndHoldings.xml");
        List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = bibDataProcessor.processBibHoldingsItems(loadReportUtil, bibliographicEntity);
        assertTrue(failureReportReCAPCSVRecords.size() == 1);
        assertEquals(bibDataProcessor.getXmlFileName(), failureReportReCAPCSVRecords.get(0).getFileName());
    }
}