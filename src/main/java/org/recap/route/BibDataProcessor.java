package org.recap.route;

import org.recap.model.etl.LoadReportEntity;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.util.CsvUtil;
import org.recap.util.LoadReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class BibDataProcessor {

    Logger logger = LoggerFactory.getLogger(BibDataProcessor.class);

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    CsvUtil csvUtil;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void processMessage(ETLExchange etlExchange) {
        if (etlExchange != null) {
            List<LoadReportEntity> loadReportEntities = new ArrayList<>();
            List<BibliographicEntity> bibliographicEntityList = etlExchange.getBibliographicEntities();

            try {
                bibliographicDetailsRepository.save(bibliographicEntityList);
                flushAndClearSession();
            } catch (Exception e) {
                LoadReportUtil loadReportUtil = new LoadReportUtil(etlExchange.getInstitutionEntityMap(), etlExchange.getCollectionGroupMap());
                for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
                    try {
                        bibliographicDetailsRepository.save(bibliographicEntity);
                        flushAndClearSession();
                    } catch (Exception ex) {
                        List<LoadReportEntity> reportEntities = processBibHoldingsItems(loadReportUtil, bibliographicEntity);
                        loadReportEntities.addAll(reportEntities);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(loadReportEntities)) {
                csvUtil.writeLoadReportToCsv(loadReportEntities);
            }
            etlExchange = null;
        }
    }

    private List<LoadReportEntity> processBibHoldingsItems(LoadReportUtil loadReportUtil, BibliographicEntity bibliographicEntity) {
        List<LoadReportEntity> loadReportEntities = new ArrayList<>();
        List<HoldingsEntity> savedHoldingsEntities = new ArrayList<>();
        List<ItemEntity> savedItemEntities = new ArrayList<>();
        try {
            List<HoldingsEntity> holdingsEntities = bibliographicEntity.getHoldingsEntities();
            bibliographicEntity.setHoldingsEntities(null);
            bibliographicEntity.setItemEntities(null);

            bibliographicDetailsRepository.save(bibliographicEntity);
            for (HoldingsEntity holdingsEntity : holdingsEntities) {
                List<ItemEntity> itemEntities = holdingsEntity.getItemEntities();
                holdingsEntity.setItemEntities(null);
                try {
                    HoldingsEntity savedHoldingsEntity = holdingsDetailsRepository.save(holdingsEntity);
                    savedHoldingsEntities.add(savedHoldingsEntity);
                    for (ItemEntity itemEntity : itemEntities) {
                        try {
                            itemEntity.setHoldingsEntity(savedHoldingsEntity);
                            ItemEntity savedItemEntity = itemDetailsRepository.save(itemEntity);
                            savedItemEntities.add(savedItemEntity);
                        } catch (Exception itemEx) {
                            LoadReportEntity loadReportEntity = loadReportUtil.populateBibHoldingsItemInfo(bibliographicEntity, holdingsEntity, itemEntity);
                            loadReportEntity.setExceptionMessage(itemEx.getCause().getCause().getMessage());
                            loadReportEntities.add(loadReportEntity);
                        }
                    }
                } catch (Exception holdingsEx) {
                    LoadReportEntity loadReportEntity = loadReportUtil.populateBibHoldingsInfo(bibliographicEntity, holdingsEntity);
                    loadReportEntity.setExceptionMessage(holdingsEx.getCause().getCause().getMessage());
                    loadReportEntities.add(loadReportEntity);
                }
            }
            bibliographicEntity.setHoldingsEntities(savedHoldingsEntities);
            bibliographicEntity.setItemEntities(savedItemEntities);
            bibliographicDetailsRepository.save(bibliographicEntity);
        } catch (Exception bibEx) {
            LoadReportEntity loadReportEntity = loadReportUtil.populateBibInfo(bibliographicEntity);
            loadReportEntity.setExceptionMessage(bibEx.getCause().getCause().getMessage());
            loadReportEntities.add(loadReportEntity);
        }

        flushAndClearSession();
        return loadReportEntities;
    }

    private void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }
}
