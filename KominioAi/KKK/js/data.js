// 더미 데이터
const dummyData = {
    // 사용자 데이터
    users: [
        {
            id: 'user-1',
            email: 'admin@kominioai.com',
            username: 'admin',
            firstName: '관리자',
            lastName: '김',
            phone: '010-1234-5678',
            profileImageUrl: null,
            emailVerified: true,
            twoFactorEnabled: true,
            accountStatus: 'ACTIVE',
            roles: ['ADMIN', 'USER'],
            createdAt: '2024-01-01T00:00:00Z',
            lastLoginAt: '2024-01-15T10:30:00Z'
        },
        {
            id: 'user-2',
            email: 'user@example.com',
            username: 'testuser',
            firstName: '테스트',
            lastName: '사용자',
            phone: '010-9876-5432',
            profileImageUrl: null,
            emailVerified: true,
            twoFactorEnabled: false,
            accountStatus: 'ACTIVE',
            roles: ['USER'],
            createdAt: '2024-01-05T00:00:00Z',
            lastLoginAt: '2024-01-14T15:20:00Z'
        }
    ],

    // 설문조사 데이터
    surveys: [
        {
            id: 'survey-1',
            title: '직장 만족도 조사',
            description: '현재 직장에서의 만족도와 개선사항을 파악하기 위한 설문조사입니다.',
            author: '김관리자',
            status: 'ACTIVE',
            surveyType: 'SURVEY',
            participantType: 'PUBLIC',
            startDate: '2024-01-10T00:00:00Z',
            endDate: '2024-01-31T23:59:59Z',
            timeLimit: null,
            participationCount: 156,
            targetCount: 200,
            createdAt: '2024-01-08T00:00:00Z',
            // 참여자 리워드 설정 (테스트용)
            participantReward: {
                enabled: true,
                type: 'GIFTCARD',
                value: 5000,
                description: '스타벅스 아메리카노 기프티콘',
                probability: 1.0, // 100% 확률 (테스트용)
                image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iIzRjYWY1MCIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+4p2UPC90ZXh0Pjwvc3ZnPg=='
            },
            questions: [
                {
                    id: 'q1',
                    content: '현재 직장에서의 전반적인 만족도는 어떠신가요?',
                    type: 'MULTIPLE_CHOICE',
                    order: 1,
                    isRequired: true,
                    options: [
                        { id: 'o1', content: '매우 만족', order: 1 },
                        { id: 'o2', content: '만족', order: 2 },
                        { id: 'o3', content: '보통', order: 3 },
                        { id: 'o4', content: '불만족', order: 4 },
                        { id: 'o5', content: '매우 불만족', order: 5 }
                    ]
                },
                {
                    id: 'q2',
                    content: '직장에서 가장 만족스러운 부분은 무엇인가요?',
                    type: 'MULTIPLE_CHOICE',
                    order: 2,
                    isRequired: true,
                    options: [
                        { id: 'o6', content: '급여 및 복리후생', order: 1 },
                        { id: 'o7', content: '업무 환경', order: 2 },
                        { id: 'o8', content: '동료 관계', order: 3 },
                        { id: 'o9', content: '성장 기회', order: 4 },
                        { id: 'o10', content: '워라밸', order: 5 }
                    ]
                },
                {
                    id: 'q3',
                    content: '개선이 필요한 부분이 있다면 구체적으로 설명해주세요.',
                    type: 'ESSAY',
                    order: 3,
                    isRequired: false,
                    options: []
                }
            ]
        },
        {
            id: 'survey-2',
            title: '고객 서비스 만족도 조사',
            description: '고객 서비스 품질 향상을 위한 만족도 조사입니다.',
            author: '김관리자',
            status: 'COMPLETED',
            surveyType: 'SURVEY',
            participantType: 'PUBLIC',
            startDate: '2023-12-01T00:00:00Z',
            endDate: '2023-12-31T23:59:59Z',
            timeLimit: null,
            participationCount: 89,
            targetCount: 100,
            createdAt: '2023-11-25T00:00:00Z',
            // 참여자 리워드 설정 (테스트용)
            participantReward: {
                enabled: true,
                type: 'CASH',
                value: 10000,
                description: '현금 10,000원',
                probability: 1.0, // 100% 확률 (테스트용)
                image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2ZmZDcwMCIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+77+9PC90ZXh0Pjwvc3ZnPg=='
            },
            questions: [
                {
                    id: 'q4',
                    content: '고객 서비스 직원의 친절도는 어떠했나요?',
                    type: 'MULTIPLE_CHOICE',
                    order: 1,
                    isRequired: true,
                    options: [
                        { id: 'o11', content: '매우 친절', order: 1 },
                        { id: 'o12', content: '친절', order: 2 },
                        { id: 'o13', content: '보통', order: 3 },
                        { id: 'o14', content: '불친절', order: 4 },
                        { id: 'o15', content: '매우 불친절', order: 5 }
                    ]
                }
            ]
        },
        {
            id: 'survey-3',
            title: '신제품 개발 아이디어 수집',
            description: '새로운 제품 개발을 위한 고객 의견 수집',
            author: '김관리자',
            status: 'DRAFT',
            surveyType: 'SURVEY',
            participantType: 'PRIVATE',
            startDate: null,
            endDate: null,
            timeLimit: null,
            participationCount: 0,
            targetCount: 50,
            createdAt: '2024-01-15T00:00:00Z',
            questions: []
        }
    ],

    // 퀴즈 데이터
    quizzes: [
        {
            id: 'quiz-1',
            title: 'AI 기초 지식 퀴즈',
            description: '인공지능의 기본 개념과 원리에 대한 퀴즈입니다.',
            author: '김관리자',
            status: 'ACTIVE',
            surveyType: 'QUIZ',
            participantType: 'PUBLIC',
            startDate: '2024-01-15T00:00:00Z',
            endDate: '2024-01-30T23:59:59Z',
            timeLimit: 30,
            participationCount: 45,
            targetCount: 100,
            createdAt: '2024-01-12T00:00:00Z',
            // 참여자 리워드 설정 (테스트용)
            participantReward: {
                enabled: true,
                type: 'POINTS',
                value: 5000,
                description: '포인트 5,000점',
                probability: 1.0, // 100% 확률 (테스트용)
                image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iIzIxOTZmMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+77+9PC90ZXh0Pjwvc3ZnPg=='
            },
            questions: [
                {
                    id: 'q5',
                    content: '머신러닝에서 지도학습(Supervised Learning)의 특징은?',
                    type: 'QUIZ_MULTIPLE_CHOICE',
                    order: 1,
                    isRequired: true,
                    options: [
                        { id: 'o16', content: '정답이 없는 데이터로 학습', order: 1, isCorrect: false },
                        { id: 'o17', content: '정답이 있는 데이터로 학습', order: 2, isCorrect: true },
                        { id: 'o18', content: '환경과의 상호작용으로 학습', order: 3, isCorrect: false },
                        { id: 'o19', content: '무작위로 학습', order: 4, isCorrect: false }
                    ]
                },
                {
                    id: 'q6',
                    content: '딥러닝에서 가장 많이 사용되는 활성화 함수는?',
                    type: 'QUIZ_MULTIPLE_CHOICE',
                    order: 2,
                    isRequired: true,
                    options: [
                        { id: 'o20', content: 'Sigmoid', order: 1, isCorrect: false },
                        { id: 'o21', content: 'ReLU', order: 2, isCorrect: true },
                        { id: 'o22', content: 'Tanh', order: 3, isCorrect: false },
                        { id: 'o23', content: 'Linear', order: 4, isCorrect: false }
                    ]
                }
            ]
        },
        {
            id: 'quiz-2',
            title: '웹 개발 기초 퀴즈',
            description: 'HTML, CSS, JavaScript 기초 지식 테스트',
            author: '김관리자',
            status: 'ACTIVE',
            surveyType: 'QUIZ',
            participantType: 'PUBLIC',
            startDate: '2024-01-10T00:00:00Z',
            endDate: '2024-01-25T23:59:59Z',
            timeLimit: 20,
            participationCount: 78,
            targetCount: 150,
            createdAt: '2024-01-08T00:00:00Z',
            // 참여자 리워드 설정 (테스트용)
            participantReward: {
                enabled: true,
                type: 'GIFTCARD',
                value: 10000,
                description: '기프티콘 10,000원',
                probability: 1.0, // 100% 확률 (테스트용)
                image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iIzRjYWY1MCIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+77+9PC90ZXh0Pjwvc3ZnPg=='
            },
            questions: [
                {
                    id: 'q7',
                    content: 'CSS에서 요소를 가운데 정렬하는 방법은?',
                    type: 'QUIZ_MULTIPLE_CHOICE',
                    order: 1,
                    isRequired: true,
                    options: [
                        { id: 'o24', content: 'text-align: center', order: 1, isCorrect: false },
                        { id: 'o25', content: 'margin: 0 auto', order: 2, isCorrect: true },
                        { id: 'o26', content: 'float: center', order: 3, isCorrect: false },
                        { id: 'o27', content: 'position: center', order: 4, isCorrect: false }
                    ]
                }
            ]
        }
    ],

    // 참여 데이터
    participations: [
        {
            id: 'part-1',
            surveyId: 'survey-1',
            userId: 'user-2',
            status: 'COMPLETED',
            startedAt: '2024-01-12T10:00:00Z',
            completedAt: '2024-01-12T10:15:00Z',
            answers: [
                {
                    questionId: 'q1',
                    answer: 'o2',
                    textAnswer: null
                },
                {
                    questionId: 'q2',
                    answer: 'o6',
                    textAnswer: null
                },
                {
                    questionId: 'q3',
                    answer: null,
                    textAnswer: '더 나은 업무 환경과 성장 기회가 필요합니다.'
                }
            ]
        }
    ],

    // 시스템 통계
    statistics: {
        totalUsers: 1234,
        activeUsers: 1156,
        suspendedUsers: 12,
        adminUsers: 3,
        usersRegisteredToday: 8,
        usersWith2FAEnabled: 234,
        totalSurveys: 567,
        activeSurveys: 45,
        completedSurveys: 522,
        totalQuizzes: 89,
        activeQuizzes: 12,
        completedQuizzes: 77,
        totalParticipations: 3456,
        averageParticipationRate: 95.2
    },

    // 보안 로그
    securityLogs: [
        {
            id: 'log-1',
            userId: 'user-2',
            eventType: 'LOGIN_SUCCESS',
            eventDescription: '로그인 성공',
            ipAddress: '192.168.1.100',
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            success: true,
            failureReason: null,
            createdAt: '2024-01-15T10:30:00Z'
        },
        {
            id: 'log-2',
            userId: 'user-2',
            eventType: 'PASSWORD_CHANGE',
            eventDescription: '비밀번호 변경',
            ipAddress: '192.168.1.100',
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            success: true,
            failureReason: null,
            createdAt: '2024-01-14T15:20:00Z'
        },
        {
            id: 'log-3',
            userId: 'user-2',
            eventType: 'LOGIN_FAILED',
            eventDescription: '로그인 실패 - 잘못된 비밀번호',
            ipAddress: '192.168.1.100',
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            success: false,
            failureReason: '잘못된 비밀번호',
            createdAt: '2024-01-14T15:18:00Z'
        },
        {
            id: 'log-4',
            userId: 'user-1',
            eventType: 'LOGIN_SUCCESS',
            eventDescription: '관리자 로그인 성공',
            ipAddress: '192.168.1.50',
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            success: true,
            failureReason: null,
            createdAt: '2024-01-15T09:15:00Z'
        },
        {
            id: 'log-5',
            userId: 'user-3',
            eventType: 'SUSPICIOUS_ACTIVITY',
            eventDescription: '의심스러운 로그인 시도 - 짧은 시간 내 다수 실패',
            ipAddress: '203.0.113.1',
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            success: false,
            failureReason: '의심스러운 활동 감지',
            createdAt: '2024-01-15T08:45:00Z'
        },
        {
            id: 'log-6',
            userId: 'user-4',
            eventType: 'ACCOUNT_SUSPENDED',
            eventDescription: '계정 정지 - 정책 위반',
            ipAddress: '192.168.1.200',
            userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            success: true,
            failureReason: null,
            createdAt: '2024-01-14T16:30:00Z'
        }
    ],

    // 관리자용 확장 사용자 데이터
    adminUsers: [
        {
            id: 'user-1',
            email: 'admin@kominioai.com',
            username: 'admin',
            firstName: '관리자',
            lastName: '김',
            phone: '010-1234-5678',
            profileImageUrl: null,
            emailVerified: true,
            twoFactorEnabled: true,
            accountStatus: 'ACTIVE',
            roles: ['ADMIN', 'USER'],
            createdAt: '2024-01-01T00:00:00Z',
            lastLoginAt: '2024-01-15T10:30:00Z',
            loginCount: 156,
            lastActivityAt: '2024-01-15T10:30:00Z'
        },
        {
            id: 'user-2',
            email: 'user@example.com',
            username: 'testuser',
            firstName: '테스트',
            lastName: '사용자',
            phone: '010-9876-5432',
            profileImageUrl: null,
            emailVerified: true,
            twoFactorEnabled: false,
            accountStatus: 'ACTIVE',
            roles: ['USER'],
            createdAt: '2024-01-05T00:00:00Z',
            lastLoginAt: '2024-01-14T15:20:00Z',
            loginCount: 23,
            lastActivityAt: '2024-01-14T15:20:00Z'
        },
        {
            id: 'user-3',
            email: 'john.doe@example.com',
            username: 'johndoe',
            firstName: 'John',
            lastName: 'Doe',
            phone: '010-1111-2222',
            profileImageUrl: null,
            emailVerified: false,
            twoFactorEnabled: false,
            accountStatus: 'PENDING',
            roles: ['USER'],
            createdAt: '2024-01-10T00:00:00Z',
            lastLoginAt: null,
            loginCount: 0,
            lastActivityAt: null
        },
        {
            id: 'user-4',
            email: 'suspended@example.com',
            username: 'suspended_user',
            firstName: '정지된',
            lastName: '사용자',
            phone: '010-3333-4444',
            profileImageUrl: null,
            emailVerified: true,
            twoFactorEnabled: false,
            accountStatus: 'SUSPENDED',
            roles: ['USER'],
            createdAt: '2024-01-08T00:00:00Z',
            lastLoginAt: '2024-01-12T14:30:00Z',
            loginCount: 5,
            lastActivityAt: '2024-01-12T14:30:00Z'
        },
        {
            id: 'user-5',
            email: 'poweruser@example.com',
            username: 'poweruser',
            firstName: '파워',
            lastName: '유저',
            phone: '010-5555-6666',
            profileImageUrl: null,
            emailVerified: true,
            twoFactorEnabled: true,
            accountStatus: 'ACTIVE',
            roles: ['USER'],
            createdAt: '2024-01-03T00:00:00Z',
            lastLoginAt: '2024-01-15T08:45:00Z',
            loginCount: 89,
            lastActivityAt: '2024-01-15T08:45:00Z'
        }
    ],

    // 관리자용 시스템 통계
    adminStatistics: {
        userGrowth: [
            { date: '2024-01-01', users: 100 },
            { date: '2024-01-02', users: 105 },
            { date: '2024-01-03', users: 112 },
            { date: '2024-01-04', users: 118 },
            { date: '2024-01-05', users: 125 },
            { date: '2024-01-06', users: 130 },
            { date: '2024-01-07', users: 135 },
            { date: '2024-01-08', users: 142 },
            { date: '2024-01-09', users: 148 },
            { date: '2024-01-10', users: 155 },
            { date: '2024-01-11', users: 162 },
            { date: '2024-01-12', users: 168 },
            { date: '2024-01-13', users: 175 },
            { date: '2024-01-14', users: 180 },
            { date: '2024-01-15', users: 185 }
        ],
        surveyQuizStats: [
            { type: '설문조사', count: 567, participation: 3456 },
            { type: '퀴즈', count: 89, participation: 1234 }
        ],
        participationRates: {
            avgSurveyParticipation: 78.5,
            avgQuizParticipation: 85.2,
            completionRate: 92.3
        }
    },

    // 리워드 시스템 데이터
    rewards: {
    // 리워드 풀 (상품 목록)
    rewardPool: [
        {
            id: 'reward-1',
            name: '스타벅스 아메리카노 기프티콘',
            description: '스타벅스 아메리카노 Tall 사이즈 기프티콘',
            value: 4500,
            type: 'GIFTCARD',
            image: 'https://via.placeholder.com/100x100/4CAF50/white?text=☕',
            probability: 0.2 // 20% 확률
        },
        {
            id: 'reward-2',
            name: '네이버페이 5,000원',
            description: '네이버페이 포인트 5,000원',
            value: 5000,
            type: 'POINTS',
            image: 'https://via.placeholder.com/100x100/2196F3/white?text=💰',
            probability: 0.2 // 20% 확률
        },
        {
            id: 'reward-3',
            name: '쿠팡 이츠 3,000원 할인쿠폰',
            description: '쿠팡 이츠 주문 시 3,000원 할인',
            value: 3000,
            type: 'COUPON',
            image: 'https://via.placeholder.com/100x100/FF9800/white?text=🍔',
            probability: 0.15 // 15% 확률
        },
        {
            id: 'reward-4',
            name: '현금 10,000원',
            description: '계좌이체로 지급되는 현금',
            value: 10000,
            type: 'CASH',
            image: 'https://via.placeholder.com/100x100/FFD700/white?text=💵',
            probability: 0.1 // 10% 확률
        },
        {
            id: 'reward-5',
            name: '아마존 기프트카드 20달러',
            description: '아마존에서 사용 가능한 기프트카드',
            value: 25000,
            type: 'GIFTCARD',
            image: 'https://via.placeholder.com/100x100/FF6B6B/white?text=🎁',
            probability: 0.1 // 10% 확률
        },
        {
            id: 'reward-6',
            name: '구글 플레이 스토어 크레딧 5,000원',
            description: '구글 플레이 스토어에서 사용 가능한 크레딧',
            value: 5000,
            type: 'DIGITAL',
            image: 'https://via.placeholder.com/100x100/4285F4/white?text=📱',
            probability: 0.1 // 10% 확률
        },
        {
            id: 'reward-7',
            name: '네이버 쇼핑 적립금 7,000원',
            description: '네이버 쇼핑에서 사용 가능한 적립금',
            value: 7000,
            type: 'POINTS',
            image: 'https://via.placeholder.com/100x100/03C75A/white?text=🛒',
            probability: 0.1 // 10% 확률
        },
        {
            id: 'reward-8',
            name: '현금 50,000원',
            description: '계좌이체로 지급되는 현금 (대박!)',
            value: 50000,
            type: 'CASH',
            image: 'https://via.placeholder.com/100x100/FFD700/white?text=💰',
            probability: 0.05 // 5% 확률 (희귀)
        }
    ],
        
        // 리워드 이벤트 설정
        eventSettings: {
            maxWinners: 3, // 최대 당첨자 수
            participationThreshold: 1, // 최소 참여자 수 (테스트용으로 낮춤)
            eventActive: true, // 이벤트 활성화 여부
            startDate: '2024-01-01T00:00:00Z',
            endDate: '2024-12-31T23:59:59Z'
        },
        
        // 당첨자 목록
        winners: [
            {
                id: 'winner-1',
                userId: 'user-2',
                surveyId: 'survey-1',
                rewardId: 'reward-1',
                wonAt: '2024-01-15T10:30:00Z',
                claimed: false,
                claimCode: 'SB20240115001'
            },
            {
                id: 'winner-2',
                userId: 'user-5',
                surveyId: 'survey-1',
                rewardId: 'reward-2',
                wonAt: '2024-01-15T11:15:00Z',
                claimed: true,
                claimCode: 'NP20240115002'
            }
        ],
        
        // 사용자 리워드 히스토리
        userRewards: {
            'user-2': [
                {
                    rewardId: 'reward-1',
                    surveyId: 'survey-1',
                    wonAt: '2024-01-15T10:30:00Z',
                    claimed: false,
                    claimCode: 'SB20240115001'
                }
            ],
            'user-5': [
                {
                    rewardId: 'reward-2',
                    surveyId: 'survey-1',
                    wonAt: '2024-01-15T11:15:00Z',
                    claimed: true,
                    claimCode: 'NP20240115002'
                }
            ]
        }
    },

    // 설문 결과 데이터
    surveyResults: {
        'survey-1': {
            totalParticipants: 156,
            completedParticipants: 142,
            completionRate: 91.0,
            averageTime: 8.5,
            questionResults: [
                {
                    questionId: 'q1',
                    questionContent: '현재 직장에서의 전반적인 만족도는 어떠신가요?',
                    questionType: 'MULTIPLE_CHOICE',
                    totalResponses: 142,
                    optionResults: [
                        { optionId: 'o1', content: '매우 만족', count: 45, percentage: 31.7 },
                        { optionId: 'o2', content: '만족', count: 52, percentage: 36.6 },
                        { optionId: 'o3', content: '보통', count: 32, percentage: 22.5 },
                        { optionId: 'o4', content: '불만족', count: 10, percentage: 7.0 },
                        { optionId: 'o5', content: '매우 불만족', count: 3, percentage: 2.1 }
                    ]
                },
                {
                    questionId: 'q2',
                    questionContent: '직장에서 가장 만족스러운 부분은 무엇인가요?',
                    questionType: 'MULTIPLE_CHOICE',
                    totalResponses: 142,
                    optionResults: [
                        { optionId: 'o6', content: '급여 및 복리후생', count: 38, percentage: 26.8 },
                        { optionId: 'o7', content: '업무 환경', count: 42, percentage: 29.6 },
                        { optionId: 'o8', content: '동료 관계', count: 35, percentage: 24.6 },
                        { optionId: 'o9', content: '성장 기회', count: 20, percentage: 14.1 },
                        { optionId: 'o10', content: '워라밸', count: 7, percentage: 4.9 }
                    ]
                },
                {
                    questionId: 'q3',
                    questionContent: '개선이 필요한 부분이 있다면 구체적으로 설명해주세요.',
                    questionType: 'ESSAY',
                    totalResponses: 89,
                    textResponses: [
                        '더 나은 업무 환경과 성장 기회가 필요합니다.',
                        '급여 인상과 복리후생 개선이 필요합니다.',
                        '동료들과의 소통이 더 원활해졌으면 좋겠습니다.',
                        '업무량이 너무 많아서 워라밸이 어렵습니다.',
                        '경력 개발을 위한 교육 기회가 더 필요합니다.'
                    ]
                }
            ]
        },
        'survey-2': {
            totalParticipants: 89,
            completedParticipants: 89,
            completionRate: 100.0,
            averageTime: 5.2,
            questionResults: [
                {
                    questionId: 'q4',
                    questionContent: '고객 서비스 직원의 친절도는 어떠했나요?',
                    questionType: 'MULTIPLE_CHOICE',
                    totalResponses: 89,
                    optionResults: [
                        { optionId: 'o11', content: '매우 친절', count: 35, percentage: 39.3 },
                        { optionId: 'o12', content: '친절', count: 42, percentage: 47.2 },
                        { optionId: 'o13', content: '보통', count: 10, percentage: 11.2 },
                        { optionId: 'o14', content: '불친절', count: 2, percentage: 2.2 },
                        { optionId: 'o15', content: '매우 불친절', count: 0, percentage: 0.0 }
                    ]
                }
            ]
        }
    },

    // 퀴즈 결과 데이터
    quizResults: {
        'quiz-1': {
            totalParticipants: 45,
            completedParticipants: 45,
            completionRate: 100.0,
            averageScore: 78.5,
            passRate: 73.3,
            passScore: 70,
            scoreDistribution: [
                { score: '90-100', count: 8, percentage: 17.8 },
                { score: '80-89', count: 15, percentage: 33.3 },
                { score: '70-79', count: 10, percentage: 22.2 },
                { score: '60-69', count: 7, percentage: 15.6 },
                { score: '50-59', count: 3, percentage: 6.7 },
                { score: '0-49', count: 2, percentage: 4.4 }
            ],
            questionResults: [
                {
                    questionId: 'q5',
                    questionContent: '머신러닝에서 지도학습(Supervised Learning)의 특징은?',
                    questionType: 'QUIZ_MULTIPLE_CHOICE',
                    totalResponses: 45,
                    correctAnswers: 38,
                    correctRate: 84.4,
                    optionResults: [
                        { optionId: 'o16', content: '정답이 없는 데이터로 학습', count: 3, percentage: 6.7, isCorrect: false },
                        { optionId: 'o17', content: '정답이 있는 데이터로 학습', count: 38, percentage: 84.4, isCorrect: true },
                        { optionId: 'o18', content: '환경과의 상호작용으로 학습', count: 2, percentage: 4.4, isCorrect: false },
                        { optionId: 'o19', content: '무작위로 학습', count: 2, percentage: 4.4, isCorrect: false }
                    ]
                },
                {
                    questionId: 'q6',
                    questionContent: '딥러닝에서 가장 많이 사용되는 활성화 함수는?',
                    questionType: 'QUIZ_MULTIPLE_CHOICE',
                    totalResponses: 45,
                    correctAnswers: 32,
                    correctRate: 71.1,
                    optionResults: [
                        { optionId: 'o20', content: 'Sigmoid', count: 8, percentage: 17.8, isCorrect: false },
                        { optionId: 'o21', content: 'ReLU', count: 32, percentage: 71.1, isCorrect: true },
                        { optionId: 'o22', content: 'Tanh', count: 4, percentage: 8.9, isCorrect: false },
                        { optionId: 'o23', content: 'Linear', count: 1, percentage: 2.2, isCorrect: false }
                    ]
                }
            ]
        },
        'quiz-2': {
            totalParticipants: 78,
            completedParticipants: 78,
            completionRate: 100.0,
            averageScore: 85.2,
            passRate: 89.7,
            passScore: 70,
            scoreDistribution: [
                { score: '90-100', count: 25, percentage: 32.1 },
                { score: '80-89', count: 20, percentage: 25.6 },
                { score: '70-79', count: 15, percentage: 19.2 },
                { score: '60-69', count: 12, percentage: 15.4 },
                { score: '50-59', count: 4, percentage: 5.1 },
                { score: '0-49', count: 2, percentage: 2.6 }
            ],
            questionResults: [
                {
                    questionId: 'q7',
                    questionContent: 'CSS에서 요소를 가운데 정렬하는 방법은?',
                    questionType: 'QUIZ_MULTIPLE_CHOICE',
                    totalResponses: 78,
                    correctAnswers: 65,
                    correctRate: 83.3,
                    optionResults: [
                        { optionId: 'o24', content: 'text-align: center', count: 8, percentage: 10.3, isCorrect: false },
                        { optionId: 'o25', content: 'margin: 0 auto', count: 65, percentage: 83.3, isCorrect: true },
                        { optionId: 'o26', content: 'float: center', count: 3, percentage: 3.8, isCorrect: false },
                        { optionId: 'o27', content: 'position: center', count: 2, percentage: 2.6, isCorrect: false }
                    ]
                }
            ]
        }
    }
};

