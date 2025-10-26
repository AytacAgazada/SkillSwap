# `authService` Mikroxidmətinin Dərinlemesine Kodu İzahı

Bu sənəd, `authService` mikroxidmətinin hər bir sinifinin və əsas kod bloklarının sətir-sətir, detallı izahını təqdim edir. Məqsəd, layihənin arxitekturasını, məntiqini və hər bir komponentin vəzifəsini tam anlamağı təmin etməkdir.

---

## 1. Əsas Tətbiq Sinifi: `AuthServiceApplication.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/AuthServiceApplication.java`

Bu sinif, Spring Boot tətbiqinin giriş nöqtəsidir. Tətbiqi başladan və əsas konfiqurasiyaları aktivləşdirən `main` metodunu ehtiva edir.

```java
package com.example.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync; // Asinxron metodları aktivləşdirmək üçün

@SpringBootApplication
@EnableAsync // Asinxron metodları işə salır (OTP göndərmək üçün vacibdir)
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

### Sətir-Sətir İzahı

- **`package com.example.authservice;`**: Bu sinifin `com.example.authservice` paketinə aid olduğunu bildirir.

- **`@SpringBootApplication`**: Bu, üç əsas annotasiyanı özündə birləşdirən bir Spring Boot annotasiyasıdır:
    1.  **`@Configuration`**: Bu sinifin tətbiq üçün konfiqurasiya mənbəyi olduğunu göstərir.
    2.  **`@EnableAutoConfiguration`**: Spring Boot-un `classpath`-da olan `jar`-lara əsasən avtomatik konfiqurasiyalar etməsini təmin edir (məsələn, `spring-boot-starter-web` tapdıqda `Tomcat` və `DispatcherServlet`-i avtomatik konfiqurasiya edir).
    3.  **`@ComponentScan`**: Spring-ə `com.example.authservice` paketindən başlayaraq bütün alt paketləri skan edərək `@Component`, `@Service`, `@Repository`, `@Controller` kimi annotasiyalı sinifləri tapıb onları Spring Application Context-ə (Bean konteynerinə) əlavə etməsini deyir.

- **`@EnableAsync`**: Bu annotasiya Spring-in asinxron metod icrası imkanlarını aktivləşdirir. Tətbiqdə `@Async` annotasiyası ilə işarələnmiş hər hansı bir metod, ayrı bir `thread`-də (arxa fonda) icra ediləcək. Bu, xüsusilə e-poçt göndərmək kimi vaxt apara bilən və əsas tətbiq axınını bloklamamalı olan əməliyyatlar üçün kritikdir. Bu olmasa, OTP e-poçtu göndərilərkən istifadəçi cavab almaq üçün prosesin bitməsini gözləməli olardı.

- **`@EnableDiscoveryClient`**: Bu annotasiya, tətbiqin bir "Discovery Service"-ə (məsələn, Eureka, Consul) özünü qeydiyyatdan keçirməsini təmin edir. Mikroxidmət arxitekturasında bu, digər xidmətlərin (məsələn, API Gateway) `auth-service`-in şəbəkədəki ünvanını (IP və port) avtomatik olaraq tapmasına və onunla əlaqə qurmasına imkan verir.

- **`public class AuthServiceApplication`**: Tətbiqin əsas sinifinin təyin edilməsi.

- **`public static void main(String[] args)`**: Java tətbiqlərinin standart giriş nöqtəsi. Tətbiq işə salındıqda ilk olaraq bu metod icra olunur.

- **`SpringApplication.run(AuthServiceApplication.class, args);`**: Bu statik metod Spring Boot tətbiqini başladır. Aşağıdakı əsas işləri görür:
    1.  Spring Application Context-i yaradır.
    2.  `@ComponentScan` vasitəsilə bütün bean-ləri tapır və yaradır.
    3.  `@EnableAutoConfiguration` sayəsində avtomatik konfiqurasiyaları tətbiq edir.
    4.  Daxili `Tomcat` serverini başladaraq tətbiqi veb-server kimi işə salır.

---

## 2. `config` Paketi

Bu paket, tətbiqin fərqli modulları üçün mərkəzi konfiqurasiya siniflərini saxlayır.

### 2.1. `AsyncConfig.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/config/AsyncConfig.java`

Bu sinif, `@EnableAsync` annotasiyası ilə aktivləşdirilmiş asinxron əməliyyatların hansı `thread` hovuzunda (thread pool) icra ediləcəyini detallı şəkildə konfiqurasiya edir.

```java
package com.example.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${spring.task.execution.pool.core-size}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size}")
    private int maxPoolSize;

    @Value("${spring.task.execution.pool.queue-capacity}")
    private int queueCapacity;

    @Value("${spring.task.execution.thread-name-prefix}")
    private String threadNamePrefix;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
```

#### Sətir-Sətir İzahı

- **`@Configuration`**: Bu sinifin bir və ya daha çox `@Bean` metodu təyin edən bir konfiqurasiya sinifi olduğunu göstərir. Spring bu sinifi başladarkən onun içindəki `@Bean` metodlarını icra edərək bean-ləri konteynerə əlavə edir.
- **`@EnableAsync`**: Burada təkrar istifadə olunması, bu konfiqurasiyanın məhz asinxronluqla bağlı olduğunu vurğulamaq məqsədi daşıyır, lakin əsas tətbiq sinifində olması kifayətdir.
- **`@Value("...")`**: Bu annotasiyalar, `application.yml` (və ya `.properties`) faylından dəyərləri oxuyub aşağıdakı dəyişənlərə mənimsətmək üçün istifadə olunur. Bu, konfiqurasiyanı koddan kənarda saxlamağa imkan verir.
    - `corePoolSize`: Hovuzda daima aktiv saxlanılacaq `thread`-lərin sayı. Yeni bir tapşırıq gəldikdə, əgər aktiv `thread` sayı bundan azdırsa, yeni bir `thread` yaradılır.
    - `maxPoolSize`: Hovuzda eyni anda mövcud ola biləcək maksimum `thread` sayı. Növbə (`queue`) dolduqda və aktiv `thread` sayı `maxPoolSize`-dan az olduqda yeni `thread`-lər yaradılır.
    - `queueCapacity`: `corePoolSize`-a çatdıqdan sonra gələn yeni tapşırıqların icra olunmaq üçün gözləyəcəyi növbənin tutumu.
    - `threadNamePrefix`: Yaradılan `thread`-lərin adlarına əlavə ediləcək prefiks (məsələn, `taskExecutor-1`, `taskExecutor-2`). Bu, logları və debug prosesini asanlaşdırır.
- **`@Bean(name = "taskExecutor")`**: Bu metodun bir Spring `bean`-i yaratdığını bildirir. `name = "taskExecutor"` atributu bu bean-ə xüsusi bir ad verir. `@Async` annotasiyası ilə işarələnmiş metodlar, əgər başqa bir `Executor` adı verilməyibsə, default olaraq `taskExecutor` adlı bu bean-i axtaracaqlar.
- **`public Executor taskExecutor()`**: `ThreadPoolTaskExecutor` obyektini yaradan və konfiqurasiya edən metod.
    - `ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();`: Spring-in təqdim etdiyi, `thread` hovuzunu idarə etmək üçün güclü bir sinifdir.
    - `executor.set...()`: `application.yml`-dən oxunan dəyərlər `executor` obyektinə təyin edilir.
    - `executor.initialize()`: `Executor`-u təyin edilmiş parametrlərlə başladır.
    - `return executor;`: Hazırlanmış `executor` obyekti Spring konteynerinə əlavə edilmək üçün qaytarılır.

### 2.2. `OpenApiConfig.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/config/OpenApiConfig.java`

Bu sinif, `springdoc-openapi` kitabxanasından istifadə edərək REST API-lər üçün Swagger UI sənədləşdirməsini yaradır və konfiqurasiya edir.

```java
package com.example.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AuthService API")
                        .version("1.0.0")
                        .description("Authentication and User Management Service API Documentation")
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact().name("Your Name").email("your.email@example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")) // JWT üçün tələb əlavə edir
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme() // JWT üçün security scheme təyin edir
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
```

#### Sətir-Sətir İzahı

- **`@Bean public OpenAPI customOpenAPI()`**: `OpenAPI` obyektini yaradan və konfiqurasiya edən bir bean metodu. Bu obyekt Swagger UI-ın görünüşünü və davranışını təyin edir.
- **`.info(new Info()...)`**: Swagger UI-ın yuxarı hissəsində görünəcək API haqqında ümumi məlumatları təyin edir:
    - `.title()`: API-nin başlığı.
    - `.version()`: API-nin versiyası.
    - `.description()`: API-nin məqsədi haqqında qısa təsvir.
    - `.contact()` və `.license()`: Əlaqə və lisenziya məlumatları.
- **`.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))`**: Bu sətir, Swagger UI-da bütün endpointlərin yanında bir "kilid" ikonasının görünməsini təmin edir. Bu, həmin endpointlərin təhlükəsizlik tələb etdiyini (bu halda, `bearerAuth` adlı sxemə uyğun bir token) göstərir. Bu, qlobal bir tələbdir.
- **`.components(new Components()...)`**: Təkrar istifadə oluna bilən komponentləri, o cümlədən təhlükəsizlik sxemlərini təyin edir.
- **`.addSecuritySchemes("bearerAuth", new SecurityScheme()...)`**: `bearerAuth` adlı yeni bir təhlükəsizlik sxemi yaradır. Bu sxem, Swagger UI-da "Authorize" düyməsini basdıqda görünən pəncərəni konfiqurasiya edir.
    - `.type(SecurityScheme.Type.HTTP)`: Təhlükəsizlik növünün HTTP olduğunu bildirir.
    - `.scheme("bearer")`: Sxemin "Bearer Token" olduğunu göstərir. Bu, sorğu başlığının `Authorization: Bearer <token>` formatında olacağını bildirir.
    - `.bearerFormat("JWT")`: Tokenin formatının JWT olduğunu göstərən bir ipucudur (hint).

Bu konfiqurasiya sayəsində, proqramçı Swagger UI üzərindən `login` endpointi ilə token aldıqdan sonra, həmin tokeni "Authorize" düyməsi vasitəsilə daxil edərək digər qorunan endpointlərə birbaşa sorğu göndərə bilər.

### 2.3. `SecurityConfig.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/config/SecurityConfig.java`

Bu, tətbiqin təhlükəsizlik konfiqurasiyasının onurğa sütunudur. Spring Security-nin davranışını, endpointlərə giriş qaydalarını, autentifikasiya və avtorizasiya mexanizmlərini təyin edir.

```java
package com.example.authservice.config;

