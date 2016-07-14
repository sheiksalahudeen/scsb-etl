package org.recap.repository;

import org.recap.model.jpa.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by chenchulakshmig on 21/6/16.
 */
public interface ItemDetailsRepository extends PagingAndSortingRepository<ItemEntity, Integer> {

    ItemEntity findByItemId(Integer itemId);

    Long countByOwningInstitutionId(Integer owningInstitutionId);

    Page<ItemEntity> findByOwningInstitutionId(Pageable pageable, Integer owningInstitutionId);

    ItemEntity findByOwningInstitutionId(Integer owningInstitutionId);

    ItemEntity findByOwningInstitutionItemId(String owningInstitutionItemId);

}
