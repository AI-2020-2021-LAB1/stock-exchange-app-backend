package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateTagDTO;
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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public Optional<Tag> getTag(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public Tag findTag(String name) {
        return tagRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
    }

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public Page<Tag> getTags(Pageable pageable, Specification<Tag> specification) {
        return tagRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional
    @LogicBusinessMeasureTime
    public void removeTag(String name) {
        Tag tag = tagRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        if (tag.getName().equals("DEFAULT")) {
            throw new InvalidInputDataException("Tag DEFAULT cannot be deleted", null);
        }
        tagRepository.delete(tag);
    }

    @Override
    @Transactional
    @LogicBusinessMeasureTime
    public void createTag(CreateTagDTO createTagDTO) {
        Optional<Tag> tag = tagRepository.findByNameIgnoreCase(createTagDTO.getName().trim());
        if (tag.isPresent()) {
            throw new EntityExistsException("Tag already exist");
        }
        tagRepository.save(Tag.builder()
                .name(createTagDTO.getName().trim().toUpperCase())
                .build());
    }

}
