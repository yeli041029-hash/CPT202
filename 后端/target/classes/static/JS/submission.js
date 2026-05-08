const toast = document.getElementById('submissionToast');
const form = document.getElementById('resourceForm');
const formHeaderTitle = document.getElementById('formHeaderTitle');
const titleInput = document.getElementById('titleInput');
const categoryInput = document.getElementById('catSelect');
const locationInput = document.getElementById('locationInput');
const tagsInput = document.getElementById('tagsInput');
const descInput = document.getElementById('descInput');
const imageUpload = document.getElementById('imageUpload');
const imageUploadStatus = document.getElementById('imageUploadStatus');
const imageUploadCount = document.getElementById('imageUploadCount');
const imageUploadList = document.getElementById('imageUploadList');
const attachmentUpload = document.getElementById('attachmentUpload');
const attachmentUploadStatus = document.getElementById('attachmentUploadStatus');
const attachmentUploadCount = document.getElementById('attachmentUploadCount');
const attachmentUploadList = document.getElementById('attachmentUploadList');
const externalLinkInput = document.getElementById('externalLinkInput');
const copyrightCheckbox = document.getElementById('copyConfirm');
const copyrightWarning = document.getElementById('copyConfirmWarning');
const resourceIdInput = document.getElementById('resourceIdInput');
const saveDraftButton = document.getElementById('btnSaveDraft');
const submitButton = document.getElementById('btnSubmit');
const draftList = document.getElementById('draftList');
const draftCount = document.getElementById('draftCount');
const statusCard = document.getElementById('statusCard');
const statusCardHeader = document.getElementById('statusCardHeader');
const statusCardCount = document.getElementById('statusCardCount');
const statusCardContent = document.getElementById('statusCardContent');
const LOCATION_OPTIONS = [
    'Xinjiang',
    'Xizang',
    'Qinghai',
    'Gansu',
    'Ningxia',
    'Inner Mongolia',
    'Heilongjiang',
    'Jilin',
    'Liaoning',
    'Beijing',
    'Hebei',
    'Shanxi',
    'Shandong',
    'Henan',
    'Shaanxi',
    'Jiangsu',
    'Anhui',
    'Shanghai',
    'Hubei',
    'Sichuan',
    'Chongqing',
    'Zhejiang',
    'Jiangxi',
    'Hunan',
    'Guizhou',
    'Fujian',
    'Yunnan',
    'Guangxi',
    'Guangdong',
    'Hainan',
    'Taiwan'
];
const MAX_IMAGE_FILES = 10;
const MAX_ATTACHMENT_FILES = 10;
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
const IMAGE_REQUIRED_MESSAGE = 'Please upload at least one image for the heritage cover.';
const IMAGE_UPLOAD_MESSAGE = 'Only image files are supported in the image channel.';
const ATTACHMENT_UPLOAD_MESSAGE = 'Only PDF files and videos are supported in this channel.';
const IMAGE_FILE_EXTENSION_PATTERN = /\.(png|jpe?g|gif|bmp|webp|svg|heic|heif)$/i;
const ATTACHMENT_FILE_EXTENSION_PATTERN = /\.(pdf|mp4|mov|avi|wmv|mkv|webm|m4v|mpeg|mpg|ogg|ogv|3gp)$/i;

const STATUS = {
    DRAFT: 'DRAFT',
    PENDING_APPROVAL: 'PENDING_APPROVAL',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED',
    ARCHIVED: 'ARCHIVED'
};

let currentUser = null;
let currentResource = null;
let currentImageUrl = '';
let toastTimer = null;
let userResources = [];
let userDrafts = [];
let selectedImageFiles = [];
let selectedAttachmentFiles = [];

function getEnglishValidationMessage(field) {
    if (!field || !field.validity) {
        return '';
    }
    if (field.validity.valueMissing) {
        if (field.type === 'checkbox') {
            return 'Please confirm the declaration before continuing.';
        }
        if (field.tagName === 'SELECT') {
            return 'Please select an option.';
        }
        return 'Please fill out this field.';
    }
    if (field.validity.typeMismatch && field.type === 'url') {
        return 'Please enter a valid URL.';
    }
    return 'Please enter a valid value.';
}

function attachEnglishValidation(field) {
    if (!field) {
        return;
    }
    field.addEventListener('invalid', () => {
        field.setCustomValidity(getEnglishValidationMessage(field));
    });
    const clearMessage = () => field.setCustomValidity('');
    field.addEventListener('input', clearMessage);
    field.addEventListener('change', clearMessage);
}

