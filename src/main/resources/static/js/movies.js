const config = window.MovieConfig ?? { genres: [], mpaaRatings: [], persons: [], currentUser: null };
const userInfo = config.currentUser ?? { username: null, admin: false };
const roleSelect = document.getElementById('roleSelect');
const ROLE_STORAGE_KEY = 'demoRole';
function loadSavedRole() {
    try {
        const stored = localStorage.getItem(ROLE_STORAGE_KEY);
        return stored === 'ADMIN' ? 'ADMIN' : 'USER';
    } catch (error) {
        return 'USER';
    }
}
let currentRole = loadSavedRole();
userInfo.admin = currentRole === 'ADMIN';

const dialog = document.getElementById('movieDialog');
const form = document.getElementById('movieForm');
const errorBox = document.getElementById('movieFormErrors');
const cancelButton = document.getElementById('movieDialogCancel');
const createButton = document.getElementById('createMovieBtn');
const submitButton = document.getElementById('movieDialogSubmit');
const pagination = document.querySelector('.pagination');
const table = document.querySelector('.movies-table tbody');
const detailsDialog = document.getElementById('movieDetailsDialog');
const detailsCloseButton = document.getElementById('movieDetailsClose');
const detailsRefs = detailsDialog ? {
    title: document.getElementById('movieDetailsTitle'),
    name: document.getElementById('detailsName'),
    creationDate: document.getElementById('detailsCreationDate'),
    genre: document.getElementById('detailsGenre'),
    mpaaRating: document.getElementById('detailsMpaa'),
    budget: document.getElementById('detailsBudget'),
    totalBoxOffice: document.getElementById('detailsBoxOffice'),
    oscars: document.getElementById('detailsOscars'),
    palms: document.getElementById('detailsPalms'),
    length: document.getElementById('detailsLength'),
    coordX: document.getElementById('detailsCoordX'),
    coordY: document.getElementById('detailsCoordY'),
    persons: document.getElementById('detailsPersons')
} : null;

const fieldRefs = {
    id: document.getElementById('movieId'),
    name: document.getElementById('movieName'),
    coordX: document.getElementById('movieCoordX'),
    coordY: document.getElementById('movieCoordY'),
    oscarsCount: document.getElementById('movieOscarsCount'),
    budget: document.getElementById('movieBudget'),
    totalBoxOffice: document.getElementById('movieTotalBoxOffice'),
    mpaaRating: document.getElementById('movieMpaaRating'),
    directorId: document.getElementById('movieDirectorId'),
    screenwriterId: document.getElementById('movieScreenwriterId'),
    operatorId: document.getElementById('movieOperatorId'),
    length: document.getElementById('movieLength'),
    goldenPalmCount: document.getElementById('movieGoldenPalmCount'),
    genre: document.getElementById('movieGenre')
};

const importForm = document.getElementById('movieImportForm');
const importFileInput = document.getElementById('importFile');
const importErrorsBox = document.getElementById('importFormErrors');
const importSuccessBox = document.getElementById('importFormSuccess');

const historyAdminToggle = document.getElementById('historyAdminMode');
const historyErrorsBox = document.getElementById('historyErrors');
const historyBody = document.getElementById('importHistoryBody');
const historyReloadButton = document.getElementById('historyReloadBtn');
const historyPagination = document.getElementById('historyPagination');
const historyPrevButton = document.getElementById('historyPrevBtn');
const historyNextButton = document.getElementById('historyNextBtn');
const historyPageInfo = document.getElementById('historyPageInfo');

const state = {
    mode: 'create',
    currentId: null
};

const historyState = {
    page: 0,
    size: 5,
    totalPages: 0
};

function applyRoleToUi() {
    if (roleSelect) {
        roleSelect.value = currentRole;
    }
    if (historyAdminToggle) {
        historyAdminToggle.checked = currentRole === 'ADMIN';
    }
}

function setRole(role) {
    currentRole = role === 'ADMIN' ? 'ADMIN' : 'USER';
    try {
        localStorage.setItem(ROLE_STORAGE_KEY, currentRole);
    } catch (error) {
        // ignore storage errors in private mode
    }
    userInfo.admin = currentRole === 'ADMIN';
    applyRoleToUi();
    loadImportHistory();
}

