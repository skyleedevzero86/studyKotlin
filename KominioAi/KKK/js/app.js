// 메인 애플리케이션 JavaScript

// 전역 변수
let currentSection = 'home';
let questionCounter = 0;

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

// 앱 초기화
function initializeApp() {
    setupEventListeners();
    loadInitialData();
    updateUI();
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 네비게이션 링크
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetSection = this.getAttribute('href').substring(1);
            showSection(targetSection);
        });
    });

    // 모바일 메뉴 토글
    document.querySelector('.hamburger').addEventListener('click', toggleMobileMenu);

    // 모달 외부 클릭 시 닫기
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal(this.id);
            }
        });
    });

    // 폼 제출 이벤트
    setupFormHandlers();
}

// 폼 핸들러 설정
function setupFormHandlers() {
    // 로그인 폼
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // 회원가입 폼
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // 프로필 폼
    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileUpdate);
    }

    // 설문 생성 폼
    const createSurveyForm = document.getElementById('create-survey-form');
    if (createSurveyForm) {
        createSurveyForm.addEventListener('submit', handleCreateSurvey);
    }

    // 퀴즈 생성 폼
    const createQuizForm = document.getElementById('create-quiz-form');
    if (createQuizForm) {
        createQuizForm.addEventListener('submit', handleCreateQuiz);
    }

    // 비밀번호 강도 체크
    const passwordInput = document.getElementById('register-password');
    if (passwordInput) {
        passwordInput.addEventListener('input', checkPasswordStrength);
    }
}

// 초기 데이터 로드
function loadInitialData() {
    // 사용자 상태 확인
    if (typeof loadUserFromStorage === 'function') {
        loadUserFromStorage();
    }
    
    // 통계 업데이트
    if (typeof updateStatistics === 'function') {
        updateStatistics();
    }
}

// UI 업데이트
function updateUI() {
    // 네비게이션 상태 업데이트
    updateNavigation();
    
    // 사용자 상태에 따른 UI 업데이트
    if (typeof updateUIForLoggedInUser === 'function') {
        updateUIForLoggedInUser();
    }
}

// 네비게이션 업데이트
function updateNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === `#${currentSection}`) {
            link.classList.add('active');
        }
    });
}

// 섹션 표시
function showSection(sectionId) {
    // 현재 섹션 숨기기
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // 새 섹션 표시
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
        currentSection = sectionId;
        updateNavigation();
    }

    // 모바일 메뉴 닫기
    closeMobileMenu();
}

// 모바일 메뉴 토글
function toggleMobileMenu() {
    const navMenu = document.getElementById('nav-menu');
    navMenu.classList.toggle('active');
}

// 모바일 메뉴 닫기
function closeMobileMenu() {
    const navMenu = document.getElementById('nav-menu');
    navMenu.classList.remove('active');
}

// 모달 표시
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

// 모달 닫기
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // 퀴즈 모달이 닫힐 때 타이머 정리
        if (modalId === 'quiz-participation-modal' && typeof clearQuizTimer === 'function') {
            clearQuizTimer();
        }
    }
}

// 로그인 모달 표시
function showLoginModal() {
    showModal('login-modal');
}

// 회원가입 모달 표시
function showRegisterModal() {
    showModal('register-modal');
}

// 설문 생성 모달 표시
function showCreateSurveyModal() {
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
    }
    showModal('create-survey-modal');
}

// 퀴즈 생성 모달 표시
function showCreateQuizModal() {
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
    }
    showModal('create-quiz-modal');
}

// 프로필 모달 표시 - data.js의 함수 사용
function showProfileModal() {
    if (typeof window.showProfileModal === 'function' && window.showProfileModal !== showProfileModal) {
        window.showProfileModal();
    } else {
        showToast('프로필 기능을 로드하는 중...', 'info');
    }
}

