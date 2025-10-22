class SocialNetworkApp {
    constructor() {
        this.authService = authService;
        this.feedService = null;
        this.init();
    }

    async init() {
        console.log('Initializing Social Network App...');
        await this.updateUI();
        this.setupEventListeners();
        this.setupAuthInterceptors();
    }

    async updateUI() {
        const nav = document.getElementById('nav');
        const authSection = document.getElementById('auth-section');
        const feedSection = document.getElementById('feed-section');

        if (!nav) {
            console.error('Navigation element not found');
            return;
        }

        if (this.authService.isAuthenticated()) {
            console.log('User is authenticated, showing user UI');

            // Пользователь авторизован
            nav.innerHTML = `
                <a href="/feed.html" class="nav-link">📰 Лента</a>
                <a href="/profile.html" class="nav-link">👤 Профиль</a>
                <button onclick="app.logout()" class="btn btn-outline">🚪 Выйти</button>
            `;

            if (authSection) authSection.style.display = 'none';
            if (feedSection) {
                feedSection.style.display = 'block';
                await this.loadFeed();
            }
        } else {
            console.log('User is not authenticated, showing auth UI');

            // Пользователь не авторизован
            nav.innerHTML = `
                <button onclick="showLogin()" class="btn btn-outline">🔑 Войти</button>
                <button onclick="showRegister()" class="btn btn-primary">📝 Регистрация</button>
            `;

            if (authSection) authSection.style.display = 'block';
            if (feedSection) feedSection.style.display = 'none';
        }
    }

    async loadFeed() {
        const feedContent = document.getElementById('feed-content');
        if (!feedContent) return;

        try {
            // Инициализируем feed service
            if (!this.feedService) {
                this.feedService = new FeedService();
                const initialized = await this.feedService.initialize();
                if (!initialized) {
                    feedContent.innerHTML = '<p>Ошибка загрузки ленты</p>';
                    return;
                }
            }

            const feed = await this.feedService.loadFeed();
            this.renderFeed(feed);
        } catch (error) {
            console.error('Error loading feed:', error);
            feedContent.innerHTML = '<p>Ошибка загрузки ленты</p>';
        }
    }

    renderFeed(feed) {
        const feedContent = document.getElementById('feed-content');
        if (!feedContent) return;

        if (!feed || feed.length === 0) {
            feedContent.innerHTML = `
                <div class="empty-state">
                    <h3>Добро пожаловать в Social Network!</h3>
                    <p>Начните с подписки на других пользователей или создания первого поста</p>
                    <div class="empty-state-actions">
                        <button onclick="window.location.href='/profile.html'" class="btn btn-primary">
                            Перейти в профиль
                        </button>
                    </div>
                </div>
            `;
            return;
        }

        // Показываем только первые 3 поста на главной
        const recentPosts = feed.slice(0, 3);

        feedContent.innerHTML = `
            <div class="recent-posts">
                <h3>Недавние посты в вашей ленте</h3>
                <div class="posts-preview">
                    ${recentPosts.map(post => `
                        <div class="post-preview-card">
                            <div class="post-preview-header">
                                <strong>@${post.authorUsername}</strong>
                                <span class="post-preview-date">
                                    ${new Date(post.postCreatedAt).toLocaleDateString()}
                                </span>
                            </div>
                            <h4>${this.escapeHtml(post.title)}</h4>
                            <p class="post-preview-content">${this.escapeHtml(post.content)}</p>
                            <div class="post-preview-stats">
                                <span>❤️ ${post.likesCount || 0}</span>
                                <span>💬 ${post.commentsCount || 0}</span>
                            </div>
                        </div>
                    `).join('')}
                </div>
                ${feed.length > 3 ? `
                    <div class="view-all-container">
                        <button onclick="window.location.href='/feed.html'" class="btn btn-outline">
                            Показать все посты (${feed.length})
                        </button>
                    </div>
                ` : ''}
            </div>
        `;
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    setupEventListeners() {
        console.log('Setting up event listeners...');

        // Закрытие модальных окон на крестик
        document.querySelectorAll('.close').forEach(closeBtn => {
            closeBtn.onclick = function() {
                console.log('Closing modal');
                this.closest('.modal').style.display = 'none';
            };
        });

        // Обработчики для форм
        this.setupAuthForms();
    }

    setupAuthForms() {
        // Форма входа
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.handleLogin();
            });
        }

        // Форма регистрации
        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.handleRegister();
            });
        }
    }

    async handleLogin() {
        const username = document.getElementById('login-username')?.value;
        const password = document.getElementById('login-password')?.value;

        if (!username || !password) {
            this.showError('Заполните все поля');
            return;
        }

        try {
            console.log('Attempting login for user:', username);
            const result = await this.authService.login(username, password);

            if (result.success) {
                console.log('Login successful');
                this.showSuccess('Успешный вход!');
                this.closeModal('login-modal');
                await this.updateUI();
            } else {
                console.error('Login failed:', result.message);
                this.showError('Ошибка входа: ' + result.message);
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showError('Ошибка входа');
        }
    }

    async handleRegister() {
        const userData = {
            username: document.getElementById('register-username')?.value,
            email: document.getElementById('register-email')?.value,
            password: document.getElementById('register-password')?.value,
            firstName: document.getElementById('register-firstname')?.value,
            lastName: document.getElementById('register-lastname')?.value
        };

        // Валидация
        if (!userData.username || !userData.email || !userData.password) {
            this.showError('Заполните обязательные поля');
            return;
        }

        try {
            console.log('Attempting registration for user:', userData.username);
            const result = await this.authService.register(userData);

            if (result.success) {
                console.log('Registration successful');
                this.showSuccess('Регистрация успешна! Теперь войдите в систему.');
                this.closeModal('register-modal');
                this.showLogin();
            } else {
                console.error('Registration failed:', result.message);
                this.showError('Ошибка регистрации: ' + result.message);
            }
        } catch (error) {
            console.error('Registration error:', error);
            this.showError('Ошибка регистрации');
        }
    }

    setupAuthInterceptors() {
        // Сохраняем оригинальный fetch
        const originalFetch = window.fetch;

        // Создаем стрелочную функцию чтобы сохранить контекст
        const authFetch = async (url, options = {}) => {
            // Пропускаем запросы к статическим файлам и публичным endpoint'ам
            if (typeof url === 'string' &&
                (url.includes('.css') || url.includes('.js') || url.includes('.html') ||
                    url.includes('/api/auth/') || url.endsWith('/api/users/register'))) {
                return originalFetch(url, options);
            }

            // Добавляем токен к запросам к API
            if (typeof url === 'string' && url.includes('/api/') && this.authService.token) {
                console.log('Adding auth token to request:', url);
                const newOptions = {
                    ...options,
                    headers: {
                        ...options.headers,
                        'Authorization': `Bearer ${this.authService.token}`
                    }
                };

                return originalFetch(url, newOptions);
            }

            return originalFetch(url, options);
        };

        // Заменяем глобальный fetch
        window.fetch = authFetch;
    }

    async logout() {
        console.log('Logging out...');
        this.authService.logout();
        await this.updateUI();
    }

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
        }
    }

    showError(message) {
        this.showToast(message, 'error');
    }

    showSuccess(message) {
        this.showToast(message, 'success');
    }

    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `app-toast app-toast-${type}`;
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
            animation: slideIn 0.3s ease;
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.3s';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }, 4000);
    }
}

