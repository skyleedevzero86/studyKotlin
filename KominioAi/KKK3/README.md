# Bulletin Board System

This module implements a comprehensive bulletin board and comment system for the KominioAI platform, featuring three types of bulletin boards and multi-level comment functionality.

## 📁 Project Structure

### Frontend (HTML/CSS/JavaScript)

- `index.html`: Main HTML structure with navigation, bulletin board sections, and modals
- `css/style.css`: Base styles for the application
- `css/bulletin.css`: Bulletin board specific styles
- `js/data.js`: Data management and business logic
- `js/app.js`: Common application utilities
- `js/bulletin.js`: Bulletin board specific functionality

### Backend (Kotlin)

- `domain/bulletin/domain/model`: Core domain entities (Post, Comment, PostId, CommentId, PostCategory)
- `domain/bulletin/domain/event`: Domain events (PostCreatedEvent, PostUpdatedEvent, PostDeletedEvent, CommentCreatedEvent, CommentUpdatedEvent, CommentDeletedEvent)
- `domain/bulletin/domain/service`: Domain services containing business logic (PostService, CommentService)
- `domain/bulletin/domain/repository`: Interfaces for domain repositories (PostRepository, CommentRepository)
- `domain/bulletin/application/port/in`: Inbound ports (use cases) for the application layer (PostUseCase, CommentUseCase)
- `domain/bulletin/application/port/out`: Outbound ports for the application layer (PostPersistencePort, CommentPersistencePort, EventPublisherPort)
- `domain/bulletin/application/dto`: Data Transfer Objects for requests and responses
- `domain/bulletin/application/service`: Application services implementing the use cases (PostApplicationService, CommentApplicationService)
- `domain/bulletin/adapter/in/web`: Inbound adapters (REST controllers) (PostController, CommentController)
- `domain/bulletin/adapter/out/persistence`: Outbound adapters for persistence (PostPersistenceAdapter, CommentPersistenceAdapter, PostEntity, CommentEntity, Spring Data R2DBC Repositories)
- `domain/bulletin/adapter/out/event`: Outbound adapter for event publishing (EventPublisherAdapter)

## ✨ Key Features

### 🏢 Bulletin Board Types

1. **공지사항 (Announcements)**: System announcements with pinning capability
2. **커뮤니티 (Community)**: User community discussions
3. **Q&A**: Question and answer board

### 📌 Post Management

- **CRUD Operations**: Create, Read, Update, Delete posts
- **Pinning System**: Announcements can be pinned to the top
- **View Count**: Track post views
- **Like System**: Users can like posts
- **Search & Filter**: Search by title/content and filter by category
- **Pagination**: Efficient post listing with pagination

### 💬 Comment System

- **Multi-level Comments**: Support for nested replies (up to 2 levels deep)
- **Comment Types**:
  - Survey/Quiz comments
  - Community comments (multi-level)
  - Q&A comments (admin replies)
- **Comment Management**: Create, update, delete comments
- **Like System**: Users can like comments
- **Thread Structure**: Hierarchical comment display

### 🎯 User Experience

- **Responsive Design**: Mobile-friendly interface
- **Real-time Updates**: Dynamic content loading
- **User Authentication**: Login/register system
- **Permission System**: Role-based access control
- **Toast Notifications**: User feedback system

## 🚀 Technical Stack

### Frontend

- **HTML5**: Semantic markup structure
- **CSS3**: Modern styling with flexbox and grid
- **JavaScript ES6+**: Modern JavaScript features
- **Responsive Design**: Mobile-first approach

### Backend

- **Kotlin**: Primary programming language
- **Spring WebFlux**: Reactive programming framework
- **Spring Data R2DBC**: Reactive database access
- **Hexagonal Architecture**: Clean architecture principles
- **Domain-Driven Design**: Rich domain models
- **Project Reactor**: Reactive streams

## 🔗 API Endpoints

### Posts

- `POST /api/posts` - Create a new post
- `GET /api/posts` - Get posts with filtering and pagination
- `GET /api/posts/{id}` - Get a specific post
- `PUT /api/posts/{id}` - Update a post
- `DELETE /api/posts/{id}` - Delete a post
- `POST /api/posts/{id}/view` - Increment view count
- `POST /api/posts/{id}/like` - Like a post
- `GET /api/posts/pinned` - Get pinned posts

### Comments

- `POST /api/comments/post/{postId}` - Create a comment
- `GET /api/comments/post/{postId}` - Get comments for a post
- `GET /api/comments/post/{postId}/tree` - Get comment tree
- `GET /api/comments/{id}` - Get a specific comment
- `PUT /api/comments/{id}` - Update a comment
- `DELETE /api/comments/{id}` - Delete a comment
- `POST /api/comments/{id}/like` - Like a comment

## 🛠️ Setup and Usage

### Frontend

1. Open `index.html` in a web browser
2. The application will load with dummy data
3. Use the navigation to explore different sections
4. Create posts and comments to test functionality