// 비밀번호 찾기 모달 표시
function showForgotPasswordModal() {
    showToast('비밀번호 찾기 기능은 준비 중입니다.', 'info');
}

// 비밀번호 변경 모달 표시
function showChangePasswordModal() {
    showToast('비밀번호 변경 기능은 준비 중입니다.', 'info');
}

// 로그인 처리
function handleLogin(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const emailOrUsername = formData.get('emailOrUsername');
    const password = formData.get('password');
    const rememberMe = formData.get('rememberMe');

    // 입력값 검증
    if (!emailOrUsername || !password) {
        showToast('이메일/사용자명과 비밀번호를 입력해주세요.', 'warning');
        return;
    }

    // 로딩 표시
    showLoading();

    // 로그인 처리 (더미 데이터 사용)
    setTimeout(() => {
        hideLoading();
        
        // 사용자 찾기 (이메일 또는 사용자명으로)
        const user = dummyData.users.find(u => 
            u.email === emailOrUsername || u.username === emailOrUsername
        );

        if (user && password === 'password') { // 더미 비밀번호
            // 로그인 성공
            if (typeof saveUserToStorage === 'function') {
                saveUserToStorage(user);
            }
            if (typeof updateUIForLoggedInUser === 'function') {
                updateUIForLoggedInUser();
            }
            
            closeModal('login-modal');
            showToast(`환영합니다, ${user.firstName || user.username}님!`, 'success');
            event.target.reset();
            
            // 로그인 후 설문조사 섹션으로 이동
            showSection('surveys');
    } else {
            showToast('이메일/사용자명 또는 비밀번호가 올바르지 않습니다.', 'error');
        }
    }, 1500);
}

// 회원가입 처리
function handleRegister(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const email = formData.get('email');
    const username = formData.get('username');
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    const firstName = formData.get('firstName');
    const lastName = formData.get('lastName');
    const phone = formData.get('phone');
    const agreeToTerms = formData.get('agreeToTerms');

    // 유효성 검사
    if (password !== confirmPassword) {
        showToast('비밀번호가 일치하지 않습니다.', 'error');
        return;
    }
    
    if (!agreeToTerms) {
        showToast('이용약관에 동의해주세요.', 'error');
        return;
    }

    // 로딩 표시
    showLoading();

    // 더미 회원가입 처리
    setTimeout(() => {
        hideLoading();
        
        // 새 사용자 생성
        const newUser = {
            id: 'user-' + Date.now(),
            email: email,
            username: username,
            firstName: firstName,
            lastName: lastName,
            phone: phone,
            profileImageUrl: null,
            emailVerified: false,
            twoFactorEnabled: false,
            accountStatus: 'ACTIVE',
            roles: ['USER'],
            createdAt: new Date().toISOString(),
            lastLoginAt: null
        };

        // 사용자 저장
        if (typeof saveUserToStorage === 'function') {
            saveUserToStorage(newUser);
        }
        if (typeof updateUIForLoggedInUser === 'function') {
            updateUIForLoggedInUser();
        }

        closeModal('register-modal');
        showToast('회원가입이 완료되었습니다!', 'success');
        event.target.reset();
    }, 2000);
}

// 프로필 업데이트 처리
function handleProfileUpdate(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const firstName = formData.get('firstName');
    const lastName = formData.get('lastName');
    const phone = formData.get('phone');

    // 로딩 표시
    showLoading();

    // 더미 프로필 업데이트
    setTimeout(() => {
        hideLoading();
        
        // 현재 사용자 정보 업데이트
        if (currentUser) {
            currentUser.firstName = firstName;
            currentUser.lastName = lastName;
            currentUser.phone = phone;
            
            if (typeof saveUserToStorage === 'function') {
                saveUserToStorage(currentUser);
            }
        }

        closeModal('profile-modal');
        showToast('프로필이 업데이트되었습니다!', 'success');
    }, 1000);
}

