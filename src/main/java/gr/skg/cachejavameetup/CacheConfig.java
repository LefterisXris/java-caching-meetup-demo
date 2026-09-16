package gr.skg.cachejavameetup;

import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {

   // This is needed, as otherwise the default Serializable will store values
   // as binary instead of json. Sometimes binary is preferred (for performance)
   @Bean
   RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
      return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(cacheProperties.getRedis().getTimeToLive()) // inject property, otherwise -1!
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                  .fromSerializer(new JacksonJsonRedisSerializer<>(Product.class)));
   }

}