function hasAvailableImage() {
    return Boolean(selectedImageFiles.length || getExistingImageCount());
}

function updateActionButtonState() {
    const confirmed = Boolean(copyrightCheckbox && copyrightCheckbox.checked);
    const imageReady = hasAvailableImage();
    const enabled = confirmed && imageReady;
    [saveDraftButton, submitButton].forEach((button) => {
        if (!button) {
            return;
        }
        button.disabled = !enabled;
        if (enabled) {
            button.style.opacity = '';
            button.style.filter = '';
            button.style.cursor = '';
            return;
        }
        button.style.opacity = '0.55';
        button.style.filter = 'grayscale(0.35)';
        button.style.cursor = 'not-allowed';
    });
    if (confirmed && copyrightWarning) {
        copyrightWarning.style.display = 'none';
    }
}

function ensureCopyrightConfirmed() {
    if (!copyrightCheckbox || copyrightCheckbox.checked) {
        if (copyrightWarning) {
            copyrightWarning.style.display = 'none';
        }
        return true;
    }
    if (copyrightWarning) {
        copyrightWarning.style.display = 'block';
    }
    return false;
}

function populateLocationOptions() {
    if (!locationInput || locationInput.tagName !== 'SELECT') {
        return;
    }

    const currentValue = locationInput.value;
    locationInput.innerHTML = '<option value="">Select...</option>' + LOCATION_OPTIONS.map((name) => {
        return `<option value="${escapeHtml(name)}">${escapeHtml(name)}</option>`;
    }).join('');

    if (currentValue && !LOCATION_OPTIONS.includes(currentValue)) {
        locationInput.innerHTML += `<option value="${escapeHtml(currentValue)}">${escapeHtml(currentValue)}</option>`;
    }

    locationInput.value = currentValue || '';
}

function ensureLocationOption(value) {
    if (!locationInput || locationInput.tagName !== 'SELECT' || !value || LOCATION_OPTIONS.includes(value)) {
        return;
    }

    if (!Array.from(locationInput.options).some((option) => option.value === value)) {
        locationInput.insertAdjacentHTML('beforeend', `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`);
    }
}

function showToast(message) {
    if (!toast) {
        return;
    }
    toast.textContent = message;
    toast.style.opacity = '1';
    toast.style.transform = 'translateY(0)';
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(12px)';
    }, 2400);
}

