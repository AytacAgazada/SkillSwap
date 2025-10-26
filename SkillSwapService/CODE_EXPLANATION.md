
# `SkillSwapService` Mikroxidmətinin Dərinlemesine Kodu İzahı

Bu sənəd, `SkillSwapService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, platformanın əsas biznes məntiqini - bacarıq mübadiləsi təkliflərinin yaradılması, coğrafi axtarışı, uyğunlaşdırılması və həyat dövrünün idarə olunmasını həyata keçirən bu mürəkkəb xidmətin arxitekturasını tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `SkillSwapServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/skillswapservice/SkillSwapServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir.

```java
package com.example.skillswapservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SkillSwapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSwapServiceApplication.class, args);
    }

}
```

### Sətir-Sətir İzahı

- **`@SpringBootApplication`**: Tətbiqin bir Spring Boot tətbiqi olduğunu bildirir.
- **`@EnableDiscoveryClient`**: Bu tətbiqin Eureka kimi bir "Discovery Service"-ə özünü qeydiyyatdan keçirməsini təmin edir.
- **`@EnableFeignClients`**: Tətbiqdə `Feign Client` interfeyslərinin axtarılıb tapılmasını və onlar üçün implementasiyaların yaradılmasını aktivləşdirir.
- **`main` metodu**: Tətbiqi işə salan standart Java giriş nöqtəsi.

---

## 2. `config` Paketi

Bu paket, Kafka və Spring Security üçün konfiqurasiya siniflərini saxlayır.

### 2.1. `KafkaProducerConfig.java`

**Fayl Yolu:** `src/main/java/com/example/skillswapservice/config/KafkaProducerConfig.java`

Bu sinif, tətbiqdən Kafka-ya mesaj göndərmək üçün istifadə olunan `KafkaTemplate` bean-ini konfiqurasiya edir.

```java
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### Sətir-Sətir İzahı

- **`producerConfigs()` metodu**: Kafka producer-i üçün əsas xüsusiyyətləri (properties) təyin edir.
    - `BOOTSTRAP_SERVERS_CONFIG`: Kafka brokerlərinin ünvanını `application.yml`-dən götürür.
    - `KEY_SERIALIZER_CLASS_CONFIG`: Mesajın açarının (`key`) `String`-dən `byte[]`-a necə çevriləcəyini (serialize) göstərir.
    - `VALUE_SERIALIZER_CLASS_CONFIG`: Mesajın dəyərinin (`value`), yəni bizim hadisə DTO-larımızın, `JSON` formatına necə çevriləcəyini göstərir. `JsonSerializer` Spring Kafka-nın təqdim etdiyi, obyektləri JSON-a çevirən bir sinifdir.
- **`producerFactory()` metodu**: Yuxarıdakı konfiqurasiyaları istifadə edərək `Producer` nüsxələri yaradan bir fabrik (factory) bean-i yaradır.
- **`kafkaTemplate()` metodu**: Mesaj göndərmə əməliyyatlarını sadələşdirən `KafkaTemplate` bean-ini yaradır. Bu, `SwapEventPublisher`-də istifadə olunan əsas komponentdir.

### 2.2. `SecurityConfig.java`

**Fayl Yolu:** `src/main/java/com/example/skillswapservice/config/SecurityConfig.java`

Bu sinif, xidmətin API endpointlərini qorumaq üçün Spring Security qaydalarını təyin edir.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/swaps/offers/search").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
```

#### Sətir-Sətir İzahı

- **`sessionManagement(...)`**: Sessiya idarəetməsini `STATELESS` olaraq təyin edir. Bu, serverin heç bir sessiya məlumatı saxlamayacağını və hər bir sorğunun özündə autentifikasiya məlumatını (adətən API Gateway-dən gələn başlıqlar vasitəsilə) daşımalı olduğunu bildirir.
- **`csrf(AbstractHttpConfigurer::disable)`**: Stateless API-lərdə adətən lazımsız olan CSRF qorumasını deaktiv edir.
- **`authorizeHttpRequests(...)`**: Endpointlərə giriş qaydalarını təyin edir.
    - `.requestMatchers("/swagger-ui/**", ...).permitAll()`: Swagger UI və Actuator endpointlərinə sərbəst girişi təmin edir.
    - `.requestMatchers(HttpMethod.GET, "/v1/swaps/offers/search").permitAll()`: **Vacib!** Təkliflərin axtarışı endpointini (`GET /v1/swaps/offers/search`) hər kəsə (autentifikasiya olunmamış istifadəçilərə də) açıq edir. Bu, platformaya daxil olmayanların da təkliflərə baxa bilməsi üçün nəzərdə tutulub.
    - `.anyRequest().authenticated()`: Yuxarıdakı istisnalar xaricində, qalan **bütün** digər sorğuların autentifikasiya tələb etdiyini bildirir. Bu, təhlükəsizlik üçün "default-deny" prinsipini tətbiq edir.

