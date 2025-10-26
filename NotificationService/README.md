# NotificationService Mikroxidməti

## 1. Giriş və Məqsəd

`NotificationService`, **SkillSwap** platformasının mərkəzləşdirilmiş bildiriş mərkəzidir. Bu mikroxidmətin əsas məqsədi, platformada baş verən müxtəlif hadisələr (məsələn, yeni bacarıq mübadiləsi təklifi, təklifin qəbulu, yeni "badge" qazanılması, görüş xatırlatmaları) haqqında istifadəçiləri fərqli kanallar vasitəsilə məlumatlandırmaqdır.

Xidmət, hadisə-yönümlü arxitektura (Event-Driven Architecture) prinsiplərinə əsaslanır. Digər mikroxidmətlər özlərində baş verən mühüm hadisələri Apache Kafka-ya göndərir və `NotificationService` bu hadisələri dinləyərək müvafiq bildirişləri formalaşdırır və istifadəçilərə çatdırır.

`NotificationService` aşağıdakı əsas kanallar vasitəsilə bildiriş göndərməyi dəstəkləyir:

1.  **Tətbiqdaxili (In-App) Bildirişlər:** İstifadəçilərin platforma daxilində gördüyü, zəng işarəsi ilə göstərilən bildirişlər. Bu bildirişlər MongoDB-də saxlanılır və tarixçə kimi istifadəçiyə göstərilir.
2.  **Real-zamanlı (WebSocket) Bildirişlər:** Tətbiqdaxili bildiriş yarandıqda, istifadəçinin brauzerinə WebSocket vasitəsilə ani olaraq göndərilən "push" tipli bildirişlər. Bu, səhifəni yeniləmədən yeni bildirişin dərhal görünməsini təmin edir.
3.  **E-poçt Bildirişləri:** Yüksək prioritetli hadisələr (məsələn, qeydiyyatın təsdiqi, vacib təkliflər) üçün istifadəçilərin qeydiyyatdan keçdiyi e-poçt ünvanlarına göndərilən bildirişlər.

---

## 2. Texnologiya Stackı

| Texnologiya | Versiya/Növ | Təyinatı və Seçim Səbəbi |
| :--- | :--- | :--- |
| **Java** | 17 | Platformanın əsas proqramlaşdırma dili. |
| **Spring Boot** | 3.x | Tətbiqin sürətli qurulması və konfiqurasiyası üçün əsas freymvork. |
| **Apache Kafka** | - | Mikroxidmətlər arasında asinxron və etibarlı məlumat mübadiləsini təmin edən, hadisə-yönümlü arxitekturanın onurğa sütunu. Yüksək ötürücülük qabiliyyətinə görə seçilib. |
| **Spring for Apache Kafka** | - | Kafka ilə inteqrasiyanı `@KafkaListener` kimi annotasiyalarla asanlaşdırır. |
| **Spring WebSocket** | - | İstifadəçilərə real-zamanlı tətbiqdaxili bildirişləri göndərmək üçün istifadə olunur. |
| **MongoDB** | - | Bildiriş tarixçəsini saxlamaq üçün istifadə olunan NoSQL verilənlər bazası. Elastik sxeması və sürətli yazma əməliyyatları bildiriş datası üçün uyğundur. |
| **Spring Data MongoDB**| - | MongoDB ilə repository pattern əsasında asan inteqrasiyanı təmin edir. |
| **Spring Mail** | - | E-poçt bildirişlərini göndərmək üçün standart Spring modulu. |
| **Spring Cloud OpenFeign**| - | Digər mikroxidmətlərə (məsələn, `SkillUserService`) deklarativ REST zəngləri etmək üçün istifadə olunur. |
| **Docker** | - | `docker-compose.yml` vasitəsilə asılılıqların (Kafka, Zookeeper, MongoDB) təcrid olunmuş mühitdə sürətli qurulmasını təmin edir. |

---

## 3. Arxitektura və Məntiq Axını

