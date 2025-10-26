
# `SkillUserService` Mikroxidmətinin Dərinlemesine Kodu İzahı

Bu sənəd, `SkillUserService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, istifadəçilərin profillərini (bioqrafiyalarını) və bacarıqlarını idarə edən bu mərkəzi xidmətin arxitekturasını, digər xidmətlərlə əlaqəsini və məlumatların idarə olunması məntiqini tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `SkillUserServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/skilluserservice/SkillUserServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir.

```java
package com.example.skilluserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SkillUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillUserServiceApplication.class, args);
    }

}
```

### Sətir-Sətir İzahı

- **`@SpringBootApplication`**: Tətbiqin bir Spring Boot tətbiqi olduğunu bildirir.
- **`@EnableDiscoveryClient`**: Bu tətbiqin Eureka kimi bir "Discovery Service"-ə özünü qeydiyyatdan keçirməsini təmin edir.
- **`@EnableFeignClients`**: Tətbiqdə `AuthClient` kimi `@FeignClient` interfeyslərinin axtarılıb tapılmasını və onlar üçün implementasiyaların yaradılmasını aktivləşdirir.
- **`main` metodu**: Tətbiqi işə salan standart Java giriş nöqtəsi.

---

## 2. `client` Paketi

Bu paket, digər mikroxidmətlərlə əlaqə qurmaq üçün istifadə olunan `OpenFeign` klient interfeyslərini saxlayır.

### 2.1. `AuthClient.java`

**Fayl Yolu:** `src/main/java/com/example/skilluserservice/client/AuthClient.java`

Bu interfeys, `AuthService`-dən istifadəçinin mövcudluğunu və rolunu yoxlamaq üçün istifadə olunur.

```java
package com.example.skilluserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${auth-service.url}")
public interface AuthClient {

    @GetMapping("/api/auth/{authUserId}/exists")
    Boolean doesUserExist(@PathVariable("authUserId") UUID authUserId);

    @GetMapping("/api/auth/{authUserId}/role")
    String getUserRole(@PathVariable("authUserId") UUID authUserId);

}
```

#### Sətir-Sətir İzahı

- **`@FeignClient(name = "auth-service", url = "${auth-service.url}")`**: Bu interfeysin bir Feign klienti olduğunu və `auth-service` adlı xidmətə qoşulacağını bildirir. Xidmətin konkret ünvanı `application.yml`-dən `${auth-service.url}` dəyəri ilə oxunur.
- **`doesUserExist(...)` metodu**: `AuthService`-in `/api/auth/{authUserId}/exists` endpointinə `GET` sorğusu göndərərək verilmiş `authUserId`-yə malik bir istifadəçinin mövcud olub-olmadığını yoxlayır və `Boolean` nəticə qaytarır.
- **`getUserRole(...)` metodu**: `AuthService`-in `/api/auth/{authUserId}/role` endpointinə `GET` sorğusu göndərərək verilmiş `authUserId`-yə malik istifadəçinin rolunu (`String` olaraq) qaytarır.

---

## 3. `controller` Paketi

Bu paket, istifadəçi profilləri (`UserBio`) və bacarıqlar (`Skill`) üçün CRUD (Create, Read, Update, Delete) əməliyyatlarını təmin edən REST API endpointlərini saxlayır.

### 3.1. `UserBioController.java`

**Fayl Yolu:** `src/main/java/com/example/skilluserservice/controller/UserBioController.java`

Bu controller, istifadəçi profillərinin idarə olunması üçün endpointləri təmin edir.

```java
@RestController
@RequestMapping("/api/user-bios")
@RequiredArgsConstructor
public class UserBioController {

    private final UserBioService userBioService;

    @PostMapping
    public ResponseEntity<UserBioResponseDTO> createUserBio(
            @RequestHeader("X-Auth-User-Id") UUID authUserId,
            @Valid @RequestBody UserBioCreateDto userBioCreateDto) { ... }

    @GetMapping("/me")
    public ResponseEntity<UserBioResponseDTO> getMyBio(@RequestHeader("X-Auth-User-Id") UUID authUserId) { ... }

    @PutMapping
    public ResponseEntity<UserBioResponseDTO> updateUserBio(
            @RequestHeader("X-Auth-User-Id") UUID authUserId,
            @Valid @RequestBody UserBioUpdateDTO userBioUpdateDTO) { ... }

    // ... digər endpointlər
}
```

#### Sətir-Sətir İzahı (Seçilmiş Metodlar)

