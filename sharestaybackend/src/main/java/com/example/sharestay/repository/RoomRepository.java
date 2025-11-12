package com.example.sharestay.repository;

import com.example.sharestay.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    /*
        검색 필터가 여러 개고 nullable 필드가 있으니 JPQL 동적 쿼리로 짜는 게 좋음.
        Spring Data JPA의 @Query + COALESCE() 트릭을 쓰면 깔끔하게 됨
     */
    // 동적 필터 검색 (지역은 필수, 나머지는 nullable)
    @Query("""
        SELECT r FROM Room r
        WHERE r.address LIKE CONCAT('%', :region, '%')
          AND (:type IS NULL OR r.type = :type)
          AND (:minPrice IS NULL OR r.rentPrice >= :minPrice)
          AND (:maxPrice IS NULL OR r.rentPrice <= :maxPrice)
          AND (:option IS NULL OR r.description LIKE CONCAT('%', :option, '%'))
        """)

    List<Room> searchRooms(
            @Param("region") String region,
            @Param("type") String type,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("option") String option    // 편의시설
    );

/*
    지역(region)은 필수니까 LIKE 검색으로 강제
    나머지는 전부 nullable 처리 (:param IS NULL OR ...)
    편의시설은 description 안에 단어로 포함된다고 가정 (LIKE %amenity%)
 */



}
