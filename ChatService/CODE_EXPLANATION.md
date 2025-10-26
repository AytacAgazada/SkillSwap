
# `ChatService` Mikroxidmətinin Dərinlemesine Kodu İzahı

Bu sənəd, `ChatService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, real-zamanlı mesajlaşma sisteminin arxitekturasını, WebSocket və STOMP protokollarının istifadəsini, MongoDB ilə inteqrasiyanı və digər servislərlə əlaqəni tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `ChatServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/ChatServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir.

```java
package com.example.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ChatServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatServiceApplication.class, args);
	}

}
```

### Sətir-Sətir İzahı

- **`@SpringBootApplication`**: Bu tətbiqin bir Spring Boot tətbiqi olduğunu bildirən əsas konfiqurasiya annotasiyası.
- **`@EnableDiscoveryClient`**: Bu annotasiya, tətbiqin bir "Discovery Service"-ə (məsələn, Eureka) özünü qeydiyyatdan keçirməsini təmin edir. Bu, API Gateway və digər servislərin `chat-service`-i şəbəkədə tapmasına imkan verir.
- **`@EnableFeignClients`**: Bu annotasiya, tətbiqdə `Feign Client` interfeyslərinin (`@FeignClient` ilə işarələnmiş) axtarılıb tapılmasını və onlar üçün konkret implementasiyaların yaradılmasını aktivləşdirir. Bu, digər mikroxidmətlərə REST zənglərini asanlaşdırır.
- **`main` metodu**: Tətbiqi işə salan standart Java giriş nöqtəsi.

---

## 2. `config` Paketi

Bu paket, WebSocket və STOMP protokolu ilə bağlı bütün konfiqurasiyaları saxlayır.

### 2.1. `WebSocketConfig.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/config/WebSocketConfig.java`

Bu sinif, WebSocket əlaqələrini və mesajlaşma brokerini konfiqurasiya edən əsas sinifdir.

```java
package com.example.chatservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Broker prefix
        config.setApplicationDestinationPrefixes("/app"); // Client-to-server messages
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new HttpHeaderChannelInterceptor());
    }
}
```

#### Sətir-Sətir İzahı

- **`@Configuration`**: Bu sinifin bir Spring konfiqurasiya sinifi olduğunu bildirir.
- **`@EnableWebSocketMessageBroker`**: WebSocket mesajlaşmasını aktivləşdirir və STOMP protokolu ilə işləyən bir mesaj brokerini (message broker) təmin edir.
- **`implements WebSocketMessageBrokerConfigurer`**: WebSocket konfiqurasiyasını fərdiləşdirmək üçün metodları təmin edən interfeysi implementasiya edir.

- **`configureMessageBroker(MessageBrokerRegistry config)` metodu**:
    - **`config.enableSimpleBroker("/topic");`**: Yaddaşda işləyən sadə bir mesaj brokerini aktivləşdirir. Bu broker, `/topic` prefiksi ilə başlayan ünvanlara (destination) göndərilən mesajları həmin ünvanlara abunə olmuş bütün klientlərə yayımlayır (broadcast). Məsələn, server `/topic/swap/123`-ə mesaj göndərdikdə, bu ünvana abunə olan bütün klientlər mesajı alacaq.
    - **`config.setApplicationDestinationPrefixes("/app");`**: Klientdən serverə göndərilən mesajların ünvanlarının hansı prefikslə başlayacağını təyin edir. `/app` prefiksli mesajlar birbaşa brokerə deyil, tətbiqin `@MessageMapping` annotasiyalı metodlarına yönləndirilir. Məsələn, klient `/app/chat.sendMessage`-ə mesaj göndərdikdə, bu mesaj `@MessageMapping("/chat.sendMessage")` annotasiyalı controller metoduna çatacaq.

