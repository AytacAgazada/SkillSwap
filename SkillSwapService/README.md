
# SkillSwapService Mikroxidməti

## 1. Giriş və Məqsəd

`SkillSwapService`, **SkillSwap** platformasının ürəyi və əsas biznes məntiqini idarə edən mərkəzi mikroxidmətdir. Bu xidmət, istifadəçilərin öz bacarıqlarını təklif etməsi, başqalarının təklif etdiyi bacarıqları axtarması, uyğun təkliflər tapdıqda qarşılıqlı mübadilə (swap) prosesini başlatması və tamamlaması üçün lazım olan bütün funksionallığı təmin edir.

Xidmətin əsas məqsədləri aşağıdakılardır:

- **Təkliflərin İdarə Olunması:** İstifadəçilərə bacarıq mübadiləsi üçün təkliflər yaratmağa və öz təkliflərini görməyə imkan vermək.
- **Ağıllı və Sürətli Axtarış:** Təklifləri təkcə bacarıq adına görə deyil, həm də istifadəçinin coğrafi mövqeyinə əsasən müəyyən bir radius daxilində axtarmaq.
- **Uyğunlaşma (Matching) Prosesi:** Bir istifadəçinin digərinin təklifinə maraq göstərməsi, tələb göndərməsi və qarşı tərəfin bu tələfi qəbul etməsi prosesini idarə etmək.
- **Mübadilənin Həyat Dövrü (Lifecycle):** Bir mübadilənin `REQUESTED` statusundan başlayaraq `ACCEPTED`, `IN_PROGRESS` və nəhayət `COMPLETED` və ya `CANCELLED` statuslarına qədər olan bütün mərhələlərini izləmək.
- **Hadisə-Yönümlü İnteqrasiya:** Mübadilə prosesindəki mühüm anlarda (tələb göndərildikdə, mübadilə tamamlandıqda, görüş yaxınlaşdıqda) digər mikroxidmətləri (məsələn, `NotificationService`) məlumatlandırmaq üçün Kafka-ya hadisələr (events) göndərmək.

Bu xidmət, polyglot persistence (həm SQL, həm də NoSQL verilənlər bazasından istifadə) prinsipini tətbiq edərək hər bir məlumat növü üçün ən uyğun texnologiyanı istifadə edir: transaksional məlumatlar üçün PostgreSQL, sürətli coğrafi axtarış üçün isə Elasticsearch.

---

## 2. Texnologiya Stackı

| Texnologiya | Versiya/Növ | Təyinatı və Seçim Səbəbi |
| :--- | :--- | :--- |
| **Java** | 17 | Platformanın əsas proqramlaşdırma dili. |
| **Spring Boot** | 3.x | Tətbiqin sürətli qurulması və konfiqurasiyası üçün əsas freymvork. |
| **Spring Data JPA** | - | Transaksional məlumatları (təkliflər, mübadilələr) idarə etmək üçün PostgreSQL ilə əlaqəni təmin edir. |
| **PostgreSQL** | - | Əsas və etibarlı məlumat mənbəyi (Source of Truth) kimi istifadə olunan relyasiyalı verilənlər bazası. |
| **Spring Data Elasticsearch** | - | Təkliflərin coğrafi məkan və bacarıq adına görə sürətli axtarışını təmin etmək üçün istifadə olunur. |
| **Elasticsearch** | - | Mətn axtarışı və coğrafi məkan sorğuları üçün optimallaşdırılmış, yüksək performanslı axtarış mühərriki. |
| **Apache Kafka** | - | Bu xidmətdə baş verən biznes hadisələrini digər mikroxidmətlərə asinxron şəkildə bildirmək üçün istifadə olunur. |
| **Spring Security** | - | REST API endpointlərini qorumaq və autentifikasiya tələb etmək üçün istifadə olunur. |
| **Docker** | - | `docker-compose.yml` vasitəsilə bütün asılılıqların (PostgreSQL, Elasticsearch, Kafka, Zookeeper) bir əmrlə işə salınmasını təmin edir. |

---

## 3. Arxitektura və Məntiq Axını

### 3.1. Təklif Yaratma Axını (Polyglot Persistence)

1.  **REST Sorğusu:** İstifadəçi `POST /api/skil-swaps/offers` endpointinə `CreateSwapOfferRequest` DTO-su ilə yeni bir təklif yaratmaq üçün sorğu göndərir. Sorğunun başlığında (`X-Auth-User-Id`) istifadəçinin kimliyi göndərilir.
2.  **Controller:** `SwapController` sorğunu qəbul edir və `SwapService`-in `createOffer` metodunu çağırır.
3.  **Service Məntiqi:** `SwapService`:
    a.  `OfferMapper` vasitəsilə gələn DTO-nu `SwapOfferEntity` (JPA entity) obyektinə çevirir.
    b.  `swapOfferRepository.save()` metodunu çağıraraq bu obyekti PostgreSQL verilənlər bazasına yazır. Bu, təklifin əsas və daimi qeydidir.
