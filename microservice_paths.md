# Microservice Paths

## authService

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`
- `POST /api/auth/logout`
- `POST /api/auth/otp/send`
- `POST /api/auth/otp/verify`
- `POST /api/auth/password/reset`
- `GET /api/auth/user/profile`
- `GET /api/auth/admin/dashboard`
- `GET /api/auth/provider/info`
- `GET /api/auth/admin-or-provider/data`
- `GET /api/auth/user-or-admin/view`
- `GET /api/auth/{authUserId}/exists`
- `GET /api/auth/{authUserId}/role`
- `POST /api/auth/test-email`

## ChatService

- `GET /api/chat/history/{swapId}`

## CommunityService

- `POST /community/groups/create`
- `GET /community/groups`
- `POST /community/groups/{groupId}/join`
- `POST /community/problems/create`
- `GET /community/problems/{groupId}`
- `POST /community/problems/{problemId}/solve`

## GamificationService

- `GET /gamification/{userId}`
- `POST /gamification/add-xp`
- `GET /gamification/badges/{userId}`

## NotificationService

- `GET /api/notifications/user/{userId}`
- `POST /api/notifications/{id}/read`

## SkillSwapService

- `POST /api/skil-swaps/offers`
- `GET /api/skil-swaps/offers/me`
- `GET /api/skil-exports/offers/search`
- `POST /api/skil-swaps/match/{offerId}`
- `POST /api/skil-swaps/{swapId}/accept`
- `POST /api/skil-swaps/complete/{swapId}`

## SkillUserService

### SkillController

- `POST /api/skills`
- `GET /api/skills/{id}`
- `GET /api/skills`
- `PUT /api/skills`
- `DELETE /api/skills/{id}`

### UserBioController

- `POST /api/user-bios`
- `GET /api/user-bios/{id}`
- `GET /api/user-bios/auth-user/{authUserId}`
- `GET /api/user-bios/me`
- `GET /api/user-bios`
- `PUT /api/user-bios`
- `DELETE /api/user-bios/{id}`
- `DELETE /api/user-bios/me`
