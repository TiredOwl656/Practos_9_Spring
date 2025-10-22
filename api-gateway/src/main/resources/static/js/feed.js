class FeedService {
    constructor() {
        this.authService = authService;
        this.currentUser = null;
    }

    // Инициализация сервиса
    async initialize() {
        if (!this.authService.isAuthenticated()) {
            return false;
        }

        this.currentUser = await this.getCurrentUser();
        return !!this.currentUser;
    }

    // Получение текущего пользователя
    async getCurrentUser() {
        try {
            console.log('Getting current user...');
            const response = await fetch('/api/users/profile/me', {
                headers: this.authService.getAuthHeaders()
            });

            console.log('User profile response status:', response.status);

            if (response.ok) {
                const user = await response.json();
                console.log('Current user:', user);
                return user;
            } else {
                console.error('Failed to get user profile:', response.status);
                return null;
            }
        } catch (error) {
            console.error('Error getting current user:', error);
            return null;
        }
    }

    // Загрузка ленты
    async loadFeed() {
        try {
            if (!this.currentUser) {
                console.error('No current user available');
                return [];
            }

            console.log('Loading feed for user:', this.currentUser.id, this.currentUser.username);

            const response = await fetch(`/api/feed/${this.currentUser.id}/full`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Feed response status:', response.status);

            if (response.ok) {
                const feed = await response.json();
                console.log('Feed loaded successfully:', feed.length, 'posts');
                return feed;
            } else {
                console.error('Feed response not OK:', response.status);
                return [];
            }
        } catch (error) {
            console.error('Error loading feed:', error);
            return [];
        }
    }

    // Создание поста
    async createPost(postData) {
        try {
            if (!this.currentUser) {
                console.error('No current user available for creating post');
                return null;
            }

            postData.authorId = this.currentUser.id;
            postData.authorUsername = this.currentUser.username;

            console.log('Creating post with data:', postData);

            const response = await fetch('/api/posts', {
                method: 'POST',
                headers: this.authService.getAuthHeaders(),
                body: JSON.stringify(postData)
            });

            if (response.ok) {
                const newPost = await response.json();
                console.log('Post created successfully:', newPost);
                return newPost;
            } else {
                console.error('Failed to create post:', response.status);
                return null;
            }
        } catch (error) {
            console.error('Error creating post:', error);
            return null;
        }
    }

    // Лайк поста
    async likePost(postId) {
        try {
            const response = await fetch(`/api/posts/${postId}/like`, {
                method: 'POST',
                headers: this.authService.getAuthHeaders()
            });

            return response.ok;
        } catch (error) {
            console.error('Error liking post:', error);
            return false;
        }
    }

    // Получение постов пользователя
    async getUserPosts(userId) {
        try {
            const response = await fetch(`/api/posts/author/${userId}`, {
                headers: this.authService.getAuthHeaders()
            });

            if (response.ok) {
                return await response.json();
            }
            return [];
        } catch (error) {
            console.error('Error getting user posts:', error);
            return [];
        }
    }
}

const feedService = new FeedService();

// Инициализация страницы ленты
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Feed page loaded');

    if (!authService.isAuthenticated()) {
        console.log('User not authenticated, redirecting to login');
        window.location.href = '/login.html';
        return;
    }

    console.log('User is authenticated, initializing feed service...');

    const initialized = await feedService.initialize();
    if (!initialized) {
        console.error('Failed to initialize feed service');
        showError('Ошибка загрузки профиля');
        return;
    }

    console.log('Feed service initialized successfully');
    await loadAndRenderFeed();
    setupEventListeners();
});

