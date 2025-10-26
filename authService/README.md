
# AuthService Mikroxidməti

`AuthService` mikroxidməti istifadəçi qeydiyyatı, autentifikasiyası, avtorizasiyası və ümumi istifadəçi idarəetməsi üçün mərkəzi bir xidmətdir. Bu xidmət JWT (JSON Web Token) əsaslı təhlükəsizlik, OTP (One-Time Password) vasitəsilə hesab təsdiqləmə və şifrə sıfırlama, eləcə də rollara əsaslanan giriş nəzarəti (RBAC) kimi funksionallıqları təmin edir.

## Əsas Funksionallıqlar

- **İstifadəçi Qeydiyyatı:** Yeni istifadəçilərin sistemdə qeydiyyatdan keçməsi.
- **Autentifikasiya:** İstifadəçilərin FIN/email/username və şifrə ilə sistemə daxil olması.
- **Token İdarəetməsi:** JWT Access və Refresh tokenlərinin yaradılması və idarə olunması.
- **Hesab Təsdiqləmə:** E-poçt vasitəsilə göndərilən OTP ilə yeni hesabların aktivləşdirilməsi.
- **Şifrə Sıfırlama:** OTP vasitəsilə istifadəçilərin şifrələrini təhlükəsiz şəkildə sıfırlaması.
- **Rol Əsaslı Giriş Nəzarəti (RBAC):** `USER`, `ADMIN`, `PROVIDER` rollarına əsasən endpointlərə girişin məhdudlaşdırılması.
- **Asinxron Əməliyyatlar:** E-poçt bildirişlərinin asinxron şəkildə göndərilməsi.
- **API Sənədləşdirməsi:** `OpenAPI (Swagger)` ilə detallı API sənədləşdirilməsi.

---

## Layihənin Strukturu

Layihə paketlərə bölünmüş şəkildə təşkil olunub. Hər bir paketin öz məsuliyyət sahəsi var:

```
com.example.authservice
├── AuthServiceApplication.java      # Spring Boot tətbiqinin əsas giriş nöqtəsi
├── config/                          # Təhlükəsizlik, OpenAPI, Asinxron konfiqurasiyaları
├── controller/                      # API endpointlərini (REST controller) saxlayır
├── exception/                       # Xüsusi xəta (exception) sinifləri və qlobal xəta idarəedicisi
├── kafka/                           # Kafka producer və DTO-ları
├── mapper/                          # MapStruct istifadə edərək DTO və Entity arasında çevirmələr
├── model/
│   ├── dto/                         # Data Transfer Object (DTO) sinifləri
│   ├── entity/                      # JPA Entity sinifləri (verilənlər bazası cədvəlləri)
│   └── enumeration/                 # Enum sinifləri (məsələn, Role)
├── repository/                      # Spring Data JPA repository interfeysləri
├── security/
│   ├── jwt/                         # JWT tokenlərinin yaradılması, doğrulanması və filtrlənməsi
│   └── services/                    # Spring Security üçün UserDetailsService implementasiyası
├── service/                         # Biznes məntiqini saxlayan servis sinifləri
└── validation/                      # Xüsusi validasiya annotasiyaları və validatorlar
```

---

## Əsas Siniflər və Məntiq

### `AuthServiceApplication.java`

Bu, Spring Boot tətbiqini başladan əsas sinifdir.

- `@SpringBootApplication`: Spring Boot-un əsas konfiqurasiya annotasiyası.
- `@EnableAsync`: Asinxron metodların (`@Async`) işləməsi üçün aktivləşdirilir. Bu, xüsusilə e-poçt göndərmə kimi vaxt apara bilən əməliyyatların arxa fonda yerinə yetirilməsi üçün vacibdir.
- `@EnableDiscoveryClient`: Bu servisin Eureka kimi bir "Discovery Service"-ə qeydiyyatdan keçməsini təmin edir. Bu, mikroxidmət arxitekturasında digər servislərin bu xidməti tapa bilməsi üçün lazımdır.

### `config` Paketi

#### `AsyncConfig.java`

Bu sinif asinxron əməliyyatlar üçün bir `ThreadPoolTaskExecutor` konfiqurasiya edir. `@Async` annotasiyası ilə işarələnmiş metodlar bu hovuzdakı thread-lərdə icra olunur. Bu, əsas tətbiq axınını bloklamadan uzun çəkən tapşırıqları (məsələn, e-poçt göndərmək) yerinə yetirməyə imkan verir.

