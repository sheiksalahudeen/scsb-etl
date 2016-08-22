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

    @Query(value="SELECT COUNT(*) FROM BibliographicEntity as BIB INNER JOIN BIB.institutionEntity AS INST WHERE INST.institutionCode = ?1 ")
    Long countByInstitutionCodes(Collection<String> institutionCodes);

    @Query(value="SELECT COUNT(*) FROM BibliographicEntity as BIB INNER JOIN BIB.institutionEntity AS INST WHERE BIB.lastUpdatedDate > ?1 ")
    Long countByLastUpdatedDate(Date lastUpdatedDate);

    @Query(value="SELECT COUNT(*) FROM BibliographicEntity as BIB INNER JOIN BIB.institutionEntity AS INST WHERE INST.institutionCode = ?1 AND BIB.lastUpdatedDate > ?2 ")
    Long countByInstitutionCodesAndLastUpdatedDate(Collection<String> institutionIds, Date lastUpdatedDate);

    @Query(value="SELECT BIB FROM BibliographicEntity BIB INNER JOIN BIB.institutionEntity INST WHERE INST.institutionCode IN ?1 AND BIB.lastUpdatedDate > ?2 ORDER BY INST.institutionCode")
    Page<BibliographicEntity> findByInstitutionCodeAndLastUpdatedDate(Pageable pageable, Collection<String> institutionCodes, Date lastUpdatedDate);

    @Query(value="SELECT BIB FROM BibliographicEntity BIB INNER JOIN BIB.institutionEntity INST WHERE INST.institutionCode IN ?1 ORDER BY INST.institutionCode")
    Page<BibliographicEntity> findByInstitutionCodes(Pageable pageable, Collection<String> institutionCodes);

    Page<BibliographicEntity> findByLastUpdatedDateAfter(Pageable pageable, Date lastUpdatedDate);


}