- **`registerStompEndpoints(StompEndpointRegistry registry)` metodu**:
    - **`registry.addEndpoint("/ws")`**: Klientlərin WebSocket əlaqəsi qurmaq üçün istifadə edəcəyi HTTP endpointini qeydiyyatdan keçirir. Klientlər `ws://<server-address>/ws` ünvanına qoşulacaqlar.
    - **`.setAllowedOriginPatterns("*")`**: Cross-Origin Resource Sharing (CORS) siyasətini təyin edir. `*` bütün domenlərdən gələn qoşulma sorğularına icazə verildiyini bildirir. Production mühitində bu, yalnız frontend tətbiqinin domeni ilə məhdudlaşdırılmalıdır.
    - **`.withSockJS()`**: SockJS dəstəyini aktivləşdirir. Bu, klientin brauzeri standart WebSocket protokolunu dəstəkləmirsə, əlaqənin avtomatik olaraq HTTP long-polling kimi alternativ, lakin eyni funksionallığı təmin edən nəqliyyat metodlarına keçməsini (fallback) təmin edir. Bu, köhnə brauzerlərlə uyğunluğu artırır.

- **`configureClientInboundChannel(ChannelRegistration registration)` metodu**:
    - **`registration.interceptors(new HttpHeaderChannelInterceptor());`**: Klientdən serverə gələn mesaj kanalına (`inbound channel`) bir "interceptor" (ələkeçirici) əlavə edir. Bu, klientdən gələn hər bir STOMP mesajını (qoşulma, mesaj göndərmə və s.) controller-ə çatmazdan əvvəl yoxlamağa və ya dəyişdirməyə imkan verir. Bizim halda, `HttpHeaderChannelInterceptor` istifadəçi kimliyini WebSocket sessiyasına əlavə etmək üçün istifadə olunur.

### 2.2. `HttpHeaderChannelInterceptor.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/config/HttpHeaderChannelInterceptor.java`

Bu sinif, WebSocket əlaqəsi qurularkən klientin göndərdiyi HTTP başlıqlarından istifadəçi kimliyini oxuyub WebSocket sessiyasına yazmaq üçün istifadə olunur.

```java
public class HttpHeaderChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object raw = message.getHeaders().get(StompHeaderAccessor.NATIVE_HEADERS);

            if (raw instanceof Map) {
                Object res = ((Map) raw).get("X-Auth-User-Id");

                if (res instanceof LinkedList) {
                    accessor.getSessionAttributes().put("X-Auth-User-Id", ((LinkedList) res).get(0));
                }
            }
        }
        return message;
    }
}
```

#### Sətir-Sətir İzahı

- **`implements ChannelInterceptor`**: Spring Messaging-in təqdim etdiyi, mesaj kanallarını ələ keçirmək üçün istifadə olunan interfeys.
- **`preSend(Message<?> message, MessageChannel channel)` metodu**: Mesaj kanala göndərilməzdən dərhal əvvəl çağırılır.
    - **`StompHeaderAccessor accessor = ...`**: Mesajın başlıqlarını STOMP protokolu kontekstində asanlıqla idarə etmək üçün bir köməkçi obyekt yaradır.
    - **`if (StompCommand.CONNECT.equals(accessor.getCommand()))`**: Yalnız klientin ilk qoşulma cəhdi zamanı (yəni, STOMP əmri `CONNECT` olduqda) bu blokun işləməsini təmin edir.
    - **`Object raw = message.getHeaders().get(StompHeaderAccessor.NATIVE_HEADERS);`**: Klientin WebSocket "handshake" sorğusu zamanı göndərdiyi orijinal HTTP başlıqlarını (`native headers`) əldə edir.
    - **`if (raw instanceof Map)`**: Başlıqların `Map` formatında olduğunu yoxlayır.
    - **`Object res = ((Map) raw).get("X-Auth-User-Id");`**: Bu `Map`-dən `X-Auth-User-Id` adlı başlığın dəyərini axtarır. Bu başlığın API Gateway tərəfindən, istifadəçinin JWT tokeni təsdiqləndikdən sonra sorğuya əlavə edildiyi fərz edilir.
    - **`if (res instanceof LinkedList)`**: Bəzi server implementasiyalarında eyni adlı başlıq bir neçə dəfə göndərilə bildiyi üçün, dəyər bir siyahı (`LinkedList`) içində gələ bilər. Bu sətir həmin vəziyyəti yoxlayır.
    - **`accessor.getSessionAttributes().put("X-Auth-User-Id", ...)`**: **Ən vacib hissə.** Oxunmuş istifadəçi ID-sini (`X-Auth-User-Id`-nin dəyərini) WebSocket sessiyasının atributlarında saxlayır. Bu andan etibarən, həmin istifadəçinin sessiyası boyunca göndərdiyi bütün digər mesajlarda bu ID-yə `headerAccessor.getSessionAttributes()` vasitəsilə çatmaq mümkün olacaq. Bu, hər mesajda istifadəçini yenidən autentifikasiya etmək ehtiyacını aradan qaldırır.
    - **`return message;`**: Mesajı (potensial olaraq dəyişdirilmiş başlıqlarla) zəncirdəki növbəti mərhələyə ötürür.