// ... importlar
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "PROVIDER")
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .rememberMe(rememberMe -> rememberMe
                        .key("superSecretKey123") // Özəl, unikal açar
                        .tokenValiditySeconds(14 * 24 * 60 * 60) // 14 gün
                        .userDetailsService(userDetailsService)
                );

        return http.build();
    }
}
```

#### Sətir-Sətir İzahı

- **`@Configuration`, `@EnableWebSecurity`**: Bu sinifin Spring Security konfiqurasiya sinifi olduğunu bildirir. `@EnableWebSecurity` Spring Security-nin veb təhlükəsizliyi dəstəyini aktivləşdirir.
- **`@EnableMethodSecurity(prePostEnabled = true)`**: Metod səviyyəsində təhlükəsizliyi aktivləşdirir. `prePostEnabled = true` atributu `@PreAuthorize` və `@PostAuthorize` kimi daha güclü annotasiyaların istifadəsinə imkan verir. Bu, controller metodlarının üzərində `hasRole('ADMIN')` kimi ifadələrlə giriş nəzarəti tətbiq etməyə imkan yaradır.
- **`@RequiredArgsConstructor`**: Lombok annotasiyasıdır. Bütün `final` sahələr üçün bir konstruktor yaradır. Bu, `@Autowired` annotasiyası ilə eyni işi görür (Constructor Injection) və kodu daha səliqəli saxlayır.
- **`private final ...` sahələr**: Təhlükəsizlik konfiqurasiyasının asılı olduğu digər bean-lərdir. Spring bunları avtomatik olaraq konstruktora inject edir.
    - `UserDetailsServiceImpl`: İstifadəçi məlumatlarını verilənlər bazasından yükləmək üçün istifadə olunur.
    - `AuthEntryPointJwt`: Autentifikasiya olunmamış istifadəçi qorunan bir resursa daxil olmaq istədikdə işə düşür (401 Unauthorized xətası).
    - `AuthTokenFilter`: Hər bir sorğuda JWT tokenini yoxlayan filtr.
    - `CustomAccessDeniedHandler`: Autentifikasiya olunmuş, lakin lazımi rola sahib olmayan bir istifadəçi bir resursa daxil olmaq istədikdə işə düşür (403 Forbidden xətası).

- **`@Bean public PasswordEncoder passwordEncoder()`**: Şifrələri kodlamaq üçün istifadə olunacaq alqoritmi təyin edən bir bean yaradır. `BCryptPasswordEncoder` hazırda sənaye standartı hesab olunur, çünki o, hər şifrə üçün fərqli "salt" istifadə edir və şifrələmə prosesi qəsdən yavaşdır, bu da "brute-force" hücumlarını çətinləşdirir.

- **`@Bean public DaoAuthenticationProvider authenticationProvider()`**: Əsas autentifikasiya məntiqini təmin edən provayderi konfiqurasiya edir.
    - `authProvider.setUserDetailsService(userDetailsService)`: Spring Security-ə istifadəçini tapmaq üçün hansı servisi (`UserDetailsServiceImpl`) istifadə etməli olduğunu deyir.
    - `authProvider.setPasswordEncoder(passwordEncoder())`: Spring Security-ə, istifadəçinin daxil etdiyi şifrə ilə verilənlər bazasındakı kodlanmış şifrəni müqayisə etmək üçün hansı `PasswordEncoder`-dən (`BCrypt`) istifadə etməli olduğunu deyir.

- **`@Bean public AuthenticationManager authenticationManager(...)`**: `AuthService`-də istifadəçini manual olaraq autentifikasiya etmək üçün lazım olan `AuthenticationManager` bean-ini yaradır.

- **`@Bean public SecurityFilterChain filterChain(HttpSecurity http)`**: HTTP sorğuları üçün təhlükəsizlik filtrləri zəncirini quran əsas metod.
    - `.csrf(csrf -> csrf.disable())`: CSRF (Cross-Site Request Forgery) qorumasını deaktiv edir. JWT kimi token-əsaslı autentifikasiya istifadə edən və serverdə sessiya saxlamayan (stateless) API-lərdə CSRF hücumları adətən relevant deyil, buna görə də deaktiv edilir.
    - `.exceptionHandling(...)`: Xəta idarəetməsini konfiqurasiya edir. `authenticationEntryPoint` 401, `accessDeniedHandler` isə 403 xətalarını idarə etmək üçün təyin edilir.
    - `.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))`: Sessiya yaratma siyasətini təyin edir. `IF_REQUIRED` o deməkdir ki, Spring Security yalnız ehtiyac olduqda (məsələn, `rememberMe` funksionallığı üçün) sessiya yaradacaq. Əgər `rememberMe` olmasaydı, `STATELESS` istifadə etmək daha doğru olardı.
    - `.authorizeHttpRequests(auth -> auth...)`: Endpointlərə giriş qaydalarını təyin edir. Qaydalar yuxarıdan aşağıya doğru yoxlanılır:
        - `.requestMatchers("/api/auth/**").permitAll()`: `/api/auth/` altında olan bütün endpointlərə (login, signup, vs.) heç bir autentifikasiya olmadan girişi icazə verir.
        - `.requestMatchers("/api/users/**").hasAnyRole("ADMIN", "PROVIDER")`: `/api/users/` altındakı endpointlərə yalnız `ADMIN` və ya `PROVIDER` roluna sahib istifadəçilərin girişinə icazə verir.
        - `.requestMatchers("/swagger-ui/**", ...).permitAll()`: Swagger UI sənədləşdirməsinə sərbəst girişi təmin edir.
        - `.anyRequest().permitAll()`: **DİQQƏT!** Bu sətir yuxarıdakı qaydalara uyğun gəlməyən **bütün digər sorğulara** icazə verir. Bu, inkişaf mərhələsində rahat olsa da, production mühitində təhlükəsizlik boşluğu yarada bilər. Production üçün bu, `.anyRequest().authenticated()` ilə əvəz olunmalıdır ki, naməlum bütün endpointlər qorunsun.
    - `.authenticationProvider(authenticationProvider())`: Yuxarıda konfiqurasiya etdiyimiz `DaoAuthenticationProvider`-ı aktivləşdirir.
    - `.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)`: Bizim yazdığımız `AuthTokenFilter`-i Spring Security-nin standart `UsernamePasswordAuthenticationFilter`-indən **əvvəl** zəncirə əlavə edir. Bu, hər sorğu gəldikdə, istifadəçi adı/şifrə yoxlanmasından əvvəl JWT tokeninin yoxlanmasını təmin edir.
    - `.rememberMe(...)`: "Məni Xatırla" funksionallığını konfiqurasiya edir.
        - `.key("superSecretKey123")`: "Remember Me" tokenini imzalamaq üçün istifadə olunan gizli açar. Bu, production-da mütləq dəyişdirilməli və təhlükəsiz saxlanmalıdır.
        - `.tokenValiditySeconds(...)`: Tokenin etibarlılıq müddətini saniyə ilə təyin edir (burada 14 gün).
        - `.userDetailsService(userDetailsService)`: "Remember Me" tokeni ilə gələn istifadəçinin məlumatlarını yenidən yükləmək üçün hansı servisin istifadə ediləcəyini göstərir.

- **`return http.build();`**: Konfiqurasiya edilmiş `HttpSecurity` obyektindən `SecurityFilterChain` yaradır və onu Spring konteynerinə qaytarır.

---

## 3. `controller` Paketi

Bu paket, xarici dünyadan (klientlərdən, Postman-dən və ya digər mikroxidmətlərdən) gələn HTTP sorğularını qəbul edən və onlara cavab verən REST controller siniflərini saxlayır. Controller-lər sorğunu emal etmək üçün adətən `service` qatına müraciət edirlər.

### 3.1. `AuthController.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/controller/AuthController.java`

Bu sinif, autentifikasiya, qeydiyyat, token idarəetməsi və OTP əməliyyatları kimi bütün əsas istifadəçi idarəetməsi endpointlərini təmin edən mərkəzi controller-dir.

```java
package com.example.authservice.controller;

// ... importlar
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication & Authorization", description = "User registration, login, token management and OTP operations")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ... metodlar
}
```

#### Sinif Səviyyəsində İzahlar

- **`@RestController`**: Bu annotasiya `@Controller` və `@ResponseBody` annotasiyalarını birləşdirir. Bu o deməkdir ki, bu sinifdəki bütün metodların qaytardığı dəyərlər (məsələn, DTO obyektləri) avtomatik olaraq JSON formatına çevrilərək HTTP cavabının body-sinə yazılacaq.
- **`@RequestMapping("/api/auth")`**: Bu sinifdəki bütün endpointlərin URL-lərinin `/api/auth` prefiksi ilə başlayacağını bildirir. Məsələn, `@PostMapping("/signup")` annotasiyalı metodun tam URL-i `/api/auth/signup` olacaq.
- **`@RequiredArgsConstructor`**: Lombok annotasiyası. `private final` olaraq işarələnmiş sahələr (`authService` və `userRepository`) üçün avtomatik olaraq bir konstruktor yaradır. Bu, asılılıqların "Constructor Injection" yolu ilə tətbiq edilməsini təmin edir.
- **`@Slf4j`**: Lombok annotasiyası. Bu sinif üçün avtomatik olaraq `private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);` sahəsini yaradır. Bu, `log.info(...)`, `log.error(...)` kimi metodlarla asan şəkildə loglama aparmağa imkan verir.
- **`@Tag(...)`**: Swagger (OpenAPI) annotasiyası. Swagger UI-da bu controller-dəki bütün endpointləri "Authentication & Authorization" adlı bir qrup altında toplayır və təsvir əlavə edir.

#### Sahələrin İzahı

- **`private final AuthService authService;`**: Biznes məntiqini həyata keçirən `AuthService`-in bir nüsxəsi. Controller sorğunu qəbul etdikdən sonra əsas işi görmək üçün bu servisə müraciət edir.
- **`private final UserRepository userRepository;`**: Bəzi spesifik, sadə yoxlamalar üçün birbaşa verilənlər bazasına müraciət etmək üçün istifadə olunan repository. (Qeyd: Arxitektura baxımından, adətən bütün verilənlər bazası əməliyyatlarının servis qatında olması daha yaxşıdır. Burada birbaşa istifadə olunması kiçik istisna kimi qəbul edilə bilər).

--- 

#### Metodların İzahı

##### **`registerUser` metodu**

```java
@Operation(summary = "Register a new user", description = "Creates a new user account with provided details.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User with given FIN, username, email or phone already exists")
})
@PostMapping("/signup")
public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
    authService.registerUser(signupRequest);
    return new ResponseEntity<>("User registered successfully. Please confirm your account.", HttpStatus.CREATED);
}
```

- **`@Operation` və `@ApiResponses`**: Swagger annotasiyaları. Endpointin məqsədini, mümkün cavab kodlarını və onların mənalarını sənədləşdirir.
- **`@PostMapping("/signup")`**: Bu metodun `/api/auth/signup` ünvanına göndərilən HTTP `POST` sorğularını idarə edəcəyini bildirir.
- **`public ResponseEntity<String> registerUser(...)`**: Metodun imzası.
    - `ResponseEntity<String>`: Bu, tam bir HTTP cavabını (status kodu, başlıqlar və body) təmsil edən bir Spring sinifidir. Bu, bizə cavabın status kodunu (məsələn, `201 CREATED`) manual olaraq təyin etməyə imkan verir.
- **`@Valid @RequestBody SignupRequest signupRequest`**: Metodun parametrləri.
    - `@RequestBody`: HTTP sorğusunun body-sindəki JSON məlumatının `SignupRequest` DTO sinifinə çevrilməsini təmin edir.
    - `@Valid`: `SignupRequest` sinifinin içindəki validasiya annotasiyalarının (`@NotBlank`, `@Size` və s.) işə salınmasını təmin edir. Əgər validasiya uğursuz olarsa, Spring avtomatik olaraq `MethodArgumentNotValidException` xətası atacaq və bu xəta `GlobalExceptionHandler` tərəfindən tutularaq 400 Bad Request cavabı qaytarılacaq.
- **`authService.registerUser(signupRequest);`**: Əsas işin görülməsi üçün `AuthService`-in `registerUser` metodu çağırılır.
- **`return new ResponseEntity<>(..., HttpStatus.CREATED);`**: Əgər `registerUser` metodu uğurla başa çatarsa (yəni, heç bir exception atmazsa), klientə body-sində bir mətn olan və status kodu `201 CREATED` olan bir HTTP cavabı qaytarılır. Bu, REST prinsiplərinə görə yeni bir resurs yaradıldıqda istifadə olunan standart status kodudur.

##### **`authenticateUser` metodu**

```java
@Operation(summary = "User login", ...)
@PostMapping("/login")
public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                                     HttpServletRequest request) {
    AuthResponse authResponse = authService.authenticateUser(loginRequest, request);
    return ResponseEntity.ok(authResponse);
}
```

- **`@PostMapping("/login")`**: `/api/auth/login` ünvanına gələn `POST` sorğularını idarə edir.
- **`public ResponseEntity<AuthResponse> authenticateUser(...)`**: Uğurlu autentifikasiya nəticəsində JWT tokenlərini və istifadəçi məlumatlarını ehtiva edən `AuthResponse` obyektini qaytarır.
- **`@Valid @RequestBody LoginRequest loginRequest`**: Sorğunun body-sindəki JSON-u `LoginRequest` DTO-suna çevirir və validasiya edir.
- **`HttpServletRequest request`**: Spring bu parametri avtomatik olaraq təmin edir. Bu, xam HTTP sorğusunun özünü təmsil edir. `AuthService`-ə ötürülür ki, oradan istifadəçinin IP ünvanı (`request.getRemoteAddr()`) və User-Agent başlığı (`request.getHeader("User-Agent")`) kimi məlumatlar oxuna bilsin. Bu məlumatlar refresh token-ə bağlanaraq təhlükəsizliyi artırır.
- **`AuthResponse authResponse = authService.authenticateUser(loginRequest, request);`**: Autentifikasiya məntiqini həyata keçirmək üçün `AuthService` çağırılır.
- **`return ResponseEntity.ok(authResponse);`**: `ResponseEntity.ok()` `HttpStatus.OK` (200) status koduna malik bir cavab yaradır. `authResponse` obyekti JSON formatında cavabın body-sinə yazılır.

##### **`refreshAccessToken` metodu**

```java
@PostMapping("/refresh-token")
public ResponseEntity<AuthResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse authResponse = authService.refreshAccessToken(request.getRefreshToken(), null);
    return ResponseEntity.ok(authResponse);
}
```

- **`@PostMapping("/refresh-token")`**: `/api/auth/refresh-token` ünvanına gələn `POST` sorğularını idarə edir.
- **`authService.refreshAccessToken(request.getRefreshToken(), null);`**: `AuthService`-ə refresh tokeni ötürərək yeni bir access token və yenilənmiş refresh token almaq üçün çağırılır. Burada ikinci parametr (`HttpServletRequest`) `null` ötürülür, çünki mövcud implementasiyada IP ünvanı yoxlaması refresh zamanı fərqli şəkildə həyata keçirilir (tokenin özündəki IP ilə müqayisə edilir).

##### **`logoutUser` metodu**

```java
@PostMapping("/logout")
public ResponseEntity<String> logoutUser(@RequestAttribute("userId") UUID userId) {
    authService.logoutUser(userId);
    return ResponseEntity.ok("User logged out successfully!");
}
```

- **`@PostMapping("/logout")`**: `/api/auth/logout` ünvanına gələn `POST` sorğularını idarə edir.
- **`@RequestAttribute("userId") UUID userId`**: Bu, standart `@RequestBody` və ya `@RequestParam`-dan fərqlidir. `@RequestAttribute` sorğunun atributlarından bir dəyər oxuyur. Bu atribut, adətən, sorğu zəncirindəki daha əvvəlki bir komponent (məsələn, bir `Filter` və ya `Interceptor`) tərəfindən təyin edilir. Bu layihədə, `AuthTokenFilter` JWT tokenini təsdiqlədikdən sonra istifadəçinin ID-sini sorğuya bir atribut kimi əlavə edir. Bu, controller-in istifadəçinin kim olduğunu təhlükəsiz şəkildə bilməsini təmin edir.
- **`authService.logoutUser(userId);`**: `AuthService`-i çağıraraq həmin istifadəçiyə aid bütün refresh tokenlərin verilənlər bazasından silinməsini təmin edir.

##### **Rol Əsaslı Endpoint Nümunələri**

```java
@GetMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> getAdminDashboard() {
    return ResponseEntity.ok("Welcome to the Admin Dashboard! (Only for ADMINs)");
}

