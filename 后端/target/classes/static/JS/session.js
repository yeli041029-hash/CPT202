(function (window) {
  var SESSION_KEY = 'heritage-platform-session-user';
  var ROLE_LABELS = {
    USER: 'Normal User',
    CONTRIBUTOR: 'Contributor',
    ADMIN: 'Admin'
  };
  var ROLE_PRIORITY = {
    USER: 0,
    CONTRIBUTOR: 1,
    ADMIN: 2
  };
  var refreshCurrentUserPromise = null;

  function getCurrentUser() {
    try {
      var raw = sessionStorage.getItem(SESSION_KEY);
      if (!raw) {
        var legacyRaw = localStorage.getItem(SESSION_KEY);
        if (legacyRaw) {
          sessionStorage.setItem(SESSION_KEY, legacyRaw);
          localStorage.removeItem(SESSION_KEY);
          raw = legacyRaw;
        }
      } else {
        localStorage.removeItem(SESSION_KEY);
      }
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }

  function saveCurrentUser(user) {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));
    localStorage.removeItem(SESSION_KEY);
    return user;
  }

  function clearCurrentUser() {
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem(SESSION_KEY);
  }

  function getRoleLabel(role) {
    return ROLE_LABELS[role] || role || 'Guest';
  }

  function getRolePriority(role) {
    return Object.prototype.hasOwnProperty.call(ROLE_PRIORITY, role) ? ROLE_PRIORITY[role] : -1;
  }

  function hasRoleAccess(user, requiredRole) {
    if (!requiredRole) {
      return true;
    }

    if (!user || !user.role) {
      return false;
    }

    return getRolePriority(user.role) >= getRolePriority(requiredRole);
  }

  function bindLogoutLinks() {
    document.querySelectorAll('.logout-link').forEach(function (link) {
      if (link.dataset.sessionBound === 'true') {
        return;
      }

      link.dataset.sessionBound = 'true';
      link.addEventListener('click', function () {
        clearCurrentUser();
      });
    });
  }

  function removeRoleSwitchItems() {
    document.querySelectorAll('.role-switch-entry').forEach(function (item) {
      item.remove();
    });
  }

  function syncMenuVisibility(user) {
    document.querySelectorAll('a[href="submission.html"]').forEach(function (link) {
      var item = link.closest('li') || link;
      item.style.display = hasRoleAccess(user, 'CONTRIBUTOR') ? '' : 'none';
    });

    document.querySelectorAll('a[href="contributor.html"]').forEach(function (link) {
      var item = link.closest('li') || link;
      item.style.display = hasRoleAccess(user, 'ADMIN') ? '' : 'none';
    });
  }

  function syncUserVisuals(user) {
    var navAvatar = document.querySelector('.user-menu-container .user-avatar');
    if (navAvatar && user && user.avatarUrl) {
      navAvatar.src = user.avatarUrl;
    }

    var usernameTarget = document.getElementById('sidebarUsername');
    if (usernameTarget && user) {
      usernameTarget.textContent = user.username || 'heritage_user';
    }

    var identityTarget = document.getElementById('sidebarIdentity');
    if (identityTarget && user) {
      identityTarget.textContent = getRoleLabel(user.role);
    }
  }

  function refreshSessionUi(user) {
    bindLogoutLinks();
    removeRoleSwitchItems();
    syncMenuVisibility(user);
    syncUserVisuals(user);
  }

  async function requestJson(path) {
    const response = await fetch(path, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(response.statusText || 'Request failed.');
    }

    return response.json();
  }

  async function refreshCurrentUserFromServer() {
    var currentUser = getCurrentUser();
    if (!currentUser || !currentUser.id) {
      return null;
    }

    if (refreshCurrentUserPromise) {
      return refreshCurrentUserPromise;
    }

    refreshCurrentUserPromise = requestJson('/api/zyl/profile/' + currentUser.id)
      .then(function (refreshedUser) {
        var latestCurrentUser = getCurrentUser() || currentUser;
        var mergedUser = Object.assign({}, latestCurrentUser, refreshedUser);
        saveCurrentUser(mergedUser);
        refreshSessionUi(mergedUser);
        window.dispatchEvent(new CustomEvent('heritage-session-refreshed', {
          detail: mergedUser
        }));
        return mergedUser;
      })
      .finally(function () {
        refreshCurrentUserPromise = null;
      });

    return refreshCurrentUserPromise;
  }

  function initPageSession(options) {
    var settings = Object.assign({
      requireLogin: false,
      requiredRole: null,
      redirectTo: 'welcome.html'
    }, options || {});

    var user = getCurrentUser();
    refreshSessionUi(user);

    if (settings.requireLogin && !user) {
      window.alert('Please log in first.');
      window.location.href = settings.redirectTo;
      return null;
    }

    if (settings.requiredRole && !hasRoleAccess(user, settings.requiredRole)) {
      window.alert('This page requires ' + getRoleLabel(settings.requiredRole) + ' access.');
      window.location.href = 'home.html';
      return null;
    }

    return user;
  }

  function autoInitSessionUi() {
    var currentUser = getCurrentUser();
    refreshSessionUi(currentUser);

    if (!currentUser || !currentUser.id) {
      return;
    }

    refreshCurrentUserFromServer().catch(function () {
      refreshSessionUi(currentUser);
    });
  }

  window.addEventListener('focus', function () {
    var currentUser = getCurrentUser();
    if (!currentUser || !currentUser.id) {
      return;
    }

    refreshCurrentUserFromServer().catch(function () {});
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', autoInitSessionUi);
  } else {
    autoInitSessionUi();
  }

  window.HeritageSession = {
    getCurrentUser: getCurrentUser,
    saveCurrentUser: saveCurrentUser,
    clearCurrentUser: clearCurrentUser,
    getRoleLabel: getRoleLabel,
    hasRoleAccess: hasRoleAccess,
    initPageSession: initPageSession,
    syncMenuVisibility: syncMenuVisibility,
    syncUserVisuals: syncUserVisuals,
    refreshCurrentUserFromServer: refreshCurrentUserFromServer
  };
})(window);
