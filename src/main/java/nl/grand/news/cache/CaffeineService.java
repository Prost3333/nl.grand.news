package nl.grand.news.cache;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;


@Data
public class CaffeineService {

    private final Cache<String, Boolean> cache;

    public CaffeineService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(345600)) // 4 дня
                .maximumSize(10_000)
                .build();

        System.out.println("✅ Caffeine Cache initialized");
        System.out.println("📦 Estimated cache size: " + cache.estimatedSize());
    }

    public void markNewsAsSent(String url) {
        cache.put(url, true);
        System.out.println(LocalDateTime.now() + " ➕ Cached URL: " + url);
    }

    public boolean isNewsAlreadySent(String normalizedUrl) {
        System.out.println(LocalDateTime.now() + " 🔍 Checking cache for URL: " + normalizedUrl);
        boolean exists = cache.getIfPresent(normalizedUrl) != null;
        System.out.println(LocalDateTime.now() + " ✅ Check result: " + exists);
        return exists;
    }

    public void shutdown(ScheduledExecutorService scheduler) {
        scheduler.shutdownNow();
        cache.cleanUp();
        System.out.println("🛑 Caffeine Cache stopped");
    }
}
