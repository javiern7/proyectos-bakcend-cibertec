document.addEventListener('DOMContentLoaded', () => {
    window.ctx = window.ctx || '';
    window.isAdmin = !!window.isAdmin;

    function reinitTooltips(root) {
        if (!window.bootstrap) return;
        (root || document).querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
            if (el._tt) {
                try {
                    el._tt.dispose();
                } catch (e) {
                }
            }
            el._tt = new bootstrap.Tooltip(el);
        });
    }

    function normalizeCard(card) {
        const id = card.dataset.id;
        const status = card.dataset.status;

        // 1) quita duplicados de contenedores de acciones
        const allActions = card.querySelectorAll('.task-actions');
        if (allActions.length > 1) {
            allActions.forEach((n, i) => {
                if (i > 0) n.remove();
            });
        }

        // 2) recalcula permisos y arrastre
        const canAdvance = window.isAdmin || (card.dataset.owner === 'true' && status !== 'DONE');
        card.draggable = !!canAdvance;
        if (window.isAdmin) card.draggable = true; // admin siempre puede arrastrar

        // 3) re-render de acciones con tu misma función
        const actions = card.querySelector('.task-actions');
        if (actions) {
            actions.innerHTML = renderActionsHtml(id, status, canAdvance, window.isAdmin);
        }

        // 4) tooltips
        reinitTooltips(card);
    }

    function normalizeAllCards() {
        document.querySelectorAll('.task-card').forEach(normalizeCard);
    }

    function nextStatusOf(status) {
        if (status === 'ASSIGNED') return 'IN_PROGRESS';
        if (status === 'IN_PROGRESS') return 'DONE';
        return null;
    }

    function renderActionsHtml(id, status, canAdvance, isAdmin) {
        // --- ADMIN: esta habilitado ---
        if (isAdmin) {
            const next = nextStatusOf(status);
            const nextForm = next ? `
          <form action="${window.ctx}/task/${id}/status" method="post" class="d-inline status-form" onsubmit="return false">
            <input type="hidden" name="status" value="${next}"/>
            <button type="button" class="btn btn-sm btn-outline-primary me-2">
              ${next === 'IN_PROGRESS' ? '→ En Proceso' : '→ En Terminado'}
            </button>
          </form>` : '';
            return `
          <a href="${window.ctx}/task/edit/${id}" class="btn btn-sm btn-outline-secondary me-2">Editar</a>
          ${nextForm}
          <form action="${window.ctx}/task/${id}/delete" method="post" class="d-inline"
                onsubmit="return confirm('¿Eliminar la tarea?');">
            <button class="btn btn-sm btn-outline-danger">Eliminar</button>
          </form>`;
        }

        // --- USER ---
        // Regla: NO mostrar "Eliminar" nunca para USER.
        // - Si no puede avanzar (no es dueño o la tarea está DONE), mostrar Editar/Cambiar deshabilitados.
        if (status === 'DONE' || !canAdvance) {
            return `
          <button class="btn btn-sm btn-outline-secondary me-2" disabled data-bs-toggle="tooltip"
                  title="${status === 'DONE' ? 'Tarea finalizada' : 'Solo el dueño puede editar'}">Editar</button>
          <button class="btn btn-sm btn-outline-primary me-2" disabled data-bs-toggle="tooltip"
                  title="${status === 'DONE' ? 'Tarea finalizada' : 'No puedes cambiar estado'}">Cambiar estado</button>`;
        }

        // Dueño y NO DONE: Editar y Cambiar estado habilitados (sin Eliminar)
        const next = nextStatusOf(status); // ASSIGNED -> IN_PROGRESS, IN_PROGRESS -> DONE
        return `
        <a href="${window.ctx}/task/edit/${id}" class="btn btn-sm btn-outline-secondary me-2">Editar</a>
        <form action="${window.ctx}/task/${id}/status" method="post" class="d-inline status-form" onsubmit="return false">
          <input type="hidden" name="status" value="${next}"/>
          <button type="button" class="btn btn-sm btn-outline-primary me-2">
            ${next === 'IN_PROGRESS' ? '→ En Proceso' : '→ En Terminado'}
          </button>
        </form>`;
    }

    function updateCardUIForStatus(card, newStatus) {
        card.classList.remove('ASSIGNED', 'IN_PROGRESS', 'DONE');
        card.classList.add(newStatus);
        card.dataset.status = newStatus;

        // quién puede avanzar ahora
        const canAdvance = window.isAdmin || (card.dataset.owner === 'true' && newStatus !== 'DONE');
        card.draggable = !!(canAdvance); // dueño puede arrastrar mientras no esté DONE; admin siempre (porque data-owner no limita si isAdmin es true)
        if (window.isAdmin) card.draggable = true; // admin: arrastrar siempre; si quieres limitar, ajusta aquí

        // re-render de acciones
        const id = card.dataset.id;
        const actions = card.querySelector('.task-actions');
        if (actions) {
            actions.innerHTML = renderActionsHtml(id, newStatus, canAdvance, window.isAdmin);
            reinitTooltips(card);
        }
    }

    function refreshCounters() {
        const count = s => (document.querySelector(`.dropzone[data-status="${s}"]`) || document).querySelectorAll('.task-card').length;
        const set = (sel, n) => {
            const el = document.querySelector(sel);
            if (el) el.textContent = n;
        };
        set('#count-assigned', count('ASSIGNED'));
        set('#count-inprogress', count('IN_PROGRESS'));
        set('#count-done', count('DONE'));
    }

    // ==== Drag & Drop global ====
    let dragged = null;

    document.addEventListener('dragstart', (e) => {
        const card = e.target.closest('.task-card');
        if (!card) return;
        dragged = card;
        card.setAttribute('dragging', 'true');
        e.dataTransfer.setData('text/plain', card.dataset.id);
        e.dataTransfer.effectAllowed = 'move';
    }, true);

    document.addEventListener('dragend', () => {
        if (dragged) dragged.removeAttribute('dragging');
        dragged = null;
    }, true);

    document.addEventListener('dragenter', (e) => {
        const zone = e.target.closest('.dropzone');
        if (!zone) return;
        e.preventDefault();
        zone.classList.add('drag-over');
    }, true);

    document.addEventListener('dragover', (e) => {
        const zone = e.target.closest('.dropzone');
        if (!zone) return;
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
    }, true);

    document.addEventListener('dragleave', (e) => {
        const zone = e.target.closest('.dropzone');
        if (!zone) return;
        zone.classList.remove('drag-over');
    }, true);

    document.addEventListener('drop', async (e) => {
        const zone = e.target.closest('.dropzone');
        if (!zone) return;
        e.preventDefault();
        zone.classList.remove('drag-over');
        if (!dragged) return;

        const card = dragged;
        const prevParent = card.parentNode;
        const prevStatus = card.dataset.status;
        const newStatus = zone.dataset.status;

        // Permisos: admin puede ser, user solo dueño y hacia delante
        const owner = (card.dataset.owner === 'true');
        const forwardOnly = nextStatusOf(prevStatus);
        const canByUser = owner && forwardOnly === newStatus;
        const allowed = window.isAdmin || canByUser;

        if (!allowed) {
            // rebota sin tocar backend
            return;
        }

        // Mover en UI + actualizar botones/contadores
        zone.appendChild(card);
        updateCardUIForStatus(card, newStatus);
        refreshCounters();

        try {
            const resp = await fetch(`${window.ctx}/task/${card.dataset.id}/status`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: `status=${encodeURIComponent(newStatus)}`
            });
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
        } catch (err) {
            // revertir
            prevParent.appendChild(card);
            updateCardUIForStatus(card, prevStatus);
            refreshCounters();
            alert('No se pudo actualizar el estado.');
        }
    }, true);


    // ==== Helper para postear cambio de estado (botón o DnD) ====
    async function postStatus(id, newStatus, card) {
        const resp = await fetch(`${window.ctx}/task/${id}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: `status=${encodeURIComponent(newStatus)}`
        });
        if (!resp.ok) {
            const msg = await resp.text().catch(() => '');
            throw new Error(msg || ('HTTP ' + resp.status));
        }
        if (card) {
            const zone = document.querySelector(`.dropzone[data-status="${newStatus}"]`);
            if (zone && card.parentNode !== zone) {
                zone.appendChild(card);
            }
            updateCardUIForStatus(card, newStatus);
            refreshCounters();
            if (window.refreshDashboard) {await window.refreshDashboard();}
        }
    }

    // ==== Click en botón de estado (evita navegación y usa AJAX) ====
    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('.status-form button');
        if (!btn) return;
        e.preventDefault();
        const form = btn.closest('.status-form');
        const card = btn.closest('.task-card');
        if (!card || !form) return;
        const id = card.dataset.id;
        const newStatus = form.querySelector('input[name="status"]')?.value;
        if (!newStatus) return;

        try {
            await postStatus(id, newStatus, card);
        } catch (err) {
            alert(err.message || 'No se pudo actualizar el estado.');
        }
    }, true);

    // ==== Guard submit por si algún navegador intenta enviar el form ====
    document.addEventListener('submit', (e) => {
        if (e.target.closest('.status-form')) {
            e.preventDefault();
            return false;
        }
    }, true);


// init
    reinitTooltips(document);
    refreshCounters();
    normalizeAllCards();
});
