# MonitorX - Free Production Deployment Guide

Deploy MonitorX completely free using Vercel + Render + MongoDB Atlas!

---

## 🆓 Free Tier Overview

| Service | Provider | Free Tier | Limitations |
|---------|----------|-----------|-------------|
| **Frontend** | Vercel | Unlimited | None for personal projects |
| **Backend** | Render | 750 hours/mo | Spins down after 15 min inactivity |
| **Database** | MongoDB Atlas | 512MB storage | Shared cluster |
| **Total Cost** | | **$0/month** | |

> **Note:** Render free tier spins down after 15 minutes of inactivity. First request after spin-down takes ~30-60 seconds. Perfect for demos, portfolios, or low-traffic apps.

---

## 📋 Prerequisites

1. **GitHub Account** (to host your code)
2. **Vercel Account** (sign up at vercel.com)
3. **Render Account** (sign up at render.com)
4. **MongoDB Atlas Account** (sign up at mongodb.com/cloud/atlas)

---

## 🚀 Step-by-Step Deployment

### Part 1: Prepare Your Code

#### 1.1 Push Code to GitHub

```bash
cd D:\MonitorX

# Initialize git (if not already done)
git init

# Add .gitignore
echo "node_modules
.next
.env
.env.local
*.log
.DS_Store" > .gitignore

# Commit all files
git add .
git commit -m "Initial commit - MonitorX"

# Create GitHub repository (via GitHub website)
# Then connect and push:
git remote add origin https://github.com/YOUR_USERNAME/MonitorX.git
git branch -M main
git push -u origin main
```

#### 1.2 Create `render.yaml` for Backend

Create a file in the root directory:

```bash
# Create render.yaml
New-Item -Path "render.yaml" -ItemType File
```

Add this content:

```yaml
services:
  # Backend Service
  - type: web
    name: monitorx-backend
    runtime: docker
    dockerfilePath: ./backend/Dockerfile
    dockerContext: ./backend
    envVars:
      - key: LOGS_DB_URI
        sync: false
      - key: METADATA_DB_URI
        sync: false
      - key: JWT_SECRET
        generateValue: true
      - key: JWT_EXPIRATION
        value: 86400000
      - key: SERVER_PORT
        value: 8080
      - key: CORS_ORIGINS
        sync: false
    healthCheckPath: /actuator/health
```

Commit this file:
```bash
git add render.yaml
git commit -m "Add Render configuration"
git push
```

---

### Part 2: Set Up MongoDB Atlas (Free Database)

#### 2.1 Create MongoDB Atlas Account

1. Go to [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)
2. Click "Start Free"
3. Sign up with Google/email

#### 2.2 Create Free Cluster

1. Choose "M0 FREE" tier
2. **Cloud Provider:** AWS
3. **Region:** Choose closest to you (e.g., Mumbai for India)
4. **Cluster Name:** `MonitorX-Cluster`
5. Click "Create Cluster" (takes 3-5 minutes)

#### 2.3 Configure Database Access

**Create Database User:**
1. Go to "Database Access" (left sidebar)
2. Click "Add New Database User"
3. **Authentication Method:** Password
4. **Username:** `monitorx_user`
5. **Password:** Generate secure password (save it!)
6. **Database User Privileges:** "Atlas Admin"
7. Click "Add User"

**Whitelist IP Addresses:**
1. Go to "Network Access" (left sidebar)
2. Click "Add IP Address"
3. Click "Allow Access from Anywhere" (0.0.0.0/0)
4. Click "Confirm"

> **Note:** For production, you'd restrict this to Render's IP ranges, but free tier needs broad access.

#### 2.4 Get Connection Strings

1. Go to "Database" → Click "Connect" on your cluster
2. Choose "Connect your application"
3. **Driver:** Node.js
4. **Version:** 4.1 or later
5. Copy the connection string

You'll get something like:
```
mongodb+srv://monitorx_user:<password>@monitorx-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
```

**Create two connection strings:**

Replace `<password>` with your actual password and add database names:

```bash
# Logs Database
mongodb+srv://monitorx_user:YOUR_PASSWORD@monitorx-cluster.xxxxx.mongodb.net/logs_db?retryWrites=true&w=majority

# Metadata Database  
mongodb+srv://monitorx_user:YOUR_PASSWORD@monitorx-cluster.xxxxx.mongodb.net/metadata_db?retryWrites=true&w=majority
```

