
# `NotificationService` Mikroxidmətinin Dərinlemesine Kodu İzahı

Bu sənəd, `NotificationService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, hadisə-yönümlü arxitektura (event-driven architecture) əsasında qurulmuş bildiriş sisteminin işləmə prinsipini, Kafka ilə inteqrasiyanı, WebSocket vasitəsilə real-zamanlı bildirişlərin çatdırılmasını və fərqli kanallar (in-app, e-poçt) üzrə bildirişlərin paylanmasını tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `NotificationServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/notificationservice/NotificationServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir.

```java
package com.example.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
```

### Sətir-Sətir İzahı

- **`@SpringBootApplication`**: Tətbiqin bir Spring Boot tətbiqi olduğunu bildirən əsas konfiqurasiya annotasiyası.
- **`@EnableDiscoveryClient`**: Bu tətbiqin Eureka kimi bir "Discovery Service"-ə özünü qeydiyyatdan keçirməsini təmin edir.
- **`@EnableFeignClients`**: Tətbiqdə `Feign Client` interfeyslərinin (`@FeignClient` ilə işarələnmiş) axtarılıb tapılmasını və onlar üçün implementasiyaların yaradılmasını aktivləşdirir. Bu, `UserClient` interfeysinin işləməsi üçün vacibdir.
- **`main` metodu**: Tətbiqi işə salan standart Java giriş nöqtəsi.

---

## 2. `client` Paketi

Bu paket, digər mikroxidmətlərlə əlaqə qurmaq üçün istifadə olunan `OpenFeign` klient interfeyslərini saxlayır.

### 2.1. `UserClient.java`

**Fayl Yolu:** `src/main/java/com/example/notificationservice/client/UserClient.java`

Bu interfeys, `SkillUserService`-dən istifadəçi məlumatlarını (xüsusilə e-poçt ünvanını) əldə etmək üçün istifadə olunur.

```java
package com.example.notificationservice.client;

import com.example.notificationservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "skill-user-service", path = "/api/users")
public interface UserClient {

    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId);
}
```

#### Sətir-Sətir İzahı

- **`@FeignClient(name = "skill-user-service", path = "/api/users")`**: Bu interfeysin bir Feign klienti olduğunu bildirir.
    - **`name = "skill-user-service"`**: Bu klientin məntiqi adıdır və Discovery Service (Eureka) vasitəsilə `skill-user-service` adlı xidmətin ünvanını tapmaq üçün istifadə olunur.
    - **`path = "/api/users"`**: Bu klientdəki bütün sorğuların URL-nin əvvəlinə `/api/users` prefiksinin əlavə olunacağını bildirir.
- **`@GetMapping("/{userId}")`**: Bu metod çağırıldıqda, Feign-in `skill-user-service`-in `/api/users/{userId}` endpointinə bir HTTP `GET` sorğusu göndərəcəyini bildirir.
- **`UserDTO getUserById(@PathVariable("userId") UUID userId);`**: Metodun imzası. Feign, bu metodu çağırdıqda arxa planda bütün HTTP sorğu məntiqini özü həyata keçirir və gələn JSON cavabını `UserDTO` obyektinə çevirir.

---

## 3. `config` Paketi

Bu paket, WebSocket konfiqurasiyasını saxlayır.

### 3.1. `WebSocketConfig.java`

**Fayl Yolu:** `src/main/java/com/example/notificationservice/config/WebSocketConfig.java`

Bu sinif, real-zamanlı bildirişlərin klientlərə çatdırılması üçün WebSocket və STOMP-u konfiqurasiya edir.

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

#### Sətir-Sətir İzahı