@GetMapping("/admin-or-provider/data")
@PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
public ResponseEntity<String> getAdminOrProviderData() {
    return ResponseEntity.ok("This data is for ADMINs or PROVIDERs!");
}
```

- **`@PreAuthorize("...")`**: Bu annotasiya, metod icra olunmazdan **əvvəl** verilən SpEL (Spring Expression Language) ifadəsinin yoxlanmasını təmin edir. Əgər ifadə `false` qaytarsa, `AccessDeniedException` atılır (və `CustomAccessDeniedHandler` tərəfindən 403 Forbidden cavabı qaytarılır).
    - **`hasRole('ADMIN')`**: Hazırkı autentifikasiya olunmuş istifadəçinin `ROLE_ADMIN` səlahiyyətinə (authority) sahib olub-olmadığını yoxlayır. Spring Security rollara avtomatik olaraq `ROLE_` prefiksini əlavə edir.
    - **`hasAnyRole('ADMIN', 'PROVIDER')`**: İstifadəçinin verilən rollardan ən azı birinə sahib olub-olmadığını yoxlayır.

Bu annotasiyalar, `SecurityConfig`-də `@EnableMethodSecurity(prePostEnabled = true)` aktivləşdirildiyi üçün işləyir və endpointlərə giriş nəzarətini deklarativ və oxunaqlı bir şəkildə həyata keçirməyə imkan verir.

##### **Daxili Xidmət Endpointləri**

```java
@GetMapping("/{authUserId}/exists")
public ResponseEntity<Boolean> doesUserExist(@PathVariable String authUserId) {
    UUID userId = UUID.fromString(authUserId); // String → UUID çevrilir
    boolean exists = userRepository.existsById(userId);
    return ResponseEntity.ok(exists);
}

@GetMapping("/{authUserId}/role")
public ResponseEntity<String> getUserRole(@PathVariable String authUserId) {
    // ...
}
```

- **`@GetMapping("/{authUserId}/exists")`**: Bu endpointlər adətən digər mikroxidmətlər tərəfindən daxili şəbəkə üzərindən çağırılmaq üçün nəzərdə tutulub (məsələn, `SkillUserService`-in `AuthClient`-i tərəfindən). Onlar bir istifadəçinin mövcudluğunu və ya rolunu yoxlamaq kimi sadə, spesifik məlumatları təqdim edirlər.
- **`@PathVariable String authUserId`**: URL-in `{authUserId}` hissəsindəki dəyişəni metodun parametrinə ötürür.
- **`UUID.fromString(authUserId)`**: URL-dən gələn `String` formatındakı ID-ni `UUID` obyektinə çevirir.
- **`userRepository.existsById(userId)`**: Birbaşa repository-dən istifadə edərək verilənlər bazasında həmin ID-yə malik bir qeydin olub-olmadığını yoxlayır. Bu, sadə bir yoxlama olduğu üçün servis qatına getmədən birbaşa controller-də edilməsi məqbul sayıla bilər.

---

## 4. `exception` Paketi

Bu paket, tətbiqdə baş verə biləcək spesifik xəta vəziyyətlərini təmsil edən xüsusi `Exception` siniflərini və bu xətaları mərkəzləşdirilmiş şəkildə idarə edərək klientə standartlaşdırılmış cavablar qaytaran qlobal xəta idarəedicisini (`GlobalExceptionHandler`) saxlayır.

### 4.1. Xüsusi Exception Sinifləri

Bu siniflər, `RuntimeException`-dan törəyir. Bu o deməkdir ki, onlar "unchecked" exceptionlardır və metod imzalarında `throws ...` ilə bəyan edilmələri məcburi deyil. Bu, kodu daha təmiz saxlayır. Onların əsas məqsədi, xətanın növünü (məsələn, `UserAlreadyExistsException` vs `InvalidCredentialsException`) aydın şəkildə ifadə etməkdir ki, `GlobalExceptionHandler` onları fərqləndirə bilsin.

#### `UserAlreadyExistsException.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/exception/UserAlreadyExistsException.java`
```java
package com.example.authservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String field, String value) {
        super(String.format("User with this %s '%s' already exists.", field, value));
    }
}
```
- **Məqsədi:** Qeydiyyat zamanı təqdim edilən `FIN`, `username`, `email` və ya `phone` nömrəsinin artıq verilənlər bazasında mövcud olduğu zaman atılır.
- **Konstruktor:** Hansı sahənin (`field`) hansı dəyərlə (`value`) təkrarladığını göstərən dinamik bir xəta mesajı yaradır. Məsələn: "User with this email 'test@example.com' already exists.".

#### `InvalidCredentialsException.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/exception/InvalidCredentialsException.java`
```java
package com.example.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
```
- **Məqsədi:** Daxil olma (login) zamanı istifadəçi tapılmadıqda və ya hesab aktivləşdirilmədikdə atılır.
- **`@ResponseStatus(HttpStatus.UNAUTHORIZED)`**: Bu annotasiya, əgər bu exception heç bir `@ExceptionHandler` tərəfindən tutulmazsa, Spring-in avtomatik olaraq `401 Unauthorized` status kodu ilə cavab qaytarmasını təmin edir. Bu, bir növ ehtiyat mexanizmidir.

#### `OtpException.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/exception/OtpException.java`
- **Məqsədi:** OTP (One-Time Password) ilə bağlı bütün xətalar üçün istifadə olunur: yanlış kod, vaxtı keçmiş kod, artıq istifadə edilmiş kod, OTP göndəriləcək ünvanın (email/telefon) olmaması və s.

#### `TokenRefreshException.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/exception/TokenRefreshException.java`
- **Məqsədi:** Refresh token ilə bağlı problemlər zamanı atılır: token tapılmadıqda, etibarlılıq müddəti bitdikdə və ya başqa bir şübhəli vəziyyət olduqda.

#### `ResourceNotFoundException.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/exception/ResourceNotFoundException.java`
- **Məqsədi:** Ümumi məqsədli bir exception. Sistemdə hər hansı bir resurs (məsələn, ID ilə axtarılan istifadəçi) tapılmadıqda atılır.

### 4.2. `GlobalExceptionHandler.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/exception/GlobalExceptionHandler.java`

Bu sinif, tətbiqin bütün controller-lərində atılan exception-ları tutan və onları standartlaşdırılmış JSON formatında klientə qaytaran mərkəzi bir komponentdir.

```java
package com.example.authservice.exception;

// ... importlar
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ... metodlar

    public static class ErrorResponse { ... } // Daxili sinif
}
```

#### Sinif Səviyyəsində İzahlar

- **`@RestControllerAdvice`**: Bu annotasiya `@ControllerAdvice` və `@ResponseBody` annotasiyalarını birləşdirir. `@ControllerAdvice` bu sinifin bütün `@RestController`-lar üçün ümumi bir məsləhətçi (advisor) olduğunu bildirir. Bu o deməkdir ki, bu sinifdəki `@ExceptionHandler` metodları tətbiqin istənilən controller-ində yaranan xətaları tuta bilər. `@ResponseBody` isə bu metodların qaytardığı obyektlərin avtomatik olaraq JSON-a çevrilməsini təmin edir.

#### Metodların İzahı

Hər bir `handle...` metodu müəyyən bir exception növünü tutmaq üçün nəzərdə tutulub.

```java
@ExceptionHandler(UserAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(), // 409
            ex.getMessage(),
            LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
}
```

- **`@ExceptionHandler(UserAlreadyExistsException.class)`**: Bu annotasiya, `handleUserAlreadyExistsException` metodunun yalnız `UserAlreadyExistsException` növlü (və ya onun alt sinifləri) exception-ları tutacağını bildirir.
- **`public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(...)`**: Metod, tutduğu exception (`ex`) haqqında məlumatları alır.
- **`ErrorResponse errorResponse = new ErrorResponse(...)`**: Klientə göndəriləcək standart cavab formatı olan `ErrorResponse` obyekti yaradılır.
    - `HttpStatus.CONFLICT.value()`: Status kodu olaraq `409 CONFLICT` təyin edilir. Bu, REST prinsiplərinə görə, sorğunun serverin hazırkı vəziyyəti ilə ziddiyyət təşkil etdiyi (məsələn, eyni resursu təkrar yaratmaq cəhdi) zaman istifadə olunur.
    - `ex.getMessage()`: Exception-ın özündən gələn dinamik xəta mesajı (`super(...)` ilə təyin edilən) cavaba əlavə edilir.
- **`return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);`**: Hazırlanmış `errorResponse` obyekti və müvafiq HTTP status kodu ilə birlikdə tam bir `ResponseEntity` obyekti yaradılaraq qaytarılır.

Digər `handle...` metodları da eyni məntiqlə işləyir, sadəcə fərqli exception növlərini tutur və fərqli HTTP status kodları (məsələn, `401 UNAUTHORIZED`, `400 BAD_REQUEST`, `403 FORBIDDEN`) qaytarırlar.

##### **Validasiya Xətaları üçün Xüsusi Handler**

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            LocalDateTime.now(),
            errors
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}
```

- **`@ExceptionHandler(MethodArgumentNotValidException.class)`**: Bu, controller metodlarında `@Valid` annotasiyası ilə işarələnmiş DTO-ların validasiyası uğursuz olduqda Spring tərəfindən avtomatik atılan exception-ı tutur.
- **`Map<String, String> errors = new HashMap<>();`**: Hansı sahədə hansı xətanın baş verdiyini saxlamaq üçün bir `Map` yaradılır.
- **`ex.getBindingResult().getAllErrors().forEach(...)`**: `MethodArgumentNotValidException` obyektindən bütün validasiya xətalarının siyahısı alınır və hər biri üçün dövr başladılır.
- **`String fieldName = ((FieldError) error).getField();`**: Xətanın baş verdiyi sahənin adı (məsələn, `"password"`) alınır.
- **`String errorMessage = error.getDefaultMessage();`**: DTO-da annotasiya ilə təyin edilmiş xəta mesajı (məsələn, `"Password must be at least 8 characters long"`) alınır.
- **`errors.put(fieldName, errorMessage);`**: Sahə adı və xəta mesajı `Map`-ə əlavə edilir.
- **`new ErrorResponse(..., errors)`**: Sonda, `ErrorResponse` obyekti bu `errors` `Map`-i ilə birlikdə yaradılır. Bu, klientə hansı sahələri səhv daxil etdiyini detallı şəkildə göstərməyə imkan verir. Məsələn:
  ```json
  {
      "status": 400,
      "message": "Validation Error",
      "timestamp": "...",
      "details": {
          "password": "Password must contain at least one uppercase letter...",
          "username": "Username cannot be blank"
      }
  }
  ```

### 4.3. `CustomAccessDeniedHandler.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/exception/CustomAccessDeniedHandler.java`

Bu sinif, Spring Security-nin təhlükəsizlik zəncirinin bir hissəsidir və `GlobalExceptionHandler`-dan fərqli olaraq birbaşa `AccessDeniedException`-ları idarə etmək üçün nəzərdə tutulub.

```java
package com.example.authservice.exception;

// ... importlar
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String json = String.format(
                """
                {
                    "status": 403,
                    "message": "Access Denied: %s",
                    "timestamp": "%s",
                    "path": "%s"
                }
                """,
                accessDeniedException.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
        );

        response.getWriter().write(json);
    }
}
```

- **`@Component`**: Bu sinifin bir Spring bean-i olduğunu bildirir ki, `SecurityConfig`-də inject edilə bilsin.
- **`implements AccessDeniedHandler`**: Spring Security-nin tələb etdiyi interfeysi implementasiya edir.
- **`handle(...)` metodu**: `@PreAuthorize` kimi yoxlamalardan keçə bilməyən (yəni, autentifikasiya olunmuş, amma lazımi rola sahib olmayan) bir istifadəçi qorunan bir endpointə müraciət etdikdə Spring Security bu metodu çağırır.
- **`response.setStatus(HttpServletResponse.SC_FORBIDDEN);`**: Cavabın status kodunu `403 FORBIDDEN` olaraq təyin edir.
- **`response.setContentType(MediaType.APPLICATION_JSON_VALUE);`**: Cavabın `Content-Type` başlığını `application/json` olaraq təyin edir.
- **`String.format(...)` və `response.getWriter().write(json)`**: `GlobalExceptionHandler`-da istifadə olunan `ErrorResponse` sinifi əvəzinə, burada JSON cavabı manual olaraq bir `String` formatında yaradılır və birbaşa cavabın `body`-sinə yazılır. Məqsəd eynidir: klientə standartlaşdırılmış bir xəta cavabı göndərmək.

---

## 5. `kafka` Paketi