function escapeHtml(value) {
    return String(value == null ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatDateTime(value) {
    return HeritageApi.formatDateTime(value);
}

function autoResize(textarea) {
    if (!textarea) {
        return;
    }
    textarea.style.height = 'auto';
    textarea.style.height = textarea.scrollHeight + 'px';
}

function resetFormHeader() {
    formHeaderTitle.textContent = 'Create New Resource Entry';
}

function getMediaKind(value) {
    const normalized = String(value || '').trim().toLowerCase();
    if (!normalized) {
        return 'other';
    }

    const dataUrlMatch = normalized.match(/^data:([^;,]+)[;,]/);
    const mimeType = dataUrlMatch ? dataUrlMatch[1] : '';
    if (mimeType.startsWith('image/')) {
        return 'image';
    }
    if (mimeType === 'application/pdf') {
        return 'pdf';
    }
    if (mimeType.startsWith('video/')) {
        return 'video';
    }

    if (IMAGE_FILE_EXTENSION_PATTERN.test(normalized)) {
        return 'image';
    }
    if (/\.pdf(\?.*)?$/.test(normalized)) {
        return 'pdf';
    }
    if (/\.(mp4|mov|avi|wmv|mkv|webm|m4v|mpeg|mpg|ogg|ogv|3gp)(\?.*)?$/.test(normalized)) {
        return 'video';
    }
    return 'other';
}

function getStoredMediaEntries(value) {
    if (!value) {
        return [];
    }

    try {
        const parsed = JSON.parse(value);
        if (Array.isArray(parsed)) {
            return parsed
                .map((item) => String(item || '').trim())
                .filter(Boolean)
                .map((url) => ({ url: url, kind: getMediaKind(url) }));
        }
    } catch (error) {
    }

    const singleValue = String(value).trim();
    if (!singleValue) {
        return [];
    }
    return [{ url: singleValue, kind: getMediaKind(singleValue) }];
}

function getStoredImageEntries(value) {
    return getStoredMediaEntries(value).filter((entry) => entry.kind === 'image');
}

function getStoredAttachmentEntries(value) {
    return getStoredMediaEntries(value).filter((entry) => entry.kind !== 'image');
}

function resetForm() {
    form.reset();
    resourceIdInput.value = '';
    currentResource = null;
    currentImageUrl = '';
    selectedImageFiles = [];
    selectedAttachmentFiles = [];
    populateLocationOptions();
    resetFormHeader();
    autoResize(descInput);
    updateImageSelectionUI();
    updateAttachmentSelectionUI();
    updateActionButtonState();
}

function mapCategoryValue(category) {
    if (!category) {
        return '';
    }
    const normalized = String(category).toLowerCase();
    if (normalized === 'relics' || normalized === '1') {
        return '1';
    }
    if (normalized === 'crafts' || normalized === '2') {
        return '2';
    }
    if (normalized === 'architecture' || normalized === '3') {
        return '3';
    }
    if (normalized === 'folktales' || normalized === 'folklore' || normalized === '4') {
        return '4';
    }
    return '';
}

function mapCategoryLabel(value) {
    if (String(value) === '1') {
        return 'Relics';
    }
    if (String(value) === '2') {
        return 'Crafts';
    }
    if (String(value) === '3') {
        return 'Architecture';
    }
    if (String(value) === '4') {
        return 'Folktales';
    }
    return '';
}

function toReviewLabel(resource) {
    const reviewer = resource.reviewedBy ? `Reviewer ID: ${resource.reviewedBy}` : 'Reviewer not recorded';
    const reviewedAt = resource.reviewedAt ? formatDateTime(resource.reviewedAt) : 'Review time pending';
    const feedback = resource.feedback || 'No review feedback provided.';
    return { reviewer, reviewedAt, feedback };
}

function buildDraftPayload() {
    return {
        id: resourceIdInput.value ? Number(resourceIdInput.value) : null,
        userId: currentUser.id,
        title: titleInput.value.trim(),
        description: descInput.value.trim(),
        content: descInput.value.trim(),
        category: mapCategoryLabel(categoryInput.value),
        location: locationInput.value.trim(),
        tags: tagsInput ? tagsInput.value.trim() : '',
        externalLink: externalLinkInput.value.trim(),
        fileUrl: currentImageUrl || ''
    };
}

function getExistingImageCount() {
    return getStoredImageEntries(currentImageUrl).length;
}

function getExistingAttachmentCount() {
    return getStoredAttachmentEntries(currentImageUrl).length;
}

function validateForm() {
    if (!form.reportValidity()) {
        return false;
    }
    if (!titleInput.value.trim() || !descInput.value.trim() || !locationInput.value.trim()) {
        showToast('Please complete all required fields.');
        return false;
    }
    if (!selectedImageFiles.length && !getExistingImageCount()) {
        showToast(IMAGE_REQUIRED_MESSAGE);
        return false;
    }
    return true;
}

function ensureImageProvided() {
    if (hasAvailableImage()) {
        return true;
    }
    showToast(IMAGE_REQUIRED_MESSAGE);
    return false;
}

function hasDraftContent() {
    return Boolean(
        titleInput.value.trim()
        || descInput.value.trim()
        || locationInput.value.trim()
        || (tagsInput ? tagsInput.value.trim() : '')
        || externalLinkInput.value.trim()
        || categoryInput.value
        || currentImageUrl
        || selectedImageFiles.length
        || selectedAttachmentFiles.length
    );
}

function readFileAsDataUrl(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '');
        reader.onerror = () => reject(new Error('Failed to read the selected file.'));
        reader.readAsDataURL(file);
    });
}

function isImageUploadFile(file) {
    if (!file) {
        return false;
    }

    const fileType = String(file.type || '').toLowerCase();
    const fileName = String(file.name || '').toLowerCase();
    return fileType.startsWith('image/') || IMAGE_FILE_EXTENSION_PATTERN.test(fileName);
}

function isAttachmentUploadFile(file) {
    if (!file) {
        return false;
    }

    const fileType = String(file.type || '').toLowerCase();
    const fileName = String(file.name || '').toLowerCase();
    return fileType === 'application/pdf'
        || fileType.startsWith('video/')
        || ATTACHMENT_FILE_EXTENSION_PATTERN.test(fileName);
}

function normalizeSelectedFiles(fileList, validator, maxFiles, invalidMessage) {
    const files = Array.from(fileList || []);
    const invalidFile = files.find((file) => !validator(file));
    if (invalidFile) {
        throw new Error(invalidMessage);
    }
    if (files.length > maxFiles) {
        throw new Error(`You can upload up to ${maxFiles} files in this channel.`);
    }
    return files;
}

