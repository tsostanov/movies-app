package ru.ifmo.movies_app.cache;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.ifmo.movies_app.support.CacheStatsLoggingToggle;

@Aspect
@Component
public class L2CacheStatsAspect {

    private static final Logger log = LoggerFactory.getLogger(L2CacheStatsAspect.class);

    private final CacheStatsLoggingToggle toggle;
    private final CacheManager cacheManager;
    private final Class<?> ehcacheClass;
    private final Method statsAccessor;
    private final Method hitsAccessor;
    private final Method missesAccessor;
    private final Method getsAccessor;
    private final Method putsAccessor;

    public L2CacheStatsAspect(CacheStatsLoggingToggle toggle, CacheManager cacheManager) {
        this.toggle = toggle;
        this.cacheManager = cacheManager;
        this.ehcacheClass = resolveClass("org.ehcache.jsr107.Eh107Cache");
        Class<?> statsClass = resolveClass("org.ehcache.jsr107.Eh107CacheStatisticsMXBean");
        this.statsAccessor = resolveMethod(ehcacheClass, "getStatisticsMBean");
        this.hitsAccessor = resolveMethod(statsClass, "getCacheHits");
        this.missesAccessor = resolveMethod(statsClass, "getCacheMisses");
        this.getsAccessor = resolveMethod(statsClass, "getCacheGets");
        this.putsAccessor = resolveMethod(statsClass, "getCachePuts");
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logCacheStats(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!toggle.isEnabled() || statsAccessor == null || ehcacheClass == null) {
            return joinPoint.proceed();
        }
        Map<String, CacheSnapshot> before = snapshot();
        Object result = joinPoint.proceed();
        Map<String, CacheSnapshot> after = snapshot();
        logDelta(joinPoint, before, after);
        return result;
    }

    private Map<String, CacheSnapshot> snapshot() {
        Map<String, CacheSnapshot> state = new LinkedHashMap<>();
        cacheManager.getCacheNames().forEach(name -> {
            Cache<?, ?> cache = cacheManager.getCache(name);
            if (cache == null) {
                return;
            }
            CacheSnapshot stats = extractStats(cache);
            if (stats != null) {
                state.put(name, stats);
            }
        });
        return state;
    }

    private CacheSnapshot extractStats(Cache<?, ?> cache) {
        if (statsAccessor == null || ehcacheClass == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Object ehcache = cache.unwrap((Class<Object>) ehcacheClass);
            Object stats = statsAccessor.invoke(ehcache);
            if (stats == null) {
                return null;
            }
            long hits = invokeLong(stats, hitsAccessor);
            long misses = invokeLong(stats, missesAccessor);
            long gets = invokeLong(stats, getsAccessor);
            long puts = invokeLong(stats, putsAccessor);
            return new CacheSnapshot(hits, misses, gets, puts);
        } catch (Exception ex) {
            log.debug("Failed to read cache stats for {}", cache.getName(), ex);
            return null;
        }
    }

    private long invokeLong(Object target, Method method) {
        if (target == null || method == null) {
            return 0;
        }
        try {
            Object value = method.invoke(target);
            return value instanceof Number number ? number.longValue() : 0;
        } catch (Exception ex) {
            log.debug("Failed to invoke stats method {}", method.getName(), ex);
            return 0;
        }
    }

    private void logDelta(ProceedingJoinPoint joinPoint,
                          Map<String, CacheSnapshot> before,
                          Map<String, CacheSnapshot> after) {
        after.forEach((name, snapshot) -> {
            CacheSnapshot prev = before.getOrDefault(name, CacheSnapshot.zero());
            long deltaHits = snapshot.hits - prev.hits;
            long deltaMisses = snapshot.misses - prev.misses;
            long deltaPuts = snapshot.puts - prev.puts;
            long deltaGets = snapshot.gets - prev.gets;
            if (deltaHits == 0 && deltaMisses == 0 && deltaPuts == 0 && deltaGets == 0) {
                return;
            }
            log.info(
                    "L2 cache [{}] after {} -> delta hits={}, delta misses={}, delta puts={}, delta gets={}, totals h/m/p/g={}/{}/{}/{}",
                    name,
                    joinPoint.getSignature().toShortString(),
                    deltaHits, deltaMisses, deltaPuts, deltaGets,
                    snapshot.hits, snapshot.misses, snapshot.puts, snapshot.gets
            );
        });
    }

    private Class<?> resolveClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            log.warn("Cache statistics class {} not found, stats logging disabled", name);
            return null;
        }
    }

    private Method resolveMethod(Class<?> type, String name) {
        if (type == null) {
            return null;
        }
        try {
            Method method = type.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            log.warn("Cannot resolve method {} on {}", name, type.getName());
            return null;
        }
    }

    private record CacheSnapshot(long hits, long misses, long gets, long puts) {
        static CacheSnapshot zero() {
            return new CacheSnapshot(0, 0, 0, 0);
        }
    }
}