- **`@EnableWebSocketMessageBroker`**: WebSocket mesajlaşmasını aktivləşdirir.
- **`configureMessageBroker(MessageBrokerRegistry config)` metodu**:
    - **`config.enableSimpleBroker("/topic");`**: Yaddaşda işləyən sadə bir mesaj brokerini aktivləşdirir. Bu broker, `/topic` prefiksi ilə başlayan ünvanlara göndərilən mesajları abunəçilərə yayımlayır. `NotificationService`-də bu, bildirişləri istifadəçilərin şəxsi kanallarına (`/topic/user/{userId}`) göndərmək üçün istifadə olunur.
    - **`config.setApplicationDestinationPrefixes("/app");`**: Klientdən serverə göndərilən mesajların ünvan prefiksini təyin edir. Bu xidmətdə klientlər serverə mesaj göndərmədiyi üçün bu sətir əslində çox istifadə olunmur, lakin standart konfiqurasiyanın bir hissəsidir.
- **`registerStompEndpoints(StompEndpointRegistry registry)` metodu**:
    - **`registry.addEndpoint("/ws")`**: Klientlərin WebSocket əlaqəsi qurmaq üçün istifadə edəcəyi `/ws` endpointini qeydiyyatdan keçirir.
    - **`.setAllowedOriginPatterns("*")`**: Bütün domenlərdən gələn qoşulma sorğularına icazə verir (CORS).
    - **`.withSockJS()`**: Köhnə brauzerlər üçün SockJS dəstəyini aktivləşdirir.

---

## 4. `controller` Paketi

Bu paket, tətbiqdaxili bildiriş tarixçəsini idarə etmək üçün REST API endpointlərini təmin edir.

### 4.1. `NotificationController.java`

**Fayl Yolu:** `src/main/java/com/example/notificationservice/controller/NotificationController.java`

```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repository;
    // ... konstruktor

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(repository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable String id) {
        Optional<Notification> opt = repository.findById(id);
        if (opt.isPresent()) {
            Notification n = opt.get();
            n.setRead(true);
            repository.save(n);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

#### Sətir-Sətir İzahı

- **`@RestController`, `@RequestMapping`**: Standart REST controller annotasiyaları.
- **`getForUser(@PathVariable Long userId)` metodu**:
    - **`@GetMapping("/user/{userId}")`**: `/api/notifications/user/{userId}` ünvanına edilən `GET` sorğularını idarə edir.
    - **Məntiq:** Bu metod, verilmiş `userId` üçün bütün bildirişləri `NotificationRepository` vasitəsilə verilənlər bazasından çəkir. `findByUserIdOrderByCreatedAtDesc` metodu sayəsində nəticələr avtomatik olaraq yaradılma tarixinə görə ən sondan ən əvvələ doğru çeşidlənir. Nəticə `ResponseEntity.ok()` ilə `200 OK` statusu ilə qaytarılır.
    - **Təhlükəsizlik Qeydi:** Şərhdə də qeyd edildiyi kimi, real sistemdə `userId` birbaşa URL-dən deyil, təhlükəsizlik kontekstindən (məsələn, JWT tokenindən) alınmalıdır ki, bir istifadəçi başqasının bildirişlərini görə bilməsin.
- **`markRead(@PathVariable String id)` metodu**:
    - **`@PostMapping("/{id}/read")`**: `/api/notifications/{id}/read` ünvanına edilən `POST` sorğularını idarə edir.
    - **Məntiq:**
        1. `repository.findById(id)` ilə verilən ID-yə malik bildirişi axtarır.
        2. `if (opt.isPresent())`: Əgər bildiriş tapılıbsa, onun `read` statusunu `true` olaraq dəyişdirir və `repository.save(n)` ilə yenilənmiş obyekti verilənlər bazasına yazır. Uğurlu olduqda `200 OK` statusu qaytarılır.
        3. Əgər bildiriş tapılmazsa, `ResponseEntity.notFound()` ilə `404 Not Found` statusu qaytarılır.

---

## 5. `kafka` Paketi

Bu paket, digər mikroxidmətlərdən gələn hadisələri dinləyən Kafka listener siniflərini saxlayır.

### 5.1. `NotificationKafkaListener.java` və `MeetingReminderKafkaListener.java`

Bu iki sinif funksional olaraq çox oxşardır, sadəcə fərqli "topic"-ləri dinləyir və fərqli DTO-ları qəbul edirlər.

```java
@Component
public class NotificationKafkaListener {
    // ...
    private final NotificationProcessor processor;

