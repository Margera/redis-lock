package br.com.bsoft.docs.redis_lock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.bsoft.docs.redis_lock.service.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisServiceTest {

    // private static JedisPool jedisPool;
    @Autowired
    private RedisService redisService;

    // @BeforeAll
    // public static void setUp() {
    //     jedisPool = new JedisPool("localhost", 6379, null, "1234abcd");
    // }

    // @BeforeEach
    // public void initialize() {
    //     redisService = new RedisService();
    //     redisService.setRedisPool(jedisPool);
    // }

    // @AfterEach
    // public void cleanup() {
    //     try (Jedis jedis = jedisPool.getResource()) {
    //         jedis.flushAll();
    //     }
    // }

    @Test
    public void testTryInsertWithDelayOnDelete_LockAcquired() {
        // Arrange
        String chaveAcesso = "chaveAcesso";
        String tipo = "tipo";
        int idEmpresa = 123;
        long delayInSeconds = 60;

        // Act
        boolean result = redisService.tryInsertWithDelayOnDelete(chaveAcesso, tipo, idEmpresa, delayInSeconds);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testTryInsertWithDelayOnDelete_LockNotAcquired() {
        // Arrange
        String chaveAcesso = "chaveAcesso";
        String tipo = "tipo";
        int idEmpresa = 123;
        long delayInSeconds = 60;

        SetParams params = new SetParams();
        params.nx().ex((int) delayInSeconds);

        // Simulate a lock already being acquired
        try (Jedis jedis = redisService.getRedisPool().getResource()) {
            String result = jedis.set(chaveAcesso + ":" + tipo + ":" + idEmpresa, "locked", params);
            assertEquals("OK", result);
        }

        // Act
        boolean result = redisService.tryInsertWithDelayOnDelete(chaveAcesso, tipo, idEmpresa, delayInSeconds);

        // Assert
        assertFalse(result);
    }
}