- `corePoolSize`: Eyni anda aktiv ola biləcək minimum thread sayı.
- `maxPoolSize`: Hovuzda ola biləcək maksimum thread sayı.
- `queueCapacity`: Maksimum thread sayına çatdıqda tapşırıqların gözləyəcəyi növbənin ölçüsü.

#### `OpenApiConfig.java`

Bu sinif `Swagger UI` və `OpenAPI 3.0` sənədləşdirməsini konfiqurasiya edir. API haqqında məlumatlar (başlıq, versiya, təsvir) və JWT (Bearer Token) üçün təhlükəsizlik sxemi burada təyin edilir. Bu, API endpointlərini asanlıqla test etmək və sənədləşdirmək üçün bir interfeys təqdim edir.

#### `SecurityConfig.java`

Bu, tətbiqin təhlükəsizlik konfiqurasiyasının mərkəzidir.

- `@EnableWebSecurity`: Spring Security-ni aktivləşdirir.
- `@EnableMethodSecurity(prePostEnabled = true)`: Metod səviyyəsində təhlükəsizliyi (`@PreAuthorize` kimi annotasiyaları) aktivləşdirir.
- `passwordEncoder()`: Şifrələri təhlükəsiz şəkildə saxlamaq üçün `BCryptPasswordEncoder` istifadə edir.
- `authenticationProvider()`: İstifadəçi məlumatlarını (`UserDetailsServiceImpl`) və şifrə kodlayıcısını təyin edərək autentifikasiya prosesini konfiqurasiya edir.
- `authenticationManager()`: Autentifikasiya prosesini idarə edən `AuthenticationManager`-i yaradır.
- `filterChain(HttpSecurity http)`: Əsas təhlükəsizlik qaydalarını təyin edir:
    - `csrf(csrf -> csrf.disable())`: CSRF (Cross-Site Request Forgery) qorumasını deaktiv edir, çünki JWT istifadə edən stateless API-lərdə bu, adətən lazımsızdır.
    - `exceptionHandling(...)`: Autentifikasiya və avtorizasiya xətaları üçün xüsusi idarəediciləri (`AuthEntryPointJwt` və `CustomAccessDeniedHandler`) təyin edir.
    - `sessionManagement(...)`: Sessiya idarəetməsini `STATELESS` olaraq təyin edir, yəni server tərəfində sessiya saxlanılmır. Hər bir sorğu müstəqil şəkildə autentifikasiya olunur.
    - `authorizeHttpRequests(...)`: Hansı endpointlərə kimin giriş əldə edə biləcəyini müəyyənləşdirir. Məsələn, `/api/auth/**` və Swagger UI endpointləri hər kəsə açıqdır, lakin digər endpointlər müəyyən rollar tələb edir.
    - `addFilterBefore(...)`: Hər bir sorğudan əvvəl JWT tokenini yoxlayan `AuthTokenFilter`-i əlavə edir.

### `controller` Paketi

#### `AuthController.java`

Bu sinif autentifikasiya və istifadəçi idarəetməsi ilə bağlı bütün API endpointlərini təmin edir.

- `@RestController`: Bu sinifin bir REST controller olduğunu bildirir.
- `@RequestMapping("/api/auth")`: Bu controllerdəki bütün endpointlərin `/api/auth` prefiksi ilə başlayacağını göstərir.
- `@Tag(...)`: Swagger UI-da bu controller üçün qruplaşdırma və təsvir əlavə edir.

**Endpointlər:**

- `POST /signup`: Yeni istifadəçi qeydiyyatdan keçirir.
- `POST /login`: İstifadəçini autentifikasiya edir və `AuthResponse` (access və refresh tokenlər) qaytarır.
- `POST /refresh-token`: Refresh token istifadə edərək yeni bir access token yaradır.
- `POST /logout`: İstifadəçinin bütün refresh tokenlərini ləğv edərək sistemdən çıxış edir.
- `POST /otp/send`: Hesab təsdiqləmə və ya şifrə sıfırlama üçün OTP göndərir.
- `POST /otp/verify`: Göndərilən OTP-ni yoxlayır.
- `POST /password/reset`: OTP təsdiqləndikdən sonra istifadəçi şifrəsini sıfırlayır.
- `GET /user/profile`, `GET /admin/dashboard`, və s.: Rol əsaslı girişə nümunə olan endpointlər. `@PreAuthorize` annotasiyası ilə qorunur.
- `GET /{authUserId}/exists`: Verilən ID-yə malik istifadəçinin mövcud olub-olmadığını yoxlayır.
- `GET /{authUserId}/role`: Verilən ID-yə malik istifadəçinin rolunu qaytarır.

### `exception` Paketi