async function loadAndRenderFeed() {
    const feedContent = document.getElementById('feed-content');
    if (!feedContent) {
        console.error('Feed content element not found');
        return;
    }

    feedContent.innerHTML = `
        <div class="loading-state">
            <div class="loading-spinner"></div>
            <p>Загрузка ленты...</p>
        </div>
    `;

    try {
        console.log('Starting to load feed...');
        const feed = await feedService.loadFeed();
        console.log('Feed data received:', feed);
        renderFeed(feed);
    } catch (error) {
        console.error('Error loading feed:', error);
        feedContent.innerHTML = `
            <div class="error-state">
                <h3>Ошибка загрузки ленты</h3>
                <p>Попробуйте обновить страницу</p>
                <button onclick="location.reload()" class="btn btn-primary">Обновить</button>
            </div>
        `;
    }
}

function renderFeed(feed) {
    const feedContent = document.getElementById('feed-content');

    if (!feed || feed.length === 0) {
        feedContent.innerHTML = `
            <div class="empty-state">
                <h3>Лента пуста</h3>
                <p>Подпишитесь на других пользователей, чтобы видеть их посты здесь</p>
                <div class="empty-state-actions">
                    <button onclick="window.location.href='/profile.html'" class="btn btn-primary">
                        Найти пользователей
                    </button>
                    <button onclick="window.location.href='/'" class="btn btn-outline">
                        На главную
                    </button>
                </div>
            </div>
        `;
        return;
    }

    feedContent.innerHTML = `
        <div class="feed-header">
            <h2>Ваша лента</h2>
            <div class="feed-info">
                <span>${feed.length} ${getPostCountText(feed.length)}</span>
                <button onclick="refreshFeed()" class="btn btn-outline btn-sm">Обновить</button>
            </div>
        </div>
        <div class="posts-container">
            ${feed.map((post, index) => `
                <div class="post-card" data-post-id="${post.postId}">
                    <div class="post-header">
                        <div class="post-author">
                            <strong>@${post.authorUsername}</strong>
                            ${post.authorId === feedService.currentUser.id ?
        '<span class="your-post-badge">Ваш пост</span>' : ''
    }
                        </div>
                        <span class="post-date">${formatPostDate(post.postCreatedAt)}</span>
                    </div>
                    <h3 class="post-title">${escapeHtml(post.title)}</h3>
                    <p class="post-content">${escapeHtml(post.content)}</p>
                    <div class="post-stats">
                        <button onclick="likePost(${post.postId})" class="btn-like">
                            ❤️ ${post.likesCount || 0}
                        </button>
                        <span class="comments-count">
                            💬 ${post.commentsCount || 0}
                        </span>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

// Вспомогательные функции
function getPostCountText(count) {
    if (count % 10 === 1 && count % 100 !== 11) return 'пост';
    if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return 'поста';
    return 'постов';
}

function formatPostDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'только что';
    if (diffMins < 60) return `${diffMins} мин. назад`;
    if (diffHours < 24) return `${diffHours} ч. назад`;
    if (diffDays < 7) return `${diffDays} дн. назад`;

    return date.toLocaleDateString('ru-RU');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function setupEventListeners() {
    // Обработчик для кнопки обновления
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-like')) {
            e.preventDefault();
            const postId = e.target.closest('.post-card').dataset.postId;
            likePost(postId);
        }
    });
}

// Функция лайка поста
async function likePost(postId) {
    try {
        const success = await feedService.likePost(postId);
        if (success) {
            console.log('Post liked successfully');
            // Можно добавить визуальную обратную связь
            showToast('Пост отмечен как понравившийся!', 'success');
        } else {
            showToast('Ошибка при лайке поста', 'error');
        }
    } catch (error) {
        console.error('Error liking post:', error);
        showToast('Ошибка при лайке поста', 'error');
    }
}

// Функция обновления ленты
async function refreshFeed() {
    console.log('Refreshing feed...');
    await loadAndRenderFeed();
}

// Функция показа уведомлений
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'error' ? '#e74c3c' : type === 'success' ? '#2ecc71' : '#3498db'};
        color: white;
        padding: 12px 20px;
        border-radius: 4px;
        z-index: 10000;
        max-width: 300px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function showError(message) {
    showToast(message, 'error');
}

// Глобальные функции для вызова из HTML
window.refreshFeed = refreshFeed;
window.likePost = likePost;