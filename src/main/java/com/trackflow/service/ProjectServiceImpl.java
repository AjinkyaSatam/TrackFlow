package com.trackflow.service;

import com.trackflow.dto.AddProjectMemberRequest;
import com.trackflow.dto.CreateProjectRequest;
import com.trackflow.dto.ProjectDTO;
import com.trackflow.dto.ProjectMemberDTO;
import com.trackflow.entity.Project;
import com.trackflow.entity.ProjectMember;
import com.trackflow.entity.Role;
import com.trackflow.entity.User;
import com.trackflow.exception.BadRequestException;
import com.trackflow.exception.DuplicateResourceException;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.ProjectMapper;
import com.trackflow.repository.ProjectMemberRepository;
import com.trackflow.repository.ProjectRepository;
import com.trackflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation providing Project & Member Management.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request, User creator) {
        if (creator.getOrganization() == null) {
            throw new BadRequestException("User must belong to an organization to create projects");
        }

        // 1. Verify that project key is globally unique
        if (projectRepository.existsByProjectKeyIgnoreCaseAndIsDeletedFalse(request.getProjectKey())) {
            throw new DuplicateResourceException("Project", "projectKey", request.getProjectKey());
        }

        // 2. Create the Project
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .projectKey(request.getProjectKey().toUpperCase())
                .repositoryUrl(request.getRepositoryUrl())
                .organization(creator.getOrganization())
                .createdBy(creator)
                .build();

        project = projectRepository.save(project);

        // 3. Auto-assign the creator as the PROJECT_MANAGER of the project
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(creator)
                .role(Role.PROJECT_MANAGER)
                .joinedAt(LocalDateTime.now())
                .build();
        projectMemberRepository.save(member);

        return projectMapper.toDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return projectMapper.toDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getProjectsByOrganization(Long orgId, Pageable pageable) {
        return projectRepository.findByOrganizationIdAndIsDeletedFalse(orgId, pageable)
                .map(projectMapper::toDTO);
    }

    @Override
    @Transactional
    public ProjectMemberDTO addMember(Long projectId, AddProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User user = userRepository.findById(request.getUserId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Enforce boundary constraint: Cannot add a user from Org B to Org A's project
        if (user.getOrganization() == null || !user.getOrganization().getId().equals(project.getOrganization().getId())) {
            throw new BadRequestException("Cannot assign members outside of the project's organization");
        }

        // Check if user is already a member
        if (projectMemberRepository.existsByProjectIdAndUserIdAndIsDeletedFalse(projectId, request.getUserId())) {
            throw new BadRequestException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(request.getRole())
                .joinedAt(LocalDateTime.now())
                .build();

        ProjectMember savedMember = projectMemberRepository.save(member);
        return projectMapper.toMemberDTO(savedMember);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this project"));

        member.setDeleted(true);
        projectMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectIdWithUsers(projectId).stream()
                .map(projectMapper::toMemberDTO)
                .toList();
    }
}
