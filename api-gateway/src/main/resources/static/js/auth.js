class AuthService {
    constructor() {
        this.token = localStorage.getItem('token');
        this.currentUser = JSON.parse(localStorage.getItem('currentUser'));
    }

    // Регистрация
    async register(userData) {
        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData)
            });

            if (!response.ok) {
                throw new Error('Ошибка регистрации');
            }

            const result = await response.text();
            return { success: true, message: result };
        } catch (error) {
            return { success: false, message: error.message };
        }
    }

    // Вход
    async login(username, password) {
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error('Неверные учетные данные');
            }

            const data = await response.json();
            this.token = data.token;
            this.currentUser = {
                username: data.username,
                role: data.role
            };

            // Сохраняем в localStorage
            localStorage.setItem('token', this.token);
            localStorage.setItem('currentUser', JSON.stringify(this.currentUser));

            return { success: true, data };
        } catch (error) {
            return { success: false, message: error.message };
        }
    }

    // Выход
    logout() {
        this.token = null;
        this.currentUser = null;
        localStorage.removeItem('token');
        localStorage.removeItem('currentUser');
        window.location.href = '/';
    }

    decodeToken(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload;
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    }

    getCurrentUserId() {
        if (!this.token) return null;
        const payload = this.decodeToken(this.token);
        return payload ? payload.sub : null; // sub обычно содержит username
    }

    // Проверка авторизации
    isAuthenticated() {
        return !!this.token;
    }

    // Получение заголовков с токеном
    getAuthHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        return headers;
    }
}

const authService = new AuthService();

// Обработчики форм
document.addEventListener('DOMContentLoaded', function() {
    // Форма входа
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;

            const result = await authService.login(username, password);
            if (result.success) {
                alert('Успешный вход!');
                closeModal('login-modal');
                updateUI();
            } else {
                alert('Ошибка: ' + result.message);
            }
        });
    }

    // Форма регистрации
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const userData = {
                username: document.getElementById('register-username').value,
                email: document.getElementById('register-email').value,
                password: document.getElementById('register-password').value,
                firstName: document.getElementById('register-firstname').value,
                lastName: document.getElementById('register-lastname').value
            };

            const result = await authService.register(userData);
            if (result.success) {
                alert('Регистрация успешна! Теперь войдите в систему.');
                closeModal('register-modal');
                showLogin();
            } else {
                alert('Ошибка: ' + result.message);
            }
        });
    }
});

// Функции для модальных окон
function showLogin() {
    document.getElementById('login-modal').style.display = 'block';
}

function showRegister() {
    document.getElementById('register-modal').style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Интерцептор для автоматической подстановки токена
function createAuthFetch() {
    const originalFetch = window.fetch;

    window.fetch = async function(...args) {
        const [url, options = {}] = args;

        // Пропускаем запросы к статическим файлам и публичным endpoint'ам
        if (typeof url === 'string' &&
            (url.includes('.css') || url.includes('.js') || url.includes('.html') ||
                url.includes('/api/auth/') || url === '/api/users/register')) {
            return originalFetch.apply(this, args);
        }

        // Добавляем токен к запросам к API
        if (typeof url === 'string' && url.includes('/api/') && authService.token) {
            const newOptions = {
                ...options,
                headers: {
                    ...options.headers,
                    'Authorization': `Bearer ${authService.token}`
                }
            };

            return originalFetch.call(this, url, newOptions);
        }

        return originalFetch.apply(this, args);
    };
}

// Инициализируем интерцептор при загрузке
document.addEventListener('DOMContentLoaded', function() {
    createAuthFetch();
});

// Закрытие модальных окон при клике вне контента
window.onclick = function(event) {
    const modals = document.getElementsByClassName('modal');
    for (let modal of modals) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    }
}