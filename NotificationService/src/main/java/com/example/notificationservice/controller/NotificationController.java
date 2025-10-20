package com.example.notificationservice.controller;

import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * İstifadəçinin son bildiriş tarixçəsini çəkir.
     * @param userId Autentifikasiya edilmiş istifadəçinin ID-si (Adətən JWT token'dən alınır)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getForUser(@PathVariable Long userId) {
        // Həqiqi sistemdə, userId path-dən yox, Spring Security Context-dən gəlməlidir
        return ResponseEntity.ok(repository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /**
     * Bir bildirişi oxunmuş kimi qeyd edir.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable String id) {
        Optional<Notification> opt = repository.findById(id);
        if (opt.isPresent()) {
            Notification n = opt.get();
            n.setRead(true);
            repository.save(n);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
