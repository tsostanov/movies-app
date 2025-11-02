const genreCountForm = document.getElementById('genreCountForm');
const genreCountResult = document.getElementById('genreCountResult');
const genreListForm = document.getElementById('genreListForm');
const genreListResult = document.getElementById('genreListResult');
const nameSearchForm = document.getElementById('nameSearchForm');
const nameSearchResult = document.getElementById('nameSearchResult');
const noOscarsBtn = document.getElementById('noOscarsBtn');
const noOscarsResult = document.getElementById('noOscarsResult');
const screenwritersBtn = document.getElementById('screenwritersBtn');
const screenwritersResult = document.getElementById('screenwritersResult');

const API_BASE = '/api/analytics';

function clearNode(node) {
    node.replaceChildren();
}

function renderError(container, message) {
    clearNode(container);
    const div = document.createElement('div');
    div.className = 'analytics-error';
    div.textContent = message;
    container.append(div);
}

function renderMessage(container, message) {
    clearNode(container);
    const div = document.createElement('div');
    div.className = 'analytics-message';
    div.textContent = message;
    container.append(div);
}

function renderMovies(container, movies) {
    clearNode(container);
    if (!Array.isArray(movies) || movies.length === 0) {
        renderMessage(container, 'Ничего не найдено.');
        return;
    }

    const table = document.createElement('table');
    const thead = document.createElement('thead');
    const headRow = document.createElement('tr');
    [
        'ID', 'Название', 'X', 'Y', 'Дата создания', 'Оскары', 'Бюджет',
        'Сборы', 'Жанр', 'MPAA', 'Режиссёр', 'Сценарист', 'Оператор',
        'Длина', 'Золотые пальмы'
    ].forEach((title) => {
        const th = document.createElement('th');
        th.textContent = title;
        headRow.append(th);
    });
    thead.append(headRow);
    table.append(thead);

    const tbody = document.createElement('tbody');
    movies.forEach((movie) => {
        const row = document.createElement('tr');
        const cells = [
            movie.id,
            movie.name,
            movie.coordX,
            movie.coordY,
            movie.creationDate ? formatDate(movie.creationDate) : '',
            movie.oscarsCount ?? '0',
            movie.budget,
            movie.totalBoxOffice,
            movie.genre ?? '',
            movie.mpaaRating ?? '',
            movie.directorName ?? '',
            movie.screenwriterName ?? '',
            movie.operatorName ?? '',
            movie.length ?? '',
            movie.goldenPalmCount
        ];
        cells.forEach((value) => {
            const td = document.createElement('td');
            td.textContent = value ?? '';
            row.append(td);
        });
        tbody.append(row);
    });
    table.append(tbody);
    container.append(table);
}

function renderPeople(container, people) {
    clearNode(container);
    if (!Array.isArray(people) || people.length === 0) {
        renderMessage(container, 'Ничего не найдено.');
        return;
    }
    const list = document.createElement('ul');
    people.forEach((person) => {
        const li = document.createElement('li');
        li.textContent = `${person.id}: ${person.name}`;
        list.append(li);
    });
    container.append(list);
}

function formatDate(value) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleString('ru-RU');
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let message = `Ошибка ${response.status}`;
        try {
            const body = await response.json();
            message = body.message || message;
        } catch (ignore) {
            const text = await response.text().catch(() => '');
            if (text) {
                message = text;
            }
        }
        throw new Error(message);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function handleGenreCount(event) {
    event.preventDefault();
    const genre = new FormData(genreCountForm).get('genre');
    if (!genre) {
        renderError(genreCountResult, 'Выберите жанр.');
        return;
    }
    renderMessage(genreCountResult, 'Загружаем...');
    fetchJson(`${API_BASE}/genre-count?genre=${encodeURIComponent(genre)}`)
        .then((count) => renderMessage(genreCountResult, `Фильмов найдено: ${count}.`))
        .catch((error) => renderError(genreCountResult, error.message));
}

function handleGenreList(event) {
    event.preventDefault();
    const genre = new FormData(genreListForm).get('genre');
    if (!genre) {
        renderError(genreListResult, 'Выберите жанр.');
        return;
    }
    renderMessage(genreListResult, 'Загружаем...');
    fetchJson(`${API_BASE}/genre-list?genre=${encodeURIComponent(genre)}`)
        .then((movies) => renderMovies(genreListResult, movies))
        .catch((error) => renderError(genreListResult, error.message));
}

function handleNameSearch(event) {
    event.preventDefault();
    const formData = new FormData(nameSearchForm);
    const query = formData.get('substring')?.trim();
    if (!query) {
        renderError(nameSearchResult, 'Введите подстроку.');
        return;
    }
    renderMessage(nameSearchResult, 'Загружаем...');
    fetchJson(`${API_BASE}/name-search?substring=${encodeURIComponent(query)}`)
        .then((movies) => renderMovies(nameSearchResult, movies))
        .catch((error) => renderError(nameSearchResult, error.message));
}

function handleNoOscars() {
    renderMessage(noOscarsResult, 'Загружаем...');
    fetchJson(`${API_BASE}/no-oscars`)
        .then((movies) => renderMovies(noOscarsResult, movies))
        .catch((error) => renderError(noOscarsResult, error.message));
}

function handleScreenwriters() {
    renderMessage(screenwritersResult, 'Загружаем...');
    fetchJson(`${API_BASE}/screenwriters-no-oscars`)
        .then((people) => renderPeople(screenwritersResult, people))
        .catch((error) => renderError(screenwritersResult, error.message));
}

function initAnalytics() {
    if (genreCountForm) {
        genreCountForm.addEventListener('submit', handleGenreCount);
    }
    if (genreListForm) {
        genreListForm.addEventListener('submit', handleGenreList);
    }
    if (nameSearchForm) {
        nameSearchForm.addEventListener('submit', handleNameSearch);
    }
    if (noOscarsBtn) {
        noOscarsBtn.addEventListener('click', handleNoOscars);
    }
    if (screenwritersBtn) {
        screenwritersBtn.addEventListener('click', handleScreenwriters);
    }
}

document.addEventListener('DOMContentLoaded', initAnalytics);
