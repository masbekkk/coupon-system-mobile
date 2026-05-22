# Coupon System API Reference (Mobile Client)

Dokumentasi ini merinci struktur HTTP headers, request body, dan JSON response untuk API yang diintegrasikan dengan aplikasi Android Coupon Production System.

---

## 🌐 Headers Global

### Headers untuk Akses Publik (Tanpa Login)
Semua request tanpa token wajib menyertakan header berikut:
```http
Accept: application/json
Content-Type: application/json
```

### Headers Terautentikasi (Bearer Token via Laravel Sanctum)
Semua request yang membutuhkan autentikasi wajib menyertakan token yang diperoleh saat login:
```http
Accept: application/json
Content-Type: application/json
Authorization: Bearer {auth_token}
```

---

## 🔑 1. Autentikasi

### login
* **Method**: `POST`
* **Path**: `/api/v1/auth/login`
* **Auth**: Tidak (Publik)

#### Request Body
```json
{
  "email": "operator@jasuindo.co.id",
  "password": "secretpassword",
  "device_name": "android_Pixel_7_Pro"
}
```

#### Response Sukses (`200 OK`)
```json
{
  "token": "1|abcdefghijklmnopqrstuvwxyz1234567890",
  "user": {
    "id": "019e46a0-5c34-71e1-b8d4-781fddb0f08a",
    "name": "Operator Jasuindo",
    "email": "operator@jasuindo.co.id"
  }
}
```

#### Response Error Validasi (`422 Unprocessable Entity`)
```json
{
  "message": "The email field is required. (and 1 more error)",
  "errors": {
    "email": [
      "Email harus berupa alamat email yang valid."
    ],
    "password": [
      "Password wajib diisi."
    ]
  }
}
```

#### Response Error Credential (`401 Unauthorized`)
```json
{
  "message": "Kredensial yang Anda masukkan salah."
}
```

---

### logout
* **Method**: `POST`
* **Path**: `/api/v1/auth/logout`
* **Auth**: Ya (Bearer Token)

#### Request Body
*None (Empty)*

#### Response Sukses (`200 OK`)
```json
{
  "message": "Berhasil keluar"
}
```

---

## 📊 2. Dashboard

### getStats
* **Method**: `GET`
* **Path**: `/api/v1/dashboard/stats`
* **Auth**: Ya (Bearer Token)

#### Request Body
*None*

#### Response Sukses (`200 OK`)
```json
{
  "data": {
    "total_projects": 12,
    "generated_batches": 4,
    "total_coupons": 150000,
    "recent_projects": [
      {
        "id": 1,
        "code": "PROJ-2026-X1",
        "name": "Kupon Gosok Ramadhan",
        "status": "ready",
        "total_coupons": 50000,
        "created_by": "Jamilah Turrohmah"
      }
    ]
  }
}
```

---

## 📁 3. Manajemen Proyek

### getProjects
* **Method**: `GET`
* **Path**: `/api/v1/projects?page={page}&per_page={per_page}`
* **Auth**: Ya (Bearer Token)

#### Query Parameters
* `page`: Integer (Default: 1)
* `per_page`: Integer (Default: 15)

#### Response Sukses (`200 OK`)
```json
{
  "data": [
    {
      "id": 7,
      "code": "TESTTT3",
      "name": "Promo Akhir Tahun",
      "description": "Generating instant prize coupons",
      "status": "ready",
      "config": {
        "total_coupons": 10000,
        "total_boxes": 10,
        "coupons_per_box": 1000,
        "total_batches": 2,
        "boxes_per_batch": 5
      },
      "creator": {
        "id": "019e4578-838f-711a-8634-71af24204a4d",
        "name": "Admin User"
      },
      "created_at": "2026-05-21T22:15:27+07:00"
    }
  ],
  "links": {
    "first": "http://127.0.0.1:8000/api/v1/projects?page=1",
    "last": "http://127.0.0.1:8000/api/v1/projects?page=1",
    "prev": null,
    "next": null
  },
  "meta": {
    "current_page": 1,
    "last_page": 1,
    "per_page": 15,
    "total": 2
  }
}
```

---

### createProject
* **Method**: `POST`
* **Path**: `/api/v1/projects`
* **Auth**: Ya (Bearer Token)

