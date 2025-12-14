package ru.ifmo.movies_app.support;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class ImportFailpointToggle {

    private final AtomicReference<ImportFailpointState> state = new AtomicReference<>(ImportFailpointState.NONE);

    public ImportFailpointState getState() {
        return state.get();
    }

    public void setState(ImportFailpointState newState) {
        state.set(newState != null ? newState : ImportFailpointState.NONE);
    }

    public void afterFileUpload() {
        if (state.get() == ImportFailpointState.AFTER_FILE_UPLOAD) {
            throw new IllegalStateException("Simulated failure after file upload");
        }
    }

    public void afterDbCommit() {
        if (state.get() == ImportFailpointState.AFTER_DB_COMMIT) {
            throw new IllegalStateException("Simulated failure after DB commit");
        }
    }
}