function getFileKey(file) {
    return [file.name, file.size, file.lastModified].join('__');
}

function mergeSelectedFiles(existingFiles, incomingFiles, maxFiles) {
    const mergedFiles = [...existingFiles];
    const fileKeys = new Set(existingFiles.map(getFileKey));

    incomingFiles.forEach((file) => {
        const fileKey = getFileKey(file);
        if (!fileKeys.has(fileKey)) {
            mergedFiles.push(file);
            fileKeys.add(fileKey);
        }
    });

    if (mergedFiles.length > maxFiles) {
        throw new Error(`You can upload up to ${maxFiles} files in this channel.`);
    }
    return mergedFiles;
}

function renderSelectedFileList(listElement, selectedFiles, actionName) {
    if (!listElement) {
        return;
    }

    if (!selectedFiles.length) {
        listElement.innerHTML = '';
        listElement.classList.remove('has-files');
        return;
    }

    listElement.innerHTML = selectedFiles.map((file, index) => {
        return `
            <div class="upload-file-row">
                <span class="upload-file-name" title="${escapeHtml(file.name)}">${escapeHtml(file.name)}</span>
                <button type="button" class="upload-file-remove" data-action="${actionName}" data-index="${index}" aria-label="Remove ${escapeHtml(file.name)}" title="Remove file">
                    <span aria-hidden="true">&#128465;</span>
                </button>
            </div>
        `;
    }).join('');
    listElement.classList.add('has-files');
}

function updateImageSelectionUI() {
    if (!imageUploadStatus || !imageUploadCount) {
        return;
    }

    if (selectedImageFiles.length) {
        imageUploadStatus.textContent = selectedImageFiles.length === 1
            ? selectedImageFiles[0].name
            : selectedImageFiles[0].name + ' +' + (selectedImageFiles.length - 1) + ' more';
        imageUploadCount.textContent = selectedImageFiles.length === 1
            ? '1 image selected'
            : selectedImageFiles.length + ' images selected';
        renderSelectedFileList(imageUploadList, selectedImageFiles, 'remove-image-file');
        updateActionButtonState();
        return;
    }

    const storedImageCount = getExistingImageCount();
    if (storedImageCount > 0) {
        imageUploadStatus.textContent = storedImageCount === 1 ? '1 image attached' : storedImageCount + ' images attached';
        imageUploadCount.textContent = storedImageCount === 1 ? '1 existing image' : storedImageCount + ' existing images';
        renderSelectedFileList(imageUploadList, selectedImageFiles, 'remove-image-file');
        updateActionButtonState();
        return;
    }

    imageUploadStatus.textContent = 'No image selected';
    imageUploadCount.textContent = '0 images selected';
    renderSelectedFileList(imageUploadList, selectedImageFiles, 'remove-image-file');
    updateActionButtonState();
}

function updateAttachmentSelectionUI() {
    if (!attachmentUploadStatus || !attachmentUploadCount) {
        return;
    }

    if (selectedAttachmentFiles.length) {
        attachmentUploadStatus.textContent = selectedAttachmentFiles.length === 1
            ? selectedAttachmentFiles[0].name
            : selectedAttachmentFiles[0].name + ' +' + (selectedAttachmentFiles.length - 1) + ' more';
        attachmentUploadCount.textContent = selectedAttachmentFiles.length === 1
            ? '1 file selected'
            : selectedAttachmentFiles.length + ' files selected';
        renderSelectedFileList(attachmentUploadList, selectedAttachmentFiles, 'remove-attachment-file');
        return;
    }

    const storedAttachmentCount = getExistingAttachmentCount();
    if (storedAttachmentCount > 0) {
        attachmentUploadStatus.textContent = storedAttachmentCount === 1 ? '1 file attached' : storedAttachmentCount + ' files attached';
        attachmentUploadCount.textContent = storedAttachmentCount === 1 ? '1 existing file' : storedAttachmentCount + ' existing files';
        renderSelectedFileList(attachmentUploadList, selectedAttachmentFiles, 'remove-attachment-file');
        return;
    }

    attachmentUploadStatus.textContent = 'No file selected';
    attachmentUploadCount.textContent = '0 files selected';
    renderSelectedFileList(attachmentUploadList, selectedAttachmentFiles, 'remove-attachment-file');
}

function removeSelectedImageFile(index) {
    if (Number.isNaN(index) || index < 0 || index >= selectedImageFiles.length) {
        return;
    }
    selectedImageFiles = selectedImageFiles.filter((_, fileIndex) => fileIndex !== index);
    updateImageSelectionUI();
}