- **`@RequestHeader("X-Auth-User-Id") UUID authUserId`**: Bu annotasiya, bütün əsas endpointlərdə istifadə olunur. O, sorğunun başlığından API Gateway tərəfindən əlavə edilmiş `X-Auth-User-Id` dəyərini oxuyur. Bu, hansı istifadəçinin adından əməliyyat aparıldığını müəyyən etmək üçün təhlükəsiz bir yoldur və servis qatında avtorizasiya yoxlamaları üçün istifadə olunur.
- **`createUserBio(...)` metodu**: Yeni bir profil yaradır. Servisə həm başlıqdan gələn təsdiqlənmiş `authUserId`-ni, həm də sorğunun body-sindən gələn DTO-nu ötürür.
- **`getMyBio(...)` metodu**: İstifadəçinin öz profilinə baxması üçün nəzərdə tutulub. Başlıqdan `authUserId`-ni alaraq `userBioService.getUserBioByAuthUserId` metodunu çağırır.
- **`updateUserBio(...)` metodu**: Mövcud profili yeniləyir. Servis qatında, bu əməliyyatı edən istifadəçinin (`authUserId` başlığından) yeniləmək istədiyi profilin həqiqi sahibi olub-olmadığı yoxlanılır.
- **`getUserBioByAuthUserId(...)` metodu**: Digər mikroxidmətlərin (`ChatService`, `SkillSwapService`) bir istifadəçinin profil məlumatlarını `authUserId`-yə görə əldə etməsi üçün istifadə etdiyi əsas endpoint.

### 3.2. `SkillController.java`

**Fayl Yolu:** `src/main/java/com/example/skilluserservice/controller/SkillController.java`

Bu controller, platformadakı bütün bacarıqların mərkəzləşdirilmiş reyestrini idarə etmək üçün standart CRUD endpointlərini təmin edir.

---

## 4. `entity` və `enumeration` Paketləri

### 4.1. `UserBio.java` (Entity)

- **Məqsədi:** İstifadəçinin profil məlumatlarını saxlayan əsas JPA entity-si.
- **`@Column(nullable = false, unique = true) private UUID authUserId;`**: Bu sahə, bu profilin `AuthService`-dəki hansı istifadəçiyə aid olduğunu göstərən "foreign key" rolunu oynayır. `unique = true` məhdudiyyəti bir `authUserId`-yə yalnız bir profilin bağlı olmasını təmin edir.
- **`@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)`**: `UserBio` ilə `Skill` arasında "birə-çox" əlaqəni təyin edir. Bir profilin bir neçə bacarığı ola bilər.
    - **`cascade = CascadeType.ALL`**: `UserBio` obyekti üzərində aparılan bütün əməliyyatların (yaratma, yeniləmə, silmə) onunla əlaqəli `Skill` obyektlərinə də təsir etməsini təmin edir. Məsələn, bir `UserBio` silindikdə, ona aid olan bütün bacarıqlar da avtomatik silinəcək.
    - **`fetch = FetchType.LAZY`**: Performans üçün vacibdir. `UserBio` obyekti verilənlər bazasından yüklənərkən, onun `skills` siyahısı dərhal yüklənmir. Bu siyahıya yalnız proqramda birbaşa müraciət edildikdə (`userBio.getSkills()`) əlavə bir sorğu ilə yüklənir.

### 4.2. `Skill.java` (Entity) və `SkillLevel.java` (Enum)

