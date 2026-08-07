package com.trackflow.service;

import com.trackflow.dto.OrganizationDTO;
import com.trackflow.dto.UpdateOrganizationRequest;
import com.trackflow.entity.Organization;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.OrganizationMapper;
import com.trackflow.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation providing business logic for Organization Management.
 */
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional(readOnly = true)
    public OrganizationDTO getOrganizationById(Long id) {
        Organization organization = findOrganizationOrThrow(id);
        return organizationMapper.toDTO(organization);
    }

    @Override
    @Transactional
    public OrganizationDTO updateOrganization(Long id, UpdateOrganizationRequest request) {
        Organization organization = findOrganizationOrThrow(id);

        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null) {
            organization.setLogoUrl(request.getLogoUrl());
        }

        Organization updatedOrganization = organizationRepository.save(organization);
        return organizationMapper.toDTO(updatedOrganization);
    }

    private Organization findOrganizationOrThrow(Long id) {
        return organizationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
    }
}
