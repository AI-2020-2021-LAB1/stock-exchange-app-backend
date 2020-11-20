package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.repository.TagRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    @LogicBusinessMeasureTime
    public Tag getTag(String name) {
        return tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> tagRepository.save(
                        Tag.builder()
                                .name(name.toUpperCase())
                                .build()
                ));
    }

}
