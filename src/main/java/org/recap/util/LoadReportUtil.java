package org.recap.util;

import org.apache.commons.lang3.StringUtils;
import org.recap.model.etl.LoadReportEntity;
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

    public LoadReportEntity validateBibliographicEntity(BibliographicEntity bibliographicEntity) {
        StringBuffer errorMessage = new StringBuffer();
        String content = new String(bibliographicEntity.getContent());
        if (StringUtils.isBlank(content)) {
            errorMessage.append("Bib Content cannot be empty");
        }
        if (bibliographicEntity.getOwningInstitutionId() == null) {
            errorMessage.append("\n");
            errorMessage.append("Owning Institution Id cannot be null");
        }
        if (bibliographicEntity.getCreatedDate() == null) {
            errorMessage.append("\n");
            errorMessage.append("Bib Created Date cannot be null");
        }
        if (StringUtils.isBlank(bibliographicEntity.getOwningInstitutionBibId())) {
            errorMessage.append("\n");
            errorMessage.append("Owning Institution Bib Id cannot be null");
        }
        if (errorMessage.toString().length() > 1) {
            LoadReportEntity loadReportEntity = populateBibInfo(bibliographicEntity);
            loadReportEntity.setErrorDescription(errorMessage.toString());
            return loadReportEntity;
        }
        return null;
    }

    public LoadReportEntity validateHoldingsEntities(BibliographicEntity bibliographicEntity) {
        StringBuffer errorMessage = new StringBuffer();
        List<HoldingsEntity> holdingsEntities = bibliographicEntity.getHoldingsEntities();
        if (!CollectionUtils.isEmpty(holdingsEntities)) {
            for (HoldingsEntity holdingsEntity : holdingsEntities) {
                if (holdingsEntity.getCreatedDate() == null) {
                    errorMessage.append("Holdings Created Date cannot be null");
                }
                String content = new String(holdingsEntity.getContent());
                if (StringUtils.isBlank(content)) {
                    errorMessage.append("\n");
                    errorMessage.append("Holdings Content cannot be empty");
                }
                if (errorMessage.toString().length() > 1) {
                    LoadReportEntity loadReportEntity = populateBibHoldingsInfo(bibliographicEntity, holdingsEntity);
                    loadReportEntity.setErrorDescription(errorMessage.toString());
                    return loadReportEntity;
                }

            }
        }
        return null;
    }

    public LoadReportEntity validateItemEntities(BibliographicEntity bibliographicEntity) {
        StringBuffer errorMessage = new StringBuffer();
        List<ItemEntity> itemEntities = bibliographicEntity.getItemEntities();
        if (!CollectionUtils.isEmpty(itemEntities)) {
            for (ItemEntity itemEntity : itemEntities) {
                if (StringUtils.isBlank(itemEntity.getBarcode())) {
                    errorMessage.append("Item Barcode cannot be null");
                }
                if (StringUtils.isBlank(itemEntity.getCustomerCode())) {
                    errorMessage.append("\n");
                    errorMessage.append("Customer Code cannot be null");
                }
                if (itemEntity.getItemAvailabilityStatusId() == null) {
                    errorMessage.append("\n");
                    errorMessage.append("Item Availabilty Status Id cannot be null");
                }
                if (itemEntity.getOwningInstitutionId() == null) {
                    errorMessage.append("\n");
                    errorMessage.append("Owning Institution Id  cannot be null");
                }
                if (itemEntity.getCollectionGroupId() == null) {
                    errorMessage.append("\n");
                    errorMessage.append("Collection Group Id  cannot be null");
                }
                if (itemEntity.getCreatedDate() == null) {
                    errorMessage.append("\n");
                    errorMessage.append("Item Created Date cannot be null");
                }
                if (StringUtils.isBlank(itemEntity.getOwningInstitutionItemId())) {
                    errorMessage.append("\n");
                    errorMessage.append("Item Owning Institution Id cannot be null");
                }
                if (errorMessage.toString().length() > 1) {
                    LoadReportEntity loadReportEntity = populateBibHoldingsItemInfo(bibliographicEntity, itemEntity.getHoldingsEntity(), itemEntity);
                    loadReportEntity.setErrorDescription(errorMessage.toString());
                    return loadReportEntity;
                }
            }
        }
        return null;
    }

    public LoadReportEntity populateBibInfo(BibliographicEntity bibliographicEntity) {
        LoadReportEntity loadReportEntity = new LoadReportEntity();
        if (bibliographicEntity.getOwningInstitutionId() != null) {
            for (Map.Entry<String, Integer> entry : institutionEntitiesMap.entrySet()) {
                if (entry.getValue() == bibliographicEntity.getOwningInstitutionId()) {
                    loadReportEntity.setOwningInstitution(entry.getKey());
                    break;
                }
            }
        }
        loadReportEntity.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        String content = new String(bibliographicEntity.getContent());
        if (StringUtils.isNotBlank(content)) {
            CollectionType collectionType = new CollectionType();
            collectionType = (CollectionType) collectionType.deserialize(content);
            if (collectionType != null && !CollectionUtils.isEmpty(collectionType.getRecord())) {
                RecordType recordType = collectionType.getRecord().get(0);
                if (recordType != null) {
                    String title = new MarcUtil().getDataFieldValue(recordType, "245", null, null, "a");
                    loadReportEntity.setTitle(title);
                }
            }

        }
        return loadReportEntity;
    }

    public LoadReportEntity populateBibHoldingsInfo(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity) {
        LoadReportEntity loadReportEntity = populateBibInfo(bibliographicEntity);
        if (holdingsEntity != null) {
            loadReportEntity.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());
        }
        return loadReportEntity;
    }

    public LoadReportEntity populateBibHoldingsItemInfo(BibliographicEntity bibliographicEntity, HoldingsEntity holdingsEntity, ItemEntity itemEntity) {
        LoadReportEntity loadReportEntity = populateBibHoldingsInfo(bibliographicEntity, holdingsEntity);
        if (itemEntity != null) {
            loadReportEntity.setLocalItemId(itemEntity.getOwningInstitutionItemId());
            loadReportEntity.setItemBarcode(itemEntity.getBarcode());
            loadReportEntity.setCustomerCode(itemEntity.getCustomerCode());

            if (itemEntity.getCollectionGroupId() != null) {
                for (Map.Entry<String, Integer> entry : collectionGroupMap.entrySet()) {
                    if (entry.getValue() == itemEntity.getCollectionGroupId()) {
                        loadReportEntity.setCollectionGroupDesignation(entry.getKey());
                        break;
                    }
                }
            }
            loadReportEntity.setCreateDateItem(itemEntity.getCreatedDate());
            loadReportEntity.setLastUpdatedDateItem(itemEntity.getLastUpdatedDate());
        }
        return loadReportEntity;
    }
}