Bu paket, `authService`-in digər mikroxidmətlərlə asinxron və əlaqəsiz (loosely coupled) bir şəkildə kommunikasiya qurması üçün Apache Kafka ilə inteqrasiyanı təmin edir. `authService` bir hadisə (event) baş verdikdə (məsələn, yeni istifadəçi qeydiyyatdan keçdikdə) bunu bir Kafka "topic"-inə göndərir və digər maraqlı tərəflər (məsələn, `SkillUserService`) bu hadisəni dinləyərək müvafiq əməliyyatları icra edir.

### 5.1. `UserRegisteredEventDTO.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/kafka/UserRegisteredEventDTO.java`

Bu sinif, yeni bir istifadəçi qeydiyyatdan keçdikdə Kafka-ya göndəriləcək mesajın məlumat strukturunu təyin edən bir Data Transfer Object-dir (DTO).

```java
package com.example.authservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEventDTO {

    private String userId;
    private String email;
    private String verificationToken;
    private LocalDateTime registrationTime = LocalDateTime.now(); // Eventin yaranma vaxtı
}
```

#### Sətir-Sətir İzahı

- **`@Data`**: Lombok annotasiyası. Bu sinif üçün avtomatik olaraq `getter`, `setter`, `toString`, `equals` və `hashCode` metodlarını, həmçinin bütün sahələri əhatə edən bir konstruktor yaradır.
- **`@NoArgsConstructor`**: Boş (parametrsiz) bir konstruktor yaradır. Bu, JSON-dan obyektə çevirmə (deserialization) zamanı bir çox kitabxana üçün tələb olunur.
- **`@AllArgsConstructor`**: Bütün sahələri parametr kimi qəbul edən bir konstruktor yaradır.
- **`private String userId;`**: Qeydiyyatdan keçən istifadəçinin `AuthService`-dəki unikal ID-si (UUID formatında, amma `String` kimi göndərilir).
- **`private String email;`**: İstifadəçinin e-poçt ünvanı. Bu, `NotificationService` tərəfindən "Xoş gəlmisiniz" e-poçtu göndərmək üçün istifadə oluna bilər.
- **`private String verificationToken;`**: Hesab təsdiqləmə prosesi üçün yaradılmış unikal token. Bu, gələcəkdə e-poçt göndərmə məntiqi `NotificationService`-ə keçirilərsə, təsdiqləmə linkini qurmaq üçün istifadə oluna bilər.
- **`private LocalDateTime registrationTime = LocalDateTime.now();`**: Hadisənin (event) yaradıldığı dəqiq vaxtı saxlayır. Bu, hadisələrin sıralanması və ya diaqnostika üçün faydalıdır.

### 5.2. `KafkaAuthProducer.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/kafka/KafkaAuthProducer.java`

Bu servis sinifi, yuxarıda təyin edilmiş `UserRegisteredEventDTO` obyektini Kafka-ya göndərmək üçün məsuliyyət daşıyır.

```java
package com.example.authservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaAuthProducer {

    private final KafkaTemplate<String, UserRegisteredEventDTO> kafkaTemplate;

    // application.yml-dən topic adını inject edirik
    @Value("${kafka.topic.user-registration-topic}")
    private String registrationTopic;

    public KafkaAuthProducer(KafkaTemplate<String, UserRegisteredEventDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Yeni istifadəçi qeydiyyatı eventini Kafka-ya göndərir.
     * Mesaj key olaraq userId istifadə edilir.
     */
    public void sendUserRegistrationEvent(UserRegisteredEventDTO event) {
        String key = event.getUserId(); // Key, mesajların sıralanması və yerləşdirilməsi üçün faydalıdır.

        kafkaTemplate.send(registrationTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // Mesaj uğurla göndərildi, loglama (opsional)
                        System.out.println("✅ Event sent: User registered. Topic: " + registrationTopic +
                                ", Partition: " + result.getRecordMetadata().partition());
                    } else {
                        // Göndərmə uğursuz olduqda xəta idarəsi
                        System.err.println("❌ Failed to send User Registration Event: " + ex.getMessage());
                    }
                });
    }
}
```

#### Sətir-Sətir İzahı

- **`@Service`**: Bu sinifin bir Spring servis bean-i olduğunu bildirir. Bu, onun başqa komponentlərə (məsələn, `AuthService`) inject edilə bilməsini təmin edir.
- **`private final KafkaTemplate<String, UserRegisteredEventDTO> kafkaTemplate;`**: Kafka-ya mesaj göndərmək üçün Spring Kafka-nın təqdim etdiyi əsas köməkçi sinif. Generics-də `<String, UserRegisteredEventDTO>` o deməkdir ki, göndəriləcək mesajların açarı (`key`) `String`, dəyəri (`value`) isə `UserRegisteredEventDTO` tipində olacaq.
- **`@Value("${kafka.topic.user-registration-topic}")`**: `application.yml` faylından `kafka.topic.user-registration-topic` dəyərini oxuyur və `registrationTopic` dəyişəninə mənimsədir. Bu, topic adını koddan kənarda, konfiqurasiyada saxlamağa imkan verir.
- **`public KafkaAuthProducer(...)`**: Asılılıqların (bu halda `KafkaTemplate`) konstruktor vasitəsilə inject edilməsi.
- **`public void sendUserRegistrationEvent(UserRegisteredEventDTO event)`**: Əsas göndərmə metodu.
    - **`String key = event.getUserId();`**: Mesaj üçün bir açar (`key`) təyin edilir. Kafka-da eyni açara malik bütün mesajların eyni "partition"-a göndərilməsinə zəmanət verilir. Bu, həmin istifadəçi ilə bağlı bütün hadisələrin ardıcıl şəkildə emal olunmasını təmin etmək üçün vacibdir.
    - **`kafkaTemplate.send(registrationTopic, key, event)`**: Mesajı göndərmək üçün `KafkaTemplate`-in `send` metodu çağırılır. Üç parametr qəbul edir: topic adı, mesajın açarı və mesajın dəyəri (bizim DTO obyektimiz).
    - **`.whenComplete((result, ex) -> { ... });`**: `send` metodu asinxron işləyir. O, dərhal bir `CompletableFuture` obyekti qaytarır. `.whenComplete` metodu, göndərmə əməliyyatı başa çatdıqda (uğurla və ya uğursuzluqla) icra olunacaq bir "callback" funksiyası təyin etməyə imkan verir.
        - `if (ex == null)`: Əgər heç bir exception baş verməyibsə, deməli mesaj uğurla göndərilib. `result.getRecordMetadata().partition()` vasitəsilə mesajın hansı partition-a yazıldığı haqqında məlumat əldə edib loglamaq olar.
        - `else`: Əgər hər hansı bir xəta baş veribsə (məsələn, Kafka serverinə qoşulmaq mümkün olmadıqda), xəta mesajı `System.err`-ə yazılır. Real tətbiqlərdə burada daha mürəkkəb xəta idarəetməsi (məsələn, təkrar cəhd mexanizmi və ya hadisəni verilənlər bazasına yazıb sonra göndərmək) tətbiq oluna bilər.

---

## 6. `mapper` Paketi

Bu paket, Data Transfer Object (DTO) sinifləri ilə JPA Entity sinifləri arasında məlumatların çevrilməsini (mapping) avtomatlaşdıran `MapStruct` interfeyslərini saxlayır. Mapper-lərin istifadəsi, bir obyektdən digərinə sahələri manual olaraq (`user.setFirstName(dto.getFirstName())` kimi) köçürən "boilerplate" kodu aradan qaldırır, kodu daha təmiz, oxunaqlı və xətalara daha az meyilli edir.

### 6.1. `UserMapper.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/mapper/UserMapper.java`

Bu interfeys, qeydiyyat sorğusu olan `SignupRequest` DTO-sunu verilənlər bazası obyekti olan `User` entity-sinə çevirmək üçün məsuliyyət daşıyır.

```java
package com.example.authservice.mapper;

import com.example.authservice.model.dto.SignupRequest;
import com.example.authservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", ignore = true) // Parol mapper tərəfindən deyil, service tərəfindən şifrələnəcək
    User toEntity(SignupRequest signupRequest);
}
```

#### Sətir-Sətir İzahı

- **`package com.example.authservice.mapper;`**: İnterfeysin `mapper` paketinə aid olduğunu bildirir.

- **`import org.mapstruct.Mapper;`**: MapStruct kitabxanasının əsas annotasiyasını import edir.

- **`@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`**: Bu annotasiya, MapStruct-a bu interfeysi bir mapper kimi emal etməsini deyir. Layihənin "build" prosesi zamanı MapStruct bu interfeysə əsasən `UserMapperImpl.java` adlı bir implementasiya sinifi avtomatik olaraq yaradacaq.
    - **`componentModel = "spring"`**: Bu atribut, yaradılacaq `UserMapperImpl` sinifinin üzərinə `@Component` annotasiyasının əlavə edilməsini təmin edir. Nəticədə, bu mapper bir Spring bean-i olur və `AuthService` kimi digər komponentlərə asanlıqla `@Autowired` və ya konstruktor vasitəsilə inject edilə bilir.
    - **`unmappedTargetPolicy = ReportingPolicy.IGNORE`**: Bu siyasət, MapStruct-a təyinat (target) obyektdə (`User` entity-sində) mənbə (source) obyektdə (`SignupRequest` DTO-sunda) qarşılığı olmayan bir sahə olduqda nə edəcəyini deyir. `IGNORE` olaraq təyin edildikdə, MapStruct bu cür sahələrə məhəl qoymur və heç bir xəbərdarlıq və ya xəta vermir. Məsələn, `User` entity-sindəki `id`, `enabled`, `createdAt` kimi sahələrin `SignupRequest`-də qarşılığı yoxdur. Bu siyasət olmasaydı, MapStruct build zamanı bu sahələr üçün xəbərdarlıqlar verərdi.

- **`public interface UserMapper`**: Mapper interfeysinin tərifi.

- **`@Mapping(target = "password", ignore = true)`**: Bu annotasiya, çevirmə prosesi üçün xüsusi bir qayda təyin edir.
    - **`target = "password"`**: Qaydanın təyinat obyektdəki (`User`) `password` sahəsinə aid olduğunu bildirir.
    - **`ignore = true`**: MapStruct-a `SignupRequest`-dəki `password` sahəsini `User` entity-sinin `password` sahəsinə birbaşa köçürməməsini deyir. Bu, çox vacib bir təhlükəsizlik tədbiridir. Çünki parol birbaşa köçürülməməli, `AuthService`-də `PasswordEncoder` vasitəsilə şifrələnərək (hash-lənərək) təyin edilməlidir. Bu annotasiya olmasaydı, şifrə verilənlər bazasına açıq mətndə yazıla bilərdi.

- **`User toEntity(SignupRequest signupRequest);`**: Mapper metodunun imzası.
    - **Adlandırma Konvensiyası:** MapStruct, metodun adına (`toEntity`), parametrinə (`SignupRequest`) və qaytarılan tipinə (`User`) baxaraq nə edəcəyini avtomatik olaraq anlayır. O, hər iki sinifdəki eyni adlı və eyni tipdəki sahələri (məsələn, `username`, `fin`, `email`, `phone`) avtomatik olaraq bir-birinə mənimsədəcək.
    - **Avtomatik Yaradılan Kod (Arxa Planda):** MapStruct bu imza və annotasiyalara əsasən təxminən aşağıdakı kimi bir kod yaradır:
      ```java
      // Bu kod avtomatik yaradılır, siz yazmırsınız!
      public class UserMapperImpl implements UserMapper {
          @Override
          public User toEntity(SignupRequest signupRequest) {
              if (signupRequest == null) {
                  return null;
              }
              User.UserBuilder user = User.builder();
              user.username(signupRequest.getUsername());
              user.fin(signupRequest.getFin());
              user.email(signupRequest.getEmail());
              user.phone(signupRequest.getPhone());
              user.role(signupRequest.getRole());
              // Diqqət: password sahəsi @Mapping(ignore=true) olduğu üçün burada təyin edilmir.
              return user.build();
          }
      }
      ```

---

## 7. `model` Paketi

Bu paket, tətbiqin məlumat strukturlarını təyin edən bütün sinifləri saxlayır. Bu, tətbiqin onurğa sütunudur və üç əsas alt paketə bölünür: `enumeration`, `entity` və `dto`.

### 7.1. `enumeration` Alt-Paketi

Bu paket, tətbiqdə sabit, əvvəlcədən təyin edilmiş dəyərlər toplusunu təmsil edən `enum` siniflərini saxlayır.

#### `Role.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/model/enumeration/Role.java`

```java
package com.example.authservice.model.enumeration;

public enum Role {
    USER,
    ADMIN,
    PROVIDER
}
```

- **Məqsədi:** Sistemdəki istifadəçi rollarını təyin edir. `enum` istifadə etmək, rolları `String` kimi saxlamaqdan daha təhlükəsizdir, çünki bu, yazı səhvlərinin qarşısını alır (məsələn, "ADMIN" əvəzinə "admin" yazmaq) və mümkün dəyərləri məhdudlaşdırır.
- **Dəyərlər:**
    - `USER`: Standart istifadəçi. Platformanın əsas funksiyalarından istifadə edə bilər.
    - `ADMIN`: Administrator. Bütün sistem üzərində tam nəzarətə malikdir.
    - `PROVIDER`: Xidmət təminatçısı. `USER`-dən daha çox, lakin `ADMIN`-dən daha az səlahiyyətlərə malik xüsusi bir rol (məsələn, müəyyən xidmətləri təklif edə bilər).

