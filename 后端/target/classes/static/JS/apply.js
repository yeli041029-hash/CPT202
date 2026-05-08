document.addEventListener('DOMContentLoaded', () => {
    const user = HeritageSession.initPageSession({
        requireLogin: true,
        requiredRole: 'USER',
        redirectTo: 'welcome.html'
    });
    const form = document.getElementById('applicationForm');
    const toast = document.getElementById('toast');
    const submitButton = form ? form.querySelector('button[type="submit"]') : null;

    if (!user || !form || !toast) {
        return;
    }

    const fullNameInput = document.getElementById('fullName');
    const domainInput = document.getElementById('domain');
    const portfolioInput = document.getElementById('portfolio');
    const reasonInput = document.getElementById('reason');
    const termsCheckbox = document.getElementById('terms');
    const termsWarning = document.getElementById('termsWarning');

    if (fullNameInput && user.username) {
        fullNameInput.value = user.username;
    }

    function showToast(message) {
        toast.textContent = message;
        toast.classList.add('show');
    }

    function hideToast() {
        toast.classList.remove('show');
    }

    function setSubmitting(isSubmitting) {
        if (!submitButton) {
            return;
        }
        submitButton.disabled = isSubmitting;
        submitButton.style.opacity = isSubmitting ? '0.7' : '';
        submitButton.style.cursor = isSubmitting ? 'wait' : '';
    }

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

    function updateAgreementState() {
        if (!submitButton || !termsCheckbox) {
            return;
        }
        if (termsCheckbox.checked) {
            submitButton.style.opacity = '';
            submitButton.style.filter = '';
            submitButton.style.cursor = '';
            if (termsWarning) {
                termsWarning.style.display = 'none';
            }
            return;
        }
        submitButton.style.opacity = '0.55';
        submitButton.style.filter = 'grayscale(0.35)';
        submitButton.style.cursor = 'not-allowed';
    }

    function ensureAgreementConfirmed() {
        if (!termsCheckbox || termsCheckbox.checked) {
            if (termsWarning) {
                termsWarning.style.display = 'none';
            }
            return true;
        }
        if (termsWarning) {
            termsWarning.style.display = 'block';
        }
        return false;
    }

    async function resolveCurrentUser() {
        const currentUser = HeritageSession.getCurrentUser();
        if (!currentUser) {
            return null;
        }

        const users = await HeritageApi.request('/api/zyl/users');
        const matchedUser = Array.isArray(users)
            ? users.find((item) => item.username === currentUser.username || (currentUser.email && item.email === currentUser.email))
            : null;

        if (!matchedUser) {
            return currentUser;
        }

        const mergedUser = Object.assign({}, currentUser, matchedUser);
        HeritageSession.saveCurrentUser(mergedUser);
        return mergedUser;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        if (!ensureAgreementConfirmed()) {
            return;
        }

        const payload = {
            userId: null,
            applicationReason: reasonInput ? reasonInput.value.trim() : '',
            applicantName: fullNameInput ? fullNameInput.value.trim() : '',
            domain: domainInput ? domainInput.value : '',
            portfolioUrl: portfolioInput ? portfolioInput.value.trim() : ''
        };

        if (!payload.applicantName || !payload.domain || !payload.applicationReason) {
            showToast('Please complete all required fields.');
            return;
        }

        setSubmitting(true);
        hideToast();

        try {
            const currentUser = await resolveCurrentUser();
            if (!currentUser || !currentUser.id) {
                throw new Error('Please log in again before submitting.');
            }
            payload.userId = currentUser.id;

            await HeritageApi.request('/api/ly-contributor/contributor-applications', {
                method: 'POST',
                body: payload
            });

            showToast('Your application has been sent to the reviewers.');
            setTimeout(() => {
                hideToast();
                window.location.href = 'home.html';
            }, 2200);
        } catch (error) {
            showToast(HeritageApi.getErrorMessage(error));
        } finally {
            setSubmitting(false);
        }
    });

    if (termsCheckbox) {
        termsCheckbox.addEventListener('change', updateAgreementState);
    }
    [fullNameInput, domainInput, portfolioInput, reasonInput, termsCheckbox].forEach(attachEnglishValidation);
    updateAgreementState();
});
