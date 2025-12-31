# MonitorX Deployment Guide

This guide will help you deploy MonitorX using Docker Compose.

## 📋 Prerequisites

- **Docker** 20.10+ and **Docker Compose** 2.0+
- **Git** (to clone the repository)
- At least **2GB RAM** available
- **Ports** 3000, 8080, 27017, 27018 available

## 🚀 Quick Start Deployment

### 1. Clone the Repository

```bash
git clone <repository-url>
cd MonitorX
```

### 2. Configure Environment Variables

The project includes a `.env.example` file with default values. For production deployment:

```bash
# Copy the example file
cp .env.example .env

# Edit the .env file and update these critical values:
# - JWT_SECRET: Use a strong random string (at least 32 characters)
# - CORS_ORIGINS: Add your production domain if deploying remotely
```

**Important Environment Variables:**

| Variable | Description | Default | Production Value |
|----------|-------------|---------|------------------|
| `JWT_SECRET` | Secret key for JWT token signing | `monitorx-super-secret-key...` | **Change to strong random string** |
| `JWT_EXPIRATION` | Token expiration time (ms) | `86400000` (24 hours) | Adjust as needed |
| `CORS_ORIGINS` | Allowed frontend origins | `http://localhost:3000` | Add production URL |
| `NEXT_PUBLIC_API_URL` | Backend API URL for frontend | `http://localhost:8080` | Update for production |

### 3. Build and Start All Services

```bash
# Build and start all containers
docker-compose up --build -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps
```

This will start:
- 🗄️ **MongoDB (Logs DB)** on port 27017
- 🗄️ **MongoDB (Metadata DB)** on port 27018
- 🔧 **Backend API** on port 8080
- 🌐 **Frontend Dashboard** on port 3000

### 4. Verify Deployment

Wait 30-60 seconds for all services to start, then check:

```bash
# Check backend health
curl http://localhost:8080/actuator/health

# Check frontend
curl http://localhost:3000

# View all running containers
docker-compose ps
```

### 5. Access the Application

- **Frontend Dashboard**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html

### 6. Create Your First User

```powershell
# Using PowerShell
$signup = @{
    username = "admin"
    email = "admin@monitorx.com"
    password = "Admin@123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/signup" `
    -Method POST `
    -Body $signup `
    -ContentType "application/json"
```

Or using `curl`:

```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@monitorx.com",
    "password": "Admin@123"
  }'
```

### 7. Login to Dashboard

1. Navigate to http://localhost:3000
2. Click "Login"
3. Use credentials: `admin@monitorx.com` / `Admin@123`

---

## 🔧 Management Commands

### Stop Services

```bash
# Stop all containers (keeps data)
docker-compose stop

# Stop and remove containers (keeps data volumes)
docker-compose down

# Stop and remove everything including data
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f frontend-dashboard
docker-compose logs -f collector-service
docker-compose logs -f mongo-logs
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart frontend-dashboard
```

### Rebuild After Code Changes

```bash
# Rebuild and restart
docker-compose up --build -d

