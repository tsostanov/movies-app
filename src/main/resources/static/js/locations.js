const dialog = document.getElementById('locationDialog');
const form = document.getElementById('locationForm');
const createButton = document.getElementById('createLocationBtn');
const cancelButton = document.getElementById('locationDialogCancel');
const submitButton = document.getElementById('locationDialogSubmit');
const errorBox = document.getElementById('locationFormErrors');
const tableBody = document.querySelector('table tbody');
const dialogTitle = document.getElementById('locationDialogTitle');

const fieldRefs = {
    id: document.getElementById('locationId'),
    name: document.getElementById('locationName'),
    x: document.getElementById('locationX'),
    y: document.getElementById('locationY')
};

const state = {
    mode: 'create',
    currentId: null
};

function resetForm() {
    form.reset();
    fieldRefs.id.value = '';
    errorBox.hidden = true;
    errorBox.textContent = '';
    if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = 'Сохранить';
    }
    if (dialogTitle) {
        dialogTitle.textContent = 'Локация';
    }
    state.mode = 'create';
    state.currentId = null;
}

function showErrors(message, details) {
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

function gatherPayload() {
    return {
        name: fieldRefs.name.value.trim(),
        x: fieldRefs.x.value ? parseFloat(fieldRefs.x.value) : null,
        y: fieldRefs.y.value ? parseFloat(fieldRefs.y.value) : null
    };
}

function openCreateDialog() {
    resetForm();
    state.mode = 'create';
    dialogTitle.textContent = 'Добавление локации';
    submitButton.textContent = 'Создать';
    dialog.showModal();
    fieldRefs.name.focus();
}

function fillForm(data) {
    fieldRefs.id.value = data.id ?? '';
    fieldRefs.name.value = data.name ?? '';
    fieldRefs.x.value = data.x ?? '';
    fieldRefs.y.value = data.y ?? '';
}

function openEditDialog(id) {
    resetForm();
    state.mode = 'edit';
    state.currentId = id;
    submitButton.textContent = 'Сохранить';
    dialogTitle.textContent = 'Изменение локации';
    dialog.showModal();
    submitButton.disabled = true;
    fetch(`/api/locations/${id}`)
        .then((response) => {
            if (!response.ok) {
                throw new Error('Не удалось загрузить данные локации');
            }
            return response.json();
        })
        .then((data) => {
            fillForm(data);
            submitButton.disabled = false;
            fieldRefs.name.focus();
        })
        .catch((error) => {
            window.alert(error.message);
            closeDialog();
        });
}

function closeDialog() {
    if (dialog && dialog.open) {
        dialog.close();
    }
    resetForm();
}

async function submitForm(event) {
    event.preventDefault();
    submitButton.disabled = true;
    const payload = gatherPayload();

    const isEdit = state.mode === 'edit' && state.currentId != null;
    const url = isEdit ? `/api/locations/${state.currentId}` : '/api/locations';
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let message = `Не удалось сохранить локацию (код ${response.status})`;
            let details = null;
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                const body = await response.json().catch(() => ({}));
                message = body.message || message;
                details = body.details || null;
            } else {
                const raw = await response.text().catch(() => '');
                if (raw) {
                    message = raw;
                }
            }
            showErrors(message, details);
            submitButton.disabled = false;
            return;
        }

        closeDialog();
        window.location.reload();
    } catch (error) {
        showErrors(error.message || 'Произошла ошибка при сохранении локации');
        submitButton.disabled = false;
    }
}

function handleTableClick(event) {
    const button = event.target.closest('button');
    if (!button) {
        return;
    }
    const id = Number(button.dataset.id);
    if (Number.isNaN(id)) {
        return;
    }
    if (button.classList.contains('editLocationBtn')) {
        openEditDialog(id);
        return;
    }
    if (button.classList.contains('deleteLocationBtn')) {
        if (!window.confirm('Удалить локацию?')) {
            return;
        }
        fetch(`/api/locations/${id}`, { method: 'DELETE' })
            .then((response) => {
                if (!response.ok) {
                    return response.json()
                        .then((body) => Promise.reject(body.message || 'Не удалось удалить локацию'));
                }
                window.location.reload();
                return null;
            })
            .catch((error) => window.alert(error));
    }
}

function init() {
    if (createButton) {
        createButton.addEventListener('click', () => openCreateDialog());
    }
    if (cancelButton) {
        cancelButton.addEventListener('click', () => closeDialog());
    }
    if (form) {
        form.addEventListener('submit', submitForm);
    }
    if (tableBody) {
        tableBody.addEventListener('click', handleTableClick);
    }
}

document.addEventListener('DOMContentLoaded', init);
