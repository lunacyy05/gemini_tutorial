package controller;

import dto.BookmarkDto;
import entity.Bookmark;
import entity.Property;
import entity.User;
import repository.BookmarkRepository;
import repository.PropertyRepository;
import repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @PostMapping("/users/{userId}")
    public ResponseEntity<BookmarkDto.Response> createBookmark(@PathVariable Long userId,
                                                              @RequestBody BookmarkDto.CreateRequest request) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Property> property = propertyRepository.findById(request.getPropertyId());
        if (property.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (bookmarkRepository.existsByUserIdAndPropertyId(userId, request.getPropertyId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user.get())
                .property(property.get())
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BookmarkDto.Response.fromEntity(savedBookmark));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<BookmarkDto.Response>> getUserBookmarks(@PathVariable Long userId,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<BookmarkDto.Response> responses = bookmarks.map(BookmarkDto.Response::fromEntity);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkDto.Response> getBookmarkById(@PathVariable Long id) {
        Optional<Bookmark> bookmark = bookmarkRepository.findById(id);
        return bookmark.map(b -> ResponseEntity.ok(BookmarkDto.Response.fromEntity(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        if (!bookmarkRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookmarkRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/properties/{propertyId}")
    public ResponseEntity<Void> deleteBookmarkByUserAndProperty(@PathVariable Long userId,
                                                               @PathVariable Long propertyId) {
        if (!bookmarkRepository.existsByUserIdAndPropertyId(userId, propertyId)) {
            return ResponseEntity.notFound().build();
        }
        bookmarkRepository.deleteByUserIdAndPropertyId(userId, propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/properties/{propertyId}/count")
    public ResponseEntity<Long> getBookmarkCount(@PathVariable Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            return ResponseEntity.notFound().build();
        }
        Long count = bookmarkRepository.countByPropertyId(propertyId);
        return ResponseEntity.ok(count);
    }
}