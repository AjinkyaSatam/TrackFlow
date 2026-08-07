package com.trackflow.mapper;

import com.trackflow.dto.ProjectDTO;
import com.trackflow.dto.ProjectMemberDTO;
import com.trackflow.entity.Project;
import com.trackflow.entity.ProjectMember;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between Project/ProjectMember entities and DTOs.
 */
@Component
public class ProjectMapper {

    public ProjectDTO toDTO(Project project) {
        if (project == null) {
            return null;
        }

        Long createdById = null;
        String createdByName = null;

        if (project.getCreatedBy() != null) {
            createdById = project.getCreatedBy().getId();
            createdByName = project.getCreatedBy().getFullName();
        }

        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .status(project.getStatus().name())
                .repositoryUrl(project.getRepositoryUrl())
                .projectKey(project.getProjectKey())
                .organizationId(project.getOrganization() != null ? project.getOrganization().getId() : null)
                .createdById(createdById)
                .createdByName(createdByName)
                .createdAt(project.getCreatedAt())
                .build();
    }

    public ProjectMemberDTO toMemberDTO(ProjectMember member) {
        if (member == null) {
            return null;
        }

        return ProjectMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser() != null ? member.getUser().getId() : null)
                .fullName(member.getUser() != null ? member.getUser().getFullName() : null)
                .email(member.getUser() != null ? member.getUser().getEmail() : null)
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
