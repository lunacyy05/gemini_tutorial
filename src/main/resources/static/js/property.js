// 매물 관련 기능을 담당하는 모듈
class PropertyManager {
    constructor() {
        this.properties = [];
        this.filteredProperties = [];
        this.currentFilters = {};
    }

    // 모든 매물 데이터 로드
    async loadProperties() {
        try {
            this.showLoading();

            const response = await fetch('/api/properties');

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            this.properties = await response.json();
            this.filteredProperties = [...this.properties];

            this.renderPropertyList();
            this.updateMap();

            console.log(`${this.properties.length}개의 매물을 로드했습니다.`);

        } catch (error) {
            console.error('매물 데이터 로드 실패:', error);
            this.showError('매물 데이터를 불러올 수 없습니다.');
        }
    }

    // 필터 적용하여 매물 검색
    async searchProperties(filters = {}) {
        try {
            this.showLoading();
            this.currentFilters = filters;

            // 백엔드 API로 필터링된 매물 요청
            const queryParams = new URLSearchParams();

            if (filters.type) queryParams.append('type', filters.type);
            if (filters.minPrice) queryParams.append('minPrice', filters.minPrice);
            if (filters.maxPrice) queryParams.append('maxPrice', filters.maxPrice);
            if (filters.address) queryParams.append('address', filters.address);

            const url = `/api/properties/search?${queryParams.toString()}`;
            const response = await fetch(url);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            this.filteredProperties = await response.json();

            this.renderPropertyList();
            this.updateMap();

            console.log(`필터 조건에 맞는 ${this.filteredProperties.length}개의 매물을 찾았습니다.`);

        } catch (error) {
            console.error('매물 검색 실패:', error);
            this.showError('매물 검색 중 오류가 발생했습니다.');
        }
    }

    // 매물 리스트 렌더링
    renderPropertyList() {
        const container = document.getElementById('propertyList');

        if (this.filteredProperties.length === 0) {
            container.innerHTML = this.getEmptyStateHTML();
            return;
        }

        const html = this.filteredProperties.map(property =>
            this.createPropertyItemHTML(property)
        ).join('');

        container.innerHTML = html;

        // 이벤트 리스너 추가
        this.attachPropertyListeners();
    }

    // 개별 매물 아이템 HTML 생성
    createPropertyItemHTML(property) {
        return `
            <div class="property-item"
                 data-property-id="${property.id}"
                 onclick="propertyManager.selectProperty(${property.id})">

                <div class="property-title">${property.title}</div>

                <div class="property-price">
                    ${property.price.toLocaleString()}만원
                </div>

                <div class="property-address">
                    ${property.address}
                </div>

                <div class="property-details">
                    <span class="property-type">${property.type}</span>

                    ${property.area ? `
                        <span style="margin-left: 10px; color: #666; font-size: 12px;">
                            ${property.area}㎡
                        </span>
                    ` : ''}

                    ${property.floor ? `
                        <span style="margin-left: 10px; color: #666; font-size: 12px;">
                            ${property.floor}층
                        </span>
                    ` : ''}
                </div>

                ${property.description ? `
                    <div style="margin-top: 8px; color: #888; font-size: 13px; line-height: 1.4;">
                        ${property.description.length > 80
                            ? property.description.substring(0, 80) + '...'
                            : property.description}
                    </div>
                ` : ''}
            </div>
        `;
    }

    // 빈 상태 HTML
    getEmptyStateHTML() {
        return `
            <div style="text-align: center; padding: 40px; color: #888;">
                <div style="font-size: 48px; margin-bottom: 15px;">🏠</div>
                <div style="font-size: 16px; margin-bottom: 5px;">검색 조건에 맞는 매물이 없습니다</div>
                <div style="font-size: 14px;">다른 조건으로 검색해보세요</div>
            </div>
        `;
    }