---

## 3. `client` Paketi

Bu paket, digər mikroxidmətlərlə əlaqə qurmaq üçün istifadə olunan `OpenFeign` klient interfeyslərini saxlayır.

### 3.1. `SkillUserClient.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/client/SkillUserClient.java`

Bu interfeys, `SkillUserService`-dən istifadəçi məlumatlarını əldə etmək üçün istifadə olunur.

```java
package com.example.chatservice.client;

import com.example.chatservice.dto.UserBioResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "skill-user-service", url = "${skill-user-service.url}")
public interface SkillUserClient {

    @GetMapping("/api/user-bios/auth-user/{authUserId}")
    public ResponseEntity<UserBioResponseDTO> getUserBioByAuthUserId(@PathVariable UUID authUserId);
}
```

#### Sətir-Sətir İzahı

- **`@FeignClient(name = "skill-user-service", url = "${skill-user-service.url}")`**: Bu interfeysin bir Feign klienti olduğunu bildirir.
    - **`name = "skill-user-service"`**: Bu klientin məntiqi adıdır. Əgər `url` verilməsəydi, Feign bu adı Eureka kimi bir Discovery Service-dən xidmətin ünvanını tapmaq üçün istifadə edərdi.
    - **`url = "${skill-user-service.url}"`**: Xidmətin konkret ünvanını `application.yml`-dən oxumağı təmin edir. Bu, xidmətin ünvanını koddan kənarda saxlamağa imkan verir.
- **`@GetMapping("/api/user-bios/auth-user/{authUserId}")`**: Bu metod çağırıldıqda, Feign-in `skill-user-service`-in `/api/user-bios/auth-user/{authUserId}` endpointinə bir HTTP `GET` sorğusu göndərəcəyini bildirir.
- **`public ResponseEntity<UserBioResponseDTO> getUserBioByAuthUserId(@PathVariable UUID authUserId);`**: Metodun imzası, çağırılacaq REST endpointinin imzası ilə tamamilə uyğun olmalıdır. `@PathVariable` annotasiyası `authUserId` parametrinin URL-dəki `{authUserId}` hissəsinə yerləşdiriləcəyini göstərir. Feign, bu metodu çağırdıqda arxa planda bütün HTTP sorğu məntiqini (URL-in qurulması, sorğunun göndərilməsi, cavabın JSON-dan `UserBioResponseDTO`-ya çevrilməsi) özü həyata keçirir.

---

## 4. `controller` Paketi

Bu paket, həm WebSocket, həm də REST sorğularını qəbul edən controller-ləri saxlayır.

### 4.1. `ChatController.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/controller/ChatController.java`

