const CACHE_NAME = 'video-call-v1';
const urlsToCache = [
    '/',
    '/create-room',
    '/join',
    '/css/styles.css',
    '/js/app.js'
];

self.addEventListener('install', function(event) {
    console.log('Service Worker 설치됨');

    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(function(cache) {
                console.log('캐시 오픈됨');
                return cache.addAll(urlsToCache);
            })
            .catch(function(error) {
                console.error('캐시 설치 실패:', error);
            })
    );
});

self.addEventListener('activate', function(event) {
    console.log('Service Worker 활성화됨');

    event.waitUntil(
        caches.keys().then(function(cacheNames) {
            return Promise.all(
                cacheNames.map(function(cacheName) {
                    if (cacheName !== CACHE_NAME) {
                        console.log('이전 캐시 삭제:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
});

self.addEventListener('fetch', function(event) {
    if (event.request.url.includes('/ws/') ||
        event.request.url.includes('/api/') ||
        event.request.method !== 'GET') {
        return;
    }

    event.respondWith(
        caches.match(event.request)
            .then(function(response) {
                if (response) {
                    return response;
                }

                return fetch(event.request).then(function(response) {
                    if (!response || response.status !== 200 || response.type !== 'basic') {
                        return response;
                    }

                    const responseToCache = response.clone();
                    caches.open(CACHE_NAME)
                        .then(function(cache) {
                            cache.put(event.request, responseToCache);
                        });

                    return response;
                });
            })
            .catch(function() {
                if (event.request.destination === 'document') {
                    return caches.match('/');
                }
            })
    );
});

self.addEventListener('push', function(event) {
    console.log('푸시 메시지 수신:', event);

    const options = {
        body: '새로운 화상통화 초대가 있습니다.',
        icon: '/icon-192x192.png',
        badge: '/badge-72x72.png',
        vibrate: [100, 50, 100],
        data: {
            dateOfArrival: Date.now(),
            primaryKey: '2'
        },
        actions: [
            {
                action: 'explore',
                title: '참가하기',
                icon: '/check.png'
            },
            {
                action: 'close',
                title: '닫기',
                icon: '/close.png'
            }
        ]
    };

    event.waitUntil(
        self.registration.showNotification('화상통화 알림', options)
    );
});

self.addEventListener('notificationclick', function(event) {
    event.notification.close();

    if (event.action === 'explore') {
        event.waitUntil(
            clients.openWindow('/')
        );
    }
});