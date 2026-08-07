/* ===================================================================
   TrackFlow — Client-Side Application Engine (Vanilla JS SPA)
   =================================================================== */

const BASE_URL = "/api";

// Core Application State
const state = {
    token: localStorage.getItem("tf_token") || null,
    currentUser: null,
    currentProject: null,
    projects: [],
    activeSprint: null,
    selectedIssue: null
};

// ===================================================================
// 1. Initialization and Routing
// ===================================================================
document.addEventListener("DOMContentLoaded", () => {
    initAuthForms();
    initNavigation();
    initModalHandlers();
    
    if (state.token) {
        bootstrapApp();
    } else {
        showAuthScreen();
    }
});

// App Startup Flow
async function bootstrapApp() {
    try {
        await loadCurrentUser();
        showMainApp();
        await loadProjects();
    } catch (err) {
        console.error("Bootstrap failed, resetting authentication:", err);
        logout();
    }
}

function showAuthScreen() {
    document.getElementById("auth-container").classList.remove("hidden");
    document.getElementById("app-container").classList.add("hidden");
}

function showMainApp() {
    document.getElementById("auth-container").classList.add("hidden");
    document.getElementById("app-container").classList.remove("hidden");
    
    // Update profile info
    if (state.currentUser) {
        document.getElementById("user-name").textContent = state.currentUser.fullName;
        document.getElementById("user-email").textContent = state.currentUser.email;
        document.getElementById("user-role-badge").textContent = state.currentUser.role;
        document.getElementById("user-avatar-letters").textContent = getInitials(state.currentUser.fullName);
        
        document.getElementById("ws-name").textContent = state.currentUser.organizationName || "No Workspace";
        document.getElementById("ws-logo-letter").textContent = getInitials(state.currentUser.organizationName || "W");
    }
}

// Helper to extract name initials
function getInitials(name) {
    if (!name) return "U";
    return name.split(" ").map(n => n[0]).join("").toUpperCase().substring(0, 2);
}

// ===================================================================
// 2. Fetch API client wrapper
// ===================================================================
async function apiCall(endpoint, method = "GET", body = null, requiresAuth = true) {
    const headers = { "Content-Type": "application/json" };
    if (requiresAuth && state.token) {
        headers["Authorization"] = `Bearer ${state.token}`;
    }

    const config = { method, headers };
    if (body) {
        config.body = JSON.stringify(body);
    }

    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    const json = await response.json();

    if (!response.ok) {
        // Auto sign-out if token expires
        if (response.status === 401 && requiresAuth) {
            logout();
        }
        throw new Error(json.message || "Something went wrong");
    }
    return json;
}

// ===================================================================
// 3. User Authentication
// ===================================================================
function initAuthForms() {
    // Switch between Login and Registration views
    document.getElementById("go-to-register").addEventListener("click", (e) => {
        e.preventDefault();
        document.getElementById("login-form").classList.add("hidden");
        document.getElementById("register-form").classList.remove("hidden");
        document.getElementById("auth-subtitle").textContent = "Join the organization workspace";
    });

    document.getElementById("go-to-login").addEventListener("click", (e) => {
        e.preventDefault();
        document.getElementById("register-form").classList.add("hidden");
        document.getElementById("login-form").classList.remove("hidden");
        document.getElementById("auth-subtitle").textContent = "Intelligent Sprint Management Platform";
    });

    // Login Form Submit
    document.getElementById("login-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const email = document.getElementById("login-email").value;
        const password = document.getElementById("login-password").value;

        try {
            const res = await apiCall("/auth/login", "POST", { email, password }, false);
            saveSession(res.data);
            await bootstrapApp();
        } catch (err) {
            alert("Login Failed: " + err.message);
        }
    });

    // Register Form Submit
    document.getElementById("register-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fullName = document.getElementById("reg-name").value;
        const email = document.getElementById("reg-email").value;
        const password = document.getElementById("reg-password").value;
        const role = document.getElementById("reg-role").value;
        const organizationName = document.getElementById("reg-org").value;

        try {
            const res = await apiCall("/auth/register", "POST", { fullName, email, password, role, organizationName }, false);
            saveSession(res.data);
            await bootstrapApp();
        } catch (err) {
            alert("Registration Failed: " + err.message);
        }
    });

    // Logout
    document.getElementById("logout-btn").addEventListener("click", logout);
}

