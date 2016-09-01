package org.recap.repository;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.BibliographicPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/10/16.
 */
public interface BibliographicDetailsRepository extends JpaRepository<BibliographicEntity, BibliographicPK> {

    Long countByOwningInstitutionId(Integer owningInstitutionId);

    Page<BibliographicEntity> findByOwningInstitutionId(Pageable pageable, Integer owningInstitutionId);

    BibliographicEntity findByOwningInstitutionIdAndOwningInstitutionBibId(Integer owningInstitutionId, String owningInstitutionBibId);

    List<BibliographicEntity> findByOwningInstitutionBibId(String owningInstitutionBibId);

    @Query(value = "select count(owning_inst_bib_id) from bibliographic_holdings_t",  nativeQuery = true)
    Long findCountOfBibliographicHoldings();

    @Query(value = "select count(owning_inst_bib_id) from bibliographic_holdings_t where owning_inst_id = ?1",  nativeQuery = true)
    Long findCountOfBibliographicHoldingsByInstId(Integer instId);

    @Query(value="SELECT COUNT(BIB) FROM ItemEntity ITEM INNER JOIN ITEM.bibliographicEntities BIB INNER JOIN BIB.institutionEntity INST WHERE ITEM.collectionGroupId IN (?1) AND INST.institutionCode IN (?2)")
    Long countByInstitutionCodes(Collection<Integer> cgIds, Collection<String> institutionCodes);

    @Query(value="SELECT COUNT(BIB) FROM ItemEntity ITEM INNER JOIN ITEM.bibliographicEntities BIB INNER JOIN BIB.institutionEntity INST WHERE ITEM.collectionGroupId IN (?1) AND INST.institutionCode IN (?2) AND BIB.lastUpdatedDate > ?3 ")
    Long countByInstitutionCodesAndLastUpdatedDate(Collection<Integer> cgIds, Collection<String> institutionIds, Date lastUpdatedDate);

    @Query(value="SELECT BIB FROM ItemEntity ITEM INNER JOIN ITEM.bibliographicEntities BIB INNER JOIN BIB.institutionEntity INST WHERE ITEM.collectionGroupId IN (?1) AND INST.institutionCode IN (?2) AND BIB.lastUpdatedDate > ?3 ")
    Page<BibliographicEntity> findByInstitutionCodeAndLastUpdatedDate(Pageable pageable, Collection<Integer> cgIds, Collection<String> institutionCodes, Date lastUpdatedDate);

    @Query(value="SELECT BIB FROM ItemEntity ITEM INNER JOIN ITEM.bibliographicEntities BIB INNER JOIN BIB.institutionEntity INST WHERE ITEM.collectionGroupId IN (?1) AND INST.institutionCode IN (?2)")
    Page<BibliographicEntity> findByInstitutionCodes(Pageable pageable, Collection<Integer> cgIds, Collection<String> institutionCodes);

}
