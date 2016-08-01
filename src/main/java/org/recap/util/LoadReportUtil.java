package org.recap.util;

import org.apache.commons.lang3.StringUtils;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.jaxb.marc.RecordType;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
public class LoadReportUtil {

    private final Map<String, Integer> institutionEntitiesMap;
    private final Map<String, Integer> collectionGroupMap;

    public LoadReportUtil(Map institutionEntitiesMap, Map collectionGroupMap) {
        this.institutionEntitiesMap = institutionEntitiesMap;
        this.collectionGroupMap = collectionGroupMap;
    }

    public FailureReportReCAPCSVRecord populateBibInfo(BibliographicEntity bibliographicEntity) {
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord= new FailureReportReCAPCSVRecord();
        if (bibliographicEntity.getOwningInstitutionId() != null) {
            for (Map.Entry<String, Integer> entry : institutionEntitiesMap.entrySet()) {
                if (entry.getValue() == bibliographicEntity.getOwningInstitutionId()) {
                    failureReportReCAPCSVRecord.setOwningInstitution(entry.getKey());
                    break;
                }
            }
        }
        failureReportReCAPCSVRecord.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        String content = new String(bibliographicEntity.getContent());
        if (StringUtils.isNotBlank(content)) {
            CollectionType collectionType = new CollectionType();
            collectionType = (CollectionType) collectionType.deserialize(content);
            if (collectionType != null && !CollectionUtils.isEmpty(collectionType.getRecord())) {
                RecordType recordType = collectionType.getRecord().get(0);
                if (recordType != null) {
                    String title = new MarcUtil().getDataFieldValue(recordType, "245", null, null, "a");
                    failureReportReCAPCSVRecord.setTitle(title.trim());
                }
            }

        }
        return failureReportReCAPCSVRecord;
    }

    public FailureReportReCAPCSVRecord populateBibHoldingsInfo(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity) {
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = populateBibInfo(bibliographicEntity);
        if (holdingsEntity != null) {
            failureReportReCAPCSVRecord.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());
        }
        return failureReportReCAPCSVRecord;
    }

    public FailureReportReCAPCSVRecord populateBibHoldingsItemInfo(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity, ItemEntity itemEntity) {
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = populateBibHoldingsInfo(bibliographicEntity, holdingsEntity);
        if (itemEntity != null) {
            failureReportReCAPCSVRecord.setLocalItemId(itemEntity.getOwningInstitutionItemId());
            failureReportReCAPCSVRecord.setItemBarcode(itemEntity.getBarcode());
            failureReportReCAPCSVRecord.setCustomerCode(itemEntity.getCustomerCode());

            if (itemEntity.getCollectionGroupId() != null) {
                for (Map.Entry<String, Integer> entry : collectionGroupMap.entrySet()) {
                    if (entry.getValue() == itemEntity.getCollectionGroupId()) {
                        failureReportReCAPCSVRecord.setCollectionGroupDesignation(entry.getKey());
                        break;
                    }
                }
            }
            failureReportReCAPCSVRecord.setCreateDateItem(itemEntity.getCreatedDate());
            failureReportReCAPCSVRecord.setLastUpdatedDateItem(itemEntity.getLastUpdatedDate());
        }
        return failureReportReCAPCSVRecord;
    }
}
