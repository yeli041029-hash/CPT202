function toggleMode(mode) {
    const loginView = document.getElementById('loginView');
    const registerView = document.getElementById('registerView');

    if (mode === 'register') {
        loginView.classList.add('hidden');
        loginView.classList.remove('fade-in');
        registerView.classList.remove('hidden');
        registerView.classList.add('fade-in');
    } else {
        registerView.classList.add('hidden');
        registerView.classList.remove('fade-in');
        loginView.classList.remove('hidden');
        loginView.classList.add('fade-in');
    }
}

window.addEventListener('load', () => {
    const intro = document.getElementById('intro-overlay');
    const card = document.getElementById('mainCard');
    const body = document.body;

    setTimeout(() => {
        body.style.backgroundColor = '#fdf8ec';
        intro.style.opacity = '0';
        card.classList.add('active');

        setTimeout(() => {
            intro.style.display = 'none';
        }, 1200);
    }, 3800);
});

const loginButton = document.getElementById('loginBtn');
const registerButton = document.getElementById('registerBtn');
const registerInlineMessage = document.getElementById('registerInlineMessage');
const PROFILE_STORAGE_KEY = 'community-heritage-profile';

function getProfileStorageKey(user, fallbackProfile) {
    const userId = user && user.id;
    if (userId != null && userId !== '') {
        return `${PROFILE_STORAGE_KEY}:${userId}`;
    }

    const fallbackKey = fallbackProfile && fallbackProfile.username
        ? String(fallbackProfile.username).trim().toLowerCase()
        : 'guest';
    return `${PROFILE_STORAGE_KEY}:${fallbackKey}`;
}

function setButtonState(button, loadingText, isLoading) {
    if (!button) {
        return;
    }

    if (!button.dataset.defaultText) {
        button.dataset.defaultText = button.textContent;
    }

    button.disabled = isLoading;
    button.textContent = isLoading ? loadingText : button.dataset.defaultText;
}

function normalizeRegisterPayload(username, email, phone, password, registerAsAdmin) {
    return {
        username: username.trim(),
        email: email.trim(),
        phone: phone.trim(),
        password: password,
        role: registerAsAdmin ? 'ADMIN' : 'USER'
    };
}

function showRegisterMessage(message) {
    if (!registerInlineMessage) {
        return;
    }

    registerInlineMessage.textContent = message || '';
    registerInlineMessage.classList.toggle('is-visible', Boolean(message));
}

function mapRoleToIdentityLabel(role) {
    switch (role) {
        case 'ADMIN':
            return 'Admin';
        case 'CONTRIBUTOR':
            return 'Contributor';
        case 'USER':
        default:
            return 'Normal User';
    }
}

function syncProfileStorageFromUser(user, fallbackProfile) {
    if (!user) {
        return;
    }

    const scopedStorageKey = getProfileStorageKey(user, fallbackProfile);
    const profile = {
        username: user.username || (fallbackProfile && fallbackProfile.username) || '',
        email: user.email || '',
        phone: user.phone || '',
        identity: mapRoleToIdentityLabel(user.role),
        avatar: user.avatarUrl || '',
        bio: user.bio || ''
    };

    localStorage.setItem(scopedStorageKey, JSON.stringify(profile));
    localStorage.removeItem(PROFILE_STORAGE_KEY);
}

async function handleLoginClick(event) {
    if (event) {
        event.preventDefault();
    }

    const identityInput = document.getElementById('loginIdentity');
    const passwordInput = document.getElementById('loginPassword');
    const identifier = identityInput ? identityInput.value.trim() : '';
    const password = passwordInput ? passwordInput.value : '';

    if (!identifier || !password) {
        window.alert('Please enter your identity and password.');
        return;
    }

    setButtonState(loginButton, 'Authenticating...', true);
        try {
            const user = await HeritageApi.request('/api/zyl/auth/login', {
                method: 'POST',
                body: {
                    username: identifier,
                    password: password
                }
            });
            syncProfileStorageFromUser(user);
            HeritageSession.saveCurrentUser(user);
            window.location.href = 'home.html';
        } catch (error) {
            window.alert(HeritageApi.getErrorMessage(error));
    } finally {
        setButtonState(loginButton, 'Authenticating...', false);
    }
}

async function handleRegisterClick(event) {
    if (event) {
        event.preventDefault();
    }

    const usernameInput = document.getElementById('registerUsername');
    const emailInput = document.getElementById('registerEmail');
    const phoneInput = document.getElementById('registerPhone');
    const passwordInput = document.getElementById('registerPassword');
    const confirmPasswordInput = document.getElementById('registerConfirmPassword');
    const adminCheckbox = document.getElementById('registerAsAdmin');

    const username = usernameInput ? usernameInput.value.trim() : '';
    const email = emailInput ? emailInput.value.trim() : '';
    const phone = phoneInput ? phoneInput.value.trim() : '';
    const password = passwordInput ? passwordInput.value : '';
    const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : '';
    const registerAsAdmin = Boolean(adminCheckbox && adminCheckbox.checked);

    showRegisterMessage('');

    if (!username || !email || !phone || !password || !confirmPassword) {
        showRegisterMessage('Please complete all registration fields.');
        return;
    }

    if (password !== confirmPassword) {
        showRegisterMessage('The two passwords do not match.');
        return;
    }

    showRegisterMessage('Creating account...');
    setButtonState(registerButton, 'Creating...', true);
    try {
        await HeritageApi.request('/api/zyl/auth/register', {
            method: 'POST',
            body: normalizeRegisterPayload(username, email, phone, password, registerAsAdmin)
        });

        if (usernameInput) {
            usernameInput.value = '';
        }
        if (emailInput) {
            emailInput.value = '';
        }
        if (phoneInput) {
            phoneInput.value = '';
        }
        if (passwordInput) {
            passwordInput.value = '';
        }
        if (confirmPasswordInput) {
            confirmPasswordInput.value = '';
        }
        if (adminCheckbox) {
            adminCheckbox.checked = false;
        }

        const loginIdentityInput = document.getElementById('loginIdentity');
        const loginPasswordInput = document.getElementById('loginPassword');
        if (loginIdentityInput) {
            loginIdentityInput.value = username;
        }
        if (loginPasswordInput) {
            loginPasswordInput.value = '';
        }

        toggleMode('login');
        window.alert('Account created successfully. Please sign in.');
    } catch (error) {
        showRegisterMessage(HeritageApi.getErrorMessage(error));
    } finally {
        setButtonState(registerButton, 'Creating...', false);
    }
}

window.handleLoginClick = handleLoginClick;
window.handleRegisterClick = handleRegisterClick;
