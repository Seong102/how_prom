/* signup.js — 회원가입 페이지 클라이언트 검증 로직 */

// 이메일 중복 확인
(function () {
    const checkBtn = document.querySelector('[data-action="check-email"]');
    const emailInput = document.getElementById('email');
    const feedback = document.getElementById('emailFeedback');

    if (!checkBtn || !emailInput || !feedback) return;

    emailInput.addEventListener('input', function () {
        feedback.textContent = '';
        feedback.className = 'form-feedback';
    });

    checkBtn.addEventListener('click', async function () {
        const email = emailInput.value.trim();

        if (!email) {
            feedback.textContent = '이메일을 입력해주세요.';
            feedback.className = 'form-feedback form-feedback--error';
            return;
        }
        if (!emailInput.checkValidity()) {
            feedback.textContent = '올바른 이메일 형식이 아닙니다.';
            feedback.className = 'form-feedback form-feedback--error';
            return;
        }

        try {
            const res = await fetch('/auth/check-email?email=' + encodeURIComponent(email));
            if (!res.ok) throw new Error('network');
            const data = await res.json();

            if (data.available) {
                feedback.textContent = '사용 가능한 이메일입니다.';
                feedback.className = 'form-feedback form-feedback--success';
            } else {
                feedback.textContent = '이미 사용 중인 이메일입니다.';
                feedback.className = 'form-feedback form-feedback--error';
            }
        } catch (e) {
            feedback.textContent = '확인 중 오류가 발생했습니다.';
            feedback.className = 'form-feedback form-feedback--error';
        }
    });
})();

// 비밀번호 일치 확인
(function () {
    const pw = document.getElementById('password');
    const pwConfirm = document.getElementById('passwordConfirm');
    const feedback = document.getElementById('passwordConfirmFeedback');

    if (!pw || !pwConfirm || !feedback) return;

    function check() {
        if (!pwConfirm.value) {
            feedback.textContent = '';
            feedback.className = 'form-feedback';
            return;
        }
        if (pw.value === pwConfirm.value) {
            feedback.textContent = '비밀번호가 일치합니다.';
            feedback.className = 'form-feedback form-feedback--success';
        } else {
            feedback.textContent = '비밀번호가 일치하지 않습니다.';
            feedback.className = 'form-feedback form-feedback--error';
        }
    }

    pw.addEventListener('input', check);
    pwConfirm.addEventListener('input', check);
})();