// Глобальные функции для вызова из HTML
function showLogin() {
    const modal = document.getElementById('login-modal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function showRegister() {
    const modal = document.getElementById('register-modal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// Закрытие модальных окон при клике вне контента
window.onclick = function(event) {
    const modals = document.getElementsByClassName('modal');
    for (let modal of modals) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    }
}

// Инициализация приложения
const app = new SocialNetworkApp();

// Глобальные функции для HTML
window.updateUI = () => app.updateUI();
window.showLogin = showLogin;
window.showRegister = showRegister;
window.closeModal = closeModal;

// Добавляем CSS анимации
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .loading-spinner {
        border: 3px solid #f3f3f3;
        border-top: 3px solid #3498db;
        border-radius: 50%;
        width: 30px;
        height: 30px;
        animation: spin 1s linear infinite;
        margin: 0 auto 1rem;
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
    
    .empty-state {
        text-align: center;
        padding: 3rem 1rem;
        color: #7f8c8d;
    }
    
    .empty-state h3 {
        margin-bottom: 1rem;
        color: #2c3e50;
    }
    
    .empty-state-actions {
        margin-top: 1.5rem;
        display: flex;
        gap: 1rem;
        justify-content: center;
        flex-wrap: wrap;
    }
    
    .post-preview-card {
        background: white;
        border-radius: 8px;
        padding: 1.5rem;
        margin-bottom: 1rem;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        border-left: 4px solid #3498db;
    }
    
    .posts-preview {
        margin-bottom: 2rem;
    }
    
    .view-all-container {
        text-align: center;
        margin-top: 2rem;
    }
    
    .your-post-badge {
        background: #3498db;
        color: white;
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 0.7rem;
        margin-left: 0.5rem;
    }
`;
document.head.appendChild(style);