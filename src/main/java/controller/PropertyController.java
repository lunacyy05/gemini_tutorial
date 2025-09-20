package controller;

import dto.PropertyDto;
import dto.PropertyImageDto;
import entity.Property;
import entity.PropertyImage;
import repository.PropertyImageRepository;
import repository.PropertyRepository;
import service.KakaoMapService;
import service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final KakaoMapService kakaoMapService;
    private final GeocodingService geocodingService;

    @PostMapping
    public ResponseEntity<PropertyDto.Response> createProperty(@RequestBody PropertyDto.CreateRequest request) {
        java.math.BigDecimal latitude = request.getLatitude();
        java.math.BigDecimal longitude = request.getLongitude();

        if ((latitude == null || longitude == null) && request.getAddress() != null) {
            GeocodingService.CoordinateResult coordinateResult = geocodingService.addressToCoordinate(request.getAddress());
            if (coordinateResult.success) {
                latitude = java.math.BigDecimal.valueOf(coordinateResult.latitude);
                longitude = java.math.BigDecimal.valueOf(coordinateResult.longitude);
            }
        }

        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .district(request.getDistrict())
                .neighborhood(request.getNeighborhood())
                .propertyType(request.getPropertyType())
                .roomType(request.getRoomType())
                .latitude(latitude)
                .longitude(longitude)
                .rentalType(request.getRentalType())
                .deposit(request.getDeposit())
                .monthlyRent(request.getMonthlyRent())
                .maintenanceFee(request.getMaintenanceFee())
                .area(request.getArea())
                .floor(request.getFloor())
                .totalFloor(request.getTotalFloor())
                .availabilityStatus(Property.AvailabilityStatus.AVAILABLE)
                .availableDate(request.getAvailableDate())
                .isParkingAvailable(request.getIsParkingAvailable())
                .isElevatorAvailable(request.getIsElevatorAvailable())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .build();

        Property savedProperty = propertyRepository.save(property);
        PropertyDto.Response response = PropertyDto.Response.fromEntity(savedProperty);

        List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByImageOrder(savedProperty.getId());
        response.setImages(images.stream()
                .map(PropertyImageDto.Response::fromEntity)
                .collect(Collectors.toList()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDto.Response> getPropertyById(@PathVariable Long id) {
        Optional<Property> property = propertyRepository.findById(id);
        if (property.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PropertyDto.Response response = PropertyDto.Response.fromEntity(property.get());
        List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByImageOrder(id);
        response.setImages(images.stream()
                .map(PropertyImageDto.Response::fromEntity)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PropertyDto.Response>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Property> properties = propertyRepository.findByAvailabilityStatus(
            Property.AvailabilityStatus.AVAILABLE, pageable);

        Page<PropertyDto.Response> responses = properties.map(PropertyDto.Response::fromEntity);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PropertyDto.Response>> searchProperties(@RequestBody PropertyDto.SearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        Sort sort = sortDirection.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Property> properties = propertyRepository.findPropertiesWithFilters(
                request.getLocation(),
                request.getLocation(),
                request.getMinBudget(),
                request.getMaxBudget(),
                request.getMinBudget(),
                request.getMaxBudget(),
                request.getRentalType(),
                request.getPropertyType(),
                request.getRoomType(),
                pageable);

        Page<PropertyDto.Response> responses = properties.map(PropertyDto.Response::fromEntity);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<PropertyDto.Response>> getNearbyProperties(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5000.0") Double radius) {

        List<Property> properties = propertyRepository.findNearbyProperties(
                java.math.BigDecimal.valueOf(latitude),
                java.math.BigDecimal.valueOf(longitude),
                radius);

        List<PropertyDto.Response> responses = properties.stream()
                .map(PropertyDto.Response::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyDto.Response> updateProperty(@PathVariable Long id,
                                                              @RequestBody PropertyDto.UpdateRequest request) {
        Optional<Property> optionalProperty = propertyRepository.findById(id);
        if (optionalProperty.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Property property = optionalProperty.get();
        if (request.getTitle() != null) property.setTitle(request.getTitle());
        if (request.getDescription() != null) property.setDescription(request.getDescription());
        if (request.getRentalType() != null) property.setRentalType(request.getRentalType());
        if (request.getDeposit() != null) property.setDeposit(request.getDeposit());
        if (request.getMonthlyRent() != null) property.setMonthlyRent(request.getMonthlyRent());
        if (request.getMaintenanceFee() != null) property.setMaintenanceFee(request.getMaintenanceFee());
        if (request.getAvailableDate() != null) property.setAvailableDate(request.getAvailableDate());
        if (request.getAvailabilityStatus() != null) property.setAvailabilityStatus(request.getAvailabilityStatus());
        if (request.getContactName() != null) property.setContactName(request.getContactName());
        if (request.getContactPhone() != null) property.setContactPhone(request.getContactPhone());

        Property updatedProperty = propertyRepository.save(property);
        PropertyDto.Response response = PropertyDto.Response.fromEntity(updatedProperty);

        List<PropertyImage> images = propertyImageRepository.findByPropertyIdOrderByImageOrder(id);
        response.setImages(images.stream()
                .map(PropertyImageDto.Response::fromEntity)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        if (!propertyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        propertyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/address")
    public ResponseEntity<List<PropertyDto.Response>> searchByAddress(
            @RequestParam String address,
            @RequestParam(defaultValue = "3000.0") Double radius) {

        GeocodingService.CoordinateResult result = geocodingService.addressToCoordinate(address);
        if (!result.success) {
            return ResponseEntity.badRequest().build();
        }

        List<Property> properties = propertyRepository.findNearbyProperties(
                java.math.BigDecimal.valueOf(result.latitude),
                java.math.BigDecimal.valueOf(result.longitude),
                radius);

        List<PropertyDto.Response> responses = properties.stream()
                .map(PropertyDto.Response::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search/poi")
    public ResponseEntity<Map<String, Object>> searchNearbyPOI(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {

        try {
            Map<String, Object> poiData = kakaoMapService.searchByCategory(
                    "MT1", longitude, latitude, radius, page, size);
            return ResponseEntity.ok(poiData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<Map<String, Object>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {

        try {
            Map<String, Object> results = kakaoMapService.searchByKeyword(keyword, page, size);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/geocoding/address-to-coordinate")
    public ResponseEntity<GeocodingService.CoordinateResult> addressToCoordinate(
            @RequestBody Map<String, String> request) {

        String address = request.get("address");
        if (address == null || address.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        GeocodingService.CoordinateResult result = geocodingService.addressToCoordinate(address);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/geocoding/coordinate-to-address")
    public ResponseEntity<GeocodingService.AddressResult> coordinateToAddress(
            @RequestBody Map<String, Double> request) {

        Double latitude = request.get("latitude");
        Double longitude = request.get("longitude");

        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().build();
        }

        GeocodingService.AddressResult result = geocodingService.coordinateToAddress(longitude, latitude);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/subway-nearby")
    public ResponseEntity<List<PropertyDto.Response>> searchNearSubway(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1000.0") Double radius) {

        try {
            Map<String, Object> subwayData = kakaoMapService.searchByCategory(
                    "SW8", longitude, latitude, radius.intValue(), 1, 15);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) subwayData.get("documents");

            if (documents != null && !documents.isEmpty()) {
                List<Property> properties = propertyRepository.findNearbyProperties(
                        java.math.BigDecimal.valueOf(latitude),
                        java.math.BigDecimal.valueOf(longitude),
                        radius);

                List<PropertyDto.Response> responses = properties.stream()
                        .map(PropertyDto.Response::fromEntity)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(responses);
            }

            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}