# TrackFlow Live API Demo Script
# Runs against local server at port 8080

$baseUrl = "http://localhost:8080/api"
$Headers = @{
    "Content-Type" = "application/json"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "         TRACKFLOW ENTERPRISE BACKEND DEMO FLOW            " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# -----------------------------------------------------------------
# 1. Register Org Admin & Create Organization
# -----------------------------------------------------------------
Write-Host "`n[1/6] Registering Org Admin & Workspace 'InnovateX'..." -ForegroundColor Yellow
$regBody = @{
    fullName = "Ajinkya Satam"
    email = "ajinkya@innovatex.io"
    password = "SecurePassword123!"
    role = "ORG_ADMIN"
    organizationName = "InnovateX Studio"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $regBody -Headers $Headers
    $token = $regResponse.data.accessToken
    $orgId = $regResponse.data.organizationId
    $userId = $regResponse.data.userId
    
    Write-Host "✅ Registered Successfully!" -ForegroundColor Green
    Write-Host "   - User ID: $userId" -ForegroundColor Gray
    Write-Host "   - Organization: $($regResponse.data.organizationName) (ID: $orgId)" -ForegroundColor Gray
    Write-Host "   - Access Token generated (JWT)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Registration failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "   Server response: $($reader.ReadToEnd())" -ForegroundColor Red
    }
    Exit
}

# Attach JWT Authorization header for all subsequent requests
$Headers.Add("Authorization", "Bearer $token")
Start-Sleep -Seconds 1

# -----------------------------------------------------------------
# 2. Get User Profile (/users/me)
# -----------------------------------------------------------------
Write-Host "`n[2/6] Fetching current user profile (/users/me)..." -ForegroundColor Yellow
try {
    $profileResponse = Invoke-RestMethod -Uri "$baseUrl/users/me" -Method Get -Headers $Headers
    Write-Host "✅ Profile Fetched!" -ForegroundColor Green
    Write-Host "   - Name: $($profileResponse.data.fullName)" -ForegroundColor Gray
    Write-Host "   - Role: $($profileResponse.data.role)" -ForegroundColor Gray
    Write-Host "   - Email: $($profileResponse.data.email)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Profile fetch failed: $_" -ForegroundColor Red
}
Start-Sleep -Seconds 1

# -----------------------------------------------------------------
# 3. Create a Project
# -----------------------------------------------------------------
Write-Host "`n[3/6] Creating a new Project 'TrackFlow Core'..." -ForegroundColor Yellow
$projectBody = @{
    name = "TrackFlow Core Backend"
    description = "Enterprise engine built with Spring Boot"
    deadline = "2026-12-31"
    repositoryUrl = "https://github.com/AjinkyaSatam/TrackFlow"
    projectKey = "TF"
} | ConvertTo-Json

try {
    $projResponse = Invoke-RestMethod -Uri "$baseUrl/projects" -Method Post -Body $projectBody -Headers $Headers
    $projectId = $projResponse.data.id
    Write-Host "✅ Project Created!" -ForegroundColor Green
    Write-Host "   - Project ID: $projectId" -ForegroundColor Gray
    Write-Host "   - Key: $($projResponse.data.projectKey)" -ForegroundColor Gray
    Write-Host "   - Status: $($projResponse.data.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Project creation failed: $_" -ForegroundColor Red
}
Start-Sleep -Seconds 1

# -----------------------------------------------------------------
# 4. Create a Sprint
# -----------------------------------------------------------------
Write-Host "`n[4/6] Creating Sprint 1 for Project $projectId..." -ForegroundColor Yellow
$sprintBody = @{
    name = "Sprint 1: Core API Setup"
    goal = "Build security, databases, and core issues workflows"
    startDate = "2026-08-01"
    endDate = "2026-08-14"
} | ConvertTo-Json

try {
    $sprintResponse = Invoke-RestMethod -Uri "$baseUrl/projects/$projectId/sprints" -Method Post -Body $sprintBody -Headers $Headers
    $sprintId = $sprintResponse.data.id
    Write-Host "✅ Sprint Scheduled!" -ForegroundColor Green
    Write-Host "   - Sprint ID: $sprintId" -ForegroundColor Gray
    Write-Host "   - Status: $($sprintResponse.data.status) (Planned)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Sprint creation failed: $_" -ForegroundColor Red
}
Start-Sleep -Seconds 1

# -----------------------------------------------------------------
# 5. Create an Issue
# -----------------------------------------------------------------
Write-Host "`n[5/6] Creating and assigning an Issue to Sprint $sprintId..." -ForegroundColor Yellow
$issueBody = @{
    title = "Configure Spring Security Filter Chain"
    description = "Setup JwtAuthenticationFilter and enable method-level PreAuthorize check rules."
    type = "FEATURE"
    priority = "HIGH"
    dueDate = "2026-08-10"
    estimatedHours = 6.5
    sprintId = $sprintId
} | ConvertTo-Json

try {
    $issueResponse = Invoke-RestMethod -Uri "$baseUrl/projects/$projectId/issues" -Method Post -Body $issueBody -Headers $Headers
    $issueId = $issueResponse.data.id
    Write-Host "✅ Issue Created!" -ForegroundColor Green
    Write-Host "   - Key: $($issueResponse.data.issueKey) (Autogenerated)" -ForegroundColor Gray
    Write-Host "   - Title: $($issueResponse.data.title)" -ForegroundColor Gray
    Write-Host "   - Status: $($issueResponse.data.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Issue creation failed: $_" -ForegroundColor Red
}
Start-Sleep -Seconds 1

# -----------------------------------------------------------------
# 6. Add Comment
# -----------------------------------------------------------------
Write-Host "`n[6/6] Adding a status comment on issue $issueId..." -ForegroundColor Yellow
$commentBody = @{
    content = "Secured JWT filter logic, ready for testing review."
} | ConvertTo-Json

try {
    $commentResponse = Invoke-RestMethod -Uri "$baseUrl/projects/$projectId/issues/$issueId/comments" -Method Post -Body $commentBody -Headers $Headers
    Write-Host "✅ Comment Added!" -ForegroundColor Green
    Write-Host "   - Author: $($commentResponse.data.authorName)" -ForegroundColor Gray
    Write-Host "   - Content: $($commentResponse.data.content)" -ForegroundColor Gray
    Write-Host "   - Timestamp: $($commentResponse.data.createdAt)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Comment add failed: $_" -ForegroundColor Red
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "                 DEMO COMPLETED SUCCESSFULLY               " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
