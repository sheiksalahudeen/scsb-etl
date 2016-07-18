package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.XmlRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.Assert.*;

/**
 * Created by peris on 7/17/16.
 */
public class XmlRecordRepositoryTest extends BaseTestCase {
    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Test
    public void fetchRecords() throws Exception {
        Page<XmlRecordEntity> xmlRecordEntities = xmlRecordRepository.findAll(new PageRequest(0, 10));
        assertNotNull(xmlRecordEntities);
    }

}