function formatDateTime(value) {
    if (!value) {
        return '—';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    return date.toLocaleString('ru-RU');
}

function formatNumeric(value, fractionDigits = 0) {
    if (value === null || value === undefined) {
        return '—';
    }
    const number = Number(value);
    if (Number.isNaN(number)) {
        return String(value);
    }
    return number.toLocaleString('ru-RU', {
        minimumFractionDigits: fractionDigits,
        maximumFractionDigits: fractionDigits
    });
}

function resetDetails() {
    if (!detailsRefs) {
        return;
    }
    detailsRefs.title.textContent = 'Информация о фильме';
    detailsRefs.name.textContent = '';
    detailsRefs.creationDate.textContent = '';
    detailsRefs.genre.textContent = '';
    detailsRefs.mpaaRating.textContent = '';
    detailsRefs.budget.textContent = '';
    detailsRefs.totalBoxOffice.textContent = '';
    detailsRefs.oscars.textContent = '';
    detailsRefs.palms.textContent = '';
    detailsRefs.length.textContent = '';
    detailsRefs.coordX.textContent = '';
    detailsRefs.coordY.textContent = '';
    detailsRefs.persons.replaceChildren();
}

function appendDetail(dl, term, value) {
    const dt = document.createElement('dt');
    dt.textContent = term;
    const dd = document.createElement('dd');
    dd.textContent = value;
    dl.append(dt, dd);
}

function formatLocation(location) {
    if (!location) {
        return '—';
    }
    const parts = [];
    if (location.name) {
        parts.push(location.name);
    }
    const coordParts = [];
    if (location.x !== null && location.x !== undefined) {
        coordParts.push(`x=${Number(location.x).toLocaleString('ru-RU', { maximumFractionDigits: 2 })}`);
    }
    if (location.y !== null && location.y !== undefined) {
        coordParts.push(`y=${Number(location.y).toLocaleString('ru-RU', { maximumFractionDigits: 2 })}`);
    }
    if (coordParts.length > 0) {
        parts.push(`(${coordParts.join(', ')})`);
    }
    return parts.join(' ');
}

function renderPersonCard(roleLabel, person) {
    const wrapper = document.createElement('div');
    wrapper.className = 'person-card';
    const title = document.createElement('h4');
    title.textContent = roleLabel;
    wrapper.append(title);
    if (!person) {
        const empty = document.createElement('p');
        empty.className = 'person-empty';
        empty.textContent = 'Не указано';
        wrapper.append(empty);
        return wrapper;
    }
    const dl = document.createElement('dl');
    appendDetail(dl, 'ID', person.id ?? '—');
    appendDetail(dl, 'Имя', person.name ?? '—');
    appendDetail(dl, 'Вес', formatNumeric(person.weight, 2));
    appendDetail(dl, 'Гражданство', person.nationality ?? '—');
    appendDetail(dl, 'Цвет глаз', person.eyeColor ?? '—');
    appendDetail(dl, 'Цвет волос', person.hairColor ?? '—');
    appendDetail(dl, 'Локация', formatLocation(person.location));
    wrapper.append(dl);
    return wrapper;
}

function fillDetails(movie) {
    if (!detailsRefs) {
        return;
    }
    detailsRefs.title.textContent = `Информация о фильме #${movie.id}`;
    detailsRefs.name.textContent = movie.name ?? '—';
    detailsRefs.creationDate.textContent = formatDateTime(movie.creationDate);
    detailsRefs.genre.textContent = movie.genre ?? '—';
    detailsRefs.mpaaRating.textContent = movie.mpaaRating ?? '—';
    detailsRefs.budget.textContent = formatNumeric(movie.budget);
    detailsRefs.totalBoxOffice.textContent = formatNumeric(movie.totalBoxOffice, 2);
    detailsRefs.oscars.textContent = formatNumeric(movie.oscarsCount);
    detailsRefs.palms.textContent = formatNumeric(movie.goldenPalmCount);
    detailsRefs.length.textContent = formatNumeric(movie.length);
    detailsRefs.coordX.textContent = formatNumeric(movie.coordX, 2);
    detailsRefs.coordY.textContent = formatNumeric(movie.coordY);

    const persons = detailsRefs.persons;
    persons.replaceChildren();
    const roles = [
        { key: 'director', label: 'Режиссёр' },
        { key: 'screenwriter', label: 'Сценарист' },
        { key: 'operator', label: 'Оператор' }
    ];
    roles.forEach(({ key, label }) => persons.append(renderPersonCard(label, movie[key])));
}

function openDetailsDialog(id) {
    if (!detailsDialog || !detailsRefs) {
        return;
    }
    resetDetails();
    detailsDialog.showModal();
    fetch(`/api/movies/${id}`)
        .then((response) => {
            if (!response.ok) {
                return response.json()
                    .then((body) => Promise.reject(new Error(body.message || 'Не удалось загрузить фильм')));
            }
            return response.json();
        })
        .then((data) => fillDetails(data))
        .catch((error) => {
            showAlert(error.message);
            closeDetailsDialog();
        });
}

function closeDetailsDialog() {
    if (!detailsDialog) {
        return;
    }
    detailsDialog.close();
    resetDetails();
}

function setupPagination() {
    if (!pagination) {
        return;
    }
    pagination.addEventListener('click', (event) => {
        const button = event.target.closest('button[data-page]');
        if (!button || button.disabled) {
            return;
        }
        const page = parseInt(button.dataset.page, 10);
        if (Number.isNaN(page) || page < 0) {
            return;
        }
        const params = new URLSearchParams(window.location.search);
        params.set('page', page);
        window.location.search = params.toString();
    });
}

function populateSelect(select, options) {
    const placeholder = select.querySelector('option[value=""]');
    select.innerHTML = '';
    if (placeholder) {
        select.append(placeholder.cloneNode(true));
    } else {
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = '--';
        select.append(defaultOption);
    }
    options.forEach((item) => {
        const option = document.createElement('option');
        if (typeof item === 'string') {
            option.value = item;
            option.textContent = item;
        } else {
            option.value = item.id;
            option.textContent = item.name;
        }
        select.append(option);
    });
}

function ensureFormOptions() {
    populateSelect(fieldRefs.genre, config.genres ?? []);
    populateSelect(fieldRefs.mpaaRating, config.mpaaRatings ?? []);
    populateSelect(fieldRefs.directorId, config.persons ?? []);
    populateSelect(fieldRefs.screenwriterId, config.persons ?? []);
    populateSelect(fieldRefs.operatorId, config.persons ?? []);
}

function resetForm() {
    if (!form) {
        return;
    }
    form.reset();
    fieldRefs.id.value = '';
    errorBox.hidden = true;
    errorBox.textContent = '';
    state.mode = 'create';
    state.currentId = null;
    if (submitButton) {
        submitButton.disabled = false;
    }
}

function openCreateDialog() {
    state.mode = 'create';
    resetForm();
    ensureFormOptions();
    document.getElementById('movieDialogTitle').textContent = 'Новый фильм';
    if (dialog) {
        dialog.showModal();
    }
    fieldRefs.name.focus();
}

function fillForm(data) {
    fieldRefs.id.value = data.id ?? '';
    fieldRefs.name.value = data.name ?? '';
    fieldRefs.coordX.value = data.coordinates?.x ?? '';
    fieldRefs.coordY.value = data.coordinates?.y ?? '';
    fieldRefs.oscarsCount.value = data.oscarsCount ?? '';
    fieldRefs.budget.value = data.budget ?? '';
    fieldRefs.totalBoxOffice.value = data.totalBoxOffice ?? '';
    fieldRefs.mpaaRating.value = data.mpaaRating ?? '';
    fieldRefs.directorId.value = data.directorId ?? '';
    fieldRefs.screenwriterId.value = data.screenwriterId ?? '';
    fieldRefs.operatorId.value = data.operatorId ?? '';
    fieldRefs.length.value = data.length ?? '';
    fieldRefs.goldenPalmCount.value = data.goldenPalmCount ?? '';
    fieldRefs.genre.value = data.genre ?? '';
}

function openEditDialog(id) {
    if (!dialog) {
        return;
    }
    resetForm();
    state.mode = 'edit';
    state.currentId = id;
    ensureFormOptions();
    document.getElementById('movieDialogTitle').textContent = 'Изменение фильма';
    submitButton.disabled = true;
    dialog.showModal();
    fetch(`/api/movies/${id}/form`)
        .then((response) => {
            if (!response.ok) {
                return response.json()
                    .then((body) => Promise.reject(new Error(body.message || 'Не удалось загрузить фильм')));
            }
            return response.json();
        })
        .then((data) => {
            fillForm(data);
            submitButton.disabled = false;
            fieldRefs.name.focus();
        })
        .catch((error) => {
            showErrors(error.message);
            closeDialog();
        });
}

function getCurrentId() {
    if (state.mode !== 'edit') {
        return null;
    }
    if (state.currentId != null) {
        return state.currentId;
    }
    const parsed = parseInt(fieldRefs.id.value, 10);
    return Number.isNaN(parsed) ? null : parsed;
}

function gatherPayload() {
    const parseNumber = (value, parser = Number) => {
        if (value === '' || value === null || value === undefined) {
            return null;
        }
        const parsed = parser(value);
        return Number.isNaN(parsed) ? null : parsed;
    };

    return {
        id: getCurrentId(),
        name: fieldRefs.name.value.trim(),
        coordinates: {
            x: parseNumber(fieldRefs.coordX.value, parseFloat),
            y: parseNumber(fieldRefs.coordY.value, (v) => parseInt(v, 10))
        },
        oscarsCount: parseNumber(fieldRefs.oscarsCount.value, (v) => parseInt(v, 10)),
        budget: parseNumber(fieldRefs.budget.value, (v) => parseInt(v, 10)),
        totalBoxOffice: parseNumber(fieldRefs.totalBoxOffice.value, parseFloat),
        mpaaRating: fieldRefs.mpaaRating.value || null,
        directorId: parseNumber(fieldRefs.directorId.value, (v) => parseInt(v, 10)),
        screenwriterId: parseNumber(fieldRefs.screenwriterId.value, (v) => parseInt(v, 10)),
        operatorId: parseNumber(fieldRefs.operatorId.value, (v) => parseInt(v, 10)),
        length: parseNumber(fieldRefs.length.value, (v) => parseInt(v, 10)),
        goldenPalmCount: parseNumber(fieldRefs.goldenPalmCount.value, (v) => parseInt(v, 10)),
        genre: fieldRefs.genre.value || null
    };
}

function showErrors(message, details) {
    if (!errorBox) {
        window.alert(message);
        return;
    }
    errorBox.hidden = false;
    if (details && Object.keys(details).length > 0) {
        const list = document.createElement('ul');
        Object.entries(details).forEach(([field, text]) => {
            const li = document.createElement('li');
            li.textContent = `${field}: ${text}`;
            list.append(li);
        });
        errorBox.replaceChildren(document.createTextNode(message), list);
    } else {
        errorBox.textContent = message;
    }
}

function showAlert(message) {
    window.alert(message);
}

function toggleSubmitAvailability() {
    if (!submitButton) {
        return;
    }
    if (!Array.isArray(config.persons) || config.persons.length === 0) {
        showErrors('Нет доступных персон. Сначала создайте хотя бы одну персону.');
        submitButton.disabled = true;
    } else {
        errorBox.hidden = true;
        errorBox.textContent = '';
        submitButton.disabled = false;
    }
}

function buildStompFrame(command, headers = {}, body = '') {
    let frame = `${command}\n`;
    Object.entries(headers).forEach(([key, value]) => {
        frame += `${key}:${value}\n`;
    });
    frame += '\n';
    frame += body || '';
    return `${frame}\0`;
}

function parseStompFrames(raw) {
    const frames = [];
    const chunks = raw.split('\0');
    for (const chunk of chunks) {
        if (!chunk.trim()) {
            continue;
        }
        const [headerPart, bodyPart = ''] = chunk.split(/\n\n/);
        const lines = headerPart.split('\n');
        const command = lines.shift()?.trim() ?? '';
        const headers = {};
        lines.forEach((line) => {
            const idx = line.indexOf(':');
            if (idx > -1) {
                const key = line.substring(0, idx).trim();
                const value = line.substring(idx + 1).trim();
                headers[key] = value;
            }
        });
        frames.push({ command, headers, body: bodyPart });
    }
    return frames;
}

let fallbackTimer = null;

function setupFallbackAutoRefresh() {
    if (fallbackTimer !== null) {
        return;
    }
    fallbackTimer = window.setInterval(() => {
        fetch(window.location.pathname + window.location.search, {
            headers: { 'X-Requested-With': 'fetch' }
        })
            .then(() => window.location.reload())
            .catch(() => {
                // ignore, retry on next tick
            });
    }, 20000);
}

function setupWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${protocol}://${window.location.host}/ws`;

    try {
        const ws = new WebSocket(url);
        let connected = false;

        ws.addEventListener('open', () => {
            ws.send(buildStompFrame('CONNECT', {
                'accept-version': '1.2',
                host: '/'
            }));
        });

        ws.addEventListener('message', (event) => {
            parseStompFrames(event.data).forEach((frame) => {
                if (frame.command === 'CONNECTED' && !connected) {
                    connected = true;
                    ws.send(buildStompFrame('SUBSCRIBE', {
                        id: 'movies-updates',
                        destination: '/topic/movies',
                        ack: 'auto'
                    }));
                }
                if (frame.command === 'MESSAGE') {
                    window.location.reload();
                }
            });
        });

        ws.addEventListener('error', () => setupFallbackAutoRefresh());
        ws.addEventListener('close', () => setupFallbackAutoRefresh());
    } catch (error) {
        setupFallbackAutoRefresh();
    }
}