---

## 3. `controller` Paketi

### 3.1. `SwapController.java`

**Fayl Yolu:** `src/main/java/com/example/skillswapservice/controller/SwapController.java`

Bu controller, bacarıq mübadiləsi prosesinin bütün mərhələlərini idarə edən REST API endpointlərini təmin edir.

```java
@RestController
@RequestMapping("/api/skil-swaps")
@RequiredArgsConstructor
public class SwapController {

    private final SwapService swapService;

    @PostMapping("/offers")
    public ResponseEntity<SwapOfferDTO> createOffer(
                                                     @RequestHeader("X-Auth-User-Id") UUID userId,
                                                     @Valid @RequestBody CreateSwapOfferRequest request) { ... }
    // ... digər metodlar
}
```

#### Sətir-Sətir İzahı (Seçilmiş Metodlar)

- **`createOffer(...)` metodu**:
    - **`@RequestHeader("X-Auth-User-Id") UUID userId`**: Bu parametr, sorğunun başlığından `X-Auth-User-Id` dəyərini oxuyur və onu `UUID` tipinə çevirir. Bu, təklifi hansı istifadəçinin yaratdığını müəyyən edir. Bu başlığın API Gateway tərəfindən təhlükəsiz şəkildə əlavə edildiyi fərz edilir.
    - **`@Valid @RequestBody CreateSwapOfferRequest request`**: Sorğunun body-sindəki JSON-u `CreateSwapOfferRequest` DTO-suna çevirir və onun üzərindəki validasiya annotasiyalarını işə salır.
    - **Məntiq:** `SwapService`-in `createOffer` metodunu çağırır və nəticəni `201 CREATED` statusu ilə qaytarır.

- **`searchOffers(...)` metodu**:
    - **`@RequestParam String skill, ...`**: Bu parametrlər URL-in sorğu hissəsindən (query string) oxunur. Məsələn: `/search?skill=Java&lat=40.7128&lon=-74.0060&radiusKm=25`.
    - **`@RequestParam(defaultValue = "10")`**: Əgər `radiusKm` parametri sorğuda göndərilməzsə, onun default dəyərinin `10` olacağını bildirir.
    - **Məntiq:** `SwapService`-in `searchOffers` metodunu çağıraraq Elasticsearch üzərindən coğrafi axtarışı həyata keçirir və nəticələri `200 OK` statusu ilə qaytarır.

- **`requestMatch(...)` metodu**:
    - **`@PathVariable Long offerId`**: URL-in özündən (məsələn, `/match/123`) təklifin ID-sini oxuyur.
    - **Məntiq:** `SwapService`-in `sendMatchRequest` metodunu çağırır. Bu metod, təklif sahibinə bildiriş göndərilməsi üçün Kafka-ya bir hadisə yayımlayır.

---

## 4. `entity` Paketi

Bu xidmət, fərqli məqsədlər üçün fərqli verilənlər bazalarından istifadə etdiyi üçün iki fərqli növ entity saxlayır: JPA (PostgreSQL üçün) və Document (Elasticsearch üçün).

### 4.1. `SwapOfferEntity.java` (JPA)

- **Məqsədi:** Təklifin əsas və tam məlumatlarını saxlayan, PostgreSQL-dəki `swap_offer_entity` cədvəlinə uyğun gələn entity. Bu, sistemin "Source of Truth"-udur.
- **Sahələr:** `id`, `userId`, `skillOffered`, `skillRequested`, `meetingType`, `description`, `isActive`, `latitude`, `longitude`, `createdAt`.

### 4.2. `GeoSwapOffer.java` (Elasticsearch Document)

- **`@Document(indexName = "swap_offers")`**: Bu sinifin Elasticsearch-də `swap_offers` adlı bir indeksə yazılacağını bildirir.
- **Məqsədi:** `SwapOfferEntity`-nin yalnız axtarış üçün lazım olan sahələrini saxlayan və axtarış üçün optimallaşdırılmış bir sənəddir.
- **`@Field(type = FieldType.Keyword)`**: Bu sahənin Elasticsearch-də `keyword` tipi ilə indekslənməsini təmin edir. Bu, tam dəyər üzrə axtarış və aqreqasiyalar üçün idealdır.
- **`@Field(type = FieldType.Object) private GeoPoint location;`**: **Ən vacib sahə.** `latitude` və `longitude` dəyərlərini Elasticsearch-in coğrafi sorğuları başa düşməsi üçün xüsusi `GeoPoint` tipində saxlayır. `MatchingService`-dəki `within` filteri məhz bu sahə üzərində işləyir.

### 4.3. `Swap.java` (JPA)

