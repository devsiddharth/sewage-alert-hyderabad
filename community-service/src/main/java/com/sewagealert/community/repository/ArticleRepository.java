package com.sewagealert.community.repository;

import com.sewagealert.community.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // findByCategory: Filters articles by category (e.g., WATER_CONSERVATION, SEWAGE_TREATMENT)
    List<Article> findByCategory(String category);

    // findByTitleContaining: Searches articles by keyword in the title
    List<Article> findByTitleContainingIgnoreCase(String keyword);
}
