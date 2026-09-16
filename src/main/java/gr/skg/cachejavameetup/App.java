package gr.skg.cachejavameetup;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@EnableCaching
@SpringBootApplication
public class App {

   public static void main(String[] args) {
      SpringApplication.run(App.class, args);
   }

   // Not required. It's just to log the CacheManager implementation
   @Bean
   ApplicationRunner logCacheManager(CacheManager cacheManager) {
      return args -> {
         var cache = cacheManager.getCache("products");
         System.out.println("CacheManager: " + cacheManager.getClass().getSimpleName());
         System.out.println("Native cache: " + cache.getNativeCache().getClass().getName());
      };
   }

}
