package com.library.repository;

import com.library.entity.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CarouselRepository extends JpaRepository<Carousel, Long> {
    
    // 获取所有激活的轮播图，按排序索引升序排列
    @Query("SELECT c FROM Carousel c WHERE c.isActive = true ORDER BY c.orderIndex ASC")
    List<Carousel> findActiveCarousels();
    
    // 获取所有轮播图，按排序索引升序排列
    @Query("SELECT c FROM Carousel c ORDER BY c.orderIndex ASC")
    List<Carousel> findAllOrdered();
}