Bu paket tətbiqə məxsus xüsusi xətaları və onların qlobal səviyyədə idarə olunmasını təmin edir.

- **Xüsusi Exceptionlar:**
    - `UserAlreadyExistsException`: Qeydiyyat zamanı eyni məlumatlara (FIN, email, və s.) malik istifadəçi olduqda atılır.
    - `InvalidCredentialsException`: Yanlış istifadəçi adı və ya şifrə daxil edildikdə atılır.
    - `OtpException`: OTP ilə bağlı xətalar (yanlış kod, vaxtı keçmiş kod) üçün istifadə olunur.
    - `TokenRefreshException`: Refresh token ilə bağlı problemlər (etibarsız, vaxtı keçmiş) üçün atılır.
    - `ResourceNotFoundException`: Axtarılan resurs (məsələn, istifadəçi) tapılmadıqda atılır.
- `GlobalExceptionHandler`: `@RestControllerAdvice` annotasiyası ilə bütün controllerlərdə yaranan xətaları tutur. Hər bir xüsusi xəta üçün `@ExceptionHandler` metodu təyin edilib və bu metodlar standartlaşdırılmış `ErrorResponse` formatında cavab qaytarır. Bu, API cavablarının ardıcıl olmasını təmin edir.
- `CustomAccessDeniedHandler`: Spring Security tərəfindən istifadə olunur. Bir istifadəçi lazımi rola sahib olmadan bir endpointə daxil olmaq istədikdə (403 Forbidden xətası) bu sinif işə düşür və standartlaşdırılmış JSON cavabı qaytarır.

### `kafka` Paketi

Bu paket Apache Kafka ilə inteqrasiyanı təmin edir.

- `KafkaAuthProducer`: Kafka-ya mesaj göndərmək üçün istifadə olunan servisdir.
    - `sendUserRegistrationEvent()`: Yeni bir istifadəçi qeydiyyatdan keçdikdə, `user-registration-topic`-inə `UserRegisteredEventDTO` obyekti göndərir. Bu, digər mikroxidmətlərin (məsələn, `SkillUserService`) yeni istifadəçi haqqında məlumat almasını və öz verilənlər bazalarında müvafiq qeydlər yaratmasını təmin edir.
- `UserRegisteredEventDTO`: Kafka mesajının məlumat strukturunu təyin edən DTO-dur. İstifadəçinin ID-si, e-poçtu və digər lazımi məlumatları saxlayır.

### `mapper` Paketi

#### `UserMapper.java`

Bu interfeys `MapStruct` istifadə edərək `SignupRequest` (DTO) obyektini `User` (Entity) obyektinə çevirir.

- `@Mapper(componentModel = "spring")`: Bu interfeysin bir Spring komponenti olduğunu və `AuthService` kimi digər komponentlərə inject edilə biləcəyini bildirir.
- `@Mapping(target = "password", ignore = true)`: Çevirmə zamanı `password` sahəsinin kopyalanmamasını təmin edir. Çünki parol `AuthService`-də ayrıca şifrələnərək təyin edilir.

### `model` Paketi

#### `dto` (Data Transfer Objects)

Bu DTO-lar API endpointləri ilə klient arasında məlumat mübadiləsi üçün istifadə olunur. Onlar validasiya annotasiyaları (`@NotBlank`, `@Size`, `@Email`) ilə təchiz olunublar.

- `SignupRequest`: İstifadəçi qeydiyyatı üçün məlumatları saxlayır.
- `LoginRequest`: Daxil olmaq üçün istifadəçi adı/FIN/email və şifrəni saxlayır.
- `AuthResponse`: Uğurlu autentifikasiyadan sonra qaytarılan access və refresh tokenləri, həmçinin istifadəçi məlumatlarını saxlayır.
- `OtpSendRequest`, `OtpVerificationRequest`, `ResetPasswordRequest`: OTP və şifrə sıfırlama axınları üçün istifadə olunur.
- `RefreshTokenRequest`: Yeni access token almaq üçün refresh tokeni saxlayır.

#### `entity` (JPA Entities)

Bu siniflər verilənlər bazasındakı cədvəlləri təmsil edir.

- `User`: Əsas istifadəçi məlumatlarını (`username`, `fin`, `password`, `email`, `role`, və s.) saxlayan entity. `UserDetails` interfeysini implementasiya edərək Spring Security ilə inteqrasiya olunur.
    - `getUsername()` metodu autentifikasiya üçün `fin`-i qaytarır. Bu, sistemə FIN ilə daxil olmağı təmin edir.
