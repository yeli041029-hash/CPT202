const DEFAULT_AVATAR = '../Resources/image/IMG_1113.JPG';
const CATEGORY_FALLBACK_IMAGES = {
    crafts: '../Resources/image/resource1.jpg',
    architecture: '../Resources/image/heritage1.JPG',
    relics: '../Resources/image/heritage3.JPG',
    folklore: '../Resources/image/lion_dance.JPG',
    default: '../Resources/image/resource2.jpg'
};

const feed = document.getElementById('resourceFeed');
const expandedComments = new Set();
const loadingDetails = new Set();
const hydratingMediaPosts = new Set();
const postDetails = {};
const replyTargets = {};
const commentDrafts = {};
let posts = [];
let currentUser = null;
let highlightedHeritageId = null;
let toastTimer = null;

function getMediaUrls(value) {
    if (!value) {
        return [];
    }

    try {
        const parsed = JSON.parse(value);
        if (Array.isArray(parsed)) {
            return parsed
                .map(function (item) { return String(item || '').trim(); })
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

function escapeHtml(value) {
    return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function showToast(message) {
    let toast = document.getElementById('resourceToast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'resourceToast';
        toast.style.position = 'fixed';
        toast.style.right = '28px';
        toast.style.bottom = '28px';
        toast.style.padding = '12px 18px';
        toast.style.borderRadius = '12px';
        toast.style.background = 'rgba(58, 38, 24, 0.95)';
        toast.style.color = '#fff';
        toast.style.fontSize = '0.92rem';
        toast.style.boxShadow = '0 12px 24px rgba(0, 0, 0, 0.18)';
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(12px)';
        toast.style.pointerEvents = 'none';
        toast.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
        toast.style.zIndex = '5000';
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.style.opacity = '1';
    toast.style.transform = 'translateY(0)';

    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(function () {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(12px)';
    }, 2200);
}

function isPlaceholderUrl(value) {
    if (!value) {
        return false;
    }

    try {
        const parsed = new URL(value, window.location.href);
        return parsed.hostname === 'example.com' || parsed.hostname === 'www.example.com';
    } catch (error) {
        return false;
    }
}

function getCurrentUserKey() {
    if (!currentUser) {
        return '__guest_browser__';
    }
    return String(currentUser.id || currentUser.email || currentUser.username);
}

function normalizePostId(postId) {
    return String(postId);
}

function isPostOwnedByCurrentUser(post) {
    return Boolean(currentUser && currentUser.id && String(currentUser.id) === String(post.contributorId));
}

function getLikeCount(post) {
    return Number(post && post.likeCount || 0);
}

function getShareCount(post) {
    return Number(post && post.shareCount || 0);
}

function isLikedByCurrentUser(post) {
    return Boolean(post && post.likedByCurrentUser);
}

function updatePostInFeed(updatedPost) {
    const normalizedPostId = normalizePostId(updatedPost.id);
    posts = posts.map(function (post) {
        if (normalizePostId(post.id) === normalizedPostId) {
            return Object.assign({}, post, updatedPost);
        }
        return post;
    });
}

async function toggleLike(postId) {
    const normalizedPostId = normalizePostId(postId);

    if (!currentUser || !currentUser.id) {
        showToast('Please log in before liking a post.');
        return;
    }

    try {
        const updatedPost = await HeritageApi.request(
            '/api/zyl/display/' + normalizedPostId + '/like?userId=' + encodeURIComponent(currentUser.id),
            { method: 'PUT' }
        );
        updatePostInFeed(updatedPost);
        renderPosts();
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    }
}

async function incrementShareCount(postId) {
    const normalizedPostId = normalizePostId(postId);
    const query = currentUser && currentUser.id
        ? '?userId=' + encodeURIComponent(currentUser.id)
        : '';

    const updatedPost = await HeritageApi.request(
        '/api/zyl/display/' + normalizedPostId + '/share' + query,
        { method: 'POST' }
    );
    updatePostInFeed(updatedPost);
    renderPosts();
}

function clearPostInteractionState(postId) {
    const normalizedPostId = normalizePostId(postId);
    delete postDetails[normalizedPostId];
    delete replyTargets[normalizedPostId];
    delete commentDrafts[normalizedPostId];
    expandedComments.delete(normalizedPostId);
    loadingDetails.delete(normalizedPostId);
}

function resolveImage(post) {
    const primaryImageUrl = getMediaUrls(post.imageUrl).find(function (url) {
        return getMediaKind(url) === 'image' && !isPlaceholderUrl(url);
    });
    if (primaryImageUrl && !isPlaceholderUrl(primaryImageUrl)) {
        return primaryImageUrl;
    }

    const category = String(post.category || '').toLowerCase();
    return CATEGORY_FALLBACK_IMAGES[category] || CATEGORY_FALLBACK_IMAGES.default || HeritageApi.fallbackImage(post.title || 'Heritage');
}

function resolveImages(post) {
    const mediaUrls = getMediaUrls(post.imageUrl)
        .filter(function (url) {
            const kind = getMediaKind(url);
            return url && !isPlaceholderUrl(url) && (kind === 'image' || kind === 'video');
        });

    if (mediaUrls.length) {
        return mediaUrls;
    }

    return [resolveImage(post)];
}

function renderMediaItem(url, index) {
    if (getMediaKind(url) === 'video') {
        return '<video class="res-post-video" controls preload="metadata" playsinline>' +
            '<source src="' + escapeHtml(url) + '">' +
            'Your browser does not support the video tag.' +
            '</video>';
    }

    return '<img class="res-post-image" src="' + escapeHtml(url) + '" alt="Heritage Resource ' + (index + 1) + '" loading="lazy" decoding="async">';
}

function renderPostMedia(post) {
    const mediaUrls = resolveImages(post);
    if (mediaUrls.length > 1) {
        return '<div class="res-post-media-grid">' + mediaUrls.map(function (url, index) {
            return renderMediaItem(url, index);
        }).join('') + '</div>';
    }

    return renderMediaItem(mediaUrls[0], 0);
}

function getAuthorName(post) {
    if (post.contributorName) {
        return post.contributorName;
    }
    if (post.contributorId) {
        return 'Contributor #' + post.contributorId;
    }
    return 'Community Contributor';
}

function getAuthorAvatar(post) {
    if (post.contributorAvatarUrl && !isPlaceholderUrl(post.contributorAvatarUrl)) {
        return post.contributorAvatarUrl;
    }
    return DEFAULT_AVATAR;
}

function countComments(comments) {
    if (!Array.isArray(comments) || comments.length === 0) {
        return 0;
    }

    return comments.reduce(function (total, comment) {
        return total + 1 + countComments(comment.replies);
    }, 0);
}

function getCommentCount(post) {
    const detail = postDetails[normalizePostId(post.id)];
    if (detail && Array.isArray(detail.comments)) {
        return countComments(detail.comments);
    }
    return Number(post.commentCount || 0);
}

function formatDateTime(value) {
    return HeritageApi.formatDateTime(value);
}

function getCommentDraft(postId) {
    return commentDrafts[normalizePostId(postId)] || '';
}

function getReplyTarget(postId) {
    return replyTargets[normalizePostId(postId)] || null;
}

function findCommentById(comments, commentId) {
    if (!Array.isArray(comments)) {
        return null;
    }

    const normalizedCommentId = normalizePostId(commentId);
    for (const comment of comments) {
        if (normalizePostId(comment.id) === normalizedCommentId) {
            return comment;
        }
        const nestedComment = findCommentById(comment.replies, normalizedCommentId);
        if (nestedComment) {
            return nestedComment;
        }
    }

    return null;
}

function focusCommentInput(postId) {
    const normalizedPostId = normalizePostId(postId);
    window.requestAnimationFrame(function () {
        const input = feed.querySelector('.res-comment-input[data-post-id="' + normalizedPostId + '"]');
        if (!input) {
            return;
        }
        input.focus();
        const length = input.value.length;
        input.setSelectionRange(length, length);
    });
}

function renderReplyBanner(postId) {
    const replyTarget = getReplyTarget(postId);
    if (!replyTarget) {
        return '';
    }

    return '<div style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-top:12px; padding:8px 10px; border-radius:10px; background:rgba(212, 175, 55, 0.12); font-size:0.8rem; color:#75593A;">' +
        '<span>Replying to ' + escapeHtml(replyTarget.username || 'Unknown User') + '</span>' +
        '<button class="res-btn" data-action="clear-reply" data-post-id="' + postId + '" style="padding:4px 10px; font-size:0.76rem;">Cancel</button>' +
        '</div>';
}

function renderDeleteButton(post) {
    if (!isPostOwnedByCurrentUser(post)) {
        return '';
    }

    return '<button data-action="delete-post" data-post-id="' + normalizePostId(post.id) + '" title="Delete post" aria-label="Delete post" style="position:absolute; top:16px; right:16px; width:34px; height:34px; border:none; border-radius:50%; background:rgba(196, 30, 58, 0.10); color:#9C2331; cursor:pointer; display:flex; align-items:center; justify-content:center; padding:0;">' +
        '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
        '<path d="M3 6h18" />' +
        '<path d="M8 6V4h8v2" />' +
        '<path d="M19 6l-1 14H6L5 6" />' +
        '<path d="M10 11v6" />' +
        '<path d="M14 11v6" />' +
        '</svg>' +
        '</button>';
}

function renderCommentItem(comment, postId, depth) {
    const normalizedPostId = normalizePostId(postId);
    const normalizedCommentId = normalizePostId(comment.id);
    const replyActionAttributes = 'data-action="select-reply" data-post-id="' + normalizedPostId + '" data-comment-id="' + normalizedCommentId + '"';
    const nestedReplies = Array.isArray(comment.replies)
        ? comment.replies.map(function (reply) {
            return renderCommentItem(reply, normalizedPostId, depth + 1);
        }).join('')
        : '';

    if (depth > 0) {
        return '<div ' + replyActionAttributes + ' style="margin:8px 0 0 18px; padding:8px 10px; border-left:2px solid rgba(212, 175, 55, 0.45); background:rgba(212, 175, 55, 0.08); border-radius:8px; cursor:pointer;">' +
            '<div style="font-size:0.8rem; color:#5B4630; line-height:1.45;">' +
            '<strong style="color:#3A2618;">' + escapeHtml(comment.username || 'Unknown User') + '</strong>' +
            (comment.replyToUsername ? ' replied to <strong style="color:#3A2618;">' + escapeHtml(comment.replyToUsername) + '</strong>' : '') +
            ': ' + escapeHtml(comment.content || '') +
            '</div>' +
            '<div style="margin-top:4px; font-size:0.74rem; color:#8C745A;">' + escapeHtml(formatDateTime(comment.sentAt)) + '</div>' +
            nestedReplies +
            '</div>';
    }

    return '<div ' + replyActionAttributes + ' style="padding:10px 0; border-bottom:1px solid rgba(0,0,0,0.06); cursor:pointer;">' +
        '<div style="display:flex; justify-content:space-between; gap:10px; margin-bottom:5px; font-size:0.82rem; color:#7D6244;">' +
        '<strong style="color:#3A2618;">' + escapeHtml(comment.username || 'Unknown User') + '</strong>' +
        '<span>' + escapeHtml(formatDateTime(comment.sentAt)) + '</span>' +
        '</div>' +
        '<div style="color:#4A3B2A; line-height:1.5; font-size:0.92rem;">' + escapeHtml(comment.content || '') + '</div>' +
        nestedReplies +
        '</div>';
}

function renderComments(postId) {
    const normalizedPostId = normalizePostId(postId);
    const detail = postDetails[normalizedPostId];
    if (loadingDetails.has(normalizedPostId)) {
        return '<div style="padding:6px 0 10px; color:#75593A; font-size:0.9rem;">Loading comments...</div>';
    }

    if (!detail || !Array.isArray(detail.comments) || detail.comments.length === 0) {
        return '<div style="padding:6px 0 10px; color:#75593A; font-size:0.9rem;">No comments yet.</div>';
    }

    return detail.comments.map(function (comment) {
        return renderCommentItem(comment, normalizedPostId, 0);
    }).join('');
}

function renderCommentComposer(postId) {
    const normalizedPostId = normalizePostId(postId);
    const replyTarget = getReplyTarget(normalizedPostId);
    const placeholder = replyTarget
        ? 'Reply to ' + (replyTarget.username || 'this comment') + '...'
        : 'Write your comment...';

    return renderReplyBanner(normalizedPostId) +
        '<div style="display:flex; align-items:flex-end; gap:12px; margin-top:14px;">' +
        '<textarea class="res-comment-input" data-post-id="' + normalizedPostId + '" placeholder="' + escapeHtml(placeholder) + '" style="margin-top:0; min-height:44px; max-height:140px; resize:vertical;">' + escapeHtml(getCommentDraft(normalizedPostId)) + '</textarea>' +
        '<button class="res-btn" data-action="send-comment" data-post-id="' + normalizedPostId + '" style="min-width:84px; height:44px;">Send</button>' +
        '</div>';
}

function renderPostCard(post) {
    const normalizedPostId = normalizePostId(post.id);
    const liked = isLikedByCurrentUser(post);
    const commentsExpanded = expandedComments.has(normalizedPostId);
    const likeCount = getLikeCount(post);
    const commentCount = getCommentCount(post);
    const shareCount = getShareCount(post);

    return '<div class="res-post-card" id="post-' + normalizedPostId + '" data-post-id="' + normalizedPostId + '" style="position:relative;">' +
        renderDeleteButton(post) +
        '<div class="res-card-header">' +
        '<img src="' + escapeHtml(getAuthorAvatar(post)) + '" class="res-user-img" alt="User" loading="lazy" decoding="async">' +
        '<span class="res-user-name">' + escapeHtml(getAuthorName(post)) + '</span>' +
        '</div>' +
        '<h2 class="res-post-title">' + escapeHtml(post.title || 'Untitled Heritage') + '</h2>' +
        '<div class="res-post-media" data-post-id="' + normalizedPostId + '">' + renderPostMedia(post) + '</div>' +
        '<p class="res-post-description">' + escapeHtml(post.description || 'No description available.') + '</p>' +
        '<div class="res-card-actions">' +
        '<button class="res-btn likeBtn" data-action="like" data-post-id="' + normalizedPostId + '">' + (liked ? 'Liked' : 'Like') + ' (<span class="like-count">' + likeCount + '</span>)</button>' +
        '<button class="res-btn commentBtn" data-action="toggle-comment" data-post-id="' + normalizedPostId + '">Comment (' + commentCount + ')</button>' +
        '<button class="res-btn shareBtn" data-action="share" data-post-id="' + normalizedPostId + '">Share (' + shareCount + ')</button>' +
        '</div>' +
        (commentsExpanded
            ? '<div class="res-comments-section">' + renderComments(normalizedPostId) + renderCommentComposer(normalizedPostId) + '</div>'
            : '<div class="res-comments-section" style="display:none;"></div>') +
        '</div>';
}

function bindImageFallbacks(root) {
    const container = root || feed;

    container.querySelectorAll('.res-user-img').forEach(function (img) {
        img.addEventListener('error', function handleAvatarError() {
            img.removeEventListener('error', handleAvatarError);
            img.src = DEFAULT_AVATAR;
        });
    });

    container.querySelectorAll('.res-post-image').forEach(function (img) {
        img.addEventListener('error', function handlePostImageError() {
            img.removeEventListener('error', handlePostImageError);
            const card = img.closest('[data-post-id]');
            const postId = card ? card.dataset.postId : null;
            const post = posts.find(function (item) {
                return String(item.id) === String(postId);
            });
            if (post) {
                const safeFallback = CATEGORY_FALLBACK_IMAGES[String(post.category || '').toLowerCase()] || CATEGORY_FALLBACK_IMAGES.default;
                img.src = safeFallback;
            } else {
                img.src = CATEGORY_FALLBACK_IMAGES.default;
            }
        });
    });
}

function renderPosts() {
    if (!posts.length) {
        feed.innerHTML = '<div class="res-post-card"><p class="res-post-description">No approved community posts are available yet.</p></div>';
        return;
    }

    feed.innerHTML = posts.map(renderPostCard).join('');
    bindImageFallbacks();
    focusRequestedPost();
}

async function fetchPostDetail(postId) {
    const normalizedPostId = normalizePostId(postId);
    const query = currentUser && currentUser.id
        ? '?userId=' + encodeURIComponent(currentUser.id)
        : '';
    return HeritageApi.request('/api/zyl/display/' + normalizedPostId + query);
}

function patchPostMedia(post) {
    const normalizedPostId = normalizePostId(post.id);
    const mediaContainer = feed.querySelector('.res-post-media[data-post-id="' + normalizedPostId + '"]');
    if (!mediaContainer) {
        return;
    }

    mediaContainer.innerHTML = renderPostMedia(post);
    bindImageFallbacks(mediaContainer);
}

async function hydratePostMedia(postId) {
    const normalizedPostId = normalizePostId(postId);
    const targetPost = posts.find(function (post) {
        return normalizePostId(post.id) === normalizedPostId;
    });
    if (!targetPost || !targetPost.mediaSummaryOnly || hydratingMediaPosts.has(normalizedPostId)) {
        return;
    }

    hydratingMediaPosts.add(normalizedPostId);
    try {
        const detailedPost = await fetchPostDetail(normalizedPostId);
        updatePostInFeed(detailedPost);
        const updatedPost = posts.find(function (post) {
            return normalizePostId(post.id) === normalizedPostId;
        });
        if (updatedPost) {
            patchPostMedia(updatedPost);
        }
    } catch (error) {
        console.warn('Failed to hydrate community post media', error);
    } finally {
        hydratingMediaPosts.delete(normalizedPostId);
    }
}

async function hydrateSummarizedPosts() {
    const queue = posts
        .filter(function (post) { return Boolean(post.mediaSummaryOnly); })
        .map(function (post) { return normalizePostId(post.id); });

    if (!queue.length) {
        return;
    }

    let nextIndex = 0;

    async function worker() {
        while (nextIndex < queue.length) {
            const postId = queue[nextIndex];
            nextIndex += 1;
            await hydratePostMedia(postId);
        }
    }

    const workerCount = Math.min(2, queue.length);
    await Promise.all(Array.from({ length: workerCount }, function () {
        return worker();
    }));
}

async function loadFeed() {
    const query = currentUser && currentUser.id
        ? '?userId=' + encodeURIComponent(currentUser.id)
        : '';
    posts = await HeritageApi.request('/api/zyl/display/all' + query);
    renderPosts();
    window.setTimeout(function () {
        hydrateSummarizedPosts().catch(function () {
        });
    }, 0);
}

async function ensurePostDetail(postId) {
    const normalizedPostId = normalizePostId(postId);
    if (postDetails[normalizedPostId] || loadingDetails.has(normalizedPostId)) {
        return;
    }

    loadingDetails.add(normalizedPostId);
    renderPosts();

    try {
        const comments = await HeritageApi.request('/api/zyl/display/' + normalizedPostId + '/comments');
        postDetails[normalizedPostId] = { comments: comments };
        posts = posts.map(function (post) {
            if (normalizePostId(post.id) === normalizedPostId) {
                return Object.assign({}, post, {
                    commentCount: countComments(postDetails[normalizedPostId].comments)
                });
            }
            return post;
        });
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    } finally {
        loadingDetails.delete(normalizedPostId);
        renderPosts();
    }
}

async function submitComment(postId) {
    const normalizedPostId = normalizePostId(postId);

    if (!currentUser || !currentUser.id) {
        showToast('Please log in before sending a comment.');
        return;
    }

    const input = feed.querySelector('.res-comment-input[data-post-id="' + normalizedPostId + '"]');
    if (!input) {
        return;
    }

    const content = input.value.trim();
    if (!content) {
        showToast('Please enter a comment first.');
        input.focus();
        return;
    }

    const sendButton = feed.querySelector('[data-action="send-comment"][data-post-id="' + normalizedPostId + '"]');
    if (sendButton) {
        sendButton.disabled = true;
        sendButton.textContent = 'Sending...';
    }

    try {
        const replyTarget = getReplyTarget(normalizedPostId);
        const updatedComments = await HeritageApi.request('/api/zyl/display/' + normalizedPostId + '/comments', {
            method: 'POST',
            body: {
                userId: currentUser.id,
                parentMessageId: replyTarget ? Number(replyTarget.id) : null,
                content: content
            }
        });
        postDetails[normalizedPostId] = { comments: updatedComments };
        delete commentDrafts[normalizedPostId];
        delete replyTargets[normalizedPostId];
        posts = posts.map(function (post) {
            if (normalizePostId(post.id) === normalizedPostId) {
                return Object.assign({}, post, {
                    commentCount: countComments(updatedComments)
                });
            }
            return post;
        });
        expandedComments.add(normalizedPostId);
        renderPosts();
        showToast('Comment sent successfully.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
        if (sendButton) {
            sendButton.disabled = false;
            sendButton.textContent = 'Send';
        }
    }
}

async function deletePost(postId) {
    const normalizedPostId = normalizePostId(postId);
    const post = posts.find(function (item) {
        return normalizePostId(item.id) === normalizedPostId;
    });

    if (!post || !isPostOwnedByCurrentUser(post)) {
        showToast('Only the post author can delete this post.');
        return;
    }

    if (!window.confirm('Delete this post?')) {
        return;
    }

    try {
        await HeritageApi.request('/api/zyl/display/' + normalizedPostId + '?userId=' + encodeURIComponent(currentUser.id), {
            method: 'DELETE'
        });
        posts = posts.filter(function (item) {
            return normalizePostId(item.id) !== normalizedPostId;
        });
        clearPostInteractionState(normalizedPostId);
        renderPosts();
        showToast('Post deleted successfully.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    }
}

function fallbackCopyText(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', 'readonly');
    textarea.style.position = 'fixed';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
}

async function sharePost(postId) {
    const normalizedPostId = normalizePostId(postId);
    const url = new URL(window.location.href);
    url.searchParams.set('heritageId', normalizedPostId);
    url.hash = 'post-' + normalizedPostId;

    try {
        if (navigator.clipboard && window.isSecureContext) {
            await navigator.clipboard.writeText(url.toString());
        } else {
            fallbackCopyText(url.toString());
        }
        await incrementShareCount(normalizedPostId);
        showToast('Link copied to your clipboard.');
    } catch (error) {
        showToast(HeritageApi.getErrorMessage(error));
    }
}

function focusRequestedPost() {
    if (!highlightedHeritageId) {
        return;
    }

    const target = document.getElementById('post-' + highlightedHeritageId);
    if (!target) {
        return;
    }

    target.scrollIntoView({ behavior: 'smooth', block: 'center' });
    highlightedHeritageId = null;
}

function handleResourceFeedClick(event) {
    const actionTarget = event.target.closest('[data-action]');
    if (!actionTarget) {
        return;
    }

    const normalizedPostId = normalizePostId(actionTarget.dataset.postId);
    if (!normalizedPostId) {
        return;
    }

    const action = actionTarget.dataset.action;
    if (action === 'like') {
        toggleLike(normalizedPostId);
        return;
    }

    if (action === 'delete-post') {
        deletePost(normalizedPostId);
        return;
    }

    if (action === 'toggle-comment') {
        if (expandedComments.has(normalizedPostId)) {
            expandedComments.delete(normalizedPostId);
            renderPosts();
        } else {
            expandedComments.add(normalizedPostId);
            renderPosts();
            ensurePostDetail(normalizedPostId);
            focusCommentInput(normalizedPostId);
        }
        return;
    }

    if (action === 'send-comment') {
        submitComment(normalizedPostId);
        return;
    }

    if (action === 'select-reply') {
        const detail = postDetails[normalizedPostId];
        const selectedComment = detail ? findCommentById(detail.comments, actionTarget.dataset.commentId) : null;
        if (!selectedComment) {
            return;
        }
        replyTargets[normalizedPostId] = {
            id: selectedComment.id,
            username: selectedComment.username
        };
        renderPosts();
        focusCommentInput(normalizedPostId);
        return;
    }

    if (action === 'clear-reply') {
        delete replyTargets[normalizedPostId];
        renderPosts();
        focusCommentInput(normalizedPostId);
        return;
    }

    if (action === 'share') {
        sharePost(normalizedPostId);
    }
}

function handleResourceFeedKeydown(event) {
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
        const input = event.target.closest('.res-comment-input');
        if (!input) {
            return;
        }
        event.preventDefault();
        submitComment(normalizePostId(input.dataset.postId));
    }
}

function handleResourceFeedInput(event) {
    const input = event.target.closest('.res-comment-input');
    if (!input) {
        return;
    }

    commentDrafts[normalizePostId(input.dataset.postId)] = input.value;
}

window.addEventListener('DOMContentLoaded', async function () {
    currentUser = HeritageSession.initPageSession();
    highlightedHeritageId = new URLSearchParams(window.location.search).get('heritageId');

    feed.addEventListener('click', handleResourceFeedClick);
    feed.addEventListener('keydown', handleResourceFeedKeydown);
    feed.addEventListener('input', handleResourceFeedInput);

    try {
        await loadFeed();
    } catch (error) {
        feed.innerHTML = '<div class="res-post-card"><p class="res-post-description">' + escapeHtml(HeritageApi.getErrorMessage(error)) + '</p></div>';
    }
});
