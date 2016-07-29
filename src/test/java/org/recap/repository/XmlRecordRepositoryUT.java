package org.recap.repository;

import org.apache.activemq.transport.tcp.ExceededMaximumConnectionsException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.XmlRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by peris on 7/17/16.
 */
public class XmlRecordRepositoryUT extends BaseTestCase {
    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Test
    public void fetchRecords() throws Exception {
        Page<XmlRecordEntity> xmlRecordEntities = xmlRecordRepository.findAll(new PageRequest(0, 10));
        assertNotNull(xmlRecordEntities);
    }

    @Test
    public void findById() throws Exception {
        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXml("mock xml content".getBytes());
        xmlRecordEntity.setOwningInst("1");
        xmlRecordEntity.setOwningInstBibId("PUL");
        xmlRecordEntity.setXmlFileName("mockfile.xml");
        xmlRecordEntity.setDataLoaded(new Date());
        XmlRecordEntity savedEntity = xmlRecordRepository.save(xmlRecordEntity);

        XmlRecordEntity byId = xmlRecordRepository.findById(savedEntity.getId());
        assertEquals(new String(byId.getXml()),"mock xml content");
        System.out.println(new String(byId.getXml()));
    }

    @Test
    public void findDistinctFileNames() throws Exception {
        List distinctFileNames = xmlRecordRepository.findDistinctFileNames();
        assertNotNull(distinctFileNames);
        assertTrue(!distinctFileNames.isEmpty());
        for (Iterator iterator = distinctFileNames.iterator(); iterator.hasNext(); ) {
            String fileName = (String) iterator.next();
            System.out.println(fileName);
        }
    }

}