# ChatService Mikroxidməti

## 1. Giriş və Məqsəd

`ChatService`, **SkillSwap** platformasının real-zamanlı mesajlaşma funksionallığını təmin edən əsas mikroxidmətidir. Bu xidmət, istifadəçilər arasında bacarıq mübadiləsi (`swap`) razılaşdırıldıqdan sonra onların bir-biri ilə təhlükəsiz və ani olaraq əlaqə saxlamasına imkan yaradır. Xidmətin əsas məqsədi, WebSocket texnologiyasından istifadə edərək dayanıqlı, genişlənə bilən və yüksək performanslı bir çat sistemi qurmaqdır.

`ChatService` aşağıdakı əsas vəzifələri yerinə yetirir:

- **Real-zamanlı Mesajlaşma:** İstifadəçilər arasında WebSocket üzərindən STOMP protokolu ilə ani mesaj mübadiləsini təmin edir.
- **Çat Tarixçəsi:** Keçmiş mesajların saxlanması və istifadəçilərə REST API vasitəsilə təqdim edilməsini həyata keçirir.
- **İstifadəçi Məlumatlarının Zənginləşdirilməsi:** Mesajlarda sadəcə istifadəçi ID-lərini deyil, həm də ad/soyad kimi məlumatları göstərmək üçün digər mikroxidmətlərlə (`SkillUserService`) inteqrasiya edir.
- **Çat Otaqları:** Hər bir unikal `swapId` üçün virtual bir çat otağı yaradır və mesajları bu otaqlar üzrə qruplaşdırır.

Bu mikroxidmət, müasir proqramlaşdırma prinsipləri və texnologiyaları (reaktiv proqramlaşdırma elementləri, message broker inteqrasiyası, NoSQL verilənlər bazası) əsasında qurulmuşdur.

---

## 2. Texnologiya Stackı

`ChatService`-in qurulmasında aşağıdakı texnologiyalardan istifadə olunmuşdur:

| Texnologiya | Versiya/Növ | Təyinatı və Seçim Səbəbi |
| :--- | :--- | :--- |
| **Java** | 17 | Platformanın əsas proqramlaşdırma dili. Uzunmüddətli dəstək (LTS) və stabil performansa görə seçilib. |
| **Spring Boot** | 3.x | Tətbiqin sürətli qurulması, konfiqurasiyası və işə salınması üçün istifadə olunan əsas freymvork. |
| **Spring WebSocket** | - | WebSocket əsaslı real-zamanlı kommunikasiyanı təmin etmək üçün Spring-in rəsmi modulu. |
| **STOMP** | - | WebSocket üzərindən işləyən sadə bir mesajlaşma protokolu. "Destination", "subscribe" kimi konseptləri ilə message broker-lərə bənzər bir arxitektura qurmağı asanlaşdırır. |
| **MongoDB** | - | Yüksək həcmdəki çat mesajlarını effektiv şəkildə saxlamaq üçün seçilmiş NoSQL, sənəd-yönümlü verilənlər bazası. Elastik sxeması və yüksək yazma sürəti çat sistemləri üçün idealdır. |
| **Spring Data MongoDB**| - | MongoDB ilə inteqrasiyanı asanlaşdıran, repository və template pattern-lərini təmin edən Spring modulu. |
| **Spring Cloud OpenFeign**| - | Digər mikroxidmətlərə (məsələn, `SkillUserService`) deklarativ REST API zəngləri etmək üçün istifadə olunur. Kodu daha oxunaqlı və idarəolunan edir. |
| **Lombok** | - | Boilerplate kodu (getters, setters, constructors, etc.) avtomatik generasiya edərək sinifləri daha səliqəli saxlamaq üçün istifadə olunur. |
| **Docker** | - | `docker-compose.yml` vasitəsilə asılılıqların (MongoDB) təcrid olunmuş mühitdə asanlıqla işə salınmasını təmin edir. |

---

## 3. Arxitektura və Məntiq Axını

### 3.1. WebSocket Əlaqəsinin Qurulması

Klient (brauzer) `ChatService` ilə real-zamanlı əlaqəni aşağıdakı addımlarla qurur:

1.  **HTTP Handshake:** Klient ilk olaraq `http://<host>:<port>/ws` endpointinə bir HTTP sorğusu göndərir. Bu sorğu WebSocket əlaqəsinə "upgrade" olmaq istəyini bildirir.
2.  **STOMP Endpoint:** Spring Boot bu sorğunu qəbul edir və `WebSocketConfig`-də təyin edilmiş `registerStompEndpoints` metodu sayəsində `/ws` endpointini tanıyır.
3.  **SockJS Fallback:** Əgər klientin brauzeri standart WebSocket-i dəstəkləmirsə, `withSockJS()` sayəsində əlaqə avtomatik olaraq HTTP long-polling kimi alternativ metodlara keçir. Bu, köhnə brauzerlərlə uyğunluğu təmin edir.
4.  **Interceptor-un İşə Düşməsi:** Əlaqə qurulan zaman `HttpHeaderChannelInterceptor` işə düşür. Bu interceptor, klientin qoşulma sorğusunun başlıqlarından (`nativeHeaders`) `X-Auth-User-Id` başlığını oxuyur. Bu başlıq, adətən API Gateway tərəfindən autentifikasiya olunmuş istifadəçinin ID-sini əlavə edir.
5.  **Sessiya Atributunun Saxlanması:** Interceptor oxuduğu `X-Auth-User-Id`-ni WebSocket sessiyasının atributlarında saxlayır. Bu, həmin istifadəçidən gələn sonrakı bütün WebSocket mesajlarında onun kimliyini bilməyə imkan verir. Beləliklə, hər mesajda yenidən autentifikasiyaya ehtiyac qalmır.