function removeSelectedAttachmentFile(index) {
    if (Number.isNaN(index) || index < 0 || index >= selectedAttachmentFiles.length) {
        return;
    }
    selectedAttachmentFiles = selectedAttachmentFiles.filter((_, fileIndex) => fileIndex !== index);
    updateAttachmentSelectionUI();
}

function serializeUploadedMedia(mediaItems) {
    if (!mediaItems.length) {
        return '';
    }
    return mediaItems.length === 1 ? mediaItems[0] : JSON.stringify(mediaItems);
}

async function readSelectedFilesAsDataUrls(files) {
    files.forEach((file) => {
        if (file.size > MAX_FILE_SIZE_BYTES) {
            throw new Error(`File "${file.name}" exceeds 10MB limit.`);
        }
    });
    return Promise.all(files.map(readFileAsDataUrl));
}

async function ensureMediaDataReady() {
    const storedImageEntries = getStoredImageEntries(currentImageUrl);
    const storedAttachmentEntries = getStoredAttachmentEntries(currentImageUrl);

    const nextImages = selectedImageFiles.length
        ? await readSelectedFilesAsDataUrls(selectedImageFiles)
        : storedImageEntries.map((entry) => entry.url);

    const nextAttachments = selectedAttachmentFiles.length
        ? await readSelectedFilesAsDataUrls(selectedAttachmentFiles)
        : storedAttachmentEntries.map((entry) => entry.url);

    currentImageUrl = serializeUploadedMedia([...nextImages, ...nextAttachments]);
}

async function saveDraftRequest() {
    const result = await HeritageApi.request('/api/lpp/resources/drafts', {
        method: 'POST',
        body: buildDraftPayload()
    });
    const draft = HeritageApi.unwrapResult(result);
    return draft;
}

async function submitDraftRequest(draftId) {
    const result = await HeritageApi.request(`/api/lpp/resources/drafts/${draftId}/submit`, {
        method: 'PUT'
    });
    return HeritageApi.unwrapResult(result);
}

