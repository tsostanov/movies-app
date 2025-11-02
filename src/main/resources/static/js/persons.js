const config = window.PersonConfig ?? { persons: [], locations: [] };

const dialog = document.getElementById('personDialog');
const form = document.getElementById('personForm');
const createButton = document.getElementById('createPersonBtn');
const cancelButton = document.getElementById('personDialogCancel');
const submitButton = document.getElementById('personDialogSubmit');
const errorBox = document.getElementById('personFormErrors');

const deleteDialog = document.getElementById('personDeleteDialog');
const deleteForm = document.getElementById('personDeleteForm');
const deleteSummary = document.getElementById('personDeleteSummary');
const deleteErrors = document.getElementById('personDeleteErrors');
const deleteCancelButton = document.getElementById('personDeleteCancel');
const deleteSubmitButton = document.getElementById('personDeleteSubmit');

const detailsDialog = document.getElementById('personDetailsDialog');
const detailsCloseButton = document.getElementById('personDetailsClose');
const detailsRefs = detailsDialog ? {
    title: document.getElementById('personDetailsTitle'),
    name: document.getElementById('personDetailsName'),
    weight: document.getElementById('personDetailsWeight'),
    nationality: document.getElementById('personDetailsNationality'),
    eyeColor: document.getElementById('personDetailsEyeColor'),
    hairColor: document.getElementById('personDetailsHairColor'),
    location: document.getElementById('personDetailsLocation')
} : null;

const fieldRefs = {
    id: document.getElementById('personId'),
    name: document.getElementById('personName'),
    weight: document.getElementById('personWeight'),
    nationality: document.getElementById('personNationality'),
    eyeColor: document.getElementById('personEyeColor'),
    hairColor: document.getElementById('personHairColor'),
    locationId: document.getElementById('personLocationId')
};

const deleteFieldRefs = deleteDialog ? {
    id: document.getElementById('personDeleteId'),
    director: document.getElementById('replaceDirector'),
    screenwriter: document.getElementById('replaceScreenwriter'),
    operator: document.getElementById('replaceOperator')
} : null;

const state = {
    mode: 'create',
    currentId: null
};

function resetForm() {
    if (!form) {
        return;
    }
    form.reset();
    errorBox.hidden = true;
    errorBox.textContent = '';
    fieldRefs.id.value = '';
    state.mode = 'create';
    state.currentId = null;
    if (submitButton) {
        submitButton.disabled = false;
    }
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

function gatherPayload() {
    return {
        name: fieldRefs.name.value.trim(),
        weight: fieldRefs.weight.value ? parseFloat(fieldRefs.weight.value) : null,
        nationality: fieldRefs.nationality.value || null,
        eyeColor: fieldRefs.eyeColor.value || null,
        hairColor: fieldRefs.hairColor.value || null,
        locationId: fieldRefs.locationId.value ? parseInt(fieldRefs.locationId.value, 10) : null
    };
}

function closeDialog() {
    if (dialog && dialog.open) {
        dialog.close();
    }
    resetForm();
}

function openCreateDialog() {
    resetForm();
    state.mode = 'create';
    if (dialog) {
        dialog.showModal();
    }
    fieldRefs.name.focus();
}

function fillForm(data) {
    state.mode = 'edit';
    fieldRefs.id.value = data.id ?? '';
    fieldRefs.name.value = data.name ?? '';
    fieldRefs.weight.value = data.weight ?? '';
    fieldRefs.nationality.value = data.nationality ?? '';
    fieldRefs.eyeColor.value = data.eyeColor ?? '';
    fieldRefs.hairColor.value = data.hairColor ?? '';
    fieldRefs.locationId.value = data.locationId ?? '';
}

function openEditDialog(id) {
    if (!dialog) {
        return;
    }
    resetForm();
    state.mode = 'edit';
    state.currentId = id;
    dialog.showModal();
    submitButton.disabled = true;
    fetch(`/api/persons/${id}`)
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Не удалось загрузить персону #${id}`);
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

function resolveLocationName(locationId) {
    if (!locationId || !Array.isArray(config.locations)) {
        return null;
    }
    const loc = config.locations.find((item) => Number(item.id) === Number(locationId));
    return loc ? loc.name : null;
}

function resetDetailsDialog() {
    if (!detailsRefs) {
        return;
    }
    detailsRefs.title.textContent = 'Информация о персона';
    detailsRefs.name.textContent = '';
    detailsRefs.weight.textContent = '';
    detailsRefs.nationality.textContent = '';
    detailsRefs.eyeColor.textContent = '';
    detailsRefs.hairColor.textContent = '';
    detailsRefs.location.textContent = '';
}

function showDetails(data) {
    if (!detailsDialog || !detailsRefs) {
        return;
    }
    detailsRefs.title.textContent = `Информация о персона #${data.id}`;
    detailsRefs.name.textContent = data.name ?? '—';
    detailsRefs.weight.textContent = data.weight != null
        ? Number(data.weight).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
        : '—';
    detailsRefs.nationality.textContent = data.nationality ?? '—';
    detailsRefs.eyeColor.textContent = data.eyeColor ?? '—';
    detailsRefs.hairColor.textContent = data.hairColor ?? '—';
    const location = resolveLocationName(data.locationId);
    detailsRefs.location.textContent = location ?? '—';
    detailsDialog.showModal();
}

function openDetailsDialog(id) {
    fetch(`/api/persons/${id}`)
        .then((response) => {
            if (!response.ok) {
                throw new Error('Не удалось загрузить данные персоны');
            }
            return response.json();
        })
        .then((data) => showDetails(data))
        .catch((error) => window.alert(error.message));
}

function resetDeleteDialog() {
    if (!deleteDialog || !deleteFieldRefs) {
        return;
    }
    deleteFieldRefs.id.value = '';
    deleteFieldRefs.director.value = '';
    deleteFieldRefs.screenwriter.value = '';
    deleteFieldRefs.operator.value = '';
    deleteErrors.hidden = true;
    deleteErrors.textContent = '';
    deleteSubmitButton.disabled = false;
}

function closeDeleteDialog() {
    if (deleteDialog && deleteDialog.open) {
        deleteDialog.close();
    }
    resetDeleteDialog();
}

function openDeleteDialog(id, name) {
    if (!deleteDialog || !deleteFieldRefs) {
        return;
    }
    resetDeleteDialog();
    deleteFieldRefs.id.value = id;
    deleteSummary.textContent = `Персона «${name}» будет удалена. Выберите замену для каждого типа роли.`;
    updateDeleteSelectOptions(id);
    deleteDialog.showModal();
}

function gatherDeletePayload() {
    const payload = {};
    if (deleteFieldRefs.director.value) {
        payload.directorReplacementId = parseInt(deleteFieldRefs.director.value, 10);
    }
    if (deleteFieldRefs.screenwriter.value) {
        payload.screenwriterReplacementId = parseInt(deleteFieldRefs.screenwriter.value, 10);
    }
    if (deleteFieldRefs.operator.value) {
        payload.operatorReplacementId = parseInt(deleteFieldRefs.operator.value, 10);
    }
    return payload;
}

function updateDeleteSelectOptions(targetId) {
    if (!deleteFieldRefs) {
        return;
    }
    const selects = [
        deleteFieldRefs.director,
        deleteFieldRefs.screenwriter,
        deleteFieldRefs.operator
    ];
    selects.forEach((select) => {
        Array.from(select.options).forEach((option) => {
            if (!option.value) {
                option.disabled = false;
                return;
            }
            const shouldDisable = Number(option.value) === Number(targetId);
            option.disabled = shouldDisable;
            if (shouldDisable && select.value === option.value) {
                select.value = '';
            }
        });
    });
}

async function submitForm(event) {
    event.preventDefault();
    const payload = gatherPayload();
    submitButton.disabled = true;

    const baseUrl = '/api/persons';
    const isEdit = state.mode === 'edit' && state.currentId != null;
    const url = isEdit ? `${baseUrl}/${state.currentId}` : baseUrl;
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
            let message = `Не удалось сохранить персону (код ${response.status})`;
            let details = null;
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                const body = await response.json().catch(() => ({}));
                message = body.message || message;
                details = body.details || null;
            }
            showErrors(message, details);
            submitButton.disabled = false;
            return;
        }

        closeDialog();
        window.location.reload();
    } catch (error) {
        showErrors(error.message || 'Произошла ошибка при сохранении данных');
        submitButton.disabled = false;
    }
}

