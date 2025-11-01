# `GamificationService` Mikroxidmətinin Dərinləməsinə Kod İzahı

Bu sənəd, `GamificationService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, layihənin arxitekturasını, məntiqini və hər bir komponentin vəzifəsini tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `GamificationServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/gamificationservice/GamificationServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir.

```java
package com.example.gamificationservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Gamification Service API", version = "1.0", description = "API for Gamification Service"))
public class GamificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamificationServiceApplication.class, args);
    }
}
```

### Sətir-Sətir İzahı

- **`@SpringBootApplication`**: Standart Spring Boot tətbiqini işə salan əsas annotasiya.
- **`@OpenAPIDefinition`**: Swagger UI sənədləşdirməsi üçün API haqqında ümumi məlumatları təyin edir.

---

## 2. `config` Paketi

Bu paket, Kafka consumer-ı üçün konfiqurasiya sinifini saxlayır.

### 2.1. `KafkaConsumerConfig.java`

**Fayl Yolu:** `src/main/java/com/example/gamificationservice/config/KafkaConsumerConfig.java`

Bu sinif, digər servislərdən gələn Kafka hadisələrini (event-lərini) dinləmək üçün lazım olan bean-ləri konfiqurasiya edir.

```java
package com.example.gamificationservice.config;

// ... importlar
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    // ... @Value sahələri

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Vacibdir!
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

#### Sətir-Sətir İzahı

- **`@Bean public ConsumerFactory<String, Object> consumerFactory()`**: Kafka consumer-ları yaratmaq üçün bir "fabrika" bean-i yaradır.
    - `GROUP_ID_CONFIG`: Eyni `groupId`-yə malik consumer-lar bir "consumer group" təşkil edir. Bir topic-dəki hər bir mesaj yalnız bir qrupdakı bir consumer-ə çatdırılır. Bu, eyni hadisənin bir neçə dəfə emal olunmasının qarşısını alır.
    - `KEY_DESERIALIZER_CLASS_CONFIG`: Mesaj açarının (`key`) necə deserializasiya ediləcəyini təyin edir.
    - `VALUE_DESERIALIZER_CLASS_CONFIG`: Mesajın dəyərinin (`value`) necə deserializasiya ediləcəyini təyin edir. `JsonDeserializer` istifadəsi, gələn JSON məlumatının bizim DTO/Event siniflərimizə avtomatik çevrilməsini təmin edir.
    - **`JsonDeserializer.TRUSTED_PACKAGES, "*"`**: **Çox Vacib!** Təhlükəsizlik səbəbi ilə, `JsonDeserializer` default olaraq yalnız `java.util` və `java.lang` paketlərindəki siniflərə deserializasiya etməyə icazə verir. `"*"` təyin etməklə, biz deserializer-ə bizim proyektimizdəki (`com.example.gamificationservice.event`) paketlər daxil olmaqla, istənilən paketdəki siniflərə etibar etməsini deyirik. Bu olmadan, Kafka-dan gələn `ProblemSolvedEvent` kimi obyektlər deserializasiya oluna bilməzdi.
- **`@Bean public ConcurrentKafkaListenerContainerFactory<...> kafkaListenerContainerFactory()`**: `@KafkaListener` annotasiyası ilə işarələnmiş metodlar üçün arxa planda mesajları dinləyən konteynerləri yaradan və idarə edən əsas fabrikadır.

---

## 3. `entity` Paketi

Bu paket, MongoDB verilənlər bazasındakı kolleksiyalara uyğun gələn document siniflərini saxlayır.

### 3.1. `UserStats.java`

```java
@Data
@Document(collection = "user_stats")
public class UserStats {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private int xp;
    private int level;
    private List<String> badges;
}
```

- **`@Document(collection = "user_stats")`**: Bu sinifin MongoDB-də `user_stats` adlı bir kolleksiyaya map olunduğunu bildirir.
- **`@Id`**: Bu sahənin document-in birincili açarı (`_id`) olduğunu bildirir.
- **`@Indexed(unique = true)`**: `userId` sahəsi üzərində bir indeks yaradılmasını təmin edir. Bu, `userId`-yə görə axtarışların sürətini kəskin şəkildə artırır. `unique = true` isə hər bir istifadəçinin yalnız bir statistika sənədinin olmasını təmin edir.

### 3.2. `XpTransaction.java`

Bu document, hər bir XP qazanma əməliyyatını ayrı-ayrılıqda saxlamaq üçün istifadə olunur. Bu, gələcəkdə istifadəçinin XP tarixçəsini izləmək və analiz etmək üçün faydalıdır.

---

## 4. `repository` Paketi

Bu paket, MongoDB ilə əlaqə qurmaq üçün istifadə olunan `Spring Data MongoDB` repository interfeyslərini saxlayır.

### 4.1. `UserStatsRepository.java`

```java
@Repository
public interface UserStatsRepository extends MongoRepository<UserStats, String> {
    Optional<UserStats> findByUserId(String userId);
}
```

- **`extends MongoRepository<UserStats, String>`**: Bu interfeysi genişləndirmək, `UserStats` document-i üçün standart CRUD əməliyyatlarını təmin edir.
- **`Optional<UserStats> findByUserId(String userId)`**: Metod adına əsasən, Spring Data MongoDB avtomatik olaraq `userId` sahəsinə görə axtarış edən bir sorğu yaradır.

---

## 5. `service` Paketi

Bu paket, tətbiqin əsas biznes məntiqini həyata keçirir.

### 5.1. `GamificationService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserStatsRepository userStatsRepository;
    private final XpTransactionRepository xpTransactionRepository;
    private final UserStatsMapper userStatsMapper;

    // ... metodlar
}
```

#### Kafka Listener Metodları

Bu servisin ən vacib hissəsi Kafka hadisələrini dinləyən metodlardır.

```java
@KafkaListener(topics = "problem-solved-topic", groupId = "gamification-group")
public void consumeProblemSolvedEvent(ProblemSolvedEvent event) {
    log.info("Consumed ProblemSolvedEvent: {}", event);
    addXp(new AddXpDto(event.getSolvedByUserId(), 30, "ProblemSolved"));
}