Bu controller, WebSocket üzərindən gələn real-zamanlı çat mesajlarını idarə edir.

```java
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequestDTO dto, SimpMessageHeaderAccessor headerAccessor) {
        String authUserId = (String) headerAccessor.getSessionAttributes().get("X-Auth-User-Id");
        MessageResponseDTO saved = messageService.saveMessage(dto, UUID.fromString(authUserId));
        messagingTemplate.convertAndSend("/topic/swap/" + saved.getSwapId(), saved);
    }
}
```

#### Sətir-Sətir İzahı

- **`@Controller`**: Bu sinifin bir Spring controller-i olduğunu bildirir. WebSocket controller-ləri üçün `@RestController` deyil, adətən `@Controller` istifadə olunur, çünki metodlar birbaşa cavab body-si qaytarmaya bilər.
- **`private final SimpMessagingTemplate messagingTemplate;`**: Klientlərə mesaj göndərmək üçün istifadə olunan əsas köməkçi sinif. Bu, mesajları müəyyən bir "destination"-a (məsələn, `/topic/...`) göndərməyə imkan verir.
- **`private final MessageService messageService;`**: Mesajları saxlamaq və digər biznes məntiqini həyata keçirmək üçün istifadə olunan servis.
- **`@MessageMapping("/chat.sendMessage")`**: Bu metodun, `/app/chat.sendMessage` ünvanına (`/app` prefiksi `WebSocketConfig`-də təyin edilib) göndərilən STOMP mesajlarını idarə edəcəyini bildirir.
- **`public void sendMessage(...)`**: Metodun imzası.
    - **`@Payload MessageRequestDTO dto`**: Gələn STOMP mesajının "payload" (yük) hissəsini `MessageRequestDTO` obyektinə çevirir.
    - **`SimpMessageHeaderAccessor headerAccessor`**: Mesajın başlıqlarına və WebSocket sessiyasına giriş imkanı verir.
- **`String authUserId = (String) headerAccessor.getSessionAttributes().get("X-Auth-User-Id");`**: `HttpHeaderChannelInterceptor`-da sessiyaya yazılmış istifadəçi ID-sini oxuyur. Bu, mesajı kimin göndərdiyini müəyyən edir.
- **`MessageResponseDTO saved = messageService.saveMessage(dto, UUID.fromString(authUserId));`**: Mesajı və göndərənin ID-sini `MessageService`-ə ötürərək verilənlər bazasına yazdırır və zənginləşdirilmiş cavab DTO-sunu alır.
- **`messagingTemplate.convertAndSend("/topic/swap/" + saved.getSwapId(), saved);`**: **Ən vacib hissə.** `SimpMessagingTemplate` vasitəsilə, saxlanmış mesajı (`saved` DTO) `/topic/swap/{swapId}` ünvanına göndərir. Bu ünvana abunə olmuş bütün klientlər (yəni, həmin çat otağındakı istifadəçilər) bu mesajı real-zamanlı olaraq alacaqlar.

### 4.2. `ChatHistoryController.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/controller/ChatHistoryController.java`

Bu controller, REST API vasitəsilə çat tarixçəsini təqdim edir.

```java
@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final MessageService messageService;

    @GetMapping("/{swapId}")
    public List<MessageResponseDTO> getChatHistory(@PathVariable String swapId) {
        return messageService.getChatHistory(swapId);
    }
}
```

#### Sətir-Sətir İzahı

- **`@RestController`**: Bu sinifin bir REST controller olduğunu və metod cavablarının avtomatik JSON-a çevriləcəyini bildirir.
- **`@RequestMapping("/api/chat/history")`**: Bu controller-dəki bütün endpointlərin `/api/chat/history` prefiksi ilə başlayacağını göstərir.
- **`@GetMapping("/{swapId}")`**: Bu metodun, `/api/chat/history/{swapId}` ünvanına edilən HTTP `GET` sorğularını idarə edəcəyini bildirir.
- **`public List<MessageResponseDTO> getChatHistory(@PathVariable String swapId)`**: URL-dən `swapId`-ni götürür, `MessageService`-in `getChatHistory` metodunu çağırır və nəticəni (mesajlar siyahısını) birbaşa qaytarır. `@RestController` olduğu üçün bu siyahı avtomatik olaraq JSON massivinə çevriləcək.