async function deleteDraft(resourceId) {
    if (!window.confirm('Delete this draft?')) {
        return;
    }
    try {
        await HeritageApi.unwrapResult(await HeritageApi.request(`/api/lpp/resources/drafts/${resourceId}?userId=${encodeURIComponent(currentUser.id)}`, {
            method: 'DELETE'
        }));
        if (currentResource && String(currentResource.id) === String(resourceId)) {
            resetForm();
        }
        await refreshWorkspace();
        showToast('Draft deleted successfully.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    }
}

async function deleteResource(resourceId) {
    if (!window.confirm('Delete this heritage project?')) {
        return;
    }
    try {
        await HeritageApi.unwrapResult(await HeritageApi.request(`/api/lpp/resources/${resourceId}?userId=${encodeURIComponent(currentUser.id)}`, {
            method: 'DELETE'
        }));
        if (currentResource && String(currentResource.id) === String(resourceId)) {
            resetForm();
        }
        await refreshWorkspace();
        showToast('Heritage project deleted successfully.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    }
}

function renderDeleteIcon(resourceId) {
    return `<button type="button" title="Delete project" aria-label="Delete project" data-action="delete-resource" data-id="${escapeHtml(resourceId)}" style="position:absolute; top:8px; right:8px; width:32px; height:32px; border:1px solid rgba(156, 35, 49, 0.18); border-radius:50%; background:#fff7f7; color:#9C2331; cursor:pointer; display:flex; align-items:center; justify-content:center; padding:0; box-shadow:0 4px 10px rgba(0,0,0,0.08); z-index:3; font-size:16px; line-height:1;">
        <span aria-hidden="true">&#128465;</span>
    </button>`;
}

function highlightFormCard() {
    const card = document.getElementById('mainFormCard');
    if (!card) {
        return;
    }
    card.style.transform = 'scale(1.02)';
    card.style.boxShadow = '0 0 25px rgba(196, 30, 58, 0.3)';
    window.setTimeout(() => {
        card.style.transform = '';
        card.style.boxShadow = '';
    }, 400);
}

function loadResourceIntoForm(resource, mode) {
    currentResource = resource;
    resourceIdInput.value = resource.id || '';
    currentImageUrl = resource.fileUrl || '';
    selectedImageFiles = [];
    selectedAttachmentFiles = [];
    titleInput.value = resource.title || '';
    categoryInput.value = mapCategoryValue(resource.category);
    ensureLocationOption(resource.location || '');
    locationInput.value = resource.location || '';
    if (tagsInput) {
        tagsInput.value = resource.tags || '';
    }
    descInput.value = resource.description || resource.content || '';
    externalLinkInput.value = resource.externalLink || '';
    autoResize(descInput);

    if (mode === 'revise') {
        formHeaderTitle.innerHTML = '<span class="text-warning">Revision Required:</span> ' + escapeHtml(resource.title || 'Untitled Heritage');
    } else if (mode === 'approved') {
        formHeaderTitle.innerHTML = '<span class="text-success">Approved Project:</span> ' + escapeHtml(resource.title || 'Untitled Heritage');
    } else {
        formHeaderTitle.innerHTML = 'Edit Draft: ' + escapeHtml(resource.title || 'Untitled Heritage');
    }

    highlightFormCard();
    updateImageSelectionUI();
    updateAttachmentSelectionUI();
    updateActionButtonState();
}

function renderDraftList() {
    draftCount.textContent = String(userDrafts.length);
    if (!userDrafts.length) {
        draftList.innerHTML = '<div class="list-group-item p-3 text-muted" style="font-size: 0.8rem;">No draft saved yet.</div>';
        return;
    }

    draftList.innerHTML = userDrafts.map((draft) => `
        <div class="list-group-item p-3">
            <h6 class="mb-1 fw-bold" style="color: #C41E3A; font-size: 0.9rem;">${escapeHtml(draft.title || 'Untitled Draft')}</h6>
            <small class="text-muted d-block mb-2" style="font-size: 0.7rem;">Last edited: ${escapeHtml(formatDateTime(draft.updateTime))}</small>
            <div class="d-flex gap-2">
                <button class="btn btn-sm btn-outline-dark px-3 py-1" style="border-radius: 12px; font-size: 0.75rem;" data-action="edit-draft" data-id="${escapeHtml(draft.id)}">Edit</button>
                <button class="btn btn-sm btn-link text-muted p-0" style="font-size: 0.75rem; text-decoration: none;" data-action="delete-draft" data-id="${escapeHtml(draft.id)}">Delete</button>
            </div>
        </div>
    `).join('');
}

function renderStatusCard() {
    const actionableResources = userResources.filter((resource) => {
        return resource.status === 1 || resource.status === 2 || resource.status === 3;
    });

    statusCardCount.textContent = String(actionableResources.length);

    if (!actionableResources.length) {
        statusCard.style.borderColor = '#C41E3A';
        statusCardHeader.className = 'card-header py-2 fw-bold bg-theme-primary';
        statusCardHeader.innerHTML = 'Submission Status <span class="badge bg-light text-danger float-end" id="statusCardCount">0</span>';
        statusCardHeader.querySelector('#statusCardCount').textContent = '0';
        statusCardContent.innerHTML = `
            <div class="p-2 bg-light rounded border-start border-4 border-secondary">
                <h6 class="fw-bold mb-1" style="font-size: 0.9rem;">No reviewed submission yet</h6>
                <p class="text-muted mb-0" style="font-size: 0.75rem;">Submitted resources will appear here after review.</p>
            </div>
        `;
        return;
    }

    const primary = actionableResources[0];
    let headerClass = 'bg-theme-primary';
    let countClass = 'text-danger';
    if (primary.status === 2) {
        headerClass = 'bg-theme-approved';
        countClass = 'text-success';
        statusCard.style.borderColor = '#2E8B57';
    } else if (primary.status === 1) {
        headerClass = 'bg-theme-pending';
        countClass = 'text-warning';
        statusCard.style.borderColor = '#8A5B2B';
    } else {
        headerClass = 'bg-theme-primary';
        countClass = 'text-danger';
        statusCard.style.borderColor = '#C41E3A';
    }

    statusCardHeader.className = `card-header py-2 fw-bold ${headerClass}`;
    statusCardHeader.innerHTML = `Submission Status <span class="badge bg-light ${countClass} float-end">${escapeHtml(actionableResources.length)}</span>`;

    statusCardContent.innerHTML = actionableResources.map((resource) => {
        const review = toReviewLabel(resource);
        if (resource.status === 3) {
            return `
                <div class="p-2 bg-light rounded border-start border-4 border-danger mb-2" style="position:relative;">
                    <h6 class="fw-bold text-danger mb-1" style="font-size: 0.9rem;">${escapeHtml(resource.title || 'Untitled Heritage')}</h6>
                    <p class="text-muted mb-2" style="font-size: 0.75rem;"><strong>Feedback:</strong> ${escapeHtml(review.feedback)}</p>
                    <p class="text-muted mb-2" style="font-size: 0.75rem;"><strong>${escapeHtml(review.reviewer)}</strong> · ${escapeHtml(review.reviewedAt)}</p>
                    <button class="btn btn-sm btn-outline-danger w-100 py-1" style="border-radius: 15px; font-size: 0.8rem;" data-action="revise-resource" data-id="${escapeHtml(resource.id)}">Revise & Resubmit</button>
                </div>
            `;
        }
        if (resource.status === 2) {
            const uploadButton = resource.platformPublished
                ? '<button class="btn btn-sm btn-outline-success w-100 py-1" style="border-radius: 15px; font-size: 0.8rem;" disabled>Uploaded to Platform</button>'
                : `<button class="btn btn-sm btn-outline-success w-100 py-1" style="border-radius: 15px; font-size: 0.8rem;" data-action="upload-resource" data-id="${escapeHtml(resource.id)}">Upload to Platform</button>`;
            return `
                <div class="p-2 bg-light rounded border-start border-4 border-success mb-2" style="position:relative;">
                    <h6 class="fw-bold text-success mb-1" style="font-size: 0.9rem;">${escapeHtml(resource.title || 'Untitled Heritage')}</h6>
                    <p class="text-muted mb-2" style="font-size: 0.75rem;"><strong>Status:</strong> Approved</p>
                    <p class="text-muted mb-2" style="font-size: 0.75rem;"><strong>${escapeHtml(review.reviewer)}</strong> · ${escapeHtml(review.reviewedAt)}</p>
                    <p class="text-muted mb-2" style="font-size: 0.75rem;"><strong>Feedback:</strong> ${escapeHtml(review.feedback)}</p>
                    ${uploadButton}
                </div>
            `;
        }
        return `
            <div class="p-2 bg-light rounded border-start border-4 border-warning mb-2" style="position:relative;">
                <h6 class="fw-bold mb-1" style="font-size: 0.9rem; color: #8A5B2B;">${escapeHtml(resource.title || 'Untitled Heritage')}</h6>
                <p class="text-muted mb-1" style="font-size: 0.75rem;"><strong>Status:</strong> Pending approval</p>
                <p class="text-muted mb-0" style="font-size: 0.75rem;">Submitted at ${escapeHtml(formatDateTime(resource.updateTime || resource.createTime))}</p>
            </div>
        `;
    }).join('');
}

async function refreshWorkspace() {
    const [draftResult, resourceResult] = await Promise.all([
        HeritageApi.request(`/api/lpp/resources/users/${currentUser.id}/drafts`),
        HeritageApi.request(`/api/lpp/resources/users/${currentUser.id}`)
    ]);

    userDrafts = HeritageApi.unwrapResult(draftResult) || [];
    userResources = HeritageApi.unwrapResult(resourceResult) || [];
    renderDraftList();
    renderStatusCard();
}

function findDraftById(resourceId) {
    return userDrafts.find((item) => String(item.id) === String(resourceId))
        || userResources.find((item) => String(item.id) === String(resourceId))
        || null;
}

async function saveDraftFlow() {
    if (!ensureCopyrightConfirmed()) {
        return;
    }
    if (!ensureImageProvided()) {
        return;
    }
    if (!currentUser || !currentUser.id) {
        showToast('Please log in again before saving a draft.');
        return;
    }

    if (!hasDraftContent()) {
        showToast('Enter some project information before saving a draft.');
        return;
    }

    try {
        const isEditingExistingDraft = Boolean(resourceIdInput.value);
        await ensureMediaDataReady();
        saveDraftButton.disabled = true;
        saveDraftButton.textContent = 'Saving...';
        const savedDraft = await saveDraftRequest();
        await refreshWorkspace();
        if (isEditingExistingDraft) {
            currentResource = savedDraft;
            resourceIdInput.value = savedDraft.id || '';
            currentImageUrl = savedDraft.fileUrl || currentImageUrl;
            loadResourceIntoForm(savedDraft, 'draft');
        } else {
            resetForm();
        }
        showToast('Draft saved successfully.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    } finally {
        saveDraftButton.textContent = 'Save Draft';
        updateActionButtonState();
    }
}

async function submitFlow(event) {
    event.preventDefault();
    if (!ensureCopyrightConfirmed()) {
        return;
    }
    if (!validateForm()) {
        return;
    }

    try {
        await ensureMediaDataReady();
        submitButton.disabled = true;
        submitButton.textContent = 'Submitting...';

        const savedDraft = await saveDraftRequest();
        const submittedResource = await submitDraftRequest(savedDraft.id);
        currentResource = submittedResource;
        resourceIdInput.value = submittedResource.id || '';
        await refreshWorkspace();
        resetForm();
        showToast('Application already sent for review!');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    } finally {
        submitButton.textContent = 'Submit for Review';
        updateActionButtonState();
    }
}

async function uploadToPlatform(resourceId) {
    try {
        const result = await HeritageApi.request(`/api/lpp/resources/${resourceId}/upload?userId=${encodeURIComponent(currentUser.id)}`, {
            method: 'PUT'
        });
        HeritageApi.unwrapResult(result);
        await refreshWorkspace();
        showToast('Approved project uploaded to platform.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    }
}

function handleSidebarClick(event) {
    const button = event.target.closest('[data-action]');
    if (!button) {
        return;
    }
    const resourceId = button.dataset.id;
    const targetResource = findDraftById(resourceId);
    if (!targetResource) {
        showToast('The selected resource could not be found.');
        return;
    }

    if (button.dataset.action === 'edit-draft') {
        loadResourceIntoForm(targetResource, 'draft');
        return;
    }
    if (button.dataset.action === 'delete-draft') {
        deleteDraft(resourceId);
        return;
    }
    if (button.dataset.action === 'revise-resource') {
        loadResourceIntoForm(targetResource, 'revise');
        return;
    }
    if (button.dataset.action === 'upload-resource') {
        uploadToPlatform(resourceId);
    }
}

async function initPage() {
    currentUser = HeritageSession.initPageSession({
        requireLogin: true,
        requiredRole: 'CONTRIBUTOR',
        redirectTo: 'welcome.html'
    });
    if (!currentUser) {
        return;
    }

    if (typeof HeritageSession.refreshCurrentUserFromServer === 'function') {
        try {
            currentUser = await HeritageSession.refreshCurrentUserFromServer() || currentUser;
        } catch (error) {
            currentUser = HeritageSession.getCurrentUser() || currentUser;
        }
    }

    autoResize(descInput);
    populateLocationOptions();

    if (imageUpload) {
        imageUpload.addEventListener('change', () => {
            try {
                const incomingFiles = normalizeSelectedFiles(
                    imageUpload.files,
                    isImageUploadFile,
                    MAX_IMAGE_FILES,
                    IMAGE_UPLOAD_MESSAGE
                );
                selectedImageFiles = mergeSelectedFiles(selectedImageFiles, incomingFiles, MAX_IMAGE_FILES);
                updateImageSelectionUI();
            } catch (error) {
                showToast(error.message);
            }
            imageUpload.value = '';
        });
    }

    if (attachmentUpload) {
        attachmentUpload.addEventListener('change', () => {
            try {
                const incomingFiles = normalizeSelectedFiles(
                    attachmentUpload.files,
                    isAttachmentUploadFile,
                    MAX_ATTACHMENT_FILES,
                    ATTACHMENT_UPLOAD_MESSAGE
                );
                selectedAttachmentFiles = mergeSelectedFiles(selectedAttachmentFiles, incomingFiles, MAX_ATTACHMENT_FILES);
                updateAttachmentSelectionUI();
            } catch (error) {
                showToast(error.message);
            }
            attachmentUpload.value = '';
        });
    }

    if (imageUploadList) {
        imageUploadList.addEventListener('click', (event) => {
            const button = event.target.closest('[data-action="remove-image-file"]');
            if (!button) {
                return;
            }
            removeSelectedImageFile(Number(button.dataset.index));
        });
    }

    if (attachmentUploadList) {
        attachmentUploadList.addEventListener('click', (event) => {
            const button = event.target.closest('[data-action="remove-attachment-file"]');
            if (!button) {
                return;
            }
            removeSelectedAttachmentFile(Number(button.dataset.index));
        });
    }

    updateImageSelectionUI();
    updateAttachmentSelectionUI();
    updateActionButtonState();
    await refreshWorkspace();
}

document.addEventListener('DOMContentLoaded', initPage);
descInput.addEventListener('input', () => autoResize(descInput));
saveDraftButton.addEventListener('click', saveDraftFlow);
form.addEventListener('submit', submitFlow);
draftList.addEventListener('click', handleSidebarClick);
statusCardContent.addEventListener('click', handleSidebarClick);
if (copyrightCheckbox) {
    copyrightCheckbox.addEventListener('change', updateActionButtonState);
}
[titleInput, categoryInput, locationInput, descInput, externalLinkInput, copyrightCheckbox, tagsInput].forEach(attachEnglishValidation);
updateActionButtonState();
