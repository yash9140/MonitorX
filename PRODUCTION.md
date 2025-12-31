# MonitorX Production Deployment Guide

This guide provides step-by-step instructions for deploying MonitorX to production environments.

---

## 🎯 Deployment Options Overview

| Option | Cost | Complexity | Best For |
|--------|------|------------|----------|
| **VPS (Docker Compose)** | $12-20/mo | Medium | Full control, all-in-one |
| **Vercel + Railway** | $5-15/mo | Low | Fastest deployment |
| **AWS ECS/Fargate** | $20-50/mo | High | Enterprise, scalability |
| **DigitalOcean App Platform** | $15-30/mo | Low | Managed containers |

---

## 🚀 Recommended: VPS Deployment (Docker Compose)

**Best for:** Small to medium teams, cost-effective, full control

**Providers:** DigitalOcean, Linode, Vultr, AWS EC2

**Cost:** ~$12/month for 2GB RAM droplet

### Prerequisites

1. **Domain name** (e.g., `monitorx.yourdomain.com`)
2. **VPS server** with Ubuntu 22.04 LTS
3. **SSH access** to server
4. **Basic Linux knowledge**

---

### Step 1: Server Setup

#### 1.1 Create VPS

**DigitalOcean Example:**
```bash
# Create a droplet via DigitalOcean dashboard:
# - OS: Ubuntu 22.04 LTS
# - Plan: Basic ($12/mo - 2GB RAM, 1 vCPU, 50GB SSD)
# - Region: Choose closest to your users
# - Authentication: SSH key (recommended)
```

#### 1.2 Initial Server Configuration

SSH into your server:
```bash
ssh root@your-server-ip
```

Create a non-root user:
```bash
# Create user
adduser monitorx
usermod -aG sudo monitorx

# Switch to new user
su - monitorx
```

Update the system:
```bash
sudo apt update && sudo apt upgrade -y
```

#### 1.3 Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker $USER

# Start Docker service
sudo systemctl enable docker
sudo systemctl start docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version
```

#### 1.4 Configure Firewall

```bash
# Allow SSH, HTTP, HTTPS
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

---

### Step 2: Deploy MonitorX

#### 2.1 Clone Repository

```bash
# Install git if needed
sudo apt install git -y

# Clone your repository
cd ~
git clone https://github.com/yourusername/MonitorX.git
cd MonitorX
```

#### 2.2 Configure Environment Variables

```bash
# Create production .env file
cp .env.example .env

# Edit with production values
nano .env
```

**Production `.env` file:**
```bash
# Generate a strong JWT secret (32+ characters)
JWT_SECRET=CHANGE_THIS_TO_A_VERY_STRONG_RANDOM_SECRET_AT_LEAST_32_CHARS_LONG
JWT_EXPIRATION=86400000
SERVER_PORT=8080

# Use container names for internal communication
LOGS_DB_URI=mongodb://mongo-logs:27017/logs_db
METADATA_DB_URI=mongodb://mongo-metadata:27017/metadata_db

# Update with your domain
CORS_ORIGINS=https://monitorx.yourdomain.com
NEXT_PUBLIC_API_URL=https://api.monitorx.yourdomain.com
```

**Generate strong JWT secret:**
```bash
# Generate random 64-character secret
openssl rand -base64 64 | tr -d '\n'
```

#### 2.3 Update Docker Compose for Production

Create `docker-compose.prod.yml`:
```bash
nano docker-compose.prod.yml
```

```yaml
version: '3.8'

services:
  mongo-logs:
    image: mongo:7.0
    container_name: monitorx-mongo-logs
    restart: always
    ports:
      - "127.0.0.1:27017:27017"  # Bind to localhost only
    environment:
      MONGO_INITDB_DATABASE: logs_db
    volumes:
      - mongo-logs-data:/data/db
    networks:
      - monitorx-network

  mongo-metadata:
    image: mongo:7.0
    container_name: monitorx-mongo-metadata
    restart: always
    ports:
      - "127.0.0.1:27018:27017"  # Bind to localhost only
    environment:
      MONGO_INITDB_DATABASE: metadata_db
    volumes:
      - mongo-metadata-data:/data/db
    networks:
      - monitorx-network

  collector-service:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: monitorx-collector-service
    restart: always
    ports:
      - "127.0.0.1:8080:8080"  # Bind to localhost only (Nginx will proxy)
    environment:
      - LOGS_DB_URI=${LOGS_DB_URI:-mongodb://mongo-logs:27017/logs_db}
      - METADATA_DB_URI=${METADATA_DB_URI:-mongodb://mongo-metadata:27017/metadata_db}
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}
      - SERVER_PORT=8080
      - CORS_ORIGINS=${CORS_ORIGINS}
    depends_on:
      - mongo-logs
      - mongo-metadata
    networks:
      - monitorx-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  frontend-dashboard:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: monitorx-frontend-dashboard
    restart: always
    ports:
      - "127.0.0.1:3000:3000"  # Bind to localhost only (Nginx will proxy)
    environment:
      - NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
      - NODE_ENV=production
    depends_on:
      - collector-service
    networks:
      - monitorx-network

volumes:
  mongo-logs-data:
    driver: local
  mongo-metadata-data:
    driver: local

networks:
  monitorx-network:
    driver: bridge
```