---

## 5. `dto` və `model` Paketləri

Bu paketlər tətbiqin məlumat strukturlarını təyin edir.

- **`MessageRequestDTO`**: Klientdən yeni bir mesaj göndərmək üçün gələn məlumatları saxlayır (`swapId`, `receiverId`, `content`).
- **`MessageResponseDTO`**: Serverdən klientə göndərilən (həm WebSocket, həm də REST vasitəsilə) zənginləşdirilmiş mesaj məlumatlarını saxlayır. Bu, `MessageRequestDTO`-dan fərqli olaraq `id`, `senderId`, `timestamp`, və ən əsası, `senderName` və `receiverName` kimi əlavə sahələrə malikdir.
- **`UserBioResponseDTO`, `SkillResponseDto`**: `SkillUserService`-dən Feign client vasitəsilə alınan məlumatların strukturunu təyin edir.
- **`Message` (model)**: MongoDB-dəki `messages` kolleksiyasına yazılacaq sənədin strukturunu təyin edən əsas entity.
    - **`@Document(collection = "messages")`**: Bu sinifin MongoDB-də `messages` adlı kolleksiyaya map olunduğunu bildirir.
    - **`@Id private String id;`**: Bu sahənin sənədin birincili açarı (`_id`) olduğunu göstərir. MongoDB avtomatik olaraq bu sahə üçün unikal bir dəyər yaradacaq.
    - **`@CompoundIndex(name = "swap_timestamp_idx", def = "{'swapId': 1, 'timestamp': 1}")`**: **Performans üçün kritik.** Bu annotasiya, MongoDB-yə `swapId` və `timestamp` sahələri üzrə birləşik bir indeks yaratmasını deyir. `def` atributundakı `1` artan sıra deməkdir. Bu indeks, `findBySwapIdOrderByTimestampAsc` sorğusunun (çat tarixçəsini çəkən sorğu) verilənlər bazasındakı bütün sənədləri skan etmək əvəzinə, birbaşa indeksdən çox sürətli şəkildə oxumasına imkan verir. Böyük həcmli çat tarixçələri üçün bu, performansı kəskin dərəcədə yaxşılaşdırır.

---

## 6. `repository` və `mapper` Paketləri

- **`MessageRepository.java`**: `MongoRepository`-ni genişləndirir və `Message` sənədləri üçün verilənlər bazası əməliyyatlarını təmin edir.
    - **`List<Message> findBySwapIdOrderByTimestampAsc(String swapId);`**: Spring Data MongoDB-nin metod adından sorğu yaratma xüsusiyyətindən istifadə edir. Verilmiş `swapId`-yə uyğun bütün sənədləri tapır və onları `timestamp` sahəsinə görə artan sırada çeşidləyərək qaytarır.
- **`MessageMapper.java`**: `MessageRequestDTO`-nu `Message` entity-sinə və `Message` entity-sini `MessageResponseDTO`-ya çevirən sadə bir mapper sinifidir. Burada MapStruct istifadə edilməməsi, çevirmə məntiqinin sadə olduğunu və manual implementasiyanın kifayət etdiyini göstərir.

---

## 7. `service` Paketi

### 7.1. `MessageService.java`

**Fayl Yolu:** `src/main/java/com/example/chatservice/service/MessageService.java`

