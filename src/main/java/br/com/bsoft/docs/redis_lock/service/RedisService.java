package br.com.bsoft.docs.redis_lock.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

@Service
public class RedisService {

    private JedisPool redisPool;

    @PostConstruct
    public void initializeJedisPool() {
        this.redisPool = new JedisPool("localhost", 6379);
    }

    @PreDestroy
    public void shutdownJedisPool() {
        this.redisPool.close();
    }

    public JedisPool getRedisPool() {
        return this.redisPool;
    }

    /**
     * Tenta inserir um valor no Redis com um tempo de expiração e retorna se a operação foi bem-sucedida.
     * 
     * @param chaveAcesso a chave de acesso para identificar o valor no Redis
     * @param tipo o tipo de valor a ser inserido
     * @param idEmpresa o ID da empresa relacionada ao valor
     * @param delayInSeconds o tempo de expiração em segundos para o valor inserido
     * @return true se o valor foi inserido com sucesso, false caso contrário
     */
    public boolean tryInsertWithDelayOnDelete(String chaveAcesso, String tipo, int idEmpresa, long delayInSeconds) {
        String key = chaveAcesso + ":" + tipo + ":" + idEmpresa;
        try (Jedis jedis = redisPool.getResource()) {

            // Cria uma instância de SetParams para configurar opções específicas para o comando SET no Redis.
            // params.nx() configura o comando SET para executar apenas se a chave não existir (NX = "not exists").
            // params.ex((int) delayInSeconds) define um tempo de expiração para a chave, em segundos. 
            // Isso significa que a chave será automaticamente deletada do Redis após o tempo especificado.

            SetParams params = new SetParams();
            params.nx().ex((int) delayInSeconds);
            String result = jedis.set(key, "locked", params);
            
            if ("OK".equals(result)) {
                // Lock adquirido, proceda com a inserção no banco de dados
                // Não é necessário definir um tempo de expiração para o lock aqui, pois já foi definido no comando SET
                return true; // Indica sucesso
            } else {
                // Lock não adquirido, entrada duplicada
                return false; // Indica falha
            }
        }
    }

    
}
