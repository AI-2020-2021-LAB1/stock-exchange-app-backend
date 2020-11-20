package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @InjectMocks
    TagServiceImpl tagService;

    @Mock
    TagRepository tagRepository;

    @Test
    void shouldReturnExistingTag() {
        Tag tag = new Tag(1L, "DEFAULT");
        when(tagRepository.findByNameIgnoreCase(tag.getName())).thenReturn(Optional.of(tag));
        Tag output = tagService.getTag(tag.getName());
        assertTag(tag, output);
    }

    @Test
    void shouldReturnNonExistingTag() {
        String tagName = "TEST";
        Tag tag = new Tag(2L, tagName);
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        when(tagRepository.save(Mockito.any(Tag.class))).thenReturn(tag);
        Tag output = tagService.getTag(tagName);
        assertTag(tag, output);
    }

    public void assertTag(Tag expected, Tag output) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getName(), output.getName()));
    }

}