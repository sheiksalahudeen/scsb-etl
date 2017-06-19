package org.recap.repository;

import org.recap.model.jpa.HoldingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface HoldingsDetailsRepository extends JpaRepository<HoldingsEntity, String> {

    /**
     * Find by holdings id holdings entity.
     *
     * @param holdingsId the holdings id
     * @return the holdings entity
     */
    HoldingsEntity findByHoldingsId(Integer holdingsId);

    /**
     * Count by owning institution id long.
     *
     * @param owningInstitutionId the owning institution id
     * @return the long
     */
    Long countByOwningInstitutionId(Integer owningInstitutionId);

}
