// dashboard.js
document.addEventListener('DOMContentLoaded', () => {
    // ==== Utiles ====
    const getInt = (sel) => parseInt((document.querySelector(sel)?.textContent || '0'), 10) || 0;
    const setText = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v; };
    const $ = (id) => document.getElementById(id);

    // ==== 1) Resumen personal (reusa tus badges del board) ====
    setText('dash-assigned',   getInt('#count-assigned'));
    setText('dash-inprogress', getInt('#count-inprogress'));
    setText('dash-done',       getInt('#count-done'));

    // ==== 2) Gráfico semanal (Chart.js) ====
    const chartCanvas = $('chart-week');
    if (chartCanvas) {
        // [MOD] limitar altura visible del gráfico
        try {
            chartCanvas.style.height = '260px';
            const cardBody = chartCanvas.closest('.card-body');
            if (cardBody) cardBody.style.height = '300px';
        } catch (e) {}

        fetch(`${window.ctx}/api/metrics/weekly`, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(r => r.json())
            .then(data => {
                // Evita re-crear si navegas entre tabs
                if (chartCanvas._chart) { chartCanvas._chart.destroy(); }
                chartCanvas._chart = new Chart(chartCanvas, {
                    type: 'bar',
                    data: {
                        labels: data.labels || [],
                        datasets: [
                            { label: 'Asignadas',   data: data.assigned   || [] },
                            { label: 'En Proceso',  data: data.inProgress || [] },
                            { label: 'Finalizadas', data: data.done       || [] }
                        ]
                    },
                    options: { responsive: true, maintainAspectRatio: false }
                });
            })
            .catch(() => { /* sin ruido si aún no hay datos */ });
    }

    // ==== 3) Auditoría con filtros + paginación ====
    const tbody = document.querySelector('#tbl-audit tbody');
    if (!tbody) return;

    // Estado de paginación (si tu backend devuelve Page<AuditDto>)
    let pageIdx = 0;
    let pageSize = 20;

    const read = (id) => $(id)?.value?.trim() || '';
    const buildQuery = (withPage = true) => {
        const p = new URLSearchParams();
        const actorId   = read('flt-actor');
        const taskId    = read('flt-task');
        const action    = read('flt-action');
        const oldStatus = read('flt-old');
        const newStatus = read('flt-new');
        const days      = read('flt-days') || '7';
        if (actorId)   p.set('actorId', actorId);
        if (taskId)    p.set('taskId', taskId);
        if (action)    p.set('action', action);
        if (oldStatus) p.set('oldStatus', oldStatus);
        if (newStatus) p.set('newStatus', newStatus);
        if (days)      p.set('days', days);

        if (withPage) {
            p.set('page', String(pageIdx));
            p.set('size', String(pageSize));
        }
        return p.toString();
    };

    const paintRows = (rows) => {
        tbody.innerHTML = (rows || []).map(r => `
      <tr>
        <td>${r.when || ''}</td>
        <td>${r.username || ''}</td>
        <td>${r.action || ''}</td>
        <td>${r.taskId ?? ''}</td>
        <td>${r.oldStatus || ''}</td>
        <td>${r.newStatus || ''}</td>
        <td class="text-truncate" style="max-width:420px">${r.details || ''}</td>
      </tr>
    `).join('');
    };

    const setExportHref = () => {
        const a = $('btn-audit-export');
        if (a) a.href = `${window.ctx}/audit/export?${buildQuery(false)}`;
    };

    const setPagerUI = (page) => {
        const prev = $('audit-prev');
        const next = $('audit-next');
        const label = $('audit-page');
        if (!page || page.totalPages == null) {
            // Caso tu backend devuelva List<AuditDto> (sin Page)
            if (prev) prev.disabled = true;
            if (next) next.disabled = true;
            if (label) label.textContent = '';
            return;
        }
        if (prev) prev.disabled = page.number <= 0;
        if (next) next.disabled = page.number >= (page.totalPages - 1);
        if (label) label.textContent = `Página ${page.number + 1} de ${page.totalPages} (total: ${page.totalElements})`;
    };

    const loadAudit = () => {
        const url = `${window.ctx}/api/audit?${buildQuery(true)}`;
        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(r => r.json())
            .then(data => {
                // Soporta Page<AuditDto> o List<AuditDto>
                if (Array.isArray(data)) {
                    paintRows(data);
                    setPagerUI(null);
                } else {
                    paintRows(data.content || []);
                    setPagerUI(data);
                }
                setExportHref();
            });
    };

    // Eventos
    $('btn-audit-apply')?.addEventListener('click', () => {
        pageIdx = 0; // reset paginación al aplicar filtros
        loadAudit();
    });

    $('audit-prev')?.addEventListener('click', () => {
        if (pageIdx > 0) { pageIdx--; loadAudit(); }
    });
    $('audit-next')?.addEventListener('click', () => {
        pageIdx++; loadAudit();
    });

    // Primera carga
    loadAudit();
});
