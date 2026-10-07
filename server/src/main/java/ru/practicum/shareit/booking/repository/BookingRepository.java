package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT b
        FROM Booking b
        WHERE (:owner = false AND b.booker.id = :userId)
           OR (:owner = true AND b.item.owner.id = :userId)
        """)
    List<Booking> findAllForUser(Long userId, boolean owner, Pageable pageable);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE (
            (:owner = false AND b.booker.id = :userId)
            OR (:owner = true AND b.item.owner.id = :userId)
        )
        AND b.start <= :now
        AND b.end >= :now
        """)
    List<Booking> findCurrentForUser(Long userId, boolean owner, LocalDateTime now, Pageable pageable);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE (
            (:owner = false AND b.booker.id = :userId)
            OR (:owner = true AND b.item.owner.id = :userId)
        )
        AND b.end < :now
        """)
    List<Booking> findPastForUser(Long userId, boolean owner, LocalDateTime now, Pageable pageable);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE (
            (:owner = false AND b.booker.id = :userId)
            OR (:owner = true AND b.item.owner.id = :userId)
        )
        AND b.start > :now
        """)
    List<Booking> findFutureForUser(Long userId, boolean owner, LocalDateTime now, Pageable pageable);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE (
            (:owner = false AND b.booker.id = :userId)
            OR (:owner = true AND b.item.owner.id = :userId)
        )
        AND b.status = :status
        """)
    List<Booking> findByStatusForUser(Long userId, boolean owner, BookingStatus status, Pageable pageable);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.item.owner.id = :ownerId
        AND b.status = :status
        """)
    List<Booking> findByOwnerAndStatus(Long ownerId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(Long itemId, BookingStatus status, LocalDateTime time);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, BookingStatus status, LocalDateTime time);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime end);

}