    // 매물 선택
    selectProperty(propertyId) {
        const property = this.filteredProperties.find(p => p.id === propertyId);

        if (!property) {
            console.error('선택된 매물을 찾을 수 없습니다:', propertyId);
            return;
        }

        // 지도에서 해당 위치로 이동
        if (mapManager && property.latitude && property.longitude) {
            mapManager.moveToProperty(property.latitude, property.longitude);
        }

        // 매물 리스트에서 하이라이트
        this.highlightProperty(propertyId);

        console.log('매물 선택:', property.title);
    }

    // 매물 하이라이트
    highlightProperty(propertyId) {
        // 기존 하이라이트 제거
        document.querySelectorAll('.property-item').forEach(item => {
            item.classList.remove('selected');
        });

        // 새 하이라이트 추가
        const targetItem = document.querySelector(`[data-property-id="${propertyId}"]`);
        if (targetItem) {
            targetItem.classList.add('selected');
        }
    }

    // 매물 상세 정보 보기
    async showPropertyDetail(propertyId) {
        try {
            const response = await fetch(`/api/properties/${propertyId}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const property = await response.json();

            // 상세 정보 모달 또는 페이지로 이동
            this.openPropertyDetailModal(property);

        } catch (error) {
            console.error('매물 상세 정보 로드 실패:', error);
            alert('매물 상세 정보를 불러올 수 없습니다.');
        }
    }

    // 매물 상세 정보 모달 (간단한 구현)
    openPropertyDetailModal(property) {
        const modal = document.createElement('div');
        modal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.7); z-index: 1000;
            display: flex; align-items: center; justify-content: center;
        `;

        modal.innerHTML = `
            <div style="background: white; padding: 30px; border-radius: 10px; max-width: 500px; width: 90%;">
                <h2 style="margin-bottom: 20px;">${property.title}</h2>

                <div style="margin-bottom: 15px;">
                    <strong>가격:</strong> ${property.price.toLocaleString()}만원
                </div>

                <div style="margin-bottom: 15px;">
                    <strong>주소:</strong> ${property.address}
                </div>

                <div style="margin-bottom: 15px;">
                    <strong>유형:</strong> ${property.type}
                </div>

                ${property.area ? `
                    <div style="margin-bottom: 15px;">
                        <strong>면적:</strong> ${property.area}㎡
                    </div>
                ` : ''}

                ${property.floor ? `
                    <div style="margin-bottom: 15px;">
                        <strong>층수:</strong> ${property.floor}층
                    </div>
                ` : ''}

                ${property.description ? `
                    <div style="margin-bottom: 20px;">
                        <strong>설명:</strong><br>
                        <div style="margin-top: 5px; line-height: 1.5;">${property.description}</div>
                    </div>
                ` : ''}

                <button onclick="this.parentElement.parentElement.remove()"
                        style="background: #3498db; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer;">
                    닫기
                </button>
            </div>
        `;

        document.body.appendChild(modal);

        // 모달 배경 클릭시 닫기
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.remove();
            }
        });
    }

    // 지도 업데이트
    updateMap() {
        if (mapManager) {
            mapManager.addPropertyMarkers(this.filteredProperties);
        }
    }

    // 이벤트 리스너 추가
    attachPropertyListeners() {
        // 매물 아이템 호버 효과는 CSS로 처리
        // 추가적인 이벤트가 필요한 경우 여기에 구현
    }

    // 로딩 상태 표시
    showLoading() {
        const container = document.getElementById('propertyList');
        container.innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
            </div>
        `;
    }

    // 에러 상태 표시
    showError(message) {
        const container = document.getElementById('propertyList');
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #e74c3c;">
                <div style="font-size: 48px; margin-bottom: 15px;">⚠️</div>
                <div style="font-size: 16px;">${message}</div>
            </div>
        `;
    }
}

// 전역 함수들 (HTML에서 직접 호출)
function showPropertyDetail(propertyId) {
    if (window.propertyManager) {
        window.propertyManager.showPropertyDetail(propertyId);
    }
}

// 전역 변수로 매물 매니저 인스턴스 생성
let propertyManager;