### Backend

1. **Database**: Ensure a PostgreSQL database is running
2. **Configuration**: Update `application.yml` with database connection details
3. **Run**: Start the Spring Boot application
4. **API**: Use the REST endpoints for integration

## 📊 Data Models

### Post

- `id`: Unique identifier
- `title`: Post title
- `content`: Post content
- `category`: Post category (ANNOUNCEMENT, COMMUNITY, QNA)
- `authorId`: Author user ID
- `authorName`: Author display name
- `pinned`: Whether the post is pinned
- `viewCount`: Number of views
- `likeCount`: Number of likes
- `commentCount`: Number of comments
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

### Comment

- `id`: Unique identifier
- `postId`: Associated post ID
- `parentId`: Parent comment ID (for replies)
- `content`: Comment content
- `authorId`: Author user ID
- `authorName`: Author display name
- `likeCount`: Number of likes
- `replyCount`: Number of replies
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

## 🎨 UI Features

### Bulletin Board Interface

- **Tab Navigation**: Switch between different board types
- **Search & Filter**: Find posts by title, content, or author
- **Post Cards**: Clean, modern post display
- **Pagination**: Navigate through multiple pages
- **Responsive Layout**: Works on all device sizes

### Comment System

- **Threaded Comments**: Hierarchical comment display
- **Reply System**: Multi-level reply support
- **Comment Actions**: Like, edit, delete comments
- **Real-time Updates**: Dynamic comment loading

### User Interface

- **Modal Dialogs**: Clean, accessible modals
- **Form Validation**: Client-side validation
- **Toast Notifications**: User feedback
- **Loading States**: Visual feedback during operations

## 🚀 **완성된 기능들**

### ✅ **프론트엔드 (완성)**

- **HTML**: 완전한 게시판 인터페이스와 모달 시스템
- **CSS**: 반응형 디자인과 모던 UI 스타일링
- **JavaScript**: 게시판/댓글 관리, 사용자 인증, API 통합
- **API 통합**: 백엔드와의 실시간 통신 지원

### ✅ **백엔드 (완성)**

- **도메인 모델**: Post, Comment, 이벤트, 서비스
- **애플리케이션 계층**: DTO, Use Case, Application Service
- **인프라 계층**: REST Controller, Persistence, Event Publisher
- **보안**: CORS, 인증, 권한 관리
- **에러 핸들링**: 전역 예외 처리

### ✅ **데이터베이스 (완성)**

- **마이그레이션**: Posts, Comments, Likes, Statistics 테이블
- **인덱스**: 성능 최적화를 위한 인덱스 설정
- **제약조건**: 데이터 무결성 보장

### ✅ **설정 및 배포 (완성)**

- **애플리케이션 설정**: 개발/운영 환경 분리
- **Docker**: 컨테이너화 지원
- **테스트**: 단위 테스트 포함
- **API 문서**: REST API 엔드포인트 명세

## 🎯 **사용 방법**

### **프론트엔드 실행**

```bash
# kkk3 폴더에서
# index.html을 브라우저로 열기
open index.html
```

### **백엔드 실행**

```bash
# kkk3 폴더에서
./gradlew bootRun
```

### **Docker로 실행**

```bash
# kkk3 폴더에서
docker-compose up -d
```

## 📊 **API 엔드포인트**

### **게시글 API**

- `POST /api/posts` - 게시글 작성
- `GET /api/posts` - 게시글 목록 (필터링, 페이지네이션)
- `GET /api/posts/{id}` - 게시글 상세
- `PUT /api/posts/{id}` - 게시글 수정
- `DELETE /api/posts/{id}` - 게시글 삭제
- `POST /api/posts/{id}/like` - 게시글 좋아요
- `POST /api/posts/{id}/view` - 조회수 증가
- `GET /api/posts/pinned` - 상단 고정 게시글

### **댓글 API**

- `POST /api/comments/post/{postId}` - 댓글 작성
- `GET /api/comments/post/{postId}` - 댓글 목록
- `GET /api/comments/post/{postId}/tree` - 계층형 댓글
- `PUT /api/comments/{id}` - 댓글 수정
- `DELETE /api/comments/{id}` - 댓글 삭제
- `POST /api/comments/{id}/like` - 댓글 좋아요

## 🔧 **기술 스택**

### **프론트엔드**

- HTML5, CSS3, JavaScript ES6+
- 반응형 디자인 (모바일 우선)
- 모던 UI/UX 패턴

### **백엔드**

- Kotlin + Spring WebFlux (리액티브)
- Spring Data R2DBC (비동기 DB)
- PostgreSQL
- Hexagonal Architecture
- Domain-Driven Design

### **인프라**

- Docker & Docker Compose
- Flyway (DB 마이그레이션)
- Spring Security
- CORS 지원

This bulletin board system provides a **완전한 솔루션** for community engagement, announcements, and Q&A functionality, ready for production deployment and integration with the main KominioAI platform.