- `RefreshToken`: İstifadəçiyə aid refresh tokenləri, onların bitmə tarixini, IP ünvanını və user-agent məlumatlarını saxlayır. Bu, təhlükəsizliyi artırır.
- `ConfirmationToken`: OTP kodlarını, onların aid olduğu istifadəçini, növünü (`ACCOUNT_CONFIRMATION` və ya `PASSWORD_RESET`) və bitmə tarixini saxlayır.

#### `enumeration`

- `Role`: Sistemdəki istifadəçi rollarını (`USER`, `ADMIN`, `PROVIDER`) təyin edən `enum`.

### `repository` Paketi

Bu paket `Spring Data JPA` repository interfeyslərini saxlayır. Bu interfeyslər verilənlər bazası ilə əməliyyatlar aparmaq üçün metodlar təqdim edir (məsələn, `save`, `findById`, `findByUsername`).

- `UserRepository`: `User` entity-ləri üçün CRUD (Create, Read, Update, Delete) əməliyyatları və xüsusi axtarış metodları (`findByFin`, `existsByEmail`, və s.) təqdim edir.
- `RefreshTokenRepository`: `RefreshToken` entity-ləri üçün əməliyyatlar təqdim edir.
- `ConfirmationTokenRepository`: `ConfirmationToken` (OTP) entity-ləri üçün əməliyyatlar təqdim edir.

### `security` Paketi

#### `jwt` Paketi

- `JwtUtils`: JWT tokenlərini yaratmaq, təsdiqləmək və onlardan məlumat çıxarmaq üçün köməkçi metodları saxlayır.
    - `generateTokenFromUsername()`: Access token yaradır.
    - `createRefreshToken()`: Refresh token yaradır və verilənlər bazasına yazır.
    - `validateJwtToken()`: Tokenin imzasının və bitmə tarixinin etibarlı olub-olmadığını yoxlayır.
    - `getUserFinFromJwtToken()`: Tokenin "subject" hissəsindən istifadəçinin FIN-ini çıxarır.
- `AuthTokenFilter`: Hər bir HTTP sorğusunu yoxlayan bir filtrdir. Sorğunun `Authorization` başlığından "Bearer " tokenini çıxarır, onu `JwtUtils` ilə təsdiqləyir və etibarlıdırsa, istifadəçinin autentifikasiya məlumatlarını `SecurityContextHolder`-a yerləşdirir. Bu, həmin sorğu üçün istifadəçinin autentifikasiya olunmuş hesab edilməsini təmin edir.
- `AuthEntryPointJwt`: Autentifikasiya olunmamış bir istifadəçi qorunan bir endpointə daxil olmaq istədikdə (401 Unauthorized xətası) işə düşür və xəta mesajı qaytarır.

#### `services` Paketi

- `UserDetailsServiceImpl`: `UserDetailsService` interfeysini implementasiya edir. Spring Security autentifikasiya zamanı istifadəçini tapmaq üçün bu servisi çağırır.
    - `loadUserByUsername(String identifier)`: Verilən `identifier`-ə (FIN, email və ya username) görə istifadəçini verilənlər bazasından tapır və onu Spring Security-nin başa düşdüyü `UserDetails` obyektinə (`UserDetailsImpl`) çevirir.
- `UserDetailsImpl`: `UserDetails` interfeysini implementasiya edən və istifadəçi haqqında əsas məlumatları (ID, FIN, parol, rollar) saxlayan bir sinifdir.

### `service` Paketi

#### `AuthService.java`

Bu, tətbiqin əsas biznes məntiqini saxlayan ən vacib servisdir.

- **Qeydiyyat:** `registerUser()` metodu yeni istifadəçinin məlumatlarını yoxlayır, şifrəsini kodlayır, verilənlər bazasına yazır və hesab təsdiqləmə prosesini başlatmaq üçün Kafka-ya event göndərir.
- **OTP:** `sendOtp()`, `verifyOtp()` metodları OTP-lərin yaradılması, göndərilməsi və təsdiqlənməsi prosesini idarə edir.
- **Autentifikasiya:** `authenticateUser()` metodu `AuthenticationManager`-dən istifadə edərək istifadəçinin daxil etdiyi məlumatları yoxlayır. Uğurlu olarsa, `JwtUtils` vasitəsilə access və refresh tokenlər yaradır və onları `AuthResponse` içində qaytarır.
- **Token Yeniləmə:** `refreshAccessToken()` metodu verilən refresh tokenin etibarlılığını yoxlayır və yeni bir access token yaradır.
- **Çıxış:** `logoutUser()` metodu istifadəçiyə aid bütün refresh tokenləri verilənlər bazasından silir.
- **Şifrə Sıfırlama:** `resetPassword()` metodu təsdiqlənmiş OTP-yə əsasən istifadəçinin şifrəsini yeniləyir.