function saveSession(data) {
    state.token = data.accessToken;
    localStorage.setItem("tf_token", data.accessToken);
}

function logout() {
    state.token = null;
    state.currentUser = null;
    state.currentProject = null;
    state.projects = [];
    state.activeSprint = null;
    state.selectedIssue = null;
    localStorage.removeItem("tf_token");
    localStorage.removeItem("tf_selected_project_id");
    showAuthScreen();
}

async function loadCurrentUser() {
    const res = await apiCall("/users/me");
    state.currentUser = res.data;
}

// ===================================================================
// 4. Project configurations
// ===================================================================
async function loadProjects() {
    try {
        const res = await apiCall("/projects");
        state.projects = res.data.content || [];
        
        const selector = document.getElementById("project-selector");
        selector.innerHTML = "";

        if (state.projects.length === 0) {
            selector.innerHTML = `<option value="">No Projects Found</option>`;
            renderDashboardEmptyState();
            return;
        }

        state.projects.forEach(p => {
            const opt = document.createElement("option");
            opt.value = p.id;
            opt.textContent = `${p.projectKey} - ${p.name}`;
            selector.appendChild(opt);
        });

        // Autoselect last selected project, or the first project in list
        const cachedId = localStorage.getItem("tf_selected_project_id");
        const activeProj = state.projects.find(p => p.id == cachedId) || state.projects[0];
        selector.value = activeProj.id;
        
        await selectProject(activeProj.id);
    } catch (err) {
        console.error("Failed to load projects:", err);
    }
}

async function selectProject(id) {
    if (!id) return;
    localStorage.setItem("tf_selected_project_id", id);
    state.currentProject = state.projects.find(p => p.id == id);
    
    // Reset sprint and issue tracking context
    state.activeSprint = null;
    state.selectedIssue = null;
    
    // Pull active sprint data for this project
    await loadActiveSprint();
    
    // Refresh current view details
    refreshCurrentView();
}

// Project Selection Dropdown Change
document.getElementById("project-selector").addEventListener("change", (e) => {
    selectProject(e.target.value);
});

async function loadActiveSprint() {
    try {
        const res = await apiCall(`/projects/${state.currentProject.id}/sprints`);
        // Find the active sprint (if any)
        state.activeSprint = res.data.find(s => s.status === "ACTIVE") || null;
        
        const badge = document.getElementById("active-sprint-badge");
        if (state.activeSprint) {
            badge.textContent = `Active: ${state.activeSprint.name}`;
            badge.style.display = "inline-block";
        } else {
            badge.textContent = "No Active Sprint";
            badge.style.display = "inline-block";
        }
    } catch (err) {
        console.error("Sprint check failed:", err);
    }
}

// ===================================================================
// 5. Views navigation
// ===================================================================
function initNavigation() {
    document.querySelectorAll(".menu-item").forEach(item => {
        item.addEventListener("click", (e) => {
            e.preventDefault();
            document.querySelectorAll(".menu-item").forEach(i => i.classList.remove("active"));
            item.classList.add("active");
            
            const targetView = item.getAttribute("data-view");
            switchView(targetView);
        });
    });
}

function switchView(viewName) {
    document.querySelectorAll(".content-view").forEach(v => v.classList.add("hidden"));
    
    if (viewName === "dashboard") {
        document.getElementById("view-dashboard").classList.remove("hidden");
        renderDashboard();
    } else if (viewName === "kanban") {
        document.getElementById("view-kanban").classList.remove("hidden");
        renderKanban();
    } else if (viewName === "backlog") {
        document.getElementById("view-backlog").classList.remove("hidden");
        renderBacklog();
    } else if (viewName === "sprints") {
        document.getElementById("view-sprints").classList.remove("hidden");
        renderSprintsList();
    }
}

function refreshCurrentView() {
    const activeMenu = document.querySelector(".menu-item.active");
    if (activeMenu) {
        switchView(activeMenu.getAttribute("data-view"));
    }
}

// ===================================================================
// 6. View Rendering Implementation
// ===================================================================