---

### 7.2. `entity` Alt-Paketi

Bu paket, verilənlər bazasındakı cədvəllərə uyğun gələn JPA (Java Persistence API) entity siniflərini saxlayır. Hər bir entity sinifinin bir nüsxəsi cədvəldəki bir sətrə uyğun gəlir. Bu siniflər verilənlər bazası ilə birbaşa işləmək üçün istifadə olunur.

#### `User.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/model/entity/User.java`

Bu, sistemdəki istifadəçinin autentifikasiya və əsas məlumatlarını saxlayan ən vacib entity-dir.

```java
package com.example.authservice.model.entity;

// ... importlar
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "skillAuthusers",
        uniqueConstraints = { ... })
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ... digər sahələr

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = false;

    // ... UserDetails metodları
}
```

##### Sətir-Sətir İzahı (Annotasiyalar və Vacib Sahələr)

- **`@Entity`**: Bu sinifin bir JPA entity-si olduğunu və bir verilənlər bazası cədvəlinə map ediləcəyini bildirir.
- **`@Table(name = "skillAuthusers", ...)`**: Bu entity-nin `skillAuthusers` adlı cədvələ map ediləcəyini göstərir. Əgər bu annotasiya olmasaydı, cədvəlin adı default olaraq sinif adı (`user`) olardı.
    - **`uniqueConstraints = { ... }`**: Verilənlər bazası səviyyəsində məhdudiyyətlər təyin edir. Bu, eyni `username`, `fin`, `email` və ya `phone` ilə ikinci bir istifadəçinin yaradılmasının qarşısını alır. Bu, tətbiq səviyyəsindəki yoxlamalara əlavə bir qoruma qatı təmin edir.
- **`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`**: Lombok annotasiyaları.
    - `@Data`: `getter`, `setter`, `toString`, `equals`, `hashCode` yaradır.
    - `@Builder`: Builder dizayn pattern-ini tətbiq edir (`User.builder().username(...).build()` kimi obyekt yaratmağa imkan verir).
    - `@NoArgsConstructor` və `@AllArgsConstructor`: Parametrsiz və bütün sahələri əhatə edən konstruktorlar yaradır. JPA üçün boş konstruktorun olması vacibdir.
- **`implements UserDetails`**: Bu, sinifin Spring Security ilə inteqrasiya etməsini təmin edən kritik bir addımdır. `UserDetails` interfeysi, Spring Security-nin autentifikasiya və avtorizasiya üçün istifadəçidən hansı məlumatları gözlədiyini (istifadəçi adı, şifrə, rollar, hesab statusu) təyin edən bir müqavilədir (contract).

- **`@Id`**: Bu sahənin (`id`) cədvəlin birincili açarı (primary key) olduğunu bildirir.
- **`@GeneratedValue(strategy = GenerationType.AUTO)`**: Birincili açarın dəyərinin verilənlər bazası tərəfindən avtomatik olaraq necə yaradılacağını təyin edir. `AUTO` strategiyası istifadə olunan verilənlər bazasının dialektinə (məsələn, PostgreSQL üçün `SEQUENCE`) uyğun ən yaxşı strategiyanı seçir.
- **`private UUID id;`**: İstifadəçinin unikal identifikatoru. `UUID` istifadə etmək, `Long` kimi sıralı ID-lərə nisbətən daha təhlükəsizdir, çünki ID-ləri təxmin etmək mümkün deyil.

- **`@Column(nullable = false, unique = true, length = 7)`**: Sahənin cədvəldəki sütuna necə map ediləcəyini təyin edir.
    - `nullable = false`: Bu sütunun boş (`NULL`) ola bilməyəcəyini bildirir.
    - `unique = true`: Bu sütundakı bütün dəyərlərin unikal olmasını tələb edir.
    - `length = 7`: `String` tipli sütunlar üçün maksimum uzunluğu təyin edir.

- **`@Enumerated(EnumType.STRING)`**: `Role` enum-unun verilənlər bazasında necə saxlanacağını təyin edir. `EnumType.STRING` seçimi, enum dəyərinin adının (`"USER"`, `"ADMIN"`) cədvələ yazılmasını təmin edir. Bu, `EnumType.ORDINAL` (enum-un sıfırdan başlayan indeksi - 0, 1, 2) istifadəsindən daha oxunaqlı və təhlükəsizdir, çünki enum-a yeni dəyər əlavə edildikdə mövcud dəyərlərin sırası dəyişə bilər.

- **`@Builder.Default`**: `@Builder` istifadə edildikdə, bu annotasiya sahənin ilkin dəyərinin (`false` və ya `Instant.now()`) qorunmasını təmin edir. Bu olmasa, builder ilə obyekt yaradarkən bu sahələr `null` və ya `0` olardı.
- **`private boolean enabled = false;`**: Yeni qeydiyyatdan keçən istifadəçinin hesabının default olaraq deaktiv olduğunu göstərir. İstifadəçi OTP ilə hesabını təsdiqlədikdən sonra bu sahə `true` olur.

##### `UserDetails` Metodlarının Implementasiyası

- **`getAuthorities()`**: İstifadəçinin rollarını Spring Security-nin başa düşdüyü `GrantedAuthority` formatına çevirir. `SimpleGrantedAuthority` sinifi rol adının qarşısına `ROLE_` prefiksini əlavə edir (`ROLE_USER`, `ROLE_ADMIN`). `@PreAuthorize("hasRole('ADMIN')")` kimi ifadələr məhz bu prefiksli dəyərlərlə işləyir.
- **`getUsername()`**: **Çox Vacib!** `UserDetails` interfeysinin bu metodu, autentifikasiya üçün istifadə olunacaq unikal identifikatoru qaytarmalıdır. Bu layihədə, istifadəçilərin sistemə öz `fin`-ləri ilə daxil olması qərara alındığı üçün bu metod `fin` sahəsini qaytarır. Spring Security daxil olma sorğusundakı "username" sahəsini bu metodun qaytardığı dəyərlə müqayisə edəcək.
- **`isAccountNonExpired()`, `isAccountNonLocked()`, `isCredentialsNonExpired()`**: Hesabın statusunu yoxlayan metodlar. Hazırda hamısı `true` qaytarır, yəni hesabların vaxtı bitmir və ya kilidlənmir. Gələcəkdə, məsələn, çox sayda uğursuz daxil olma cəhdindən sonra hesabı kilidləmək üçün `isAccountNonLocked()` metodu `failedLoginAttempts` sahəsinə əsaslanan bir məntiqə sahib ola bilər.
- **`isEnabled()`**: Hesabın aktiv olub-olmadığını yoxlayır. `AuthService`-də bu metod, OTP ilə təsdiqlənməmiş istifadəçilərin sistemə daxil olmasının qarşısını almaq üçün istifadə olunur.

#### `RefreshToken.java` və `ConfirmationToken.java`

Bu entity-lər də oxşar JPA annotasiyaları ilə təyin edilib. Əsas fərqlər onların əlaqələrindədir:

- **`RefreshToken.java`**:
    - **`@OneToOne @JoinColumn(name = "user_id", ...)`**: `RefreshToken`-in bir `User`-ə aid olduğunu göstərən "birə-bir" əlaqə. Hər istifadəçinin yalnız bir aktiv refresh tokeni ola bilər (bu dizayna görə). `user_id` sütunu `refresh_tokens` cədvəlində `users` cədvəlinə bir "foreign key" yaradır.

- **`ConfirmationToken.java`**:
    - **`@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", ...)`**: `ConfirmationToken`-in bir `User`-ə aid olduğunu göstərən "çoxa-bir" əlaqə. Bir istifadəçinin bir neçə fərqli təsdiqləmə tokeni ola bilər (məsələn, həm hesab təsdiqləmə, həm də şifrə sıfırlama üçün). `fetch = FetchType.LAZY` təyin edilməsi vacibdir. Bu, `ConfirmationToken` yüklənərkən əlaqəli `User` obyektinin dərhal verilənlər bazasından yüklənməməsini təmin edir. `User` obyektinə yalnız ona birbaşa müraciət edildikdə (`token.getUser()`) yüklənir. Bu, performansı artırır.

---

### 7.3. `dto` Alt-Paketi

Data Transfer Object (DTO) sinifləri, API sərhədlərində (controller-lərə gələn sorğular və onlardan gedən cavablar) məlumat daşımaq üçün istifadə olunur. Onların əsas məqsədləri:

1.  **Fasad Yaratmaq:** Verilənlər bazası strukturunu (entity-ləri) xarici dünyadan gizlətmək. Klientə yalnız ehtiyacı olan məlumatları göstərmək və ondan yalnız lazım olan məlumatları qəbul etmək.
2.  **Validasiya:** Gələn məlumatların düzgünlüyünü (`@NotBlank`, `@Size`, `@Email` kimi annotasiyalarla) yoxlamaq.
3.  **Xüsusi Məlumat Strukturları:** Fərqli mənbələrdən gələn məlumatları bir obyektdə birləşdirmək.

#### `SignupRequest.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/model/dto/SignupRequest.java`

```java
@Data
@PasswordMatches
@OneOfFieldsNotBlank(fieldNames = {"email", "phone"}, ...)
public class SignupRequest {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, ...)
    private String username;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])...", ...)
    private String password;

    // ... digər sahələr
}
```

- **`@PasswordMatches`, `@OneOfFieldsNotBlank`**: Bunlar `validation` paketində yaradılmış xüsusi (custom) validasiya annotasiyalarıdır. `@PasswordMatches` `password` və `confirmPassword` sahələrinin eyni olmasını, `@OneOfFieldsNotBlank` isə `email` və `phone` sahələrindən ən azı birinin doldurulmasını tələb edir.
- **`@NotBlank`, `@Size`, `@Email`, `@Pattern`**: Bunlar `jakarta.validation.constraints` paketindən gələn standart validasiya annotasiyalarıdır. `@Valid` annotasiyası ilə birlikdə istifadə olunduqda, controller-ə gələn DTO-nun bu qaydalara uyğunluğunu avtomatik yoxlayır.
    - `@Pattern(regexp = "...")`: Şifrənin mürəkkəblik tələblərini (böyük/kiçik hərf, rəqəm, xüsusi simvol) təmin etmək üçün Regular Expression (regex) istifadə edir.

#### `LoginRequest.java`

- **`private String identifier;`**: Bu sahə, istifadəçinin sistemə `username`, `FIN` və ya `email`-dən hər hansı biri ilə daxil olmasına imkan vermək üçün yaradılıb. `AuthService`-də bu `identifier`-in hansı növ məlumat olduğunu yoxlayan məntiq var.

#### `AuthResponse.java`

- **Məqsədi:** Uğurlu daxil olma və ya token yeniləmə əməliyyatından sonra klientə qaytarılacaq məlumatları saxlayır.
- **Sahələr:** `accessToken`, `refreshToken`, istifadəçi haqqında əsas məlumatlar (`id`, `username`, `fin`, `email`, `roles`). Bu, klientin sonrakı sorğular üçün `accessToken`-i saxlamasına və istifadəçi interfeysini (məsələn, "Xoş gəldin, [username]") fərdiləşdirməsinə imkan verir.

Digər DTO-lar (`OtpSendRequest`, `ResetPasswordRequest` və s.) da oxşar şəkildə, müvafiq API endpointlərinin tələblərinə uyğun olaraq konkret məlumatları daşımaq və validasiya etmək üçün yaradılıb.

---

## 8. `repository` Paketi

Bu paket, verilənlər bazası ilə əlaqə qurmaq üçün istifadə olunan `Spring Data JPA` repository interfeyslərini saxlayır. Bu interfeyslər, heç bir SQL sorğusu yazmadan verilənlər bazası əməliyyatlarını (CRUD - Create, Read, Update, Delete və daha mürəkkəb sorğular) həyata keçirməyə imkan verir. Spring Data JPA, metod adlarına əsasən müvafiq sorğuları avtomatik olaraq generasiya edir.

### 8.1. `UserRepository.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/repository/UserRepository.java`

Bu interfeys, `User` entity-si ilə bağlı verilənlər bazası əməliyyatları üçün məsuliyyət daşıyır.

```java
package com.example.authservice.repository;

import com.example.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByFin(String fin);
    Optional<User> findByPhone(String phone);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByFin(String fin);
    Boolean existsByPhone(String phone);
}
```

#### Sətir-Sətir İzahı

- **`@Repository`**: Bu annotasiya, sinifin bir Spring Data repository-si olduğunu bildirir. Bu, Spring-in bu interfeys üçün avtomatik olaraq bir proksi (proxy) implementasiya yaratmasını təmin edir. Həmçinin, bu annotasiya sayəsində JPA ilə bağlı xətalar (məsələn, `PersistenceException`) avtomatik olaraq Spring-in standart `DataAccessException` iyerarxiyasına çevrilir.
- **`public interface UserRepository extends JpaRepository<User, UUID>`**: `UserRepository` interfeysini təyin edir və `JpaRepository`-ni genişləndirir.
    - **`JpaRepository<User, UUID>`**: Bu, Spring Data JPA-nın əsas interfeyslərindən biridir. Generics-dəki parametrlər:
        - `User`: Bu repository-nin idarə edəcəyi entity sinifi.
        - `UUID`: Həmin entity-nin birincili açarının (`@Id` sahəsinin) tipi.
    - `JpaRepository`-ni genişləndirmək, `UserRepository`-yə heç bir kod yazmadan bir çox standart metodu (məsələn, `save()`, `findById()`, `findAll()`, `deleteById()`, `count()`) miras olaraq verir.