4.  **İndeksləmə:** `SwapService`, `matchingService.indexOffer()` metodunu çağırır.
5.  **Elasticsearch-ə Yazma:** `MatchingService`:
    a.  Yenicə yaradılmış `SwapOfferEntity`-ni `OfferMapper` vasitəsilə `GeoSwapOffer` (Elasticsearch sənədi) obyektinə çevirir. Bu zaman coğrafi koordinatlar `GeoPoint` tipinə salınır.
    b.  `geoSwapOfferRepository.save()` metodunu çağıraraq bu sənədi Elasticsearch-də indeksləyir.

Bu yanaşma sayəsində sistem həm PostgreSQL-in təmin etdiyi transaksional bütövlükdən, həm də Elasticsearch-in təmin etdiyi güclü axtarış imkanlarından faydalanır.

### 3.2. Coğrafi Axtarış Axını

1.  **REST Sorğusu:** İstifadəçi `GET /api/skil-swaps/offers/search` endpointinə axtardığı bacarıq (`skill`), öz mövqeyi (`lat`, `lon`) və axtarış radiusu (`radiusKm`) ilə sorğu göndərir.
2.  **Controller:** `SwapController` sorğunu qəbul edir və `SwapService`-in `searchOffers` metodunu çağırır.
3.  **Axtarışın Delegasiyası:** `SwapService` axtarış məntiqini həyata keçirmək üçün `matchingService.findMatches` metodunu çağırır.
4.  **Elasticsearch Sorğusu:** `MatchingService`:
    a.  Spring Data Elasticsearch-in `Criteria` API-sindən istifadə edərək mürəkkəb bir sorğu qurur. Bu sorğu üç şərti birləşdirir:
        i.  `skillRequested` sahəsi axtarılan bacarıqla eyni olmalıdır.
        ii. `location` sahəsi (`GeoPoint`) verilən mərkəz nöqtəsindən müəyyən bir `radiusKm` məsafəsi **daxilində** olmalıdır (`within` filteri).
        iii. `isActive` sahəsi `true` olmalıdır.
    b.  `elasticsearchOperations.search()` metodu ilə bu sorğunu Elasticsearch-ə göndərir.
5.  **Nəticələrin Emalı:** `SwapService` Elasticsearch-dən qayıdan `GeoSwapOffer` sənədlərinin ID-lərini alır. Sonra `swapOfferRepository.findAllById()` metodu ilə bu ID-lərə uyğun tam `SwapOfferEntity` obyektlərini PostgreSQL-dən (əsas məlumat mənbəyindən) yükləyir və nəticələri DTO-ya çevirərək klientə qaytarır.

### 3.3. Uyğunlaşma (Matching) və Həyat Dövrü

1.  **Tələbin Göndərilməsi:** İstifadəçi A, axtarış nəticəsində tapdığı və istifadəçi B-yə aid olan bir təklifə (`offerId`) maraq göstərir. `POST /api/skil-swaps/match/{offerId}` endpointinə sorğu göndərir.
2.  **Servis və Validasiya:** `SwapService`:
    a.  Təklifin mövcudluğunu yoxlayır.
    b.  Tələb göndərən istifadəçinin təklifin sahibi olmadığını yoxlayır (`SelfMatchException`).
3.  **Hadisənin Göndərilməsi:** `SwapEventPublisher.publishMatchRequestedEvent()` metodu çağırılır. Bu metod, `match-requested-events` topic-inə bir hadisə göndərir. `NotificationService` bu hadisəni dinləyərək təklifin sahibi olan istifadəçi B-yə ("Sizin təklifinizlə maraqlanan var") bildiriş göndərir.
4.  **Təsdiqləmə:** İstifadəçi B bildirişi alır və tələbi qəbul etmək qərarına gəlir. `POST /api/skil-swaps/{swapId}/accept` endpointinə görüş vaxtını (`meetingDateTime`) da daxil edərək sorğu göndərir.
5.  **Statusun Yenilənməsi:** `SwapService`, `swapRepository.updateStatusAndMeetingTime()` metodunu çağıraraq müvafiq `Swap` qeydinin statusunu `ACCEPTED`-ə dəyişir və görüş vaxtını təyin edir.
6.  **Görüş Xatırlatması (Planlanmış Tapşırıq):** `MeetingReminderTask`, `@Scheduled` annotasiyası sayəsində hər gün səhər saat 5-də işə düşür. Sabahkı günə təyin edilmiş bütün `ACCEPTED` statuslu `Swap`-ları tapır və hər biri üçün `meeting-reminder-events` topic-inə bir hadisə göndərir. `NotificationService` bu hadisəni tutaraq hər iki istifadəçiyə görüş haqqında xatırlatma e-poçtu göndərir.
7.  **Tamamlama:** Görüş baş tutduqdan sonra istifadəçilərdən biri `POST /api/skil-swaps/complete/{swapId}` endpointinə sorğu göndərərək mübadilənin tamamlandığını bildirir.
8.  **Son Hadisə:** `SwapService` `Swap` statusunu `COMPLETED`-ə dəyişir və `SwapEventPublisher` vasitəsilə `swap-completed-events` topic-inə hadisə göndərir. Bu hadisə, məsələn, `BadgeService` tərəfindən dinlənilərək istifadəçilərə yeni "badge"-lər vermək üçün istifadə oluna bilər.