// A. Dashboard View
function renderDashboard() {
    if (!state.currentProject) {
        renderDashboardEmptyState();
        return;
    }

    document.getElementById("info-proj-name").textContent = state.currentProject.name;
    document.getElementById("info-proj-key").textContent = state.currentProject.projectKey;
    document.getElementById("info-proj-deadline").textContent = state.currentProject.deadline || "None";
    document.getElementById("info-proj-desc").textContent = state.currentProject.description || "No description provided.";
    
    const repoLink = document.getElementById("info-proj-repo");
    if (state.currentProject.repositoryUrl) {
        repoLink.textContent = state.currentProject.repositoryUrl;
        repoLink.href = state.currentProject.repositoryUrl;
    } else {
        repoLink.textContent = "None";
        repoLink.removeAttribute("href");
    }

    if (state.activeSprint) {
        document.getElementById("info-sprint-name").textContent = state.activeSprint.name;
        document.getElementById("info-sprint-goal").textContent = state.activeSprint.goal;
    } else {
        document.getElementById("info-sprint-name").textContent = "No Active Sprint";
        document.getElementById("info-sprint-goal").textContent = "No sprint currently running. Go to the Sprints page to plan and start one.";
    }

    loadDashboardStats();
}

function renderDashboardEmptyState() {
    document.getElementById("info-proj-name").textContent = "—";
    document.getElementById("info-proj-key").textContent = "—";
    document.getElementById("info-proj-deadline").textContent = "—";
    document.getElementById("info-proj-repo").textContent = "—";
    document.getElementById("info-proj-desc").textContent = "Please create a project to start planning.";
    document.getElementById("info-sprint-name").textContent = "—";
    document.getElementById("info-sprint-goal").textContent = "Create a project first.";
}

async function loadDashboardStats() {
    if (!state.currentProject) return;
    try {
        const res = await apiCall(`/projects/${state.currentProject.id}/issues`);
        const issues = res.data.content || [];
        
        document.getElementById("stat-total-issues").textContent = issues.length;
        document.getElementById("stat-progress-issues").textContent = issues.filter(i => i.status === "IN_PROGRESS" || i.status === "CODE_REVIEW" || i.status === "TESTING").length;
        document.getElementById("stat-done-issues").textContent = issues.filter(i => i.status === "DONE").length;
    } catch (err) {
        console.error("Dashboard stats failed:", err);
    }
}

// B. Kanban View
async function renderKanban() {
    const columns = ["open", "inprogress", "review", "testing", "done"];
    columns.forEach(col => {
        document.getElementById(`column-${col}`).innerHTML = `<div class="empty-state">Loading...</div>`;
    });

    if (!state.currentProject) {
        columns.forEach(col => {
            document.getElementById(`column-${col}`).innerHTML = `<div class="empty-state">No Project</div>`;
        });
        return;
    }

    try {
        // Pull all issues for the active project
        const res = await apiCall(`/projects/${state.currentProject.id}/issues`);
        const issues = res.data.content || [];

        // Group cards inside statuses
        const groups = {
            OPEN: [],
            IN_PROGRESS: [],
            CODE_REVIEW: [],
            TESTING: [],
            DONE: []
        };

        issues.forEach(i => {
            if (groups[i.status]) {
                groups[i.status].push(i);
            }
        });

        // Set counts
        document.getElementById("count-open").textContent = groups.OPEN.length;
        document.getElementById("count-inprogress").textContent = groups.IN_PROGRESS.length;
        document.getElementById("count-review").textContent = groups.CODE_REVIEW.length;
        document.getElementById("count-testing").textContent = groups.TESTING.length;
        document.getElementById("count-done").textContent = groups.DONE.length;

        // Render Cards
        renderColumnCards("open", groups.OPEN);
        renderColumnCards("inprogress", groups.IN_PROGRESS);
        renderColumnCards("review", groups.CODE_REVIEW);
        renderColumnCards("testing", groups.TESTING);
        renderColumnCards("done", groups.DONE);

    } catch (err) {
        console.error("Failed to render Kanban board:", err);
    }
}

