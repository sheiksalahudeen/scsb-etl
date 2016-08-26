package org.recap.repository;

import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.ItemPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by chenchulakshmig on 21/6/16.
 */
public interface ItemDetailsRepository extends PagingAndSortingRepository<ItemEntity, ItemPK> {

    ItemEntity findByItemId(Integer itemId);

    Long countByOwningInstitutionId(Integer owningInstitutionId);

    Page<ItemEntity> findByOwningInstitutionId(Pageable pageable, Integer owningInstitutionId);

    List<ItemEntity> findByOwningInstitutionId(Integer owningInstitutionId);

    ItemEntity findByOwningInstitutionItemId(String owningInstitutionItemId);

    @Query(value = "select count(*) from bibliographic_item_t",  nativeQuery = true)
    Long findCountOfBibliographicItems();

    @Query(value = "select count(*) from bibliographic_item_t where bib_inst_id = ?1",  nativeQuery = true)
    Long findCountOfBibliographicItemsByInstId(Integer instId);


}