    @KafkaListener(topics = "notifications", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(NotificationEventDTO event) {
        log.info("Received Kafka notification event: type={} userId={}", event.getType(), event.getUserId());
        try {
            processor.process(event);
        } catch (Exception e) {
            log.error("Error processing notification event: ...", e);
        }
    }
}
```

#### Sətir-Sətir İzahı

- **`@Component`**: Bu sinifin bir Spring bean-i olduğunu bildirir.
- **`private final NotificationProcessor processor;`**: Qəbul edilən hadisəni emal etmək üçün istifadə olunacaq əsas servis.
- **`@KafkaListener(...)`**: Bu metodu bir Kafka mesaj dinləyicisi kimi qeydiyyatdan keçirir.
    - **`topics = "notifications"`**: Bu dinləyicinin hansı topic-dən mesajları qəbul edəcəyini göstərir.
    - **`groupId = "notification-service-group"`**: Bu dinləyicinin aid olduğu "consumer group"-u təyin edir. Eyni qrup ID-sinə malik bir neçə servis nüsxəsi işləyirsə, Kafka bir mesajın həmin qrupdakı yalnız bir nüsxə tərəfindən emal ediləcəyinə zəmanət verir. Bu, mesajların təkrarlanmasının qarşısını alır.
    - **`containerFactory = "kafkaListenerContainerFactory"`**: Mesajları qəbul edən "listener container"-ı yaratmaq üçün hansı fabrikin istifadə ediləcəyini göstərir. Bu, adətən JSON deserializasiyası kimi xüsusi konfiqurasiyalar təyin etmək üçün istifadə olunur.
- **`public void listen(NotificationEventDTO event)`**: Metodun imzası. Spring Kafka, topic-dən gələn JSON mesajını avtomatik olaraq `NotificationEventDTO` obyektinə çevirməyə (deserialize) cəhd edəcək.
- **`processor.process(event);`**: Mesaj uğurla qəbul edildikdən və çevrildikdən sonra, onun emal edilməsi üçün `NotificationProcessor` servisinə ötürülür.
- **`try-catch` bloku**: Mesajın emalı zamanı hər hansı bir xəta baş verərsə, bu xəta tutulur və loglanır. Bu, bir mesajdakı xətanın bütün tətbiqi dayandırmasının qarşısını alır. Production mühitində, uğursuz olmuş mesajlar adətən sonradan analiz üçün bir "Dead Letter Queue" (DLQ) topic-inə göndərilir.

---

## 6. `service` Paketi

Bu paket, bildirişlərin emalı və göndərilməsi ilə bağlı bütün biznes məntiqini saxlayır.

### 6.1. `NotificationProcessor.java`

**Fayl Yolu:** `src/main/java/com/example/notificationservice/service/NotificationProcessor.java`

Bu, Kafka-dan gələn hadisələri qəbul edib onları müvafiq kanallara (in-app, websocket, email) paylayan mərkəzi servisdir.

```java
@Service
public class NotificationProcessor {

    // ... asılılıqlar

    @Transactional
    public void process(NotificationEventDTO event) {
        Notification savedNotification = saveInAppNotification(event);
        sendRealTimeNotification(savedNotification);
        handleExternalChannels(event);
    }

    private Notification saveInAppNotification(NotificationEventDTO event) { ... }

    private void sendRealTimeNotification(Notification notification) { ... }

    private void handleExternalChannels(NotificationEventDTO event) { ... }

