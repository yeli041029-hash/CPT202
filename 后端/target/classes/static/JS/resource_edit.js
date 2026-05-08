function autoResize(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = textarea.scrollHeight + 'px';
}

const form = document.getElementById('resourceForm');
const publishButton = form ? form.querySelector('.publish-btn') : null;
const imageInput = document.getElementById('resourceImage');
const imageTrigger = document.getElementById('resourceImageTrigger');
const imageStatus = document.getElementById('resourceImageStatus');
const imageCount = document.getElementById('resourceImageCount');
const imageList = document.getElementById('resourceImageList');
const confirmCheckbox = document.getElementById('resourceConfirm');
const confirmWarning = document.getElementById('resourceConfirmWarning');
const MAX_UPLOAD_FILES = 10;
const SUPPORTED_UPLOAD_MESSAGE = 'Only images and videos are supported.';
const SUPPORTED_FILE_EXTENSION_PATTERN = /\.(png|jpe?g|gif|bmp|webp|svg|heic|heif|mp4|mov|avi|wmv|mkv|webm|m4v|mpeg|mpg|ogg|ogv|3gp)$/i;
let currentUser = null;
let selectedImageFiles = [];

function setPublishButtonState() {
    if (!publishButton) {
        return;
    }

    const enabled = !confirmCheckbox || confirmCheckbox.checked;
    publishButton.style.opacity = enabled ? '1' : '0.55';
    publishButton.style.cursor = enabled ? 'pointer' : 'not-allowed';
}

function showConfirmWarning(visible) {
    if (!confirmWarning) {
        return;
    }
    confirmWarning.style.display = visible ? 'block' : 'none';
}

function readImageAsDataUrl(file) {
    return new Promise(function (resolve, reject) {
        const reader = new FileReader();
        reader.onload = function () {
            resolve(reader.result);
        };
        reader.onerror = function () {
            reject(new Error('Failed to read the selected file.'));
        };
        reader.readAsDataURL(file);
    });
}