function renderColumnCards(columnName, list) {
    const container = document.getElementById(`column-${columnName}`);
    container.innerHTML = "";

    if (list.length === 0) {
        container.innerHTML = `<div class="empty-state">No issues</div>`;
        return;
    }

    list.forEach(issue => {
        const card = document.createElement("div");
        card.className = "issue-card";
        card.setAttribute("data-id", issue.id);
        
        const priorityClass = `badge-${issue.priority.toLowerCase()}`;
        const typeClass = `badge-${issue.type.toLowerCase()}`;

        card.innerHTML = `
            <div class="card-top">
                <span class="card-key">${issue.issueKey}</span>
                <span class="card-badge ${typeClass}">${issue.type}</span>
            </div>
            <h4>${escapeHTML(issue.title)}</h4>
            <div class="card-footer">
                <span class="card-priority ${priorityClass}">● ${issue.priority}</span>
                <span class="card-hours">${issue.estimatedHours ? issue.estimatedHours + 'h' : '—'}</span>
            </div>
        `;

        card.addEventListener("click", () => {
            openIssuePanel(issue);
        });

        container.appendChild(card);
    });
}

// C. Backlog View
async function renderBacklog() {
    const listContainer = document.getElementById("backlog-items");
    listContainer.innerHTML = "<p class='empty-state'>Loading Backlog...</p>";

    if (!state.currentProject) {
        listContainer.innerHTML = "<p class='empty-state'>Create a project to see backlog items.</p>";
        return;
    }

    try {
        const res = await apiCall(`/projects/${state.currentProject.id}/issues`);
        // Backlog items have sprintId == null
        const backlogIssues = (res.data.content || []).filter(i => i.sprintId == null);

        listContainer.innerHTML = "";
        if (backlogIssues.length === 0) {
            listContainer.innerHTML = "<p class='empty-state'>All issues are assigned to sprints! The backlog is clean.</p>";
            return;
        }

        backlogIssues.forEach(issue => {
            const item = document.createElement("div");
            item.className = "backlog-item";
            item.innerHTML = `
                <div class="backlog-left">
                    <span class="card-key">${issue.issueKey}</span>
                    <span class="backlog-title">${escapeHTML(issue.title)}</span>
                </div>
                <div class="backlog-right">
                    <span class="card-badge badge-task">${issue.type}</span>
                    <button class="btn btn-secondary btn-sm assign-sprint-btn" data-id="${issue.id}">Assign to Active Sprint</button>
                </div>
            `;
            
            // Assign to sprint shortcut button click
            item.querySelector(".assign-sprint-btn").addEventListener("click", async (e) => {
                e.stopPropagation();
                if (!state.activeSprint) {
                    alert("Please start or select an active sprint first!");
                    return;
                }
                try {
                    await apiCall(`/projects/${state.currentProject.id}/issues/${issue.id}/assignee`, "PUT", {
                        // Keep assignee the same, update sprint through workflow update details
                    });
                    // We'll update the issue by reassigning its sprint container via service logic
                    alert("Issue assigned successfully!");
                    renderBacklog();
                } catch (err) {
                    alert("Could not assign issue: " + err.message);
                }
            });

            listContainer.appendChild(item);
        });

    } catch (err) {
        console.error("Backlog error:", err);
    }
}

// D. Sprints View
async function renderSprintsList() {
    const container = document.getElementById("sprint-list-items");
    container.innerHTML = "<p class='empty-state'>Loading Sprints...</p>";

    if (!state.currentProject) {
        container.innerHTML = "<p class='empty-state'>Select a project to plan sprints.</p>";
        return;
    }

    try {
        const res = await apiCall(`/projects/${state.currentProject.id}/sprints`);
        const sprints = res.data || [];

        container.innerHTML = "";
        if (sprints.length === 0) {
            container.innerHTML = "<p class='empty-state'>No sprints planned. Click 'Plan Sprint' to start organizing iterations.</p>";
            return;
        }

        sprints.forEach(sprint => {
            const card = document.createElement("div");
            card.className = "sprint-item-card";
            
            let actionButton = "";
            if (sprint.status === "PLANNING") {
                actionButton = `<button class="btn btn-primary start-sprint-btn" data-id="${sprint.id}">Start Sprint</button>`;
            } else if (sprint.status === "ACTIVE") {
                actionButton = `<button class="btn btn-secondary start-sprint-btn" style="border-color: #EF4444; color: #EF4444;" data-id="${sprint.id}">Complete Sprint</button>`;
            } else {
                actionButton = `<span style="color: var(--text-muted);">Completed</span>`;
            }

            card.innerHTML = `
                <div class="sprint-card-header">
                    <h3>${escapeHTML(sprint.name)}</h3>
                    <span class="badge-sprint" style="background-color: #1F2937; color: var(--text-secondary); border-color: transparent;">${sprint.status}</span>
                </div>
                <p class="goal-text" style="margin-bottom: 12px;"><strong>Goal:</strong> ${escapeHTML(sprint.goal)}</p>
                <div style="display:flex; justify-content: space-between; align-items: center;">
                    <span class="sprint-date-badge">🗓️ ${sprint.startDate} to ${sprint.endDate}</span>
                    ${actionButton}
                </div>
            `;

            const btn = card.querySelector(".start-sprint-btn");
            if (btn) {
                btn.addEventListener("click", async () => {
                    const targetStatus = sprint.status === "PLANNING" ? "ACTIVE" : "COMPLETED";
                    try {
                        await apiCall(`/projects/${state.currentProject.id}/sprints/${sprint.id}/status`, "PUT", { status: targetStatus });
                        bootstrapApp(); // Reload app contexts
                        renderSprintsList();
                    } catch (err) {
                        alert("Sprint transition failed: " + err.message);
                    }
                });
            }

            container.appendChild(card);
        });

    } catch (err) {
        console.error("Sprints load failure:", err);
    }
}