function clearImportMessages() {
    if (importErrorsBox) {
        importErrorsBox.hidden = true;
        importErrorsBox.textContent = '';
    }
    if (importSuccessBox) {
        importSuccessBox.hidden = true;
        importSuccessBox.textContent = '';
    }
}

function showImportError(message) {
    if (!importErrorsBox) {
        showAlert(message);
        return;
    }
    importErrorsBox.hidden = false;
    importErrorsBox.textContent = message;
    if (importSuccessBox) {
        importSuccessBox.hidden = true;
        importSuccessBox.textContent = '';
    }
}

function showImportSuccess(message) {
    if (!importSuccessBox) {
        showAlert(message);
        return;
    }
    importSuccessBox.hidden = false;
    importSuccessBox.textContent = message;
    if (importErrorsBox) {
        importErrorsBox.hidden = true;
        importErrorsBox.textContent = '';
    }
}

function clearHistoryError() {
    if (historyErrorsBox) {
        historyErrorsBox.hidden = true;
        historyErrorsBox.textContent = '';
    }
}

function showHistoryError(message) {
    if (!historyErrorsBox) {
        showAlert(message);
        return;
    }
    historyErrorsBox.hidden = false;
    historyErrorsBox.textContent = message;
}

function renderHistoryPage(result) {
    if (!historyBody) {
        return;
    }
    const rows = result?.content ?? [];
    historyState.page = result?.page ?? 0;
    historyState.size = result?.size ?? historyState.size;
    historyState.totalPages = result?.totalPages ?? 0;

    historyBody.replaceChildren();
    if (!rows.length) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 8;
        td.textContent = 'Нет операций для отображения';
        tr.append(td);
        historyBody.append(tr);
        renderHistoryPagination();
        return;
    }
    rows.forEach((op) => {
        const tr = document.createElement('tr');
        const cells = [
            op.id ?? '—',
            op.username ?? '—',
            op.status ?? '—',
            formatDateTime(op.createdAt),
            formatDateTime(op.completedAt),
            op.addedCount ?? '—',
            op.errorMessage ?? '—'
        ];
        cells.forEach((value) => {
            const td = document.createElement('td');
            td.textContent = value;
            tr.append(td);
        });
        const fileCell = document.createElement('td');
        if (op.fileAvailable) {
            const link = document.createElement('a');
            link.href = `/api/movies/import/${op.id}/file`;
            link.textContent = op.originalFilename || 'download';
            link.download = '';
            fileCell.append(link);
        } else {
            fileCell.textContent = '-';
        }
        tr.append(fileCell);
        historyBody.append(tr);
    });
    renderHistoryPagination();
}