function escapeHtml(value) {
    return String(value == null ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function isSupportedUploadFile(file) {
    if (!file) {
        return false;
    }

    const fileType = String(file.type || '').toLowerCase();
    const fileName = String(file.name || '').toLowerCase();
    return fileType.startsWith('image/')
        || fileType.startsWith('video/')
        || SUPPORTED_FILE_EXTENSION_PATTERN.test(fileName);
}

function normalizeSelectedFiles(fileList) {
    const files = Array.from(fileList || []);
    const invalidFile = files.find((file) => !isSupportedUploadFile(file));
    if (invalidFile) {
        throw new Error(SUPPORTED_UPLOAD_MESSAGE);
    }
    if (files.length > MAX_UPLOAD_FILES) {
        throw new Error('You can upload up to 10 files at a time.');
    }
    return files;
}

function getFileKey(file) {
    return [file.name, file.size, file.lastModified].join('__');
}

function mergeSelectedFiles(existingFiles, incomingFiles) {
    const mergedFiles = [...existingFiles];
    const fileKeys = new Set(existingFiles.map(getFileKey));

    incomingFiles.forEach((file) => {
        const fileKey = getFileKey(file);
        if (!fileKeys.has(fileKey)) {
            mergedFiles.push(file);
            fileKeys.add(fileKey);
        }
    });

    if (mergedFiles.length > MAX_UPLOAD_FILES) {
        throw new Error('You can upload up to 10 files at a time.');
    }
    return mergedFiles;
}

function renderSelectedImageFileList() {
    if (!imageList) {
        return;
    }

    if (!selectedImageFiles.length) {
        imageList.innerHTML = '';
        imageList.classList.remove('has-files');
        return;
    }

    imageList.innerHTML = selectedImageFiles.map(function (file, index) {
        return `
            <div class="upload-file-row">
                <span class="upload-file-name" title="${escapeHtml(file.name)}">${escapeHtml(file.name)}</span>
                <button type="button" class="upload-file-remove" data-action="remove-image-file" data-index="${index}" aria-label="Remove ${escapeHtml(file.name)}" title="Remove file">
                    <span aria-hidden="true">&#128465;</span>
                </button>
            </div>
        `;
    }).join('');
    imageList.classList.add('has-files');
}

function updateImageSelectionUI() {
    if (!selectedImageFiles.length) {
        if (imageStatus) {
            imageStatus.textContent = 'No file selected';
        }
        if (imageCount) {
            imageCount.textContent = '0 files selected';
        }
        renderSelectedImageFileList();
        return;
    }

    if (imageStatus) {
        imageStatus.textContent = selectedImageFiles.length === 1
            ? selectedImageFiles[0].name
            : selectedImageFiles.length + ' files selected';
    }

    if (imageCount) {
        imageCount.textContent = selectedImageFiles.length === 1
            ? '1 file selected'
            : selectedImageFiles.length + ' files selected';
    }

    renderSelectedImageFileList();
}

function removeSelectedImageFile(index) {
    if (Number.isNaN(index) || index < 0 || index >= selectedImageFiles.length) {
        return;
    }
    selectedImageFiles = selectedImageFiles.filter(function (_, fileIndex) {
        return fileIndex !== index;
    });
    updateImageSelectionUI();
}

async function readImagesAsPayload(fileList) {
    const files = normalizeSelectedFiles(fileList);
    const mediaItems = await Promise.all(files.map(readImageAsDataUrl));
    if (!mediaItems.length) {
        return '';
    }
    return mediaItems.length === 1 ? mediaItems[0] : JSON.stringify(mediaItems);
}

window.addEventListener('DOMContentLoaded', function () {
    currentUser = HeritageSession.initPageSession({
        requireLogin: true,
        redirectTo: 'welcome.html'
    });

    const desc = document.getElementById('resourceDescription');
    if (desc && desc.value) {
        autoResize(desc);
    }

    if (imageInput) {
        if (imageTrigger) {
            imageTrigger.addEventListener('click', function () {
                imageInput.click();
            });
        }

        imageInput.addEventListener('change', function () {
            try {
                const incomingFiles = normalizeSelectedFiles(imageInput.files);
                selectedImageFiles = mergeSelectedFiles(selectedImageFiles, incomingFiles);
                updateImageSelectionUI();
            } catch (error) {
                alert(error.message);
            }
            imageInput.value = '';
        });
    }

    if (imageList) {
        imageList.addEventListener('click', function (event) {
            const button = event.target.closest('[data-action="remove-image-file"]');
            if (!button) {
                return;
            }
            removeSelectedImageFile(Number(button.dataset.index));
        });
    }

    updateImageSelectionUI();

    if (confirmCheckbox) {
        confirmCheckbox.addEventListener('change', function () {
            showConfirmWarning(false);
            setPublishButtonState();
        });
    }

    setPublishButtonState();
});

if (form) {
    form.addEventListener('submit', async function (event) {
        event.preventDefault();

        if (confirmCheckbox && !confirmCheckbox.checked) {
            showConfirmWarning(true);
            setPublishButtonState();
            return;
        }

        if (!currentUser || !currentUser.id) {
            alert('Please log in before publishing a resource.');
            window.location.href = 'welcome.html';
            return;
        }

        const title = document.getElementById('resourceTitle').value.trim();
        const description = document.getElementById('resourceDescription').value.trim();
        const tagsInput = document.getElementById('resourceTags');
        const tags = tagsInput ? tagsInput.value.trim() : '';
        if (!title || !description || !selectedImageFiles.length) {
            alert('Please fill all required fields and select at least one image or video file.');
            return;
        }

        if (publishButton) {
            publishButton.disabled = true;
            publishButton.textContent = 'Publishing...';
        }

        try {
            const imageData = await readImagesAsPayload(selectedImageFiles);
            const createdPost = await HeritageApi.request('/api/zyl/display', {
                method: 'POST',
                body: {
                    userId: currentUser.id,
                    title: title,
                    description: description,
                    tags: tags,
                    imageUrl: imageData
                }
            });

            window.location.href = 'resource.html?heritageId=' + createdPost.id;
        } catch (error) {
            alert(HeritageApi.getErrorMessage(error));
            if (publishButton) {
                publishButton.disabled = false;
                publishButton.textContent = 'Publish Resource';
            }
        }
    });
}
