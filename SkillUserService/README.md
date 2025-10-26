
# SkillUserService Mikroxidməti

## 1. Giriş və Məqsəd

`SkillUserService`, **SkillSwap** platformasında istifadəçilərin profillərini, bioqrafiyalarını və bacarıqlarını idarə etmək üçün nəzərdə tutulmuş mərkəzi mikroxidmətdir. Bu xidmət, `AuthService`-də yaradılan autentifikasiya qeydlərini tamamlayaraq, hər bir istifadəçi üçün daha zəngin və detallı bir profil təqdim edir.

Bu xidmətin əsas məqsədi, istifadəçi ilə bağlı bütün qeyri-autentikasiya məlumatlarını (ad, soyad, təhsil, iş təcrübəsi, bacarıqlar və s.) bir yerdə cəmləşdirmək və digər mikroxidmətlər (`ChatService`, `SkillSwapService` və s.) üçün etibarlı bir "məlumat mənbəyi" (Source of Truth) rolunu oynamaqdır.

`SkillUserService` aşağıdakı əsas funksionallıqları təmin edir:

- **İstifadəçi Bioqrafiyasının (Profil) İdarə Olunması:** İstifadəçilərə öz profillərini yaratmaq, yeniləmək və silmək imkanı verir.
- **Bacarıqların (Skills) İdarə Olunması:** Platformada mövcud olan bacarıqların mərkəzləşdirilmiş reyestrini idarə edir (CRUD əməliyyatları).
- **Profili Bacarıqlarla Əlaqələndirmək:** İstifadəçilərə mövcud bacarıqları öz profillərinə əlavə etməyə imkan verir.
- **Məlumatların Təqdim Edilməsi:** Digər mikroxidmətlərin sorğularına cavab olaraq istifadəçi və bacarıq məlumatlarını təhlükəsiz və strukturlaşdırılmış şəkildə (DTO-lar vasitəsilə) təqdim edir.
- **Xidmətlərarası Kommunikasiya:** İstifadəçi profilini yaradarkən və ya yeniləyərkən, istifadəçinin həqiqətən mövcud olduğunu və lazımi səlahiyyətlərə sahib olduğunu yoxlamaq üçün `AuthService` ilə əlaqə saxlayır.

---

## 2. Texnologiya Stackı

| Texnologiya | Versiya/Növ | Təyinatı və Seçim Səbəbi |
| :--- | :--- | :--- |
| **Java** | 17 | Platformanın əsas proqramlaşdırma dili. |
| **Spring Boot** | 3.x | Tətbiqin sürətli qurulması və konfiqurasiyası üçün əsas freymvork. |
| **Spring Data JPA** | - | İstifadəçi profilləri və bacarıqlar kimi relyasiyalı məlumatları idarə etmək üçün PostgreSQL ilə əlaqəni təmin edir. |
| **PostgreSQL** | - | İstifadəçi profilləri və bacarıqlar üçün əsas və etibarlı verilənlər bazası. |
| **Spring Cloud OpenFeign**| - | `AuthService` kimi digər mikroxidmətlərə deklarativ və asan şəkildə REST API zəngləri etmək üçün istifadə olunur. |
| **MapStruct** | - | DTO (Data Transfer Object) və JPA Entity sinifləri arasında çevirmələri avtomatlaşdırır, boilerplate kodu azaldır və performansı artırır. |
| **Spring Security** | - | REST API endpointlərini qorumaq üçün istifadə olunur. |
| **Swagger (OpenAPI 3)** | - | REST API-lərin sənədləşdirilməsi və asan test edilməsi üçün istifadə olunur. |
| **Docker** | - | `docker-compose.yml` vasitəsilə asılılıqların (PostgreSQL) təcrid olunmuş mühitdə sürətli qurulmasını təmin edir. |

---

## 3. Arxitektura və Məntiq Axını

### 3.1. İstifadəçi Bioqrafiyasının Yaradılması Axını

Bu proses istifadəçinin autentifikasiya qeydini (`AuthService`-də) zəngin profil məlumatları ilə tamamlamaq üçün kritik əhəmiyyət daşıyır.

