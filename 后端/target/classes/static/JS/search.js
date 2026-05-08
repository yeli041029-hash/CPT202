const provinceAnchors = [
  { id: 'xinjiang', name: 'Xinjiang', top: 27, left: 23, aliases: ['xinjiang', 'urumqi'] },
  { id: 'xizang', name: 'Xizang', top: 61.5, left: 24.5, aliases: ['xizang', 'tibet', 'lhasa'] },
  { id: 'qinghai', name: 'Qinghai', top: 50.5, left: 35.5, aliases: ['qinghai', 'xining'] },
  { id: 'gansu', name: 'Gansu', top: 45, left: 43, aliases: ['gansu', 'dunhuang', 'gansu corridor'] },
  { id: 'ningxia', name: 'Ningxia', top: 41.5, left: 47.5, aliases: ['ningxia', 'yinchuan'] },
  { id: 'inner-mongolia', name: 'Inner Mongolia', top: 33, left: 51, aliases: ['inner mongolia', 'neimenggu', 'hohhot'] },
  { id: 'heilongjiang', name: 'Heilongjiang', top: 17.5, left: 79.5, aliases: ['heilongjiang', 'harbin', 'qiqihar', 'mudanjiang', 'heihe'] },
  { id: 'jilin', name: 'Jilin', top: 27, left: 77.5, aliases: ['jilin', 'changchun', 'yanbian'] },
  { id: 'liaoning', name: 'Liaoning', top: 31.5, left: 75.5, aliases: ['liaoning', 'shenyang', 'dalian'] },
  { id: 'beijing', name: 'Beijing', top: 35, left: 67.5, aliases: ['beijing'] },
  { id: 'hebei', name: 'Hebei', top: 38.5, left: 67, aliases: ['hebei', 'shijiazhuang', 'baoding', 'qinhuangdao'] },
  { id: 'shanxi', name: 'Shanxi', top: 44, left: 60.5, aliases: ['shanxi', 'taiyuan', 'pingyao'] },
  { id: 'shandong', name: 'Shandong', top: 45, left: 71.5, aliases: ['shandong', 'jinan', 'qingdao', 'qufu'] },
  { id: 'henan', name: 'Henan', top: 52, left: 61, aliases: ['henan', 'luoyang', 'zhengzhou'] },
  { id: 'shaanxi', name: 'Shaanxi', top: 56, left: 54, aliases: ['shaanxi', 'xian'] },
  { id: 'jiangsu', name: 'Jiangsu', top: 59.5, left: 69.5, aliases: ['jiangsu', 'suzhou', 'nanjing', 'wuxi'] },
  { id: 'anhui', name: 'Anhui', top: 60.5, left: 65.5, aliases: ['anhui', 'huangshan', 'hefei', 'huizhou'] },
  { id: 'shanghai', name: 'Shanghai', top: 61.5, left: 73.5, aliases: ['shanghai'] },
  { id: 'hubei', name: 'Hubei', top: 61, left: 58.5, aliases: ['hubei', 'wuhan'] },
  { id: 'sichuan', name: 'Sichuan', top: 65.5, left: 46.5, aliases: ['sichuan', 'chengdu'] },
  { id: 'chongqing', name: 'Chongqing', top: 66.5, left: 51, aliases: ['chongqing'] },
  { id: 'zhejiang', name: 'Zhejiang', top: 69, left: 71.5, aliases: ['zhejiang', 'hangzhou', 'ningbo', 'longquan'] },
  { id: 'jiangxi', name: 'Jiangxi', top: 73, left: 64, aliases: ['jiangxi', 'jingdezhen', 'nanchang'] },
  { id: 'hunan', name: 'Hunan', top: 71.5, left: 58, aliases: ['hunan', 'changsha', 'xiangxi'] },
  { id: 'guizhou', name: 'Guizhou', top: 76.5, left: 52, aliases: ['guizhou', 'guiyang'] },
  { id: 'fujian', name: 'Fujian', top: 76.5, left: 70, aliases: ['fujian', 'xiamen', 'fuzhou', 'tulou'] },
  { id: 'yunnan', name: 'Yunnan', top: 81.5, left: 43.5, aliases: ['yunnan', 'kunming'] },
  { id: 'guangxi', name: 'Guangxi', top: 84, left: 56, aliases: ['guangxi', 'nanning', 'guilin'] },
  { id: 'guangdong', name: 'Guangdong', top: 82.5, left: 62, aliases: ['guangdong', 'guangzhou', 'shenzhen'] },
  { id: 'hainan', name: 'Hainan', top: 95, left: 53.5, aliases: ['hainan', 'haikou', 'sanya'] },
  { id: 'taiwan', name: 'Taiwan', top: 80.5, left: 73.5, aliases: ['taiwan', 'taipei'] }
];

