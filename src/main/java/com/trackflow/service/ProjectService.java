package com.trackflow.service;

import com.trackflow.dto.AddProjectMemberRequest;
import com.trackflow.dto.CreateProjectRequest;
import com.trackflow.dto.ProjectDTO;
import com.trackflow.dto.ProjectMemberDTO;
import com.trackflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface defining project and membership management operations.
 */
public interface ProjectService {

    ProjectDTO createProject(CreateProjectRequest request, User creator);

    ProjectDTO getProjectById(Long id);

    Page<ProjectDTO> getProjectsByOrganization(Long orgId, Pageable pageable);

    ProjectMemberDTO addMember(Long projectId, AddProjectMemberRequest request);

    void removeMember(Long projectId, Long userId);

    List<ProjectMemberDTO> getProjectMembers(Long projectId);
}