// 현재 사용자 상태
let currentUser = null;
let isLoggedIn = false;

// 로컬 스토리지에서 사용자 정보 로드
function loadUserFromStorage() {
    const userData = localStorage.getItem('currentUser');
    if (userData) {
        currentUser = JSON.parse(userData);
        isLoggedIn = true;
        updateUIForLoggedInUser();
    }
}

// 사용자 정보를 로컬 스토리지에 저장
function saveUserToStorage(user) {
    localStorage.setItem('currentUser', JSON.stringify(user));
    currentUser = user;
    isLoggedIn = true;
}

// 로그아웃 시 로컬 스토리지 정리
function clearUserFromStorage() {
    localStorage.removeItem('currentUser');
    currentUser = null;
    isLoggedIn = false;
}

// 로그인된 사용자 UI 업데이트
function updateUIForLoggedInUser() {
    if (isLoggedIn && currentUser) {
        // 네비게이션 업데이트
        document.getElementById('nav-auth').style.display = 'none';
        document.getElementById('nav-user').style.display = 'flex';
        document.getElementById('user-name').textContent = currentUser.firstName || currentUser.username;
        
        // 관리자 권한 확인
        if (currentUser.roles && currentUser.roles.includes('ADMIN')) {
            document.querySelector('.admin-only').style.display = 'block';
        }
    } else {
        // 로그아웃 상태 UI
        document.getElementById('nav-auth').style.display = 'flex';
        document.getElementById('nav-user').style.display = 'none';
        document.querySelector('.admin-only').style.display = 'none';
    }
}

// 페이지 로드 시 사용자 상태 확인
document.addEventListener('DOMContentLoaded', function() {
    loadUserFromStorage();
    updateUIForLoggedInUser();
    loadSurveys();
    loadQuizzes();
    updateStatistics();
});

// 설문조사 로드
function loadSurveys() {
    const surveysGrid = document.getElementById('surveys-grid');
    if (!surveysGrid) return;

    surveysGrid.innerHTML = '';
    
    dummyData.surveys.forEach(survey => {
        const surveyCard = createSurveyCard(survey);
        surveysGrid.appendChild(surveyCard);
    });
}