- **`Optional<User> findByUsername(String username);`**: Bu, Spring Data JPA-nın "Query Creation from Method Names" (Metod Adlarından Sorğu Yaratma) xüsusiyyətinin bir nümunəsidir.
    - **`find...By...`**: Spring Data bu prefiksə baxaraq bunun bir axtarış sorğusu olduğunu anlayır.
    - **`Username`**: `User` entity-sindəki `username` sahəsinə görə axtarış ediləcəyini bildirir.
    - **`String username`**: Metodun parametri, axtarış üçün istifadə olunacaq dəyərdir.
    - **Nəticə:** Spring Data bu metod imzasına əsasən arxa planda təxminən `SELECT u FROM User u WHERE u.username = ?1` kimi bir JPQL (Java Persistence Query Language) sorğusu yaradır.
    - **`Optional<User>`**: Qaytarılan tipin `Optional` olması, axtarış nəticəsində heç bir istifadəçi tapılmadıqda `NullPointerException` xətasının qarşısını almaq üçün yaxşı bir praktikadır. Nəticənin olub-olmadığını `isPresent()` metodu ilə yoxlamaq və ya `orElseThrow()` kimi metodlarla nəticə olmadıqda exception atmaq olar.

- **`findByEmail`, `findByFin`, `findByPhone`**: Bu metodlar da eyni məntiqlə, müvafiq olaraq `email`, `fin` və `phone` sahələrinə görə axtarış edən sorğular yaradır.

- **`Boolean existsByUsername(String username);`**: Bu da bir törəmə sorğudur (derived query).
    - **`exists...By...`**: Spring Data bu prefiksə əsasən, tam obyekti qaytarmaq əvəzinə, verilən şərtə uyğun bir qeydin mövcud olub-olmadığını yoxlayan və `boolean` dəyər qaytaran daha səmərəli bir sorğu (`SELECT COUNT(...) > 0` kimi) yaradır. Bu, sadəcə bir qeydin varlığını yoxlamaq lazım olduqda (`registerUser` metodundakı yoxlamalar kimi) `findBy...` metodunu çağırıb nəticənin `null` olub-olmadığını yoxlamaqdan daha performanslıdır.

### 8.2. `RefreshTokenRepository.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/repository/RefreshTokenRepository.java`

```java
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    int deleteByUser(User user);
}
```

- **`extends JpaRepository<RefreshToken, Long>`**: Bu repository `RefreshToken` entity-sini idarə edir və onun birincili açarı `Long` tipindədir. (Qeyd: `RefreshToken` entity-sində ID `UUID` olaraq göstərilib, burada `Long` olması bir uyğunsuzluqdur. Kodda `UUID` olmalıdır).
- **`Optional<RefreshToken> findByToken(String token);`**: Refresh tokenin özünün dəyərinə (`token` sahəsinə) görə axtarış edir. Bu, klientdən gələn refresh tokeni təsdiqləmək üçün istifadə olunur.
- **`Optional<RefreshToken> findByUser(User user);`**: Verilmiş `User` obyektinə bağlı olan refresh tokeni tapır.
- **`int deleteByUser(User user);`**: Verilmiş istifadəçiyə aid bütün refresh tokenləri silir. Bu, `logout` zamanı istifadə olunur. Metodun `int` qaytarması, silinən sətirlərin sayını göstərir.

### 8.3. `ConfirmationTokenRepository.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/repository/ConfirmationTokenRepository.java`

```java
@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, UUID> {

    Optional<ConfirmationToken> findByTokenAndTypeAndUsedFalseAndExpiresAtAfter(String token, String type, Instant now);

    @Modifying
    @Query("DELETE FROM ConfirmationToken ct WHERE ct.user.id = :userId AND ct.type = :type")
    void deleteAllByUserIdAndType(UUID userId, String type);

    // ... digər metodlar
}
```

- **`Optional<ConfirmationToken> findByTokenAndTypeAndUsedFalseAndExpiresAtAfter(...)`**: Bu, Spring Data JPA-nın gücünü göstərən daha mürəkkəb bir törəmə sorğudur. Metodun adı bir neçə şərti birləşdirir:
    - `findBy...`: Axtarış et.
    - `Token`: `token` sahəsi bərabər olsun...
    - `And`: VƏ...
    - `Type`: `type` sahəsi bərabər olsun...
    - `And`: VƏ...
    - `UsedFalse`: `used` sahəsi `false` olsun.
    - `And`: VƏ...
    - `ExpiresAtAfter`: `expiresAt` sahəsi verilən zamandan (`now`) **sonra** olsun.
    - Bu metod, etibarlı (vaxtı keçməmiş və istifadə olunmamış) bir OTP kodunu yoxlamaq üçün mükəmməl bir yoldur.

- **`@Modifying`**: Bu annotasiya, Spring Data JPA-ya bu sorğunun verilənlər bazasını dəyişdirən bir əməliyyat (`UPDATE`, `DELETE`) olduğunu bildirir. Bu annotasiya olmadan, dəyişiklik edən `@Query` sorğuları işləməyəcək.
- **`@Query("...")`**: Metod adından sorğu yaratmaq mümkün olmadıqda və ya daha mürəkkəb bir məntiq tələb olunduqda, JPQL (və ya native SQL) sorğusunu birbaşa yazmağa imkan verir.
    - **`DELETE FROM ConfirmationToken ct WHERE ct.user.id = :userId AND ct.type = :type`**: Bu JPQL sorğusu, verilmiş istifadəçi ID-si (`:userId`) və token növü (`:type`) üçün bütün `ConfirmationToken` qeydlərini silir. `ct` burada `ConfirmationToken` üçün bir aliasdır. `:userId` və `:type` isə metodun parametrlərinə istinad edən adlandırılmış parametrlərdir. Bu metod, yeni bir OTP göndərməzdən əvvəl köhnə, eyni tipli OTP-ləri təmizləmək üçün istifadə olunur.

---

## 9. `security` Paketi

Bu paket, Spring Security ilə inteqrasiyanı və JWT (JSON Web Token) əsaslı autentifikasiya mexanizmini həyata keçirən bütün sinifləri özündə cəmləşdirir. Bu, tətbiqin təhlükəsizliyinin əsasını təşkil edir və iki alt paketə bölünür: `jwt` və `services`.

### 9.1. `jwt` Alt-Paketi

Bu paket, JWT tokenlərinin yaradılması, təsdiqlənməsi, sorğulardan oxunması və təhlükəsizlik kontekstinə əlavə edilməsi ilə bağlı bütün məntiqi saxlayır.

#### `JwtUtils.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/security/jwt/JwtUtils.java`

Bu, JWT ilə bağlı bütün yardımçı metodları saxlayan mərkəzi sinifdir.

```java
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    // ...

    private Key getSigningKey() { ... }

    public String generateTokenFromUsername(String username) { ... }

    public String getUserFinFromJwtToken(String token) { ... }

    public boolean validateJwtToken(String authToken) { ... }

    public RefreshToken createRefreshToken(...) { ... }

    public RefreshToken verifyRefreshToken(String token) { ... }
}
```

##### Sətir-Sətir İzahı (Əsas Metodlar)

- **`@Value` annotasiyaları**: `application.yml`-dən JWT-nin gizli açarını (`jwtSecret`) və etibarlılıq müddətlərini (`jwtExpirationMs`, `refreshTokenExpirationMs`) oxuyur.
- **`getSigningKey()`**: `jwtSecret`-i (BASE64 formatında olan) deşifrə edərək tokenləri imzalamaq və təsdiqləmək üçün istifadə olunacaq bir `Key` obyekti yaradır. `Keys.hmacShaKeyFor()` metodu HMAC-SHA alqoritmləri üçün təhlükəsiz bir açar yaradır.
- **`generateTokenFromUsername(String username)`**: Verilmiş istifadəçi adı (bu tətbiqdə FIN) üçün yeni bir Access Token yaradır.
    - `Jwts.builder()`: `jjwt` kitabxanasından gələn, token qurmaq üçün istifadə olunan bir builder.
    - `.setSubject(username)`: Tokenin "subject"-ini, yəni kimə aid olduğunu təyin edir. Bu, tokenin əsas məlumatıdır.
    - `.setIssuedAt(new Date())`: Tokenin nə vaxt yaradıldığını təyin edir.
    - `.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))`: Tokenin bitmə tarixini təyin edir (hazırkı vaxt + konfiqurasiyadan gələn müddət).
    - `.signWith(getSigningKey(), SignatureAlgorithm.HS512)`: Tokeni yuxarıda yaradılmış açar və HS512 alqoritmi ilə imzalayır. İmza, tokenin məzmununun sonradan dəyişdirilmədiyini təsdiqləmək üçün istifadə olunur.
    - `.compact()`: Qurulmuş və imzalanmış tokeni `String` formatına çevirir.
- **`getUserFinFromJwtToken(String token)`**: Verilmiş tokeni parse edərək onun "subject" hissəsini (yəni istifadəçinin FIN-ini) çıxarır. Bu, `AuthTokenFilter`-də istifadəçini tapmaq üçün istifadə olunur.
- **`validateJwtToken(String authToken)`**: Verilmiş tokenin etibarlı olub-olmadığını yoxlayır.
    - `Jwts.parserBuilder().setSigningKey(...).build().parseClaimsJws(authToken);`: Bu sətir tokeni parse etməyə cəhd edir. Proses zamanı `jjwt` kitabxanası aşağıdakıları avtomatik yoxlayır:
        1.  **İmza:** Tokenin imzası `jwtSecret` ilə uyğun gəlirmi? Əgər uyğun gəlmirsə, `SecurityException` və ya `MalformedJwtException` atılır (token saxtadır).
        2.  **Bitmə Tarixi:** Tokenin `exp` (expiration) sahəsi hazırkı vaxtdan keçibmi? Əgər keçibsə, `ExpiredJwtException` atılır.
    - Əgər heç bir exception atılmazsa, metod `true` qaytarır, əks halda xətanı loglayır və `false` qaytarır.
- **`createRefreshToken(...)` və `verifyRefreshToken(...)`**: Access token-lər qısa ömürlü olduğu üçün, istifadəçinin hər dəfə yenidən login olmasının qarşısını almaq üçün istifadə olunan Refresh Token-lərin məntiqini idarə edir. `createRefreshToken` yeni bir refresh token yaradıb verilənlər bazasına yazır, `verifyRefreshToken` isə verilən tokenin bazada mövcudluğunu və vaxtının keçmədiyini yoxlayır.

#### `AuthTokenFilter.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/security/jwt/AuthTokenFilter.java`

Bu sinif, hər bir HTTP sorğusunu yoxlayan və etibarlı bir JWT tokeni varsa, istifadəçini autentifikasiya edən bir Spring Security filtridir.

```java
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String fin = jwtUtils.getUserFinFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(fin);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) { ... }
}
```

##### Sətir-Sətir İzahı

- **`extends OncePerRequestFilter`**: Bu, Spring tərəfindən təmin edilən bir baza sinifidir və bu filtrin hər bir sorğu üçün **yalnız bir dəfə** icra olunacağına zəmanət verir.
- **`doFilterInternal(...)`**: Filtrin əsas məntiqinin yerləşdiyi metod.
    1.  **`String jwt = parseJwt(request);`**: `parseJwt` köməkçi metodu çağırılır. Bu metod, sorğunun `Authorization` başlığını oxuyur, `"Bearer "` prefiksinin olub-olmadığını yoxlayır və əgər varsa, prefiksi kəsərək xalis tokeni qaytarır.
    2.  **`if (jwt != null && jwtUtils.validateJwtToken(jwt))`**: Əgər token tapılıbsa və `JwtUtils` tərəfindən təsdiqlənibsə, blokun içi icra olunur.
    3.  **`String fin = jwtUtils.getUserFinFromJwtToken(jwt);`**: Tokenin içindən istifadəçinin FIN-i çıxarılır.
    4.  **`UserDetails userDetails = userDetailsService.loadUserByUsername(fin);`**: Həmin FIN-ə uyğun istifadəçi məlumatları (`UserDetails` obyekti) verilənlər bazasından yüklənir.
    5.  **`UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(...)`**: Spring Security-nin başa düşdüyü bir `Authentication` obyekti yaradılır. Bu obyekt, autentifikasiya olunmuş istifadəçini, onun parolu (artıq lazım olmadığı üçün `null`) və səlahiyyətlərini (rollarını) saxlayır.
    6.  **`SecurityContextHolder.getContext().setAuthentication(authentication);`**: Bu, ən vacib sətirdir. Yaradılmış `authentication` obyekti `SecurityContextHolder`-a yerləşdirilir. Bu andan etibarən, həmin sorğu prosesinin qalan hissəsi üçün Spring Security bu istifadəçini **autentifikasiya olunmuş** hesab edir. `@PreAuthorize` kimi annotasiyalar və controller-də `AuthenticationPrincipal` kimi parametrlər məhz bu kontekstdən məlumatlarını götürür.
    7.  **`catch (Exception e)`**: Proses zamanı hər hansı bir xəta baş verərsə, loglanır, lakin sorğunun davam etməsinə icazə verilir (istifadəçi sadəcə autentifikasiya olunmamış qalır).
    8.  **`filterChain.doFilter(request, response);`**: Sorğunu təhlükəsizlik zəncirindəki növbəti filtrə ötürür. Bu sətir mütləq çağırılmalıdır, əks halda sorğu heç vaxt controller-ə çatmaz.

