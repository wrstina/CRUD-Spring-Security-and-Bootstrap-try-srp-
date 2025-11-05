async function fetchJson(url, options) {
    const res = await fetch(url, options);
    const ct = res.headers.get('content-type') || '';
    const data = ct.includes('application/json') ? await res.json() : null;
    if (!res.ok) {
        const err = new Error(data?.message || `${res.status} ${res.statusText}`);
        err.status = res.status;
        throw err;
    }
    return data;
}

function renderProfile(u) {
    const el = document.getElementById('profile');
    el.innerHTML = `
    <dt class="col-sm-2">Username</dt><dd class="col-sm-10">${u.username}</dd>
    <dt class="col-sm-2">Email</dt><dd class="col-sm-10">${u.email}</dd>
    <dt class="col-sm-2">Age</dt><dd class="col-sm-10">${u.age}</dd>
    <dt class="col-sm-2">Roles</dt>
    <dd class="col-sm-10">${u.roles.map(r=>`<span class="badge rounded-pill badge-rose me-1">${r}</span>`).join('')}</dd>
  `;
}

(async function init(){
    try {
        const me = await fetchJson('/api/users/me');
        renderProfile(me);
    } catch (err) {
        if (err.status === 401) return location.href = '/login';
        const box = document.getElementById('alert');
        box.textContent = err.message;
        box.classList.remove('d-none');
    }
})();
