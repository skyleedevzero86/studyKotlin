// 더미 데이터
const dummyData = {
    users: [
        {
            id: 'user-1',
            email: 'admin@kominioai.com',
            password: 'admin123',
            firstName: '관리자',
            lastName: '김',
            role: 'ADMIN',
            createdAt: '2024-01-01T00:00:00Z'
        },
        {
            id: 'user-2',
            email: 'user@kominioai.com',
            password: 'user123',
            firstName: '사용자',
            lastName: '이',
            role: 'USER',
            createdAt: '2024-01-02T00:00:00Z'
        },
        {
            id: 'user-3',
            email: 'test@kominioai.com',
            password: 'test123',
            firstName: '테스트',
            lastName: '박',
            role: 'USER',
            createdAt: '2024-01-03T00:00:00Z'
        },
        {
            id: 'user-4',
            email: 'member@kominioai.com',
            password: 'member123',
            firstName: '회원',
            lastName: '최',
            role: 'USER',
            createdAt: '2024-01-04T00:00:00Z'
        }
    ],
    posts: [
        {
            id: 'post-1',
            title: '시스템 점검 안내',
            content: '2024년 1월 15일 오전 2시부터 오전 6시까지 시스템 점검을 실시합니다. 이 시간 동안 서비스 이용이 제한될 수 있습니다.',
            category: 'announcement',
            authorId: 'user-1',
            authorName: '관리자 김',
            pinned: true,
            viewCount: 150,
            likeCount: 25,
            commentCount: 8,
            createdAt: '2024-01-10T09:00:00Z',
            updatedAt: '2024-01-10T09:00:00Z'
        },
        {
            id: 'post-2',
            title: '새로운 기능 업데이트',
            content: '게시판 기능이 추가되었습니다. 공지사항, 커뮤니티, Q&A 게시판을 이용해보세요.',
            category: 'announcement',
            authorId: 'user-1',
            authorName: '관리자 김',
            pinned: true,
            viewCount: 200,
            likeCount: 45,
            commentCount: 12,
            createdAt: '2024-01-08T14:30:00Z',
            updatedAt: '2024-01-08T14:30:00Z'
        },
        {
            id: 'post-3',
            title: '서비스 이용 가이드',
            content: 'KominioAI 서비스를 처음 이용하시는 분들을 위한 가이드입니다. 설문조사와 퀴즈 기능을 활용해보세요.',
            category: 'announcement',
            authorId: 'user-1',
            authorName: '관리자 김',
            pinned: true,
            viewCount: 300,
            likeCount: 60,
            commentCount: 15,
            createdAt: '2024-01-05T10:00:00Z',
            updatedAt: '2024-01-05T10:00:00Z'
        },
        {
            id: 'post-4',
            title: '설문조사 참여 후기',
            content: '최근 참여한 설문조사가 정말 유익했습니다. 다양한 주제의 설문조사가 있어서 좋네요.',
            category: 'community',
            authorId: 'user-2',
            authorName: '사용자 이',
            pinned: false,
            viewCount: 45,
            likeCount: 8,
            commentCount: 3,
            createdAt: '2024-01-12T16:20:00Z',
            updatedAt: '2024-01-12T16:20:00Z'
        },
        {
            id: 'post-5',
            title: '퀴즈 만들기 팁',
            content: '퀴즈를 만들 때 주의사항과 팁을 공유합니다. 좋은 퀴즈를 만드는 방법을 알아보세요.',
            category: 'community',
            authorId: 'user-3',
            authorName: '테스트 박',
            pinned: false,
            viewCount: 80,
            likeCount: 15,
            commentCount: 7,
            createdAt: '2024-01-11T11:15:00Z',
            updatedAt: '2024-01-11T11:15:00Z'
        },
        {
            id: 'post-6',
            title: '로그인이 안 돼요',
            content: '로그인을 시도했는데 계속 실패합니다. 비밀번호를 분실했을 수도 있어요. 어떻게 해야 하나요?',
            category: 'qna',
            authorId: 'user-2',
            authorName: '사용자 이',
            pinned: false,
            viewCount: 25,
            likeCount: 2,
            commentCount: 1,
            createdAt: '2024-01-13T09:30:00Z',
            updatedAt: '2024-01-13T09:30:00Z'
        },
        {
            id: 'post-7',
            title: '설문조사 결과는 언제 볼 수 있나요?',
            content: '설문조사에 참여했는데 결과를 언제 볼 수 있는지 궁금합니다.',
            category: 'qna',
            authorId: 'user-3',
            authorName: '테스트 박',
            pinned: false,
            viewCount: 35,
            likeCount: 3,
            commentCount: 2,
            createdAt: '2024-01-14T14:45:00Z',
            updatedAt: '2024-01-14T14:45:00Z'
        }
    ],
    comments: [
        {
            id: 'comment-1',
            postId: 'post-1',
            parentId: null,
            content: '점검 시간에 주의하겠습니다.',
            authorId: 'user-2',
            authorName: '사용자 이',
            createdAt: '2024-01-10T10:30:00Z',
            updatedAt: '2024-01-10T10:30:00Z'
        },
        {
            id: 'comment-2',
            postId: 'post-1',
            parentId: 'comment-1',
            content: '네, 알겠습니다.',
            authorId: 'user-3',
            authorName: '테스트 박',
            createdAt: '2024-01-10T11:00:00Z',
            updatedAt: '2024-01-10T11:00:00Z'
        },
        {
            id: 'comment-3',
            postId: 'post-4',
            parentId: null,
            content: '저도 참여해봤는데 정말 좋았어요!',
            authorId: 'user-3',
            authorName: '테스트 박',
            createdAt: '2024-01-12T17:00:00Z',
            updatedAt: '2024-01-12T17:00:00Z'
        },
        {
            id: 'comment-4',
            postId: 'post-6',
            parentId: null,
            content: '비밀번호 재설정 기능을 이용해보세요. 이메일로 재설정 링크를 보내드릴 수 있습니다.',
            authorId: 'user-1',
            authorName: '관리자 김',
            createdAt: '2024-01-13T10:00:00Z',
            updatedAt: '2024-01-13T10:00:00Z'
        },
        {
            id: 'comment-5',
            postId: 'post-7',
            parentId: null,
            content: '설문조사 종료 후 24시간 내에 결과를 확인할 수 있습니다.',
            authorId: 'user-1',
            authorName: '관리자 김',
            createdAt: '2024-01-14T15:00:00Z',
            updatedAt: '2024-01-14T15:00:00Z'
        }
    ]
};

