const REVIEW_LOG_KEY = 'contributor-console-live-logs-v2';
const CONTRIBUTOR_APP_STATUS = {
  PENDING: { label: 'Pending Review', className: 'status-badge' },
  APPROVED: { label: 'Approved', className: 'status-badge approved' },
  REJECTED: { label: 'Rejected', className: 'status-badge rejected' }
};
const RESOURCE_STATUS = {
  0: { label: 'Draft', className: 'status-badge none' },
  1: { label: 'Pending Approval', className: 'status-badge' },
  2: { label: 'Approved', className: 'status-badge approved' },
  3: { label: 'Rejected', className: 'status-badge rejected' },
  4: { label: 'Archived', className: 'status-badge revoked' }
};

const overviewGrid = document.getElementById('overviewGrid');
const backButton = document.getElementById('backButton');
const storageStatus = document.getElementById('storageStatus');
const pendingBadge = document.getElementById('pendingBadge');
const queueTableBody = document.getElementById('queueTableBody');
const contributorRegistry = document.getElementById('contributorRegistry');
const submissionReviewPanel = document.getElementById('submissionReviewPanel');
const reviewSummary = document.getElementById('reviewSummary');
const reviewForm = document.getElementById('reviewForm');
const reviewFeedback = document.getElementById('reviewFeedback');
const approveButton = document.getElementById('approveButton');
const rejectButton = document.getElementById('rejectButton');
const activityLog = document.getElementById('activityLog');
const toast = document.getElementById('toast');
const tabBar = document.getElementById('tabBar');
const navItems = Array.from(document.querySelectorAll('.nav__item[data-module]'));
const modulePanels = Array.from(document.querySelectorAll('[data-module-panel]'));

const state = {
  currentAdmin: null,
  usersById: new Map(),
  contributorApplications: [],
  pendingResources: [],
  allResources: [],
  selectedContext: null,
  activeTab: 'review',
  logs: loadLogs()
};

let toastTimer = null;

function getPrimaryMediaUrl(value) {
  if (!value) {
    return '';
  }
  try {
    const parsed = JSON.parse(value);
    if (Array.isArray(parsed) && parsed.length) {
      return String(parsed[0] || '');
    }
  } catch (error) {
    return String(value);
  }
  return String(value);
}

function loadLogs() {
  try {
    const raw = localStorage.getItem(REVIEW_LOG_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed.slice(0, 5) : [];
  } catch (error) {
    return [];
  }
}

function saveLogs() {
  localStorage.setItem(REVIEW_LOG_KEY, JSON.stringify(state.logs.slice(0, 5)));
}

function addLog(title, detail) {
  state.logs.unshift({ id: `log-${Date.now()}`, time: formatNow(), title, detail });
  state.logs = state.logs.slice(0, 5);
  saveLogs();
}

function formatNow() {
  const now = new Date();
  const date = [now.getFullYear(), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')].join('-');
  const time = [String(now.getHours()).padStart(2, '0'), String(now.getMinutes()).padStart(2, '0')].join(':');
  return `${date} ${time}`;
}

function formatDateTime(value) {
  if (!value) {
    return 'N/A';
  }
  return window.HeritageApi && typeof window.HeritageApi.formatDateTime === 'function'
    ? window.HeritageApi.formatDateTime(value)
    : String(value);
}

function setStorageStatus(message) {
  if (storageStatus) {
    storageStatus.textContent = message;
  }
}

function showToast(message) {
  if (!toast) {
    return;
  }
  toast.textContent = message;
  toast.classList.add('is-visible');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.remove('is-visible');
  }, 2200);
}