@KafkaListener(topics = "swap-completed-topic", groupId = "gamification-group")
public void consumeSwapCompletedEvent(SwapCompletedEvent event) {
    log.info("Consumed SwapCompletedEvent: {}", event);
    addXp(new AddXpDto(event.getUserId(), 50, "SwapCompleted"));
}
```

- **`@KafkaListener(...)`**: Bu annotasiya, metodun bir Kafka mesaj dinləyicisi olduğunu bildirir.
    - `topics`: Hansı topic-i dinləyəcəyini göstərir.
    - `groupId`: Bu dinləyicinin aid olduğu consumer qrupunu təyin edir.
- **`consumeProblemSolvedEvent(ProblemSolvedEvent event)`**: `problem-solved-topic`-inə yeni bir mesaj gəldikdə bu metod avtomatik olaraq çağırılır. Spring Kafka, gələn JSON mesajını `ProblemSolvedEvent` obyektinə deserializasiya edir.
- **`addXp(...)`**: Mesaj qəbul edildikdən sonra, hadisənin növünə uyğun olaraq (`ProblemSolved` üçün +30 XP, `SwapCompleted` üçün +50 XP) `addXp` metodu çağırılır.

#### Əsas Biznes Məntiqi Metodları

- **`addXp(AddXpDto addXpDto)`**:
    1. `userStatsRepository.findByUserId()` ilə istifadəçinin statistikasını axtarır. Əgər tapılmazsa, `createNewUserStats` metodu ilə yeni bir statistika sənədi yaradır.
    2. İstifadəçinin mövcud `xp`-sinin üzərinə yeni qazanılan XP-ni əlavə edir.
    3. **`checkForLevelUp(userStats)`** metodunu çağıraraq səviyyə artımı olub-olmadığını yoxlayır.
    4. Yenilənmiş `userStats` obyektini verilənlər bazasına yazır.
    5. Bu əməliyyatı qeydə almaq üçün yeni bir `XpTransaction` obyekti yaradır və onu da verilənlər bazasına yazır.

- **`checkForLevelUp(UserStats userStats)`**:
    1. İstifadəçinin hazırkı XP-sini 100-ə bölərək hədəflənən səviyyəni hesablayır (`newLevel = (currentXp / 100) + 1`).
    2. Əgər yeni səviyyə hazırkı səviyyədən böyükdürsə, istifadəçinin səviyyəsini yeniləyir.
    3. Səviyyə artımı zamanı, əgər istifadəçidə hələ yoxdursa, ona "Helper" nişanını (`badge`) əlavə edir.

- **`getUserStats(String userId)`**: Verilmiş istifadəçi ID-si üçün statistikanı tapır və DTO-ya çevirərək qaytarır. Əgər istifadəçi üçün statistika yoxdursa, avtomatik olaraq yeni bir boş statistika yaradılır.

---

## 6. Digər Paketlər

- **`controller`**: Xarici sorğuları qəbul edən `GamificationController`-i saxlayır. Endpoint-lər vasitəsilə istifadəçi statistikasını və nişanlarını əldə etməyə və manual olaraq XP əlavə etməyə imkan verir.
- **`dto`**: API sərhədlərində istifadə olunan `UserStatsDto` və `AddXpDto` siniflərini saxlayır.
- **`event`**: Bu servisin dinlədiyi Kafka hadisə siniflərini (`ProblemSolvedEvent`, `SwapCompletedEvent`) saxlayır.
- **`mapper`**: `UserStats` entity-sini `UserStatsDto`-ya çevirən `UserStatsMapper` interfeysini saxlayır.
- **`exception`**: `CommunityService`-də olduğu kimi, ümumi xətaları idarə edən qlobal `RestExceptionHandler`-ı saxlayır.

---
**`GamificationService` üçün Kod İzahının Sonu.**
