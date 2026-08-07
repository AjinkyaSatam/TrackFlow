package com.trackflow.service;

import com.trackflow.dto.OrganizationDTO;
import com.trackflow.dto.UpdateOrganizationRequest;

/**
 * Service interface defining organization configuration operations.
 */
public interface OrganizationService {

    /**
     * Retrieves organization profile details by ID.
     */
    OrganizationDTO getOrganizationById(Long id);

    /**
     * Updates organization settings.
     */
    OrganizationDTO updateOrganization(Long id, UpdateOrganizationRequest request);
}