### 3.1. Ümumi Bildiriş Axını (Event-Driven)

Arxitektura tamamilə hadisələrə (events) əsaslanır. Proses aşağıdakı kimidir:

1.  **Hadisənin Yaranması:** `AuthService`, `SkillSwapService` və ya digər bir mikroxidmətdə bildiriş tələb edən bir hadisə baş verir (məsələn, istifadəçi qeydiyyatdan keçir).
2.  **Hadisənin Kafka-ya Göndərilməsi:** Həmin servis, hadisə haqqında məlumatları (`NotificationEventDTO` formatında) müvafiq Kafka "topic"-inə (`notifications`) göndərir.
3.  **Hadisənin Dinlənilməsi:** `NotificationService`-dəki `NotificationKafkaListener` bu "topic"-i davamlı olaraq dinləyir. Yeni bir hadisə gəldikdə, onu qəbul edir.
4.  **Hadisənin Emal Edilməsi:** `KafkaListener` qəbul etdiyi hadisəni emal etmək üçün mərkəzi `NotificationProcessor` servisinə ötürür.
5.  **Kanallar Üzrə Paylanma:** `NotificationProcessor`, hadisəni analiz edərək onu fərqli kanallara yönləndirir:
    a.  **Tətbiqdaxili (In-App):** Hadisəni `Notification` obyektinə çevirir və `NotificationRepository` vasitəsilə MongoDB-yə yazır.
    b.  **Real-zamanlı (WebSocket):** MongoDB-yə yazılmış `Notification` obyektini `SimpMessagingTemplate` vasitəsilə müvafiq istifadəçinin şəxsi WebSocket "topic"-inə (`/topic/user/{userId}`) göndərir.
    c.  **E-poçt:** Hadisənin növünün vacibliyini yoxlayır. Əgər vacibdirsə (`SWAP_REQUEST` kimi), `EmailSender` vasitəsilə istifadəçinin e-poçt ünvanına bildiriş göndərir.