function renderHistoryPagination() {
    if (!historyPagination || !historyPageInfo) {
        return;
    }
    const totalPages = historyState.totalPages;
    if (!totalPages || totalPages <= 1) {
        historyPagination.hidden = true;
        return;
    }
    historyPagination.hidden = false;
    historyPageInfo.textContent = (historyState.page + 1) + ' / ' + totalPages;
    if (historyPrevButton) {
        historyPrevButton.disabled = historyState.page <= 0;
    }
    if (historyNextButton) {
        historyNextButton.disabled = historyState.page >= totalPages - 1;
    }
}

async function loadImportHistory() {
    if (!historyBody) {
        return;
    }
    const adminMode = currentRole === 'ADMIN';
    clearHistoryError();
    try {
        const params = new URLSearchParams({
            all: adminMode,
            page: historyState.page,
            size: historyState.size
        });
        const response = await fetch('/api/movies/import/history?' + params.toString());
        if (!response.ok) {
            const body = await response.json().catch(() => ({}));
            throw new Error(body.message || 'Не удалось загрузить историю импорта');
        }
        const data = await response.json();
        renderHistoryPage(data);
    } catch (error) {
        showHistoryError(error.message);
    }
}
async function submitImportFile(event) {
    event.preventDefault();
    if (!importForm) {
        return;
    }
    clearImportMessages();
    if (!importFileInput?.files?.length) {
        showImportError('Выберите YAML-файл для импорта');
        return;
    }
    const formData = new FormData();
    formData.append('file', importFileInput.files[0]);
    try {
        const response = await fetch('/api/movies/import', {
            method: 'POST',
            body: formData
        });
        const body = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(body.message || 'Не удалось выполнить импорт');
        }
        const added = body.addedCount ?? 0;
        const statusText = body.status === 'SUCCESS'
            ? `Импорт #${body.id} завершён. Добавлено фильмов: ${added}`
            : `Операция #${body.id} завершилась со статусом ${body.status}`;
        showImportSuccess(statusText);
        importForm.reset();
        await loadImportHistory();
    } catch (error) {
        showImportError(error.message);
    }
}

