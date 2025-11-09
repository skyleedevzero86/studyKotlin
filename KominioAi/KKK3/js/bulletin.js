// 게시판 전용 JavaScript

// 게시판 상태 관리
let currentBulletinState = {
    activeTab: 'announcement',
    currentPage: 1,
    postsPerPage: 10,
    searchTerm: '',
    categoryFilter: ''
};

// 게시판 초기화
function initBulletin() {
    loadBulletinPosts();
    setupBulletinEventListeners();
}

// 게시판 이벤트 리스너 설정
function setupBulletinEventListeners() {
    // 검색 입력 디바운스
    const searchInput = document.getElementById('bulletin-search');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function() {
            currentBulletinState.searchTerm = this.value;
            filterAndSearchPosts();
        }, 300));
    }
    
    // 카테고리 필터
    const categorySelect = document.getElementById('bulletin-category');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            currentBulletinState.categoryFilter = this.value;
            filterAndSearchPosts();
        });
    }
    
    // 게시글 작성 폼 카테고리 변경
    const postCategorySelect = document.getElementById('post-category');
    const announcementOptions = document.getElementById('announcement-options');
    if (postCategorySelect && announcementOptions) {
        postCategorySelect.addEventListener('change', function() {
            if (this.value === 'announcement') {
                announcementOptions.style.display = 'block';
            } else {
                announcementOptions.style.display = 'none';
                document.getElementById('post-pinned').checked = false;
            }
        });
    }
}