// 설문 생성 처리
function handleCreateSurvey(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const title = formData.get('title');
    const surveyType = formData.get('surveyType');
    const participantType = formData.get('participantType');
    const startDate = formData.get('startDate');
    const endDate = formData.get('endDate');
    const timeLimit = formData.get('timeLimit');

    // 로딩 표시
    showLoading();

    // 더미 설문 생성
    setTimeout(() => {
        hideLoading();
        
        // 리워드 설정 수집
        const participantRewardEnabled = formData.get('participantRewardEnabled') === 'on';
        const participantRewardType = formData.get('participantRewardType') || 'NONE';
        const participantRewardValue = parseInt(formData.get('participantRewardValue')) || 0;
        const participantRewardDescription = formData.get('participantRewardDescription') || '';
        const participantRewardProbability = parseInt(formData.get('participantRewardProbability')) || 50;
        
        const newSurvey = {
            id: 'survey-' + Date.now(),
            title: title,
            description: '새로 생성된 설문입니다.',
            author: currentUser ? currentUser.firstName + ' ' + currentUser.lastName : '사용자',
            status: 'DRAFT',
            surveyType: surveyType,
            participantType: participantType,
            startDate: startDate || null,
            endDate: endDate || null,
            timeLimit: timeLimit ? parseInt(timeLimit) : null,
            participationCount: 0,
            targetCount: 100,
            createdAt: new Date().toISOString(),
            // 참여자 리워드 설정
            participantReward: {
                enabled: participantRewardEnabled,
                type: participantRewardType,
                value: participantRewardValue,
                description: participantRewardDescription,
                probability: participantRewardProbability / 100, // 백분율을 소수로 변환
                image: getRewardImage(participantRewardType)
            },
            questions: []
        };

        // 설문 목록에 추가
        dummyData.surveys.unshift(newSurvey);
        
        // UI 업데이트
        if (typeof loadSurveys === 'function') {
            loadSurveys();
        }

        closeModal('create-survey-modal');
        showToast('설문이 생성되었습니다!', 'success');
        showSection('surveys');
    }, 2000);
}

// 퀴즈 생성 처리
function handleCreateQuiz(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const title = formData.get('title');
    const quizType = formData.get('quizType');
    const difficulty = formData.get('difficulty');
    const startDate = formData.get('startDate');
    const endDate = formData.get('endDate');
    const targetCount = parseInt(formData.get('targetCount'));
    const timeLimit = formData.get('timeLimit');

    // 로딩 표시
    showLoading();

    // 더미 퀴즈 생성
    setTimeout(() => {
        hideLoading();
        
        // 리워드 설정 수집
        const participantRewardEnabled = formData.get('participantRewardEnabled') === 'on';
        const participantRewardType = formData.get('participantRewardType') || 'NONE';
        const participantRewardValue = parseInt(formData.get('participantRewardValue')) || 0;
        const participantRewardDescription = formData.get('participantRewardDescription') || '';
        const participantRewardProbability = parseInt(formData.get('participantRewardProbability')) || 50;
        
        const newQuiz = {
            id: 'quiz-' + Date.now(),
            title: title,
            description: '새로 생성된 퀴즈입니다.',
            author: currentUser ? currentUser.firstName + ' ' + currentUser.lastName : '사용자',
            status: 'DRAFT',
            quizType: quizType,
            difficulty: difficulty,
            startDate: startDate || null,
            endDate: endDate || null,
            timeLimit: timeLimit ? parseInt(timeLimit) : null,
            participationCount: 0,
            targetCount: targetCount,
            createdAt: new Date().toISOString(),
            // 참여자 리워드 설정
            participantReward: {
                enabled: participantRewardEnabled,
                type: participantRewardType,
                value: participantRewardValue,
                description: participantRewardDescription,
                probability: participantRewardProbability / 100, // 백분율을 소수로 변환
                image: getRewardImage(participantRewardType)
            },
            questions: []
        };

        // 퀴즈 목록에 추가
        dummyData.quizzes.unshift(newQuiz);
        
        // UI 업데이트
        if (typeof loadQuizzes === 'function') {
            loadQuizzes();
        }

        closeModal('create-quiz-modal');
        showToast('퀴즈가 생성되었습니다!', 'success');
        showSection('quizzes');
    }, 2000);
}

