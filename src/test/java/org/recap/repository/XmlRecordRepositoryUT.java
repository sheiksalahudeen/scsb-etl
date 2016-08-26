package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.XmlRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    public void saveAndFindAndUpdate() throws Exception {
        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXml("mock xml content".getBytes());
        xmlRecordEntity.setOwningInst("1");
        xmlRecordEntity.setOwningInstBibId("PUL");
        xmlRecordEntity.setXmlFileName("mockfile.xml");
        xmlRecordEntity.setDataLoaded(new Date());
        XmlRecordEntity savedEntity = xmlRecordRepository.save(xmlRecordEntity);

        XmlRecordEntity byId = xmlRecordRepository.findById(savedEntity.getId());
        assertNotNull(byId.getDataLoaded());
        assertEquals(byId.getOwningInst(), xmlRecordEntity.getOwningInst());
        assertEquals(byId.getOwningInstBibId(), xmlRecordEntity.getOwningInstBibId());
        assertEquals(byId.getXmlFileName(), xmlRecordEntity.getXmlFileName());
        assertEquals(new String(byId.getXml()), "mock xml content");
        System.out.println(new String(byId.getXml()));

        XmlRecordEntity xmlRecordEntityToUpdate = new XmlRecordEntity();
        xmlRecordEntityToUpdate.setId(byId.getId());
        xmlRecordEntityToUpdate.setXml("new mock xml content".getBytes());
        xmlRecordEntityToUpdate.setOwningInst("1");
        xmlRecordEntityToUpdate.setOwningInstBibId("PUL");
        xmlRecordEntityToUpdate.setXmlFileName("mockfile.xml");
        xmlRecordEntityToUpdate.setDataLoaded(byId.getDataLoaded());
        xmlRecordRepository.save(xmlRecordEntityToUpdate);
        XmlRecordEntity byIdAfterUpdate = xmlRecordRepository.findById(savedEntity.getId());
        assertEquals(byIdAfterUpdate.getId(), byId.getId());
        assertEquals(new String(byIdAfterUpdate.getXml()), "new mock xml content");
        System.out.println(new String(byIdAfterUpdate.getXml()));
    }

    @Test
    public void findDistinctFileNames() throws Exception {
        XmlRecordEntity xmlRecordEntity1 = new XmlRecordEntity();
        xmlRecordEntity1.setXml("mock xml content".getBytes());
        xmlRecordEntity1.setOwningInst("1");
        xmlRecordEntity1.setOwningInstBibId("PUL");
        xmlRecordEntity1.setXmlFileName("mockfile1.xml");
        xmlRecordEntity1.setDataLoaded(new Date());
        xmlRecordRepository.save(xmlRecordEntity1);
        XmlRecordEntity xmlRecordEntity2 = new XmlRecordEntity();
        xmlRecordEntity2.setXml("mock xml content".getBytes());
        xmlRecordEntity2.setOwningInst("1");
        xmlRecordEntity2.setOwningInstBibId("PUL");
        xmlRecordEntity2.setXmlFileName("mockfile2.xml");
        xmlRecordEntity2.setDataLoaded(new Date());
        xmlRecordRepository.save(xmlRecordEntity2);
        List distinctFileNames = xmlRecordRepository.findDistinctFileNames();
        assertNotNull(distinctFileNames);
        assertTrue(!distinctFileNames.isEmpty());
        for (Iterator iterator = distinctFileNames.iterator(); iterator.hasNext(); ) {
            String fileName = (String) iterator.next();
            System.out.println(fileName);
        }
    }

    @Test
    public void testFindInstByXmlFileName() throws Exception {
        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXml("mock xml content".getBytes());
        xmlRecordEntity.setOwningInst("PUL");
        xmlRecordEntity.setOwningInstBibId("1");
        xmlRecordEntity.setXmlFileName("mockfile.xml");
        xmlRecordEntity.setDataLoaded(new Date());
        XmlRecordEntity savedXmlRecordEntity = xmlRecordRepository.save(xmlRecordEntity);

        Integer instId = xmlRecordRepository.findInstIdByFileNames(savedXmlRecordEntity.getXmlFileName());

        assertNotNull(instId);
        assertEquals(new Integer(1), instId);
    }
}