async function submitDelete(event) {
    event.preventDefault();
    deleteSubmitButton.disabled = true;
    deleteErrors.hidden = true;
    deleteErrors.textContent = '';
    const id = deleteFieldRefs.id.value;
    if (!id) {
        deleteErrors.hidden = false;
        deleteErrors.textContent = 'Не удалось определить персону.';
        deleteSubmitButton.disabled = false;
        return;
    }

    try {
        const response = await fetch(`/api/persons/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(gatherDeletePayload())
        });
        if (!response.ok) {
            let message = `Не удалось удалить персону (код ${response.status})`;
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                const body = await response.json().catch(() => ({}));
                message = body.message || message;
            } else {
                const text = await response.text().catch(() => '');
                if (text) {
                    message = text;
                }
            }
            deleteErrors.hidden = false;
            deleteErrors.textContent = message;
            deleteSubmitButton.disabled = false;
            return;
        }

        closeDeleteDialog();
        window.location.reload();
    } catch (error) {
        deleteErrors.hidden = false;
        deleteErrors.textContent = error.message || 'Произошла ошибка при удалении';
        deleteSubmitButton.disabled = false;
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
    if (button.classList.contains('editPersonBtn')) {
        openEditDialog(id);
        return;
    }
    if (button.classList.contains('deletePersonBtn')) {
        const row = button.closest('tr');
        const nameCell = row ? row.querySelector('td') : null;
        const name = nameCell ? nameCell.textContent.trim() : '';
        openDeleteDialog(id, name);
        return;
    }
    if (button.classList.contains('detailsPersonBtn')) {
        openDetailsDialog(id);
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
    const table = document.querySelector('table tbody');
    if (table) {
        table.addEventListener('click', handleTableClick);
    }
    if (deleteCancelButton) {
        deleteCancelButton.addEventListener('click', () => closeDeleteDialog());
    }
    if (deleteForm) {
        deleteForm.addEventListener('submit', submitDelete);
    }
    if (deleteDialog) {
        deleteDialog.addEventListener('cancel', (event) => {
            event.preventDefault();
            closeDeleteDialog();
        });
    }
    if (detailsCloseButton) {
        detailsCloseButton.addEventListener('click', () => {
            if (detailsDialog && detailsDialog.open) {
                detailsDialog.close();
            }
        });
    }
    if (detailsDialog) {
        detailsDialog.addEventListener('close', () => resetDetailsDialog());
        detailsDialog.addEventListener('cancel', (event) => {
            event.preventDefault();
            detailsDialog.close();
        });
    }
}

document.addEventListener('DOMContentLoaded', init);