const mapLayer = document.getElementById('mapLayer');
const provincePins = document.getElementById('provincePins');
const infoWindow = document.getElementById('infoWindow');
const infoTitle = document.getElementById('infoTitle');
const infoImg = document.getElementById('infoImg');
const infoDesc = document.getElementById('infoDesc');
const searchInput = document.querySelector('.search-bar input');
const searchButton = document.querySelector('.search-bar button');
const resultsList = document.getElementById('resultsList');
const resultCount = document.getElementById('resultCount');
const filterTags = Array.from(document.querySelectorAll('.filter-tag'));
const detailModal = document.getElementById('detailModal');
const detailModalClose = document.getElementById('detailModalClose');
const detailModalImage = document.getElementById('detailModalImage');
const detailModalTitle = document.getElementById('detailModalTitle');
const detailModalDescription = document.getElementById('detailModalDescription');
const detailImageSection = document.getElementById('detailImageSection');
const detailImageGallery = document.getElementById('detailImageGallery');
const detailPdfSection = document.getElementById('detailPdfSection');
const detailPdfLinks = document.getElementById('detailPdfLinks');
const detailVideoSection = document.getElementById('detailVideoSection');
const detailVideoLinks = document.getElementById('detailVideoLinks');
const detailVideoWrap = document.getElementById('detailVideoWrap');
const detailVideoPlayer = document.getElementById('detailVideoPlayer');
const detailLinkSection = document.getElementById('detailLinkSection');
const detailLinkLinks = document.getElementById('detailLinkLinks');

let platformItems = [];
let activeCategory = 'All';
let activeItemId = null;
let activeProvinceId = '';
let currentUser = null;
let currentDetailItem = null;

function normalizeCategory(category) {
  if (!category) {
    return 'Uncategorized';
  }
  const lower = String(category).trim().toLowerCase();
  if (lower === 'architecture') return 'Architecture';
  if (lower === 'crafts' || lower === 'craft') return 'Crafts';
  if (lower === 'folklore' || lower === 'folktales') return 'Folktales';
  if (lower === 'relics' || lower === 'relic') return 'Relics';
  return lower.charAt(0).toUpperCase() + lower.slice(1);
}

