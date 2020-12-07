package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateTagDTO;
import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import javax.persistence.EntityExistsException;
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
    @DisplayName("Getting existing tag")
    void shouldReturnExistingTag() {
        Optional<Tag> tag = Optional.of(getTagsList().get(0));
        String tagName = tag.get().getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(tag);
        assertTag(tag.get(), tagService.getTag(tagName).get());
    }

    @Test
    @DisplayName("Getting non-existing tag")
    void shouldReturnNonExistingTag() {
        String tagName = "tag";
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        assertTrue(tagService.getTag(tagName).isEmpty());
    }

    @Test
    @DisplayName("Searching existing tag")
    void shouldFindExistingTag() {
        Tag tag = getTagsList().get(0);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertTag(tag, tagService.findTag(tagName));
    }

    @Test
    @DisplayName("Getting existing tag when tag not found")
    void shouldThrowNotFoundExceptionWhenLookingForNonExistingTag() {
        String tagName = "TEST";
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> tagService.findTag(tagName));
    }

    @Test
    @DisplayName("Paging and filtering tags")
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
    @DisplayName("Deleting tag")
    void shouldRemoveTag() {
        Tag tag = getTagsList().get(1);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertAll(() -> tagService.removeTag(tagName));
    }

    @Test
    @DisplayName("Deleting tag when desired tag is default")
    void shouldThrowInvalidInputDataExceptionWhenRemovingDefaultTag() {
        Tag tag = getTagsList().get(0);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.of(tag));
        assertThrows(InvalidInputDataException.class, () -> tagService.removeTag(tagName));
    }

    @Test
    @DisplayName("Deleting tag when tag not found")
    void shouldThrowEntityNotFoundExceptionWhenRemovingTag() {
        Tag tag = getTagsList().get(1);
        String tagName = tag.getName();
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> tagService.removeTag(tagName));
    }

    @Test
    @DisplayName("Creating new tag")
    void shouldCreateNewTag() {
        CreateTagDTO createTagDTO = new CreateTagDTO("newTag");
        when(tagRepository.findByNameIgnoreCase(createTagDTO.getName())).thenReturn(Optional.empty());
        assertAll(() -> tagService.createTag(createTagDTO));
    }

    @Test
    @DisplayName("Creating new tag when tag already exist")
    void shouldThrowEntityExistsExceptionWhenCreatingNewTag() {
        Tag tag = getTagsList().get(0);
        CreateTagDTO createTagDTO = new CreateTagDTO(tag.getName());
        when(tagRepository.findByNameIgnoreCase(createTagDTO.getName())).thenReturn(Optional.of(tag));
        assertThrows(EntityExistsException.class, () -> tagService.createTag(createTagDTO));
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