#### Request Body
```json
{
  "name": "Promo Akhir Tahun",
  "code": "TESTTT3",
  "description": "Generating instant prize coupons",
  "total_coupons": 10000,
  "coupons_per_box": 1000,
  "total_boxes": 10,
  "total_batches": 2,
  "boxes_per_batch": 5,
  "tiers": [
    {
      "name": "Hadiah Rp 100.000",
      "amount": 100000,
      "total_quantity": 10,
      "per_box_quantity": 1
    },
    {
      "name": "Anda Belum Beruntung",
      "amount": 0,
      "total_quantity": 8100,
      "per_box_quantity": 810
    }
  ]
}
```
> **Catatan Validasi Proyek**: Total `per_box_quantity` dari semua `tiers` **harus persis sama** dengan nilai `coupons_per_box`.

#### Response Sukses (`201 Created`)
```json
{
  "message": "Project created successfully",
  "data": {
    "id": 7,
    "code": "TESTTT3",
    "name": "Promo Akhir Tahun",
    "description": "Generating instant prize coupons",
    "status": "draft",
    "config": {
      "total_coupons": 10000,
      "total_boxes": 10,
      "coupons_per_box": 1000,
      "total_batches": 2,
      "boxes_per_batch": 5
    },
    "creator": {
      "id": "019e4578-838f-711a-8634-71af24204a4d",
      "name": "Admin User"
    },
    "prize_tiers": [
      {
        "id": 37,
        "name": "Hadiah Rp 100.000",
        "amount": 100000
      }
    ],
    "created_at": "2026-05-21T22:15:27+07:00"
  }
}
```

---

### getProjectDetail
* **Method**: `GET`
* **Path**: `/api/v1/projects/{id}`
* **Auth**: Ya (Bearer Token)

#### Response Sukses (`200 OK`)
```json
{
  "data": {
    "id": 7,
    "code": "TESTTT3",
    "name": "Promo Akhir Tahun",
    "description": "Generating instant prize coupons",
    "status": "ready",
    "config": {
      "total_coupons": 10000,
      "total_boxes": 10,
      "coupons_per_box": 1000,
      "total_batches": 2,
      "boxes_per_batch": 5
    },
    "creator": {
      "id": "019e4578-838f-711a-8634-71af24204a4d",
      "name": "Admin User"
    },
    "prize_tiers": [
      {"id": 37, "name": "Hadiah Rp 100.000", "amount": 100000},
      {"id": 38, "name": "Hadiah Rp 50.000", "amount": 50000},
      {"id": 39, "name": "Hadiah Rp 20.000", "amount": 20000},
      {"id": 40, "name": "Hadiah Rp 10.000", "amount": 10000},
      {"id": 41, "name": "Hadiah Rp 5.000", "amount": 5000},
      {"id": 42, "name": "Anda Belum Beruntung", "amount": 0}
    ],
    "created_at": "2026-05-21T22:15:27+07:00"
  }
}
```

---

### deleteProject
* **Method**: `DELETE`
* **Path**: `/api/v1/projects/{id}`
* **Auth**: Ya (Bearer Token)

#### Response Sukses (`200 OK`)
```json
{
  "message": "Proyek berhasil dihapus"
}
```

#### Response Error (`400 Bad Request` / Proyek bukan Draft)
```json
{
  "message": "Hanya proyek berstatus draft yang dapat dihapus."
}
```

---

## ⚙️ 4. Batch & Laporan Produksi

### getProjectBatches
* **Method**: `GET`
* **Path**: `/api/v1/projects/{id}/batches`
* **Auth**: Ya (Bearer Token)

#### Response Sukses (`200 OK`)
```json
{
  "data": [
    {
      "id": 12,
      "batch_number": 1,
      "status": "pending",
      "operator": null,
      "location": null,
      "generated_at": null,
      "total_boxes": 5,
      "project_id": 1
    },
    {
      "id": 13,
      "batch_number": 2,
      "status": "completed",
      "operator": "Operator Jasuindo",
      "location": "Surabaya Plant A",
      "generated_at": "2026-05-21 15:30:22",
      "total_boxes": 5,
      "project_id": 1
    }
  ]
}
```

---

### generateBatch
* **Method**: `POST`
* **Path**: `/api/v1/batches/{id}/generate`
* **Auth**: Ya (Bearer Token)