#### `EmailService.java`

Bu servis e-poçt göndərmək üçün istifadə olunur.

- `@Async("taskExecutor")`: `sendEmail()` metodu asinxron olaraq işləyir. Bu o deməkdir ki, e-poçt göndərmə prosesi əsas axını gözlətmir və tətbiqin performansını artırır.
- `JavaMailSender`: E-poçtları göndərmək üçün Spring-in təqdim etdiyi standart vasitədir.

### `validation` Paketi

Bu paket xüsusi validasiya qaydalarını tətbiq etmək üçün istifadə olunur.

- `@PasswordMatches`: `SignupRequest`-də `password` və `confirmPassword` sahələrinin eyni olmasını yoxlayan bir annotasiyadır.
- `PasswordMatchesValidator`: `@PasswordMatches` annotasiyasının məntiqini həyata keçirən validatordur.
- `@OneOfFieldsNotBlank`: `SignupRequest`-də `email` və ya `phone` sahələrindən ən azı birinin doldurulmasını tələb edən annotasiyadır.
- `OneOfFieldsNotBlankValidator`: `@OneOfFieldsNotBlank` annotasiyasının məntiqini həyata keçirir.

---

## Verilənlər Bazasının Sxemi

Bu mikroxidmət aşağıdakı əsas cədvəllərdən istifadə edir:

- **`skill_auth_users`**: İstifadəçilərin əsas məlumatlarını saxlayır.
  - `id` (UUID, Primary Key)
  - `username` (String, Unique)
  - `fin` (String, Unique)
  - `password` (String)
  - `email` (String, Unique)
  - `phone` (String, Unique)
  - `role` (String)
  - `enabled` (boolean) - Hesabın aktiv olub-olmadığı.
  - `created_at` (Timestamp)
  - `updated_at` (Timestamp)
  - `failed_login_attempts` (Integer)

- **`refresh_tokens`**: Refresh tokenləri saxlayır.
  - `id` (UUID, Primary Key)
  - `user_id` (Foreign Key to `skill_auth_users`)
  - `token` (String, Unique)
  - `expiry_date` (Timestamp)
  - `ip_address` (String)
  - `user_agent` (String)

- **`confirmation_tokens`**: OTP kodlarını və hesab təsdiqləmə tokenlərini saxlayır.
  - `id` (UUID, Primary Key)
  - `user_id` (Foreign Key to `skill_auth_users`)
  - `token` (String, Unique) - OTP kodu.
  - `expires_at` (Timestamp)
  - `confirmed_at` (Timestamp)
  - `used` (boolean)
  - `type` (String) - `ACCOUNT_CONFIRMATION` və ya `PASSWORD_RESET`.

---

## Necə İşə Salmaq

1.  **Ön Tələblər:**
    - Java 17 və ya daha yuxarı
    - PostgreSQL verilənlər bazası
    - Apache Kafka və Zookeeper
    - Eureka Discovery Server (əgər mikroxidmət arxitekturasında işləyirsə)

2.  **Konfiqurasiya:**
    - `application.yml` faylında verilənlər bazası, Kafka, Eureka və JWT parametrlərini öz mühitinizə uyğun olaraq tənzimləyin.
      ```yaml
      spring:
        datasource:
          url: jdbc:postgresql://localhost:5432/authdb
          username: your_username
          password: your_password
        jpa:
          hibernate:
            ddl-auto: update
        kafka:
          bootstrap-servers: localhost:9092
      
      jwt:
        secret: # Base64 formatında güclü bir gizli açar
        expiration:
          ms: 86400000 # 1 gün
        refresh-expiration:
          ms: 604800000 # 7 gün
      
      otp:
        expiration-seconds: 300 # 5 dəqiqə
      ```

3.  **Qurmaq və İşə Salmaq:**
    - Layihənin kök qovluğunda terminalı açın.
    - Layihəni qurmaq üçün aşağıdakı Gradle əmrini icra edin:
      ```bash
      ./gradlew build
      ```
    - Tətbiqi işə salmaq üçün:
      ```bash
      java -jar build/libs/authService-0.0.1-SNAPSHOT.jar
      ```

4.  **API Sənədləşdirməsi:**
    - Tətbiq işə düşdükdən sonra Swagger UI-a aşağıdakı URL vasitəsilə daxil ola bilərsiniz:
      [http://localhost:1110/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) (Port `application.yml`-də təyin olunan port olmalıdır).
