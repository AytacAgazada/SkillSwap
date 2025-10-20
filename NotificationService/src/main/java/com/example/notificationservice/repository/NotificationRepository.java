package com.example.notificationservice.repository;

import com.example.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Verilmiş istifadəçi üçün bütün bildirişləri tarixə görə azalan formada çəkir.
     * Bu metod, NotificationController tərəfindən istifadəçinin bildiriş tarixçəsini göstərmək üçün çağırılır.
     * @param userId Axtarış edilən istifadəçinin ID-si
     * @return Bildirişlərin Siyahısı
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // findById və save kimi əməliyyatlar MongoRepository-dən miras alınır.
}