# Rebuild specific service
docker-compose up --build -d frontend-dashboard
```

---

## 🌍 Production Deployment

### For VPS/Cloud Server Deployment

1. **Set up DNS and Domain**
   - Point your domain to server IP
   - Configure A records for frontend and backend

2. **Update Environment Variables**
   ```bash
   # In .env file
   JWT_SECRET=<generate-strong-secret-key>
   CORS_ORIGINS=https://your-domain.com
   NEXT_PUBLIC_API_URL=https://api.your-domain.com
   ```

3. **Set up Reverse Proxy (Nginx)**

   Create `/etc/nginx/sites-available/monitorx`:
   
   ```nginx
   # Frontend
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           proxy_pass http://localhost:3000;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   
   # Backend
   server {
       listen 80;
       server_name api.your-domain.com;
       
       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

4. **Enable SSL with Let's Encrypt**
   ```bash
   sudo apt install certbot python3-certbot-nginx
   sudo certbot --nginx -d your-domain.com -d api.your-domain.com
   ```

5. **Configure Firewall**
   ```bash
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw allow 22/tcp
   sudo ufw enable
   ```

### Using External MongoDB (MongoDB Atlas)

Update docker-compose.yml to remove MongoDB containers and set URIs:

```yaml
collector-service:
  environment:
    - LOGS_DB_URI=mongodb+srv://<username>:<password>@cluster.mongodb.net/logs_db
    - METADATA_DB_URI=mongodb+srv://<username>:<password>@cluster.mongodb.net/metadata_db
```

---

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Check what's using the port
netstat -ano | findstr :3000

# Kill the process or change the port in docker-compose.yml
ports:
  - "3001:3000"  # Map to different host port
```

### Container Fails to Start

```bash
# Check logs
docker-compose logs <service-name>

# Rebuild from scratch
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### Cannot Connect to Backend from Frontend

1. Check backend is healthy: `curl http://localhost:8080/actuator/health`
2. Verify CORS settings include frontend URL
3. Check browser console for errors
4. Verify `NEXT_PUBLIC_API_URL` is correct

### Database Connection Issues

```bash
# Test MongoDB connections
docker exec -it monitorx-mongo-logs mongosh --eval "db.adminCommand('ping')"
docker exec -it monitorx-mongo-metadata mongosh --eval "db.adminCommand('ping')"

# Check MongoDB logs
docker-compose logs mongo-logs
docker-compose logs mongo-metadata
```

### Out of Disk Space

```bash
# Clean up unused Docker resources
docker system prune -a

# Remove old images
docker image prune -a

# Check disk usage
docker system df
```

---

## 📊 Monitoring

### Health Checks

All services have health checks configured. Check status:

```bash
docker-compose ps
```

Look for "healthy" status.

### Resource Usage

```bash
# Container stats
docker stats

# Specific service
docker stats monitorx-frontend-dashboard
```

---

## 🔄 Updates and Maintenance

### Updating the Application

```bash
# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose up --build -d

# Check logs for errors
docker-compose logs -f
```

### Backup MongoDB Data

```bash
# Backup logs database
docker exec monitorx-mongo-logs mongodump --out=/data/backup

# Copy backup to host
docker cp monitorx-mongo-logs:/data/backup ./backups/logs_$(date +%Y%m%d)

# Backup metadata database
docker exec monitorx-mongo-metadata mongodump --out=/data/backup
docker cp monitorx-mongo-metadata:/data/backup ./backups/metadata_$(date +%Y%m%d)
```

### Restore from Backup

```bash
# Restore logs database
docker cp ./backups/logs_20240101 monitorx-mongo-logs:/data/restore
docker exec monitorx-mongo-logs mongorestore /data/restore

# Restore metadata database
docker cp ./backups/metadata_20240101 monitorx-mongo-metadata:/data/restore
docker exec monitorx-mongo-metadata mongorestore /data/restore
```

---

## 📝 Environment Variables Reference

### Backend Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `LOGS_DB_URI` | Yes | mongodb://mongo-logs:27017/logs_db | MongoDB URI for logs |
| `METADATA_DB_URI` | Yes | mongodb://mongo-metadata:27017/metadata_db | MongoDB URI for metadata |
| `JWT_SECRET` | Yes | (default provided) | Secret for JWT signing |
| `JWT_EXPIRATION` | No | 86400000 | Token expiration (milliseconds) |
| `SERVER_PORT` | No | 8080 | Backend server port |
| `CORS_ORIGINS` | Yes | http://localhost:3000 | Comma-separated allowed origins |

### Frontend Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `NEXT_PUBLIC_API_URL` | Yes | http://localhost:8080 | Backend API URL |
| `NODE_ENV` | No | production | Node environment |

---

## 🎯 Next Steps

1. ✅ Deploy the application
2. ✅ Create admin user
3. ✅ Configure your services to send logs to MonitorX
4. 📊 Monitor your APIs in the dashboard
5. 🔔 Set up alerting rules
6. 📈 Analyze performance metrics

## 🆘 Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review logs: `docker-compose logs -f`
3. Verify environment variables are set correctly
4. Check Docker and system resources

---

**MonitorX** - Premium API Monitoring & Observability Platform