    private String extractEmailFromPayload(String payload) { ... }
}
```

#### Metodların Detallı İzahı

- **`process(NotificationEventDTO event)` metodu**:
    - **`@Transactional`**: Bu metodun bir tranzaksiya daxilində işləməsini təmin edir. Bu, xüsusilə `saveInAppNotification` metodunun uğurlu olmasını təmin etmək üçün vacibdir.
    - **Məntiq:** Bu metod bir orkestrator rolunu oynayır və işi üç əsas mərhələyə bölür:
        1.  **`Notification savedNotification = saveInAppNotification(event);`**: Gələn hadisəni dərhal tətbiqdaxili bildiriş kimi MongoDB-yə yazır. Bu, bildirişin tarixçədə saxlanmasını təmin edir.
        2.  **`sendRealTimeNotification(savedNotification);`**: Saxlanmış bildirişi götürür və WebSocket vasitəsilə istifadəçinin brauzerinə real-zamanlı olaraq göndərir.
        3.  **`handleExternalChannels(event);`**: Hadisəni e-poçt kimi xarici kanallara göndərmək üçün əlavə məntiqi işə salır.

- **`saveInAppNotification(NotificationEventDTO event)` metodu**:
    - Gələn `NotificationEventDTO`-dan məlumatları götürərək yeni bir `Notification` entity-si yaradır.
    - `read` statusunu default olaraq `false`, `createdAt` sahəsini isə hazırkı vaxt olaraq təyin edir.
    - `repository.save(n)` ilə obyekti MongoDB-yə yazır və saxlanmış obyekti geri qaytarır.

- **`sendRealTimeNotification(Notification notification)` metodu**:
    - **`String destination = "/topic/user/" + notification.getUserId();`**: Mesajın göndəriləcəyi şəxsi WebSocket ünvanını (destination) yaradır. Hər istifadəçinin özünəməxsus bir kanalı olur.
    - **`webSocketTemplate.convertAndSend(destination, notification);`**: `SimpMessagingTemplate` vasitəsilə bildiriş obyektini JSON formatına çevirərək həmin ünvana göndərir. Yalnız bu ünvana abunə olmuş klient (yəni, həmin `userId`-yə malik istifadəçi) bu mesajı alacaq.

- **`handleExternalChannels(NotificationEventDTO event)` metodu**:
    - **`String userEmail = extractEmailFromPayload(event.getPayload());`**: Hadisənin `payload` sahəsindən (JSON string) istifadəçinin e-poçt ünvanını çıxarmağa cəhd edir.
    - **`if (userEmail != null && ...)`**: Əgər e-poçt ünvanı tapılıbsa və hadisənin növü yüksək prioritetli (məsələn, `USER_REGISTERED` və ya `SWAP_REQUEST`) olaraq təyin edilmiş siyahıdadırsa, `emailSender.sendEmail()` metodu çağırılır.
    - Bu məntiq, hər kiçik hadisə üçün istifadəçiyə e-poçt göndərilərək onun "spam" edilməsinin qarşısını alır.

### 6.2. `EmailSender` və `SmtpEmailSenderImpl.java`

- **`EmailSender.java` (İnterfeys)**: E-poçt göndərmə funksionallığı üçün bir müqavilə (contract) təyin edir. Bu, abstraksiya yaradır və gələcəkdə e-poçt göndərmə provayderini (məsələn, `JavaMail`-dan `SendGrid`-ə) dəyişməyi asanlaşdırır. Yeni provayder üçün sadəcə bu interfeysi implementasiya edən yeni bir sinif yazmaq kifayət edəcək.
- **`SmtpEmailSenderImpl.java` (Implementasiya)**: `EmailSender` interfeysinin `Spring Mail`-in `JavaMailSender`-i istifadə edən konkret implementasiyasıdır. `EmailService`-də izah edildiyi kimi, `MimeMessageHelper` vasitəsilə HTML formatlı e-poçtlar hazırlayıb göndərir.

---

**`NotificationService` üçün Kod İzahının Sonu.**