- **Məqsədi:** İki istifadəçi arasında razılaşdırılmış və davam edən mübadilə prosesini təmsil edir. Bir təklifə tələb göndərildikdən və qəbul edildikdən sonra bu cədvəldə yeni bir qeyd yaranır.
- **Sahələr:** `id`, `user1Id` (təklifi edən), `user2Id` (tələbi göndərən), `status` (prosesin hazırkı vəziyyəti - `REQUESTED`, `ACCEPTED`, `COMPLETED` və s.), `swapOfferId` (hansı təklifə aid olduğu), `meetingDateTime` (görüş vaxtı).

---

## 5. `repository` Paketi

- **`SwapOfferRepository` (JPA)**: `SwapOfferEntity` üçün standart CRUD əməliyyatlarını təmin edir.
- **`SwapRepository` (JPA)**: `Swap` entity-si üçün əməliyyatlar təmin edir. `@Modifying` və `@Query` annotasiyaları ilə statusu və görüş vaxtını yeniləmək üçün xüsusi metodları var.
- **`GeoSwapOfferRepository` (Elasticsearch)**: `ElasticsearchRepository`-ni genişləndirir. Bu, `GeoSwapOffer` sənədləri üçün Elasticsearch-ə məxsus sorğuları (məsələn, `findBySkillRequestedAndIsActiveTrue`) və standart CRUD əməliyyatlarını təmin edir.

---

## 6. `service` Paketi

Bu paket, xidmətin əsas biznes məntiqini və orkestrasiyasını həyata keçirir.

### 6.1. `SwapService.java`

- **`createOffer(CreateSwapOfferRequest request, UUID userId)` metodu**:
    1. DTO-nu JPA entity-sinə çevirir.
    2. `swapOfferRepository.save()` ilə PostgreSQL-ə yazır.
    3. `matchingService.indexOffer()` metodunu çağıraraq, saxlanmış entity-ni Elasticsearch-də indeksləyir.
    4. Nəticəni DTO olaraq qaytarır.
- **`searchOffers(String skill, double lat, double lon, double radiusKm)` metodu**:
    1. `matchingService.findMatches()` metodunu çağıraraq Elasticsearch-dən uyğun təkliflərin ID-lərini alır.
    2. `swapOfferRepository.findAllById()` ilə həmin ID-lərə uyğun tam məlumatları PostgreSQL-dən yükləyir.
    3. Nəticələri DTO siyahısına çevirib qaytarır.
- **`sendMatchRequest(Long offerId, UUID requestingUserId)` metodu**:
    1. Təklifin mövcudluğunu və istifadəçinin öz təklifinə tələb göndərmədiyini yoxlayır.
    2. `eventPublisher.publishMatchRequestedEvent()` metodunu çağıraraq `NotificationService`-in dinlədiyi Kafka topic-inə hadisə göndərir.
- **`completeSwap(...)` metodu**:
    1. `swapRepository.updateStatus()` ilə mübadilənin statusunu `COMPLETED`-ə dəyişir.
    2. `eventPublisher.publishSwapCompletedEvent()` ilə Kafka-ya hadisə göndərir ki, digər servislər (məsələn, `BadgeService`) istifadəçilərə mükafatlar verə bilsin.

### 6.2. `MatchingService.java`

- **Məqsədi:** Elasticsearch ilə bütün qarşılıqlı əlaqəni bir yerdə cəmləşdirir.
- **`indexOffer(SwapOfferEntity offer)` metodu**: JPA entity-sini Elasticsearch sənədinə çevirib indeksləyir.
- **`findMatches(...)` metodu**: Spring Data Elasticsearch-in `CriteriaQuery` API-sindən istifadə edərək, verilən bacarıq, mərkəz nöqtəsi və radiusa əsasən mürəkkəb bir coğrafi axtarış sorğusu qurur və icra edir.

### 6.3. `SwapEventPublisher.java`

- **Məqsədi:** Kafka-ya hadisə göndərmək məntiqini mərkəzləşdirir.
- **Metodlar:** Hər bir hadisə növü (`SwapCompletedEvent`, `MeetingReminderEvent`, `MatchRequestedEvent`) üçün ayrıca bir `publish...` metodu var. Bu metodlar, müvafiq hadisə DTO-sunu yaradır və `KafkaTemplate` vasitəsilə onu lazımi topic-ə göndərir.

### 6.4. `MeetingReminderTask.java`

- **`@Scheduled(cron = "0 0 5 * * *")`**: Bu metodun hər gün səhər saat 5:00-da avtomatik olaraq işə düşməsini təmin edir.
- **`sendMeetingReminders()` metodu**:
    1. Hazırkı tarixdən bir gün sonrakı tarixi (`tomorrow`) hesablayır.
    2. `swapRepository.findSwapsForTomorrow()` ilə görüş vaxtı sabaha təyin edilmiş bütün `ACCEPTED` statuslu `Swap`-ları tapır.
    3. Hər bir tapılmış `Swap` üçün `eventPublisher.publishMeetingReminderEvent()` metodunu çağıraraq Kafka-ya xatırlatma hadisəsi göndərir.

---

**`SkillSwapService` üçün Kod İzahının Sonu.**