**Key Production Changes:**
- Port bindings to `127.0.0.1` (localhost only) for security
- `restart: always` for auto-recovery
- Environment variables loaded from `.env`

#### 2.4 Build and Start Services

```bash
# Build images
docker-compose -f docker-compose.prod.yml build

# Start services in background
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

---

### Step 3: Configure Nginx Reverse Proxy

#### 3.1 Install Nginx

```bash
sudo apt install nginx -y
```

#### 3.2 Create Nginx Configuration

```bash
sudo nano /etc/nginx/sites-available/monitorx
```

**Nginx configuration:**
```nginx
# Frontend - monitorx.yourdomain.com
server {
    listen 80;
    server_name monitorx.yourdomain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}

# Backend API - api.monitorx.yourdomain.com
server {
    listen 80;
    server_name api.monitorx.yourdomain.com;

    # Increase body size for log ingestion
    client_max_body_size 10M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 3.3 Enable Nginx Configuration

```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/monitorx /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

---

### Step 4: Configure DNS

In your domain registrar (Namecheap, GoDaddy, Cloudflare, etc.):

**Add A Records:**
```
Type: A
Host: monitorx
Value: YOUR_SERVER_IP
TTL: 3600

Type: A
Host: api.monitorx
Value: YOUR_SERVER_IP
TTL: 3600
```

**Wait for DNS propagation** (5-30 minutes):
```bash
# Check DNS resolution
nslookup monitorx.yourdomain.com
nslookup api.monitorx.yourdomain.com
```

---

### Step 5: Enable SSL with Let's Encrypt

#### 5.1 Install Certbot

```bash
sudo apt install certbot python3-certbot-nginx -y
```

#### 5.2 Obtain SSL Certificates

```bash
# Obtain certificates for both domains
sudo certbot --nginx -d monitorx.yourdomain.com -d api.monitorx.yourdomain.com

# Follow the prompts:
# - Enter email address
# - Agree to terms
# - Choose to redirect HTTP to HTTPS (option 2)
```

#### 5.3 Verify Auto-Renewal

```bash
# Test renewal process
sudo certbot renew --dry-run

# Certbot auto-renewal is set up via systemd timer
sudo systemctl status certbot.timer
```

**SSL certificates will auto-renew every 60 days!**

---

### Step 6: Create Admin User

```bash
# Create admin user via API
curl -X POST https://api.monitorx.yourdomain.com/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@yourdomain.com",
    "password": "ChangeThisStrongPassword123!"
  }'
```

---

### Step 7: Access Your Production Deployment

- **Frontend:** https://monitorx.yourdomain.com
- **Backend API:** https://api.monitorx.yourdomain.com
- **API Docs:** https://api.monitorx.yourdomain.com/swagger-ui.html

🎉 **MonitorX is now live in production!**

---

## 📊 Alternative: Vercel + Railway Deployment

**Best for:** Quick deployment, minimal DevOps, scalable

**Cost:** ~$5-15/month

### Frontend on Vercel

#### 1. Push Code to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/yourusername/MonitorX.git
git push -u origin main
```

#### 2. Deploy to Vercel

