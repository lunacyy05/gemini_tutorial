package controller;

import dto.PropertyImageDto;
import entity.Property;
import entity.PropertyImage;
import repository.PropertyImageRepository;
import repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/property-images")
@RequiredArgsConstructor
public class PropertyImageController {

    private final PropertyImageRepository propertyImageRepository;
    private final PropertyRepository propertyRepository;

    @PostMapping
    public ResponseEntity<PropertyImageDto.Response> createPropertyImage(@RequestBody PropertyImageDto.CreateRequest request) {
        Optional<Property> property = propertyRepository.findById(request.getPropertyId());
        if (property.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PropertyImage propertyImage = PropertyImage.builder()
                .property(property.get())
                .imageUrl(request.getImageUrl())
                .imageOrder(request.getImageOrder())
                .altText(request.getAltText())
                .build();

        PropertyImage savedImage = propertyImageRepository.save(propertyImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PropertyImageDto.Response.fromEntity(savedImage));
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<List<PropertyImageDto.Response>> getPropertyImages(@PathVariable Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            return ResponseEntity.notFound().build();
        }

        List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByImageOrder(propertyId);
        List<PropertyImageDto.Response> responses = images.stream()
                .map(PropertyImageDto.Response::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/properties/{propertyId}/main")
    public ResponseEntity<PropertyImageDto.Response> getMainImage(@PathVariable Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            return ResponseEntity.notFound().build();
        }

        PropertyImage mainImage = propertyImageRepository.findMainImageByPropertyId(propertyId);
        if (mainImage == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(PropertyImageDto.Response.fromEntity(mainImage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyImageDto.Response> getPropertyImageById(@PathVariable Long id) {
        Optional<PropertyImage> image = propertyImageRepository.findById(id);
        return image.map(img -> ResponseEntity.ok(PropertyImageDto.Response.fromEntity(img)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePropertyImage(@PathVariable Long id) {
        if (!propertyImageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        propertyImageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/properties/{propertyId}")
    public ResponseEntity<Void> deleteAllPropertyImages(@PathVariable Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            return ResponseEntity.notFound().build();
        }
        propertyImageRepository.deleteByPropertyId(propertyId);
        return ResponseEntity.noContent().build();
    }
}