#### `AuthEntryPointJwt.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/security/jwt/AuthEntryPointJwt.java`

- **Məqsədi:** Bu sinif, bir istifadəçi heç bir etimadnamə (credentials) təqdim etmədən (yəni, etibarlı JWT tokeni olmadan) qorunan bir resursa daxil olmağa cəhd etdikdə işə düşür. `SecurityConfig`-də `exceptionHandling().authenticationEntryPoint()` ilə təyin edilir.
- **`commence(...)` metodu**: Bu metod, sadəcə olaraq cavabın status kodunu `401 Unauthorized` olaraq təyin edir və bir xəta mesajı göndərir.

### 9.2. `services` Alt-Paketi

Bu paket, Spring Security-nin istifadəçi məlumatlarını necə əldə edəcəyini təyin edən sinifləri saxlayır.

#### `UserDetailsServiceImpl.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/security/services/UserDetailsServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByFin(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseGet(() -> userRepository.findByUsername(identifier)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier))));

        return UserDetailsImpl.build(user);
    }
}
```

- **`implements UserDetailsService`**: Spring Security-nin tələb etdiyi əsas interfeys. Bu interfeysin yalnız bir metodu var: `loadUserByUsername`.
- **`loadUserByUsername(String identifier)`**: Spring Security autentifikasiya prosesi zamanı (həm `login` zamanı, həm də `AuthTokenFilter`-də) bu metodu çağırır. Parametr olaraq verilən `identifier` (istifadəçi adı, FIN, email) əsasında istifadəçini tapıb, onu `UserDetails` tipində bir obyektə çevirib qaytarmalıdır.
- **Məntiq:** Metod, verilən `identifier`-in FIN, email və ya username ola biləcəyini nəzərə alaraq, `UserRepository`-dən istifadə edərək üç fərqli sahə üzrə axtarış aparır. `orElseGet` zənciri sayəsində, birinci axtarış uğursuz olarsa, ikincisi, o da uğursuz olarsa, üçüncüsü icra olunur. Əgər heç biri ilə istifadəçi tapılmazsa, `UsernameNotFoundException` atılır. Bu exception Spring Security tərəfindən tutulur və "Bad credentials" xətasına səbəb olur.
- **`return UserDetailsImpl.build(user);`**: Tapılmış `User` entity-si, `UserDetailsImpl.build()` statik metodu vasitəsilə Spring Security-nin başa düşdüyü `UserDetails` formatına (`UserDetailsImpl` obyektinə) çevrilərək qaytarılır.
- **`@Transactional`**: Bu metodun bir verilənlər bazası tranzaksiyası daxilində icra olunmasını təmin edir. Bu, xüsusilə `LAZY` fetch strategiyası ilə yüklənən sahələrə müraciət edərkən `LazyInitializationException`-ın qarşısını almaq üçün vacibdir.

#### `UserDetailsImpl.java`
**Fayl Yolu:** `src/main/java/com/example/authservice/security/services/UserDetailsImpl.java`

- **Məqsədi:** Bu sinif, `UserDetails` interfeysinin konkret bir implementasiyasıdır. O, `User` entity-sindən alınan məlumatları (ID, FIN, parol, rollar və s.) Spring Security-nin istifadə edəcəyi bir formatda saxlayan bir "wrapper" sinifidir. `Authentication` obyektinin içində məhz bu obyekt saxlanılır.
- **`build(User user)` metodu**: `User` entity-sini parametr kimi qəbul edib, ondan yeni bir `UserDetailsImpl` obyekti yaradan bir "factory" metodudur.

---

## 10. `service` Paketi

Bu paket, tətbiqin əsas biznes məntiqini (business logic) həyata keçirən servis siniflərini saxlayır. Controller-lər sorğuları qəbul edib bu servislərə ötürür, servislər isə lazımi yoxlamaları, hesablamaları, verilənlər bazası əməliyyatlarını (repozitorilər vasitəsilə) və digər servislərlə əlaqəni idarə edir.

### 10.1. `EmailService.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/service/EmailService.java`

Bu servis, tətbiqdən xarici e-poçt ünvanlarına mesaj göndərmək üçün məsuliyyət daşıyır.

```java
package com.example.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async("taskExecutor") // `AsyncConfig`də təyin olunmuş `taskExecutor` bean-ini istifadə edir
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true for multipart, UTF-8 for encoding
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true for HTML content
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email göndərilərkən xəta baş verdi: " + e.getMessage());
        }
    }
    // ... sendTestEmail metodu
}
```

#### Sətir-Sətir İzahı

- **`@Service`, `@RequiredArgsConstructor`, `@Slf4j`**: Standart servis sinifi üçün Lombok və Spring annotasiyaları.
- **`private final JavaMailSender mailSender;`**: E-poçt göndərmək üçün Spring Framework-un təqdim etdiyi əsas interfeys. Onun konkret implementasiyası və SMTP serveri ilə bağlı parametrlər (`host`, `port`, `username`, `password`) `application.yml`-də `spring.mail` altında konfiqurasiya olunur.
- **`@Async("taskExecutor")`**: **Çox Vacib!** Bu annotasiya, `sendEmail` metodunun çağırıldığı `thread`-də deyil, `AsyncConfig`-də konfiqurasiya etdiyimiz `taskExecutor` `thread` hovuzundakı ayrı bir `thread`-də icra olunmasını təmin edir. Bu, performansı kəskin şəkildə artırır. Əgər bu annotasiya olmasaydı, istifadəçi OTP tələb etdikdə, API cavabı e-poçt göndərmə prosesi (SMTP serveri ilə əlaqə, mesajın göndərilməsi) bitənə qədər gözləyəcəkdi. Bu, bir neçə saniyə çəkə bilər. `@Async` sayəsində isə, metod çağırılan kimi API dərhal `200 OK` cavabı qaytarır və e-poçt arxa fonda göndərilir.
- **`public void sendEmail(String to, String subject, String body)`**: Metodun imzası. Kimə, hansı mövzuda və hansı məzmunda e-poçt göndəriləcəyini təyin edir.
- **`MimeMessage message = mailSender.createMimeMessage();`**: `JavaMailSender` vasitəsilə, HTML, şəkil və ya qoşma (attachment) kimi mürəkkəb məzmunu dəstəkləyən bir `MimeMessage` obyekti yaradılır.
- **`MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");`**: `MimeMessage`-i asanlıqla konfiqurasiya etmək üçün bir köməkçi sinif. `true` parametri mesajın "multipart" (çoxhissəli) ola biləcəyini, `"UTF-8"` isə Azərbaycan hərfləri kimi simvolların düzgün göstərilməsi üçün kodlaşdırmanı təyin edir.
- **`helper.setTo(to);`**, **`helper.setSubject(subject);`**: Mesajın alıcısını və mövzusunu təyin edir.
- **`helper.setText(body, true);`**: Mesajın mətnini təyin edir. İkinci parametr olan `true`, `body` məzmununun düz mətn kimi deyil, HTML kimi interpretasiya olunmasını təmin edir. Bu, `<h1>`, `<br>` kimi teqlərdən istifadə etməyə imkan verir.
- **`mailSender.send(message);`**: Hazırlanmış mesajı göndərir.
- **`catch (MessagingException e)`**: E-poçt göndərilərkən hər hansı bir problem (məsələn, SMTP serverinə qoşula bilməmək, yanlış ünvan) baş verərsə, `MessagingException` atılır. Bu xəta loglanır və tətbiqin dayanmaması üçün `RuntimeException` olaraq yenidən atılır.

### 10.2. `AuthService.java`

**Fayl Yolu:** `src/main/java/com/example/authservice/service/AuthService.java`

Bu, tətbiqin əsas biznes məntiqini özündə cəmləşdirən ən böyük və ən vacib servis sinifidir. Bütün qeydiyyat, autentifikasiya, OTP və token əməliyyatları burada idarə olunur.

```java
@Service
@Slf4j
public class AuthService {

    // Asılılıqlar (Dependencies)
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final KafkaAuthProducer kafkaProducer;

    @Value("${otp.expiration-seconds}")
    private long otpExpirationSeconds;

    // Konstruktor

    // Metodlar
}
```

#### Asılılıqların İzahı

- **`UserRepository`**: `User` entity-ləri ilə verilənlər bazası əməliyyatları aparmaq üçün.
- **`PasswordEncoder`**: Şifrələri şifrələmək (hash-ləmək) üçün.
- **`UserMapper`**: `SignupRequest` DTO-sunu `User` entity-sinə çevirmək üçün.
- **`AuthenticationManager`**: Spring Security-nin əsas autentifikasiya komponenti. İstifadəçi adı/şifrəni yoxlamaq üçün istifadə olunur.
- **`JwtUtils`**: JWT Access və Refresh tokenlərini yaratmaq və idarə etmək üçün.
- **`RefreshTokenRepository`**: Refresh tokenləri verilənlər bazasında saxlamaq və axtarmaq üçün.
- **`EmailService`**: OTP kodlarını e-poçt vasitəsilə göndərmək üçün.
- **`ConfirmationTokenRepository`**: OTP kodlarını (tokenlərini) verilənlər bazasında saxlamaq və yoxlamaq üçün.
- **`KafkaAuthProducer`**: Yeni istifadəçi qeydiyyatı kimi hadisələri Kafka-ya göndərmək üçün.
- **`@Value("${otp.expiration-seconds}")`**: `application.yml`-dən OTP kodlarının etibarlılıq müddətini (saniyə ilə) oxuyur.

--- 

#### Metodların Detallı İzahı

##### **`registerUser` metodu**

```java
@Transactional
public User registerUser(SignupRequest signupRequest) {
    // 1. Mövcudluq yoxlamaları
    if (userRepository.existsByFin(signupRequest.getFin())) {
        throw new UserAlreadyExistsException("FIN", signupRequest.getFin());
    }
    // ... email, username, phone üçün oxşar yoxlamalar

    // 2. Entity yaratmaq və şifrəni kodlamaq
    User user = userMapper.toEntity(signupRequest);
    user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
    user.setRole(Role.USER); // Default rol
    user.setEnabled(false); // Hesab hələ aktiv deyil

    // 3. Verilənlər bazasına yazmaq
    User savedUser = userRepository.save(user);

    // 4. Kafka hadisəsi göndərmək
    UserRegisteredEventDTO event = new UserRegisteredEventDTO(...);
    kafkaProducer.sendUserRegistrationEvent(event);
    log.info("Kafka user registration event sent for user: {}", savedUser.getUsername());

    return savedUser;
}
```

- **`@Transactional`**: Bu annotasiya, metodun bir verilənlər bazası tranzaksiyası daxilində icra olunmasını təmin edir. Əgər metodun icrası zamanı hər hansı bir `RuntimeException` baş verərsə (məsələn, Kafka-ya göndərmə zamanı xəta), bu tranzaksiya daxilində edilmiş bütün verilənlər bazası dəyişiklikləri (bu halda, istifadəçinin `save` edilməsi) avtomatik olaraq geri qaytarılacaq (`rollback`). Bu, verilənlər bazasının bütövlüyünü qoruyur.
- **Addım 1 (Mövcudluq yoxlamaları):** `userRepository`-nin `existsBy...` metodlarından istifadə edərək, təqdim edilən `fin`, `username`, `email` və `phone`-un sistemdə artıq mövcud olub-olmadığı yoxlanılır. Əgər hər hansı biri mövcuddursa, müvafiq `UserAlreadyExistsException` atılaraq proses dayandırılır.
- **Addım 2 (Entity yaratmaq):** `UserMapper` vasitəsilə DTO `User` entity-sinə çevrilir. **Vacib:** Şifrə birbaşa köçürülmür. `passwordEncoder.encode()` metodu ilə şifrə BCrypt alqoritmi ilə hash-lənir və sonra entity-yə təyin edilir. Yeni istifadəçiyə default olaraq `USER` rolu verilir və hesabı OTP təsdiqi gözlədiyi üçün `enabled` statusu `false` olaraq təyin edilir.
- **Addım 3 (Verilənlər bazasına yazmaq):** Hazırlanmış `user` obyekti `userRepository.save()` metodu ilə verilənlər bazasına yazılır. `save` metodu, verilənlər bazasına yazılmış və ID-si təyin edilmiş obyekti (`savedUser`) geri qaytarır.
- **Addım 4 (Kafka hadisəsi göndərmək):** Yeni istifadəçinin qeydiyyatdan keçdiyini digər servislərə bildirmək üçün `UserRegisteredEventDTO` yaradılır və `kafkaProducer` vasitəsilə müvafiq topic-ə göndərilir.

##### **`sendOtp` metodu**

