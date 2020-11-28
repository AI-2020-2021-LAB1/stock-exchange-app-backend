package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.TagRepository;
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

    @Test
    void shouldReturnExistingTag() {
        Tag tag = new Tag(1L, "DEFAULT");
        when(tagRepository.findByNameIgnoreCase(tag.getName())).thenReturn(Optional.of(tag));
        assertTag(tag, tagService.getTag(tag.getName()));
    }

    @Test
    void shouldReturnNonExistingTag() {
        String tagName = "TEST";
        Tag tag = new Tag(2L, tagName);
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        when(tagRepository.save(Mockito.any(Tag.class))).thenReturn(tag);
        assertTag(tag, tagService.getTag(tagName));
    }

    @Test
    void shouldFindExistingTag() {
        Tag tag = new Tag(1L, "DEFAULT");
        when(tagRepository.findByNameIgnoreCase(tag.getName())).thenReturn(Optional.of(tag));
        assertTag(tag, tagService.findTag(tag.getName()));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenLookingForNonExistingTag() {
        String tagName = "TEST";
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> tagService.findTag(tagName));
    }

    @Test
    void shouldPageAndFilterTags() {
        List<Tag> tags = List.of(new Tag(1L, "default"),
                new Tag(2L, "de"));
        Pageable pageable = PageRequest.of(0,20);
        Specification<Tag> specification = (Specification<Tag>) (root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("name"), "de");
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
        Tag tag = new Tag(2L, "test");
        when(tagRepository.findByNameIgnoreCase(tag.getName())).thenReturn(Optional.of(tag));
        assertAll(() -> tagService.removeTag(tag.getName()));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenRemovingDedaultTag() {
        Tag tag = new Tag(1L, "DEFAULT");
        when(tagRepository.findByNameIgnoreCase(tag.getName())).thenReturn(Optional.of(tag));
        assertThrows(InvalidInputDataException.class, () -> tagService.removeTag(tag.getName()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRemovingTag() {
        String tag = "default";
        when(tagRepository.findByNameIgnoreCase(tag)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> tagService.removeTag(tag));
    }

    public static void assertTag(Tag expected, Tag output) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getName(), output.getName()));
    }

}
