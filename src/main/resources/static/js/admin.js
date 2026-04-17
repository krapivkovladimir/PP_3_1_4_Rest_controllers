const adminState = {
    users: [],
    roles: []
};

const adminAlert = document.getElementById('adminAlert');
const usersTableBody = document.getElementById('usersTableBody');
const newUserForm = document.getElementById('newUserForm');
const editUserForm = document.getElementById('editUserForm');
const deleteUserForm = document.getElementById('deleteUserForm');
const currentUserEmail = document.getElementById('currentUserEmail');
const currentUserRoles = document.getElementById('currentUserRoles');
const editModalElement = document.getElementById('editModal');
const deleteModalElement = document.getElementById('deleteModal');
const editModal = new bootstrap.Modal(editModalElement);
const deleteModal = new bootstrap.Modal(deleteModalElement);

document.addEventListener('DOMContentLoaded', async () => {
    bindAdminEvents();
    await loadAdminPage();
});

function bindAdminEvents() {
    newUserForm.addEventListener('submit', handleCreateUser);
    editUserForm.addEventListener('submit', handleEditUser);
    deleteUserForm.addEventListener('submit', handleDeleteUser);

    usersTableBody.addEventListener('click', (event) => {
        const editButton = event.target.closest('[data-action="edit"]');
        const deleteButton = event.target.closest('[data-action="delete"]');

        if (editButton) {
            openEditModal(Number(editButton.dataset.userId));
        }

        if (deleteButton) {
            openDeleteModal(Number(deleteButton.dataset.userId));
        }
    });
}

async function loadAdminPage() {
    try {
        hideAlert();
        const [currentUser, roles, users] = await Promise.all([
            fetchJson('/api/user/current'),
            fetchJson('/api/admin/roles'),
            fetchJson('/api/admin/users')
        ]);

        adminState.roles = roles;
        adminState.users = users;

        fillCurrentUser(currentUser);
        fillRoleSelect(document.getElementById('newRoles'), roles);
        fillRoleSelect(document.getElementById('editRoles'), roles);
        fillRoleSelect(document.getElementById('deleteRoles'), roles);
        renderUsers(users);
    } catch (error) {
        showAlert(error.message);
    }
}

async function refreshUsers() {
    adminState.users = await fetchJson('/api/admin/users');
    renderUsers(adminState.users);
}

function renderUsers(users) {
    usersTableBody.innerHTML = users.map((user) => `
        <tr>
            <td>${user.id}</td>
            <td>${escapeHtml(user.name)}</td>
            <td>${escapeHtml(user.lastname)}</td>
            <td>${user.age}</td>
            <td>${escapeHtml(user.username)}</td>
            <td>${formatRoles(user.roles)}</td>
            <td>
                <button type="button"
                        class="btn btn-info btn-sm text-white"
                        data-action="edit"
                        data-user-id="${user.id}">
                    Edit
                </button>
            </td>
            <td>
                <button type="button"
                        class="btn btn-danger btn-sm"
                        data-action="delete"
                        data-user-id="${user.id}">
                    Delete
                </button>
            </td>
        </tr>
    `).join('');
}

function fillCurrentUser(user) {
    currentUserEmail.textContent = user.username;
    currentUserRoles.textContent = formatRoles(user.roles);
}

function fillRoleSelect(select, roles, selectedNames = []) {
    select.innerHTML = roles.map((role) => `
        <option value="${role.name}" ${selectedNames.includes(role.name) ? 'selected' : ''}>
            ${formatRoleName(role.name)}
        </option>
    `).join('');
}

function openEditModal(userId) {
    const user = adminState.users.find((item) => item.id === userId);
    if (!user) {
        showAlert('User not found');
        return;
    }

    document.getElementById('editId').value = user.id;
    document.getElementById('editIdView').value = user.id;
    document.getElementById('editName').value = user.name;
    document.getElementById('editLastname').value = user.lastname;
    document.getElementById('editAge').value = user.age;
    document.getElementById('editUsername').value = user.username;
    document.getElementById('editPassword').value = '';
    fillRoleSelect(
        document.getElementById('editRoles'),
        adminState.roles,
        user.roles.map((role) => role.name)
    );

    editModal.show();
}

function openDeleteModal(userId) {
    const user = adminState.users.find((item) => item.id === userId);
    if (!user) {
        showAlert('User not found');
        return;
    }

    document.getElementById('deleteId').value = user.id;
    document.getElementById('deleteIdView').value = user.id;
    document.getElementById('deleteName').value = user.name;
    document.getElementById('deleteLastname').value = user.lastname;
    document.getElementById('deleteAge').value = user.age;
    document.getElementById('deleteUsername').value = user.username;
    fillRoleSelect(
        document.getElementById('deleteRoles'),
        adminState.roles,
        user.roles.map((role) => role.name)
    );

    deleteModal.show();
}

async function handleCreateUser(event) {
    event.preventDefault();

    try {
        hideAlert();
        await fetchJson('/api/admin/users', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(collectFormData(newUserForm))
        });

        newUserForm.reset();
        await refreshUsers();
        bootstrap.Tab.getOrCreateInstance(document.getElementById('users-tab')).show();
    } catch (error) {
        showAlert(error.message);
    }
}

async function handleEditUser(event) {
    event.preventDefault();

    const userId = document.getElementById('editId').value;

    try {
        hideAlert();
        await fetchJson(`/api/admin/users/${userId}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(collectEditFormData())
        });

        await refreshUsers();
        editModal.hide();
    } catch (error) {
        showAlert(error.message);
    }
}

async function handleDeleteUser(event) {
    event.preventDefault();

    const userId = document.getElementById('deleteId').value;

    try {
        hideAlert();
        await fetchJson(`/api/admin/users/${userId}`, {
            method: 'DELETE'
        });

        await refreshUsers();
        deleteModal.hide();
    } catch (error) {
        showAlert(error.message);
    }
}

function collectFormData(form) {
    const formData = new FormData(form);

    return {
        name: String(formData.get('name')).trim(),
        lastname: String(formData.get('lastname')).trim(),
        age: Number(formData.get('age')),
        username: String(formData.get('username')).trim(),
        password: String(formData.get('password')),
        roles: Array.from(document.getElementById('newRoles').selectedOptions).map((option) => option.value)
    };
}

function collectEditFormData() {
    return {
        name: document.getElementById('editName').value.trim(),
        lastname: document.getElementById('editLastname').value.trim(),
        age: Number(document.getElementById('editAge').value),
        username: document.getElementById('editUsername').value.trim(),
        password: document.getElementById('editPassword').value,
        roles: Array.from(document.getElementById('editRoles').selectedOptions).map((option) => option.value)
    };
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `Request failed with status ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function formatRoles(roles) {
    return roles.map((role) => formatRoleName(role.name)).join(' ');
}

function formatRoleName(roleName) {
    return roleName.replace('ROLE_', '');
}

function showAlert(message) {
    adminAlert.textContent = message;
    adminAlert.classList.remove('d-none');
}

function hideAlert() {
    adminAlert.textContent = '';
    adminAlert.classList.add('d-none');
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