function escapeHtml(value) {
  return String(value == null ? '' : value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function isPlaceholderUrl(value) {
  if (!value || String(value).startsWith('data:')) {
    return false;
  }
  try {
    const parsed = new URL(value, window.location.href);
    return parsed.hostname === 'example.com' || parsed.hostname === 'www.example.com';
  } catch (error) {
    return false;
  }
}

function getStoredMediaUrls(value) {
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    if (Array.isArray(parsed)) {
      return parsed
        .map((item) => String(item || '').trim())
        .filter(Boolean);
    }
  } catch (error) {
  }

  const singleValue = String(value).trim();
  return singleValue ? [singleValue] : [];
}

function getMediaKind(url) {
  const normalized = String(url || '').trim().toLowerCase();
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

  if (/\.(png|jpe?g|gif|bmp|webp|svg|heic|heif)(\?.*)?$/.test(normalized)) {
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

function getMediaExtension(url, fallbackKind) {
  const normalized = String(url || '').trim();
  const dataUrlMatch = normalized.match(/^data:([^;,]+)[;,]/i);
  const mimeType = dataUrlMatch ? dataUrlMatch[1].toLowerCase() : '';

  if (mimeType === 'application/pdf') {
    return 'pdf';
  }
  if (mimeType.startsWith('image/')) {
    return mimeType.split('/')[1] || 'jpg';
  }
  if (mimeType.startsWith('video/')) {
    return mimeType.split('/')[1] || 'mp4';
  }

  const urlWithoutQuery = normalized.split('?')[0];
  const extensionMatch = urlWithoutQuery.match(/\.([a-z0-9]+)$/i);
  if (extensionMatch) {
    return extensionMatch[1].toLowerCase();
  }

  if (fallbackKind === 'pdf') {
    return 'pdf';
  }
  if (fallbackKind === 'video') {
    return 'mp4';
  }
  if (fallbackKind === 'image') {
    return 'jpg';
  }
  return 'bin';
}

function buildDownloadFilename(title, kind, index, url) {
  const safeTitle = String(title || 'heritage')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '') || 'heritage';
  const extension = getMediaExtension(url, kind);
  return `${safeTitle}-${kind}-${index + 1}.${extension}`;
}

function findProvinceId(location) {
  const source = String(location || '').toLowerCase();
  const matchedProvince = provinceAnchors.find((province) => province.aliases.some((alias) => source.includes(alias.toLowerCase())));
  return matchedProvince ? matchedProvince.id : '';
}

function preparePlatformItems(items) {
  return (Array.isArray(items) ? items : []).map((item) => {
    const provinceId = findProvinceId(item.location);
    const mediaItems = getStoredMediaUrls(item.imageUrl).map((url) => ({
      url: url,
      kind: getMediaKind(url)
    }));
    const imageItems = mediaItems.filter((entry) => entry.kind === 'image' && !isPlaceholderUrl(entry.url));
    const pdfItems = mediaItems.filter((entry) => entry.kind === 'pdf');
    const videoItems = mediaItems.filter((entry) => entry.kind === 'video');

    return Object.assign({}, item, {
      provinceId: provinceId,
      normalizedCategory: normalizeCategory(item.category),
      mediaItems: mediaItems,
      imageItems: imageItems,
      pdfItems: pdfItems,
      videoItems: videoItems,
      coverImage: imageItems.length
        ? imageItems[0].url
        : HeritageApi.fallbackImage(item.title || 'Heritage')
    });
  });
}

function upsertPlatformItem(item) {
  const preparedItem = preparePlatformItems([item])[0] || null;
  if (!preparedItem) {
    return null;
  }

  let replaced = false;
  platformItems = platformItems.map((currentItem) => {
    if (String(currentItem.id) === String(preparedItem.id)) {
      replaced = true;
      return preparedItem;
    }
    return currentItem;
  });

  if (!replaced) {
    platformItems.push(preparedItem);
  }

  return preparedItem;
}

async function fetchPlatformItemDetail(itemId) {
  const detailedItem = await HeritageApi.request('/api/zyl/display/platform/' + encodeURIComponent(itemId));
  return upsertPlatformItem(detailedItem);
}

async function ensurePlatformDetailItem(item) {
  if (!item) {
    return null;
  }

  const cachedItem = findItemById(item.id);
  if (cachedItem && !cachedItem.mediaSummaryOnly) {
    return cachedItem;
  }

  if (!item.mediaSummaryOnly) {
    return item;
  }

  return fetchPlatformItemDetail(item.id);
}

function resolveImage(item) {
  if (!item) {
    return HeritageApi.fallbackImage('Heritage');
  }
  return item.coverImage || HeritageApi.fallbackImage(item.title || 'Heritage');
}

function canDeleteItem(item) {
  if (!currentUser || !currentUser.id || !item) {
    return false;
  }
  if (currentUser.role === 'ADMIN') {
    return true;
  }
  return String(currentUser.id) === String(item.contributorId);
}

function renderDeleteButton(item) {
  if (!canDeleteItem(item)) {
    return '';
  }
  return `<button type="button" title="Delete project" aria-label="Delete project" data-action="delete-resource" data-id="${escapeHtml(item.id)}" style="position:absolute; top:10px; right:10px; width:30px; height:30px; border:1px solid rgba(156, 35, 49, 0.18); border-radius:50%; background:#fff7f7; color:#9C2331; cursor:pointer; display:flex; align-items:center; justify-content:center; padding:0; box-shadow:0 4px 10px rgba(0,0,0,0.08); z-index:3; font-size:15px; line-height:1;">
    <span aria-hidden="true">&#128465;</span>
  </button>`;
}

function getFilteredItems() {
  const keyword = searchInput ? searchInput.value.trim().toLowerCase() : '';
  return platformItems.filter((item) => {
    const matchesCategory = activeCategory === 'All' || item.normalizedCategory === activeCategory;
    const matchesKeyword = !keyword || [item.title, item.description, item.location, item.category, item.tags]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(keyword);
    return matchesCategory && matchesKeyword;
  });
}

function getProvinceItems(provinceId, items) {
  return (items || platformItems).filter((item) => item.provinceId === provinceId);
}

function findItemById(itemId, items) {
  return (items || platformItems).find((item) => String(item.id) === String(itemId)) || null;
}

function renderProvincePins() {
  const filteredItems = getFilteredItems();
  provincePins.innerHTML = provinceAnchors.map((province) => {
    const provinceItems = getProvinceItems(province.id, filteredItems);
    const hasItems = provinceItems.length > 0;
    const activeClass = activeProvinceId === province.id ? ' active' : '';
    const dotStyle = hasItems ? '' : 'style="background:#BCA98C; opacity:0.7;"';
    const labelStyle = hasItems ? '' : 'style="opacity:0.75;"';
    return `
      <div class="map-pin${activeClass}" id="pin-${province.id}" data-location="${province.id}" style="top:${province.top}%; left:${province.left}%;">
        <div class="pin-dot" ${dotStyle}></div>
        <div class="pin-label" ${labelStyle}>${province.name}</div>
      </div>
    `;
  }).join('');
}

function hideInfoWindow() {
  activeItemId = null;
  activeProvinceId = '';
  if (infoWindow) {
    infoWindow.classList.remove('show');
    infoWindow.removeAttribute('data-heritage-id');
  }
}

function renderResultCards() {
  const filteredItems = getFilteredItems();
  resultsList.innerHTML = filteredItems.length ? filteredItems.map((item) => `
    <div class="result-card${activeItemId === item.id ? ' active' : ''}" id="card-${item.id}" data-heritage-id="${item.id}" data-location="${item.provinceId}" style="position:relative;">
      ${renderDeleteButton(item)}
      <img src="${escapeHtml(resolveImage(item))}" alt="${escapeHtml(item.title || 'Heritage')}" loading="lazy" decoding="async">
      <div class="result-info">
        <h6>${escapeHtml(item.title || 'Untitled Heritage')}</h6>
      </div>
    </div>
  `).join('') : '<div class="result-card"><div class="result-info"><h6>No uploaded projects</h6></div></div>';

  if (resultCount) {
    resultCount.textContent = 'Found ' + filteredItems.length + ' items';
  }
}

function showInfoForItem(item, pinElement) {
  if (!item || !pinElement || !mapLayer || !infoWindow) {
    return;
  }
  const mapRect = mapLayer.getBoundingClientRect();
  const pinRect = pinElement.getBoundingClientRect();
  const centerX = (pinRect.left - mapRect.left) + (pinRect.width / 2);
  const topY = pinRect.top - mapRect.top;

  infoTitle.textContent = item.title || 'Untitled Heritage';
  infoImg.src = resolveImage(item);
  infoImg.alt = item.title || 'Heritage cover';
  if (infoDesc) {
    infoDesc.textContent = item.description || '';
  }
  infoWindow.dataset.heritageId = String(item.id);
  infoWindow.style.left = centerX + 'px';
  infoWindow.style.top = topY + 'px';
  infoWindow.classList.add('show');
}

function activateItem(itemId) {
  const filteredItems = getFilteredItems();
  const selectedItem = findItemById(itemId, filteredItems);
  if (!selectedItem) {
    return null;
  }

  activeItemId = selectedItem.id;
  activeProvinceId = selectedItem.provinceId || '';
  renderProvincePins();
  renderResultCards();

  const pinElement = activeProvinceId ? document.getElementById('pin-' + activeProvinceId) : null;
  if (pinElement) {
    showInfoForItem(selectedItem, pinElement);
  } else if (infoWindow) {
    infoWindow.classList.remove('show');
  }

  return selectedItem;
}

function activateProvince(provinceId) {
  const filteredItems = getFilteredItems();
  const provinceItems = getProvinceItems(provinceId, filteredItems);
  activeProvinceId = provinceId;
  activeItemId = provinceItems.length ? provinceItems[0].id : null;
  renderProvincePins();
  renderResultCards();

  const pinElement = document.getElementById('pin-' + provinceId);
  if (provinceItems.length && pinElement) {
    showInfoForItem(provinceItems[0], pinElement);
  } else if (infoWindow) {
    infoWindow.classList.remove('show');
  }
}

function applyFilters() {
  const filteredItems = getFilteredItems();
  if (activeItemId && !filteredItems.some((item) => String(item.id) === String(activeItemId))) {
    hideInfoWindow();
  }

  renderProvincePins();
  renderResultCards();

  if (activeItemId) {
    const activeItem = findItemById(activeItemId, filteredItems);
    const pinElement = activeItem && activeItem.provinceId
      ? document.getElementById('pin-' + activeItem.provinceId)
      : null;
    if (activeItem && pinElement) {
      showInfoForItem(activeItem, pinElement);
      return;
    }
  }

  if (infoWindow) {
    infoWindow.classList.remove('show');
  }
}

function hideDetailVideo() {
  if (!detailVideoPlayer || !detailVideoWrap) {
    return;
  }
  detailVideoPlayer.pause();
  detailVideoPlayer.removeAttribute('src');
  detailVideoPlayer.load();
  detailVideoWrap.hidden = true;
}

async function openDetailModal(item) {
  if (!item || !detailModal) {
    return;
  }

  let detailItem = item;
  try {
    detailItem = await ensurePlatformDetailItem(item);
  } catch (error) {
    window.alert(HeritageApi.getErrorMessage(error));
    return;
  }

  if (!detailItem) {
    return;
  }

  currentDetailItem = detailItem;
  detailModalImage.src = resolveImage(detailItem);
  detailModalImage.alt = detailItem.title || 'Heritage cover';
  detailModalTitle.textContent = detailItem.title || 'Untitled Heritage';
  detailModalDescription.textContent = detailItem.description || 'No description available.';

  const supplementalImages = detailItem.imageItems.slice(1);
  if (supplementalImages.length) {
    detailImageGallery.innerHTML = supplementalImages.map((entry, index) => `
      <img src="${escapeHtml(entry.url)}" alt="${escapeHtml((detailItem.title || 'Heritage') + ' image ' + (index + 2))}" loading="lazy" decoding="async">
    `).join('');
    detailImageSection.hidden = false;
  } else {
    detailImageGallery.innerHTML = '';
    detailImageSection.hidden = true;
  }

  if (detailItem.pdfItems.length) {
    detailPdfLinks.innerHTML = detailItem.pdfItems.map((entry, index) => `
      <a class="detail-link" href="${escapeHtml(entry.url)}" download="${escapeHtml(buildDownloadFilename(detailItem.title, 'pdf', index, entry.url))}">
        Download PDF ${index + 1}
      </a>
    `).join('');
    detailPdfSection.hidden = false;
  } else {
    detailPdfLinks.innerHTML = '';
    detailPdfSection.hidden = true;
  }

  if (detailItem.videoItems.length) {
    detailVideoLinks.innerHTML = detailItem.videoItems.map((entry, index) => `
      <button type="button" class="detail-link" data-play-video-index="${index}">
        Play Video ${index + 1}
      </button>
    `).join('');
    detailVideoSection.hidden = false;
  } else {
    detailVideoLinks.innerHTML = '';
    detailVideoSection.hidden = true;
  }

  const externalLink = String(detailItem.externalLink || '').trim();
  if (externalLink) {
    detailLinkLinks.innerHTML = `
      <a class="detail-link" href="${escapeHtml(externalLink)}" target="_blank" rel="noopener">
        Open External Link
      </a>
    `;
    detailLinkSection.hidden = false;
  } else {
    detailLinkLinks.innerHTML = '';
    detailLinkSection.hidden = true;
  }

  hideDetailVideo();
  detailModal.classList.add('is-open');
  detailModal.setAttribute('aria-hidden', 'false');
  document.body.style.overflow = 'hidden';
}

function closeDetailModal() {
  if (!detailModal) {
    return;
  }
  hideDetailVideo();
  detailModal.classList.remove('is-open');
  detailModal.setAttribute('aria-hidden', 'true');
  currentDetailItem = null;
  document.body.style.overflow = '';
}

function playDetailVideo(videoIndex) {
  if (!currentDetailItem || !Array.isArray(currentDetailItem.videoItems)) {
    return;
  }
  const targetVideo = currentDetailItem.videoItems[videoIndex];
  if (!targetVideo || !detailVideoPlayer || !detailVideoWrap) {
    return;
  }

  detailVideoPlayer.src = targetVideo.url;
  detailVideoWrap.hidden = false;
  detailVideoPlayer.load();
  detailVideoPlayer.play().catch(() => {
  });
}

async function loadPlatformItems() {
  const items = await HeritageApi.request('/api/zyl/display/platform');
  platformItems = preparePlatformItems(items).filter((item) => Boolean(item.provinceId));
  applyFilters();
}

async function deletePlatformItem(itemId) {
  const targetItem = findItemById(itemId);
  if (!targetItem || !canDeleteItem(targetItem)) {
    return;
  }

  if (!window.confirm('Delete this heritage project?')) {
    return;
  }

  const endpoint = currentUser.role === 'ADMIN'
    ? `/api/lpp/admin/resources/${itemId}?adminId=${encodeURIComponent(currentUser.id)}`
    : `/api/lpp/resources/${itemId}?userId=${encodeURIComponent(currentUser.id)}`;

  try {
    const result = await HeritageApi.request(endpoint, { method: 'DELETE' });
    HeritageApi.unwrapResult(result);

    if (String(activeItemId) === String(itemId)) {
      hideInfoWindow();
    }
    if (currentDetailItem && String(currentDetailItem.id) === String(itemId)) {
      closeDetailModal();
    }

    await loadPlatformItems();
  } catch (error) {
    window.alert(HeritageApi.getErrorMessage(error));
  }
}

filterTags.forEach((tag) => {
  tag.addEventListener('click', function () {
    activeCategory = this.textContent.trim();
    filterTags.forEach((item) => item.classList.remove('active'));
    this.classList.add('active');
    applyFilters();
  });
});

if (searchInput) {
  searchInput.addEventListener('input', applyFilters);
}

if (searchButton) {
  searchButton.addEventListener('click', applyFilters);
}

provincePins.addEventListener('click', function (event) {
  const pin = event.target.closest('.map-pin[data-location]');
  if (!pin) {
    return;
  }
  activateProvince(pin.dataset.location);
});

resultsList.addEventListener('click', async function (event) {
  const deleteButton = event.target.closest('[data-action="delete-resource"]');
  if (deleteButton) {
    deletePlatformItem(deleteButton.dataset.id);
    return;
  }

  const card = event.target.closest('.result-card[data-heritage-id]');
  if (!card) {
    return;
  }

  const selectedItem = activateItem(card.dataset.heritageId);
  if (selectedItem) {
    await openDetailModal(selectedItem);
  }
});

resultsList.addEventListener('mouseover', function (event) {
  const card = event.target.closest('.result-card[data-location]');
  if (!card) {
    return;
  }
  const pin = document.getElementById('pin-' + card.dataset.location);
  if (pin && card.dataset.location !== activeProvinceId) {
    pin.style.transform = 'scale(1.2) translateY(-3px)';
    pin.style.zIndex = '15';
  }
});

resultsList.addEventListener('mouseout', function (event) {
  const card = event.target.closest('.result-card[data-location]');
  if (!card) {
    return;
  }
  const pin = document.getElementById('pin-' + card.dataset.location);
  if (pin && card.dataset.location !== activeProvinceId) {
    pin.style.transform = '';
    pin.style.zIndex = '';
  }
});

if (infoWindow) {
  infoWindow.addEventListener('click', async function () {
    const targetItem = findItemById(infoWindow.dataset.heritageId, getFilteredItems()) || findItemById(infoWindow.dataset.heritageId);
    if (targetItem) {
      await openDetailModal(targetItem);
    }
  });

  infoWindow.addEventListener('keydown', async function (event) {
    if (event.key !== 'Enter' && event.key !== ' ') {
      return;
    }
    event.preventDefault();
    const targetItem = findItemById(infoWindow.dataset.heritageId, getFilteredItems()) || findItemById(infoWindow.dataset.heritageId);
    if (targetItem) {
      await openDetailModal(targetItem);
    }
  });
}

if (detailModal) {
  detailModal.addEventListener('click', function (event) {
    const closeTrigger = event.target.closest('[data-detail-close]');
    if (closeTrigger || event.target === detailModal) {
      closeDetailModal();
      return;
    }

    const playButton = event.target.closest('[data-play-video-index]');
    if (!playButton) {
      return;
    }
    playDetailVideo(Number(playButton.dataset.playVideoIndex));
  });
}

if (detailModalClose) {
  detailModalClose.addEventListener('click', closeDetailModal);
}

document.addEventListener('keydown', function (event) {
  if (event.key === 'Escape' && detailModal && detailModal.classList.contains('is-open')) {
    closeDetailModal();
  }
});

if (mapLayer) {
  mapLayer.addEventListener('click', function (event) {
    if (event.target === mapLayer || event.target === provincePins) {
      hideInfoWindow();
      renderProvincePins();
      renderResultCards();
    }
  });
}

document.addEventListener('DOMContentLoaded', async function () {
  currentUser = HeritageSession.initPageSession();

  const refreshPromise = typeof HeritageSession.refreshCurrentUserFromServer === 'function'
    ? HeritageSession.refreshCurrentUserFromServer()
      .then(function (refreshedUser) {
        currentUser = refreshedUser || currentUser;
        return currentUser;
      })
      .catch(function () {
        currentUser = HeritageSession.getCurrentUser ? HeritageSession.getCurrentUser() : currentUser;
        return currentUser;
      })
    : Promise.resolve(currentUser);

  renderProvincePins();
  renderResultCards();

  let itemsLoaded = false;
  try {
    await loadPlatformItems();
    itemsLoaded = true;
  } catch (error) {
    resultsList.innerHTML = '<div class="result-card"><div class="result-info"><h6>Load failed</h6><p>' + escapeHtml(HeritageApi.getErrorMessage(error)) + '</p></div></div>';
    if (resultCount) {
      resultCount.textContent = 'Found 0 items';
    }
  }

  await refreshPromise;
  if (itemsLoaded) {
    applyFilters();
  }
});