![WebSocket Connection Flow](https://i.imgur.com/your-diagram-link.png) <!-- Diaqram üçün link əlavə edin -->

### 3.2. Mesaj Göndərmə Axını

Bir istifadəçinin mesaj göndərməsi və digərinin onu alması prosesi belə baş verir:

1.  **Klient Mesajı Göndərir:** Klient (JavaScript kodu) STOMP vasitəsilə `/app/chat.sendMessage` ünvanına (`destination`) bir mesaj göndərir. Mesajın `payload`-u `MessageRequestDTO` formatında olur.
2.  **Controller Mesajı Qəbul edir:** `ChatController`-dəki `@MessageMapping("/chat.sendMessage")` annotasiyalı `sendMessage` metodu bu mesajı qəbul edir.
3.  **İstifadəçi Kimliyinin Əldə Edilməsi:** Metod, `SimpMessageHeaderAccessor`-dan istifadə edərək WebSocket sessiyasından əvvəlcədən saxlanmış `X-Auth-User-Id`-ni əldə edir. Bu, mesajı kimin göndərdiyini müəyyən edir.
4.  **Servisə Ötürmə:** Controller, mesajın DTO-sunu və göndərən istifadəçinin ID-sini `MessageService`-in `saveMessage` metoduna ötürür.
5.  **Biznes Məntiqi və Saxlama:** `MessageService`:
    a.  Göndərən və alan istifadəçilərin mövcudluğunu yoxlamaq üçün `SkillUserClient` vasitəsilə `SkillUserService`-ə sorğu göndərir.
    b.  `MessageMapper` istifadə edərək `MessageRequestDTO`-nu `Message` entity-sinə çevirir.
    c.  Göndərənin ID-sini və serverin поточний vaxtını (`timestamp`) təyin edir.
    d.  `MessageRepository` vasitəsilə mesajı MongoDB-dəki `messages` kolleksiyasına yazır.
    e.  Saxlanmış mesajı və istifadəçi adlarını (`senderName`, `receiverName`) içeren `MessageResponseDTO` yaradır.
6.  **Mesajın Yayımlanması (Broadcast):** `ChatController` `MessageService`-dən qayıdan `MessageResponseDTO`-nu alır və `SimpMessagingTemplate` vasitəsilə onu `/topic/swap/{swapId}` ünvanına göndərir.
7.  **Klient Mesajı Alır:** Bu ünvana (`topic`) abunə olmuş bütün klientlər (yəni həmin çat otağındakı istifadəçilər) mesajı WebSocket vasitəsilə alırlar və ekranda göstərirlər.

### 3.3. Çat Tarixçəsinin Yüklənməsi

1.  **REST Sorğusu:** Klient çat pəncərəsini açdıqda, `GET /api/chat/history/{swapId}` endpointinə bir HTTP sorğusu göndərir.
2.  **Controller Sorğunu Qəbul edir:** `ChatHistoryController` bu sorğunu qəbul edir və `swapId`-ni `MessageService`-in `getChatHistory` metoduna ötürür.
3.  **Verilənlər Bazasından Oxuma:** `MessageService`, `MessageRepository`-nin `findBySwapIdOrderByTimestampAsc` metodunu çağıraraq verilən `swapId`-yə aid bütün mesajları zaman ardıcıllığı ilə MongoDB-dən oxuyur.
4.  **İstifadəçi Məlumatlarının Toplanması:** Servis, bütün mesajlardakı unikal `senderId` və `receiverId`-ləri toplayır.
5.  **Toplu Sorğu (Bulk Request):** Performansı optimallaşdırmaq üçün hər mesaj üçün ayrı-ayrı sorğu göndərmək əvəzinə, toplanmış bütün unikal ID-lər üçün `SkillUserClient` vasitəsilə `SkillUserService`-ə bir neçə sorğu göndərir və nəticələri bir `Map`-də saxlayır.
6.  **DTO-ların Hazırlanması:** Hər bir `Message` entity-si `MessageResponseDTO`-ya çevrilir və `Map`-dən istifadə edərək `senderName` və `receiverName` sahələri doldurulur.
7.  **Nəticənin Qaytarılması:** Hazırlanmış `List<MessageResponseDTO>` klientə JSON formatında qaytarılır.

---

## 4. Layihənin Detallı Strukturu

### `config` Paketi

- **`WebSocketConfig.java`**: WebSocket və STOMP konfiqurasiyasının mərkəzi.
    - `configureMessageBroker()`: Mesaj brokerini konfiqurasiya edir.
        - `enableSimpleBroker("/topic")`: Yaddaşda işləyən sadə bir mesaj brokerini aktivləşdirir. `/topic` prefiksi ilə başlayan ünvanlara göndərilən mesajlar bu broker vasitəsilə abunəçilərə çatdırılır.
        - `setApplicationDestinationPrefixes("/app")`: Klientdən serverə göndərilən mesajların ünvanlarının hansı prefikslə başlayacağını təyin edir. Bu prefiksli mesajlar `@MessageMapping` annotasiyalı metodlara yönləndirilir.
    - `registerStompEndpoints()`: Klientlərin qoşula biləcəyi HTTP endpointini qeydiyyatdan keçirir.
        - `registry.addEndpoint("/ws")`: Klientlərin WebSocket əlaqəsi qurmaq üçün istifadə edəcəyi əsas endpoint.
        - `.setAllowedOriginPatterns("*")`: Bütün mənbələrdən (origin) gələn qoşulma sorğularına icazə verir (CORS).
        - `.withSockJS()`: WebSocket-i dəstəkləməyən brauzerlər üçün alternativ kommunikasiya metodlarını (long-polling, etc.) aktivləşdirir.
    - `configureClientInboundChannel()`: Klientdən gələn mesajlar üçün interceptor əlavə edir. Burada `HttpHeaderChannelInterceptor` qeydiyyatdan keçirilir.

- **`HttpHeaderChannelInterceptor.java`**: WebSocket əlaqəsi qurularkən (STOMP `CONNECT` əmri) HTTP başlıqlarını ələ keçirən bir interceptor.
    - `preSend()`: Mesaj göndərilməzdən əvvəl işə düşür. Əgər mesaj `CONNECT` əmridirsə, `nativeHeaders`-dən `X-Auth-User-Id` başlığını axtarır və tapdığı dəyəri WebSocket sessiyasının atributlarında `"X-Auth-User-Id"` açarı ilə saxlayır. Bu, istifadəçi kimliyini sessiya boyunca əlçatan edir.

### `client` Paketi

- **`SkillUserClient.java`**: `SkillUserService` mikroxidməti ilə əlaqə qurmaq üçün istifadə olunan `OpenFeign` interfeysi.
    - `@FeignClient`: Bu interfeysin bir Feign klienti olduğunu və `skill-user-service` adlı xidmətə qoşulacağını bildirir. URL `application.yml`-dən götürülür.
    - `getUserBioByAuthUserId()`: `SkillUserService`-dəki `/api/user-bios/auth-user/{authUserId}` endpointinə `GET` sorğusu göndərərək istifadəçinin bioqrafik məlumatlarını (`UserBioResponseDTO`) əldə edir.

### `controller` Paketi

- **`ChatController.java`**: WebSocket üzərindən gələn mesajları idarə edir.
    - `@Controller`: Bu sinifin Spring MVC controlleri olduğunu bildirir (WebSocket controllerləri üçün `@RestController` deyil, `@Controller` istifadə olunur).
    - `sendMessage()`: `/app/chat.sendMessage` ünvanına gələn mesajları qəbul edir. `@Payload` annotasiyası mesajın məzmununu `MessageRequestDTO`-ya çevirir. `SimpMessageHeaderAccessor` isə sessiya məlumatlarına (məsələn, istifadəçi ID-si) giriş imkanı verir. Mesajı servisdə saxladıqdan sonra `SimpMessagingTemplate` ilə lazımi `topic`-ə yayımlayır.

- **`ChatHistoryController.java`**: REST API vasitəsilə çat tarixçəsini təqdim edir.
    - `@RestController`: Bu sinifin bir REST controller olduğunu və cavabların avtomatik olaraq JSON-a çevriləcəyini bildirir.
    - `getChatHistory()`: `GET /api/chat/history/{swapId}` sorğularını qəbul edir və `MessageService`-dən aldığı mesaj siyahısını qaytarır.

### `service` Paketi

- **`MessageService.java`**: Bütün əsas biznes məntiqini özündə cəmləşdirir.
    - `saveMessage()`: Yeni bir mesajı saxlamaq üçün alqoritm:
        1. `fetchUser()` köməkçi metodu ilə həm göndərənin, həm də alanın `SkillUserService`-də mövcud olduğunu yoxlayır. Bu, sistemdə olmayan birinə mesaj göndərməyin qarşısını alır.
        2. DTO-nu `Message` entity-sinə çevirir.
        3. `senderId`-ni WebSocket sessiyasından alınan ID ilə doldurur.
        4. Mesajı MongoDB-yə yazır.
        5. Cavab olaraq göndəriləcək `MessageResponseDTO`-nu hazırlayır və istifadəçi adlarını əlavə edir.
    - `getChatHistory()`: Çat tarixçəsini almaq üçün alqoritm:
        1. Verilən `swapId` üzrə bütün mesajları MongoDB-dən zaman sırası ilə alır.
        2. Bütün mesajlardakı unikal istifadəçi ID-lərini bir `Set`-ə yığır.
        3. Bu `Set`-dəki hər bir ID üçün `SkillUserService`-ə sorğu göndərərək istifadəçi məlumatlarını alır və bir `Map<UUID, UserBioResponseDTO>`-də saxlayır. Bu, N+1 sorğu probleminin qarşısını alır.
        4. Hər bir mesajı `MessageResponseDTO`-ya çevirir və `Map`-dən istifadə edərək adları təyin edir.
    - `fetchUser()`: `SkillUserClient`-i çağıran və `FeignException.NotFound` xətasını tutaraq daha aydın bir `IllegalArgumentException` atan privat köməkçi metod.

### `model` və `repository` Paketləri

- **`Message.java`**: MongoDB-dəki `messages` kolleksiyasının sənəd strukturunu təyin edir.
    - `@Document`: Bu sinifin bir MongoDB sənədi olduğunu bildirir.
    - `@Id`: Bu sahənin sənədin birincili açarı (`_id`) olduğunu göstərir.
    - `@CompoundIndex`: `swapId` və `timestamp` sahələri üzrə birləşik indeks yaradır. Bu, `findBySwapIdOrderByTimestampAsc` sorğularının çox sürətli işləməsini təmin edir, çünki verilənlər bazası məlumatları əvvəlcədən bu sahələrə görə sıralanmış şəkildə saxlayır.
- **`MessageRepository.java`**: `MongoRepository`-ni genişləndirir və `Message` sənədləri üçün standart CRUD əməliyyatlarını təmin edir.
    - `findBySwapIdOrderByTimestampAsc()`: Spring Data JPA-nın metod adı konvensiyası sayəsində heç bir kod yazmadan `swapId`-yə görə mesajları tapıb `timestamp`-ə görə artan sırada çeşidləyən bir sorğu generasiya edir.

---

## 5. API və WebSocket Sənədləşdirməsi

### 5.1. REST API

**Endpoint: `GET /api/chat/history/{swapId}`**

- **Təsvir:** Verilmiş `swapId`-yə aid bütün çat tarixçəsini qaytarır.
- **URL Parametrləri:**
  - `swapId` (String, məcburi): Çat tarixçəsi alınacaq mübadilənin ID-si.
- **Uğurlu Cavab (200 OK):**
  - **Body:** `MessageResponseDTO` obyektlərindən ibarət JSON massivi.
    ```json
    [
      {
        "id": "60c72b2f9b1d8c1f7c8e4c1a",
        "swapId": "SWAP12345",
        "senderId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "receiverId": "f1e2d3c4-b5a6-9870-4321-fedcba098765",
        "senderName": "Ali",
        "receiverName": "Vali",
        "content": "Salam, necəsən?",
        "timestamp": "2023-10-27T10:00:00Z"
      },
      // ... digər mesajlar
    ]
    ```

### 5.2. WebSocket API (STOMP)

- **Qoşulma Endpointi:** `ws://<host>:<port>/ws`
  - **Qoşulma Başlıqları (Headers):**
    - `X-Auth-User-Id`: Autentifikasiya olunmuş istifadəçinin UUID-si. Bu başlıq məcburidir və API Gateway tərəfindən təmin edilməlidir.

- **Mesaj Göndərmə Ünvanı (Destination):** `/app/chat.sendMessage`
  - **Body (`MessageRequestDTO`):**
    ```json
    {
      "swapId": "SWAP12345",
      "receiverId": "f1e2d3c4-b5a6-9870-4321-fedcba098765",
      "content": "Mən də yaxşıyam, təklifinizlə maraqlanıram."
    }
    ```

- **Mesajları Dinləmə Ünvanı (Subscribe Topic):** `/topic/swap/{swapId}`
  - **URL Parametrləri:**
    - `swapId`: Abunə olmaq istədiyiniz çat otağının ID-si.
  - **Alınan Mesaj Formatı (`MessageResponseDTO`):**
    ```json
    {
      "id": "60c72b2f9b1d8c1f7c8e4c1b",
      "swapId": "SWAP12345",
      "senderId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "receiverId": "f1e2d3c4-b5a6-9870-4321-fedcba098765",
      "senderName": "Ali",
      "receiverName": "Vali",
      "content": "Salam, necəsən?",
      "timestamp": "2023-10-27T10:00:00Z"
    }
    ```

---

## 6. Quraşdırma və İşə Salma

1.  **Ön Tələblər:**
    - Java 17+
    - Docker və Docker Compose

2.  **Konfiqurasiya (`application.yml`):**
    - `spring.data.mongodb.uri`: MongoDB bağlantı sətrini təyin edin. `docker-compose` istifadə edirsinizsə, bu `mongodb://localhost:27017/chatdb` kimi olacaq.
    - `skill-user-service.url`: `SkillUserService`-in ünvanını təyin edin (məsələn, `http://localhost:8082`).

3.  **Verilənlər Bazasının İşə Salınması (Docker ilə):**
    - Layihənin kök qovluğunda `docker-compose.yml` faylı mövcuddur. Bu fayl MongoDB xidmətini sizin üçün avtomatik konfiqurasiya edir.
    - Terminalda aşağıdakı əmri icra edərək MongoDB konteynerini başladın:
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
      java -jar build/libs/chatservice-0.0.1-SNAPSHOT.jar
      ```

---

## 7. Təhlükəsizlik Aspektləri

`ChatService`-də təhlükəsizlik bir neçə səviyyədə təmin edilir:

- **API Gateway:** Bütün xarici trafik API Gateway üzərindən keçməlidir. Gateway, istifadəçinin JWT tokenini yoxlayır, onu təsdiqləyir və etibarlıdırsa, sorğunun başlığına `X-Auth-User-Id` əlavə edərək `ChatService`-ə yönləndirir. `ChatService`-in özü birbaşa internetə çıxmamalıdır.
- **WebSocket Autentifikasiyası:** `HttpHeaderChannelInterceptor` sayəsində yalnız autentifikasiya olunmuş və `X-Auth-User-Id` başlığına malik istifadəçilər WebSocket əlaqəsi qura bilər. Bu, anonim istifadəçilərin çat sisteminə qoşulmasının qarşısını alır.
- **Məntiqi Avtorizasiya:** Gələcəkdə `MessageService`-də əlavə yoxlamalar aparıla bilər. Məsələn, mesaj göndərən istifadəçinin həqiqətən də həmin `swapId`-nin iştirakçısı olub-olmadığını yoxlamaq olar. Bu, bir istifadəçinin aid olmadığı bir çata mesaj göndərməsinin qarşısını alar.