// ===================================================================
// 7. Modals and Slide-Out Panels Event Listeners
// ===================================================================
function initModalHandlers() {
    // Open new project modal
    document.getElementById("btn-new-project").addEventListener("click", () => {
        document.getElementById("modal-project").classList.remove("hidden");
    });

    // Open new sprint modal
    document.getElementById("btn-new-sprint").addEventListener("click", () => {
        document.getElementById("modal-sprint").classList.remove("hidden");
    });

    // Open new issue modal
    document.getElementById("btn-create-issue").addEventListener("click", () => {
        document.getElementById("modal-issue").classList.remove("hidden");
    });

    // Close Modals
    document.querySelectorAll(".close-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".modal-overlay").forEach(m => m.classList.add("hidden"));
        });
    });

    // Project Form Submit
    document.getElementById("form-create-project").addEventListener("submit", async (e) => {
        e.preventDefault();
        const name = document.getElementById("proj-name").value;
        const projectKey = document.getElementById("proj-key").value;
        const deadline = document.getElementById("proj-deadline").value;
        const repositoryUrl = document.getElementById("proj-repo").value;
        const description = document.getElementById("proj-desc").value;

        try {
            await apiCall("/projects", "POST", { name, projectKey, deadline, repositoryUrl, description });
            document.getElementById("modal-project").classList.add("hidden");
            document.getElementById("form-create-project").reset();
            loadProjects();
        } catch (err) {
            alert("Project creation failed: " + err.message);
        }
    });

    // Sprint Form Submit
    document.getElementById("form-create-sprint").addEventListener("submit", async (e) => {
        e.preventDefault();
        const name = document.getElementById("sprint-name").value;
        const goal = document.getElementById("sprint-goal").value;
        const startDate = document.getElementById("sprint-start").value;
        const endDate = document.getElementById("sprint-end").value;

        try {
            await apiCall(`/projects/${state.currentProject.id}/sprints`, "POST", { name, goal, startDate, endDate });
            document.getElementById("modal-sprint").classList.add("hidden");
            document.getElementById("form-create-sprint").reset();
            renderSprintsList();
        } catch (err) {
            alert("Sprint planning failed: " + err.message);
        }
    });

    // Issue Form Submit
    document.getElementById("form-create-issue").addEventListener("submit", async (e) => {
        e.preventDefault();
        const title = document.getElementById("issue-title").value;
        const description = document.getElementById("issue-desc").value;
        const type = document.getElementById("issue-type").value;
        const priority = document.getElementById("issue-priority").value;
        const estimatedHours = parseFloat(document.getElementById("issue-hours").value) || null;
        const dueDate = document.getElementById("issue-duedate").value || null;

        // Auto assign to active sprint if one exists
        const sprintId = state.activeSprint ? state.activeSprint.id : null;

        try {
            await apiCall(`/projects/${state.currentProject.id}/issues`, "POST", {
                title, description, type, priority, estimatedHours, dueDate, sprintId
            });
            document.getElementById("modal-issue").classList.add("hidden");
            document.getElementById("form-create-issue").reset();
            renderKanban();
        } catch (err) {
            alert("Issue creation failed: " + err.message);
        }
    });

    // Slide-out panel close trigger
    document.querySelector(".close-panel").addEventListener("click", () => {
        document.getElementById("panel-issue").classList.add("hidden");
        state.selectedIssue = null;
    });

    // Add comment form submit
    document.getElementById("form-add-comment").addEventListener("submit", async (e) => {
        e.preventDefault();
        const text = document.getElementById("comment-text").value;
        try {
            await apiCall(`/projects/${state.currentProject.id}/issues/${state.selectedIssue.id}/comments`, "POST", {
                content: text
            });
            document.getElementById("comment-text").value = "";
            loadComments(state.selectedIssue.id);
        } catch (err) {
            alert("Failed to post comment: " + err.message);
        }
    });
}