// 퀴즈 로드
function loadQuizzes() {
    const quizzesGrid = document.getElementById('quizzes-grid');
    if (!quizzesGrid) return;

    quizzesGrid.innerHTML = '';
    
    dummyData.quizzes.forEach(quiz => {
        const quizCard = createQuizCard(quiz);
        quizzesGrid.appendChild(quizCard);
    });
}

// 설문 카드 생성
function createSurveyCard(survey) {
    const card = document.createElement('div');
    card.className = 'survey-card';
    card.onclick = () => showSurveyDetail(survey.id);

    const statusClass = survey.status.toLowerCase();
    const statusText = {
        'active': '진행중',
        'completed': '완료',
        'draft': '초안'
    }[survey.status.toLowerCase()] || survey.status;

    card.innerHTML = `
        <div class="card-header">
            <div class="card-status status-${statusClass}">${statusText}</div>
            <h3>${survey.title}</h3>
            <p>${survey.description}</p>
        </div>
        <div class="card-body">
            <div class="card-meta">
                <span><i class="fas fa-user"></i> ${survey.author}</span>
                <span><i class="fas fa-calendar"></i> ${formatDate(survey.createdAt)}</span>
            </div>
            <div class="card-stats">
                <div class="stat-item">
                    <i class="fas fa-users"></i>
                    <span>${survey.participationCount}/${survey.targetCount}</span>
                </div>
                <div class="stat-item">
                    <i class="fas fa-question-circle"></i>
                    <span>${survey.questions.length}개 질문</span>
                </div>
                <div class="stat-item">
                    <i class="fas fa-clock"></i>
                    <span>${survey.timeLimit ? survey.timeLimit + '분' : '제한없음'}</span>
                </div>
            </div>
            <div class="card-actions">
                <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); participateSurvey('${survey.id}')">
                    <i class="fas fa-play"></i> 참여하기
                </button>
                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation(); showSurveyResults('${survey.id}')">
                    <i class="fas fa-chart-bar"></i> 결과보기
                </button>
            </div>
        </div>
    `;

    return card;
}

// 퀴즈 카드 생성
function createQuizCard(quiz) {
    const card = document.createElement('div');
    card.className = 'quiz-card';
    card.onclick = () => showQuizDetail(quiz.id);

    const statusClass = quiz.status.toLowerCase();
    const statusText = {
        'active': '진행중',
        'completed': '완료',
        'draft': '초안'
    }[quiz.status.toLowerCase()] || quiz.status;

    card.innerHTML = `
        <div class="card-header">
            <div class="card-status status-${statusClass}">${statusText}</div>
            <h3>${quiz.title}</h3>
            <p>${quiz.description}</p>
        </div>
        <div class="card-body">
            <div class="card-meta">
                <span><i class="fas fa-user"></i> ${quiz.author}</span>
                <span><i class="fas fa-calendar"></i> ${formatDate(quiz.createdAt)}</span>
            </div>
            <div class="card-stats">
                <div class="stat-item">
                    <i class="fas fa-users"></i>
                    <span>${quiz.participationCount}/${quiz.targetCount}</span>
                </div>
                <div class="stat-item">
                    <i class="fas fa-question-circle"></i>
                    <span>${quiz.questions.length}개 질문</span>
                </div>
                <div class="stat-item">
                    <i class="fas fa-clock"></i>
                    <span>${quiz.timeLimit}분</span>
                </div>
            </div>
            <div class="card-actions">
                <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); participateQuiz('${quiz.id}')">
                    <i class="fas fa-play"></i> 퀴즈 시작
                </button>
                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation(); showQuizResults('${quiz.id}')">
                    <i class="fas fa-chart-bar"></i> 결과보기
                </button>
            </div>
        </div>
    `;

    return card;
}

// 통계 업데이트
function updateStatistics() {
    const stats = dummyData.statistics;
    
    document.getElementById('total-users').textContent = stats.totalUsers.toLocaleString();
    document.getElementById('total-surveys').textContent = stats.totalSurveys.toLocaleString();
    document.getElementById('total-quizzes').textContent = stats.totalQuizzes.toLocaleString();
    document.getElementById('participation-rate').textContent = stats.averageParticipationRate + '%';
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// 설문 필터링
function filterSurveys() {
    const filter = document.getElementById('survey-filter').value;
    const surveys = dummyData.surveys;
    let filteredSurveys = surveys;

    if (filter !== 'all') {
        filteredSurveys = surveys.filter(survey => survey.status.toLowerCase() === filter);
    }

    const surveysGrid = document.getElementById('surveys-grid');
    surveysGrid.innerHTML = '';
    
    filteredSurveys.forEach(survey => {
        const surveyCard = createSurveyCard(survey);
        surveysGrid.appendChild(surveyCard);
    });
}

// 설문 참여
function participateSurvey(surveyId) {
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
    }

    const survey = dummyData.surveys.find(s => s.id === surveyId);
    if (!survey) {
        showToast('설문을 찾을 수 없습니다.', 'error');
        return;
    }

    if (survey.status !== 'ACTIVE') {
        showToast('현재 참여할 수 없는 설문입니다.', 'warning');
        return;
    }

    // 현재 설문 ID 설정
    currentSurveyId = surveyId;
    
    // 설문 참여 모달 표시
    showSurveyParticipationModal(survey);
}

// 퀴즈 참여
function participateQuiz(quizId) {
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        showLoginModal();
        return;
    }

    const quiz = dummyData.quizzes.find(q => q.id === quizId);
    if (!quiz) {
        showToast('퀴즈를 찾을 수 없습니다.', 'error');
        return;
    }

    if (quiz.status !== 'ACTIVE') {
        showToast('현재 참여할 수 없는 퀴즈입니다.', 'warning');
        return;
    }

    // 현재 퀴즈 ID 설정
    currentQuizId = quizId;
    
    // 퀴즈 참여 모달 표시
    showQuizParticipationModal(quiz);
}

// 설문 결과 보기
function showSurveyResults(surveyId) {
    const survey = dummyData.surveys.find(s => s.id === surveyId);
    const result = dummyData.surveyResults[surveyId];
    
    if (!survey || !result) {
        showToast('설문 결과를 찾을 수 없습니다.', 'error');
        return;
    }

    // 모달 제목 설정
    document.getElementById('survey-result-title').textContent = `${survey.title} - 결과`;
    
    // 요약 정보 업데이트
    document.getElementById('total-participants').textContent = result.totalParticipants.toLocaleString();
    document.getElementById('completion-rate').textContent = result.completionRate.toFixed(1) + '%';
    document.getElementById('avg-time').textContent = result.averageTime.toFixed(1) + '분';
    
    // 질문별 결과 렌더링
    renderSurveyQuestionResults(result.questionResults);
    
    // 모달 표시
    showModal('survey-result-modal');
}

// 퀴즈 결과 보기
function showQuizResults(quizId) {
    const quiz = dummyData.quizzes.find(q => q.id === quizId);
    const result = dummyData.quizResults[quizId];
    
    if (!quiz || !result) {
        showToast('퀴즈 결과를 찾을 수 없습니다.', 'error');
        return;
    }

    // 모달 제목 설정
    document.getElementById('quiz-result-title').textContent = `${quiz.title} - 결과`;
    
    // 요약 정보 업데이트
    document.getElementById('quiz-total-participants').textContent = result.totalParticipants.toLocaleString();
    document.getElementById('quiz-avg-score').textContent = result.averageScore.toFixed(1) + '점';
    document.getElementById('quiz-pass-rate').textContent = result.passRate.toFixed(1) + '%';
    
    // 점수 분포 차트 렌더링
    renderScoreDistributionChart(result.scoreDistribution);
    
    // 질문별 결과 렌더링
    renderQuizQuestionResults(result.questionResults);
    
    // 모달 표시
    showModal('quiz-result-modal');
}

// 설문 질문 결과 렌더링
function renderSurveyQuestionResults(questionResults) {
    const container = document.getElementById('result-questions');
    container.innerHTML = '';
    
    questionResults.forEach((question, index) => {
        const questionDiv = document.createElement('div');
        questionDiv.className = 'result-question';
        
        if (question.questionType === 'MULTIPLE_CHOICE') {
            questionDiv.innerHTML = `
                <div class="question-header">
                    <h4>Q${index + 1}. ${question.questionContent}</h4>
                    <div class="question-stats">
                        <span class="response-count">응답: ${question.totalResponses}명</span>
                    </div>
                </div>
                <div class="question-chart">
                    ${question.optionResults.map(option => `
                        <div class="chart-bar">
                            <div class="bar-label">
                                <span class="option-text">${option.content}</span>
                                <span class="option-percentage">${option.percentage.toFixed(1)}%</span>
                            </div>
                            <div class="bar-container">
                                <div class="bar-fill" style="width: ${option.percentage}%"></div>
                            </div>
                            <div class="bar-count">${option.count}명</div>
                        </div>
                    `).join('')}
                </div>
            `;
        } else if (question.questionType === 'ESSAY') {
            questionDiv.innerHTML = `
                <div class="question-header">
                    <h4>Q${index + 1}. ${question.questionContent}</h4>
                    <div class="question-stats">
                        <span class="response-count">응답: ${question.totalResponses}명</span>
                    </div>
                </div>
                <div class="essay-responses">
                    <h5>주요 응답 내용:</h5>
                    <div class="response-list">
                        ${question.textResponses.map(response => `
                            <div class="response-item">
                                <i class="fas fa-quote-left"></i>
                                <p>"${response}"</p>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }
        
        container.appendChild(questionDiv);
    });
}

// 퀴즈 질문 결과 렌더링
function renderQuizQuestionResults(questionResults) {
    const container = document.getElementById('quiz-result-questions');
    container.innerHTML = '';
    
    questionResults.forEach((question, index) => {
        const questionDiv = document.createElement('div');
        questionDiv.className = 'result-question';
        
        questionDiv.innerHTML = `
            <div class="question-header">
                <h4>Q${index + 1}. ${question.questionContent}</h4>
                <div class="question-stats">
                    <span class="response-count">응답: ${question.totalResponses}명</span>
                    <span class="correct-rate">정답률: ${question.correctRate.toFixed(1)}%</span>
                </div>
            </div>
            <div class="question-chart">
                ${question.optionResults.map(option => `
                    <div class="chart-bar ${option.isCorrect ? 'correct-option' : ''}">
                        <div class="bar-label">
                            <span class="option-text">${option.content}</span>
                            <span class="option-percentage">${option.percentage.toFixed(1)}%</span>
                            ${option.isCorrect ? '<span class="correct-badge">정답</span>' : ''}
                        </div>
                        <div class="bar-container">
                            <div class="bar-fill" style="width: ${option.percentage}%"></div>
                        </div>
                        <div class="bar-count">${option.count}명</div>
                    </div>
                `).join('')}
            </div>
        `;
        
        container.appendChild(questionDiv);
    });
}

// 점수 분포 차트 렌더링
function renderScoreDistributionChart(scoreDistribution) {
    const container = document.getElementById('score-chart');
    container.innerHTML = '';
    
    const maxCount = Math.max(...scoreDistribution.map(item => item.count));
    
    scoreDistribution.forEach(item => {
        const barDiv = document.createElement('div');
        barDiv.className = 'score-bar';
        
        const height = (item.count / maxCount) * 100;
        
        barDiv.innerHTML = `
            <div class="score-bar-container">
                <div class="score-bar-fill" style="height: ${height}%"></div>
            </div>
            <div class="score-bar-label">
                <span class="score-range">${item.score}</span>
                <span class="score-count">${item.count}명</span>
                <span class="score-percentage">${item.percentage.toFixed(1)}%</span>
            </div>
        `;
        
        container.appendChild(barDiv);
    });
}

// 설문 참여 모달 표시
function showSurveyParticipationModal(survey) {
    // 모달 제목과 정보 설정
    document.getElementById('survey-participation-title').textContent = survey.title;
    document.getElementById('participation-survey-title').textContent = survey.title;
    document.getElementById('participation-survey-description').textContent = survey.description;
    document.getElementById('participation-author').textContent = survey.author;
    document.getElementById('participation-time-limit').textContent = survey.timeLimit ? `${survey.timeLimit}분` : '제한없음';
    
    // 질문들 렌더링
    renderSurveyQuestions(survey.questions);
    
    // 모달 표시
    showModal('survey-participation-modal');
}

// 퀴즈 참여 모달 표시
function showQuizParticipationModal(quiz) {
    // 모달 제목과 정보 설정
    document.getElementById('quiz-participation-title').textContent = quiz.title;
    document.getElementById('participation-quiz-title').textContent = quiz.title;
    document.getElementById('participation-quiz-description').textContent = quiz.description;
    document.getElementById('participation-quiz-author').textContent = quiz.author;
    document.getElementById('participation-quiz-time-limit').textContent = `${quiz.timeLimit}분`;
    document.getElementById('participation-pass-score').textContent = '70점 이상';
    
    // 타이머 설정
    if (quiz.timeLimit) {
        document.getElementById('quiz-timer').style.display = 'block';
        startQuizTimer(quiz.timeLimit);
    }
    
    // 질문들 렌더링
    renderQuizQuestions(quiz.questions);
    
    // 모달 표시
    showModal('quiz-participation-modal');
}

// 설문 질문 렌더링
function renderSurveyQuestions(questions) {
    const container = document.getElementById('participation-questions');
    container.innerHTML = '';
    
    questions.forEach((question, index) => {
        const questionDiv = document.createElement('div');
        questionDiv.className = 'participation-question';
        
        if (question.type === 'MULTIPLE_CHOICE') {
            questionDiv.innerHTML = `
                <div class="question-header">
                    <h4>Q${index + 1}. ${question.content}</h4>
                    ${question.isRequired ? '<span class="required-badge">필수</span>' : ''}
                </div>
                <div class="question-options">
                    ${question.options.map((option, optionIndex) => `
                        <label class="option-label">
                            <input type="radio" name="question-${question.id}" value="${option.id}" ${question.isRequired ? 'required' : ''}>
                            <span class="option-text">${option.content}</span>
                        </label>
                    `).join('')}
                </div>
            `;
        } else if (question.type === 'ESSAY') {
            questionDiv.innerHTML = `
                <div class="question-header">
                    <h4>Q${index + 1}. ${question.content}</h4>
                    ${question.isRequired ? '<span class="required-badge">필수</span>' : ''}
                </div>
                <div class="question-input">
                    <textarea name="question-${question.id}" placeholder="답변을 입력해주세요..." ${question.isRequired ? 'required' : ''} rows="4"></textarea>
                </div>
            `;
        }
        
        container.appendChild(questionDiv);
    });
}