- **`Skill.java`**: Bir bacarığın adını, təsvirini və səviyyəsini (`SkillLevel` enum) saxlayan sadə bir JPA entity-si.
- **`SkillLevel.java`**: Bacarıq səviyyələrini təmsil edən `enum` (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`).

---

## 5. `mapper` Paketi (MapStruct)

Bu paket, DTO və Entity sinifləri arasında çevirmələri avtomatlaşdırır.

- **`UserBioMapper.java`**: `UserBio` ilə əlaqəli DTO-lar və entity arasında çevirmələri həyata keçirir.
    - **`@Mapper(componentModel = "spring", uses = {SkillMapper.class})`**: `uses = {SkillMapper.class}` atributu, `UserBioMapper`-ə `UserBio`-nun içindəki `Set<Skill>` sahəsini çevirərkən `SkillMapper`-dən istifadə etməsini deyir. Bu, mapper-lərin bir-birini çağırmasına imkan verir.
    - **`@Mapping(target = "skills", ignore = true)`**: `toEntity` və `updateEntityFromDto` metodlarında bu annotasiya, bacarıqların birbaşa DTO-dan kopyalanmasının qarşısını alır. Çünki bacarıqlar sadəcə ID-lərlə gəlir və servis qatında `SkillRepository`-dən ayrıca tapılıb entity-yə əlavə edilməlidir.
- **`SkillMapper.java`**: `Skill` entity-si və onun DTO-ları arasında sadə çevirmələri həyata keçirir.

---

## 6. `service` Paketi

Bu paket, xidmətin əsas biznes məntiqini həyata keçirən interfeysləri və onların implementasiyalarını saxlayır.

### 6.1. `UserBioServiceImpl.java`

**Fayl Yolu:** `src/main/java/com/example/skilluserservice/service/impl/UserBioServiceImpl.java`

Bu, `UserBioService` interfeysini implementasiya edən və profil idarəetməsinin bütün məntiqini özündə cəmləşdirən əsas servis sinifidir.

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBioServiceImpl implements UserBioService {

    private final UserBioRepository userBioRepository;
    private final SkillRepository skillRepository;
    private final UserBioMapper userBioMapper;
    private final AuthClient authClient;

    @Override
    @Transactional
    public UserBioResponseDTO createUserBio(UUID authUserId, UserBioCreateDto userBioCreateDto) { ... }

    @Override
    @Transactional
    public UserBioResponseDTO updateUserBio(UUID authUserId, UserBioUpdateDTO updateDTO) { ... }

    private Set<Skill> mapSkills(Set<Long> skillIds) { ... }
}
```

#### Metodların Detallı İzahı

- **`createUserBio(UUID authUserId, UserBioCreateDto userBioCreateDto)` metodu**:
    1.  **`userBioRepository.findByAuthUserId(authUserId).isPresent()`**: Bu `authUserId` ilə artıq bir profilin olub-olmadığını yoxlayır.
    2.  **`authClient.doesUserExist(authUserId)`**: `AuthService`-ə Feign zəngi edərək istifadəçinin autentifikasiya sistemində mövcud olduğunu təsdiqləyir.
    3.  **`authClient.getUserRole(authUserId)`**: Eyni şəkildə, istifadəçinin rolunu yoxlayır və yalnız `USER` roluna sahib olanların davam etməsinə icazə verir.
    4.  **`UserBio userBio = userBioMapper.toEntity(userBioCreateDto);`**: DTO-nu entity-yə çevirir.
    5.  **`userBio.setAuthUserId(authUserId);`**: Təhlükəsizlik üçün, DTO-dan gələn `authUserId`-yə etibar etmir, birbaşa sorğu başlığından gələn və təsdiqlənmiş `authUserId`-ni təyin edir.
    6.  **`Set<Skill> skills = mapSkills(userBioCreateDto.getSkillIds());`**: `mapSkills` köməkçi metodunu çağırır.
    7.  **`userBio.setSkills(skills);`**: Verilənlər bazasından tapılmış `Skill` entity-lərini `UserBio` obyektinə əlavə edir.
    8.  **`userBioRepository.save(userBio)`**: Hazırlanmış obyekti verilənlər bazasına yazır.

- **`updateUserBio(UUID authUserId, UserBioUpdateDTO updateDTO)` metodu**:
    1.  **`UserBio existing = userBioRepository.findById(...)`**: Yenilənmək istənən profili ID-sinə görə tapır.
    2.  **`if (!existing.getAuthUserId().equals(authUserId))`**: **Avtorizasiya yoxlaması.** Sorğunu göndərən istifadəçinin (`authUserId`) yeniləmək istədiyi profilin sahibi olub-olmadığını yoxlayır. Əgər sahibi deyilsə, `InvalidInputException` ataraq icazəsiz cəhdin qarşısını alır.
    3.  **`userBioMapper.updateEntityFromDto(updateDTO, existing);`**: MapStruct-un `update` metodundan istifadə edərək DTO-dakı yeni dəyərləri mövcud `existing` entity obyektinin üzərinə yazır. Bu, yeni bir obyekt yaratmaq əvəzinə mövcud obyekti yeniləyir.
    4.  Bacarıqları yeniləyir və obyekti verilənlər bazasına yazır.

- **`mapSkills(Set<Long> skillIds)` metodu**:
    - Bu privat metod, DTO-dan gələn bacarıq ID-ləri siyahısını qəbul edir.
    - `skillRepository.findAllById(skillIds)` ilə bütün bu ID-lərə uyğun `Skill` obyektlərini verilənlər bazasından bir sorğuda yükləyir.
    - **`if (skills.size() != skillIds.size())`**: Əgər verilənlər bazasından qayıdan bacarıqların sayı DTO-dan gələn ID-lərin sayından azdırsa, bu o deməkdir ki, göndərilən ID-lərdən bəziləri səhvdir (mövcud deyil). Bu halda, `InvalidInputException` atılır.
    - Nəticə olaraq, tapılmış `Skill` entity-lərindən ibarət bir `Set` qaytarır.

---

**`SkillUserService` üçün Kod İzahının Sonu.**
