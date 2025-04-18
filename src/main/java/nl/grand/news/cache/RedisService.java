package nl.grand.news.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;


import java.net.URI;


@Data
public class RedisService {

    private final Jedis redis;

    public RedisService() {
        this.redis = createJedis();

        try {
            String pingResponse = redis.ping();
            System.out.println("✅ Redis ping: " + pingResponse);
            System.out.println("📦 Redis keys count: " + redis.dbSize());
        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения к Redis:");
            e.printStackTrace();
        }
    }

    private Jedis createJedis() {
        String redisUrl = System.getenv("REDIS_URL");
        if (redisUrl != null && !redisUrl.isEmpty()) {
            try {
                URI uri = new URI(redisUrl);
                return new Jedis(uri);
            } catch (Exception e) {
                throw new RuntimeException("❌ Не удалось подключиться к Redis по REDIS_URL", e);
            }
        } else {
            System.out.println("ℹ️ REDIS_URL не найден, подключаюсь к localhost:6379");
            return new Jedis("localhost", 6379);
        }
    }

    public void markNewsAsSent(String url) {
        redis.setex(url, 172800, "1"); // 2 дня
    }

    public boolean isNewsAlreadySent(String normalizedUrl) {
        System.out.println(LocalDateTime.now() + " 🔍 Checking Redis for URL: " + normalizedUrl);
        boolean exists = redis.exists(normalizedUrl);
        System.out.println(LocalDateTime.now() + " ✅ Check result: " + exists);
        return exists;
    }

    public void shutdown(ScheduledExecutorService scheduler) {
        scheduler.shutdownNow();
        redis.close();
        System.out.println("🛑 Redis stopped");
    }
}
