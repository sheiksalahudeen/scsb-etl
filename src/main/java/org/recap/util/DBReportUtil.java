package org.recap.util;


import org.apache.commons.lang3.StringUtils;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 8/10/16.
 */

@Component
public class DBReportUtil {

    private Map<String, Integer> institutionEntitiesMap;
    private Map<String, Integer> collectionGroupMap;

    public Map<String, Integer> getInstitutionEntitiesMap() {
        return institutionEntitiesMap;
    }

    public void setInstitutionEntitiesMap(Map<String, Integer> institutionEntitiesMap) {
        this.institutionEntitiesMap = institutionEntitiesMap;
    }

    public Map<String, Integer> getCollectionGroupMap() {
        return collectionGroupMap;
    }

    public void setCollectionGroupMap(Map<String, Integer> collectionGroupMap) {
        this.collectionGroupMap = collectionGroupMap;
    }

    public List<ReportDataEntity> generateBibHoldingsAndItemsFailureReportEntities(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity, ItemEntity itemEntity) {
        List<ReportDataEntity> reportEntities = new ArrayList<>();
        reportEntities.addAll(generateBibHoldingsFaiureReortEntity(bibliographicEntity, holdingsEntity));

        if (itemEntity != null) {
            ReportDataEntity localItemIdReportDataEntity = new ReportDataEntity();
            localItemIdReportDataEntity.setHeaderName("LocalItemId");
            localItemIdReportDataEntity.setHeaderValue(itemEntity.getOwningInstitutionItemId());
            reportEntities.add(localItemIdReportDataEntity);

            ReportDataEntity itemBarcodeReportDataEntity = new ReportDataEntity();
            itemBarcodeReportDataEntity.setHeaderName("ItemBarcode");
            itemBarcodeReportDataEntity.setHeaderValue(itemEntity.getBarcode());
            reportEntities.add(itemBarcodeReportDataEntity);

            ReportDataEntity customerCodeReportDataEntity = new ReportDataEntity();
            customerCodeReportDataEntity.setHeaderName("CustomerCode");
            customerCodeReportDataEntity.setHeaderValue(itemEntity.getCustomerCode());
            reportEntities.add(customerCodeReportDataEntity);

            if (itemEntity.getCollectionGroupId() != null) {
                for (Map.Entry<String, Integer> entry : collectionGroupMap.entrySet()) {
                    if (entry.getValue() == itemEntity.getCollectionGroupId()) {
                        ReportDataEntity collectionGroupDesignationEntity = new ReportDataEntity();
                        collectionGroupDesignationEntity.setHeaderName("CollectionGroupDesignation");
                        collectionGroupDesignationEntity.setHeaderValue(entry.getKey());
                        reportEntities.add(collectionGroupDesignationEntity);
                        break;
                    }
                }
            }

            ReportDataEntity createDateItemEntity = new ReportDataEntity();
            createDateItemEntity.setHeaderName("CreateDateItem");
            createDateItemEntity.setHeaderValue(new SimpleDateFormat("mm-dd-yyyy").format(itemEntity.getCreatedDate()));
            reportEntities.add(createDateItemEntity);

            ReportDataEntity lastUpdateItemEntity = new ReportDataEntity();
            lastUpdateItemEntity.setHeaderName("LastUpdatedDateItem");
            lastUpdateItemEntity.setHeaderValue(new SimpleDateFormat("mm-dd-yyyy").format(itemEntity.getLastUpdatedDate()));
            reportEntities.add(lastUpdateItemEntity);

        }
        return reportEntities;
    }



    public List<ReportDataEntity> generateBibHoldingsFaiureReortEntity(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity) {
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
        reportDataEntities.addAll(generateBibFailureReportEntity(bibliographicEntity));
        if (holdingsEntity != null) {
            ReportDataEntity owningInstitutionHoldingsIdReportDataEntity = new ReportDataEntity();
            owningInstitutionHoldingsIdReportDataEntity.setHeaderName("OwningInstitutionHoldingsId");
            owningInstitutionHoldingsIdReportDataEntity.setHeaderValue(holdingsEntity.getOwningInstitutionHoldingsId());
            reportDataEntities.add(owningInstitutionHoldingsIdReportDataEntity);
        }
        return reportDataEntities;
    }

    public List<ReportDataEntity> generateBibFailureReportEntity(BibliographicEntity bibliographicEntity) {
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportDataEntity owningInstitutionReportDataEntity = new ReportDataEntity();

        if (bibliographicEntity.getOwningInstitutionId() != null) {
            for (Map.Entry<String, Integer> entry : institutionEntitiesMap.entrySet()) {
                if (entry.getValue() == bibliographicEntity.getOwningInstitutionId()) {
                    owningInstitutionReportDataEntity.setHeaderName("OwningInstitution");
                    owningInstitutionReportDataEntity.setHeaderValue(entry.getKey());
                    reportDataEntities.add(owningInstitutionReportDataEntity);
                    break;
                }
            }
        }

        ReportDataEntity owningInstitutionBibIdReportDataEntity = new ReportDataEntity();
        owningInstitutionBibIdReportDataEntity.setHeaderName("OwningInstituionBibId");
        owningInstitutionBibIdReportDataEntity.setHeaderValue(bibliographicEntity.getOwningInstitutionBibId());
        reportDataEntities.add(owningInstitutionBibIdReportDataEntity);

        String content = new String(bibliographicEntity.getContent());
        if (StringUtils.isNotBlank(content)) {
            CollectionType collectionType = new CollectionType();
            collectionType = (CollectionType) collectionType.deserialize(content);
            if (collectionType != null && !CollectionUtils.isEmpty(collectionType.getRecord())) {
                RecordType recordType = collectionType.getRecord().get(0);
                if (recordType != null) {
                    String title = new MarcUtil().getDataFieldValue(recordType, "245", null, null, "a");
                    ReportDataEntity titleReportDataEntity = new ReportDataEntity();
                    titleReportDataEntity.setHeaderName("title");
                    titleReportDataEntity.setHeaderValue(title.trim());
                }
            }
        }
        return reportDataEntities;
    }
}
