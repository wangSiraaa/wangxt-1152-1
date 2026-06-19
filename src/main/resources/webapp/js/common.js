const API_BASE = '/api/forest';

let currentUser = JSON.parse(localStorage.getItem('forest_user') || 'null');

function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (currentUser) {
        headers['userId'] = currentUser.pk_forest_user;
        headers['pkOrg'] = currentUser.pk_org;
        headers['pkGroup'] = currentUser.pk_group;
    }
    return headers;
}

async function apiGet(url, params = {}) {
    const query = Object.keys(params).map(k => `${k}=${encodeURIComponent(params[k])}`).join('&');
    const fullUrl = query ? `${url}?${query}` : url;
    const res = await fetch(fullUrl, { headers: getHeaders() });
    return await res.json();
}

async function apiPost(url, data = {}) {
    const res = await fetch(url, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    return await res.json();
}

function requireLogin(role) {
    if (!currentUser) {
        window.location.href = 'login.html?role=' + role;
        return false;
    }
    return true;
}

function logout() {
    localStorage.removeItem('forest_user');
    window.location.href = 'index.html';
}

function getUserInfo() {
    return currentUser;
}

function showAlert(message, type = 'info') {
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999;min-width:300px;';
    alert.textContent = message;
    document.body.appendChild(alert);
    setTimeout(() => alert.remove(), 3000);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    return dateStr.substring(0, 10);
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    return dateStr;
}

function getRiskBadge(level) {
    switch (level) {
        case 1: return '<span class="badge badge-success">低风险</span>';
        case 2: return '<span class="badge badge-warning">中风险</span>';
        case 3: return '<span class="badge badge-danger">高风险</span>';
        default: return '<span class="badge badge-pending">未评估</span>';
    }
}

function getStatusBadge(status) {
    switch (status) {
        case 0: return '<span class="badge badge-pending">待复核</span>';
        case 1: return '<span class="badge badge-info">已复核</span>';
        case 2: return '<span class="badge badge-success">已处置</span>';
        default: return '-';
    }
}

function getDisposalStatusBadge(status) {
    switch (status) {
        case 0: return '<span class="badge badge-pending">待处置</span>';
        case 1: return '<span class="badge badge-warning">处置中</span>';
        case 2: return '<span class="badge badge-success">已完成</span>';
        default: return '-';
    }
}

function getSuspectBadge(isSuspect) {
    if (isSuspect == 1) {
        return '<span class="badge badge-danger">疑似检疫</span>';
    }
    return '<span class="badge badge-success">正常</span>';
}

function getKeyPatrolBadge(isKey) {
    if (isKey == 1) {
        return '<span class="badge badge-key">重点巡查</span>';
    }
    return '<span class="badge badge-info">常规</span>';
}

function showModal(title, contentHtml, onOk) {
    const mask = document.createElement('div');
    mask.className = 'modal-mask';
    mask.innerHTML = `
        <div class="modal-box">
            <div class="modal-header">
                <span class="modal-title">${title}</span>
                <button class="modal-close">&times;</button>
            </div>
            <div class="modal-body">${contentHtml}</div>
            <div class="modal-footer">
                <button class="btn btn-default modal-cancel">取消</button>
                <button class="btn btn-primary modal-ok">确定</button>
            </div>
        </div>
    `;
    document.body.appendChild(mask);

    mask.querySelector('.modal-close').onclick = () => mask.remove();
    mask.querySelector('.modal-cancel').onclick = () => mask.remove();
    mask.querySelector('.modal-ok').onclick = () => {
        if (onOk) {
            const result = onOk(mask);
            if (result !== false) mask.remove();
        } else {
            mask.remove();
        }
    };
    mask.onclick = (e) => { if (e.target === mask) mask.remove(); };

    return mask;
}

function renderSidebar(activeMenu) {
    const user = currentUser;
    if (!user) return '';

    const roleMenus = {
        1: [
            { key: 'trap', label: '诱捕器点位', icon: '📍' },
            { key: 'record', label: '虫情登记', icon: '🐛' },
            { key: 'recordList', label: '登记记录', icon: '📋' }
        ],
        2: [
            { key: 'pending', label: '待复核列表', icon: '⏳' },
            { key: 'reviewed', label: '已复核列表', icon: '✅' },
            { key: 'keyPatrol', label: '重点巡查点位', icon: '⭐' }
        ],
        3: [
            { key: 'pending', label: '待处置列表', icon: '📦' },
            { key: 'processing', label: '处置中列表', icon: '⚙️' },
            { key: 'completed', label: '已完成列表', icon: '✅' }
        ]
    };

    const menus = roleMenus[user.user_role] || [];
    const menuHtml = menus.map(m =>
        `<li class="${activeMenu === m.key ? 'active' : ''}" data-key="${m.key}">
            <span style="margin-right:8px">${m.icon}</span>${m.label}
        </li>`
    ).join('');

    return `
        <div class="app-sidebar" style="position:relative">
            <div class="sidebar-header">
                <div class="sidebar-title">🌲 监测系统</div>
                <div class="sidebar-role">${user.role_name}工作台</div>
            </div>
            <ul class="sidebar-menu" id="sidebarMenu">
                ${menuHtml}
            </ul>
            <div class="sidebar-user">
                <div class="user-name">👤 ${user.user_name}</div>
                <div class="user-logout" onclick="logout()">退出登录</div>
            </div>
        </div>
    `;
}
