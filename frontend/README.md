# SkillSwap Frontend

SkillSwap layihəsinin frontend hissəsi. Modern, minimalist dizayn ilə hazırlanmış React + TypeScript + Vite tətbiqi.

## Xüsusiyyətlər

- 🎨 **Modern Dizayn**: Narıncı tonlarda enerjili və dinamik görünüş
- 📱 **Responsive**: Bütün cihazlarda optimal görünüş
- ⚡ **Sürətli**: Vite ilə sürətli development və build
- 🎯 **Minimalist Formlar**: Sadə və istifadəçi dostu interfeys
- 🔄 **Hover Effektləri**: Yumru künclü butonlar və interaktiv elementlər

## Texnologiyalar

- React 18
- TypeScript
- Vite
- React Router DOM
- CSS3 (Custom Properties)

## Quraşdırma

```bash
npm install
```

## İşə salma

Development serveri işə salmaq üçün:

```bash
npm run dev
```

Sayt `http://localhost:5173` ünvanında açılacaq.

## Build

Production build üçün:

```bash
npm run build
```

## Dizayn Sistemi

### Rəng Palitrası

- **Əsas Rəng**: Narıncı tonlar (#FF6B35, #FF8C42)
- **Fon**: Yumşaq açıq rənglər (#FFF8F5, #FFFFFF)
- **Mətn**: Qara və boz tonlar

### Fontlar

- **Başlıqlar**: Poppins
- **Mətn**: Inter

### Komponentlər

- Navbar: Sticky navigation bar
- Footer: Saytın alt hissəsi
- Forms: Minimalist form elementləri
- Buttons: Yumru künclü, hover effektli butonlar

## Struktur

```
frontend/
├── src/
│   ├── components/        # Yenidən istifadə olunan komponentlər
│   │   ├── Navbar.tsx
│   │   ├── Footer.tsx
│   │   └── ProtectedRoute.tsx
│   ├── contexts/          # React Contexts
│   │   └── AuthContext.tsx
│   ├── pages/             # Səhifələr
│   │   ├── Home.tsx
│   │   ├── Login.tsx
│   │   ├── Register.tsx
│   │   └── Dashboard.tsx
│   ├── services/          # API Servisləri
│   │   ├── api.ts
│   │   ├── authService.ts
│   │   ├── skillService.ts
│   │   ├── userBioService.ts
│   │   ├── swapService.ts
│   │   ├── communityService.ts
│   │   ├── gamificationService.ts
│   │   ├── notificationService.ts
│   │   └── chatService.ts
│   ├── App.tsx            # Əsas komponent
│   ├── main.tsx           # Entry point
│   └── index.css          # Global stillər
└── package.json
```

## API Əlaqəsi

Frontend tam olaraq backend API ilə inteqrasiya olunub. Bütün servislər üçün API çağırışları hazırlanmışdır:

### Backend Servisləri

- **Auth Service**: `/api/auth/*` - Giriş, qeydiyyat, token idarəsi
- **Skill Service**: `/api/skills/*` - Bacarıq idarəetməsi
- **User Bio Service**: `/api/user-bios/*` - İstifadəçi profilləri
- **Swap Service**: `/api/skil-swaps/*` - Bacarıq mübadiləsi
- **Community Service**: `/community/*` - İctimaiyyət və qruplar
- **Gamification Service**: `/gamification/*` - XP və badge sistemi
- **Notification Service**: `/api/notifications/*` - Bildirişlər
- **Chat Service**: `/api/chat/*` - Mesajlaşma

### Konfiqurasiya

API base URL-i `.env` faylında təyin edilir:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Default olaraq `http://localhost:8080` (API Gateway) istifadə olunur.

### Authentication

Frontend JWT token-based authentication istifadə edir:
- Access token localStorage-də saxlanılır
- Hər API çağırışında token avtomatik əlavə olunur
- Token expire olduqda refresh token ilə yenilənir

### Xüsusiyyətlər

✅ **Tam Backend İnteqrasiyası**
- Login/Register backend ilə əlaqəlidir
- Bütün API endpointləri hazırdır
- Error handling və validation

✅ **Protected Routes**
- Dashboard və digər məxfi səhifələr qorunur
- Auth guard avtomatik yönləndirmə edir

✅ **State Management**
- Auth Context ilə global state idarəsi
- JWT token avtomatik idarəsi

