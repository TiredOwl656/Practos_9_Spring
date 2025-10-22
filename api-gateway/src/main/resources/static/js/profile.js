class ProfileService {
    constructor() {
        this.authService = authService;
        this.currentUserId = null;
        this.currentUserProfile = null;
    }

    // Инициализация - получение текущего пользователя
    async initialize() {
        if (!this.authService.isAuthenticated()) {
            console.error('User not authenticated');
            return false;
        }

        this.currentUserProfile = await this.getCurrentUserProfile();
        if (this.currentUserProfile) {
            this.currentUserId = this.currentUserProfile.id;
            console.log('ProfileService initialized for user:', this.currentUserProfile.username, 'ID:', this.currentUserId);
            return true;
        }

        console.error('Failed to get current user profile');
        return false;
    }

    // Получение текущего пользователя по токену
    async getCurrentUserProfile() {
        try {
            console.log('Getting current user profile...');

            // Пробуем получить через специальный endpoint
            const response = await fetch('/api/users/profile/me', {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Profile me response status:', response.status);

            if (response.ok) {
                const user = await response.json();
                console.log('Current user profile from /me:', user);
                return user;
            }

            // Fallback: получаем всех пользователей и ищем по username
            console.log('Trying fallback method to get user profile...');
            const username = this.getUsernameFromToken();
            if (!username) {
                console.error('Cannot get username from token');
                return null;
            }

            const usersResponse = await fetch('/api/users', {
                headers: this.authService.getAuthHeaders()
            });

            if (usersResponse.ok) {
                const users = await usersResponse.json();
                const user = users.find(u => u.username === username);
                console.log('Found user from all users list:', user);
                return user || null;
            }

            console.error('Failed to get users list');
            return null;
        } catch (error) {
            console.error('Error getting current user profile:', error);
            return null;
        }
    }

    // Получение username из токена
    getUsernameFromToken() {
        if (!this.authService.token) {
            console.error('No token available');
            return null;
        }
        try {
            const payload = JSON.parse(atob(this.authService.token.split('.')[1]));
            console.log('Token payload:', payload);
            return payload.sub;
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    }

    // Получение информации о пользователе по ID
    async getUserProfile(userId) {
        try {
            console.log('Getting user profile for ID:', userId);
            const response = await fetch(`/api/users/${userId}`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('User profile response status:', response.status);

            if (response.ok) {
                const user = await response.json();
                console.log('User profile data:', user);
                return user;
            }
            console.error('Failed to get user profile, status:', response.status);
            throw new Error('Ошибка загрузки профиля');
        } catch (error) {
            console.error('Ошибка загрузки профиля:', error);
            return null;
        }
    }

    // Получение статистики для текущего пользователя
    async getCurrentUserStats() {
        if (!this.currentUserId) {
            console.error('No current user ID available');
            return { followersCount: 0, followingCount: 0, postsCount: 0 };
        }

        return await this.getProfileStats(this.currentUserId);
    }

    // Получение статистики по ID пользователя
    async getProfileStats(userId) {
        try {
            console.log('Getting stats for user ID:', userId);
            const [followersCount, followingCount, postsCount] = await Promise.all([
                this.getFollowersCount(userId),
                this.getFollowingCount(userId),
                this.getUserPostsCount(userId)
            ]);

            console.log('User stats:', { followersCount, followingCount, postsCount });

            return { followersCount, followingCount, postsCount };
        } catch (error) {
            console.error('Ошибка загрузки статистики:', error);
            return { followersCount: 0, followingCount: 0, postsCount: 0 };
        }
    }

    // Количество подписчиков
    async getFollowersCount(userId) {
        try {
            console.log('Getting followers count for user ID:', userId);
            const response = await fetch(`/api/feed/${userId}/followers-count`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Followers count response status:', response.status);

            if (response.ok) {
                const count = await response.json();
                console.log('Followers count:', count);
                return count;
            }
            console.error('Failed to get followers count, status:', response.status);
            return 0;
        } catch (error) {
            console.error('Error getting followers count:', error);
            return 0;
        }
    }

    // Количество подписок
    async getFollowingCount(userId) {
        try {
            console.log('Getting following count for user ID:', userId);
            const response = await fetch(`/api/feed/${userId}/following-count`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Following count response status:', response.status);

            if (response.ok) {
                const count = await response.json();
                console.log('Following count:', count);
                return count;
            }
            console.error('Failed to get following count, status:', response.status);
            return 0;
        } catch (error) {
            console.error('Error getting following count:', error);
            return 0;
        }
    }

    // Количество постов пользователя
    async getUserPostsCount(userId) {
        try {
            console.log('Getting posts count for user ID:', userId);
            const posts = await this.getUserPosts(userId);
            const count = posts.length;
            console.log('Posts count:', count);
            return count;
        } catch (error) {
            console.error('Error getting posts count:', error);
            return 0;
        }
    }

    // Получение постов текущего пользователя
    async getCurrentUserPosts() {
        if (!this.currentUserId) {
            console.error('No current user ID available for getting posts');
            return [];
        }

        return await this.getUserPosts(this.currentUserId);
    }

    // Получение постов по ID пользователя
    async getUserPosts(userId) {
        try {
            console.log('Getting posts for user ID:', userId);
            const response = await fetch(`/api/posts/author/${userId}`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('User posts response status:', response.status);

            if (response.ok) {
                const posts = await response.json();
                console.log('User posts loaded:', posts.length, 'posts');
                return posts;
            }
            console.error('Failed to get user posts, status:', response.status);
            return [];
        } catch (error) {
            console.error('Ошибка загрузки постов:', error);
            return [];
        }
    }

    // Поиск пользователей
    async searchUsers(query) {
        try {
            console.log('Searching users with query:', query);
            // Используем существующий endpoint для получения всех пользователей
            const response = await fetch('/api/users', {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Users search response status:', response.status);

            if (response.ok) {
                const allUsers = await response.json();
                // Фильтруем по username и исключаем текущего пользователя
                const filteredUsers = allUsers.filter(user =>
                    user.username.toLowerCase().includes(query.toLowerCase()) &&
                    user.id !== this.currentUserId
                );
                console.log('Search results:', filteredUsers.length, 'users');
                return filteredUsers;
            }
            console.error('Failed to search users, status:', response.status);
            return [];
        } catch (error) {
            console.error('Ошибка поиска:', error);
            return [];
        }
    }

    // Подписка на пользователя
    async subscribeToUser(followingId, followingUsername) {
        try {
            if (!this.currentUserId || !this.currentUserProfile) {
                throw new Error('User not initialized');
            }

            console.log('Subscribing:', this.currentUserId, '->', followingId);

            const response = await fetch(
                `/api/feed/subscribe?` +
                `followerId=${this.currentUserId}&` +
                `followerUsername=${this.currentUserProfile.username}&` +
                `followingId=${followingId}&` +
                `followingUsername=${followingUsername}`,
                {
                    method: 'POST',
                    headers: this.authService.getAuthHeaders()
                }
            );

            console.log('Subscribe response status:', response.status);
            return response.ok;
        } catch (error) {
            console.error('Ошибка подписки:', error);
            return false;
        }
    }

    // Создание поста от текущего пользователя
    async createPost(postData) {
        try {
            if (!this.currentUserId || !this.currentUserProfile) {
                throw new Error('User not initialized');
            }

            // Добавляем информацию об авторе
            postData.authorId = this.currentUserId;
            postData.authorUsername = this.currentUserProfile.username;

            console.log('Creating post with data:', postData);

            const response = await fetch('/api/posts', {
                method: 'POST',
                headers: this.authService.getAuthHeaders(),
                body: JSON.stringify(postData)
            });

            console.log('Create post response status:', response.status);

            if (response.ok) {
                const newPost = await response.json();
                console.log('Post created successfully:', newPost);

                // Уведомляем feed-service о новом посте для добавления в ленты подписчиков
                await this.notifyNewPost(newPost.id);

                return newPost;
            }
            throw new Error('Ошибка создания поста');
        } catch (error) {
            console.error('Ошибка создания поста:', error);
            return null;
        }
    }

    // Уведомление о новом посте
    async notifyNewPost(postId) {
        try {
            if (!this.currentUserId || !this.currentUserProfile) {
                return;
            }

            console.log('Notifying about new post:', postId);
            await fetch(
                `/api/feed/notify-new-post?` +
                `postId=${postId}&` +
                `authorId=${this.currentUserId}&` +
                `authorUsername=${this.currentUserProfile.username}`,
                {
                    method: 'POST',
                    headers: this.authService.getAuthHeaders()
                }
            );
            console.log('New post notification sent');
        } catch (error) {
            console.error('Error notifying about new post:', error);
        }
    }

    // Удаление поста
    async deletePost(postId) {
        try {
            console.log('Deleting post:', postId);
            const response = await fetch(`/api/posts/${postId}`, {
                method: 'DELETE',
                headers: this.authService.getAuthHeaders()
            });

            console.log('Delete post response status:', response.status);
            return response.ok;
        } catch (error) {
            console.error('Ошибка удаления поста:', error);
            return false;
        }
    }

    // Получение подписок текущего пользователя
    async getCurrentUserSubscriptions() {
        if (!this.currentUserId) {
            console.error('No current user ID available for subscriptions');
            return [];
        }

        return await this.getUserSubscriptions(this.currentUserId);
    }

    // Получение подписок по ID пользователя
    async getUserSubscriptions(userId) {
        try {
            console.log('Getting subscriptions for user ID:', userId);
            const response = await fetch(`/api/feed/${userId}/subscriptions`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Subscriptions response status:', response.status);

            if (response.ok) {
                const subscriptions = await response.json();
                console.log('Subscriptions loaded:', subscriptions.length);
                return subscriptions;
            }
            console.error('Failed to get subscriptions, status:', response.status);
            return [];
        } catch (error) {
            console.error('Ошибка загрузки подписок:', error);
            return [];
        }
    }

    // Обновление поста
    async updatePost(postId, postData) {
        try {
            console.log('Updating post:', postId, 'with data:', postData);
            const response = await fetch(`/api/posts/${postId}`, {
                method: 'PATCH',
                headers: this.authService.getAuthHeaders(),
                body: JSON.stringify(postData)
            });

            console.log('Update post response status:', response.status);

            if (response.ok) {
                const updatedPost = await response.json();
                console.log('Post updated successfully:', updatedPost);
                return updatedPost;
            }
            throw new Error('Ошибка обновления поста');
        } catch (error) {
            console.error('Ошибка обновления поста:', error);
            return null;
        }
    }

    // Получение поста по ID
    async getPostById(postId) {
        try {
            console.log('Getting post by ID:', postId);
            const response = await fetch(`/api/posts/${postId}`, {
                headers: this.authService.getAuthHeaders()
            });

            console.log('Get post response status:', response.status);

            if (response.ok) {
                const post = await response.json();
                console.log('Post data:', post);
                return post;
            }
            return null;
        } catch (error) {
            console.error('Ошибка получения поста:', error);
            return null;
        }
    }
}

function renderUserInfo(user) {
    const profileInfo = document.getElementById('profile-info');
    if (!profileInfo) {
        console.error('Profile info element not found');
        return;
    }

    console.log('Rendering user info:', user);

    profileInfo.innerHTML = `
        <div class="user-card">
            <div class="user-avatar">
                ${user.username ? user.username.charAt(0).toUpperCase() : '?'}
            </div>
            <div class="user-details">
                <h3>${user.username || 'Неизвестный пользователь'}</h3>
                <p class="user-email">${user.email || ''}</p>
                <p class="user-name">${user.firstName || ''} ${user.lastName || ''}</p>
                <p class="user-role">${user.role || 'USER'}</p>
                <p class="user-id">ID: ${user.id || 'неизвестен'}</p>
                <p class="user-since">Зарегистрирован: ${user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'неизвестно'}</p>
            </div>
        </div>
    `;
}

// Инициализация страницы профиля
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Profile page loaded');

    if (!authService.isAuthenticated()) {
        console.log('User not authenticated, redirecting to login');
        window.location.href = '/login.html';
        return;
    }

    console.log('User is authenticated, initializing profile service...');

    // Инициализируем профиль сервиса
    const initialized = await profileService.initialize();
    if (!initialized) {
        console.error('Failed to initialize profile service');
        showError('Ошибка загрузки профиля. Пожалуйста, войдите заново.');
        return;
    }

    console.log('Profile service initialized successfully');
    await loadProfile();
    await loadUserPosts();
    setupEventListeners();
});

async function loadProfile() {
    console.log('Loading profile data...');

    if (!profileService.currentUserId) {
        console.error('No current user ID available');
        showError('Не удалось определить пользователя');
        return;
    }

    console.log('Loading profile for user ID:', profileService.currentUserId);

    // Загружаем информацию о пользователе
    const user = await profileService.getUserProfile(profileService.currentUserId);
    if (user) {
        renderUserInfo(user);
    } else {
        // Используем данные из токена как fallback
        console.log('Using fallback user data from token');
        renderUserInfo(profileService.currentUserProfile);
    }

    // Загружаем статистику для текущего пользователя
    const stats = await profileService.getCurrentUserStats();
    renderStats(stats);

    console.log('Profile data loaded successfully');
}

async function loadUserPosts() {
    console.log('Loading user posts...');

    if (!profileService.currentUserId) {
        console.error('No current user ID available for loading posts');
        return;
    }

    console.log('Loading posts for user ID:', profileService.currentUserId);

    const posts = await profileService.getCurrentUserPosts();
    renderUserPosts(posts);

    console.log('User posts loaded:', posts.length, 'posts');
}

// Функция для загрузки постов другого пользователя (для просмотра чужих профилей)
async function loadOtherUserPosts(userId) {
    console.log('Loading posts for other user ID:', userId);

    const posts = await profileService.getUserPosts(userId);
    renderUserPosts(posts);

    console.log('Other user posts loaded:', posts.length, 'posts');
}