---

## 4. Layihənin Detallı Strukturu

### `service` Paketi

- **`SwapService`**: Əsas biznes məntiqini idarə edən orkestrator servis. Controller-dən gələn sorğuları qəbul edir və digər servislərə (MatchingService, SwapEventPublisher) və repozitorilərə delegasiya edir.
- **`MatchingService`**: Elasticsearch ilə bağlı bütün məntiqi özündə saxlayır. Təkliflərin indekslənməsi və mürəkkəb coğrafi axtarış sorğularının qurulması və icrası ilə məşğul olur.
- **`SwapEventPublisher`**: Kafka ilə kommunikasiya üçün məsuliyyət daşıyan sinif. Müxtəlif biznes hadisələrini müvafiq Kafka topic-lərinə göndərir.
- **`MeetingReminderTask`**: Spring-in `@Scheduled` funksionallığından istifadə edərək periodik olaraq işə düşən və yaxınlaşan görüşlər üçün xatırlatma hadisələri yaradan sinif.

### `entity` və `repository` Paketləri

- **`SwapOfferEntity` (JPA) / `SwapOfferRepository` (JPA)**: Təkliflərin əsas məlumatlarını saxlayan PostgreSQL cədvəli (`swap_offer_entity`) və onunla işləyən JPA repozitorisi.
- **`GeoSwapOffer` (Elasticsearch) / `GeoSwapOfferRepository` (Elasticsearch)**: Təkliflərin axtarış üçün optimallaşdırılmış versiyasını saxlayan Elasticsearch indeksi (`swap_offers`) və onunla işləyən Elasticsearch repozitorisi.
- **`Swap` (JPA) / `SwapRepository` (JPA)**: İki istifadəçi arasında baş tutan mübadilə prosesinin statusunu və digər məlumatlarını saxlayan PostgreSQL cədvəli (`swaps`) və onunla işləyən JPA repozitorisi.

### `controller`, `dto`, `mapper`

- **`SwapController`**: Xarici dünya ilə əlaqəni təmin edən REST API endpointlərini saxlayır.
- **DTOs (`CreateSwapOfferRequest`, `SwapOfferDTO`, `AcceptSwapRequest`)**: API sərhədlərində məlumat transferi üçün istifadə olunan, validasiya qaydaları ilə zənginləşdirilmiş siniflər.
- **`OfferMapper`**: `CreateSwapOfferRequest` (DTO), `SwapOfferEntity` (JPA) və `GeoSwapOffer` (Elasticsearch) arasında məlumat çevirmələrini həyata keçirən komponent.

---

## 5. API və Event Sənədləşdirməsi

### 5.1. REST API

- `POST /api/skil-swaps/offers`: Yeni bir bacarıq mübadiləsi təklifi yaradır.
- `GET /api/skil-swaps/offers/me`: Autentifikasiya olunmuş istifadəçinin aktiv təkliflərini qaytarır.
- `GET /api/skil-swaps/offers/search`: Verilmiş bacarıq və coğrafi mövqe üzrə təklifləri axtarır.
- `POST /api/skil-swaps/match/{offerId}`: Bir təklifə uyğunlaşma (match) tələbi göndərir.
- `POST /api/skil-swaps/{swapId}/accept`: Göndərilmiş tələbi qəbul edir və görüş vaxtını təyin edir.
- `POST /api/skil-swaps/complete/{swapId}`: Mübadiləni tamamlanmış kimi işarələyir.

### 5.2. Kafka Topics & Events

- **Topic: `match-requested-events`**: Bir istifadəçi digərinin təklifinə maraq göstərdikdə göndərilir.
- **Topic: `meeting-reminder-events`**: Planlaşdırılmış görüşdən bir gün əvvəl xatırlatma məqsədilə göndərilir.
- **Topic: `swap-completed-events`**: Bir mübadilə uğurla tamamlandıqda göndərilir.

---

## 6. Quraşdırma və İşə Salma

1.  **Ön Tələblər:**
    - Java 17+
    - Docker və Docker Compose

2.  **Konfiqurasiya (`application.yml`):**
    - `spring.datasource.url/username/password`: PostgreSQL bağlantı parametrləri.
    - `spring.elasticsearch.*`: Elasticsearch bağlantı parametrləri.
    - `spring.kafka.bootstrap-servers`: Kafka brokerlərinin ünvanı.

3.  **Asılılıqların İşə Salınması (Docker ilə):**
    - Layihənin kökündəki `docker-compose.yml` faylı PostgreSQL, Elasticsearch, Kafka və Zookeeper xidmətlərini sizin üçün qurur.
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
      java -jar build/libs/skillswapservice-0.0.1-SNAPSHOT.jar
      ```
