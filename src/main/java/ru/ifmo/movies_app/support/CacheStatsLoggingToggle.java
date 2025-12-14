package ru.ifmo.movies_app.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheStatsLoggingToggle {

    private final AtomicBoolean enabled;

    public CacheStatsLoggingToggle(@Value("${app.cache.stats-logging-enabled:false}") boolean enabled) {
        this.enabled = new AtomicBoolean(enabled);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean value) {
        enabled.set(value);
    }
}
