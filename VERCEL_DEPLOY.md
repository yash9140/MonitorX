# Deploy MonitorX Frontend to Vercel

## 🎯 Backend Deployed Successfully

**Backend URL:** https://monitorx-backend-latest.onrender.com

**Note:** If you're getting 403 errors on health check, the service might still be starting. Give it 2-3 minutes on first deploy.

---

## 🚀 Deploy Frontend to Vercel

### Step 1: Go to Vercel

1. Visit: https://vercel.com
2. Click **"Sign Up"** or **"Login"** (use GitHub account)

### Step 2: Import Repository

1. Click **"Add New..."** → **"Project"**
2. Click **"Import Git Repository"**
3. Find and select **"MonitorX"** repository
4. Click **"Import"**

### Step 3: Configure Project

**Framework Preset:** Next.js (auto-detected) ✅

**Root Directory:** Click **"Edit"** → Enter: `frontend`

**Build Command:** `npm run build` (auto-filled)

**Output Directory:** `.next` (auto-filled)

**Install Command:** `npm install` (auto-filled)

### Step 4: Environment Variables

Click **"Environment Variables"** section and add:

**Key:** `NEXT_PUBLIC_API_URL`  
**Value:** `https://monitorx-backend-latest.onrender.com`

**Important:** Don't include trailing slash!

### Step 5: Deploy

1. Click **"Deploy"**
2. Wait 2-3 minutes for build to complete
3. You'll get a URL like: `https://monitorx-xxx.vercel.app`

---

## 🔄 Update Backend CORS

Once frontend is deployed, update backend to allow frontend requests:

### Step 1: Get Your Vercel URL

After deployment, copy your Vercel URL (e.g., `https://monitorx-abc123.vercel.app`)

### Step 2: Update Render Environment Variable

1. Go to Render dashboard: https://dashboard.render.com
2. Select your **monitorx-backend-latest** service
3. Go to **"Environment"** tab
4. Find `CORS_ORIGINS` variable
5. Update value to: `https://your-vercel-url.vercel.app`
6. Click **"Save Changes"**

### Step 3: Redeploy Backend

Render will automatically redeploy with new CORS settings.

---

## ✅ Verify Full Stack

### Test Backend Health

```powershell
curl https://monitorx-backend-latest.onrender.com/actuator/health
```

Expected: `{"status":"UP"}`

### Test Frontend

1. Open: `https://your-vercel-url.vercel.app`
2. Should see MonitorX landing page
3. Click "Sign Up"
4. Create account
5. Login

### Test API Connection

1. After login, check browser console (F12)
2. Should see API calls to `https://monitorx-backend-latest.onrender.com`
3. No CORS errors

---

## 🎯 Custom Domain (Optional)

### For Frontend (Vercel):

1. Go to Vercel project → "Settings" → "Domains"
2. Add your domain (e.g., `monitorx.yourdomain.com`)
3. Configure DNS as instructed
4. SSL automatic!

### For Backend (Render):

1. Go to Render service → "Settings"
2. Add custom domain (e.g., `api.monitorx.yourdomain.com`)
3. Add CNAME record to DNS
4. SSL automatic!

Then update:
- Vercel: `NEXT_PUBLIC_API_URL=https://api.monitorx.yourdomain.com`
- Render: `CORS_ORIGINS=https://monitorx.yourdomain.com`

---

## 📋 Quick Reference

| Component | URL | Purpose |
|-----------|-----|---------|
| **Backend** | https://monitorx-backend-latest.onrender.com | API server |
| **Frontend** | https://monitorx-xxx.vercel.app | User interface |
| **API Docs** | https://monitorx-backend-latest.onrender.com/swagger-ui.html | API documentation |

---

## 🎉 You're Done!

Full stack deployed:
- ✅ Backend on Render
- ✅ Frontend on Vercel (deploying)
- ✅ MongoDB Atlas
- ✅ All free tier!

**Start monitoring your APIs!** 🚀