1. Go to [vercel.com](https://vercel.com)
2. Click "Import Project"
3. Connect GitHub and select MonitorX repository
4. **Configure Build Settings:**
   - Framework Preset: `Next.js`
   - Root Directory: `frontend`
   - Build Command: `npm run build`
   - Output Directory: `.next`

5. **Environment Variables:**
   ```
   NEXT_PUBLIC_API_URL=https://your-backend.railway.app
   ```

6. Click "Deploy"

**Your frontend will be live at:** `https://monitorx-xxx.vercel.app`

### Backend on Railway

#### 1. Connect Railway to GitHub

1. Go to [railway.app](https://railway.app)
2. Click "New Project" → "Deploy from GitHub repo"
3. Select MonitorX repository
4. Railway will detect the backend Dockerfile

#### 2. Configure Backend Service

**Environment Variables:**
```
LOGS_DB_URI=mongodb://mongo-logs:27017/logs_db
METADATA_DB_URI=mongodb://mongo-metadata:27017/metadata_db
JWT_SECRET=your-strong-secret-here
JWT_EXPIRATION=86400000
SERVER_PORT=8080
CORS_ORIGINS=https://monitorx-xxx.vercel.app
```

**Root Directory:** `backend`

#### 3. Add MongoDB Databases

In Railway:
1. Click "New" → "Database" → "MongoDB"
2. Create two separate MongoDB instances (logs_db, metadata_db)
3. Update environment variables with connection strings

Railway will automatically:
- Build your Docker image
- Deploy the backend
- Provide a public URL: `https://your-backend.railway.app`

#### 4. Update Vercel Environment

Go back to Vercel and update:
```
NEXT_PUBLIC_API_URL=https://your-backend.railway.app
```

Redeploy the frontend.

---

## 🔒 Production Security Checklist

### Essential Security

- [x] **Change JWT_SECRET** to strong random value (64+ chars)
- [x] **Enable HTTPS** for all services (Let's Encrypt or Cloudflare)
- [x] **Restrict CORS** to production domains only
- [x] **Firewall** configured (allow only 80, 443, 22)
- [x] **MongoDB** not exposed to internet (localhost binding)
- [ ] **Rate limiting** on API endpoints
- [ ] **DDoS protection** (Cloudflare proxy)
- [ ] **Regular backups** automated
- [ ] **Security headers** in Nginx
- [ ] **Log aggregation** for monitoring

### Nginx Security Headers

Add to your Nginx config:
```nginx
# Security headers
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;
add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
```

---

## 📈 Monitoring & Maintenance

### Set Up Monitoring

**UptimeRobot (Free):**
1. Go to [uptimerobot.com](https://uptimerobot.com)
2. Add monitors for:
   - Frontend: `https://monitorx.yourdomain.com`
   - Backend Health: `https://api.monitorx.yourdomain.com/actuator/health`
3. Configure email alerts

### Database Backups

**Automated MongoDB Backup Script:**
```bash
# Create backup script
sudo nano /usr/local/bin/backup-monitorx.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/home/monitorx/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup logs database
docker exec monitorx-mongo-logs mongodump --out=/data/backup
docker cp monitorx-mongo-logs:/data/backup $BACKUP_DIR/logs_$DATE

# Backup metadata database
docker exec monitorx-mongo-metadata mongodump --out=/data/backup
docker cp monitorx-mongo-metadata:/data/backup $BACKUP_DIR/metadata_$DATE

# Compress backups
tar -czf $BACKUP_DIR/monitorx_backup_$DATE.tar.gz $BACKUP_DIR/*_$DATE
rm -rf $BACKUP_DIR/logs_$DATE $BACKUP_DIR/metadata_$DATE

# Keep only last 7 days of backups
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: $DATE"
```

Make executable and schedule:
```bash
sudo chmod +x /usr/local/bin/backup-monitorx.sh

# Add to crontab (daily at 2 AM)
crontab -e

# Add this line:
0 2 * * * /usr/local/bin/backup-monitorx.sh >> /home/monitorx/backup.log 2>&1
```

### Update Application

```bash
cd ~/MonitorX

# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose -f docker-compose.prod.yml up --build -d

# Check logs
docker-compose -f docker-compose.prod.yml logs -f
```

---

## 💰 Cost Breakdown

### VPS Option (Recommended)

| Item | Provider | Cost/Month |
|------|----------|------------|
| VPS (2GB RAM) | DigitalOcean | $12 |
| Domain | Namecheap | $1 |
| SSL Certificate | Let's Encrypt | Free |
| Backups (optional) | DigitalOcean | $2.40 |
| **Total** | | **$15.40** |

### Serverless Option

| Item | Provider | Cost/Month |
|------|----------|------------|
| Frontend | Vercel | Free (Hobby) |
| Backend | Railway | $5 |
| MongoDB | MongoDB Atlas | Free (512MB) |
| Domain | Namecheap | $1 |
| **Total** | | **$6** |

---

## 🚨 Troubleshooting Production Issues

### Service Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs backend

# Check if ports are in use
sudo netstat -tulpn | grep :8080

# Restart service
docker-compose -f docker-compose.prod.yml restart backend
```

### SSL Certificate Issues

```bash
# Renew manually
sudo certbot renew

# Check certificate status
sudo certbot certificates
```

### High Memory Usage

```bash
# Check container stats
docker stats

# Restart containers
docker-compose -f docker-compose.prod.yml restart
```

### Database Connection Errors

```bash
# Check MongoDB is running
docker ps | grep mongo

# Test connection
docker exec -it monitorx-mongo-logs mongosh --eval "db.adminCommand('ping')"
```

---

## ✅ Production Launch Checklist

- [ ] Server provisioned and configured
- [ ] Docker and Docker Compose installed
- [ ] Repository cloned to server
- [ ] Environment variables configured
- [ ] Services built and running
- [ ] Nginx reverse proxy configured
- [ ] DNS records created and propagated
- [ ] SSL certificates obtained and verified
- [ ] Admin user created
- [ ] Frontend accessible via HTTPS
- [ ] Backend API accessible via HTTPS
- [ ] Monitoring configured (UptimeRobot)
- [ ] Automated backups scheduled
- [ ] Firewall rules configured
- [ ] Security headers added
- [ ] Documentation updated with production URLs
- [ ] Team notified and trained

---

## 📚 Additional Resources

- [Docker Compose Production Guide](https://docs.docker.com/compose/production/)
- [Nginx Configuration Best Practices](https://www.nginx.com/blog/nginx-best-practices/)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [MongoDB Security Checklist](https://www.mongodb.com/docs/manual/administration/security-checklist/)
- [Next.js Deployment](https://nextjs.org/docs/deployment)

---

**Need help?** Review the complete DEPLOYMENT.md file for detailed troubleshooting and advanced configuration options.