```java
@Transactional
public void sendOtp(OtpSendRequest otpSendRequest) {
    // 1. İstifadəçini tapmaq
    User user = findUserByIdentifier(otpSendRequest.getIdentifier());

    // 2. OTP növünü və məqsədini yoxlamaq
    String otpType = otpSendRequest.getOtpType().toUpperCase();
    // ... (ACCOUNT_CONFIRMATION və ya PASSWORD_RESET yoxlamaları)

    // 3. Köhnə OTP-ləri silmək
    confirmationTokenRepository.deleteAllByUserIdAndType(user.getId(), otpType);

    // 4. Yeni OTP yaratmaq və saxlamaq
    String otpCode = generateOtpCode();
    ConfirmationToken confirmationToken = new ConfirmationToken(user, otpCode, otpType, otpExpirationSeconds);
    confirmationTokenRepository.save(confirmationToken);

    // 5. OTP-ni göndərmək
    switch (sendMethod) {
        case "email":
            // ...
            emailService.sendEmail(user.getEmail(), "Verification Code - AuthService", emailBody);
            break;
        // ...
    }
}
```

- **Addım 1 (İstifadəçini tapmaq):** `findUserByIdentifier` köməkçi metodu ilə istifadəçi FIN/email/username-ə görə tapılır.
- **Addım 2 (OTP növünü yoxlamaq):** OTP-nin məqsədi yoxlanılır. Məsələn, əgər `ACCOUNT_CONFIRMATION` üçün OTP istənilibsə, amma istifadəçinin hesabı artıq aktivdirsə (`user.isEnabled()` `true`-dursa), `OtpException` atılır.
- **Addım 3 (Köhnə OTP-ləri silmək):** Yeni OTP yaratmazdan əvvəl, həmin istifadəçi üçün eyni növdə olan bütün köhnə, istifadə olunmamış OTP-lər verilənlər bazasından silinir. Bu, çaşqınlığın qarşısını alır.
- **Addım 4 (Yeni OTP yaratmaq):** 6 rəqəmli təsadüfi bir kod (`generateOtpCode`) yaradılır. Bu kod, istifadəçi, növ və `application.yml`-dən gələn bitmə müddəti ilə birlikdə yeni bir `ConfirmationToken` obyektinə yerləşdirilir və verilənlər bazasına yazılır.
- **Addım 5 (OTP-ni göndərmək):** `otpSendRequest`-də göstərilən metoda (`email` və ya gələcəkdə `phone`) uyğun olaraq, `EmailService` (və ya gələcəkdə `SmsService`) vasitəsilə OTP kodu istifadəçiyə göndərilir.

##### **`authenticateUser` metodu**

```java
@Transactional
public AuthResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
    // 1. İstifadəçini tapmaq və statusunu yoxlamaq
    User user = findUserByIdentifier(loginRequest.getIdentifier());
    if (!user.isEnabled()) {
        throw new InvalidCredentialsException("Account is not activated. Please verify your account.");
    }

    // 2. Spring Security ilə autentifikasiya
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword()));

    // 3. Təhlükəsizlik kontekstini təyin etmək
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 4. Tokenləri yaratmaq
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername()); // getUsername() -> FIN

    // ... Refresh token yaratmaq (IP və User-Agent ilə)
    RefreshToken refreshToken = jwtUtils.createRefreshToken(user, ipAddress, userAgent);

    // 5. Cavabı hazırlamaq
    return new AuthResponse(jwt, refreshToken.getToken(), ...);
}
```

- **Addım 1 (Status yoxlaması):** İstifadəçi tapıldıqdan sonra onun `enabled` statusu yoxlanılır. Əgər `false`-dursa, hesab aktivləşdirilməyib və daxil olmasına icazə verilmir.
- **Addım 2 (Autentifikasiya):** Bu, ən vacib hissədir. `authenticationManager.authenticate()` metodu çağırılır. `AuthenticationManager` (hansı ki, arxa planda bizim `DaoAuthenticationProvider`-ı istifadə edir) aşağıdakı işləri görür:
    a. `UserDetailsServiceImpl.loadUserByUsername()` metodunu çağıraraq verilən `identifier`-ə uyğun istifadəçini tapır.
    b. `PasswordEncoder` vasitəsilə `loginRequest`-dən gələn `password`-u hash-ləyir.
    c. Alınan hash ilə verilənlər bazasından gələn `User` obyektinin hash-lənmiş şifrəsini müqayisə edir.
    d. Əgər şifrələr uyğun gəlmirsə, `BadCredentialsException` atır. Əgər uyğun gəlirsə, tam doldurulmuş bir `Authentication` obyekti qaytarır.
- **Addım 3 (Konteksti təyin etmək):** Uğurlu autentifikasiyadan sonra alınan `authentication` obyekti `SecurityContextHolder`-a yerləşdirilir. Bu, istifadəçinin bu sessiya üçün "daxil olmuş" hesab edilməsini təmin edir.
- **Addım 4 (Tokenləri yaratmaq):** `JwtUtils` vasitəsilə istifadəçinin FIN-inə (`userDetails.getUsername()`) bağlı yeni bir Access Token və verilənlər bazasına yazılan bir Refresh Token yaradılır.
- **Addım 5 (Cavabı hazırlamaq):** Yaradılmış tokenlər və istifadəçi məlumatları ilə birlikdə `AuthResponse` DTO-su yaradılaraq controller-ə qaytarılır.

---

## 11. `validation` Paketi

Bu paket, `jakarta.validation` standartının təqdim etdiyi imkanları genişləndirərək, tətbiqə məxsus mürəkkəb validasiya qaydalarını həyata keçirmək üçün istifadə olunan xüsusi (custom) annotasiyaları və onların validator siniflərini saxlayır. Bu, biznes məntiqini birbaşa DTO-ların üzərində deklarativ şəkildə ifadə etməyə imkan verir.

### 11.1. `PasswordMatches` Annotasiyası və Validatoru

Bu cütlük, qeydiyyat zamanı daxil edilən `password` və `confirmPassword` sahələrinin eyni olmasını təmin edir.

#### `PasswordMatches.java` (Annotasiya)
**Fayl Yolu:** `src/main/java/com/example/authservice/validation/PasswordMatches.java`

```java
package com.example.authservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {
    String message() default "Passwords do not match.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

- **`@Target({ElementType.TYPE, ...})`**: Bu annotasiyanın hara tətbiq oluna biləcəyini göstərir. `ElementType.TYPE` o deməkdir ki, bu annotasiya birbaşa sinifin (`SignupRequest` kimi) üzərinə yazıla bilər. Bu vacibdir, çünki validatorun eyni anda bir neçə sahəyə (həm `password`, həm də `confirmPassword`) çıxışı olmalıdır.
- **`@Retention(RetentionPolicy.RUNTIME)`**: Annotasiyanın işləmə zamanı (runtime) da əlçatan olmasını təmin edir ki, Spring-in validasiya mexanizmi onu görə bilsin.
- **`@Constraint(validatedBy = PasswordMatchesValidator.class)`**: **Ən vacib hissə.** Bu annotasiyanın məntiqini hansı sinifin (`PasswordMatchesValidator`) həyata keçirəcəyini göstərir.
- **`message()`**, **`groups()`**, **`payload()`**: Bunlar standart `jakarta.validation` annotasiyaları üçün tələb olunan standart elementlərdir.
    - `message()`: Validasiya uğursuz olduqda qaytarılacaq default xəta mesajını təyin edir.
    - `groups()` və `payload()`: Daha mürəkkəb validasiya ssenariləri üçün istifadə olunur (məsələn, validasiyaları qruplaşdırmaq).

#### `PasswordMatchesValidator.java` (Validator)
**Fayl Yolu:** `src/main/java/com/example/authservice/validation/PasswordMatchesValidator.java`

```java
package com.example.authservice.validation;

import com.example.authservice.model.dto.SignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // İlkinləşdirmə yoxdur
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (!(obj instanceof SignupRequest)) {
            return false;
        }

        SignupRequest user = (SignupRequest) obj;

        boolean isValid = user.getPassword().equals(user.getConfirmPassword());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
```

- **`implements ConstraintValidator<PasswordMatches, Object>`**: Bu sinifin `PasswordMatches` annotasiyası üçün bir validator olduğunu və `Object` tipindəki obyektləri (yəni, annotasiyanın tətbiq olunduğu sinifi) validasiya edəcəyini bildirir.
- **`isValid(Object obj, ConstraintValidatorContext context)`**: Əsas validasiya məntiqinin yerləşdiyi metod.
    - `Object obj`: Annotasiyanın üzərinə yazıldığı obyektin özü (bizim halda `SignupRequest` obyekti).
    - `ConstraintValidatorContext context`: Validasiya prosesi haqqında kontekst məlumatları saxlayır və xəta mesajını fərdiləşdirməyə imkan verir.
    - **`SignupRequest user = (SignupRequest) obj;`**: Obyekti lazımi tipə cast edirik.
    - **`boolean isValid = user.getPassword().equals(user.getConfirmPassword());`**: Əsas müqayisə burada aparılır.
    - **`if (!isValid)` bloku**: Əgər parollar uyğun gəlmirsə:
        - `context.disableDefaultConstraintViolation();`: Default xəta mesajının avtomatik olaraq sinif səviyyəsində deyil, bizim təyin etdiyimiz sahəyə bağlanmasını təmin edir.
        - `context.buildConstraintViolationWithTemplate(...)`: Yeni bir xəta mesajı qurmağa başlayır.
        - `.addPropertyNode("confirmPassword")`: Xəta mesajının `confirmPassword` sahəsinə aid olduğunu bildirir. Bu, klientə qaytarılan JSON cavabında xətanın məhz bu sahə ilə əlaqəli göstərilməsinə səbəb olur.
        - `.addConstraintViolation();`: Qurulmuş xəta mesajını aktivləşdirir.
    - **`return isValid;`**: Validasiyanın nəticəsini (`true` və ya `false`) qaytarır.

### 11.2. `OneOfFieldsNotBlank` Annotasiyası və Validatoru

Bu cütlük, verilən bir neçə sahədən ən azı birinin boş olmamasını təmin edir (məsələn, `email` və ya `phone` sahələrindən biri mütləq doldurulmalıdır).

#### `OneOfFieldsNotBlank.java` (Annotasiya)
**Fayl Yolu:** `src/main/java/com/example/authservice/validation/OneOfFieldsNotBlank.java`

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneOfFieldsNotBlankValidator.class)
@Documented
public @interface OneOfFieldsNotBlank {
    String message() default "At least one of the fields must be filled in.";
    String[] fieldNames(); // Xüsusi parametr
    // ... standart elementlər
}
```

- **`String[] fieldNames();`**: Bu, annotasiyanın özünə məxsus bir parametrdir. Bu annotasiyanı istifadə edərkən hansı sahələrin yoxlanılacağını təyin etməyə imkan verir. Məsələn: `@OneOfFieldsNotBlank(fieldNames = {"email", "phone"})`.

#### `OneOfFieldsNotBlankValidator.java` (Validator)
**Fayl Yolu:** `src/main/java/com/example/authservice/validation/OneOfFieldsNotBlankValidator.java`

```java
public class OneOfFieldsNotBlankValidator implements ConstraintValidator<OneOfFieldsNotBlank, Object> {

    private String[] fieldNames;

    @Override
    public void initialize(OneOfFieldsNotBlank constraintAnnotation) {
        this.fieldNames = constraintAnnotation.fieldNames();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        boolean atLeastOneNotBlank = false;

        for (String fieldName : fieldNames) {
            Object fieldValue = beanWrapper.getPropertyValue(fieldName);
            if (fieldValue != null && !fieldValue.toString().trim().isEmpty()) {
                atLeastOneNotBlank = true;
                break;
            }
        }
        // ...
        return atLeastOneNotBlank;
    }
}
```

- **`initialize(OneOfFieldsNotBlank constraintAnnotation)`**: `isValid` metodu çağırılmazdan əvvəl bir dəfə icra olunur. Annotasiyadan `fieldNames` massivini oxuyur və validatorun daxili vəziyyətində (`this.fieldNames`) saxlayır.
- **`isValid(Object value, ...)`**: Əsas validasiya məntiqi.
    - **`BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);`**: Spring-in təqdim etdiyi bu sinif, obyektin sahələrinin adlarına görə onların dəyərlərini proqrammatik şəkildə (reflection vasitəsilə) oxumağa imkan verir.
    - **`for (String fieldName : fieldNames)` dövrü**: `initialize` metodunda saxlanmış hər bir sahə adı üçün dövr başladılır.
    - **`Object fieldValue = beanWrapper.getPropertyValue(fieldName);`**: `BeanWrapper` vasitəsilə həmin sahənin dəyəri oxunur.
    - **`if (fieldValue != null && !fieldValue.toString().trim().isEmpty())`**: Sahənin dəyərinin `null` olmadığı və boş `String` olmadığı yoxlanılır.
    - **`atLeastOneNotBlank = true; break;`**: Əgər ən azı bir sahə doldurulubsa, `atLeastOneNotBlank` bayrağı `true` edilir və dövrdən çıxılır, çünki şərt artıq ödənib.
    - **`return atLeastOneNotBlank;`**: Sonda, ən azı bir sahənin dolu olub-olmadığını göstərən `boolean` dəyər qaytarılır.

---

**`authService` üçün Kod İzahının Sonu.**