// 전역 변수
let currentUser = null;
let isLoggedIn = false;
let currentPost = null;
let currentComments = [];

// 유틸리티 함수
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

function showLoading() {
    document.getElementById('loading-overlay').style.display = 'block';
}

function hideLoading() {
    document.getElementById('loading-overlay').style.display = 'none';
}

function showModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function showSection(sectionId) {
    // 모든 섹션 숨기기
    document.querySelectorAll('.content-section').forEach(section => {
        section.style.display = 'none';
    });
    
    // 선택된 섹션 보이기
    document.getElementById(`${sectionId}-section`).style.display = 'block';
    
    // 네비게이션 활성화
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector(`[onclick="showSection('${sectionId}')"]`).classList.add('active');
}

// 로그인/회원가입 처리
function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const email = formData.get('email');
    const password = formData.get('password');
    
    const user = dummyData.users.find(u => u.email === email && u.password === password);
    if (user) {
        currentUser = user;
        isLoggedIn = true;
        showToast('로그인 성공!', 'success');
        closeModal('login-modal');
        updateNavbar();
        loadBulletinPosts(); // 게시글 목록 새로고침
    } else {
        showToast('이메일 또는 비밀번호가 올바르지 않습니다.', 'error');
    }
}

function handleRegister(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const email = formData.get('email');
    const password = formData.get('password');
    const firstName = formData.get('firstName');
    const lastName = formData.get('lastName');
    
    const newUser = {
        id: 'user-' + Date.now(),
        email: email,
        password: password,
        firstName: firstName,
        lastName: lastName,
        role: 'USER',
        createdAt: new Date().toISOString()
    };
    
    dummyData.users.push(newUser);
    showToast('회원가입 성공!', 'success');
    closeModal('register-modal');
}