// 소셜 로그인
function socialLogin(provider) {
    showToast(`${provider} 로그인 기능은 준비 중입니다.`, 'info');
}

// 로그아웃
function logout() {
    if (typeof clearUserFromStorage === 'function') {
        clearUserFromStorage();
    }
    if (typeof updateUIForLoggedInUser === 'function') {
        updateUIForLoggedInUser();
    }
    
    showToast('로그아웃되었습니다.', 'info');
    showSection('home');
}

// 사용자 드롭다운 토글
function toggleUserDropdown() {
    const dropdown = document.getElementById('user-dropdown-menu');
    dropdown.classList.toggle('show');
}

// 질문 추가
function addQuestion() {
    questionCounter++;
    const questionsContainer = document.getElementById('questions-container');
    
    const questionDiv = document.createElement('div');
    questionDiv.className = 'question-item';
    questionDiv.innerHTML = `
        <div class="question-header">
            <span class="question-number">질문 ${questionCounter}</span>
            <button type="button" class="remove-question" onclick="removeQuestion(this)">삭제</button>
            </div>
        <div class="form-group">
            <label>질문 내용</label>
            <input type="text" name="question-content-${questionCounter}" placeholder="질문을 입력하세요" required>
        </div>
        <div class="form-group">
            <label>질문 유형</label>
            <select name="question-type-${questionCounter}" onchange="toggleQuestionOptions(this)">
                <option value="MULTIPLE_CHOICE">객관식</option>
                <option value="ESSAY">서술형</option>
                <option value="SHORT_ANSWER">단답형</option>
                    </select>
                </div>
        <div class="question-options" id="options-${questionCounter}">
            <div class="option-item">
                <input type="text" name="option-${questionCounter}-1" placeholder="선택지 1" required>
                <button type="button" class="remove-option" onclick="removeOption(this)">삭제</button>
                </div>
            <div class="option-item">
                <input type="text" name="option-${questionCounter}-2" placeholder="선택지 2" required>
                <button type="button" class="remove-option" onclick="removeOption(this)">삭제</button>
            </div>
            <button type="button" class="add-option" onclick="addOption(${questionCounter})">선택지 추가</button>
            </div>
        `;
    
    questionsContainer.appendChild(questionDiv);
}

// 질문 삭제
function removeQuestion(button) {
    button.closest('.question-item').remove();
}

// 질문 옵션 토글
function toggleQuestionOptions(select) {
    const questionItem = select.closest('.question-item');
    const optionsDiv = questionItem.querySelector('.question-options');
    
    if (select.value === 'MULTIPLE_CHOICE') {
        optionsDiv.style.display = 'block';
    } else {
        optionsDiv.style.display = 'none';
    }
}

// 선택지 추가
function addOption(questionNumber) {
    const optionsContainer = document.getElementById(`options-${questionNumber}`);
    const optionCount = optionsContainer.querySelectorAll('.option-item').length + 1;
    
    const optionDiv = document.createElement('div');
    optionDiv.className = 'option-item';
    optionDiv.innerHTML = `
        <input type="text" name="option-${questionNumber}-${optionCount}" placeholder="선택지 ${optionCount}" required>
        <button type="button" class="remove-option" onclick="removeOption(this)">삭제</button>
    `;
    
    optionsContainer.insertBefore(optionDiv, optionsContainer.querySelector('.add-option'));
}

// 선택지 삭제
function removeOption(button) {
    button.closest('.option-item').remove();
}

