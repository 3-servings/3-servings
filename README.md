# 🍽️ 3-Servings

Spring Boot 기반 음식 주문 관리 플랫폼

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=springboot&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?logo=postgresql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-232F3E?logo=amazonwebservices&logoColor=white)

## 📌 Project Overview

- **프로젝트 기간** : 2026.07.02~07.16
- **개발 인원** : 6명
- **아키텍처** : Spring Boot Monolithic
- **배포 환경** : AWS EC2 + Docker + Nginx

## ✨ 주요 기능

## ✨ 주요 기능

| 👤 회원(Auth) | 🏪 가게 | 🍽️ 메뉴 | 🛒 주문 |
|:---:|:---:|:---:|:---:|
| ✅ 회원가입 / 로그인<br>✅ 소셜 로그인<br>✅ JWT 인증<br>✅ Access Token 재발급<br>✅ 로그아웃 / 회원탈퇴 | ✅ 가게 CRUD<br>✅ 카테고리 관리<br>✅ 지역 관리<br>✅ 서비스 가능 지역 설정 | ✅ 메뉴 CRUD<br>✅ 메뉴 카테고리 관리<br>✅ 옵션 그룹 관리<br>✅ Presigned URL 발급 | ✅ 장바구니 관리<br>✅ 체크아웃(주문 생성)<br>✅ 주문 조회<br>✅ 주문 수정 / 취소 |

| 👨‍🍳 주문 관리 | 💳 결제 | ⭐ 리뷰 | 🤖 AI |
|:---:|:---:|:---:|:---:|
| ✅ 주문 수락 / 거절<br>✅ 주문 상태 변경<br>✅ 예상 조리시간 수정<br>✅ 주문 통계 조회 | ✅ Toss Payments 연동<br>✅ 결제 요청 / 환불<br>✅ 결제 내역 조회<br>✅ 결제 로그 조회 | ✅ 리뷰 CRUD<br>✅ 가게 리뷰 조회<br>✅ 사장 답글 작성 / 수정 | ✅ AI 상품 설명 생성<br>✅ AI 메뉴 추천 |

## 👨‍💻 Team Members

| 이름 | 담당 |
|:----:|------|
| 나상우 | 회원(Auth), 가게 |
| 남건우 | 회원(Auth), 리뷰 |
| 김동현 | 메뉴 |
| 김준서 | 주문 |
| 주원영 | 주문 관리 |
| 이은빈 | 결제, 인프라 |

## 🛠 Tech Stack

### Backend

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/JPA-59666C?logo=hibernate&logoColor=white)

### Database

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?logo=postgresql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)

### Infra

![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white) ![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?logo=amazonec2&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?logo=amazons3&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?logo=nginx&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white)

### Collaboration

![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white) ![Slack](https://img.shields.io/badge/Slack-4A154B?logo=slack&logoColor=white) ![Notion](https://img.shields.io/badge/Notion-000000?logo=notion&logoColor=white)


## 🏗️ Service Architecture

<img src="docs/infra_architecture.png" width="900">

Docker Compose를 기반으로 애플리케이션을 컨테이너화하고 AWS EC2 환경에 배포했습니다. <br><br>

✔️ Docker Compose를 통해 Spring Boot, PostgreSQL, Redis를 컨테이너로 구성<br>
✔️ Nginx Reverse Proxy를 적용하여 외부 요청을 처리<br>
✔️ GitHub Actions 기반 CI/CD를 구축하여 자동 배포
## 🗄 ERD

🔗 [ERD Cloud](https://www.erdcloud.com/d/m7CQPnZPCsg9htrRu)

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/3-servings/3-servings.git
cd 3-servings
```

### 2. Environment Variables

프로젝트 실행 전 `.env` 파일을 생성하고 아래 환경 변수를 설정합니다.

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=

# PostgreSQL
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# Redis
REDIS_HOST=
REDIS_PORT=

# AWS S3
AWS_REGION=
S3_BUCKET_NAME=

# JWT
JWT_SECRET=

# Toss Payments
TOSS_SECRET_KEY=
```

### 3. Run

```bash
docker compose up -d
```

### 4. Access

애플리케이션 실행 후 아래 주소로 접속할 수 있습니다.

```
http://localhost:19096
```

### 5. Stop

실행 중인 컨테이너를 종료합니다.

```bash
docker compose down
```

데이터 볼륨까지 함께 삭제하려면 다음 명령어를 사용합니다.

```bash
docker compose down -v
```