1.  **REST Sorğusu:** İstifadəçi öz profilini yaratmaq üçün `POST /api/user-bios` endpointinə `UserBioCreateDto` ilə sorğu göndərir. Təhlükəsizlik məqsədilə, sorğunun başlığında API Gateway tərəfindən əlavə edilmiş `X-Auth-User-Id` başlığı olmalıdır.
2.  **Controller:** `UserBioController` sorğunu qəbul edir və həm DTO-nu, həm də başlıqdan gələn `authUserId`-ni `UserBioService`-in `createUserBio` metoduna ötürür.
3.  **Servis Məntiqi və Validasiya:** `UserBioService` aşağıdakı yoxlamaları aparır:
    a.  **Dublikat Yoxlaması:** `userBioRepository.findByAuthUserId()` metodu ilə bu `authUserId`-yə aid bir profilin artıq mövcud olub-olmadığını yoxlayır. Əgər varsa, `InvalidInputException` atılır (bir istifadəçinin yalnız bir profili ola bilər).
    b.  **Xidmətlərarası Validasiya (Feign Client):** `authClient.doesUserExist()` metodunu çağıraraq `AuthService`-ə sorğu göndərir və bu `authUserId`-yə malik bir istifadəçinin həqiqətən mövcud olduğunu təsdiqləyir. Mövcud deyilsə, `ResourceNotFoundException` atılır.
    c.  **Rol Yoxlaması:** `authClient.getUserRole()` metodunu çağıraraq istifadəçinin rolunu yoxlayır. Yalnız `USER` roluna sahib istifadəçilərin profil yaratmasına icazə verilir. Bu, məsələn, `ADMIN` və ya digər sistem rollarının profil yaratmasının qarşısını alır.
4.  **Məlumatların Hazırlanması:**
    a.  `UserBioMapper` vasitəsilə DTO `UserBio` entity-sinə çevrilir.
    b.  `mapSkills()` köməkçi metodu çağırılır. Bu metod, DTO-dan gələn bacarıq ID-ləri (`skillIds`) əsasında `SkillRepository`-dən müvafiq `Skill` entity-lərini tapır və bir `Set` olaraq qaytarır. Əgər hər hansı bir ID səhvdirsə, `InvalidInputException` atılır.
    c.  Tapılmış bacarıqlar `UserBio` obyektinə əlavə edilir.
5.  **Verilənlər Bazasında Saxlama:** Hazırlanmış `UserBio` obyekti `userBioRepository.save()` metodu ilə PostgreSQL verilənlər bazasına yazılır.
6.  **Cavabın Qaytarılması:** Saxlanmış `UserBio` entity-si yenidən `UserBioMapper` vasitəsilə `UserBioResponseDTO`-ya çevrilir və klientə `201 CREATED` statusu ilə qaytarılır.

### 3.2. Təhlükəsiz Yeniləmə və Silmə Axını

Sistemin təhlükəsizliyi üçün kritik tələblərdən biri, istifadəçilərin yalnız öz profillərini dəyişə bilməsini təmin etməkdir.

1.  **REST Sorğusu:** İstifadəçi öz profilini yeniləmək üçün `PUT /api/user-bios` endpointinə `UserBioUpdateDTO` ilə sorğu göndərir. Sorğunun başlığında yenə də `X-Auth-User-Id` göndərilir.
2.  **Controller:** `UserBioController` sorğunu qəbul edir və məlumatları `userBioService.updateUserBio` metoduna ötürür.
3.  **Servis Məntiqi və Avtorizasiya:** `UserBioService`:
    a.  Əvvəlcə, yenilənmək istənən profilin (`updateDTO.getId()` ilə) verilənlər bazasında mövcud olub-olmadığını yoxlayır.
    b.  **Ən Vacib Addım:** Verilənlər bazasından tapılan `existing` profilin `authUserId`-si ilə sorğunun başlığından gələn `authUserId`-ni müqayisə edir (`!existing.getAuthUserId().equals(authUserId)`).
    c.  Əgər bu ID-lər fərqlidirsə, bu, bir istifadəçinin başqasının profilini yeniləməyə cəhd etdiyini göstərir. Bu halda, proses dərhal dayandırılır, xəta loglanır və `InvalidInputException` ("You are not authorized to update this user bio.") atılır.
4.  **Yeniləmə:** Avtorizasiya uğurlu olarsa, `UserBioMapper.updateEntityFromDto()` metodu çağırılır. Bu metod, DTO-dakı yeni məlumatları mövcud entity obyektinin üzərinə yazır. Bacarıqlar da eyni şəkildə yenilənir.
5.  **Nəticə:** Yenilənmiş entity verilənlər bazasına yazılır və nəticə DTO olaraq klientə qaytarılır.

Silmə əməliyyatı (`deleteMyBio`) da eyni avtorizasiya məntiqi ilə işləyir.

---

## 4. Layihənin Detallı Strukturu

### `service` Paketi

- **`UserBioServiceImpl`**: `UserBio` ilə bağlı bütün biznes məntiqini həyata keçirən əsas servis. Xidmətlərarası yoxlamalar (`AuthClient` vasitəsilə), avtorizasiya məntiqi və repozitori ilə əlaqə burada cəmləşib.
- **`SkillServiceImpl`**: `Skill` entity-ləri üçün standart CRUD (Create, Read, Update, Delete) əməliyyatlarını təmin edir. Bu servis daha çox məlumatların idarə olunması (data management) funksiyasını daşıyır.

### `controller` Paketi

