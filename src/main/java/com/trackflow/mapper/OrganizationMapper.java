package com.trackflow.mapper;

import com.trackflow.dto.OrganizationDTO;
import com.trackflow.entity.Organization;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to convert between {@link Organization} entity and {@link OrganizationDTO}.
 */
@Component
public class OrganizationMapper {

    public OrganizationDTO toDTO(Organization organization) {
        if (organization == null) {
            return null;
        }

        Long ownerId = null;
        String ownerName = null;

        if (organization.getOwner() != null) {
            ownerId = organization.getOwner().getId();
            ownerName = organization.getOwner().getFullName();
        }

        return OrganizationDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .website(organization.getWebsite())
                .logoUrl(organization.getLogoUrl())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .createdAt(organization.getCreatedAt())
                .build();
    }
}