#### Request Body
```json
{
  "location": "Sidoarjo Gedangan Plant 3"
}
```

#### Response Sukses (`200 OK`)
```json
{
  "data": {
    "id": 12,
    "batch_number": 1,
    "status": "ready",
    "operator": "Operator Jasuindo",
    "location": "Sidoarjo Gedangan Plant 3",
    "generated_at": "2026-05-21 22:04:15",
    "total_boxes": 5,
    "project_id": 1
  },
  "message": "Batch berhasil diproduksi"
}
```

---

### getBatchReport
* **Method**: `GET`
* **Path**: `/api/v1/batches/{id}/report`
* **Auth**: Ya (Bearer Token)

#### Response Sukses (`200 OK`)
```json
{
  "data": {
    "id": 12,
    "batch_number": 1,
    "status": "ready",
    "operator": "Operator Jasuindo",
    "location": "Sidoarjo Gedangan Plant 3",
    "total_boxes": 5,
    "project_name": "Kupon Gosok Ramadhan",
    "boxes": [
      {
        "box_number": 1,
        "coupon_count": 1000,
        "prize_distribution": [
          {
            "tier_name": "Jackpot Utama",
            "count": 1
          },
          {
            "tier_name": "ZonK",
            "count": 999
          }
        ]
      }
    ]
  }
}
```

---

## 🎟️ 5. Manajemen Kupon

### getProjectCoupons
* **Method**: `GET`
* **Path**: `/api/v1/projects/{id}/coupons?page={page}&per_page={per_page}&search={search}&batch_id={batch_id}&prize_tier={prize_tier}&sort={sort}`
* **Auth**: Ya (Bearer Token)

#### Query Parameters
* `page`: Integer (Default: 1)
* `per_page`: Integer (Default: 25)
* `search`: String (Pencarian Parsial berdasarkan Serial Number)
* `batch_id`: Integer (Filter ID Batch spesifik)
* `prize_tier`: String (Filter Nama Tier Hadiah spesifik)
* `sort`: String (Pilihan: `asc`, `desc` untuk urutan Serial Number)

#### Response Sukses (`200 OK`)
```json
{
  "data": [
    {
      "id": 1024,
      "serial_number": "SN-0000000000000000000000001024",
      "box_number": 1,
      "position": 24,
      "prize_tier": "Jackpot Utama",
      "amount": 1000000,
      "batch_number": 1
    }
  ],
  "links": {
    "first": "https://domain.com/api/v1/projects/1/coupons?page=1",
    "last": "https://domain.com/api/v1/projects/1/coupons?page=400",
    "prev": null,
    "next": "https://domain.com/api/v1/projects/1/coupons?page=2"
  },
  "meta": {
    "current_page": 1,
    "last_page": 400,
    "per_page": 25,
    "total": 10000
  }
}
```

---

### exportCoupons (Download)
* **Method**: `GET`
* **Path**: `/api/v1/projects/{id}/coupons/export`
* **Auth**: Ya (Bearer Token)

#### Headers Wajib
```http
Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Authorization: Bearer {auth_token}
```
* **Output**: Binary Stream file Excel (.xlsx)

---

## ⚙️ 6. Pengaturan Profil & Password

### updateProfile
* **Method**: `PATCH`
* **Path**: `/api/v1/user`
* **Auth**: Ya (Bearer Token)

#### Request Body
```json
{
  "name": "Operator Baru Jasuindo",
  "email": "new.op@jasuindo.co.id"
}
```

#### Response Sukses (`200 OK`)
```json
{
  "message": "Profil berhasil disimpan"
}
```

---

### changePassword
* **Method**: `POST`
* **Path**: `/api/v1/user/password`
* **Auth**: Ya (Bearer Token)

#### Request Body
```json
{
  "current_password": "secretpassword",
  "new_password": "mynewpassword123",
  "new_password_confirmation": "mynewpassword123"
}
```

#### Response Sukses (`200 OK`)
```json
{
  "message": "Password berhasil diubah"
}
```

#### Response Error Validasi (`422 Unprocessable Entity`)
```json
{
  "message": "The current password does not match.",
  "errors": {
    "current_password": [
      "Password saat ini yang Anda masukkan salah."
    ],
    "new_password": [
      "Panjang password baru minimal adalah 8 karakter."
    ]
  }
}
```