Bu servis, çatla bağlı bütün əsas biznes məntiqini həyata keçirir.

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final SkillUserClient skillUserClient;

    @Transactional
    public MessageResponseDTO saveMessage(MessageRequestDTO dto, UUID authUserId) { ... }

    public List<MessageResponseDTO> getChatHistory(String swapId) { ... }

    private UserBioResponseDTO fetchUser(UUID authUserId) { ... }
}
```

#### Metodların Detallı İzahı

- **`saveMessage(MessageRequestDTO dto, UUID authUserId)` metodu**:
    1.  **`UserBioResponseDTO sender = fetchUser(authUserId);`**: `fetchUser` köməkçi metodunu çağıraraq mesajı göndərən istifadəçinin `SkillUserService`-də mövcud olduğunu yoxlayır və məlumatlarını alır.
    2.  **`UserBioResponseDTO receiver = fetchUser(dto.getReceiverId());`**: Eyni yoxlamanı mesajı alan istifadəçi üçün də edir.
    3.  **`Message message = messageMapper.toEntity(dto);`**: Gələn DTO-nu `Message` entity-sinə çevirir.
    4.  **`message.setSenderId(authUserId);`**: Mesajı kimin göndərdiyini WebSocket sessiyasından alınan `authUserId` ilə təyin edir.
    5.  **`Message saved = messageRepository.save(message);`**: Hazırlanmış mesaj obyektini MongoDB-yə yazır.
    6.  **`MessageResponseDTO responseDTO = messageMapper.toResponseDTO(saved);`**: Saxlanmış entity-ni cavab DTO-suna çevirir.
    7.  **`responseDTO.setSenderName(sender.getFirstName());`**, **`responseDTO.setReceiverName(receiver.getFirstName());`**: Cavab DTO-sunu `SkillUserService`-dən alınan adlarla zənginləşdirir.
    8.  Zənginləşdirilmiş DTO-nu `ChatController`-ə qaytarır ki, o da bunu klientlərə yayımlasın.

- **`getChatHistory(String swapId)` metodu**:
    1.  **`List<Message> messages = messageRepository.findBySwapIdOrderByTimestampAsc(swapId);`**: Verilən `swapId` üçün bütün mesajları verilənlər bazasından çəkir.
    2.  **`Set<UUID> userIds = messages.stream()...`**: Bütün mesajlardakı `senderId` və `receiverId`-ləri toplayaraq unikal istifadəçi ID-lərindən ibarət bir `Set` yaradır. Bu, eyni istifadəçi üçün təkrar sorğular göndərməyin qarşısını alır.
    3.  **`Map<UUID, UserBioResponseDTO> userMap = new HashMap<>();`**: ID-ləri istifadəçi məlumatları ilə əlaqələndirmək üçün bir `Map` yaradır.
    4.  **`userIds.forEach(id -> { ... });`**: Hər bir unikal ID üçün `fetchUser` metodunu çağırır və nəticəni `userMap`-ə yerləşdirir. Bu, "N+1 sorğu" probleminin qarşısını alır (yəni, N sayda mesaj üçün N*2 sayda sorğu əvəzinə, yalnız unikal istifadəçi sayı qədər sorğu göndərilir).
    5.  **`messages.stream().map(msg -> { ... });`**: Hər bir `Message` obyektini `MessageResponseDTO`-ya çevirir və `userMap`-dən istifadə edərək `senderName` və `receiverName` sahələrini doldurur.
    6.  Nəticə olaraq, tam zənginləşdirilmiş mesajlar siyahısını qaytarır.

- **`fetchUser(UUID authUserId)` metodu**:
    - `SkillUserClient` vasitəsilə `SkillUserService`-ə sorğu göndərən privat bir metoddur.
    - `try-catch` bloku, `FeignException.NotFound` xətasını (yəni, `SkillUserService`-in 404 cavabı qaytardığı vəziyyəti) tutmaq üçün istifadə olunur. Bu xəta tutulduqda, daha aydın bir `IllegalArgumentException` atılır ki, bu da problemin mənbəyini daha dəqiq göstərir.

---

**`ChatService` üçün Kod İzahının Sonu.**