async function submitForm(event) {
    event.preventDefault();
    const payload = gatherPayload();
    const currentId = getCurrentId();
    if (state.mode === 'edit' && (currentId === null || Number.isNaN(currentId))) {
        showErrors('Не удалось определить фильм для редактирования');
        return;
    }
    const url = state.mode === 'edit' ? `/api/movies/${currentId}` : '/api/movies';
    const method = state.mode === 'edit' ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const body = await response.json().catch(() => ({}));
            const message = body.message || 'Не удалось сохранить фильм';
            showErrors(message, body.details);
            return;
        }

        closeDialog();
        window.location.reload();
    } catch (error) {
        showErrors(error.message);
    }
}

function closeDialog() {
    if (dialog) {
        dialog.close();
    }
    resetForm();
}

function handleTableClick(event) {
    const detailsButton = event.target.closest('.detailsMovieBtn');
    if (detailsButton) {
        const movieId = Number(detailsButton.dataset.id);
        if (!Number.isNaN(movieId)) {
            openDetailsDialog(movieId);
        }
        return;
    }
    const editButton = event.target.closest('.editMovieBtn');
    if (editButton) {
        const movieId = Number(editButton.dataset.id);
        if (!Number.isNaN(movieId)) {
            openEditDialog(movieId);
        }
        return;
    }
    const deleteButton = event.target.closest('.deleteMovieBtn');
    if (deleteButton) {
        const movieId = Number(deleteButton.dataset.id);
        if (Number.isNaN(movieId)) {
            return;
        }
        if (window.confirm('Удалить фильм?')) {
            fetch(`/api/movies/${movieId}`, { method: 'DELETE' })
                .then((response) => {
                    if (!response.ok) {
                        return response.json()
                            .then((body) => Promise.reject(body.message || 'Не удалось удалить фильм'));
                    }
                    window.location.reload();
                    return null;
                })
                .catch((error) => showAlert(error));
        }
    }
}

