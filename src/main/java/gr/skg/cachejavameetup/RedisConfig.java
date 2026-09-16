package gr.skg.cachejavameetup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Config.
 * By default, spring boot gives the properties
 * spring.data.redis.host=localhost & port=6379
 * so no extra yml config needed
 */
@Configuration
public class RedisConfig {
   @Bean
   RedisTemplate<String, Product> productRedisTemplate(RedisConnectionFactory connectionFactory) {
      // Note: this can be generic for Object, but then 'casting' and custom 'serializer'
      // are needed, to include the whole class in the key. For example:
      // {"@class":"gr.skg.cachejavameetup.Product","id":1,"name":"iPhone 16","price":1200.0}
      // vs {"id":1,"name":"iPhone 16","price":1200.0}
      RedisTemplate<String, Product> template = new RedisTemplate<>();
      template.setConnectionFactory(connectionFactory);
      template.setKeySerializer(new StringRedisSerializer());
      template.setValueSerializer(new JacksonJsonRedisSerializer<>(Product.class));
      return template;
   }
}