const userAlert = document.getElementById('userAlert');
const userEmail = document.getElementById('userEmail');
const userRoles = document.getElementById('userRoles');
const userTableBody = document.getElementById('userTableBody');
const userSidebarNav = document.getElementById('userSidebarNav');

document.addEventListener('DOMContentLoaded', loadUserPage);

async function loadUserPage() {
    try {
        hideUserAlert();
        const user = await fetchCurrentUser();

        userEmail.textContent = user.username;
        userRoles.textContent = formatRoles(user.roles);
        renderSidebar(user.roles);
        userTableBody.innerHTML = `
            <tr>
                <td>${user.id}</td>
                <td>${escapeHtml(user.name)}</td>
                <td>${escapeHtml(user.lastname)}</td>
                <td>${user.age}</td>
                <td>${escapeHtml(user.username)}</td>
                <td>${formatRoles(user.roles)}</td>
            </tr>
        `;
    } catch (error) {
        showUserAlert(error.message);
    }
}

async function fetchCurrentUser() {
    const response = await fetch('/api/user/current');

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `Request failed with status ${response.status}`);
    }

    return response.json();
}

function renderSidebar(roles) {
    const isAdmin = roles.some((role) => role.name === 'ROLE_ADMIN');

    userSidebarNav.innerHTML = `
        ${isAdmin ? '<a class="nav-link rounded-0" href="/admin">Admin</a>' : ''}
        <a class="nav-link active rounded-0" href="/user">User</a>
    `;
}

function formatRoles(roles) {
    return roles.map((role) => role.name.replace('ROLE_', '')).join(' ');
}

function showUserAlert(message) {
    userAlert.textContent = message;
    userAlert.classList.remove('d-none');
}

function hideUserAlert() {
    userAlert.textContent = '';
    userAlert.classList.add('d-none');
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
