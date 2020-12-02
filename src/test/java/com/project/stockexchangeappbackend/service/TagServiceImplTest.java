package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @InjectMocks
    TagServiceImpl tagService;

    @Mock
    TagRepository tagRepository;

    @BeforeEach
    void setup() {
        setTagsList();
    }

    @Test
    void shouldReturnExistingTag() {
        Tag tag = getTagsList().get(0);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertTag(tag, tagService.getTag(tagName));
    }

    @Test
    void shouldReturnNonExistingTag() {
        Tag tag = getTagsList().get(1);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        when(tagRepository.save(Mockito.any(Tag.class))).thenReturn(tag);
        assertTag(tag, tagService.getTag(tagName));
    }

    @Test
    void shouldFindExistingTag() {
        Tag tag = getTagsList().get(0);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertTag(tag, tagService.findTag(tagName));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenLookingForNonExistingTag() {
        String tagName = "TEST";
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> tagService.findTag(tagName));
    }

    @Test
    void shouldPageAndFilterTags() {
        List<Tag> tags = getTagsList();
        Pageable pageable = PageRequest.of(0,20);
        Specification<Tag> specification = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("name"), "t");
        when(tagRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(tags, pageable, tags.size()));
        Page<Tag> output = tagService.getTags(pageable, specification);
        assertEquals(tags.size(), output.getNumberOfElements());
        for (int i=0; i<tags.size(); i++) {
            assertTag(tags.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldRemoveTag() {
        Tag tag = getTagsList().get(1);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertAll(() -> tagService.removeTag(tagName));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenRemovingDefaultTag() {
        Tag tag = getTagsList().get(0);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertThrows(InvalidInputDataException.class, () -> tagService.removeTag(tagName));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRemovingTag() {
        Tag tag = getTagsList().get(1);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> tagService.removeTag(tagName));
    }

    private static List<Tag> tags;

    public static void assertTag(Tag expected, Tag output) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getName(), output.getName()));
    }

    public static List<Tag> getTagsList() {
        if (tags == null) {
            setTagsList();
        }
        return tags;
    }

    private static void setTagsList() {
        tags = Arrays.asList(
                new Tag(1L, "DEFAULT"),
                new Tag(2L, "TEST")
        );
    }

}