function updateNavbar() {
    const navActions = document.querySelector('.nav-actions');
    if (isLoggedIn) {
        navActions.innerHTML = `
            <span>안녕하세요, ${currentUser.firstName}님!</span>
            <button class="btn btn-outline" onclick="handleLogout()">로그아웃</button>
        `;
    } else {
        navActions.innerHTML = `
            <button class="btn btn-outline" onclick="showLoginModal()">로그인</button>
            <button class="btn btn-primary" onclick="showRegisterModal()">회원가입</button>
        `;
    }
    
    // 카테고리 필터 업데이트
    updateCategoryFilter();
}

function handleLogout() {
    currentUser = null;
    isLoggedIn = false;
    showToast('로그아웃되었습니다.', 'info');
    updateNavbar();
    loadBulletinPosts(); // 게시글 목록 새로고침
    showSection('bulletin');
}

// 게시판 관련 함수
function showBulletinTab(tab) {
    // 탭 버튼 활성화
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[onclick="showBulletinTab('${tab}')"]`).classList.add('active');
    
    // 카테고리 필터 업데이트
    updateCategoryFilter();
    
    // 게시글 목록 로드
    loadBulletinPosts(tab);
}

// 사용자 권한에 따른 카테고리 필터 업데이트
function updateCategoryFilter() {
    const categorySelect = document.getElementById('bulletin-category');
    if (!categorySelect) return;
    
    // 기존 옵션 제거
    categorySelect.innerHTML = '<option value="">전체</option>';
    
    // 사용자 권한에 따른 옵션 추가
    if (isLoggedIn && currentUser && currentUser.role === 'ADMIN') {
        // 관리자: 전체, 공지사항, 커뮤니티, Q&A
        categorySelect.innerHTML += `
            <option value="announcement">공지사항</option>
            <option value="community">커뮤니티</option>
            <option value="qna">Q&A</option>
        `;
    } else {
        // 일반 사용자: 전체, 커뮤니티, Q&A
        categorySelect.innerHTML += `
            <option value="community">커뮤니티</option>
            <option value="qna">Q&A</option>
        `;
    }
}

function loadBulletinPosts(category = null) {
    let posts = dummyData.posts;
    
    // 사용자 권한에 따른 필터링
    if (isLoggedIn && currentUser && currentUser.role !== 'ADMIN') {
        // 일반 사용자는 공지사항을 볼 수 없음
        posts = posts.filter(post => post.category !== 'announcement');
    }
    
    // 카테고리 필터링
    if (category) {
        posts = posts.filter(post => post.category === category);
    }
    
    // 공지사항 상단 고정
    posts.sort((a, b) => {
        if (a.pinned && !b.pinned) return -1;
        if (!a.pinned && b.pinned) return 1;
        return new Date(b.createdAt) - new Date(a.createdAt);
    });
    
    const bulletinList = document.getElementById('bulletin-list');
    bulletinList.innerHTML = '';
    
    posts.forEach(post => {
        const postElement = createPostElement(post);
        bulletinList.appendChild(postElement);
    });
}

function createPostElement(post) {
    const postDiv = document.createElement('div');
    postDiv.className = `bulletin-item ${post.pinned ? 'pinned' : ''}`;
    postDiv.onclick = () => showPostDetail(post.id);
    
    const categoryClass = post.category;
    const categoryName = {
        'announcement': '공지사항',
        'community': '커뮤니티',
        'qna': 'Q&A'
    }[post.category];
    
    postDiv.innerHTML = `
        <div class="bulletin-item-header">
            <div>
                <div class="bulletin-item-title">${post.title}</div>
                <div class="bulletin-item-meta">
                    <span class="bulletin-item-category ${categoryClass}">${categoryName}</span>
                    <span>작성자: ${post.authorName}</span>
                    <span>작성일: ${new Date(post.createdAt).toLocaleDateString()}</span>
                </div>
            </div>
        </div>
        <div class="bulletin-item-content">${post.content}</div>
        <div class="bulletin-item-footer">
            <div class="bulletin-item-stats">
                <span>조회 ${post.viewCount}</span>
                <span>좋아요 ${post.likeCount}</span>
                <span>댓글 ${post.commentCount}</span>
            </div>
            <div class="bulletin-item-actions">
                <button class="btn btn-outline" onclick="event.stopPropagation(); likePost('${post.id}')">좋아요</button>
                <button class="btn btn-outline" onclick="event.stopPropagation(); showCreateCommentModal('${post.id}')">댓글</button>
            </div>
        </div>
    `;
    
    return postDiv;
}

function showPostDetail(postId) {
    const post = dummyData.posts.find(p => p.id === postId);
    if (!post) return;
    
    currentPost = post;
    
    // 조회수 증가
    post.viewCount++;
    
    // 댓글 로드
    loadComments(postId);
    
    // 상세 페이지 표시
    document.getElementById('bulletin-section').style.display = 'none';
    document.getElementById('post-detail-section').style.display = 'block';
    
    const postDetailContent = document.getElementById('post-detail-content');
    postDetailContent.innerHTML = `
        <div class="post-detail-title">${post.title}</div>
        <div class="post-detail-meta">
            <span>작성자: ${post.authorName}</span>
            <span>작성일: ${new Date(post.createdAt).toLocaleDateString()}</span>
            <span>조회수: ${post.viewCount}</span>
            <span>좋아요: ${post.likeCount}</span>
        </div>
        <div class="post-detail-content-text">${post.content}</div>
        <div class="comments-section">
            <div class="comments-header">
                <h3>댓글 (${post.commentCount})</h3>
                <button class="btn btn-primary" onclick="showCreateCommentModal('${post.id}')">댓글 작성</button>
            </div>
            <div class="comment-list" id="comment-list">
                <!-- 댓글들이 여기에 로드됩니다 -->
            </div>
        </div>
    `;
}

function loadComments(postId) {
    const comments = dummyData.comments.filter(c => c.postId === postId);
    currentComments = comments;
    
    const commentList = document.getElementById('comment-list');
    if (!commentList) return;
    
    commentList.innerHTML = '';
    
    // 댓글 계층 구조 생성
    const commentTree = buildCommentTree(comments);
    
    commentTree.forEach(comment => {
        const commentElement = createCommentElement(comment, 0);
        commentList.appendChild(commentElement);
    });
}

function buildCommentTree(comments) {
    const commentMap = new Map();
    const rootComments = [];
    
    // 모든 댓글을 맵에 저장
    comments.forEach(comment => {
        commentMap.set(comment.id, { ...comment, replies: [] });
    });
    
    // 댓글 계층 구조 생성
    comments.forEach(comment => {
        if (comment.parentId) {
            const parent = commentMap.get(comment.parentId);
            if (parent) {
                parent.replies.push(commentMap.get(comment.id));
            }
        } else {
            rootComments.push(commentMap.get(comment.id));
        }
    });
    
    return rootComments;
}

function createCommentElement(comment, depth) {
    const commentDiv = document.createElement('div');
    commentDiv.className = `comment-item ${depth > 0 ? 'reply' : ''} ${depth > 1 ? 'reply-2' : ''}`;
    
    commentDiv.innerHTML = `
        <div class="comment-header">
            <div class="comment-author">${comment.authorName}</div>
            <div class="comment-meta">${new Date(comment.createdAt).toLocaleDateString()}</div>
        </div>
        <div class="comment-content">${comment.content}</div>
        <div class="comment-actions">
            <button class="btn btn-outline" onclick="likeComment('${comment.id}')">좋아요</button>
            <button class="btn btn-outline" onclick="showCreateCommentModal('${currentPost.id}', '${comment.id}')">답글</button>
            ${isLoggedIn && currentUser.id === comment.authorId ? `
                <button class="btn btn-outline" onclick="editComment('${comment.id}')">수정</button>
                <button class="btn btn-danger" onclick="deleteComment('${comment.id}')">삭제</button>
            ` : ''}
        </div>
    `;
    
    // 답글 추가
    if (comment.replies && comment.replies.length > 0) {
        const repliesDiv = document.createElement('div');
        comment.replies.forEach(reply => {
            const replyElement = createCommentElement(reply, depth + 1);
            repliesDiv.appendChild(replyElement);
        });
        commentDiv.appendChild(repliesDiv);
    }
    
    return commentDiv;
}

// 게시글 작성
function showCreatePostModal() {
    // 로그인 없이도 글쓰기 가능하도록 수정
    if (!isLoggedIn) {
        // 익명 사용자로 설정
        currentUser = {
            id: 'anonymous-' + Date.now(),
            email: 'anonymous@kominioai.com',
            firstName: '익명',
            lastName: '사용자',
            role: 'USER'
        };
        isLoggedIn = true;
        updateNavbar();
        showToast('익명으로 글쓰기를 시작합니다.', 'info');
    }
    
    // 카테고리 옵션 업데이트
    updatePostCategoryOptions();
    
    // 관리자인 경우 공지사항 선택 시 상단 고정 옵션 표시
    if (isLoggedIn && currentUser && currentUser.role === 'ADMIN') {
        const announcementOptions = document.getElementById('announcement-options');
        if (announcementOptions) {
            announcementOptions.style.display = 'block';
        }
    }
    
    showModal('create-post-modal');
}

// 게시글 작성 시 카테고리 옵션 업데이트
function updatePostCategoryOptions() {
    const categorySelect = document.getElementById('post-category');
    if (!categorySelect) return;
    
    // 기존 옵션 제거
    categorySelect.innerHTML = '';
    
    // 사용자 권한에 따른 옵션 추가
    if (isLoggedIn && currentUser && currentUser.role === 'ADMIN') {
        // 관리자: 공지사항이 기본 선택되도록 설정
        categorySelect.innerHTML = `
            <option value="announcement" selected>공지사항</option>
            <option value="community">커뮤니티</option>
            <option value="qna">Q&A</option>
        `;
    } else {
        // 일반 사용자: 커뮤니티가 기본 선택되도록 설정
        categorySelect.innerHTML = `
            <option value="community" selected>커뮤니티</option>
            <option value="qna">Q&A</option>
        `;
    }
}

function handleCreatePost(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const category = formData.get('category');
    const title = formData.get('title');
    const content = formData.get('content');
    const pinned = formData.get('pinned') === 'on';
    
    const newPost = {
        id: 'post-' + Date.now(),
        title: title,
        content: content,
        category: category,
        authorId: currentUser.id,
        authorName: currentUser.firstName + ' ' + currentUser.lastName,
        pinned: pinned,
        viewCount: 0,
        likeCount: 0,
        commentCount: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
    };
    
    dummyData.posts.unshift(newPost);
    showToast('게시글이 작성되었습니다!', 'success');
    closeModal('create-post-modal');
    loadBulletinPosts();
}

// 댓글 작성
function showCreateCommentModal(postId, parentId = null) {
    // 로그인 없이도 댓글 작성 가능하도록 수정
    if (!isLoggedIn) {
        // 익명 사용자로 설정
        currentUser = {
            id: 'anonymous-' + Date.now(),
            email: 'anonymous@kominioai.com',
            firstName: '익명',
            lastName: '사용자',
            role: 'USER'
        };
        isLoggedIn = true;
        updateNavbar();
        showToast('익명으로 댓글을 작성합니다.', 'info');
    }
    
    document.getElementById('comment-post-id').value = postId;
    document.getElementById('comment-parent-id').value = parentId || '';
    showModal('create-comment-modal');
}

function handleCreateComment(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const postId = formData.get('postId');
    const parentId = formData.get('parentId');
    const content = formData.get('content');
    
    const newComment = {
        id: 'comment-' + Date.now(),
        postId: postId,
        parentId: parentId || null,
        content: content,
        authorId: currentUser.id,
        authorName: currentUser.firstName + ' ' + currentUser.lastName,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
    };
    
    dummyData.comments.push(newComment);
    
    // 게시글 댓글 수 증가
    const post = dummyData.posts.find(p => p.id === postId);
    if (post) {
        post.commentCount++;
    }
    
    showToast('댓글이 작성되었습니다!', 'success');
    closeModal('create-comment-modal');
    
    // 댓글 목록 새로고침
    if (currentPost && currentPost.id === postId) {
        loadComments(postId);
    }
}

// 좋아요 기능
function likePost(postId) {
    // 로그인 없이도 좋아요 가능하도록 수정
    if (!isLoggedIn) {
        // 익명 사용자로 설정
        currentUser = {
            id: 'anonymous-' + Date.now(),
            email: 'anonymous@kominioai.com',
            firstName: '익명',
            lastName: '사용자',
            role: 'USER'
        };
        isLoggedIn = true;
        updateNavbar();
    }
    
    const post = dummyData.posts.find(p => p.id === postId);
    if (post) {
        post.likeCount++;
        showToast('좋아요를 눌렀습니다!', 'success');
        loadBulletinPosts();
    }
}

function likeComment(commentId) {
    // 로그인 없이도 좋아요 가능하도록 수정
    if (!isLoggedIn) {
        // 익명 사용자로 설정
        currentUser = {
            id: 'anonymous-' + Date.now(),
            email: 'anonymous@kominioai.com',
            firstName: '익명',
            lastName: '사용자',
            role: 'USER'
        };
        isLoggedIn = true;
        updateNavbar();
    }
    
    showToast('댓글에 좋아요를 눌렀습니다!', 'success');
}

// 댓글 수정/삭제
function editComment(commentId) {
    const comment = dummyData.comments.find(c => c.id === commentId);
    if (!comment) return;
    
    const newContent = prompt('댓글을 수정하세요:', comment.content);
    if (newContent && newContent.trim()) {
        comment.content = newContent.trim();
        comment.updatedAt = new Date().toISOString();
        showToast('댓글이 수정되었습니다!', 'success');
        loadComments(currentPost.id);
    }
}

function deleteComment(commentId) {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;
    
    const commentIndex = dummyData.comments.findIndex(c => c.id === commentId);
    if (commentIndex !== -1) {
        dummyData.comments.splice(commentIndex, 1);
        
        // 게시글 댓글 수 감소
        const post = dummyData.posts.find(p => p.id === currentPost.id);
        if (post) {
            post.commentCount--;
        }
        
        showToast('댓글이 삭제되었습니다!', 'success');
        loadComments(currentPost.id);
    }
}

// 게시글 수정/삭제
function editPost() {
    if (!isLoggedIn || currentUser.id !== currentPost.authorId) {
        showToast('수정 권한이 없습니다.', 'error');
        return;
    }
    
    const newTitle = prompt('제목을 수정하세요:', currentPost.title);
    if (newTitle && newTitle.trim()) {
        currentPost.title = newTitle.trim();
        currentPost.updatedAt = new Date().toISOString();
        showToast('게시글이 수정되었습니다!', 'success');
        showPostDetail(currentPost.id);
    }
}

function deletePost() {
    if (!isLoggedIn || currentUser.id !== currentPost.authorId) {
        showToast('삭제 권한이 없습니다.', 'error');
        return;
    }
    
    if (!confirm('게시글을 삭제하시겠습니까?')) return;
    
    const postIndex = dummyData.posts.findIndex(p => p.id === currentPost.id);
    if (postIndex !== -1) {
        dummyData.posts.splice(postIndex, 1);
        showToast('게시글이 삭제되었습니다!', 'success');
        showSection('bulletin');
    }
}

// 필터링 및 검색
function filterBulletinPosts() {
    const category = document.getElementById('bulletin-category').value;
    loadBulletinPosts(category);
}

function searchBulletinPosts() {
    const searchTerm = document.getElementById('bulletin-search').value.toLowerCase();
    let posts = dummyData.posts;
    
    // 사용자 권한에 따른 필터링
    if (isLoggedIn && currentUser && currentUser.role !== 'ADMIN') {
        // 일반 사용자는 공지사항을 볼 수 없음
        posts = posts.filter(post => post.category !== 'announcement');
    }
    
    if (searchTerm) {
        posts = posts.filter(post => 
            post.title.toLowerCase().includes(searchTerm) ||
            post.content.toLowerCase().includes(searchTerm)
        );
    }
    
    const bulletinList = document.getElementById('bulletin-list');
    bulletinList.innerHTML = '';
    
    posts.forEach(post => {
        const postElement = createPostElement(post);
        bulletinList.appendChild(postElement);
    });
}

function resetBulletinFilters() {
    document.getElementById('bulletin-category').value = '';
    document.getElementById('bulletin-search').value = '';
    loadBulletinPosts();
}

// 카테고리 변경 시 상단 고정 옵션 표시/숨김
document.addEventListener('DOMContentLoaded', function() {
    const categorySelect = document.getElementById('post-category');
    const announcementOptions = document.getElementById('announcement-options');
    
    if (categorySelect && announcementOptions) {
        categorySelect.addEventListener('change', function() {
            if (this.value === 'announcement') {
                announcementOptions.style.display = 'block';
            } else {
                announcementOptions.style.display = 'none';
                // 다른 카테고리 선택 시 상단 고정 체크 해제
                document.getElementById('post-pinned').checked = false;
            }
        });
    }
});

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    updateNavbar();
    // bulletin.js에서 초기화하므로 여기서는 제거
});