![Notification Flow Diagram](https://i.imgur.com/your-flow-diagram.png) <!-- Diaqram linki -->

### 3.2. Real-zamanlı Bildiriş (WebSocket)

Klientlər real-zamanlı bildirişləri aşağıdakı mexanizmlə alır:

1.  **Qoşulma:** Klient (brauzer) `ws://<host>:<port>/ws` endpointinə qoşulur.
2.  **Şəxsi Kanala Abunə Olma:** Qoşulduqdan sonra, klient STOMP vasitəsilə özünə məxsus şəxsi kanala abunə olur. Bu kanalın ünvanı dinamik olaraq istifadəçinin ID-sini ehtiva edir: `/topic/user/{userId}`. Məsələn, ID-si 123 olan istifadəçi `/topic/user/123` ünvanına abunə olur.
3.  **Bildirişin Göndərilməsi:** `NotificationProcessor`-da `sendRealTimeNotification` metodu işə düşdükdə, `SimpMessagingTemplate.convertAndSend()` metodu ilə bildiriş obyektini birbaşa həmin istifadəçinin şəxsi ünvanına göndərir.
4.  **Bildirişin Alınması:** Yalnız həmin kanala abunə olmuş klient (yəni, doğru istifadəçi) mesajı alır və ekranda göstərir. Bu, bildirişlərin səhv istifadəçilərə getməsinin qarşısını alır.

---

## 4. Layihənin Detallı Strukturu

### `kafka` Paketi

- **`NotificationKafkaListener.java`**: `notifications` topic-ini dinləyən əsas listener.
    - `@KafkaListener`: Bu metodun bir Kafka dinləyicisi olduğunu bildirir. `topics`, `groupId` və `containerFactory` kimi parametrlərlə konfiqurasiya olunur. `groupId` sayəsində eyni servisdən bir neçə nüsxə (instance) işləsə belə, bir hadisə yalnız onlardan biri tərəfindən emal edilir.
    - `listen(NotificationEventDTO event)`: Kafka-dan gələn JSON mesajını avtomatik olaraq `NotificationEventDTO` obyektinə çevirir və `NotificationProcessor`-a ötürür. Xəta baş verərsə, loglanır.
- **`MeetingReminderKafkaListener.java`**: `meeting-reminder-events` topic-ini dinləyir. Bu, fərqli bir hadisə növü (`MeetingReminderEvent`) üçün xüsusiləşmiş ayrı bir listenerdir. Bu, fərqli məntiqə sahib hadisələri ayırmağın yaxşı bir nümunəsidir.

### `service` Paketi

- **`NotificationProcessor.java`**: Xidmətin "beyni". Bütün bildiriş emal məntiqi burada yerləşir.
    - `process(NotificationEventDTO event)`: Əsas metod. Gələn hadisəni götürür və üç əsas funksiyanı ardıcıl olaraq çağırır:
        1.  `saveInAppNotification()`: Hadisəni MongoDB-də saxlamaq üçün `Notification` obyektinə çevirir və `repository.save()`-i çağırır.
        2.  `sendRealTimeNotification()`: Saxlanmış bildirişi götürür və `webSocketTemplate` vasitəsilə istifadəçinin şəxsi WebSocket kanalına göndərir.
        3.  `handleExternalChannels()`: Hadisənin xarici kanallara (hazırda e-poçt) göndərilib-göndərilməyəcəyini yoxlayır. Burada biznes məntiqi tətbiq olunur (məsələn, yalnız yüksək prioritetli bildirişləri e-poçt etmək). E-poçt ünvanını DTO-nun `payload` sahəsindən çıxarır.
    - `processMeetingReminder()`: Görüş xatırlatmaları üçün fərqli bir məntiqi həyata keçirir. `UserClient` vasitəsilə hər iki istifadəçinin məlumatlarını alır və hər ikisinə e-poçt göndərir.
    - `extractEmailFromPayload()`: `payload` sahəsindəki JSON string-i `ObjectMapper` ilə parse edərək `email` sahəsini təhlükəsiz şəkildə çıxarmağa çalışır.

- **`EmailSender.java`**: E-poçt göndərmə funksionallığı üçün bir interfeys (abstraksiya). Bu, gələcəkdə `SmtpEmailSenderImpl`-i `SendGridEmailSenderImpl` və ya `SesEmailSenderImpl` ilə asanlıqla əvəz etməyə imkan verir.
- **`SmtpEmailSenderImpl.java`**: `EmailSender` interfeysinin `Spring Mail`-in `JavaMailSender`-i istifadə edən konkret implementasiyası.

### `controller` Paketi

- **`NotificationController.java`**: İstifadəçilərin öz bildiriş tarixçəsini görməsi və idarə etməsi üçün REST API endpointlərini təmin edir.
    - `getForUser()`: `GET /api/notifications/user/{userId}` sorğularını idarə edir. Verilmiş istifadəçi ID-si üçün `NotificationRepository`-dən bütün bildirişləri tarix sırası ilə çəkir və qaytarır.
    - `markRead()`: `POST /{id}/read` sorğusu ilə tək bir bildirişin `read` statusunu `true` olaraq dəyişdirir.

---

## 5. API və Event Sənədləşdirməsi

### 5.1. Kafka Topics & Events

- **Topic: `notifications`**
  - **Məqsəd:** Ümumi bildiriş hadisələrini daşımaq.
  - **Event Formatı (`NotificationEventDTO`):**
    ```json
    {
      "type": "SWAP_REQUEST", // Hadisənin növü (məcburi)
      "userId": 123, // Bildirişi alacaq istifadəçinin ID-si (məcburi)
      "title": "Yeni Bacarıq Təklifi", // Bildiriş başlığı (məcburi)
      "message": "'Java Proqramlaşdırma' bacarığınız üçün yeni bir təklifiniz var.", // Bildiriş mətni (məcburi)
      "payload": "{\"email\":\"user@example.com\",\"swapId\":\"S1234\"}" // Əlavə məlumatlar (opsional)
    }
    ```

- **Topic: `meeting-reminder-events`**
  - **Məqsəd:** Planlaşdırılmış görüşlər üçün xatırlatmaları daşımaq.
  - **Event Formatı (`MeetingReminderEvent`):**
    ```json
    {
      "swapId": 567,
      "user1Id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "user2Id": "f1e2d3c4-b5a6-9870-4321-fedcba098765",
      "meetingDateTime": "2023-11-15T14:00:00"
    }
    ```

### 5.2. REST API

- `GET /api/notifications/user/{userId}`: İstifadəçinin bildiriş tarixçəsini alır.
- `POST /api/notifications/{id}/read`: Bir bildirişi oxunmuş kimi işarələyir.

### 5.3. WebSocket API

- **Qoşulma Endpointi:** `ws://<host>:<port>/ws`.
- **Abunə Olma Ünvanı:** `/topic/user/{userId}` (hər istifadəçi öz ID-si ilə abunə olur).
- **Alınan Mesaj Formatı (`Notification`):**
  ```json
  {
    "id": "60c72b2f9b1d8c1f7c8e4c1a",
    "userId": 123,
    "type": "SWAP_REQUEST",
    "title": "Yeni Bacarıq Təklifi",
    "message": "'Java Proqramlaşdırma' bacarığınız üçün yeni bir təklifiniz var.",
    "payload": "{\"swapId\":\"S1234\"}",
    "read": false,
    "createdAt": "2023-10-27T12:00:00Z"
  }
  ```

---

## 6. Quraşdırma və İşə Salma

1.  **Ön Tələblər:**
    - Java 17+
    - Docker və Docker Compose

2.  **Konfiqurasiya (`application.yml`):**
    - `spring.data.mongodb.uri`: MongoDB bağlantı ünvanı.
    - `spring.kafka.bootstrap-servers`: Kafka brokerlərinin ünvanı.
    - `spring.mail.*`: SMTP serverinizin parametrləri.
    - `skill-user-service.url`: `SkillUserService`-in tam ünvanı.

3.  **Asılılıqların İşə Salınması (Docker ilə):**
    - Layihənin kökündəki `docker-compose.yml` faylı Kafka, Zookeeper və MongoDB xidmətlərini sizin üçün qurur.
    - Terminalda aşağıdakı əmri icra edin:
      ```bash
      docker-compose up -d
      ```

4.  **Tətbiqin Qurulması və İşə Salınması:**
    - Layihəni qurmaq üçün:
      ```bash
      ./gradlew build
      ```
    - Tətbiqi işə salmaq üçün:
      ```bash
      java -jar build/libs/notificationservice-0.0.1-SNAPSHOT.jar
      ```

---

## 7. Genişləndirmə və Gələcək Təkmilləşdirmələr

- **Yeni Kanallar:** SMS və ya Mobil Push Notification (FCM/APNS) kimi yeni kanallar əlavə etmək üçün `NotificationProcessor`-da yeni metodlar və `EmailSender` kimi yeni interfeyslər (`SmsSender`, `PushNotifier`) yaradıla bilər.
- **İstifadəçi Tənzimləmələri:** `UserClient` vasitəsilə `SkillUserService`-dən istifadəçinin bildiriş tənzimləmələrini (məsələn, "e-poçt bildirişlərini söndür") almaq və `handleExternalChannels` metodunda bu tənzimləmələrə əməl etmək olar.
- **DLQ (Dead Letter Queue):** Kafka hadisəsi emal edilərkən xəta baş verərsə (məsələn, JSON formatı səhvdirsə), bu hadisənin itməməsi üçün onu avtomatik olaraq bir "ölü məktub" topic-inə yönləndirən bir mexanizm qurulmalıdır. Bu, sonradan bu xətaları araşdırıb həll etməyə imkan verir.