// 참여자 리워드 설정 토글
function toggleParticipantReward() {
    const checkbox = document.getElementById('participant-reward-enabled');
    const settings = document.getElementById('participant-reward-settings');
    
    if (checkbox.checked) {
        settings.style.display = 'block';
    } else {
        settings.style.display = 'none';
    }
}

// 퀴즈 참여자 리워드 설정 토글
function toggleQuizParticipantReward() {
    const checkbox = document.getElementById('quiz-participant-reward-enabled');
    const settings = document.getElementById('quiz-participant-reward-settings');
    
    if (checkbox.checked) {
        settings.style.display = 'block';
    } else {
        settings.style.display = 'none';
    }
}

// 리워드 타입에 따른 이미지 반환
function getRewardImage(rewardType) {
    const imageMap = {
        'GIFTCARD': 'https://via.placeholder.com/100x100/4CAF50/white?text=🎁',
        'POINTS': 'https://via.placeholder.com/100x100/2196F3/white?text=💰',
        'COUPON': 'https://via.placeholder.com/100x100/FF9800/white?text=🎫',
        'CASH': 'https://via.placeholder.com/100x100/FFD700/white?text=💵',
        'DIGITAL': 'https://via.placeholder.com/100x100/4285F4/white?text=📱',
        'PHYSICAL': 'https://via.placeholder.com/100x100/9C27B0/white?text=📦'
    };
    return imageMap[rewardType] || 'https://via.placeholder.com/100x100/666/white?text=🎁';
}

// 비밀번호 강도 체크
function checkPasswordStrength() {
    const password = document.getElementById('register-password').value;
    const strengthDiv = document.getElementById('password-strength');
    
    if (!password) {
        strengthDiv.className = 'password-strength';
        return;
    }
    
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    
    strengthDiv.className = 'password-strength';
    if (strength <= 2) strengthDiv.classList.add('weak');
    else if (strength === 3) strengthDiv.classList.add('fair');
    else if (strength === 4) strengthDiv.classList.add('good');
    else if (strength >= 5) strengthDiv.classList.add('strong');
}

// 토스트 알림 표시
function showToast(message, type = 'info', duration = 5000) {
    const toastContainer = document.getElementById('toast-container');
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const iconMap = {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-circle',
        'warning': 'fas fa-exclamation-triangle',
        'info': 'fas fa-info-circle'
    };
    
    toast.innerHTML = `
        <div class="toast-icon">
            <i class="${iconMap[type]}"></i>
            </div>
        <div class="toast-content">
            <div class="toast-message">${message}</div>
            </div>
        <button class="toast-close" onclick="removeToast(this)">&times;</button>
    `;
    
    toastContainer.appendChild(toast);
    
    // 자동 제거
    setTimeout(() => {
        if (toast.parentNode) {
            removeToast(toast.querySelector('.toast-close'));
        }
    }, duration);
}

// 토스트 제거
function removeToast(button) {
    const toast = button.closest('.toast');
    if (toast) {
        toast.remove();
    }
}

// 로딩 표시
function showLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'flex';
    }
}

// 로딩 숨기기
function hideLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

// 관리자 기능들은 data.js에서 직접 구현됨
// app.js에서는 별도의 함수 정의 없이 data.js의 함수들을 직접 호출

function showMySurveys() {
    showToast('내 설문 기능은 준비 중입니다.', 'info');
}

function showSettings() {
    showToast('설정 기능은 준비 중입니다.', 'info');
}

// 키보드 이벤트 처리
document.addEventListener('keydown', function(e) {
    // ESC 키로 모달 닫기
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.show').forEach(modal => {
            closeModal(modal.id);
        });
    }
});

// 페이지 스크롤 시 네비게이션 효과
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar');
    if (window.scrollY > 100) {
        navbar.style.background = 'rgba(102, 126, 234, 0.95)';
        navbar.style.backdropFilter = 'blur(10px)';
    } else {
        navbar.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
        navbar.style.backdropFilter = 'none';
    }
});
