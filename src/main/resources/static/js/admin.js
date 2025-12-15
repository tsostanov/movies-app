(() => {
    const messageBox = document.getElementById('adminMessage');

    const showMessage = (text, isError = false) => {
        if (!messageBox) {
            return;
        }
        messageBox.textContent = text;
        messageBox.hidden = false;
        messageBox.className = `message ${isError ? 'error' : 'success'}`;
        setTimeout(() => {
            messageBox.hidden = true;
        }, 5000);
    };

    const postAction = async (url) => {
        const response = await fetch(url, { method: 'POST' });
        if (!response.ok) {
            const body = await response.json().catch(() => ({}));
            throw new Error(body.message || `Запрос к ${url} завершился ошибкой`);
        }
        return response.json().catch(() => ({}));
    };

    const bindButtons = () => {
        const cacheOn = document.getElementById('cacheOnBtn');
        const cacheOff = document.getElementById('cacheOffBtn');
        const failpointSelect = document.getElementById('failpointSelect');
        const setFailpoint = document.getElementById('setFailpointBtn');
        const recoverBtn = document.getElementById('recoverBtn');
        const recoverLimit = document.getElementById('recoverLimit');

        if (cacheOn) {
            cacheOn.addEventListener('click', async () => {
                try {
                    await postAction('/api/admin/cache-stats-logging?enabled=true');
                    showMessage('Логирование кэша включено');
                } catch (err) {
                    showMessage(err.message, true);
                }
            });
        }

        if (cacheOff) {
            cacheOff.addEventListener('click', async () => {
                try {
                    await postAction('/api/admin/cache-stats-logging?enabled=false');
                    showMessage('Логирование кэша выключено');
                } catch (err) {
                    showMessage(err.message, true);
                }
            });
        }

        if (setFailpoint && failpointSelect) {
            setFailpoint.addEventListener('click', async () => {
                const value = failpointSelect.value || 'NONE';
                try {
                    await postAction(`/api/admin/import-failpoint?value=${encodeURIComponent(value)}`);
                    showMessage(`Failpoint установлен: ${value}`);
                } catch (err) {
                    showMessage(err.message, true);
                }
            });
        }

        if (recoverBtn && recoverLimit) {
            recoverBtn.addEventListener('click', async () => {
                const limit = Math.max(parseInt(recoverLimit.value, 10) || 50, 1);
                try {
                    const res = await postAction(`/api/admin/recover-imports?limit=${limit}`);
                    showMessage(`Recovery выполнен. Обработано: ${res.recovered ?? 0}`);
                } catch (err) {
                    showMessage(err.message, true);
                }
            });
        }
    };

    document.addEventListener('DOMContentLoaded', bindButtons);
})();
