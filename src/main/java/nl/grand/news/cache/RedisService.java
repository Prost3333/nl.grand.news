package nl.grand.news.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;

@Data
public class RedisService {

    public RedisService(){
        try {
            String pingResponse = redis.ping();
            System.out.println("Redis ping: " + pingResponse);
            System.out.println("Redis keys count: " + redis.dbSize());
        } catch (Exception e) {
            System.err.println("Ошибка подключения к Redis:");
            e.printStackTrace();
        }
    }
    private final Jedis redis = new Jedis("localhost", 6379);
    public void markNewsAsSent(String url) {
        redis.setex(url, 172800, "1");
    }

    public boolean isNewsAlreadySent(String normalizedUrl) {
        System.out.println(LocalDateTime.now()+"🔍 Checking Redis for URL: " + normalizedUrl);
        boolean exists = redis.exists(normalizedUrl);
        System.out.println(LocalDateTime.now()+"Check result: " + exists);
        return exists;
    }

    public void shutdown(ScheduledExecutorService scheduler) {
        scheduler.shutdownNow();
        redis.close();
        System.out.println("Redis stopped");
    }
}
