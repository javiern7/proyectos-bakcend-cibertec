// ===============================
// dashboard.js (full + fixes)
// ===============================
(() => {
    'use strict';

    // ====== Config ======
    const CTX = (typeof window !== 'undefined' && window.ctx) ? window.ctx.replace(/\/$/, '') : '';
    const API_BASE = `${CTX}/dashboard/api`; // ajusta si tu controller usa otro prefijo
    const IS_ADMIN = !!(window && window.isAdmin);
    const MY_USER_ID = (window && window.userId != null) ? Number(window.userId) : null;

    // ====== Helpers DOM ======
    const $ = (id) => document.getElementById(id);
    const setText = (id, v) => { const el = $(id); if (el) el.textContent = v; };
    const getInt = (sel) => parseInt((document.querySelector(sel)?.textContent || '0'), 10) || 0;
    const toISODate = (d) => d.toISOString().slice(0, 10);

    // Normaliza fecha: dd/MM/yyyy -> yyyy-MM-dd, o recorta si viene con hora
    function normDate(val) {
        if (!val) return null;
        if (/^\d{4}-\d{2}-\d{2}$/.test(val)) return val;          // yyyy-MM-dd
        const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(val);        // dd/MM/yyyy
        if (m) return `${m[3]}-${m[2]}-${m[1]}`;
        return val.slice(0, 10);
    }

    // Mapea variantes de estado a ASSIGNED / IN_PROGRESS / DONE
    function normStatus(s) {
        const x = (s || '').toString().trim().toUpperCase();
        if (x === 'ASSIGNED' || x.startsWith('ASIG')) return 'ASSIGNED';
        if (x === 'IN_PROGRESS' || x.includes('PROCES')) return 'IN_PROGRESS';
        if (x === 'DONE' || x.startsWith('FIN')) return 'DONE';
        return null;
    }

    // ====== Elementos Dashboard ======
    const selUser   = $('chart-user');     // <select> visible solo a admin
    const inputFrom = $('chart-from');     // <input type="date">
    const inputTo   = $('chart-to');       // <input type="date">
    const btnApply  = $('chart-apply');    // botón "Aplicar"
    const pieCanvas = $('chart-status');   // <canvas> donut
    const barCanvas = $('chart-week');     // <canvas> barras

    // ====== Resumen inicial (opcional, desde badges del tablero) ======
    setText('dash-assigned',   getInt('#count-assigned'));
    setText('dash-inprogress', getInt('#count-inprogress'));
    setText('dash-done',       getInt('#count-done'));

    // Ajustes de altura compacta (opcional)
    try {
        if (barCanvas) {
            barCanvas.style.height = '260px';
            barCanvas.closest('.card-body')?.style.setProperty('height', '300px');
        }
        if (pieCanvas) {
            pieCanvas.style.height = '230px';
            pieCanvas.closest('.card-body')?.style.setProperty('height', '260px');
        }
    } catch (_) {}

    // ====== Estado charts ======
    let pieChart = null;
    let barChart = null;
    let lastSnapshot = { assigned: 0, inProgress: 0, done: 0 };

    // ====== Fechas por defecto (últimos 7 días) ======
    function ensureDefaultDateRange() {
        if (!inputFrom || !inputTo) return;
        if (!inputFrom.value || !inputTo.value) {
            const today = new Date();
            const start = new Date(); start.setDate(today.getDate() - 6);
            inputFrom.value = toISODate(start);
            inputTo.value   = toISODate(today);
        }
    }

    // ====== Usuario actual (combo para admin) ======
    function currentUserId() {
        if (IS_ADMIN && selUser) {
            return selUser.value ? Number(selUser.value) : null; // null => "Todos"
        }
        return MY_USER_ID;
    }

    async function loadUsersIfAdmin() {
        if (!IS_ADMIN || !selUser) return;
        try {
            const res = await fetch(`${API_BASE}/users`, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
            if (!res.ok) { selUser.style.display = 'none'; return; }
            const list = await res.json(); // [{id, username}]
            selUser.innerHTML =
                `<option value="">Todos</option>` +
                list.map(u => `<option value="${u.id}" ${u.id === MY_USER_ID ? 'selected' : ''}>${u.username}</option>`).join('');
            selUser.style.display = '';
            selUser.addEventListener('change', () => refresh());
        } catch {
            selUser.style.display = 'none';
        }
    }

    // ====== QueryString para dashboard ======
    function buildDashParams(includeDates = true) {
        const p = new URLSearchParams();
        const uid = currentUserId();
        if (uid != null && !Number.isNaN(uid)) p.set('userId', String(uid));
        if (includeDates && inputFrom?.value) p.set('from', normDate(inputFrom.value));
        if (includeDates && inputTo?.value)   p.set('to',   normDate(inputTo.value));
        return p.toString();
    }

    // ====== Render charts ======
    function renderPie(a, b, c) {
        if (!pieCanvas || typeof Chart === 'undefined') return;
        if (pieChart) { pieChart.destroy(); pieChart = null; }
        pieChart = new Chart(pieCanvas.getContext('2d'), {
            type: 'doughnut',
            data: { labels: ['Asignadas', 'En Proceso', 'Finalizadas'], datasets: [{ data: [a||0, b||0, c||0] }] },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
        });
    }

    function renderBar(labels, seriesA, seriesB, seriesC) {
        if (!barCanvas || typeof Chart === 'undefined') return;

        const maxVal = Math.max(
            ...(seriesA || [0]),
            ...(seriesB || [0]),
            ...(seriesC || [0]),
            0
        );
        const suggestedMax = Math.max(3, maxVal + 1); // mínimo 3, sino se ve 0→1

        if (barChart) { barChart.destroy(); barChart = null; }
        barChart = new Chart(barCanvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels,
                datasets: [
                    { label: 'Asignadas',  data: seriesA || [], borderWidth: 1, barPercentage: 0.8, categoryPercentage: 0.7 },
                    { label: 'En Proceso', data: seriesB || [], borderWidth: 1, barPercentage: 0.8, categoryPercentage: 0.7 },
                    { label: 'Finalizadas',data: seriesC || [], borderWidth: 1, barPercentage: 0.8, categoryPercentage: 0.7 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                scales: {
                    y: {
                        beginAtZero: true,
                        suggestedMax,
                        ticks: {
                            stepSize: 1,
                            precision: 0,
                            callback: (v) => Number.isInteger(v) ? v : ''
                        }
                    }
                }
            }
        });
    }


    // ====== Cargas de datos Dashboard ======
    async function loadSnapshot() {
        try {
            const qs = buildDashParams(false); // snapshot no necesita fechas
            const url = `${API_BASE}/snapshot${qs ? `?${qs}` : ''}`;
            const r = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
            if (!r.ok) throw new Error('snapshot not ok');
            const snap = await r.json();

            // Resumen
            setText('dash-assigned',   snap.assigned ?? 0);
            setText('dash-inprogress', snap.inProgress ?? 0);
            setText('dash-done',       snap.done ?? 0);

            // Donut
            renderPie(snap.assigned, snap.inProgress, snap.done);

            // Guarda para fallback de barras
            lastSnapshot = {
                assigned:   Number(snap.assigned   ?? 0),
                inProgress: Number(snap.inProgress ?? 0),
                done:       Number(snap.done       ?? 0),
            };
        } catch (e) {
            // fallback seguro
            setText('dash-assigned',   0);
            setText('dash-inprogress', 0);
            setText('dash-done',       0);

            // asegura que lastSnapshot exista coherente
            lastSnapshot = { assigned: 0, inProgress: 0, done: 0 };
            renderPie(0, 0, 0);
        }
    }

    async function loadWeekly() {
        try {
            const qs = buildDashParams(true);
            const url = `${API_BASE}/weekly?${qs}`;
            const r = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
            if (!r.ok) throw new Error('weekly not ok');
            const points = await r.json(); // [{day, status, count}]
            console.debug('WEEKLY points =>', points);

            // Labels de días con UTC para evitar saltos horarios
            const labels = [];
            const fromIso = normDate(inputFrom?.value);
            const toIso   = normDate(inputTo?.value);
            if (fromIso && toIso) {
                const start = new Date(fromIso + 'T00:00:00Z');
                const end   = new Date(toIso   + 'T00:00:00Z');
                for (let d = new Date(start); d <= end; d.setUTCDate(d.getUTCDate() + 1)) {
                    labels.push(d.toISOString().slice(0, 10)); // yyyy-MM-dd
                }
            }

            // Series por estado
            const series = {
                'ASSIGNED':    Array(labels.length).fill(0),
                'IN_PROGRESS': Array(labels.length).fill(0),
                'DONE':        Array(labels.length).fill(0)
            };

            points.forEach(p => {
                const day = (p.day || '').toString().slice(0, 10);
                const st  = normStatus(p.status);
                const idx = labels.indexOf(day);
                if (st && idx >= 0) series[st][idx] = Number(p.count) || 0;
            });

            // ¿Hay algún valor > 0?
            const hasData = [
                ...series.ASSIGNED,
                ...series.IN_PROGRESS,
                ...series.DONE
            ].some(v => v > 0);

            if (!hasData) {
                // Fallback: mostramos el "stock" actual en el último día del rango
                const fallbackLabels = labels.length ? labels : [new Date().toISOString().slice(0,10)];
                const lastIdx = fallbackLabels.length - 1;

                const a = Array(fallbackLabels.length).fill(0);
                const b = Array(fallbackLabels.length).fill(0);
                const c = Array(fallbackLabels.length).fill(0);

                a[lastIdx] = lastSnapshot.assigned;
                b[lastIdx] = lastSnapshot.inProgress;
                c[lastIdx] = lastSnapshot.done;

                renderBar(fallbackLabels, a, b, c);
                return; // ⬅️ salimos, ya pintamos el fallback
            }

            renderBar(labels, series.ASSIGNED, series.IN_PROGRESS, series.DONE);
        } catch {
            renderBar([], [], [], []);
        }
    }

    async function refresh() {
        await Promise.all([ loadSnapshot(), loadWeekly() ]);
    }

    function wireQuickRanges() {
        document.querySelectorAll('[data-range]').forEach(btn => {
            btn.addEventListener('click', () => {
                const n = parseInt(btn.dataset.range, 10) || 7;
                const end = new Date();
                const start = new Date(); start.setDate(end.getDate() - (n - 1));
                if (inputFrom) inputFrom.value = toISODate(start);
                if (inputTo)   inputTo.value   = toISODate(end);
                refresh();
            });
        });
    }

    function initDashboard() {
        ensureDefaultDateRange();
        wireQuickRanges();
        btnApply?.addEventListener('click', refresh);
        loadUsersIfAdmin().then(refresh);
    }

    // =========================
    // ==== AUDITORÍA (UI) ====
    // =========================
    const auditTbody = document.querySelector('#tbl-audit tbody');
    let pageIdx = 0;
    let pageSize = 20;

    const readVal = (id) => $(id)?.value?.trim() || '';

    const buildAuditQuery = (withPage = true) => {
        const p = new URLSearchParams();
        const actorId   = readVal('flt-actor');
        const taskId    = readVal('flt-task');
        const action    = readVal('flt-action');
        const oldStatus = readVal('flt-old');
        const newStatus = readVal('flt-new');
        const days      = readVal('flt-days') || '7';

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

    const paintAuditRows = (rows) => {
        if (!auditTbody) return;
        auditTbody.innerHTML = (rows || []).map(r => `
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

    const setAuditExportHref = () => {
        const a = $('btn-audit-export');
        if (a) a.href = `${CTX}/audit/export?${buildAuditQuery(false)}`;
    };

    const setAuditPagerUI = (page) => {
        const prev  = $('audit-prev');
        const next  = $('audit-next');
        const label = $('audit-page');

        if (!page || page.totalPages == null) {
            if (prev)  prev.disabled  = true;
            if (next)  next.disabled  = true;
            if (label) label.textContent = '';
            return;
        }
        if (prev)  prev.disabled  = page.number <= 0;
        if (next)  next.disabled  = page.number >= (page.totalPages - 1);
        if (label) label.textContent = `Página ${page.number + 1} de ${page.totalPages} (total: ${page.totalElements})`;
    };

    const loadAudit = () => {
        if (!auditTbody) return;
        const url = `${CTX}/api/audit?${buildAuditQuery(true)}`;
        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(r => r.json())
            .then(data => {
                if (Array.isArray(data)) {
                    paintAuditRows(data);
                    setAuditPagerUI(null);
                } else {
                    paintAuditRows(data.content || []);
                    setAuditPagerUI(data);
                }
                setAuditExportHref();
            })
            .catch(() => {
                paintAuditRows([]);
                setAuditPagerUI(null);
            });
    };

    function wireAuditEvents() {
        $('btn-audit-apply')?.addEventListener('click', () => {
            pageIdx = 0; // reset al aplicar filtros
            loadAudit();
        });

        $('audit-prev')?.addEventListener('click', () => {
            if (pageIdx > 0) { pageIdx--; loadAudit(); }
        });

        $('audit-next')?.addEventListener('click', () => {
            pageIdx++; loadAudit();
        });
    }

    function initAudit() {
        if (!auditTbody) return;
        wireAuditEvents();
        loadAudit(); // primera carga
    }

    // ====== Init General ======
    function init() {
        initDashboard();
        initAudit();
    }
    window.refreshDashboard = refresh;

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }


})();
