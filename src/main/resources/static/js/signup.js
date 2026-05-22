document.addEventListener('DOMContentLoaded', () => {

    // === 이메일 중복 확인 ===
    const emailInput    = document.getElementById('email');
    const checkBtn      = document.querySelector('[data-action="check-email"]');
    const emailFeedback = document.getElementById('emailFeedback');

    let emailChecked = false;

    checkBtn.addEventListener('click', async () => {
        const email = emailInput.value.trim();
        if (!email) {
            showFeedback(emailFeedback, '이메일을 입력해주세요.', 'error');
            return;
        }

        try {
            const res = await fetch(`/auth/check-email?email=${encodeURIComponent(email)}`);
            const available = await res.json();
            if (available) {
                showFeedback(emailFeedback, '사용 가능한 이메일입니다.', 'success');
                emailChecked = true;
            } else {
                showFeedback(emailFeedback, '이미 사용 중인 이메일입니다.', 'error');
                emailChecked = false;
            }
        } catch (e) {
            showFeedback(emailFeedback, '확인 중 오류가 발생했습니다.', 'error');
        }
    });

    // 이메일 수정하면 중복확인 다시 받게
    emailInput.addEventListener('input', () => {
        emailChecked = false;
        emailFeedback.textContent = '';
        emailFeedback.className = 'form-feedback';
    });

    // === 비밀번호 일치 확인 ===
    const passwordInput        = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const pwFeedback           = document.getElementById('passwordConfirmFeedback');

    const checkPasswordMatch = () => {
        const pw = passwordInput.value;
        const cf = passwordConfirmInput.value;
        if (!cf) {
            pwFeedback.textContent = '';
            pwFeedback.className = 'form-feedback';
            return;
        }
        if (pw === cf) {
            showFeedback(pwFeedback, '비밀번호가 일치합니다.', 'success');
        } else {
            showFeedback(pwFeedback, '비밀번호가 일치하지 않습니다.', 'error');
        }
    };

    passwordInput.addEventListener('input', checkPasswordMatch);
    passwordConfirmInput.addEventListener('input', checkPasswordMatch);

    // === 제출 전 최종 확인 ===
    document.querySelector('form.auth-card').addEventListener('submit', (e) => {
        if (!emailChecked) {
            e.preventDefault();
            showFeedback(emailFeedback, '이메일 중복 확인을 해주세요.', 'error');
            return;
        }
        if (passwordInput.value !== passwordConfirmInput.value) {
            e.preventDefault();
            showFeedback(pwFeedback, '비밀번호가 일치하지 않습니다.', 'error');
        }
    });

    function showFeedback(el, msg, type) {
        el.textContent = msg;
        el.className = `form-feedback form-feedback--${type}`;
    }
});