function escapeHtml(value) {
  return String(value == null ? '' : value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function getUserById(userId) {
  return state.usersById.get(String(userId)) || null;
}

function getCurrentContextRecord() {
  if (!state.selectedContext) {
    return null;
  }
  if (state.selectedContext.type === 'application') {
    return state.contributorApplications.find((item) => String(item.id) === String(state.selectedContext.id)) || null;
  }
  if (state.selectedContext.type === 'resource') {
    return state.pendingResources.find((item) => String(item.id) === String(state.selectedContext.id)) || null;
  }
  return null;
}

function getContributorAppStatus(status) {
  return CONTRIBUTOR_APP_STATUS[status] || CONTRIBUTOR_APP_STATUS.PENDING;
}

function getResourceStatus(statusCode) {
  return RESOURCE_STATUS[Number(statusCode)] || RESOURCE_STATUS[1];
}

function getApplicantName(application) {
  const linkedUser = getUserById(application.userId);
  return application.applicantName || (linkedUser && linkedUser.username) || 'Unknown Applicant';
}

function setButtonLabels() {
  if (!approveButton || !rejectButton) {
    return;
  }
  if (!state.selectedContext) {
    approveButton.textContent = 'Approve Application';
    rejectButton.textContent = 'Reject Application';
    return;
  }
  if (state.selectedContext.type === 'resource') {
    approveButton.textContent = 'Approve Resource';
    rejectButton.textContent = 'Reject Resource';
    return;
  }
  approveButton.textContent = 'Approve Application';
  rejectButton.textContent = 'Reject Application';
}

function renderOverview() {
  if (!overviewGrid) {
    return;
  }
  const users = Array.from(state.usersById.values());
  const cards = [
    { label: 'Pending Applications', value: state.contributorApplications.length, meta: 'Normal users waiting for contributor approval' },
    { label: 'Pending Heritage Files', value: state.pendingResources.length, meta: 'Contributor resource submissions waiting for review' },
    { label: 'Active Contributors', value: users.filter((user) => user.role === 'CONTRIBUTOR').length, meta: 'Current contributor accounts' },
    { label: 'Admin Accounts', value: users.filter((user) => user.role === 'ADMIN').length, meta: 'Shared admin review access' }
  ];
  overviewGrid.innerHTML = cards.map((card) => `
    <article class="overview-card">
      <p>${escapeHtml(card.label)}</p>
      <strong>${escapeHtml(card.value)}</strong>
      <span>${escapeHtml(card.meta)}</span>
    </article>
  `).join('');
}

function renderQueue() {
  if (!pendingBadge || !queueTableBody) {
    return;
  }
  pendingBadge.textContent = `${state.contributorApplications.length} Pending`;

  if (!state.contributorApplications.length) {
    queueTableBody.innerHTML = '<tr><td colspan="4"><div class="empty">There are currently no pending contributor applications.</div></td></tr>';
    if (state.selectedContext && state.selectedContext.type === 'application') {
      state.selectedContext = null;
    }
    return;
  }

  const hasSelectedApplication = state.selectedContext
    && state.selectedContext.type === 'application'
    && state.contributorApplications.some((item) => String(item.id) === String(state.selectedContext.id));
  if (!hasSelectedApplication && !state.selectedContext) {
    state.selectedContext = { type: 'application', id: state.contributorApplications[0].id };
  }

  queueTableBody.innerHTML = state.contributorApplications.map((application) => {
    const linkedUser = getUserById(application.userId);
    const statusInfo = getContributorAppStatus(application.status);
    const active = state.selectedContext && state.selectedContext.type === 'application' && String(state.selectedContext.id) === String(application.id) ? 'is-active' : '';
    return `
      <tr class="${active}" data-review-id="${escapeHtml(application.id)}">
        <td>${escapeHtml(getApplicantName(application))}<br><small>${escapeHtml((linkedUser && linkedUser.email) || 'No email')}</small></td>
        <td>${escapeHtml(application.domain || 'General Review')}</td>
        <td>${escapeHtml(formatDateTime(application.createdAt))}</td>
        <td><span class="${escapeHtml(statusInfo.className)}">${escapeHtml(statusInfo.label)}</span></td>
      </tr>
    `;
  }).join('');
}

function renderRegistry() {
  if (!contributorRegistry) {
    return;
  }
  if (!state.pendingResources.length) {
    contributorRegistry.innerHTML = '<div class="empty">There are currently no contributor files waiting for review.</div>';
    if (state.selectedContext && state.selectedContext.type === 'resource') {
      state.selectedContext = null;
    }
    return;
  }

  contributorRegistry.innerHTML = state.pendingResources.map((resource) => {
    const contributor = getUserById(resource.userId);
    const statusInfo = getResourceStatus(resource.status);
    return `
      <article class="registry-card" style="position:relative;">
        <div class="registry-head">
          <div>
            <h4>${escapeHtml(resource.title || 'Untitled Heritage')}</h4>
            <p>${escapeHtml((contributor && contributor.username) || `Contributor #${resource.userId}`)} / ${escapeHtml((contributor && contributor.email) || 'No email')}</p>
          </div>
          <span class="${escapeHtml(statusInfo.className)}">${escapeHtml(statusInfo.label)}</span>
        </div>
        <div class="registry-meta">
          <span>Category: ${escapeHtml(resource.category || 'General')}</span>
          <span>Location: ${escapeHtml(resource.location || 'Unknown')}</span>
        </div>
        <div class="submission-file">
          <h5>Contributor Information</h5>
          <p>Contributor: ${escapeHtml((contributor && contributor.username) || `Contributor #${resource.userId}`)}</p>
          <p>Phone: ${escapeHtml((contributor && contributor.phone) || 'No phone')}</p>
          <p>Email: ${escapeHtml((contributor && contributor.email) || 'No email')}</p>
          <h5 style="margin-top:12px;">Document Information</h5>
          <p>Title: ${escapeHtml(resource.title || 'Untitled Heritage')}</p>
          <p>Submitted At: ${escapeHtml(formatDateTime(resource.createTime))}</p>
          <p>${escapeHtml(resource.description || 'No description provided.')}</p>
        </div>
        <div class="registry-actions">
          <span>${escapeHtml(statusInfo.label)}</span>
          <button class="button button--ghost" type="button" data-view-resource="${escapeHtml(resource.id)}">View</button>
        </div>
      </article>
    `;
  }).join('');
}

function renderLogs() {
  if (!activityLog) {
    return;
  }
  const latestLogs = state.logs.slice(0, 5);
  activityLog.innerHTML = latestLogs.length ? latestLogs.map((item) => `
    <article class="log-item">
      <h4>${escapeHtml(item.title)}</h4>
      <p>${escapeHtml(item.detail)}</p>
      <span>${escapeHtml(item.time)}</span>
    </article>
  `).join('') : '<div class="empty">No activity logs available.</div>';
}

function renderContributorApplicationWorkspace(application) {
  const linkedUser = getUserById(application.userId);
  reviewSummary.innerHTML = `
    <h4>${escapeHtml(getApplicantName(application))}</h4>
    <p>Contact: ${escapeHtml((linkedUser && linkedUser.email) || 'No email')} / ${escapeHtml((linkedUser && linkedUser.phone) || 'No phone')}</p>
    <p>Domain: ${escapeHtml(application.domain || 'General Review')}</p>
    <p>Submitted At: ${escapeHtml(formatDateTime(application.createdAt))}</p>
    <p>Application Notes: ${escapeHtml(application.applicationReason || 'No motivation statement provided.')}</p>
  `;
  submissionReviewPanel.innerHTML = '';
}

function renderResourceWorkspace(resource) {
  const contributor = getUserById(resource.userId);
  const primaryFileUrl = getPrimaryMediaUrl(resource.fileUrl);
  const imageMarkup = primaryFileUrl
    ? `<img src="${escapeHtml(primaryFileUrl)}" alt="${escapeHtml(resource.title || 'Heritage image')}" style="width:100%; max-height:180px; object-fit:cover; border-radius:10px; margin-bottom:14px;">`
    : '';
  const downloadMarkup = primaryFileUrl
    ? `<a class="button button--ghost" href="${escapeHtml(primaryFileUrl)}" download="${escapeHtml((resource.title || 'heritage-file').replace(/\s+/g, '-'))}" style="text-decoration:none;">Download File</a>`
    : '';

  submissionReviewPanel.innerHTML = `
    <article class="submission-review-card">
      <div class="submission-review-head">
        <div>
          <p class="panel__tag">Contributor File View</p>
          <h4>${escapeHtml(resource.title || 'Untitled Heritage')}</h4>
        </div>
        <span class="${escapeHtml(getResourceStatus(resource.status).className)}">${escapeHtml(getResourceStatus(resource.status).label)}</span>
      </div>
      ${imageMarkup}
      <div class="submission-review-grid">
        <div class="submission-review-block">
          <h5>Contributor Information</h5>
          <p>Name: ${escapeHtml((contributor && contributor.username) || `Contributor #${resource.userId}`)}</p>
          <p>User ID: ${escapeHtml(resource.userId)}</p>
          <p>Email: ${escapeHtml((contributor && contributor.email) || 'No email')}</p>
          <p>Phone: ${escapeHtml((contributor && contributor.phone) || 'No phone')}</p>
        </div>
        <div class="submission-review-block">
          <h5>Document Information</h5>
          <p>Category: ${escapeHtml(resource.category || 'General')}</p>
          <p>Location: ${escapeHtml(resource.location || 'Unknown')}</p>
          <p>Tags: ${escapeHtml(resource.tags || 'None')}</p>
          <p>Submitted At: ${escapeHtml(formatDateTime(resource.createTime))}</p>
          <p>Updated At: ${escapeHtml(formatDateTime(resource.updateTime))}</p>
          <p>${escapeHtml(resource.description || 'No description provided.')}</p>
        </div>
      </div>
      <div class="submission-review-actions">${downloadMarkup}</div>
    </article>
  `;

  reviewSummary.innerHTML = `
    <h4>${escapeHtml(resource.title || 'Untitled Heritage')}</h4>
    <p>Contributor: ${escapeHtml((contributor && contributor.username) || `Contributor #${resource.userId}`)}</p>
    <p>Location: ${escapeHtml(resource.location || 'Unknown')}</p>
    <p>Category: ${escapeHtml(resource.category || 'General')}</p>
    <p>Description: ${escapeHtml(resource.description || 'No description provided.')}</p>
  `;
}

function renderReviewWorkspace() {
  const record = getCurrentContextRecord();
  setButtonLabels();

  if (!record || !reviewSummary || !submissionReviewPanel || !approveButton || !rejectButton) {
    if (reviewSummary) {
      reviewSummary.innerHTML = '<div class="empty">Select a contributor application or a contributor file first.</div>';
    }
    if (submissionReviewPanel) {
      submissionReviewPanel.innerHTML = '';
    }
    if (approveButton) {
      approveButton.disabled = true;
    }
    if (rejectButton) {
      rejectButton.disabled = true;
    }
    return;
  }

  if (state.selectedContext.type === 'resource') {
    renderResourceWorkspace(record);
  } else {
    renderContributorApplicationWorkspace(record);
  }

  if (state.selectedContext.type === 'resource' && Number(record.status) !== 1) {
    approveButton.disabled = true;
    rejectButton.disabled = true;
    return;
  }

  approveButton.disabled = false;
  rejectButton.disabled = false;
}

function setActiveTab(tabName) {
  state.activeTab = tabName;
  document.querySelectorAll('[data-tab]').forEach((button) => {
    button.classList.toggle('is-active', button.dataset.tab === tabName);
  });
  document.querySelectorAll('[data-pane]').forEach((pane) => {
    pane.classList.toggle('is-active', pane.dataset.pane === tabName);
  });
}

function setActiveModule(moduleName) {
  navItems.forEach((item) => {
    item.classList.toggle('is-active', item.dataset.module === moduleName);
  });
  modulePanels.forEach((panel) => {
    panel.classList.toggle('module-focus', panel.dataset.modulePanel === moduleName);
  });
}

function bindModuleHover() {
  if (!navItems.length) {
    return;
  }
  navItems.forEach((item) => {
    item.addEventListener('mouseenter', () => setActiveModule(item.dataset.module));
    item.addEventListener('focus', () => setActiveModule(item.dataset.module));
  });
  const navContainer = document.querySelector('.nav');
  if (navContainer) {
    navContainer.addEventListener('mouseleave', () => setActiveModule('overview'));
  }
  setActiveModule('overview');
}

function rerender() {
  renderOverview();
  renderQueue();
  renderRegistry();
  renderReviewWorkspace();
  renderLogs();
  setActiveTab(state.activeTab);
}

async function syncCurrentAdmin() {
  const sessionUser = window.HeritageSession && typeof window.HeritageSession.getCurrentUser === 'function'
    ? window.HeritageSession.getCurrentUser()
    : null;
  if (!sessionUser) {
    window.alert('Please log in first.');
    window.location.href = 'welcome.html';
    return null;
  }

  let refreshedUser = sessionUser;
  if (window.HeritageSession && typeof window.HeritageSession.refreshCurrentUserFromServer === 'function') {
    try {
      refreshedUser = await window.HeritageSession.refreshCurrentUserFromServer() || sessionUser;
    } catch (error) {
      refreshedUser = sessionUser;
    }
  }

  if (!refreshedUser || refreshedUser.role !== 'ADMIN') {
    window.alert('This page requires Admin access.');
    window.location.href = 'home.html';
    return null;
  }

  state.currentAdmin = refreshedUser;
  return refreshedUser;
}

async function loadDashboardData() {
  const [users, contributorApplications, pendingResources, allResources] = await Promise.all([
    window.HeritageApi.request('/api/zyl/users'),
    window.HeritageApi.request('/api/ly-contributor/admin/contributor-applications/pending'),
    window.HeritageApi.request('/api/lpp/admin/resources/pending'),
    window.HeritageApi.request('/api/lpp/admin/resources')
  ]);

  state.usersById = new Map((Array.isArray(users) ? users : []).map((user) => [String(user.id), user]));
  state.contributorApplications = Array.isArray(contributorApplications) ? contributorApplications : [];
  state.pendingResources = Array.isArray(pendingResources) ? pendingResources : [];
  state.allResources = Array.isArray(allResources) ? allResources : [];

  const currentRecord = getCurrentContextRecord();
  if (!currentRecord) {
    if (state.contributorApplications.length) {
      state.selectedContext = { type: 'application', id: state.contributorApplications[0].id };
    } else if (state.pendingResources.length) {
      state.selectedContext = { type: 'resource', id: state.pendingResources[0].id };
    } else if (state.allResources.length) {
      state.selectedContext = { type: 'resource', id: state.allResources[0].id };
    } else {
      state.selectedContext = null;
    }
  }

  setStorageStatus(`Live sync ${formatNow()}`);
  rerender();
}

async function reviewSelected(action) {
  const record = getCurrentContextRecord();
  if (!record || !state.currentAdmin) {
    showToast('There is no selected review target.');
    return;
  }
  if (state.selectedContext.type === 'resource' && Number(record.status) !== 1) {
    showToast('Only pending resources can be reviewed.');
    return;
  }

  const feedbackValue = reviewFeedback ? reviewFeedback.value.trim() : '';
  const feedback = feedbackValue || (action === 'approve' ? 'Approved by administrator.' : 'Rejected by administrator.');
  approveButton.disabled = true;
  rejectButton.disabled = true;

  try {
    if (state.selectedContext.type === 'resource') {
      const endpoint = action === 'approve' ? 'publish' : 'reject';
      await window.HeritageApi.request(`/api/lpp/admin/resources/${record.id}/${endpoint}`, {
        method: 'PUT',
        body: {
          reviewerId: state.currentAdmin.id,
          feedback: feedback
        }
      });
      addLog(
        action === 'approve' ? 'Resource Approved' : 'Resource Rejected',
        `${record.title || 'Untitled heritage'} was ${action === 'approve' ? 'approved' : 'rejected'} by ${state.currentAdmin.username}.`
      );
    } else {
      await window.HeritageApi.request(`/api/ly-contributor/admin/contributor-applications/${record.id}/${action}`, {
        method: 'PUT',
        body: {
          reviewedBy: state.currentAdmin.id,
          feedback: feedback
        }
      });
      addLog(
        action === 'approve' ? 'Application Approved' : 'Application Rejected',
        `${getApplicantName(record)} was ${action === 'approve' ? 'approved' : 'rejected'} by ${state.currentAdmin.username}.`
      );
    }

    if (reviewFeedback) {
      reviewFeedback.value = '';
    }

    await loadDashboardData();
    showToast(action === 'approve' ? 'Review approved.' : 'Review rejected.');
  } catch (error) {
    approveButton.disabled = false;
    rejectButton.disabled = false;
    showToast(window.HeritageApi.getErrorMessage(error));
  }
}

async function deleteSelectedResource(resourceId) {
  if (!state.currentAdmin || !resourceId) {
    showToast('There is no selected resource to delete.');
    return;
  }
  if (!window.confirm('Delete this heritage project?')) {
    return;
  }

  try {
    const result = await window.HeritageApi.request(`/api/lpp/admin/resources/${resourceId}?adminId=${encodeURIComponent(state.currentAdmin.id)}`, {
      method: 'DELETE'
    });
    window.HeritageApi.unwrapResult(result);

    const deletedRecord = state.allResources.find((item) => String(item.id) === String(resourceId));
    addLog(
      'Resource Deleted',
      `${(deletedRecord && deletedRecord.title) || 'Selected heritage project'} was deleted by ${state.currentAdmin.username}.`
    );

    if (state.selectedContext && state.selectedContext.type === 'resource' && String(state.selectedContext.id) === String(resourceId)) {
      state.selectedContext = null;
    }

    await loadDashboardData();
    showToast('Resource deleted.');
  } catch (error) {
    showToast(window.HeritageApi.getErrorMessage(error));
  }
}

async function initPage() {
  try {
    setStorageStatus('Connecting to live data...');
    const admin = await syncCurrentAdmin();
    if (!admin) {
      return;
    }

    if (window.HeritageSession && typeof window.HeritageSession.syncMenuVisibility === 'function') {
      window.HeritageSession.syncMenuVisibility(admin);
    }
    if (window.HeritageSession && typeof window.HeritageSession.syncUserVisuals === 'function') {
      window.HeritageSession.syncUserVisuals(admin);
    }

    await loadDashboardData();
    bindModuleHover();
  } catch (error) {
    setStorageStatus('Live sync failed');
    showToast(window.HeritageApi.getErrorMessage(error));
  }
}

if (queueTableBody) {
  queueTableBody.addEventListener('click', (event) => {
    const row = event.target.closest('[data-review-id]');
    if (!row) {
      return;
    }
    state.selectedContext = { type: 'application', id: row.dataset.reviewId };
    state.activeTab = 'review';
    rerender();
  });
}

if (contributorRegistry) {
  contributorRegistry.addEventListener('click', (event) => {
    const viewButton = event.target.closest('[data-view-resource]');
    if (!viewButton) {
      return;
    }
    state.selectedContext = { type: 'resource', id: viewButton.dataset.viewResource };
    state.activeTab = 'review';
    rerender();
  });
}

if (reviewForm) {
  reviewForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const submitter = event.submitter;
    if (!submitter || !submitter.dataset.action) {
      return;
    }
    reviewSelected(submitter.dataset.action);
  });
}

if (tabBar) {
  tabBar.addEventListener('click', (event) => {
    const button = event.target.closest('[data-tab]');
    if (!button) {
      return;
    }
    setActiveTab(button.dataset.tab);
  });
}

if (backButton) {
  backButton.addEventListener('click', () => {
    window.location.href = 'home.html';
  });
}

document.addEventListener('DOMContentLoaded', initPage);
