package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TagService {

    Tag getTag(String name);
    Tag findTag(String name);
    Page<Tag> getTags(Pageable pageable, Specification<Tag> specification);
    void removeTag(String name);

}
