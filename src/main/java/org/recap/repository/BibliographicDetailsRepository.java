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

    @Query(value="SELECT COUNT(BIB) FROM BibliographicEntity AS BIB INNER JOIN BIB.institutionEntity AS INST WHERE INST.institutionCode IN (?2) AND " +
            "BIB.owningInstitutionBibId IN (SELECT DISTINCT BIB1.owningInstitutionBibId FROM BibliographicEntity as BIB1 INNER JOIN BIB1.institutionEntity AS INST1 " +
            "INNER JOIN BIB1.holdingsEntities AS HOLDING INNER JOIN HOLDING.itemEntities AS ITEMS WHERE ITEMS.collectionGroupId IN (?1) AND INST1.institutionCode IN (?2))")
    Long countByInstitutionCodes(Collection<Integer> cgIds, Collection<String> institutionCodes);

    @Query(value="SELECT BIB FROM BibliographicEntity as BIB INNER JOIN BIB.institutionEntity AS INST WHERE INST.institutionCode IN (?2) AND " +
            "BIB.owningInstitutionBibId IN (SELECT DISTINCT BIB1.owningInstitutionBibId FROM BibliographicEntity as BIB1 INNER JOIN BIB1.institutionEntity AS INST1 " +
            "INNER JOIN BIB1.itemEntities AS ITEMS WHERE ITEMS.collectionGroupId IN (?1) AND INST1.institutionCode IN (?2))")
    Page<BibliographicEntity> findByInstitutionCodes(Pageable pageable, Collection<Integer> cgIds, Collection<String> institutionCodes);

    @Query(value="SELECT COUNT(BIB) FROM BibliographicEntity as BIB INNER JOIN BIB.institutionEntity AS INST WHERE INST.institutionCode IN (?2) AND  " +
        "BIB.owningInstitutionBibId IN (SELECT DISTINCT BIB1.owningInstitutionBibId FROM BibliographicEntity as BIB1 INNER JOIN BIB1.institutionEntity AS INST1 " +
        "INNER JOIN BIB1.itemEntities AS ITEMS WHERE ITEMS.collectionGroupId IN (?1) AND INST1.institutionCode IN (?2) AND BIB1.lastUpdatedDate >= ?3)")
    Long countByInstitutionCodesAndLastUpdatedDate(Collection<Integer> cgIds, Collection<String> institutionIds, Date lastUpdatedDate);

    @Query(value="SELECT BIB FROM BibliographicEntity as BIB  INNER JOIN BIB.institutionEntity AS INST WHERE INST.institutionCode IN (?2) AND  " +
            "BIB.owningInstitutionBibId IN (SELECT DISTINCT BIB1.owningInstitutionBibId FROM BibliographicEntity as BIB1 INNER JOIN BIB1.institutionEntity AS INST1 " +
            "INNER JOIN BIB1.itemEntities AS ITEMS WHERE ITEMS.collectionGroupId IN (?1) AND INST1.institutionCode IN (?2) AND BIB1.lastUpdatedDate >= ?3)")
    Page<BibliographicEntity> findByInstitutionCodeAndLastUpdatedDate(Pageable pageable, Collection<Integer> cgIds, Collection<String> institutionCodes, Date lastUpdatedDate);

}
