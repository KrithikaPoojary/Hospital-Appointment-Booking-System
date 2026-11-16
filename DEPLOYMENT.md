# 🚀 Cloud Deployment Guide — Render (Free Tier)

This application can be deployed 100% free on [Render](https://render.com) using the included `render.yaml` Blueprint or via manual setup.

---

## Option 1: Automatic Blueprint Deployment (Recommended)

1. **Sign in to Render**:
   - Go to [dashboard.render.com](https://dashboard.render.com/) and log in with your GitHub account.

2. **Create New Blueprint**:
   - Click **New +** in the top navigation bar and select **Blueprint**.
   - Connect your GitHub repository:
     `https://github.com/KrithikaPoojary/Hospital-Appointment-Booking-System.git`
   - Render will automatically detect [`render.yaml`](./render.yaml).

3. **Deploy Services**:
   - Render will automatically provision:
     1. **PostgreSQL Database** (`hospital-postgres`, Free Tier).
     2. **Web Service** (`hospital-appointment-app`, Docker runtime).
   - Click **Apply** to start building and deploying.

---

## Option 2: Manual Deployment

### Step A: Provision PostgreSQL Database
1. In Render Dashboard, click **New +** → **PostgreSQL**.
2. Set Name: `hospital-db`.
3. Set Plan: **Free**.
4. Click **Create Database**.
5. Once created, copy the **Internal Database URL**.

### Step B: Deploy the Web Service
1. Click **New +** → **Web Service**.
2. Select **Build and deploy from a Git repository** and pick your repository.
3. Configure the service:
   - **Name**: `hospital-appointment-app`
   - **Language**: `Docker`
   - **Branch**: `main`
   - **Plan**: `Free`
4. Add the following **Environment Variables**:
   | Key | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | `<Your Internal Database URL from Step A>` |
   | `SPRING_DATASOURCE_USERNAME` | `<Your DB User from Step A>` |
   | `SPRING_DATASOURCE_PASSWORD` | `<Your DB Password from Step A>` |
   | `APP_JWT_SECRET` | `MyHospitalProductionSecretKeyForJWT2024MustBeLongEnough12345` |
   | `APP_JWT_EXPIRATION_MS` | `86400000` |
   | `PORT` | `8080` |
5. Click **Create Web Service**.

---

## 🔑 Demo Seed Accounts (Ready for Testing)

The system automatically initializes test accounts on the first run:

| Role | Username | Password | Notes |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Access to metrics, appointment status updates, global log |
| **Patient** | `patient` | `patient123` | Search doctors, book slots, view & cancel appointments |
| **Doctor** | `dr.smith` | `doctor123` | Cardiology specialist — manage availability slots |
| **Doctor** | `dr.sarah` | `doctor123` | Neurology specialist |
| **Doctor** | `dr.raj` | `doctor123` | Pediatrics specialist |
| **Doctor** | `dr.emily` | `doctor123` | Dermatology specialist |

---

## 🌐 URLs After Deployment

- **Web Application**: `https://<your-render-subdomain>.onrender.com/`
- **Swagger API UI**: `https://<your-render-subdomain>.onrender.com/swagger-ui.html`
- **OpenAPI JSON Spec**: `https://<your-render-subdomain>.onrender.com/api-docs`
