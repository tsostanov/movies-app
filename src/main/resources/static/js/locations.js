const dialog = document.getElementById('locationDialog');
const form = document.getElementById('locationForm');
const createButton = document.getElementById('createLocationBtn');
const cancelButton = document.getElementById('locationDialogCancel');
const submitButton = document.getElementById('locationDialogSubmit');
const errorBox = document.getElementById('locationFormErrors');

const fieldRefs = {
    name: document.getElementById('locationName'),
    x: document.getElementById('locationX'),
    y: document.getElementById('locationY')
};

function resetForm() {
    form.reset();
    errorBox.hidden = true;
    errorBox.textContent = '';
}

function openDialog() {
    resetForm();
    dialog.showModal();
    fieldRefs.name.focus();
}

function closeDialog() {
    dialog.close();
    resetForm();
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
        x: parseFloat(fieldRefs.x.value),
        y: parseFloat(fieldRefs.y.value)
    };
}

async function submitForm(event) {
    event.preventDefault();
    submitButton.disabled = true;
    const payload = gatherPayload();
    try {
        const baseUrl = `${window.location.protocol}//${window.location.host}`;
        const response = await fetch(`${baseUrl}/api/locations`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let body = {};
            let rawMessage = '';
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                body = await response.json().catch(() => ({}));
            } else {
                rawMessage = await response.text().catch(() => '');
            }
            const message = body.message || rawMessage || `Не удалось сохранить локацию (код ${response.status})`;
            showErrors(message, body.details);
            submitButton.disabled = false;
            return;
        }

        closeDialog();
        window.location.reload();
    } catch (error) {
        showErrors(error.message || 'Ошибка сети при сохранении локации');
        submitButton.disabled = false;
    }
}

function init() {
    if (createButton) {
        createButton.addEventListener('click', openDialog);
    }
    if (cancelButton) {
        cancelButton.addEventListener('click', () => closeDialog());
    }
    if (form) {
        form.addEventListener('submit', submitForm);
    }
}

document.addEventListener('DOMContentLoaded', init);