// 게시판 탭 변경
function showBulletinTab(tab) {
    currentBulletinState.activeTab = tab;
    currentBulletinState.currentPage = 1;
    
    // 탭 버튼 활성화
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[onclick="showBulletinTab('${tab}')"]`).classList.add('active');
    
    // 카테고리 필터 업데이트
    if (typeof updateCategoryFilter === 'function') {
        updateCategoryFilter();
    }
    
    // 게시글 목록 로드
    loadBulletinPosts();
}

// 게시글 목록 로드
async function loadBulletinPosts() {
    try {
        // API를 사용하여 게시글 로드
        const params = {};
        if (currentBulletinState.activeTab !== 'all') {
            params.category = currentBulletinState.activeTab.toUpperCase();
        }
        if (currentBulletinState.categoryFilter) {
            params.category = currentBulletinState.categoryFilter.toUpperCase();
        }
        if (currentBulletinState.searchTerm) {
            params.searchTerm = currentBulletinState.searchTerm;
        }
        params.page = currentBulletinState.currentPage - 1;
        params.size = currentBulletinState.postsPerPage;
        
        const response = await apiGetPosts(params);
        
        // 권한에 따른 필터링 (API에서 처리되지 않은 경우를 대비)
        let filteredPosts = response.posts;
        if (isLoggedIn && currentUser && currentUser.role !== 'ADMIN') {
            filteredPosts = response.posts.filter(post => post.category !== 'ANNOUNCEMENT');
        }
        
        renderBulletinList(filteredPosts);
        renderPagination(currentBulletinState.currentPage, Math.ceil(response.totalCount / currentBulletinState.postsPerPage));
    } catch (error) {
        console.error('Failed to load posts:', error);
        // API 실패 시 더미 데이터로 폴백
        loadBulletinPostsFromDummy();
    }
}

// 더미 데이터로 폴백하는 함수
function loadBulletinPostsFromDummy() {
    let posts = dummyData.posts;
    
    // 사용자 권한에 따른 필터링
    if (isLoggedIn && currentUser && currentUser.role !== 'ADMIN') {
        // 일반 사용자는 공지사항을 볼 수 없음
        posts = posts.filter(post => post.category !== 'announcement');
    }
    
    // 활성 탭에 따른 필터링
    if (currentBulletinState.activeTab !== 'all') {
        posts = posts.filter(post => post.category === currentBulletinState.activeTab);
    }
    
    // 카테고리 필터 적용
    if (currentBulletinState.categoryFilter) {
        posts = posts.filter(post => post.category === currentBulletinState.categoryFilter);
    }
    
    // 검색어 필터 적용
    if (currentBulletinState.searchTerm) {
        const searchTerm = currentBulletinState.searchTerm.toLowerCase();
        posts = posts.filter(post => 
            post.title.toLowerCase().includes(searchTerm) ||
            post.content.toLowerCase().includes(searchTerm) ||
            post.authorName.toLowerCase().includes(searchTerm)
        );
    }
    
    // 공지사항 상단 고정 정렬
    posts.sort((a, b) => {
        if (a.pinned && !b.pinned) return -1;
        if (!a.pinned && b.pinned) return 1;
        return new Date(b.createdAt) - new Date(a.createdAt);
    });
    
    // 페이지네이션 적용
    const totalPages = Math.ceil(posts.length / currentBulletinState.postsPerPage);
    const startIndex = (currentBulletinState.currentPage - 1) * currentBulletinState.postsPerPage;
    const endIndex = startIndex + currentBulletinState.postsPerPage;
    const paginatedPosts = posts.slice(startIndex, endIndex);
    
    // 게시글 목록 렌더링
    renderBulletinList(paginatedPosts);
    
    // 페이지네이션 렌더링
    renderPagination(currentBulletinState.currentPage, totalPages);
}

// 게시글 목록 렌더링
function renderBulletinList(posts) {
    const bulletinList = document.getElementById('bulletin-list');
    if (!bulletinList) return;
    
    bulletinList.innerHTML = '';
    
    if (posts.length === 0) {
        bulletinList.innerHTML = `
            <div class="empty-state">
                <h3>게시글이 없습니다</h3>
                <p>첫 번째 게시글을 작성해보세요!</p>
            </div>
        `;
        return;
    }
    
    posts.forEach(post => {
        const postElement = createPostElement(post);
        bulletinList.appendChild(postElement);
    });
}

// 게시글 요소 생성
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
                    <span>작성일: ${formatDate(post.createdAt)}</span>
                </div>
            </div>
        </div>
        <div class="bulletin-item-content">${truncateText(post.content, 150)}</div>
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

// 페이지네이션 렌더링
function renderPagination(currentPage, totalPages) {
    const pagination = document.getElementById('bulletin-pagination');
    if (!pagination) return;
    
    pagination.innerHTML = '';
    
    if (totalPages <= 1) return;
    
    // 이전 페이지 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = 'pagination-btn';
    prevBtn.textContent = '이전';
    prevBtn.disabled = currentPage === 1;
    prevBtn.onclick = () => changePage(currentPage - 1);
    pagination.appendChild(prevBtn);
    
    // 페이지 번호 버튼들
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `pagination-btn ${i === currentPage ? 'active' : ''}`;
        pageBtn.textContent = i;
        pageBtn.onclick = () => changePage(i);
        pagination.appendChild(pageBtn);
    }
    
    // 다음 페이지 버튼
    const nextBtn = document.createElement('button');
    nextBtn.className = 'pagination-btn';
    nextBtn.textContent = '다음';
    nextBtn.disabled = currentPage === totalPages;
    nextBtn.onclick = () => changePage(currentPage + 1);
    pagination.appendChild(nextBtn);
}

// 페이지 변경
function changePage(page) {
    currentBulletinState.currentPage = page;
    loadBulletinPosts();
    
    // 페이지 상단으로 스크롤
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 필터링 및 검색
function filterAndSearchPosts() {
    currentBulletinState.currentPage = 1;
    loadBulletinPosts();
}

// 게시글 상세 보기
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
    
    renderPostDetail(post);
}

// 게시글 상세 렌더링
function renderPostDetail(post) {
    const postDetailContent = document.getElementById('post-detail-content');
    if (!postDetailContent) return;
    
    postDetailContent.innerHTML = `
        <div class="post-detail-title">${post.title}</div>
        <div class="post-detail-meta">
            <span>작성자: ${post.authorName}</span>
            <span>작성일: ${formatDate(post.createdAt)}</span>
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

// 댓글 로드
function loadComments(postId) {
    const comments = dummyData.comments.filter(c => c.postId === postId);
    currentComments = comments;
    
    const commentList = document.getElementById('comment-list');
    if (!commentList) return;
    
    commentList.innerHTML = '';
    
    if (comments.length === 0) {
        commentList.innerHTML = `
            <div class="empty-state">
                <p>아직 댓글이 없습니다. 첫 번째 댓글을 작성해보세요!</p>
            </div>
        `;
        return;
    }
    
    // 댓글 계층 구조 생성
    const commentTree = buildCommentTree(comments);
    
    commentTree.forEach(comment => {
        const commentElement = createCommentElement(comment, 0);
        commentList.appendChild(commentElement);
    });
}

// 댓글 계층 구조 생성
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

// 댓글 요소 생성
function createCommentElement(comment, depth) {
    const commentDiv = document.createElement('div');
    commentDiv.className = `comment-item ${depth > 0 ? 'reply' : ''} ${depth > 1 ? 'reply-2' : ''}`;
    
    commentDiv.innerHTML = `
        <div class="comment-header">
            <div class="comment-author">${comment.authorName}</div>
            <div class="comment-meta">${formatDate(comment.createdAt)}</div>
        </div>
        <div class="comment-content">${comment.content}</div>
        <div class="comment-actions">
            <button class="btn btn-outline" onclick="likeComment('${comment.id}')">좋아요</button>
            <button class="btn btn-outline" onclick="showCreateCommentModal('${currentPost.id}', '${comment.id}')">답글</button>
            ${isLoggedIn && currentUser && currentUser.id === comment.authorId ? `
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
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
    }
    showModal('create-post-modal');
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
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
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
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
    }
    
    const post = dummyData.posts.find(p => p.id === postId);
    if (post) {
        post.likeCount++;
        showToast('좋아요를 눌렀습니다!', 'success');
        loadBulletinPosts();
    }
}

function likeComment(commentId) {
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
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
    currentBulletinState.categoryFilter = category;
    currentBulletinState.currentPage = 1;
    loadBulletinPosts();
}

function searchBulletinPosts() {
    const searchTerm = document.getElementById('bulletin-search').value;
    currentBulletinState.searchTerm = searchTerm;
    currentBulletinState.currentPage = 1;
    loadBulletinPosts();
}

function resetBulletinFilters() {
    document.getElementById('bulletin-category').value = '';
    document.getElementById('bulletin-search').value = '';
    currentBulletinState.categoryFilter = '';
    currentBulletinState.searchTerm = '';
    currentBulletinState.currentPage = 1;
    loadBulletinPosts();
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initBulletin();
    // 카테고리 필터 초기화
    if (typeof updateCategoryFilter === 'function') {
        updateCategoryFilter();
    }
});
