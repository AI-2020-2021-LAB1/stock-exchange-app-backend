package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.TagRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    @LogicBusinessMeasureTime
    public Tag getTag(String name) {
        return tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> tagRepository.save(Tag.builder()
                                .name(name.toUpperCase())
                                .build()));
    }

    @Override
    public Tag findTag(String name) {
        return tagRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
    }

    @Override
    public Page<Tag> getTags(Pageable pageable, Specification<Tag> specification) {
        return tagRepository.findAll(specification, pageable);
    }

    @Override
    public void removeTag(String name) {
        Tag tag = tagRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        if (tag.getName().equals("DEFAULT")) {
            throw new InvalidInputDataException("Tag DEFAULT cannot be deleted", null);
        }
        tagRepository.delete(tag);
    }

}
