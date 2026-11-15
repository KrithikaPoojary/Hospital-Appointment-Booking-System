// Auth & API Helper utilities

const API = {
    getToken() {
        return localStorage.getItem('token');
    },

    getUser() {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    setSession(token, user) {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
    },

    async request(url, options = {}) {
        const token = this.getToken();
        const headers = options.headers || {};

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        options.headers = headers;

        try {
            const res = await fetch(url, options);
            if (res.status === 401) {
                this.logout();
                return null;
            }
            const data = await res.json().catch(() => null);
            if (!res.ok) {
                const message = (data && (data.message || data.error)) || 'Request failed';
                throw new Error(message);
            }
            return data;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    }
};

// Update Navbar links based on login state
document.addEventListener('DOMContentLoaded', () => {
    const user = API.getUser();
    const guestNav = document.getElementById('guestNav');
    const userNav = document.getElementById('userNav');
    const userDisplay = document.getElementById('userDisplay');
    const roleNavPatient = document.getElementById('roleNavPatient');
    const roleNavDoctor = document.getElementById('roleNavDoctor');
    const roleNavAdmin = document.getElementById('roleNavAdmin');

    if (user && API.isLoggedIn()) {
        if (guestNav) guestNav.classList.add('d-none');
        if (userNav) userNav.classList.remove('d-none');
        if (userDisplay) userDisplay.textContent = `${user.username} (${user.role})`;

        if (user.role === 'PATIENT' && roleNavPatient) {
            roleNavPatient.classList.remove('d-none');
        } else if (user.role === 'DOCTOR' && roleNavDoctor) {
            roleNavDoctor.classList.remove('d-none');
        } else if (user.role === 'ADMIN' && roleNavAdmin) {
            roleNavAdmin.classList.remove('d-none');
        }
    } else {
        if (guestNav) guestNav.classList.remove('d-none');
        if (userNav) userNav.classList.add('d-none');
    }

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            API.logout();
        });
    }
});

function showAlert(message, type = 'success', containerId = 'alertContainer') {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}
