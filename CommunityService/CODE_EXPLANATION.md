# `CommunityService` Mikroxidmətinin Dərinləməsinə Kod İzahı

Bu sənəd, `CommunityService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, layihənin arxitekturasını, məntiqini və hər bir komponentin vəzifəsini tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `CommunityServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/communityservice/CommunityServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir. Tətbiqi başladan və əsas konfiqurasiyaları aktivləşdirən `main` metodunu ehtiva edir.

```java
package com.example.communityservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Community Service API", version = "1.0", description = "API for Community Service"))
public class CommunityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityServiceApplication.class, args);
    }
}
```

### Sətir-Sətir İzahı

- **`@SpringBootApplication`**: Bu, üç əsas annotasiyanı özündə birləşdirən bir Spring Boot annotasiyasıdır: `@Configuration`, `@EnableAutoConfiguration`, və `@ComponentScan`. Bu annotasiya Spring Boot tətbiqini işə salmaq üçün lazımi olan bütün standart konfiqurasiyaları təmin edir.
- **`@OpenAPIDefinition`**: Bu annotasiya `springdoc-openapi` kitabxanası tərəfindən təmin edilir və Swagger UI sənədləşdirməsinin başlığını, versiyasını və təsvirini təyin etmək üçün istifadə olunur.
- **`public static void main(String[] args)`**: Java tətbiqlərinin standart giriş nöqtəsi.
- **`SpringApplication.run(...)`**: Spring Boot tətbiqini başladan əsas metod. Daxili `Tomcat` serverini işə salır və tətbiqi veb-server kimi fəaliyyətə hazırlayır.

---

## 2. `config` Paketi

Bu paket, tətbiqin fərqli modulları üçün mərkəzi konfiqurasiya siniflərini saxlayır.

### 2.1. `KafkaProducerConfig.java`

**Fayl Yolu:** `src/main/java/com/example/communityservice/config/KafkaProducerConfig.java`

Bu sinif, Kafka-ya mesaj göndərmək üçün lazım olan `KafkaTemplate` bean-ini konfiqurasiya edir.

```java
package com.example.communityservice.config;

// ... importlar
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### Sətir-Sətir İzahı

- **`@Configuration`**: Bu sinifin bir Spring konfiqurasiya sinifi olduğunu göstərir.
- **`@Value(...)`**: `application.yml` faylından Kafka serverlərinin ünvanını oxuyur.
- **`@Bean public ProducerFactory<String, Object> producerFactory()`**: Kafka producer-ları yaratmaq üçün bir "fabrika" bean-i yaradır.
    - `KEY_SERIALIZER_CLASS_CONFIG`: Mesaj açarının (`key`) necə seriallaşdırılacağını təyin edir (burada `String`).
    - `VALUE_SERIALIZER_CLASS_CONFIG`: Mesajın dəyərinin (`value`) necə seriallaşdırılacağını təyin edir. `JsonSerializer.class` istifadəsi, bizim DTO obyektlərimizin avtomatik olaraq JSON formatına çevrilərək Kafka-ya göndərilməsini təmin edir.
- **`@Bean public KafkaTemplate<String, Object> kafkaTemplate()`**: Mesajları Kafka topic-lərinə göndərmək üçün istifadə olunan əsas köməkçi sinif olan `KafkaTemplate`-i yaradır və konfiqurasiya edir.

---

## 3. `controller` Paketi

Bu paket, xarici dünyadan gələn HTTP sorğularını qəbul edən və onlara cavab verən REST controller siniflərini saxlayır.

### 3.1. `CommunityController.java`

**Fayl Yolu:** `src/main/java/com/example/communityservice/controller/CommunityController.java`

Bu sinif, qruplar və problemlərlə bağlı bütün əməliyyatlar üçün endpoint-ləri təmin edir.

```java
package com.example.communityservice.controller;