function init() {
    applyRoleToUi();
    if (createButton) {
        createButton.addEventListener('click', () => openCreateDialog());
    }
    if (cancelButton) {
        cancelButton.addEventListener('click', () => closeDialog());
    }
    if (form) {
        form.addEventListener('submit', submitForm);
    }
    if (table) {
        table.addEventListener('click', handleTableClick);
    }
    if (detailsCloseButton) {
        detailsCloseButton.addEventListener('click', () => closeDetailsDialog());
    }
    if (detailsDialog) {
        detailsDialog.addEventListener('cancel', (event) => {
            event.preventDefault();
            closeDetailsDialog();
        });
        detailsDialog.addEventListener('close', () => resetDetails());
        resetDetails();
    }
    if (importForm) {
        importForm.addEventListener('submit', submitImportFile);
    }
    if (roleSelect) {
        roleSelect.addEventListener('change', (event) => setRole(event.target.value));
    }
    if (historyReloadButton) {
        historyReloadButton.addEventListener('click', () => {
            historyState.page = 0;
            loadImportHistory();
        });
    }
    if (historyAdminToggle) {
        historyAdminToggle.addEventListener('change', () => {
            const role = historyAdminToggle.checked ? 'ADMIN' : 'USER';
            setRole(role);
            historyState.page = 0;
        });
    }
    if (historyPrevButton) {
        historyPrevButton.addEventListener('click', () => {
            if (historyState.page > 0) {
                historyState.page -= 1;
                loadImportHistory();
            }
        });
    }
    if (historyNextButton) {
        historyNextButton.addEventListener('click', () => {
            if (historyState.page < historyState.totalPages - 1) {
                historyState.page += 1;
                loadImportHistory();
            }
        });
    }
    if (historyBody) {
        loadImportHistory();
    }
    ensureFormOptions();
    toggleSubmitAvailability();
    setupPagination();
    setupWebSocket();
}

document.addEventListener('DOMContentLoaded', init);