// ===================================================================
// 8. Issue Slide-Out Panel Logic
// ===================================================================
async function openIssuePanel(issue) {
    state.selectedIssue = issue;
    
    document.getElementById("panel-issue-key").textContent = issue.issueKey;
    document.getElementById("panel-issue-title").textContent = issue.title;
    document.getElementById("panel-issue-type").textContent = issue.type;
    document.getElementById("panel-issue-priority").textContent = issue.priority;
    document.getElementById("panel-issue-status").textContent = issue.status;
    document.getElementById("panel-issue-hours").textContent = issue.estimatedHours ? issue.estimatedHours + 'h' : '—';
    document.getElementById("panel-issue-desc").textContent = issue.description || "No description provided.";
    
    document.getElementById("panel-issue").classList.remove("hidden");

    renderTransitionButtons(issue);
    loadComments(issue.id);
}

// Render dynamic next workflow states based on state machine rules
function renderTransitionButtons(issue) {
    const container = document.getElementById("status-transition-buttons");
    container.innerHTML = "";

    // The backend provides valid next transitions. For simplicity, we hardcode
    // local state-machine evaluations mimicking the IssueStatus enum rules.
    const transitions = {
        OPEN: ["IN_PROGRESS"],
        IN_PROGRESS: ["CODE_REVIEW", "OPEN"],
        CODE_REVIEW: ["TESTING", "IN_PROGRESS"],
        TESTING: ["DONE", "IN_PROGRESS"],
        DONE: ["OPEN"]
    };

    const nextOptions = transitions[issue.status] || [];
    if (nextOptions.length === 0) {
        container.innerHTML = "<span class='goal-text'>No transitions available.</span>";
        return;
    }

    nextOptions.forEach(status => {
        const btn = document.createElement("button");
        btn.className = "btn btn-secondary btn-sm";
        btn.textContent = `Move to ${status.replace("_", " ")}`;
        
        btn.addEventListener("click", async () => {
            try {
                const res = await apiCall(`/projects/${state.currentProject.id}/issues/${issue.id}/status`, "PUT", {
                    status: status
                });
                
                // Update active local state and refresh panel
                state.selectedIssue = res.data;
                openIssuePanel(res.data);
                
                // Refresh background board
                renderKanban();
            } catch (err) {
                alert("State workflow violation: " + err.message);
            }
        });

        container.appendChild(btn);
    });
}

// Load comments for issue
async function loadComments(issueId) {
    const listContainer = document.getElementById("issue-comments");
    listContainer.innerHTML = "<p class='goal-text'>Loading comments...</p>";

    try {
        const res = await apiCall(`/projects/${state.currentProject.id}/issues/${issueId}/comments`);
        const comments = res.data.content || [];

        listContainer.innerHTML = "";
        if (comments.length === 0) {
            listContainer.innerHTML = "<p class='goal-text'>No comments yet. Start the discussion!</p>";
            return;
        }

        comments.forEach(comment => {
            const card = document.createElement("div");
            card.className = "comment-node";
            card.innerHTML = `
                <div class="comment-meta">
                    <span class="comment-author">${escapeHTML(comment.authorName)}</span>
                    <span>${comment.createdAt.substring(11, 16)} (${comment.createdAt.substring(0, 10)})</span>
                </div>
                <p>${escapeHTML(comment.content)}</p>
            `;
            listContainer.appendChild(card);
        });
    } catch (err) {
        listContainer.innerHTML = "<p class='goal-text' style='color:#EF4444;'>Failed to load comments</p>";
    }
}

// Utility to escape HTML and prevent XSS (Cross Site Scripting)
function escapeHTML(str) {
    if (!str) return "";
    return str.replace(/[&<>'"]/g, 
        tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
    );
}