- **`UserBioController`**: İstifadəçi profilləri ilə bağlı bütün REST API endpointlərini təmin edir. Fərqli ehtiyaclar üçün müxtəlif endpointlər mövcuddur:
    - `POST /`: Yeni profil yaratmaq.
    - `GET /me`: Autentifikasiya olunmuş istifadəçinin öz profilini əldə etməsi üçün (`@RequestHeader` istifadə edir).
    - `GET /{id}`: Daxili DB ID-si ilə profil axtarmaq (məsələn, admin paneli üçün).
    - `GET /auth-user/{authUserId}`: `AuthService`-dən gələn UUID ilə profil axtarmaq (digər mikroxidmətlərin istifadəsi üçün).
    - `PUT /`: İstifadəçinin öz profilini yeniləməsi.
    - `DELETE /me`: İstifadəçinin öz profilini silməsi.
- **`SkillController`**: Bacarıqların idarə olunması üçün CRUD endpointlərini təmin edir.

### `mapper` Paketi (MapStruct)

- **`UserBioMapper`**: `UserBioCreateDto`, `UserBioUpdateDTO`, `UserBio` və `UserBioResponseDTO` arasında kompleks çevirmələri həyata keçirir. `uses = {SkillMapper.class}` annotasiyası sayəsində `UserBio`-nun içindəki `Set<Skill>`-i `Set<SkillResponseDto>`-ya çevirərkən `SkillMapper`-dən istifadə edir.
- **`SkillMapper`**: `Skill` entity-si və onun DTO-ları arasında çevirmələri həyata keçirir.

### `client` Paketi (Feign)

- **`AuthClient`**: `AuthService` mikroxidməti ilə əlaqə qurmaq üçün istifadə olunan deklarativ Feign interfeysi. `doesUserExist` və `getUserRole` metodları vasitəsilə `AuthService`-in müvafiq endpointlərinə şəffaf şəkildə zənglər edir.

### `exception` Paketi

- **`GlobalExceptionHandler`**: `@ControllerAdvice` ilə bütün controllerlərdə yaranan xətaları tutur. `ResourceNotFoundException` (404), `InvalidInputException` (400) və `MethodArgumentNotValidException` (validasiya xətaları, 400) üçün xüsusi `ExceptionHandler`-lar təyin edilib. Bu, bütün xətaların standartlaşdırılmış `ErrorDetails` formatında qaytarılmasını təmin edir.

---

## 5. API Sənədləşdirməsi

Bu xidmətin bütün endpointləri `Swagger UI` vasitəsilə sənədləşdirilib. Tətbiq işə düşdükdən sonra aşağıdakı ünvandan API sənədləşdirməsinə baxa bilərsiniz:

- **Swagger UI:** `http://<host>:<port>/swagger-ui/index.html`

**Əsas Endpointlər:**

- **UserBio:**
  - `POST /api/user-bios`: Yeni istifadəçi profili yaradır.
  - `GET /api/user-bios/me`: Cari istifadəçinin profilini qaytarır.
  - `PUT /api/user-bios`: Cari istifadəçinin profilini yeniləyir.
  - `GET /api/user-bios/auth-user/{authUserId}`: Verilmiş `authUserId` ilə profili qaytarır.
- **Skill:**
  - `POST /api/skills`: Yeni bacarıq yaradır.
  - `GET /api/skills`: Bütün bacarıqları siyahalayır.
  - `GET /api/skills/{id}`: ID ilə tək bir bacarığı qaytarır.

---

## 6. Verilənlər Bazasının Sxemi (PostgreSQL)

- **`user_bio` cədvəli:**
  - `id` (BIGINT, Primary Key)
  - `auth_user_id` (UUID, Unique, Not Null) - `AuthService`-dəki istifadəçi ilə əlaqə.
  - `first_name`, `last_name`, `education`, `phone`, `job_title`, `years_of_experience`, `linked_in_profile_url`, `bio`.

- **`skill` cədvəli:**
  - `id` (BIGINT, Primary Key)
  - `name`, `description`, `level`.
  - `user_bio_id` (BIGINT, Foreign Key) - `user_bio` cədvəlinə istinad edir. Bu, bir profilin bir neçə bacarığa sahib ola biləcəyini göstərən `@OneToMany` əlaqəsini yaradır.

---

## 7. Quraşdırma və İşə Salma

1.  **Ön Tələblər:**
    - Java 17+
    - Docker və Docker Compose

2.  **Konfiqurasiya (`application.yml`):**
    - `spring.datasource.url/username/password`: PostgreSQL verilənlər bazası üçün bağlantı parametrləri.
    - `auth-service.url`: `AuthService`-in tam ünvanı (məsələn, `http://localhost:8081`).

3.  **Asılılıqların İşə Salınması (Docker ilə):**
    - Layihənin kökündəki `docker-compose.yml` faylı PostgreSQL xidmətini sizin üçün qurur.
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
      java -jar build/libs/skilluserservice-0.0.1-SNAPSHOT.jar
      ```
