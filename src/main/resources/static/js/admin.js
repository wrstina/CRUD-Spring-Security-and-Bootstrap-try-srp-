// ========= Admin UI (розовые и ровные кнопки) =========
(function () {
    "use strict";

    const $$  = (s, r=document) => r.querySelector(s);
    const $$$ = (s, r=document) => Array.from(r.querySelectorAll(s));
    const $id = (id) => document.getElementById(id.replace(/^#/, ""));

    function showBanner(msg, type="danger"){
        const el = $id("banner");
        el.className = `alert alert-${type}`;
        el.textContent = msg;
        el.classList.remove("d-none");
        clearTimeout(showBanner._t);
        showBanner._t = setTimeout(()=>el.classList.add("d-none"), 6000);
    }
    function hideBanner(){ $id("banner").classList.add("d-none"); }

    function clearFieldErrors(form){
        $$$(".is-invalid", form).forEach(i=>i.classList.remove("is-invalid"));
        $$$(".invalid-feedback", form).forEach(f=>f.textContent="");
    }
    function setFieldError(form, field, msg){
        const simple = ["username","email","password","age"];
        if (simple.includes(field)) {
            const input = $$( `#${field}`, form );
            if (input) {
                input.classList.add("is-invalid");
                const fb = $$(`.invalid-feedback[data-for="${field}"]`, form);
                if (fb) fb.textContent = msg;
            }
            return;
        }
        if (field.toLowerCase().includes("role")){
            const fb = $$(`.invalid-feedback[data-for="roles"]`, form);
            if (fb) fb.textContent = msg;
        }
    }
    const esc = (s) => String(s ?? "").replace(/[&<>"']/g, m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));

    // ---------- HTTP + авто-поиск API
    let API_BASE = null;

    async function http(method, url, body){
        const resp = await fetch(url, {
            method,
            headers:{ "Content-Type":"application/json", "X-Requested-With":"fetch" },
            credentials:"same-origin",
            body: body? JSON.stringify(body): undefined
        });
        let data=null; try{ data=await resp.json(); }catch(_){}

        if (resp.status===401){
            showBanner("Сессия истекла (401). Переход на вход…", "warning");
            setTimeout(()=>location.assign("/login"), 800);
            throw new Error("401");
        }
        if(!resp.ok){
            const err = new Error((data && (data.message||data.error))||`${resp.status} ${resp.statusText}`);
            err.status  = resp.status;
            err.errors  = (data && (data.errors || data.fields)) || {}; // поддерживаем 400/422
            err.payload = data;
            throw err;
        }
        return data;
    }

    async function discoverApiBase(){
        const meta = $$('meta[name="api-base"]')?.content?.trim();
        const strip = s => s.replace(/\/+$/,"");
        const path = strip(location.pathname);
        const cand = [];
        if (meta) cand.push(meta);
        if (path && path!=="/"){ cand.push(`${path}/api`, path); }
        cand.push("/api/admin","/admin/api","/api","/admin");
        const uniq = a => Array.from(new Set(a.filter(Boolean)));
        for (const base of uniq(cand)){
            try{
                const resp = await fetch(`${strip(base)}/users`, {credentials:"same-origin"});
                if (resp.ok && Array.isArray(await resp.json().catch(()=>null))) return strip(base);
            }catch(_){}
        }
        return null;
    }

    const API = {
        listUsers: () => http("GET", `${API_BASE}/users`),
        listRoles: () => http("GET", `${API_BASE}/roles`),
        createUser: (dto) => http("POST", `${API_BASE}/users`, dto),
        updateUser: (id, dto) => http("PUT", `${API_BASE}/users/${id}`, dto),
        deleteUser: (id) => http("DELETE", `${API_BASE}/users/${id}`)
    };

    // ---------- Render
    function rolesToText(user){
        if (!user.roles) return "";
        return user.roles.map(r => typeof r==="string"? r : r.name).join(", ");
    }

    function renderRow(u){
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${u.id}</td>
      <td>${esc(u.username)}</td>
      <td>${esc(u.email)}</td>
      <td>${u.age ?? ""}</td>
      <td>${esc(rolesToText(u))}</td>
      <td class="text-end">
        <div class="actions">
          <button class="btn btn-rose-outline btn-sm btn-edit" data-id="${u.id}">Изменить</button>
          <button class="btn btn-rose btn-sm btn-delete" data-id="${u.id}" data-name="${esc(u.username)}">Удалить</button>
        </div>
      </td>
    `;
        return tr;
    }

    async function reloadTable(){
        const tbody = $id("usersTbody");
        tbody.innerHTML = "";
        try{
            const users = await API.listUsers();
            users.forEach(u => tbody.appendChild(renderRow(u)));
        }catch(e){
            showBanner(`Не удалось загрузить пользователей (${e.status ?? ""}). ${e.message}`, "danger");
        }
    }

    // ---------- Roles
    let ALL_ROLES = []; // [{id, name:"ROLE_USER"}, ...]
    const normName = (n) => n?.startsWith("ROLE_") ? n.slice(5) : n;

    function drawRolesCheckboxes(container, selected=[]){
        container.innerHTML = "";
        const selectedSet = new Set(selected.map(String).map(normName));
        for (const role of ALL_ROLES){
            const label = normName(role.name);   // показываем без префикса
            const value = role.id;               // на сервер отправляем ID
            const id    = `role_${label}`;
            const checked = selectedSet.has(label);
            const wrap = document.createElement("div");
            wrap.className = "form-check form-check-inline";
            wrap.innerHTML = `
        <input class="form-check-input" type="checkbox" id="${id}" value="${value}" ${checked?"checked":""}>
        <label class="form-check-label" for="${id}">${label}</label>
      `;
            container.appendChild(wrap);
        }
    }
    function collectSelectedRoleIds(container){
        return $$$('input[type="checkbox"]:checked', container)
            .map(i => Number(i.value))
            .filter(n => Number.isFinite(n));
    }

    // ---------- Anti-autofill helpers
    function disableAutofillForCreate(form){
        // 1) Обрубим автозаполнение на форме и полях
        form.setAttribute("autocomplete","off");

        const inputs = ["username","email","password","age"]
            .map(id => $id(id))
            .filter(Boolean);

        inputs.forEach(i=>{
            i.setAttribute("autocomplete", i.id==="password" ? "new-password" : "off");
            // 2) Сменим name, чтобы менеджер паролей не подставлял сохранённые значения
            i.setAttribute("data-orig-name", i.getAttribute("name") || "");
            i.setAttribute("name", `nf_${i.id}_${Date.now()}`);
            // 3) Полностью очищаем значение и снимаем возможный autofill
            i.value = "";
            i.readOnly = true; // хак против мгновенного автозаполнения
            setTimeout(()=>{ i.readOnly = false; i.value=""; }, 120);
            setTimeout(()=>{ i.value=""; }, 260); // на случай «упрямого» Chrome
        });

        // 4) Honeypot-поля: куда браузер «сольёт» автозаполнение, а не в реальные input’ы
        if (!form.querySelector(".hp-anti-autofill")){
            const hpU = document.createElement("input");
            hpU.type = "text";
            hpU.autocomplete = "username";
            hpU.tabIndex = -1;

            const hpP = document.createElement("input");
            hpP.type = "password";
            hpP.autocomplete = "current-password";
            hpP.tabIndex = -1;

            [hpU,hpP].forEach(hp=>{
                hp.className = "hp-anti-autofill";
                hp.style.position = "fixed";
                hp.style.left = "-9999px";
                hp.style.width = "1px";
                hp.style.height = "1px";
                hp.style.opacity = "0";
            });
            form.prepend(hpP);
            form.prepend(hpU);
        }
    }

    function restoreNamesAfterEdit(form){
        // для редактирования нам не важно имя полей (мы читаем по id),
        // но на всякий случай вернём «нормальные» значения
        const pairs = [
            ["username","username"],
            ["email","email"],
            ["password","password"],
            ["age","age"]
        ];
        pairs.forEach(([id,name])=>{
            const el = $id(id);
            if (!el) return;
            const orig = el.getAttribute("data-orig-name");
            el.setAttribute("name", orig || name);
            el.removeAttribute("data-orig-name");
            el.setAttribute("autocomplete", id==="password" ? "new-password" : "off");
        });
    }

    // ---------- Modals logic
    let userModal, deleteModal, isEditMode=false;

    function openCreateModal(){
        isEditMode=false;
        const form = $id("userForm");

        $id("userModalTitle").textContent="Добавить пользователя";
        $id("userId").value="";
        $id("username").value="";
        $id("email").value="";
        $id("password").value="";
        $id("pwdNote").classList.add("invisible");
        $id("age").value="";
        clearFieldErrors(form);
        drawRolesCheckboxes($id("rolesBox"), []); // НИЧЕГО не отмечаем

        disableAutofillForCreate(form); // <-- ключевой вызов

        userModal.show();
    }

    function openEditModal(user){
        isEditMode=true;
        const form = $id("userForm");

        $id("userModalTitle").textContent="Редактирование пользователя";
        $id("userId").value=user.id;
        $id("username").value=user.username ?? "";
        $id("email").value=user.email ?? "";
        $id("password").value="";
        $id("pwdNote").classList.remove("invisible");
        $id("age").value=user.age ?? "";
        clearFieldErrors(form);
        drawRolesCheckboxes($id("rolesBox"), user.roles || []);

        restoreNamesAfterEdit(form); // на всякий случай

        userModal.show();
    }

    async function submitUserForm(e){
        e.preventDefault();
        hideBanner();
        const form = e.currentTarget;
        clearFieldErrors(form);

        const id = $id("userId").value?.trim();

        const dto = {
            username: $id("username").value?.trim(),
            email: $id("email").value?.trim(),
            roleIds: collectSelectedRoleIds($id("rolesBox"))
        };
        const ageStr = String($id("age").value ?? "").trim();
        if (ageStr !== "") dto.age = Number(ageStr);
        const pwd = $id("password").value;
        if (!isEditMode || pwd) dto.password = pwd;

        try{
            if (isEditMode) {
                await API.updateUser(id, dto);
                showBanner("Пользователь обновлён", "success");
            } else {
                await API.createUser(dto);
                showBanner("Пользователь создан", "success");
            }
            userModal.hide();
            await reloadTable();
        }catch(err){
            const fieldMap = err.errors || err.payload?.fields || {};
            if ((err.status===400 || err.status===422) && Object.keys(fieldMap).length){
                Object.entries(fieldMap).forEach(([f,m])=>setFieldError(form,f,m));
                showBanner(err.message || "Проверьте введённые данные", "danger");
            } else if (err.status===404){
                showBanner("API-метод не найден (404). Проверь базовый путь API.", "danger");
            } else {
                showBanner(err.message || "Неизвестная ошибка", "danger");
            }
        }
    }

    async function submitDeleteForm(e){
        e.preventDefault();
        hideBanner();
        try{
            await API.deleteUser($id("deleteUserId").value);
            deleteModal.hide();
            showBanner("Пользователь удалён", "success");
            await reloadTable();
        }catch(err){
            showBanner(err.message || "Не удалось удалить пользователя", "danger");
        }
    }

    function onTableClick(e){
        const edit = e.target.closest(".btn-edit");
        const del  = e.target.closest(".btn-delete");
        if (edit){
            const tr = edit.closest("tr");
            openEditModal({
                id: edit.dataset.id,
                username: tr.children[1].textContent.trim(),
                email: tr.children[2].textContent.trim(),
                age: Number(tr.children[3].textContent.trim()) || "",
                roles: tr.children[4].textContent.split(",").map(s=>s.trim()).filter(Boolean) // ["USER", ...]
            });
        }
        if (del){
            $id("deleteUserId").value = del.dataset.id;
            $id("deleteUserName").textContent = del.dataset.name || "";
            deleteModal.show();
        }
    }

    // ---------- Init
    document.addEventListener("DOMContentLoaded", async ()=>{
        userModal   = new bootstrap.Modal($id("userModal"));
        deleteModal = new bootstrap.Modal($id("deleteModal"));

        $id("btnOpenCreate").addEventListener("click", openCreateModal);
        $id("userForm").addEventListener("submit", submitUserForm);
        $id("deleteForm").addEventListener("submit", submitDeleteForm);
        $id("usersTbody").addEventListener("click", onTableClick);

        API_BASE = await discoverApiBase();
        if (!API_BASE){
            showBanner("Не смог найти базовый URL REST-API. Укажи его в <meta name=\"api-base\"> или проверь @RequestMapping.", "danger");
            return;
        }

        try{ ALL_ROLES = await API.listRoles(); }
        catch(_){ ALL_ROLES = []; }

        await reloadTable();
    });
})();