// 퀴즈 질문 렌더링
function renderQuizQuestions(questions) {
    const container = document.getElementById('quiz-participation-questions');
    container.innerHTML = '';
    
    questions.forEach((question, index) => {
        const questionDiv = document.createElement('div');
        questionDiv.className = 'participation-question';
        
        questionDiv.innerHTML = `
            <div class="question-header">
                <h4>Q${index + 1}. ${question.content}</h4>
                <span class="required-badge">필수</span>
            </div>
            <div class="question-options">
                ${question.options.map((option, optionIndex) => `
                    <label class="option-label">
                        <input type="radio" name="question-${question.id}" value="${option.id}" required>
                        <span class="option-text">${option.content}</span>
                    </label>
                `).join('')}
            </div>
        `;
        
        container.appendChild(questionDiv);
    });
}

// 설문 제출 처리
function handleSurveySubmission(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const answers = [];
    
    // 답변 수집
    document.querySelectorAll('input[name^="question-"]:checked, textarea[name^="question-"]').forEach(input => {
        const questionId = input.name.replace('question-', '');
        const answer = input.type === 'radio' ? input.value : input.value;
        answers.push({
            questionId: questionId,
            answer: input.type === 'radio' ? answer : null,
            textAnswer: input.type === 'radio' ? null : answer
        });
    });
    
    // 필수 질문 확인
    const requiredQuestions = document.querySelectorAll('input[required], textarea[required]');
    let missingRequired = false;
    
    requiredQuestions.forEach(input => {
        if (input.type === 'radio') {
            const questionName = input.name;
            const isAnswered = document.querySelector(`input[name="${questionName}"]:checked`);
            if (!isAnswered) {
                missingRequired = true;
            }
        } else if (input.type === 'textarea' && !input.value.trim()) {
            missingRequired = true;
        }
    });
    
    if (missingRequired) {
        showToast('필수 질문에 답변해주세요.', 'warning');
        return;
    }
    
    // 로딩 표시
    showLoading();
    
    // 설문 제출 처리
    setTimeout(() => {
        hideLoading();
        
        // 참여 데이터 저장 (실제로는 서버에 전송)
        const participation = {
            id: 'part-' + Date.now(),
            surveyId: currentSurveyId,
            userId: currentUser.id,
            status: 'COMPLETED',
            startedAt: new Date().toISOString(),
            completedAt: new Date().toISOString(),
            answers: answers
        };
        
        // 더미 데이터에 추가
        dummyData.participations.push(participation);
        
        // 참여자 리워드 당첨 처리 (설문별 리워드 설정 확인)
        const survey = dummyData.surveys.find(s => s.id === currentSurveyId);
        let rewardResult = null;
        
        if (survey && survey.participantReward && survey.participantReward.enabled) {
            // 설문별 리워드 설정에 따른 당첨 처리
            rewardResult = processParticipantReward(currentSurveyId, currentUser.id, survey.participantReward);
        }
        
        closeModal('survey-participation-modal');
        
        if (rewardResult) {
            // 당첨된 경우 리워드 모달 표시
            console.log('🎉 리워드 당첨!', rewardResult);
            showRewardModal(rewardResult.winner, rewardResult.reward);
        } else {
            // 미당첨인 경우 일반 완료 메시지
            console.log('😢 리워드 미당첨');
            showToast('설문에 참여해주셔서 감사합니다!', 'success');
        }
        
        // 설문조사 섹션으로 이동
        showSection('surveys');
    }, 2000);
}

// 퀴즈 제출 처리
function handleQuizSubmission(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const answers = [];
    let score = 0;
    let totalQuestions = 0;
    
    // 해당 퀴즈 찾기
    const quiz = dummyData.quizzes.find(q => q.id === currentQuizId);
    if (!quiz) {
        showToast('퀴즈를 찾을 수 없습니다.', 'error');
        return;
    }
    
    // 답변 수집 및 채점
    document.querySelectorAll('input[name^="question-"]:checked').forEach(input => {
        const questionId = input.name.replace('question-', '');
        const selectedOptionId = input.value;
        
        // 해당 질문 찾기
        const question = quiz.questions.find(q => q.id === questionId);
        if (!question || !question.options) {
            console.error('Question not found or has no options:', questionId);
            return;
        }
        
        const selectedOption = question.options.find(o => o.id === selectedOptionId);
        
        totalQuestions++;
        if (selectedOption && selectedOption.isCorrect) {
            score++;
        }
        
        answers.push({
            questionId: questionId,
            answer: selectedOptionId,
            isCorrect: selectedOption && selectedOption.isCorrect
        });
    });
    
    if (totalQuestions === 0) {
        showToast('답변을 선택해주세요.', 'warning');
        return;
    }
    
    const finalScore = Math.round((score / totalQuestions) * 100);
    const isPassed = finalScore >= 70;
    
    // 로딩 표시
    showLoading();
    
    // 퀴즈 제출 처리
    setTimeout(() => {
        hideLoading();
        
        // 참여 데이터 저장
        const participation = {
            id: 'part-' + Date.now(),
            quizId: currentQuizId,
            userId: currentUser.id,
            status: 'COMPLETED',
            score: finalScore,
            isPassed: isPassed,
            startedAt: new Date().toISOString(),
            completedAt: new Date().toISOString(),
            answers: answers
        };
        
        // 더미 데이터에 추가
        dummyData.participations.push(participation);
        
        closeModal('quiz-participation-modal');
        
        // 리워드 처리 (퀴즈 참여자 리워드)
        if (quiz.participantReward && quiz.participantReward.enabled) {
            const rewardResult = processParticipantReward(currentQuizId, currentUser.id, quiz.participantReward);
            if (rewardResult) {
                showRewardModal(rewardResult.winner, rewardResult.reward);
                showCelebrationEffect();
            }
        }
        
        // 결과 표시
        if (isPassed) {
            showToast(`축하합니다! ${finalScore}점으로 합격하셨습니다!`, 'success');
        } else {
            showToast(`아쉽게도 ${finalScore}점으로 불합격입니다. 다시 도전해보세요!`, 'warning');
        }
        
        // 퀴즈 섹션으로 이동
        showSection('quizzes');
    }, 2000);
}

// 퀴즈 타이머 시작
let quizTimer = null;

function startQuizTimer(minutes) {
    // 기존 타이머가 있다면 정리
    if (quizTimer) {
        clearInterval(quizTimer);
    }
    
    let timeLeft = minutes * 60; // 초 단위로 변환
    
    const timerElement = document.getElementById('time-remaining');
    const timerContainer = document.getElementById('quiz-timer');
    if (!timerElement || !timerContainer) return;
    
    quizTimer = setInterval(() => {
        const minutesLeft = Math.floor(timeLeft / 60);
        const secondsLeft = timeLeft % 60;
        
        timerElement.textContent = `${minutesLeft}:${secondsLeft.toString().padStart(2, '0')}`;
        
        // 시간이 5분 이하일 때 경고 스타일 적용
        if (timeLeft <= 300) { // 5분 = 300초
            timerElement.style.color = '#ff4757';
            timerContainer.classList.add('warning');
        } else {
            timerElement.style.color = '';
            timerContainer.classList.remove('warning');
        }
        
        if (timeLeft <= 0) {
            clearInterval(quizTimer);
            quizTimer = null;
            showToast('시간이 종료되었습니다. 자동으로 제출됩니다.', 'warning');
            // 자동 제출 로직
            const form = document.getElementById('quiz-participation-form');
            if (form) {
                form.dispatchEvent(new Event('submit'));
            }
        }
        
        timeLeft--;
    }, 1000);
}

// 타이머 정리 함수
function clearQuizTimer() {
    if (quizTimer) {
        clearInterval(quizTimer);
        quizTimer = null;
    }
}

// 전역 변수 (현재 설문/퀴즈 ID)
let currentSurveyId = null;
let currentQuizId = null;

// 설문 결과 내보내기
function exportSurveyResults() {
    showToast('설문 결과를 CSV 파일로 내보내는 중...', 'info');
    // TODO: 실제 CSV 내보내기 기능 구현
    setTimeout(() => {
        showToast('설문 결과가 성공적으로 내보내졌습니다.', 'success');
    }, 2000);
}

// 퀴즈 결과 내보내기
function exportQuizResults() {
    showToast('퀴즈 결과를 CSV 파일로 내보내는 중...', 'info');
    // TODO: 실제 CSV 내보내기 기능 구현
    setTimeout(() => {
        showToast('퀴즈 결과가 성공적으로 내보내졌습니다.', 'success');
    }, 2000);
}

// 설문 상세 보기
function showSurveyDetail(surveyId) {
    const survey = dummyData.surveys.find(s => s.id === surveyId);
    if (survey) {
        showToast(`${survey.title} 상세 정보를 불러오는 중...`, 'info');
        // TODO: 설문 상세 페이지 구현
    }
}

// 퀴즈 상세 보기
function showQuizDetail(quizId) {
    const quiz = dummyData.quizzes.find(q => q.id === quizId);
    if (quiz) {
        showToast(`${quiz.title} 상세 정보를 불러오는 중...`, 'info');
        // TODO: 퀴즈 상세 페이지 구현
    }
}

// ==================== 관리자 기능 ====================

// 사용자 관리 모달 표시
function showUserManagement() {
    if (!isLoggedIn || !currentUser || !currentUser.roles.includes('ADMIN')) {
        showToast('관리자 권한이 필요합니다.', 'error');
        return;
    }
    
    loadUsersTable();
    showModal('user-management-modal');
}