**Save these!** You'll need them for Render configuration.

---

### Part 3: Deploy Backend to Render

#### 3.1 Create Render Account

1. Go to [render.com](https://render.com)
2. Sign up with GitHub (easiest)
3. Authorize Render to access your repositories

#### 3.2 Create New Web Service

1. Click "New +" → "Web Service"
2. Connect your GitHub repository: `MonitorX`
3. Render will detect your repository

#### 3.3 Configure Backend Service

**Basic Settings:**
- **Name:** `monitorx-backend`
- **Region:** Oregon (US West) or Singapore (closest free region)
- **Branch:** `main`
- **Root Directory:** `backend`
- **Runtime:** `Docker`
- **Dockerfile Path:** `backend/Dockerfile`

**Plan:**
- Select **"Free"** plan

**Environment Variables:**

Click "Add Environment Variable" for each:

| Key | Value |
|-----|-------|
| `LOGS_DB_URI` | `mongodb+srv://monitorx_user:YOUR_PASSWORD@monitorx-cluster.xxxxx.mongodb.net/logs_db?retryWrites=true&w=majority` |
| `METADATA_DB_URI` | `mongodb+srv://monitorx_user:YOUR_PASSWORD@monitorx-cluster.xxxxx.mongodb.net/metadata_db?retryWrites=true&w=majority` |
| `JWT_SECRET` | (Click "Generate" or paste: get from `openssl rand -base64 64`) |
| `JWT_EXPIRATION` | `86400000` |
| `SERVER_PORT` | `8080` |
| `CORS_ORIGINS` | `https://monitorx-frontend.vercel.app` (we'll update this later) |

**Advanced Settings:**
- **Health Check Path:** `/actuator/health`
- **Auto-Deploy:** Yes

#### 3.4 Deploy

1. Click "Create Web Service"
2. Render will start building your Docker image
3. **Build time:** 5-10 minutes (first deploy)
4. Watch the logs for any errors

Once deployed, you'll get a URL like:
```
https://monitorx-backend.onrender.com
```

**Test it:**
```bash
curl https://monitorx-backend.onrender.com/actuator/health
```

Should return: `{"status":"UP"}`

---

### Part 4: Deploy Frontend to Vercel

#### 4.1 Create Vercel Account

1. Go to [vercel.com](https://vercel.com)
2. Sign up with GitHub
3. Authorize Vercel to access repositories

#### 4.2 Import Project

1. Click "Add New..." → "Project"
2. Import your `MonitorX` repository
3. Vercel will auto-detect Next.js

#### 4.3 Configure Frontend

**Framework Preset:** Next.js (auto-detected)

**Root Directory:** `frontend` ← **Important!**

**Build Settings:**
- Build Command: `npm run build`
- Output Directory: `.next` (auto-detected)
- Install Command: `npm install`

**Environment Variables:**

Add this environment variable:

| Key | Value |
|-----|-------|
| `NEXT_PUBLIC_API_URL` | `https://monitorx-backend.onrender.com` |

Replace with your actual Render backend URL.

#### 4.4 Deploy

1. Click "Deploy"
2. **Build time:** 3-5 minutes
3. Vercel will build and deploy automatically

Once deployed, you'll get URLs like:
```
Production: https://monitorx-frontend.vercel.app
```

---

### Part 5: Update CORS Settings

Now that you have your frontend URL, update the backend:

1. Go to Render Dashboard
2. Select your `monitorx-backend` service
3. Go to "Environment"
4. Update `CORS_ORIGINS`:
   ```
   https://monitorx-frontend.vercel.app
   ```
5. Click "Save Changes"
6. Render will automatically redeploy

---

### Part 6: Configure Custom Domain (Optional)

#### For Frontend (Vercel):

1. Go to your Vercel project → "Settings" → "Domains"
2. Add your domain: `monitorx.yourdomain.com`
3. Vercel will provide DNS records
4. Add these records to your domain registrar
5. SSL is automatic and free!

#### For Backend (Render):

1. Go to your Render service → "Settings" → "Custom Domain"
2. Add domain: `api.yourdomain.com`
3. Render will provide CNAME record
4. Add to your domain registrar
5. SSL is automatic and free!

**Update Environment Variables:**
- Vercel: Update `NEXT_PUBLIC_API_URL` to `https://api.yourdomain.com`
- Render: Update `CORS_ORIGINS` to `https://monitorx.yourdomain.com`

---

### Part 7: Create Admin User

Once both services are deployed:

```powershell
# Create admin user
$signup = @{
    username = "admin"
    email = "admin@yourdomain.com"
    password = "YourStrongPassword123!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://monitorx-backend.onrender.com/auth/signup" `
    -Method POST `
    -Body $signup `
    -ContentType "application/json"
```

Or using curl:
```bash
curl -X POST https://monitorx-backend.onrender.com/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@yourdomain.com",
    "password": "YourStrongPassword123!"
  }'
```

---

## ✅ Verify Deployment

### 1. Test Frontend
Visit: `https://monitorx-frontend.vercel.app`

You should see:
- ✅ Landing page loads
- ✅ Login page accessible
- ✅ Styling working correctly

### 2. Test Backend
```bash
curl https://monitorx-backend.onrender.com/actuator/health
```

Should return: `{"status":"UP"}`

### 3. Test Full Flow
1. Go to frontend URL
2. Click "Signup"
3. Create a test account
4. Login with credentials
5. Access dashboard
6. Verify you can see the dashboard (no data yet)

---

## 🎯 Your Free Production URLs

After deployment, you'll have:

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | `https://monitorx-frontend.vercel.app` | User interface |
| **Backend** | `https://monitorx-backend.onrender.com` | API server |
| **API Docs** | `https://monitorx-backend.onrender.com/swagger-ui.html` | API documentation |
| **Health Check** | `https://monitorx-backend.onrender.com/actuator/health` | Service status |

---

## ⚡ Important: Render Free Tier Behavior

### Spin Down After Inactivity

**What happens:**
- Backend spins down after **15 minutes** of no requests
- First request after spin-down takes **30-60 seconds** to wake up
- Subsequent requests are instant

**Solution for Important Demos:**

Use a free uptime monitoring service to ping your backend every 10 minutes:

**Option 1: UptimeRobot** (Free)
1. Sign up at [uptimerobot.com](https://uptimerobot.com)
2. Add monitor:
   - Type: HTTP(S)
   - URL: `https://monitorx-backend.onrender.com/actuator/health`
   - Interval: 5 minutes
3. This keeps your backend awake!

**Option 2: Cron-Job.org** (Free)
1. Sign up at [cron-job.org](https://cron-job.org)
2. Create job:
   - URL: `https://monitorx-backend.onrender.com/actuator/health`
   - Interval: Every 10 minutes

---

## 🔄 Automatic Deployments

### Frontend (Vercel)
- **Auto-deploys** on every push to `main` branch
- Preview deployments for pull requests
- Instant rollback capability

### Backend (Render)
- **Auto-deploys** on every push to `main` branch
- Zero-downtime deployments
- Automatic health checks

**To disable auto-deploy:**
- Vercel: Project Settings → Git → Disable
- Render: Service Settings → Auto-Deploy → Toggle off

---

## 📊 Monitoring Your Free Deployment

### Vercel Analytics (Free)

1. Go to your Vercel project
2. Click "Analytics" tab
3. View:
   - Page views
   - Unique visitors
   - Top pages
   - Performance metrics

### Render Metrics (Free)

1. Go to your Render service
2. Click "Metrics" tab
3. View:
   - Request count
   - Response times
   - Error rates
   - Memory usage

### MongoDB Atlas Monitoring (Free)

1. Go to MongoDB Atlas
2. Select your cluster
3. Click "Metrics" tab
4. View:
   - Connection count
   - Operations per second
   - Storage usage
   - Network traffic

---

## 🐛 Troubleshooting Free Deployment

### Frontend Build Fails on Vercel

**Error:** "Module not found" or "Cannot find package"

**Solution:**
```bash
# Ensure package.json is in frontend directory
cd frontend
npm install
git add package.json package-lock.json
git commit -m "Update dependencies"
git push
```

### Backend Build Fails on Render

**Error:** "Dockerfile not found"

**Solution:**
- Verify "Root Directory" is set to `backend`
- Check Dockerfile exists at `backend/Dockerfile`

**Error:** "MongoDB connection failed"

**Solution:**
- Verify MongoDB Atlas connection strings are correct
- Check password doesn't contain special characters (escape if needed)
- Ensure IP whitelist includes 0.0.0.0/0

### Backend Takes Forever to Respond

**Cause:** Backend spun down (free tier limitation)

**Solution:**
- Wait 30-60 seconds for first request
- Set up UptimeRobot to ping every 10 minutes
- Or upgrade to Render paid plan ($7/mo for always-on)

### CORS Errors in Browser Console

**Error:** "Access to fetch at ... has been blocked by CORS policy"

**Solution:**
1. Go to Render → Environment Variables
2. Update `CORS_ORIGINS` to exact Vercel URL
3. Include `https://` and no trailing slash
4. Example: `https://monitorx-frontend.vercel.app`

---

## 💡 Optimization Tips for Free Tier

### 1. Reduce Backend Cold Start Time

Add to `backend/build.gradle.kts`:
```kotlin
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    launchScript {
        properties["mode"] = "service"
    }
}
```

### 2. Optimize Frontend Build

Already configured in `next.config.js`:
```javascript
{
  output: 'standalone',  // Smaller deployment size
  compress: true,        // Enable compression
  telemetry: {
    disabled: true       // Faster builds
  }
}
```

### 3. Minimize MongoDB Storage

**Set TTL on logs** (auto-delete old logs):

Add to backend `ApiLog` model:
```kotlin
@Indexed(expireAfterSeconds = 2592000) // 30 days
var timestamp: LocalDateTime
```

### 4. Cache Static Assets

Vercel automatically caches static assets with optimal headers.

---

## 🚀 Upgrade Path

When you outgrow free tier:

| Service | Paid Plan | Cost | Benefits |
|---------|-----------|------|----------|
| **Vercel** | Pro | $20/mo | Team features, analytics |
| **Render** | Starter | $7/mo | No spin-down, more resources |
| **MongoDB Atlas** | M2 | $9/mo | 2GB storage, backups |
| **Total** | | **$36/mo** | Production-ready |

---

## 📝 Deployment Checklist

### Pre-Deployment
- [x] Code pushed to GitHub
- [x] `.env` removed from repository (use .gitignore)
- [x] `render.yaml` created (optional but recommended)

### MongoDB Atlas
- [ ] Account created
- [ ] Free M0 cluster created
- [ ] Database user created
- [ ] IP whitelist configured (0.0.0.0/0)
- [ ] Connection strings obtained

### Render (Backend)
- [ ] Account created with GitHub
- [ ] Web service created
- [ ] Environment variables configured
- [ ] First deployment successful
- [ ] Health check passing

### Vercel (Frontend)
- [ ] Account created with GitHub
- [ ] Project imported
- [ ] Root directory set to `frontend`
- [ ] Environment variable configured
- [ ] First deployment successful
- [ ] Site accessible

### Post-Deployment
- [ ] CORS updated with frontend URL
- [ ] Admin user created
- [ ] Login tested
- [ ] Dashboard accessible
- [ ] UptimeRobot configured (optional)
- [ ] Custom domain configured (optional)

---

## 🎉 Congratulations!

You've successfully deployed MonitorX to production **completely free**!

**Your Production Stack:**
- ✅ Frontend on Vercel (global CDN)
- ✅ Backend on Render (containerized)
- ✅ Database on MongoDB Atlas (managed)
- ✅ SSL/HTTPS on all services
- ✅ Auto-deployments on push
- ✅ $0/month cost

**Share your deployment:** 
`https://monitorx-frontend.vercel.app`

---

## 📚 Additional Resources

- [Render Documentation](https://render.com/docs)
- [Vercel Documentation](https://vercel.com/docs)
- [MongoDB Atlas Tutorial](https://www.mongodb.com/docs/atlas/getting-started/)
- [Next.js Deployment](https://nextjs.org/docs/deployment)
- [Spring Boot on Render](https://render.com/docs/deploy-spring-boot)

---

**Need help?** Check the main DEPLOYMENT.md for detailed troubleshooting!
