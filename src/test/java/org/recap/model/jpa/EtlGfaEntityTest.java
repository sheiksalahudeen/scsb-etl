package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.EtlGfaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 21/7/16.
 */
public class EtlGfaEntityTest extends BaseTestCase {

    @Autowired
    EtlGfaRepository etlGfaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testSaveAndFetchEtlGfa(){
        EtlGfaEntity etlGfaEntity = new EtlGfaEntity();
        etlGfaEntity.setId(1);
        etlGfaEntity.setItemBarcode("3210457796");
        etlGfaEntity.setAccessionDate(new Date());
        etlGfaEntity.setCustomer("GP");
        etlGfaEntity.setDeleteDate(new Date());
        etlGfaEntity.setStatus("Out on Ret WO: 387966 09/29/15 To PA");

        EtlGfaEntity savedEtlGfa = etlGfaRepository.saveAndFlush(etlGfaEntity);
        entityManager.refresh(savedEtlGfa);

        List<EtlGfaEntity> etlGfaEntityList = etlGfaRepository.findBystatus("Out on Ret WO: 387966 09/29/15 To PA");
        assertNotNull(savedEtlGfa);
        assertNotNull(etlGfaEntityList);
        assertEquals(etlGfaEntityList.get(0).getStatus(),"Out on Ret WO: 387966 09/29/15 To PA");


    }

}