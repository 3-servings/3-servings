-- Cart/CartItem처럼 유저가 직접 만드는 데이터는 여기 안 넣음 -> 테스트에서 실제 서비스 메서드로 생성할 것.

DELETE FROM p_menu_option_group WHERE id = 'a1000000-0000-0000-0000-000000000008';
DELETE FROM p_option_item WHERE id IN ('a1000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000007');
DELETE FROM p_option_group WHERE id = 'a1000000-0000-0000-0000-000000000005';
DELETE FROM p_menu WHERE id IN ('a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000009');
DELETE FROM p_menu_category WHERE id = 'a1000000-0000-0000-0000-000000000003';
DELETE FROM p_store WHERE id = 'a1000000-0000-0000-0000-000000000002';
DELETE FROM p_region WHERE id = 'a1000000-0000-0000-0000-000000000001';
DELETE FROM p_user WHERE id IN (900001, 900002);

-- User: 가게 사장(900001) / 손님(900002)
INSERT INTO p_user (id, username, nickname, email, password, role, login_type, status, created_by, updated_by, created_at, updated_at)
VALUES
    (900001, 'test_owner', 'test_owner', 'test_owner@test.com', 'password', 'OWNER', 'LOCAL', 'ACTIVE', 900001, 900001, now(), now()),
    (900002, 'test_customer', 'test_customer', 'test_customer@test.com', 'password', 'CUSTOMER', 'LOCAL', 'ACTIVE', 900002, 900002, now(), now());

-- Region
INSERT INTO p_region (id, name, is_service_arrea, created_by, updated_by, created_at, updated_at)
VALUES ('a1000000-0000-0000-0000-000000000001', '테스트지역', true, 900001, 900001, now(), now());

-- Store
INSERT INTO p_store (
    id, region_id, owner_id, name, phone, address, detail_address,
    latitude, longitude, min_order_price, delivery_fee, delivery_radius_m,
    is_open, average_rating, review_count, created_by, updated_by, created_at, updated_at, order_count
)
VALUES (
    'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 900001,
    '테스트가게', '010-0000-0000', '서울시 테스트구', NULL,
    NULL, NULL, 10000, 3000, NULL,
    true, 0.0, 0, 900001, 900001, now(), now(), 0
);

-- MenuCategory
INSERT INTO p_menu_category (id, store_id, name, display_order, created_by, updated_by, created_at, updated_at)
VALUES ('a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', '메인메뉴', 0, 900001, 900001, now(), now());

-- Menu (자장면, 5000원)
INSERT INTO p_menu (
    id, menu_category_id, store_id, name, price, description,
    is_description_ai_generated, status, display_order, created_by, updated_by, created_at, updated_at
)
VALUES (
    'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002',
    '자장면', 5000, NULL, false, 'AVAILABLE', 0, 900001, 900001, now(), now()
);

-- Menu (군만두, 3000원)
INSERT INTO p_menu (
    id, menu_category_id, store_id, name, price, description,
    is_description_ai_generated, status, display_order, created_by, updated_by, created_at, updated_at
)
VALUES (
    'a1000000-0000-0000-0000-000000000009', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002',
    '군만두', 3000, NULL, false, 'AVAILABLE', 1, 900001, 900001, now(), now()
);

-- OptionGroup (곱빼기 여부 - 필수 1개 선택: min_select=1, max_select=1)
INSERT INTO p_option_group (id, store_id, name, min_select, max_select, created_by, updated_by, created_at, updated_at)
VALUES ('a1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000002', '곱빼기 여부', 1, 1, 900001, 900001, now(), now());

-- OptionItem (보통 / 곱빼기)
INSERT INTO p_option_item (id, option_group_id, name, price, status, created_by, updated_by, created_at, updated_at, display_order)
VALUES
    ('a1000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000005', '보통', 0, 'AVAILABLE', 900001, 900001, now(), now(), 0),
    ('a1000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000005', '곱빼기', 1000, 'AVAILABLE', 900001, 900001, now(), now(),1);

-- MenuOptionGroup (메뉴 <-> 옵션그룹 연결)
INSERT INTO p_menu_option_group (id, menu_id, option_group_id, display_order, created_by, created_at)
VALUES ('a1000000-0000-0000-0000-000000000008', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000005', 0, 900001, now());