// 사용자 테이블 로드
function loadUsersTable() {
    const tbody = document.getElementById('users-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    dummyData.adminUsers.forEach(user => {
        const row = document.createElement('tr');
        
        const userInfo = `
            <div class="user-info">
                <div class="user-avatar">
                    ${(user.firstName || user.username).charAt(0).toUpperCase()}
                </div>
                <div class="user-details">
                    <h4>${user.firstName || ''} ${user.lastName || ''}</h4>
                    <p>@${user.username}</p>
                </div>
            </div>
        `;
        
        const statusClass = user.accountStatus.toLowerCase();
        const statusText = {
            'active': '활성',
            'suspended': '정지',
            'pending': '대기'
        }[statusClass] || user.accountStatus;
        
        const roleText = user.roles.includes('ADMIN') ? '관리자' : '사용자';
        const roleClass = user.roles.includes('ADMIN') ? 'role-admin' : '';
        
        const lastLogin = user.lastLoginAt ? formatDate(user.lastLoginAt) : '없음';
        const twoFAStatus = user.twoFactorEnabled ? '활성' : '비활성';
        
        row.innerHTML = `
            <td>${userInfo}</td>
            <td>${user.email}</td>
            <td><span class="role-badge ${roleClass}">${roleText}</span></td>
            <td><span class="status-badge status-${statusClass}">${statusText}</span></td>
            <td>${formatDate(user.createdAt)}</td>
            <td>${lastLogin}</td>
            <td>${twoFAStatus}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn edit" onclick="editUser('${user.id}')">
                        <i class="fas fa-edit"></i>
                    </button>
                    ${user.accountStatus === 'ACTIVE' ? 
                        `<button class="action-btn suspend" onclick="suspendUser('${user.id}')">
                            <i class="fas fa-ban"></i>
                        </button>` :
                        `<button class="action-btn activate" onclick="activateUser('${user.id}')">
                            <i class="fas fa-check"></i>
                        </button>`
                    }
                    <button class="action-btn delete" onclick="deleteUser('${user.id}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

// 사용자 필터링
function filterUsers() {
    const searchTerm = document.getElementById('user-search').value.toLowerCase();
    const statusFilter = document.getElementById('user-status-filter').value;
    const roleFilter = document.getElementById('user-role-filter').value;
    
    const tbody = document.getElementById('users-table-body');
    const rows = tbody.querySelectorAll('tr');
    
    rows.forEach(row => {
        const userInfo = row.querySelector('.user-details h4').textContent.toLowerCase();
        const email = row.cells[1].textContent.toLowerCase();
        const status = row.querySelector('.status-badge').textContent;
        const role = row.querySelector('.role-badge').textContent;
        
        const matchesSearch = userInfo.includes(searchTerm) || email.includes(searchTerm);
        const matchesStatus = statusFilter === 'all' || status.includes(statusFilter);
        const matchesRole = roleFilter === 'all' || role.includes(roleFilter);
        
        if (matchesSearch && matchesStatus && matchesRole) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// 사용자 추가 모달 표시
function showAddUserModal() {
    document.getElementById('add-user-title').textContent = '사용자 추가';
    document.getElementById('add-user-form').reset();
    showModal('add-user-modal');
}

// 사용자 편집
function editUser(userId) {
    const user = dummyData.adminUsers.find(u => u.id === userId);
    if (!user) return;
    
    document.getElementById('add-user-title').textContent = '사용자 편집';
    document.getElementById('add-user-email').value = user.email;
    document.getElementById('add-user-username').value = user.username;
    document.getElementById('add-user-firstname').value = user.firstName || '';
    document.getElementById('add-user-lastname').value = user.lastName || '';
    document.getElementById('add-user-phone').value = user.phone || '';
    document.getElementById('add-user-status').value = user.accountStatus;
    
    // 권한 설정
    const rolesSelect = document.getElementById('add-user-roles');
    Array.from(rolesSelect.options).forEach(option => {
        option.selected = user.roles.includes(option.value);
    });
    
    showModal('add-user-modal');
}

// 사용자 추가/편집 처리
function handleAddUser(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const email = formData.get('email');
    const username = formData.get('username');
    const firstName = formData.get('firstName');
    const lastName = formData.get('lastName');
    const phone = formData.get('phone');
    const status = formData.get('status');
    const roles = Array.from(formData.getAll('roles'));
    
    // 로딩 표시
    showLoading();
    
    setTimeout(() => {
        hideLoading();
        
        // 새 사용자 생성 또는 기존 사용자 업데이트
        const isEdit = document.getElementById('add-user-title').textContent === '사용자 편집';
        
        if (isEdit) {
            // 기존 사용자 업데이트
            const existingUser = dummyData.adminUsers.find(u => u.email === email);
            if (existingUser) {
                existingUser.firstName = firstName;
                existingUser.lastName = lastName;
                existingUser.phone = phone;
                existingUser.accountStatus = status;
                existingUser.roles = roles;
            }
            showToast('사용자 정보가 업데이트되었습니다.', 'success');
        } else {
            // 새 사용자 추가
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
                accountStatus: status,
                roles: roles,
            createdAt: new Date().toISOString(),
                lastLoginAt: null,
                loginCount: 0,
                lastActivityAt: null
            };
            
            dummyData.adminUsers.push(newUser);
            showToast('새 사용자가 추가되었습니다.', 'success');
        }
        
        closeModal('add-user-modal');
        loadUsersTable();
    }, 1500);
}

// 사용자 정지
function suspendUser(userId) {
    if (confirm('이 사용자를 정지하시겠습니까?')) {
        const user = dummyData.adminUsers.find(u => u.id === userId);
        if (user) {
            user.accountStatus = 'SUSPENDED';
            loadUsersTable();
            showToast('사용자가 정지되었습니다.', 'warning');
        }
    }
}

// 사용자 활성화
function activateUser(userId) {
    if (confirm('이 사용자를 활성화하시겠습니까?')) {
        const user = dummyData.adminUsers.find(u => u.id === userId);
        if (user) {
            user.accountStatus = 'ACTIVE';
            loadUsersTable();
            showToast('사용자가 활성화되었습니다.', 'success');
        }
    }
}

// 사용자 삭제
function deleteUser(userId) {
    if (confirm('이 사용자를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        const userIndex = dummyData.adminUsers.findIndex(u => u.id === userId);
        if (userIndex > -1) {
            dummyData.adminUsers.splice(userIndex, 1);
            loadUsersTable();
            showToast('사용자가 삭제되었습니다.', 'success');
        }
    }
}

// 시스템 통계 모달 표시
function showSystemStats() {
    if (!isLoggedIn || !currentUser || !currentUser.roles.includes('ADMIN')) {
        showToast('관리자 권한이 필요합니다.', 'error');
        return;
    }
    
    loadSystemStats();
    showModal('system-stats-modal');
}

// 시스템 통계 로드
function loadSystemStats() {
    const stats = dummyData.adminStatistics;
    
    // 기본 통계 업데이트
    document.getElementById('admin-total-users').textContent = dummyData.adminUsers.length.toLocaleString();
    document.getElementById('admin-active-users').textContent = 
        dummyData.adminUsers.filter(u => u.accountStatus === 'ACTIVE').length.toLocaleString();
    document.getElementById('admin-total-surveys').textContent = dummyData.surveys.length.toLocaleString();
    document.getElementById('admin-total-quizzes').textContent = dummyData.quizzes.length.toLocaleString();
    
    // 참여율 통계
    document.getElementById('avg-survey-participation').textContent = stats.participationRates.avgSurveyParticipation + '%';
    document.getElementById('avg-quiz-participation').textContent = stats.participationRates.avgQuizParticipation + '%';
    document.getElementById('completion-rate').textContent = stats.participationRates.completionRate + '%';
    
    // 차트 렌더링 (간단한 텍스트 기반)
    renderUserGrowthChart(stats.userGrowth);
    renderSurveyQuizChart(stats.surveyQuizStats);
}

// 사용자 성장 차트 렌더링
function renderUserGrowthChart(userGrowth) {
    const canvas = document.getElementById('user-growth-chart');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;
    
    // 캔버스 초기화
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = '#f8f9fa';
    ctx.fillRect(0, 0, width, height);
    
    // 간단한 차트 그리기
    const maxUsers = Math.max(...userGrowth.map(d => d.users));
    const stepX = width / (userGrowth.length - 1);
    
    ctx.strokeStyle = '#667eea';
    ctx.lineWidth = 3;
    ctx.beginPath();
    
    userGrowth.forEach((point, index) => {
        const x = index * stepX;
        const y = height - (point.users / maxUsers) * height;
        
        if (index === 0) {
            ctx.moveTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
    });
    
    ctx.stroke();
    
    // 점 표시
    ctx.fillStyle = '#667eea';
    userGrowth.forEach((point, index) => {
        const x = index * stepX;
        const y = height - (point.users / maxUsers) * height;
        ctx.beginPath();
        ctx.arc(x, y, 4, 0, 2 * Math.PI);
        ctx.fill();
    });
}

// 설문/퀴즈 차트 렌더링
function renderSurveyQuizChart(stats) {
    const canvas = document.getElementById('survey-quiz-chart');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;
    
    // 캔버스 초기화
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = '#f8f9fa';
    ctx.fillRect(0, 0, width, height);
    
    // 막대 차트 그리기
    const barWidth = width / stats.length / 2;
    const maxCount = Math.max(...stats.map(s => s.count));
    
    stats.forEach((stat, index) => {
        const barHeight = (stat.count / maxCount) * height * 0.8;
        const x = index * (width / stats.length) + width / stats.length / 4;
        const y = height - barHeight;
        
        // 막대 그리기
        ctx.fillStyle = index === 0 ? '#667eea' : '#764ba2';
        ctx.fillRect(x, y, barWidth, barHeight);
        
        // 라벨 그리기
        ctx.fillStyle = '#333';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(stat.type, x + barWidth / 2, height - 5);
        ctx.fillText(stat.count.toString(), x + barWidth / 2, y - 5);
    });
}

// 보안 로그 모달 표시
function showSecurityLogs() {
    if (!isLoggedIn || !currentUser || !currentUser.roles.includes('ADMIN')) {
        showToast('관리자 권한이 필요합니다.', 'error');
        return;
    }
    
    loadSecurityLogs();
    showModal('security-logs-modal');
}

// 보안 로그 테이블 로드
function loadSecurityLogs() {
    const tbody = document.getElementById('security-logs-table-body');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    dummyData.securityLogs.forEach(log => {
        const row = document.createElement('tr');
        
        const user = dummyData.adminUsers.find(u => u.id === log.userId);
        const userName = user ? `${user.firstName || ''} ${user.lastName || ''}` : '알 수 없음';
        
        const eventText = {
            'LOGIN_SUCCESS': '로그인 성공',
            'LOGIN_FAILED': '로그인 실패',
            'PASSWORD_CHANGE': '비밀번호 변경',
            'ACCOUNT_SUSPENDED': '계정 정지',
            'SUSPICIOUS_ACTIVITY': '의심스러운 활동'
        }[log.eventType] || log.eventType;
        
        const statusClass = log.success ? 'success' : 'failed';
        const statusText = log.success ? '성공' : '실패';
        
        const eventClass = `event-${log.eventType.toLowerCase().replace('_', '-')}`;
        
        row.innerHTML = `
            <td>${formatDateTime(log.createdAt)}</td>
            <td>
                <div class="log-entry">
                    <div class="log-icon ${statusClass}">
                        <i class="fas fa-${log.success ? 'check' : 'times'}"></i>
                    </div>
                    <div class="log-details">
                        <h4>${userName}</h4>
                        <p>${user ? user.email : '알 수 없음'}</p>
                    </div>
                </div>
            </td>
            <td><span class="event-badge ${eventClass}">${eventText}</span></td>
            <td><span class="status-badge status-${statusClass}">${statusText}</span></td>
            <td>${log.ipAddress}</td>
            <td>${log.eventDescription}</td>
            <td>
                <button class="action-btn edit" onclick="viewLogDetails('${log.id}')">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

// 로그 필터링
function filterLogs() {
    const searchTerm = document.getElementById('log-search').value.toLowerCase();
    const eventFilter = document.getElementById('log-event-filter').value;
    const statusFilter = document.getElementById('log-status-filter').value;
    const dateFilter = document.getElementById('log-date-filter').value;
    
    const tbody = document.getElementById('security-logs-table-body');
    const rows = tbody.querySelectorAll('tr');
    
    rows.forEach(row => {
        const userInfo = row.cells[1].textContent.toLowerCase();
        const event = row.cells[2].textContent;
        const status = row.cells[3].textContent;
        const date = row.cells[0].textContent;
        
        const matchesSearch = userInfo.includes(searchTerm);
        const matchesEvent = eventFilter === 'all' || event.includes(eventFilter);
        const matchesStatus = statusFilter === 'all' || status.includes(statusFilter);
        const matchesDate = !dateFilter || date.includes(dateFilter);
        
        if (matchesSearch && matchesEvent && matchesStatus && matchesDate) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// 로그 상세 보기
function viewLogDetails(logId) {
    const log = dummyData.securityLogs.find(l => l.id === logId);
    if (log) {
        const user = dummyData.adminUsers.find(u => u.id === log.userId);
        const details = `
            이벤트: ${log.eventDescription}
            사용자: ${user ? user.email : '알 수 없음'}
            IP 주소: ${log.ipAddress}
            시간: ${formatDateTime(log.createdAt)}
            상태: ${log.success ? '성공' : '실패'}
            ${log.failureReason ? `실패 사유: ${log.failureReason}` : ''}
        `;
        
        showToast(details, 'info');
    }
}

// 보안 로그 내보내기
function exportSecurityLogs() {
    showToast('보안 로그를 CSV 파일로 내보내는 중...', 'info');
    setTimeout(() => {
        showToast('보안 로그가 성공적으로 내보내졌습니다.', 'success');
    }, 2000);
}

// 날짜/시간 포맷팅
function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ==================== 리워드 시스템 ====================

// 참여자 리워드 당첨 처리 (설문별 설정)
function processParticipantReward(surveyId, userId, rewardSettings) {
    // 이미 당첨된 사용자인지 확인
    const alreadyWon = dummyData.rewards.winners.some(w => 
        w.userId === userId && w.surveyId === surveyId
    );
    
    if (alreadyWon) {
        return null;
    }
    
    // 랜덤 당첨 여부 결정 (테스트용으로 항상 당첨)
    const randomValue = Math.random();
    const testProbability = 1.0; // 테스트용 100% 확률
    if (randomValue > testProbability) {
        console.log('참여자 리워드 미당첨:', {
            surveyId,
            userId,
            probability: testProbability,
            randomValue
        });
        return null;
    }
    
    console.log('참여자 리워드 당첨!', {
        surveyId,
        userId,
        reward: rewardSettings
    });
    
    // 당첨자 정보 생성
    const winner = {
        id: 'winner-' + Date.now(),
        userId: userId,
        surveyId: surveyId,
        rewardId: 'custom-' + Date.now(),
        wonAt: new Date().toISOString(),
        claimed: false,
        claimCode: generateClaimCode(rewardSettings.type)
    };
    
    // 당첨자 목록에 추가
    dummyData.rewards.winners.push(winner);
    
    // 사용자 리워드 히스토리에 추가
    if (!dummyData.rewards.userRewards[userId]) {
        dummyData.rewards.userRewards[userId] = [];
    }
    
    const userReward = {
        id: winner.id,
        surveyId: surveyId,
        rewardId: winner.rewardId,
        wonAt: winner.wonAt,
        claimed: false,
        claimCode: winner.claimCode
    };
    
    dummyData.rewards.userRewards[userId].push(userReward);
    
    return {
        winner: winner,
        reward: {
            id: winner.rewardId,
            name: rewardSettings.description || `${rewardSettings.type} 리워드`,
            description: rewardSettings.description,
            value: rewardSettings.value,
            type: rewardSettings.type,
            image: rewardSettings.image
        }
    };
}

// 기존 리워드 당첨 처리 (전역 리워드 풀 사용)
function processRewardDraw(surveyId, userId) {
    const settings = dummyData.rewards.eventSettings;
    
    // 이벤트가 비활성화되어 있으면 리턴
    if (!settings.eventActive) {
        return null;
    }
    
    // 이벤트 기간 확인
    const now = new Date();
    const startDate = new Date(settings.startDate);
    const endDate = new Date(settings.endDate);
    
    if (now < startDate || now > endDate) {
        return null;
    }
    
    // 해당 설문의 참여자 수 확인
    const surveyParticipants = dummyData.participations.filter(p => 
        p.surveyId === surveyId && p.status === 'COMPLETED'
    );
    
    // 최소 참여자 수 미달 시 리턴
    if (surveyParticipants.length < settings.participationThreshold) {
        return null;
    }
    
    // 이미 당첨된 사용자인지 확인
    const alreadyWon = dummyData.rewards.winners.some(w => 
        w.userId === userId && w.surveyId === surveyId
    );
    
    if (alreadyWon) {
        return null;
    }
    
    // 현재 설문의 당첨자 수 확인
    const currentWinners = dummyData.rewards.winners.filter(w => w.surveyId === surveyId);
    
    if (currentWinners.length >= settings.maxWinners) {
        return null;
    }
    
    // 랜덤 당첨 확률 계산 (테스트용으로 100% 확률)
    const baseProbability = 1.0; // 100% 확률 (테스트용)
    const participationBonus = 0; // 보너스 없음
    const totalProbability = baseProbability + participationBonus;
    
    console.log('리워드 추첨 정보:', {
        surveyId,
        userId,
        surveyParticipants: surveyParticipants.length,
        baseProbability,
        participationBonus,
        totalProbability,
        randomValue: Math.random()
    });
    
    // 랜덤 당첨 여부 결정
    if (Math.random() > totalProbability) {
        console.log('리워드 미당첨');
        return null;
    }
    
    console.log('리워드 당첨!');
    
    // 리워드 선택 (확률 기반)
    const selectedReward = selectRandomReward();
    if (!selectedReward) {
        return null;
    }
    
    // 당첨자 정보 생성
    const winner = {
        id: 'winner-' + Date.now(),
        userId: userId,
        surveyId: surveyId,
        rewardId: selectedReward.id,
        wonAt: new Date().toISOString(),
        claimed: false,
        claimCode: generateClaimCode(selectedReward.type)
    };
    
    // 당첨자 목록에 추가
    dummyData.rewards.winners.push(winner);
    
    // 사용자 리워드 히스토리에 추가
    if (!dummyData.rewards.userRewards[userId]) {
        dummyData.rewards.userRewards[userId] = [];
    }
    
    dummyData.rewards.userRewards[userId].push({
        rewardId: selectedReward.id,
        surveyId: surveyId,
        wonAt: winner.wonAt,
        claimed: false,
        claimCode: winner.claimCode
    });
    
    return {
        winner: winner,
        reward: selectedReward
    };
}

// 랜덤 리워드 선택
function selectRandomReward() {
    const rewardPool = dummyData.rewards.rewardPool;
    const totalProbability = rewardPool.reduce((sum, reward) => sum + reward.probability, 0);
    
    let random = Math.random() * totalProbability;
    
    for (const reward of rewardPool) {
        random -= reward.probability;
        if (random <= 0) {
            return reward;
        }
    }
    
    return rewardPool[0]; // 기본값
}

// 클레임 코드 생성
function generateClaimCode(rewardType) {
    const prefixes = {
        'GIFTCARD': 'GC',
        'POINTS': 'PT',
        'COUPON': 'CP',
        'CASH': 'CS',
        'DIGITAL': 'DG',
        'PHYSICAL': 'PH'
    };
    
    const prefix = prefixes[rewardType] || 'RW';
    const timestamp = Date.now().toString().slice(-8);
    const random = Math.random().toString(36).substr(2, 4).toUpperCase();
    
    const claimCode = `${prefix}${timestamp}${random}`;
    console.log('🎫 클레임 코드 생성:', claimCode, '타입:', rewardType);
    return claimCode;
}

// 리워드 당첨 모달 표시
function showRewardModal(winner, reward) {
    console.log('🎁 리워드 모달 표시 시작:', { winner, reward });
    
    // 리워드 당첨 모달이 없으면 생성
    let modal = document.getElementById('reward-modal');
    if (!modal) {
        console.log('📝 리워드 모달 생성 중...');
        createRewardModal();
        modal = document.getElementById('reward-modal');
    }
    
    // 모달 내용 업데이트
    const rewardTitle = document.getElementById('reward-title');
    const rewardName = document.getElementById('reward-name');
    const rewardDescription = document.getElementById('reward-description');
    const rewardValue = document.getElementById('reward-value');
    const rewardImage = document.getElementById('reward-image');
    const claimCode = document.getElementById('claim-code');
    
    if (rewardTitle) rewardTitle.textContent = '🎉 축하합니다!';
    if (rewardName) rewardName.textContent = reward.name;
    if (rewardDescription) rewardDescription.textContent = reward.description;
    if (rewardValue) rewardValue.textContent = `${reward.value.toLocaleString()}원`;
    if (rewardImage) rewardImage.src = reward.image;
    if (claimCode) {
        claimCode.value = winner.claimCode;
        claimCode.textContent = winner.claimCode;
        console.log('🎫 클레임 코드 설정:', winner.claimCode);
    }
    
    console.log('🎨 축하 효과 표시 중...');
    
    // 축하 효과 표시
    showCelebrationEffect();
    
    console.log('🪟 리워드 모달 표시 중...');
    // 모달 표시
    showModal('reward-modal');
}

// 리워드 모달 생성
function createRewardModal() {
    const modalHTML = `
        <div id="reward-modal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 id="reward-title">🎉 축하합니다!</h3>
                    <button class="close-btn" onclick="closeModal('reward-modal')">
                        &times;
                    </button>
                </div>
                <div class="modal-body">
                    <div class="reward-content">
                        <div class="reward-image-container">
                            <img id="reward-image" src="" alt="리워드 이미지" class="reward-image">
                        </div>
                        <div class="reward-info">
                            <h4 id="reward-name">리워드 이름</h4>
                            <p id="reward-description">리워드 설명</p>
                            <div class="reward-value">
                                <span class="value-label">상품 가치:</span>
                                <span id="reward-value" class="value-amount">0원</span>
                            </div>
                            <div class="claim-code-container">
                                <label>클레임 코드:</label>
                                <div class="claim-code-wrapper">
                                    <input type="text" id="claim-code" readonly>
                                    <button class="btn btn-sm" onclick="copyClaimCode()">
                                        <i class="fas fa-copy"></i> 복사
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="reward-actions">
                        <button class="btn btn-primary" onclick="claimReward()">
                            <i class="fas fa-gift"></i> 리워드 받기
                        </button>
                        <button class="btn btn-outline" onclick="closeModal('reward-modal')">
                            나중에 받기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 클레임 코드 복사
function copyClaimCode() {
    const claimCodeInput = document.getElementById('claim-code');
    if (claimCodeInput && claimCodeInput.value) {
        claimCodeInput.select();
        claimCodeInput.setSelectionRange(0, 99999); // 모바일에서도 작동하도록
        document.execCommand('copy');
        showToast('클레임 코드가 복사되었습니다!', 'success');
        console.log('📋 클레임 코드 복사:', claimCodeInput.value);
    } else {
        showToast('클레임 코드를 찾을 수 없습니다.', 'error');
        console.log('❌ 클레임 코드 없음');
    }
}

// 리워드 받기 처리
function claimReward() {
    const claimCodeInput = document.getElementById('claim-code');
    const claimCode = claimCodeInput ? claimCodeInput.value || claimCodeInput.textContent : '';
    
    // 실제로는 서버에 클레임 요청을 보내야 함
    showLoading();
    
    setTimeout(() => {
        hideLoading();
        
        // 당첨자 정보 업데이트
        const winner = dummyData.rewards.winners.find(w => w.claimCode === claimCode);
        if (winner) {
            winner.claimed = true;
            winner.claimedAt = new Date().toISOString();
        }
        
        // 사용자 리워드 히스토리 업데이트
        const userId = winner ? winner.userId : currentUser.id;
        if (dummyData.rewards.userRewards[userId]) {
            const userReward = dummyData.rewards.userRewards[userId].find(r => r.claimCode === claimCode);
            if (userReward) {
                userReward.claimed = true;
                userReward.claimedAt = new Date().toISOString();
            }
        }
        
        closeModal('reward-modal');
        showToast('리워드를 성공적으로 받았습니다!', 'success');
    }, 2000);
}

// 축하 효과 표시
function showCelebrationEffect() {
    // 화려한 축하 효과
    createConfetti();
    playCelebrationSound();
    showFireworks();
}

// 컨페티 효과 생성
function createConfetti() {
    const confettiContainer = document.createElement('div');
    confettiContainer.className = 'confetti-container';
    confettiContainer.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
        z-index: 9999;
        overflow: hidden;
    `;
    
    // 다양한 컨페티 생성
    const emojis = ['🎉', '🎊', '✨', '🌟', '💫', '⭐', '🎈', '🎁'];
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F'];
    
    for (let i = 0; i < 50; i++) {
        const confetti = document.createElement('div');
        confetti.innerHTML = emojis[Math.floor(Math.random() * emojis.length)];
        confetti.style.cssText = `
            position: absolute;
            top: -50px;
            left: ${Math.random() * 100}%;
            font-size: ${Math.random() * 20 + 15}px;
            color: ${colors[Math.floor(Math.random() * colors.length)]};
            animation: confetti-fall ${Math.random() * 3 + 2}s ease-out forwards;
            animation-delay: ${Math.random() * 0.5}s;
        `;
        confettiContainer.appendChild(confetti);
    }
    
    document.body.appendChild(confettiContainer);
    
    setTimeout(() => {
        confettiContainer.remove();
    }, 5000);
}

// 축하 사운드 효과 (시각적 피드백)
function playCelebrationSound() {
    // 실제 사운드 대신 시각적 피드백
    const soundEffect = document.createElement('div');
    soundEffect.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 4rem;
        z-index: 10000;
        animation: celebration-bounce 1s ease-out;
    `;
    soundEffect.innerHTML = '🎵';
    document.body.appendChild(soundEffect);
    
    setTimeout(() => {
        soundEffect.remove();
    }, 1000);
}

// 불꽃 효과
function showFireworks() {
    const fireworks = document.createElement('div');
    fireworks.className = 'fireworks';
    fireworks.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
        z-index: 9998;
        background: radial-gradient(circle at 20% 20%, rgba(255, 107, 107, 0.1) 0%, transparent 50%),
                    radial-gradient(circle at 80% 80%, rgba(78, 205, 196, 0.1) 0%, transparent 50%),
                    radial-gradient(circle at 40% 60%, rgba(255, 234, 167, 0.1) 0%, transparent 50%);
        animation: fireworks-glow 2s ease-out;
    `;
    
    document.body.appendChild(fireworks);
    
    setTimeout(() => {
        fireworks.remove();
    }, 2000);
}

// 사용자 리워드 히스토리 조회
function getUserRewards(userId) {
    return dummyData.rewards.userRewards[userId] || [];
}

// 리워드 통계 조회
function getRewardStats() {
    const totalWinners = dummyData.rewards.winners.length;
    const claimedRewards = dummyData.rewards.winners.filter(w => w.claimed).length;
    const totalValue = dummyData.rewards.winners.reduce((sum, winner) => {
        const reward = dummyData.rewards.rewardPool.find(r => r.id === winner.rewardId);
        return sum + (reward ? reward.value : 0);
    }, 0);
        
        return {
        totalWinners,
        claimedRewards,
        pendingRewards: totalWinners - claimedRewards,
        totalValue
    };
}

// 리워드 통계 모달 표시
function showRewardStatsModal() {
    // 리워드 통계 모달이 없으면 생성
    let modal = document.getElementById('reward-stats-modal');
    if (!modal) {
        createRewardStatsModal();
        modal = document.getElementById('reward-stats-modal');
    }
    
    // 통계 데이터 로드
    loadRewardStats();
    
    // 모달 표시
    showModal('reward-stats-modal');
}

// 리워드 통계 모달 생성
function createRewardStatsModal() {
    const modalHTML = `
        <div id="reward-stats-modal" class="modal">
            <div class="modal-content large">
                <div class="modal-header">
                    <h3>📊 리워드 통계</h3>
                    <button class="close-btn" onclick="closeModal('reward-stats-modal')">
                        &times;
                    </button>
                </div>
                <div class="modal-body">
                    <div class="stats-overview">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-trophy"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-winners">0</h3>
                                    <p>총 당첨자</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-check-circle"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="claimed-rewards">0</h3>
                                    <p>수령 완료</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-clock"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="pending-rewards">0</h3>
                                    <p>수령 대기</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-won-sign"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-value">0원</h3>
                                    <p>총 지급액</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="detailed-stats">
                        <div class="stats-section">
                            <h4>리워드 타입별 통계</h4>
                            <div id="reward-type-stats">
                                <!-- 리워드 타입별 통계가 여기에 표시됩니다 -->
                            </div>
                        </div>
                        
                        <div class="stats-section">
                            <h4>최근 당첨자</h4>
                            <div id="recent-winners">
                                <!-- 최근 당첨자 목록이 여기에 표시됩니다 -->
                            </div>
                        </div>
                        
                        <div class="stats-section">
                            <h4>월별 당첨 통계</h4>
                            <div class="chart-container">
                                <canvas id="monthly-winners-chart" width="400" height="200"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 리워드 통계 데이터 로드
function loadRewardStats() {
    const stats = getRewardStats();
    
    // 기본 통계 업데이트
    document.getElementById('total-winners').textContent = stats.totalWinners;
    document.getElementById('claimed-rewards').textContent = stats.claimedRewards;
    document.getElementById('pending-rewards').textContent = stats.pendingRewards;
    document.getElementById('total-value').textContent = stats.totalValue.toLocaleString() + '원';
    
    // 리워드 타입별 통계
    loadRewardTypeStats();
    
    // 최근 당첨자
    loadRecentWinners();
    
    // 월별 차트
    renderMonthlyWinnersChart();
}

// 리워드 타입별 통계
function loadRewardTypeStats() {
    const typeStats = {};
    
    dummyData.rewards.winners.forEach(winner => {
        const reward = dummyData.rewards.rewardPool.find(r => r.id === winner.rewardId);
        if (reward) {
            if (!typeStats[reward.type]) {
                typeStats[reward.type] = {
                    count: 0,
                    totalValue: 0,
                    name: reward.name
                };
            }
            typeStats[reward.type].count++;
            typeStats[reward.type].totalValue += reward.value;
        }
    });
    
    const typeStatsContainer = document.getElementById('reward-type-stats');
    typeStatsContainer.innerHTML = Object.entries(typeStats).map(([type, data]) => `
        <div class="participation-item">
            <div class="label">${data.name}</div>
            <div class="value">${data.count}명 (${data.totalValue.toLocaleString()}원)</div>
        </div>
    `).join('');
}

// 최근 당첨자 목록
function loadRecentWinners() {
    const recentWinners = dummyData.rewards.winners
        .sort((a, b) => new Date(b.wonAt) - new Date(a.wonAt))
        .slice(0, 5);
    
    const recentWinnersContainer = document.getElementById('recent-winners');
    recentWinnersContainer.innerHTML = recentWinners.map(winner => {
        const reward = dummyData.rewards.rewardPool.find(r => r.id === winner.rewardId);
        const user = dummyData.users.find(u => u.id === winner.userId);
        
        return `
            <div class="log-entry">
                <div class="log-icon success">
                    <i class="fas fa-gift"></i>
                </div>
                <div class="log-details">
                    <h4>${user ? user.firstName + ' ' + user.lastName : '사용자'}님이 ${reward ? reward.name : '리워드'}에 당첨!</h4>
                    <p>${formatDateTime(winner.wonAt)} • ${winner.claimed ? '수령완료' : '수령대기'}</p>
                </div>
            </div>
        `;
    }).join('');
}

// 월별 당첨자 차트
function renderMonthlyWinnersChart() {
    const canvas = document.getElementById('monthly-winners-chart');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    const winners = dummyData.rewards.winners;
    
    // 월별 데이터 집계
    const monthlyData = {};
    winners.forEach(winner => {
        const month = new Date(winner.wonAt).toISOString().substr(0, 7); // YYYY-MM
        monthlyData[month] = (monthlyData[month] || 0) + 1;
    });
    
    // 최근 6개월 데이터
    const months = Object.keys(monthlyData).sort().slice(-6);
    const counts = months.map(month => monthlyData[month] || 0);
    
    // 간단한 막대 차트 그리기
    const maxCount = Math.max(...counts, 1);
    const barWidth = canvas.width / months.length;
    const barHeight = canvas.height - 40;
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    months.forEach((month, index) => {
        const height = (counts[index] / maxCount) * barHeight;
        const x = index * barWidth + 10;
        const y = canvas.height - height - 20;
        
        // 막대 그리기
        ctx.fillStyle = '#4CAF50';
        ctx.fillRect(x, y, barWidth - 20, height);
        
        // 월 라벨
        ctx.fillStyle = '#666';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(month.substr(5), x + barWidth/2, canvas.height - 5);
        
        // 수치 라벨
        ctx.fillStyle = '#333';
        ctx.font = 'bold 14px Arial';
        ctx.fillText(counts[index], x + barWidth/2, y - 5);
    });
}

// 사용자 리워드 히스토리 표시
function loadUserRewards(userId) {
    const userRewards = getUserRewards(userId);
    const rewardsList = document.getElementById('user-rewards-list');
    const rewardsSection = document.getElementById('reward-history-section');
    
    if (!rewardsList || !rewardsSection) return;
    
    if (userRewards.length === 0) {
        rewardsList.innerHTML = `
            <div class="text-center" style="padding: 2rem; color: #666;">
                <i class="fas fa-gift" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.3;"></i>
                <p>아직 받은 리워드가 없습니다.</p>
                <p>설문조사에 참여하여 리워드를 받아보세요! 🎁</p>
            </div>
        `;
    } else {
        rewardsList.innerHTML = userRewards.map(reward => {
            const rewardInfo = dummyData.rewards.rewardPool.find(r => r.id === reward.rewardId);
            if (!rewardInfo) return '';
            
            return `
                <div class="reward-item">
                    <img src="${rewardInfo.image}" alt="${rewardInfo.name}" class="reward-item-image">
                    <div class="reward-item-info">
                        <h5>${rewardInfo.name}</h5>
                        <p>${rewardInfo.description}</p>
                        <p><strong>가치:</strong> ${rewardInfo.value.toLocaleString()}원</p>
                        <p><strong>당첨일:</strong> ${formatDateTime(reward.wonAt)}</p>
                    </div>
                    <div class="reward-item-status">
                        <span class="status-badge ${reward.claimed ? 'status-claimed' : 'status-pending'}">
                            ${reward.claimed ? '수령완료' : '수령대기'}
                        </span>
                        <div class="claim-code">${reward.claimCode}</div>
                    </div>
                </div>
            `;
        }).join('');
    }
    
    // 리워드 섹션 표시
    rewardsSection.style.display = 'block';
}

// 프로필 모달 열 때 리워드 히스토리 로드
function showProfileModal() {
    if (!isLoggedIn) {
        showToast('로그인이 필요합니다.', 'warning');
        return;
    }
    
    // 현재 사용자 정보로 폼 채우기
    if (currentUser) {
        document.getElementById('profile-firstname').value = currentUser.firstName || '';
        document.getElementById('profile-lastname').value = currentUser.lastName || '';
        document.getElementById('profile-email').value = currentUser.email || '';
        document.getElementById('profile-username').value = currentUser.username || '';
        document.getElementById('profile-phone').value = currentUser.phone || '';
        
        // 리워드 히스토리 로드
        loadUserRewards(currentUser.id);
    }
    
    showModal('profile-modal');
}

// ==================== 리워드 설정 기능 ====================

// 리워드 설정 모달 표시
function showRewardSettingsModal() {
    if (!isLoggedIn || !currentUser || !currentUser.roles.includes('ADMIN')) {
        showToast('관리자 권한이 필요합니다.', 'warning');
        return;
    }
    
    // 리워드 설정 모달이 없으면 생성
    let modal = document.getElementById('reward-settings-modal');
    if (!modal) {
        createRewardSettingsModal();
        modal = document.getElementById('reward-settings-modal');
    }
    
    // 현재 설정 로드
    loadRewardSettings();
    
    // 모달 표시
    showModal('reward-settings-modal');
}

// 리워드 설정 모달 생성
function createRewardSettingsModal() {
    const modalHTML = `
        <div id="reward-settings-modal" class="modal">
            <div class="modal-content large">
                <div class="modal-header">
                    <h3>🎁 리워드 설정</h3>
                    <button class="close-btn" onclick="closeModal('reward-settings-modal')">
                        &times;
                    </button>
                </div>
                <div class="modal-body">
                    <!-- 이벤트 설정 -->
                    <div class="settings-section">
                        <h4>이벤트 설정</h4>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="event-active">이벤트 활성화</label>
                                <select id="event-active">
                                    <option value="true">활성화</option>
                                    <option value="false">비활성화</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="max-winners">최대 당첨자 수</label>
                                <input type="number" id="max-winners" min="1" max="10" value="3">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="participation-threshold">최소 참여자 수</label>
                                <input type="number" id="participation-threshold" min="1" max="100" value="1">
                            </div>
                            <div class="form-group">
                                <label for="base-probability">기본 당첨 확률 (%)</label>
                                <input type="number" id="base-probability" min="0" max="100" value="50">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="event-start-date">이벤트 시작일</label>
                                <input type="datetime-local" id="event-start-date">
                            </div>
                            <div class="form-group">
                                <label for="event-end-date">이벤트 종료일</label>
                                <input type="datetime-local" id="event-end-date">
                            </div>
                        </div>
                    </div>
                    
                    <!-- 리워드 풀 관리 -->
                    <div class="settings-section">
                        <h4>리워드 풀 관리</h4>
                        <div class="reward-pool-container" id="reward-pool-container">
                            <!-- 리워드 목록이 여기에 동적으로 추가됩니다 -->
                        </div>
                        <button class="btn btn-primary" onclick="addRewardItem()">
                            <i class="fas fa-plus"></i> 리워드 추가
                        </button>
                    </div>
                    
                    <!-- 설정 저장 -->
                    <div class="settings-actions">
                        <button class="btn btn-primary" onclick="saveRewardSettings()">
                            <i class="fas fa-save"></i> 설정 저장
                        </button>
                        <button class="btn btn-outline" onclick="resetRewardSettings()">
                            <i class="fas fa-undo"></i> 기본값으로 복원
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 리워드 설정 로드
function loadRewardSettings() {
    const settings = dummyData.rewards.eventSettings;
    
    // 이벤트 설정 로드
    document.getElementById('event-active').value = settings.eventActive.toString();
    document.getElementById('max-winners').value = settings.maxWinners;
    document.getElementById('participation-threshold').value = settings.participationThreshold;
    document.getElementById('base-probability').value = Math.round(settings.baseProbability * 100);
    
    // 날짜 설정
    const startDate = new Date(settings.startDate);
    const endDate = new Date(settings.endDate);
    document.getElementById('event-start-date').value = startDate.toISOString().slice(0, 16);
    document.getElementById('event-end-date').value = endDate.toISOString().slice(0, 16);
    
    // 리워드 풀 로드
    loadRewardPool();
}

// 리워드 풀 로드
function loadRewardPool() {
    const container = document.getElementById('reward-pool-container');
    const rewards = dummyData.rewards.rewardPool;
    
    container.innerHTML = rewards.map((reward, index) => `
        <div class="reward-item-settings" data-index="${index}">
            <div class="reward-item-header">
                <h5>리워드 ${index + 1}</h5>
                <button class="btn btn-sm" onclick="removeRewardItem(${index})">
                    <i class="fas fa-trash"></i> 삭제
                </button>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>리워드 이름</label>
                    <input type="text" value="${reward.name}" onchange="updateRewardItem(${index}, 'name', this.value)">
                </div>
                <div class="form-group">
                    <label>리워드 타입</label>
                    <select onchange="updateRewardItem(${index}, 'type', this.value)">
                        <option value="GIFTCARD" ${reward.type === 'GIFTCARD' ? 'selected' : ''}>기프티콘</option>
                        <option value="POINTS" ${reward.type === 'POINTS' ? 'selected' : ''}>포인트</option>
                        <option value="COUPON" ${reward.type === 'COUPON' ? 'selected' : ''}>쿠폰</option>
                        <option value="CASH" ${reward.type === 'CASH' ? 'selected' : ''}>현금</option>
                        <option value="DIGITAL" ${reward.type === 'DIGITAL' ? 'selected' : ''}>디지털 상품</option>
                        <option value="PHYSICAL" ${reward.type === 'PHYSICAL' ? 'selected' : ''}>실물 상품</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>설명</label>
                <input type="text" value="${reward.description}" onchange="updateRewardItem(${index}, 'description', this.value)">
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>가치 (원)</label>
                    <input type="number" value="${reward.value}" onchange="updateRewardItem(${index}, 'value', parseInt(this.value))">
                </div>
                <div class="form-group">
                    <label>확률 (%)</label>
                    <input type="number" value="${Math.round(reward.probability * 100)}" min="0" max="100" onchange="updateRewardItem(${index}, 'probability', parseFloat(this.value) / 100)">
                </div>
            </div>
        </div>
    `).join('');
}

// 리워드 아이템 업데이트
function updateRewardItem(index, field, value) {
    if (dummyData.rewards.rewardPool[index]) {
        dummyData.rewards.rewardPool[index][field] = value;
    }
}

// 리워드 아이템 추가
function addRewardItem() {
    const newReward = {
        id: 'reward-' + Date.now(),
        name: '새 리워드',
        description: '새로 추가된 리워드입니다',
        value: 1000,
        type: 'GIFTCARD',
        image: 'https://via.placeholder.com/100x100/4CAF50/white?text=🎁',
        probability: 0.1
    };
    
    dummyData.rewards.rewardPool.push(newReward);
    loadRewardPool();
}

// 리워드 아이템 삭제
function removeRewardItem(index) {
    if (dummyData.rewards.rewardPool.length > 1) {
        dummyData.rewards.rewardPool.splice(index, 1);
        loadRewardPool();
    } else {
        showToast('최소 1개의 리워드는 유지해야 합니다.', 'warning');
    }
}

// 리워드 설정 저장
function saveRewardSettings() {
    // 이벤트 설정 저장
    dummyData.rewards.eventSettings.eventActive = document.getElementById('event-active').value === 'true';
    dummyData.rewards.eventSettings.maxWinners = parseInt(document.getElementById('max-winners').value);
    dummyData.rewards.eventSettings.participationThreshold = parseInt(document.getElementById('participation-threshold').value);
    
    // 확률 설정 업데이트
    const baseProbability = parseInt(document.getElementById('base-probability').value) / 100;
    
    // 날짜 설정
    const startDate = new Date(document.getElementById('event-start-date').value);
    const endDate = new Date(document.getElementById('event-end-date').value);
    dummyData.rewards.eventSettings.startDate = startDate.toISOString();
    dummyData.rewards.eventSettings.endDate = endDate.toISOString();
    
    // 확률 정규화 (총합이 1이 되도록)
    const totalProbability = dummyData.rewards.rewardPool.reduce((sum, reward) => sum + reward.probability, 0);
    if (totalProbability > 0) {
        dummyData.rewards.rewardPool.forEach(reward => {
            reward.probability = reward.probability / totalProbability;
        });
    }
    
    closeModal('reward-settings-modal');
    showToast('리워드 설정이 저장되었습니다!', 'success');
}

// 리워드 설정 초기화
function resetRewardSettings() {
    if (confirm('리워드 설정을 기본값으로 복원하시겠습니까?')) {
        // 기본 설정으로 복원
        dummyData.rewards.eventSettings = {
            maxWinners: 3,
            participationThreshold: 10,
            eventActive: true,
            startDate: '2024-01-01T00:00:00Z',
            endDate: '2024-12-31T23:59:59Z'
        };
        
        // 기본 리워드 풀 복원
        dummyData.rewards.rewardPool = [
            {
                id: 'reward-1',
                name: '스타벅스 아메리카노 기프티콘',
                description: '스타벅스 아메리카노 Tall 사이즈 기프티콘',
                value: 4500,
                type: 'GIFTCARD',
                image: 'https://via.placeholder.com/100x100/4CAF50/white?text=☕',
                probability: 0.3
            },
            {
                id: 'reward-2',
                name: '네이버페이 5,000원',
                description: '네이버페이 포인트 5,000원',
                value: 5000,
                type: 'POINTS',
                image: 'https://via.placeholder.com/100x100/2196F3/white?text=💰',
                probability: 0.4
            },
            {
                id: 'reward-3',
                name: '쿠팡 이츠 3,000원 할인쿠폰',
                description: '쿠팡 이츠 주문 시 3,000원 할인',
                value: 3000,
                type: 'COUPON',
                image: 'https://via.placeholder.com/100x100/FF9800/white?text=🍔',
                probability: 0.3
            }
        ];
        
        loadRewardSettings();
        showToast('리워드 설정이 기본값으로 복원되었습니다.', 'success');
    }
}

// ==================== 리워드 대상자 리스트 ====================

// 리워드 대상자 리스트 모달 표시
function showRewardTargetsModal() {
    if (!isLoggedIn || !currentUser || !currentUser.roles.includes('ADMIN')) {
        showToast('관리자 권한이 필요합니다.', 'warning');
        return;
    }
    
    // 리워드 대상자 모달이 없으면 생성
    let modal = document.getElementById('reward-targets-modal');
    if (!modal) {
        createRewardTargetsModal();
        modal = document.getElementById('reward-targets-modal');
    }
    
    // 대상자 데이터 로드
    loadRewardTargets();
    
    // 모달 표시
    showModal('reward-targets-modal');
}

// 리워드 대상자 모달 생성
function createRewardTargetsModal() {
    const modalHTML = `
        <div id="reward-targets-modal" class="modal">
            <div class="modal-content large">
                <div class="modal-header">
                    <h3>🎯 리워드 대상자 관리</h3>
                    <button class="close-btn" onclick="closeModal('reward-targets-modal')">
                        &times;
                    </button>
                </div>
                <div class="modal-body">
                    <!-- 필터 및 검색 -->
                    <div class="admin-toolbar">
                        <div class="search-box">
                            <input type="text" id="target-search" placeholder="사용자 검색..." onkeyup="filterRewardTargets()">
                            <i class="fas fa-search"></i>
                        </div>
                        <div class="filter-controls">
                            <select id="target-status-filter" onchange="filterRewardTargets()">
                                <option value="">모든 상태</option>
                                <option value="eligible">당첨 가능</option>
                                <option value="won">당첨됨</option>
                                <option value="claimed">수령완료</option>
                            </select>
                            <select id="target-reward-filter" onchange="filterRewardTargets()">
                                <option value="">모든 리워드</option>
                            </select>
                        </div>
                    </div>
                    
                    <!-- 통계 요약 -->
                    <div class="stats-overview">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-users"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-eligible">0</h3>
                                    <p>당첨 가능자</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-trophy"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-winners">0</h3>
                                    <p>당첨자</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-check-circle"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-claimed">0</h3>
                                    <p>수령완료</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-won-sign"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-value">0원</h3>
                                    <p>총 지급액</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 대상자 목록 -->
                    <div class="targets-table-container">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>사용자</th>
                                    <th>참여 설문</th>
                                    <th>리워드</th>
                                    <th>상태</th>
                                    <th>당첨일</th>
                                    <th>수령일</th>
                                    <th>액션</th>
                                </tr>
                            </thead>
                            <tbody id="targets-table-body">
                                <!-- 대상자 목록이 여기에 동적으로 추가됩니다 -->
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- 액션 버튼 -->
                    <div class="targets-actions">
                        <button class="btn btn-primary" onclick="exportRewardTargets()">
                            <i class="fas fa-download"></i> 대상자 목록 내보내기
                        </button>
                        <button class="btn btn-outline" onclick="sendRewardNotifications()">
                            <i class="fas fa-bell"></i> 수령 알림 발송
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 리워드 대상자 데이터 로드
function loadRewardTargets() {
    // 통계 업데이트
    updateRewardTargetsStats();
    
    // 리워드 필터 옵션 로드
    loadRewardFilterOptions();
    
    // 대상자 목록 로드
    loadRewardTargetsList();
}

// 리워드 대상자 통계 업데이트
function updateRewardTargetsStats() {
    const allUsers = dummyData.users;
    const winners = dummyData.rewards.winners;
    const claimedRewards = winners.filter(w => w.claimed);
    const totalValue = winners.reduce((sum, winner) => {
        const reward = dummyData.rewards.rewardPool.find(r => r.id === winner.rewardId);
        return sum + (reward ? reward.value : 0);
    }, 0);
    
    document.getElementById('total-eligible').textContent = allUsers.length;
    document.getElementById('total-winners').textContent = winners.length;
    document.getElementById('total-claimed').textContent = claimedRewards.length;
    document.getElementById('total-value').textContent = totalValue.toLocaleString() + '원';
}

// 리워드 필터 옵션 로드
function loadRewardFilterOptions() {
    const select = document.getElementById('target-reward-filter');
    const rewards = dummyData.rewards.rewardPool;
    
    select.innerHTML = '<option value="">모든 리워드</option>' + 
        rewards.map(reward => `<option value="${reward.id}">${reward.name}</option>`).join('');
}

// 리워드 대상자 목록 로드
function loadRewardTargetsList() {
    const tbody = document.getElementById('targets-table-body');
    const winners = dummyData.rewards.winners;
    
    tbody.innerHTML = winners.map(winner => {
        const user = dummyData.users.find(u => u.id === winner.userId);
        const reward = dummyData.rewards.rewardPool.find(r => r.id === winner.rewardId);
        const survey = dummyData.surveys.find(s => s.id === winner.surveyId);
        
        if (!user || !reward || !survey) return '';
        
        const statusClass = winner.claimed ? 'status-claimed' : 'status-pending';
        const statusText = winner.claimed ? '수령완료' : '수령대기';
        
        return `
            <tr>
                <td>
                    <div class="user-info">
                        <div class="user-avatar">${user.firstName ? user.firstName[0] : user.username[0]}</div>
                        <div class="user-details">
                            <h4>${user.firstName ? user.firstName + ' ' + user.lastName : user.username}</h4>
                            <p>${user.email}</p>
                        </div>
                    </div>
                </td>
                <td>${survey.title}</td>
                <td>
                    <div class="reward-info">
                        <strong>${reward.name}</strong>
                        <br><small>${reward.value.toLocaleString()}원</small>
                    </div>
                </td>
                <td>
                    <span class="status-badge ${statusClass}">${statusText}</span>
                </td>
                <td>${formatDateTime(winner.wonAt)}</td>
                <td>${winner.claimedAt ? formatDateTime(winner.claimedAt) : '-'}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit" onclick="viewWinnerDetails('${winner.id}')">
                            <i class="fas fa-eye"></i>
                        </button>
                        ${!winner.claimed ? `
                            <button class="action-btn activate" onclick="markAsClaimed('${winner.id}')">
                                <i class="fas fa-check"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// 리워드 대상자 필터링
function filterRewardTargets() {
    const searchTerm = document.getElementById('target-search').value.toLowerCase();
    const statusFilter = document.getElementById('target-status-filter').value;
    const rewardFilter = document.getElementById('target-reward-filter').value;
    
    const rows = document.querySelectorAll('#targets-table-body tr');
    
    rows.forEach(row => {
        const userText = row.querySelector('.user-details h4').textContent.toLowerCase();
        const emailText = row.querySelector('.user-details p').textContent.toLowerCase();
        const statusBadge = row.querySelector('.status-badge');
        const status = statusBadge.textContent;
        
        let showRow = true;
        
        // 검색어 필터
        if (searchTerm && !userText.includes(searchTerm) && !emailText.includes(searchTerm)) {
            showRow = false;
        }
        
        // 상태 필터
        if (statusFilter) {
            if (statusFilter === 'eligible' && status !== '수령대기') showRow = false;
            if (statusFilter === 'won' && status !== '수령완료') showRow = false;
            if (statusFilter === 'claimed' && status !== '수령완료') showRow = false;
        }
        
        // 리워드 필터
        if (rewardFilter) {
            const rewardName = row.querySelector('.reward-info strong').textContent;
            const reward = dummyData.rewards.rewardPool.find(r => r.id === rewardFilter);
            if (reward && rewardName !== reward.name) showRow = false;
        }
        
        row.style.display = showRow ? '' : 'none';
    });
}

// 당첨자 상세 보기
function viewWinnerDetails(winnerId) {
    const winner = dummyData.rewards.winners.find(w => w.id === winnerId);
    if (!winner) return;
    
    const user = dummyData.users.find(u => u.id === winner.userId);
    const reward = dummyData.rewards.rewardPool.find(r => r.id === winner.rewardId);
    const survey = dummyData.surveys.find(s => s.id === winner.surveyId);
    
    const details = `
        당첨자: ${user ? user.firstName + ' ' + user.lastName : '사용자'}
        이메일: ${user ? user.email : '-'}
        설문: ${survey ? survey.title : '-'}
        리워드: ${reward ? reward.name : '-'}
        가치: ${reward ? reward.value.toLocaleString() + '원' : '-'}
        당첨일: ${formatDateTime(winner.wonAt)}
        클레임 코드: ${winner.claimCode}
        수령 상태: ${winner.claimed ? '수령완료' : '수령대기'}
    `;
    
    alert(details);
}

// 수령 완료 처리
function markAsClaimed(winnerId) {
    if (confirm('이 리워드를 수령 완료로 처리하시겠습니까?')) {
        const winner = dummyData.rewards.winners.find(w => w.id === winnerId);
        if (winner) {
            winner.claimed = true;
            winner.claimedAt = new Date().toISOString();
            
            // 사용자 리워드 히스토리 업데이트
            if (dummyData.rewards.userRewards[winner.userId]) {
                const userReward = dummyData.rewards.userRewards[winner.userId].find(r => r.claimCode === winner.claimCode);
                if (userReward) {
                    userReward.claimed = true;
                    userReward.claimedAt = new Date().toISOString();
                }
            }
            
            loadRewardTargetsList();
            updateRewardTargetsStats();
            showToast('수령 완료로 처리되었습니다.', 'success');
        }
    }
}

// 리워드 대상자 목록 내보내기
function exportRewardTargets() {
    showLoading();
    
    setTimeout(() => {
        hideLoading();
        showToast('리워드 대상자 목록이 내보내졌습니다.', 'success');
    }, 2000);
}

// 수령 알림 발송
function sendRewardNotifications() {
    const pendingWinners = dummyData.rewards.winners.filter(w => !w.claimed);
    
    if (pendingWinners.length === 0) {
        showToast('수령 대기 중인 리워드가 없습니다.', 'info');
        return;
    }
    
    if (confirm(`${pendingWinners.length}명에게 수령 알림을 발송하시겠습니까?`)) {
        showLoading();
        
        setTimeout(() => {
            hideLoading();
            showToast(`${pendingWinners.length}명에게 수령 알림이 발송되었습니다.`, 'success');
        }, 2000);
    }
}

// ==================== 생성자 리워드 관리 ====================

// 생성자 리워드 모달 표시
function showCreatorRewardsModal() {
    if (!isLoggedIn || !currentUser || !currentUser.roles.includes('ADMIN')) {
        showToast('관리자 권한이 필요합니다.', 'warning');
        return;
    }
    
    // 생성자 리워드 모달이 없으면 생성
    let modal = document.getElementById('creator-rewards-modal');
    if (!modal) {
        createCreatorRewardsModal();
        modal = document.getElementById('creator-rewards-modal');
    }
    
    // 데이터 로드
    loadCreatorRewards();
    
    // 모달 표시
    showModal('creator-rewards-modal');
}

// 생성자 리워드 모달 생성
function createCreatorRewardsModal() {
    const modalHTML = `
        <div id="creator-rewards-modal" class="modal">
            <div class="modal-content large">
                <div class="modal-header">
                    <h3>👑 생성자 리워드 관리</h3>
                    <button class="close-btn" onclick="closeModal('creator-rewards-modal')">
                        &times;
                    </button>
                </div>
                <div class="modal-body">
                    <!-- 통계 요약 -->
                    <div class="stats-overview">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-users"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-creators">0</h3>
                                    <p>활성 생성자</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-gift"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-creator-rewards">0</h3>
                                    <p>지급된 리워드</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-won-sign"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="total-creator-value">0원</h3>
                                    <p>총 지급액</p>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">
                                    <i class="fas fa-chart-line"></i>
                                </div>
                                <div class="stat-content">
                                    <h3 id="avg-participation">0</h3>
                                    <p>평균 참여율</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 생성자 목록 -->
                    <div class="creator-rewards-table-container">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>생성자</th>
                                    <th>설문/퀴즈</th>
                                    <th>참여자 수</th>
                                    <th>리워드 상태</th>
                                    <th>지급액</th>
                                    <th>액션</th>
                                </tr>
                            </thead>
                            <tbody id="creator-rewards-table-body">
                                <!-- 생성자 목록이 여기에 동적으로 추가됩니다 -->
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- 액션 버튼 -->
                    <div class="creator-rewards-actions">
                        <button class="btn btn-primary" onclick="calculateCreatorRewards()">
                            <i class="fas fa-calculator"></i> 리워드 계산
                        </button>
                        <button class="btn btn-outline" onclick="exportCreatorRewards()">
                            <i class="fas fa-download"></i> 내보내기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 생성자 리워드 데이터 로드
function loadCreatorRewards() {
    // 통계 업데이트
    updateCreatorRewardsStats();
    
    // 생성자 목록 로드
    loadCreatorRewardsList();
}

// 생성자 리워드 통계 업데이트
function updateCreatorRewardsStats() {
    const surveys = dummyData.surveys;
    const activeCreators = [...new Set(surveys.map(s => s.author))].length;
    const totalCreatorRewards = surveys.filter(s => s.creatorReward && s.creatorReward.claimed).length;
    const totalCreatorValue = surveys.reduce((sum, survey) => {
        if (survey.creatorReward && survey.creatorReward.claimed) {
            return sum + (survey.creatorReward.value || 0);
        }
        return sum;
    }, 0);
    const avgParticipation = surveys.reduce((sum, survey) => {
        return sum + (survey.participationCount / survey.targetCount * 100);
    }, 0) / surveys.length || 0;
    
    document.getElementById('total-creators').textContent = activeCreators;
    document.getElementById('total-creator-rewards').textContent = totalCreatorRewards;
    document.getElementById('total-creator-value').textContent = totalCreatorValue.toLocaleString() + '원';
    document.getElementById('avg-participation').textContent = Math.round(avgParticipation) + '%';
}

// 생성자 리워드 목록 로드
function loadCreatorRewardsList() {
    const tbody = document.getElementById('creator-rewards-table-body');
    const surveys = dummyData.surveys;
    
    tbody.innerHTML = surveys.map(survey => {
        const participationRate = Math.round((survey.participationCount / survey.targetCount) * 100);
        const hasCreatorReward = survey.creatorReward && survey.creatorReward.enabled;
        const isClaimed = survey.creatorReward && survey.creatorReward.claimed;
        
        return `
            <tr>
                <td>
                    <div class="user-info">
                        <div class="user-avatar">${survey.author[0]}</div>
                        <div class="user-details">
                            <h4>${survey.author}</h4>
                            <p>${survey.surveyType === 'SURVEY' ? '설문조사' : '퀴즈'} 생성자</p>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="survey-info">
                        <strong>${survey.title}</strong>
                        <br><small>${survey.surveyType === 'SURVEY' ? '설문조사' : '퀴즈'}</small>
                    </div>
                </td>
                <td>
                    <div class="participation-info">
                        <strong>${survey.participationCount}명</strong>
                        <br><small>목표: ${survey.targetCount}명</small>
                        <br><small>달성률: ${participationRate}%</small>
                    </div>
                </td>
                <td>
                    ${hasCreatorReward ? `
                        <span class="status-badge ${isClaimed ? 'status-claimed' : 'status-pending'}">
                            ${isClaimed ? '수령완료' : '수령대기'}
                        </span>
                    ` : '<span class="status-badge" style="background: #e9ecef; color: #6c757d;">리워드 없음</span>'}
                </td>
                <td>
                    ${hasCreatorReward ? `
                        <strong>${survey.creatorReward.value.toLocaleString()}원</strong>
                    ` : '-'}
                </td>
                <td>
                    <div class="action-buttons">
                        ${hasCreatorReward && !isClaimed ? `
                            <button class="action-btn activate" onclick="claimCreatorReward('${survey.id}')">
                                <i class="fas fa-check"></i> 수령처리
                            </button>
                        ` : ''}
                        <button class="action-btn edit" onclick="viewCreatorDetails('${survey.id}')">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// 생성자 리워드 계산
function calculateCreatorRewards() {
    if (confirm('모든 설문/퀴즈에 대해 생성자 리워드를 계산하시겠습니까?')) {
        showLoading();
        
        setTimeout(() => {
            // 리워드 계산 로직
            dummyData.surveys.forEach(survey => {
                if (!survey.creatorReward) {
                    const participationRate = survey.participationCount / survey.targetCount;
                    if (participationRate >= 0.8) { // 80% 이상 달성 시
                        survey.creatorReward = {
                            enabled: true,
                            type: 'CASH',
                            value: Math.round(survey.participationCount * 100), // 참여자당 100원
                            description: '설문 참여율 달성 리워드',
                            claimed: false,
                            createdAt: new Date().toISOString()
                        };
                    }
                }
            });
            
            hideLoading();
            loadCreatorRewards();
            showToast('생성자 리워드가 계산되었습니다.', 'success');
        }, 2000);
    }
}

// 생성자 리워드 수령 처리
function claimCreatorReward(surveyId) {
    const survey = dummyData.surveys.find(s => s.id === surveyId);
    if (!survey || !survey.creatorReward) return;
    
    if (confirm('이 생성자 리워드를 수령 완료로 처리하시겠습니까?')) {
        survey.creatorReward.claimed = true;
        survey.creatorReward.claimedAt = new Date().toISOString();
        
        loadCreatorRewards();
        showToast('생성자 리워드가 수령 완료로 처리되었습니다.', 'success');
    }
}

// 생성자 상세 보기
function viewCreatorDetails(surveyId) {
    const survey = dummyData.surveys.find(s => s.id === surveyId);
    if (!survey) return;
    
    const participationRate = Math.round((survey.participationCount / survey.targetCount) * 100);
    const hasCreatorReward = survey.creatorReward && survey.creatorReward.enabled;
    
    const details = `
        생성자: ${survey.author}
        설문/퀴즈: ${survey.title}
        타입: ${survey.surveyType === 'SURVEY' ? '설문조사' : '퀴즈'}
        참여자: ${survey.participationCount}명 / ${survey.targetCount}명
        달성률: ${participationRate}%
        리워드: ${hasCreatorReward ? survey.creatorReward.value.toLocaleString() + '원' : '없음'}
        상태: ${hasCreatorReward ? (survey.creatorReward.claimed ? '수령완료' : '수령대기') : '리워드 없음'}
    `;
    
    alert(details);
}

// 생성자 리워드 내보내기
function exportCreatorRewards() {
    showLoading();
    
    setTimeout(() => {
        hideLoading();
        showToast('생성자 리워드 목록이 내보내졌습니다.', 'success');
    }, 2000);
}
