// 지도 관련 기능을 담당하는 모듈
class MapManager {
    constructor() {
        this.map = null;
        this.markers = [];
        this.infowindows = [];
        this.clusterer = null;
        this.currentPosition = null;

        this.init();
    }

    // 지도 초기화
    init() {
        try {
            // 지도 컨테이너
            const container = document.getElementById('map');

            // 기본 옵션 (서울 중심)
            const options = {
                center: new kakao.maps.LatLng(37.5665, 126.9780),
                level: 3
            };

            // 지도 생성
            this.map = new kakao.maps.Map(container, options);

            // 지도 컨트롤 추가
            this.addMapControls();

            // 현재 위치 가져오기
            this.getCurrentPosition();

            console.log('카카오 지도 초기화 완료');
        } catch (error) {
            console.error('지도 초기화 실패:', error);
            this.showError('지도를 불러올 수 없습니다.');
        }
    }

    // 지도 컨트롤 추가
    addMapControls() {
        // 확대/축소 컨트롤
        const zoomControl = new kakao.maps.ZoomControl();
        this.map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

        // 지도 타입 컨트롤
        const mapTypeControl = new kakao.maps.MapTypeControl();
        this.map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);
    }

    // 현재 위치 가져오기
    getCurrentPosition() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const lat = position.coords.latitude;
                    const lng = position.coords.longitude;

                    this.currentPosition = new kakao.maps.LatLng(lat, lng);
                    this.map.setCenter(this.currentPosition);

                    // 현재 위치 마커 추가
                    this.addCurrentLocationMarker();
                },
                (error) => {
                    console.warn('위치 정보를 가져올 수 없습니다:', error);
                    // 기본 위치 유지
                }
            );
        }
    }

    // 현재 위치 마커 추가
    addCurrentLocationMarker() {
        if (!this.currentPosition) return;

        const marker = new kakao.maps.Marker({
            position: this.currentPosition,
            image: this.createMarkerImage('/images/current-location.png', 24, 24)
        });

        marker.setMap(this.map);

        const infowindow = new kakao.maps.InfoWindow({
            content: '<div style="padding:5px;">현재 위치</div>'
        });

        kakao.maps.event.addListener(marker, 'click', () => {
            infowindow.open(this.map, marker);
        });
    }

    // 매물 마커들 추가
    addPropertyMarkers(properties) {
        // 기존 마커들 제거
        this.clearMarkers();

        if (!properties || properties.length === 0) {
            return;
        }

        properties.forEach((property, index) => {
            this.addPropertyMarker(property, index);
        });

        // 마커들이 모두 보이도록 지도 범위 조정
        this.fitMapToBounds();
    }

    // 개별 매물 마커 추가
    addPropertyMarker(property, index) {
        if (!property.latitude || !property.longitude) {
            console.warn('매물의 좌표 정보가 없습니다:', property);
            return;
        }

        const position = new kakao.maps.LatLng(property.latitude, property.longitude);

        // 마커 이미지 설정
        const markerImage = this.createPropertyMarkerImage(property.type);

        const marker = new kakao.maps.Marker({
            position: position,
            image: markerImage,
            clickable: true
        });

        marker.setMap(this.map);

        // 인포윈도우 생성
        const infowindow = new kakao.maps.InfoWindow({
            content: this.createInfoWindowContent(property),
            removable: true
        });

        // 마커 클릭 이벤트
        kakao.maps.event.addListener(marker, 'click', () => {
            // 다른 인포윈도우들 닫기
            this.closeAllInfoWindows();

            // 현재 인포윈도우 열기
            infowindow.open(this.map, marker);

            // 매물 리스트에서 해당 항목 하이라이트
            this.highlightPropertyItem(property.id);
        });

        // 배열에 저장
        this.markers.push(marker);
        this.infowindows.push(infowindow);
    }

    // 매물 타입별 마커 이미지 생성
    createPropertyMarkerImage(propertyType) {
        let imageSrc = '/images/marker-default.png';

        switch(propertyType) {
            case '아파트':
                imageSrc = '/images/marker-apartment.png';
                break;
            case '원룸':
            case '투룸':
                imageSrc = '/images/marker-room.png';
                break;
            case '오피스텔':
                imageSrc = '/images/marker-officetel.png';
                break;
        }

        return this.createMarkerImage(imageSrc, 32, 32);
    }

    // 마커 이미지 생성 헬퍼
    createMarkerImage(src, width, height) {
        const imageSize = new kakao.maps.Size(width, height);
        const imageOption = { offset: new kakao.maps.Point(width/2, height) };

        return new kakao.maps.MarkerImage(src, imageSize, imageOption);
    }

    // 인포윈도우 콘텐츠 생성
    createInfoWindowContent(property) {
        return `
            <div style="padding:10px; width:250px;">
                <div style="font-weight:bold; margin-bottom:5px;">${property.title}</div>
                <div style="color:#e74c3c; font-weight:bold; margin-bottom:5px;">${property.price.toLocaleString()}만원</div>
                <div style="color:#666; margin-bottom:5px;">${property.address}</div>
                <div style="margin-bottom:10px;">
                    <span style="background:#3498db; color:white; padding:2px 6px; border-radius:3px; font-size:12px;">
                        ${property.type}
                    </span>
                </div>
                <button onclick="showPropertyDetail(${property.id})"
                        style="background:#3498db; color:white; border:none; padding:5px 10px; border-radius:3px; cursor:pointer;">
                    상세보기
                </button>
            </div>
        `;
    }

    // 모든 마커 제거
    clearMarkers() {
        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];
        this.closeAllInfoWindows();
        this.infowindows = [];
    }

    // 모든 인포윈도우 닫기
    closeAllInfoWindows() {
        this.infowindows.forEach(infowindow => infowindow.close());
    }

    // 마커들이 모두 보이도록 지도 범위 조정
    fitMapToBounds() {
        if (this.markers.length === 0) return;

        const bounds = new kakao.maps.LatLngBounds();

        this.markers.forEach(marker => {
            bounds.extend(marker.getPosition());
        });

        this.map.setBounds(bounds);
    }

    // 특정 매물 위치로 지도 이동
    moveToProperty(latitude, longitude) {
        const position = new kakao.maps.LatLng(latitude, longitude);
        this.map.setCenter(position);
        this.map.setLevel(2); // 확대
    }

    // 매물 리스트 항목 하이라이트
    highlightPropertyItem(propertyId) {
        // 기존 하이라이트 제거
        document.querySelectorAll('.property-item').forEach(item => {
            item.classList.remove('selected');
        });

        // 새 하이라이트 추가
        const targetItem = document.querySelector(`[data-property-id="${propertyId}"]`);
        if (targetItem) {
            targetItem.classList.add('selected');
            targetItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    // 에러 메시지 표시
    showError(message) {
        const mapContainer = document.getElementById('map');
        mapContainer.innerHTML = `
            <div style="display:flex; align-items:center; justify-content:center; height:100%; color:#666;">
                <div style="text-align:center;">
                    <div style="font-size:48px; margin-bottom:10px;">⚠️</div>
                    <div>${message}</div>
                </div>
            </div>
        `;
    }
}

// 전역 변수로 지도 매니저 인스턴스 생성
let mapManager;