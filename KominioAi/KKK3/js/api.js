// API 통신을 위한 JavaScript 모듈

const API_BASE_URL = 'http://localhost:8080/bulletin/api';

// API 클라이언트 클래스
class BulletinAPI {
    constructor() {
        this.baseURL = API_BASE_URL;
    }

    // 공통 요청 메서드
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': getCurrentUserId(),
                'X-User-Name': getCurrentUserName(),
                'X-User-Role': getCurrentUserRole(),
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    // 게시글 관련 API
    async createPost(postData) {
        return this.request('/posts', {
            method: 'POST',
            body: JSON.stringify(postData)
        });
    }

    async getPosts(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/posts${queryString ? `?${queryString}` : ''}`);
    }

    async getPost(postId) {
        return this.request(`/posts/${postId}`);
    }

    async updatePost(postId, postData) {
        return this.request(`/posts/${postId}`, {
            method: 'PUT',
            body: JSON.stringify(postData)
        });
    }

    async deletePost(postId) {
        return this.request(`/posts/${postId}`, {
            method: 'DELETE'
        });
    }

    async likePost(postId) {
        return this.request(`/posts/${postId}/like`, {
            method: 'POST'
        });
    }

    async incrementViewCount(postId) {
        return this.request(`/posts/${postId}/view`, {
            method: 'POST'
        });
    }

    async getPinnedPosts() {
        return this.request('/posts/pinned');
    }

    // 댓글 관련 API
    async createComment(postId, commentData) {
        return this.request(`/comments/post/${postId}`, {
            method: 'POST',
            body: JSON.stringify(commentData)
        });
    }

    async getComments(postId) {
        return this.request(`/comments/post/${postId}`);
    }

    async getCommentTree(postId) {
        return this.request(`/comments/post/${postId}/tree`);
    }

    async updateComment(commentId, commentData) {
        return this.request(`/comments/${commentId}`, {
            method: 'PUT',
            body: JSON.stringify(commentData)
        });
    }

    async deleteComment(commentId) {
        return this.request(`/comments/${commentId}`, {
            method: 'DELETE'
        });
    }

    async likeComment(commentId) {
        return this.request(`/comments/${commentId}/like`, {
            method: 'POST'
        });
    }
}

// 전역 API 인스턴스
const bulletinAPI = new BulletinAPI();

// 사용자 정보 가져오기 함수들
function getCurrentUserId() {
    return currentUser?.id || 'anonymous';
}

function getCurrentUserName() {
    return currentUser ? `${currentUser.firstName} ${currentUser.lastName}` : 'Anonymous';
}

function getCurrentUserRole() {
    return currentUser?.role || 'USER';
}

// API 에러 핸들링
function handleAPIError(error) {
    console.error('API Error:', error);
    
    let message = '오류가 발생했습니다.';
    
    if (error.message.includes('401')) {
        message = '로그인이 필요합니다.';
        showLoginModal();
    } else if (error.message.includes('403')) {
        message = '권한이 없습니다.';
    } else if (error.message.includes('404')) {
        message = '요청한 데이터를 찾을 수 없습니다.';
    } else if (error.message.includes('500')) {
        message = '서버 오류가 발생했습니다.';
    } else {
        message = error.message || message;
    }
    
    showToast(message, 'error');
}

// API 호출 래퍼 함수들
async function apiCreatePost(postData) {
    try {
        const result = await bulletinAPI.createPost(postData);
        showToast('게시글이 작성되었습니다!', 'success');
        return result;
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiGetPosts(params = {}) {
    try {
        return await bulletinAPI.getPosts(params);
    } catch (error) {
        console.warn('API call failed, falling back to dummy data:', error);
        // API 실패 시 더미 데이터로 폴백
        return {
            posts: dummyData.posts,
            totalCount: dummyData.posts.length
        };
    }
}

async function apiGetPost(postId) {
    try {
        return await bulletinAPI.getPost(postId);
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiUpdatePost(postId, postData) {
    try {
        const result = await bulletinAPI.updatePost(postId, postData);
        showToast('게시글이 수정되었습니다!', 'success');
        return result;
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiDeletePost(postId) {
    try {
        await bulletinAPI.deletePost(postId);
        showToast('게시글이 삭제되었습니다!', 'success');
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiLikePost(postId) {
    try {
        const result = await bulletinAPI.likePost(postId);
        showToast('좋아요를 눌렀습니다!', 'success');
        return result;
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiIncrementViewCount(postId) {
    try {
        await bulletinAPI.incrementViewCount(postId);
    } catch (error) {
        console.warn('View count increment failed:', error);
    }
}

async function apiCreateComment(postId, commentData) {
    try {
        const result = await bulletinAPI.createComment(postId, commentData);
        showToast('댓글이 작성되었습니다!', 'success');
        return result;
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiGetComments(postId) {
    try {
        return await bulletinAPI.getComments(postId);
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiGetCommentTree(postId) {
    try {
        return await bulletinAPI.getCommentTree(postId);
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiUpdateComment(commentId, commentData) {
    try {
        const result = await bulletinAPI.updateComment(commentId, commentData);
        showToast('댓글이 수정되었습니다!', 'success');
        return result;
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiDeleteComment(commentId) {
    try {
        await bulletinAPI.deleteComment(commentId);
        showToast('댓글이 삭제되었습니다!', 'success');
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

async function apiLikeComment(commentId) {
    try {
        const result = await bulletinAPI.likeComment(commentId);
        showToast('댓글에 좋아요를 눌렀습니다!', 'success');
        return result;
    } catch (error) {
        handleAPIError(error);
        throw error;
    }
}

// API 사용 여부 설정
const USE_API = true; // true로 설정하면 실제 API 호출, false면 더미 데이터 사용

// API 통합 함수들
async function loadBulletinPostsFromAPI(category = null) {
    if (!USE_API) {
        return loadBulletinPosts(category);
    }
    
    try {
        const params = {};
        if (category) {
            params.category = category.toUpperCase();
        }
        
        const response = await apiGetPosts(params);
        renderBulletinListFromAPI(response.posts);
        return response;
    } catch (error) {
        console.error('Failed to load posts from API:', error);
        // API 실패 시 더미 데이터로 폴백
        return loadBulletinPosts(category);
    }
}

async function loadPostDetailFromAPI(postId) {
    if (!USE_API) {
        return showPostDetail(postId);
    }
    
    try {
        const post = await apiGetPost(postId);
        await apiIncrementViewCount(postId);
        renderPostDetailFromAPI(post);
        return post;
    } catch (error) {
        console.error('Failed to load post detail from API:', error);
        // API 실패 시 더미 데이터로 폴백
        return showPostDetail(postId);
    }
}

async function createPostFromAPI(postData) {
    if (!USE_API) {
        return handleCreatePost({ target: { elements: postData } });
    }
    
    try {
        const result = await apiCreatePost(postData);
        await loadBulletinPostsFromAPI();
        return result;
    } catch (error) {
        console.error('Failed to create post via API:', error);
        throw error;
    }
}

async function createCommentFromAPI(postId, commentData) {
    if (!USE_API) {
        return handleCreateComment({ target: { elements: commentData } });
    }
    
    try {
        const result = await apiCreateComment(postId, commentData);
        await loadCommentsFromAPI(postId);
        return result;
    } catch (error) {
        console.error('Failed to create comment via API:', error);
        throw error;
    }
}

// API 응답을 UI에 렌더링하는 함수들
function renderBulletinListFromAPI(posts) {
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
        const postElement = createPostElementFromAPI(post);
        bulletinList.appendChild(postElement);
    });
}

function createPostElementFromAPI(post) {
    const postDiv = document.createElement('div');
    postDiv.className = `bulletin-item ${post.pinned ? 'pinned' : ''}`;
    postDiv.onclick = () => loadPostDetailFromAPI(post.id);
    
    const categoryClass = post.category.toLowerCase();
    const categoryName = {
        'announcement': '공지사항',
        'community': '커뮤니티',
        'qna': 'Q&A'
    }[post.category.toLowerCase()];
    
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
                <button class="btn btn-outline" onclick="event.stopPropagation(); apiLikePost('${post.id}')">좋아요</button>
                <button class="btn btn-outline" onclick="event.stopPropagation(); showCreateCommentModal('${post.id}')">댓글</button>
            </div>
        </div>
    `;
    
    return postDiv;
}

function renderPostDetailFromAPI(post) {
    document.getElementById('bulletin-section').style.display = 'none';
    document.getElementById('post-detail-section').style.display = 'block';
    
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
    
    loadCommentsFromAPI(post.id);
}

async function loadCommentsFromAPI(postId) {
    if (!USE_API) {
        return loadComments(postId);
    }
    
    try {
        const response = await apiGetCommentTree(postId);
        renderCommentsFromAPI(response.comments);
    } catch (error) {
        console.error('Failed to load comments from API:', error);
        // API 실패 시 더미 데이터로 폴백
        loadComments(postId);
    }
}

function renderCommentsFromAPI(comments) {
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
    
    comments.forEach(comment => {
        const commentElement = createCommentElementFromAPI(comment, 0);
        commentList.appendChild(commentElement);
    });
}

function createCommentElementFromAPI(comment, depth) {
    const commentDiv = document.createElement('div');
    commentDiv.className = `comment-item ${depth > 0 ? 'reply' : ''} ${depth > 1 ? 'reply-2' : ''}`;
    
    commentDiv.innerHTML = `
        <div class="comment-header">
            <div class="comment-author">${comment.authorName}</div>
            <div class="comment-meta">${formatDate(comment.createdAt)}</div>
        </div>
        <div class="comment-content">${comment.content}</div>
        <div class="comment-actions">
            <button class="btn btn-outline" onclick="apiLikeComment('${comment.id}')">좋아요</button>
            <button class="btn btn-outline" onclick="showCreateCommentModal('${currentPost.id}', '${comment.id}')">답글</button>
            ${isLoggedIn && currentUser && currentUser.id === comment.authorId ? `
                <button class="btn btn-outline" onclick="editComment('${comment.id}')">수정</button>
                <button class="btn btn-danger" onclick="deleteComment('${comment.id}')">삭제</button>
            ` : ''}
        </div>
    `;
    
    return commentDiv;
}