// ... importlar

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Tag(name = "Community Service")
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/groups/create")
    @Operation(summary = "Create a new group")
    public GroupDto createGroup(@RequestBody CreateGroupDto createGroupDto) {
        return communityService.createGroup(createGroupDto);
    }

    // ... digər metodlar
}
```

#### Sinif Səviyyəsində İzahlar

- **`@RestController`**: Bu sinifdəki bütün metodların qaytardığı dəyərlərin avtomatik olaraq JSON formatına çevrilərək HTTP cavabının `body`-sinə yazılacağını bildirir.
- **`@RequestMapping("/community")`**: Bu sinifdəki bütün endpoint-lərin URL-lərinin `/community` prefiksi ilə başlayacağını bildirir.
- **`@RequiredArgsConstructor`**: Lombok annotasiyası. `private final CommunityService communityService;` sahəsi üçün avtomatik olaraq bir konstruktor yaradır (Constructor Injection).
- **`@Tag(name = "Community Service")`**: Swagger UI-da bu controller-dəki endpoint-ləri qruplaşdırır.

#### Metodların İzahı

- **`createGroup(@RequestBody CreateGroupDto createGroupDto)`**: `/community/groups/create` ünvanına gələn `POST` sorğusunu idarə edir. `@RequestBody` annotasiyası sorğunun `body`-sindəki JSON-u `CreateGroupDto` obyektinə çevirir. Sonra bu obyekti `communityService`-ə ötürür.
- **`getGroups()`**: `/community/groups` ünvanına gələn `GET` sorğusunu idarə edir və bütün qrupların siyahısını qaytarır.
- **`joinGroup(@PathVariable Long groupId, @RequestParam String userId)`**: `/community/groups/{groupId}/join` ünvanına gələn `POST` sorğusunu idarə edir.
    - `@PathVariable Long groupId`: URL-in `groupId` hissəsindəki dəyişəni metodun parametrinə ötürür.
    - `@RequestParam String userId`: Sorğunun query parametrindən (`?userId=...`) istifadəçi ID-sini oxuyur.
- **`createProblem(@RequestBody CreateProblemDto createProblemDto)`**: Yeni bir problem yaratmaq üçün istifadə olunur.
- **`getProblems(@PathVariable Long groupId, Pageable pageable)`**: Bir qrupa aid problemləri səhifələnmiş (paginated) şəkildə qaytarır. `Pageable` parametri Spring Data tərəfindən avtomatik təmin edilir və sorğudakı `page`, `size`, `sort` kimi parametrləri özündə saxlayır.
- **`solveProblem(@PathVariable Long problemId, @RequestParam String userId)`**: Bir problemi "həll edilmiş" kimi işarələmək üçün istifadə olunur.

---

## 4. `entity` Paketi

Bu paket, verilənlər bazasındakı cədvəllərə uyğun gələn JPA entity siniflərini saxlayır.

### 4.1. `Group.java`

```java
@Data
@Entity
@Table(name = "community_groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    // ...
    @ElementCollection
    private List<String> members;
    private LocalDateTime createdAt;
}
```

- **`@Entity`**: Bu sinifin bir JPA entity-si olduğunu bildirir.
- **`@Table(name = "community_groups")`**: Entity-nin `community_groups` adlı cədvələ map olunacağını göstərir.
- **`@Id` və `@GeneratedValue`**: `id` sahəsinin birincili açar (primary key) olduğunu və dəyərinin verilənlər bazası tərəfindən avtomatik artırılacağını bildirir (`IDENTITY` strategiyası).
- **`@ElementCollection`**: `members` siyahısının ayrı bir cədvəldə saxlanılacağını, lakin `Group` entity-sinin bir hissəsi kimi idarə ediləcəyini bildirir. Bu, sadə tiplərdən (məsələn, `String`) ibarət kolleksiyaları saxlamaq üçün rahat bir yoldur.

### 4.2. `Problem.java`

Bu entity də oxşar şəkildə `problem` cədvəlini təmsil edir və problemin başlığı, təsviri, kim tərəfindən yaradıldığı, hansı qrupa aid olduğu və həll edilmə statusu kimi məlumatları saxlayır.

---

## 5. `repository` Paketi

Bu paket, verilənlər bazası ilə əlaqə qurmaq üçün istifadə olunan `Spring Data JPA` repository interfeyslərini saxlayır.

### 5.1. `GroupRepository.java` və `ProblemRepository.java`

```java
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Page<Problem> findByGroupId(Long groupId, Pageable pageable);
}
```

- **`extends JpaRepository<Entity, ID>`**: Bu interfeysləri genişləndirmək, heç bir kod yazmadan `save()`, `findById()`, `findAll()`, `delete()` kimi standart CRUD əməliyyatlarını əldə etməyə imkan verir.
- **`Page<Problem> findByGroupId(Long groupId, Pageable pageable)`**: Bu, Spring Data-nın "Query Creation from Method Names" xüsusiyyətidir. Metodun adına əsasən, Spring Data avtomatik olaraq `groupId`-yə görə axtarış edən və nəticəni `Pageable` parametrinə uyğun olaraq səhifələrə bölən bir sorğu yaradır.

---

## 6. `service` Paketi

Bu paket, tətbiqin əsas biznes məntiqini həyata keçirən servis siniflərini saxlayır.

### 6.1. `CommunityService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final GroupRepository groupRepository;
    private final ProblemRepository problemRepository;
    private final GroupMapper groupMapper;
    private final ProblemMapper problemMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ... metodlar
}
```

#### Metodların İzahı

- **`createGroup(CreateGroupDto createGroupDto)`**:
    1. `GroupMapper` vasitəsilə DTO-nu entity-yə çevirir.
    2. Qrupun yaradılma tarixini və yaradan istifadəçini üzvlər siyahısına əlavə edir.
    3. `groupRepository.save()` ilə verilənlər bazasına yazır.
    4. Nəticəni DTO-ya çevirərək controller-ə qaytarır.

- **`joinGroup(Long groupId, String userId)`**:
    1. Verilmiş `groupId` ilə qrupu tapır. Tapılmazsa, `RuntimeException` atır.
    2. Verilmiş `userId`-ni qrupun `members` siyahısına əlavə edir.
    3. Dəyişikliyi `groupRepository.save()` ilə yadda saxlayır.

- **`solveProblem(Long problemId, String userId)`**:
    1. Verilmiş `problemId` ilə problemi tapır.
    2. Problemin `solved` statusunu `true` edir, `solvedByUserId` və `solvedAt` sahələrini təyin edir.
    3. Dəyişikliyi `problemRepository.save()` ilə yadda saxlayır.
    4. **Kafka İnteqrasiyası:** Yeni bir `ProblemSolvedEvent` obyekti yaradır.
    5. `kafkaTemplate.send("problem-solved-topic", event)` metodu ilə bu hadisəni `problem-solved-topic` adlı Kafka topic-inə göndərir. Bu, `GamificationService` kimi digər servislərin bu hadisədən xəbərdar olmasını və müvafiq reaksiya verməsini (məsələn, istifadəçiyə XP verməsini) təmin edir.

Digər metodlar da oxşar şəkildə controller-dən gələn tələbləri emal etmək üçün repozitorilər və mapper-lərdən istifadə edir.

---

## 7. Digər Paketlər

- **`dto`**: Data Transfer Object siniflərini saxlayır. API sərhədlərində məlumat daşımaq üçün istifadə olunur.
- **`mapper`**: `MapStruct` interfeyslərini saxlayır. DTO və Entity sinifləri arasında çevirmələri avtomatlaşdırır.
- **`event`**: Kafka-ya göndəriləcək hadisə (event) siniflərini saxlayır (`ProblemSolvedEvent`).
- **`exception`**: Qlobal xəta idarəedicisi olan `RestExceptionHandler`-ı saxlayır. `@ControllerAdvice` annotasiyası sayəsində tətbiqdə baş verən bütün exception-ları tutur və standart bir formatda JSON cavabı qaytarır.

---
**`CommunityService` üçün Kod İzahının Sonu.**
