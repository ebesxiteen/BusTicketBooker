# Deploy va Van Hanh Production

Tai lieu nay danh cho viec chay TicketBooker tren VPS/Linux bang Docker Compose, dat sau reverse proxy HTTPS nhu Nginx/Caddy/Traefik.

## 1. Chuan bi server

- Ubuntu 22.04/24.04 hoac distro Linux tuong duong.
- Docker Engine va Docker Compose plugin.
- Domain da tro DNS ve server, vi du `https://greenbus.example.com`.
- Firewall chi mo `80`, `443`, va SSH. Khong expose MySQL ra Internet.

## 2. Cau hinh bien moi truong

```bash
cp .env.production.example .env
chmod 600 .env
```

Sua `.env`:

- `APP_BASE_URL=https://domain-production-cua-ban`
- `DB_PASSWORD` phai la mat khau manh.
- Dien day du OAuth, VNPay, ZaloPay, SMTP, OpenAI secrets.

Can cau hinh callback tren cac cong dich vu ngoai:

- Google: `${APP_BASE_URL}/login/oauth2/code/google`
- Facebook: `${APP_BASE_URL}/login/oauth2/code/facebook`
- GitHub: `${APP_BASE_URL}/login/oauth2/code/github`
- VNPay: `${APP_BASE_URL}/vnpay/return`
- ZaloPay redirect: `${APP_BASE_URL}/greenbus/thankyou?paymentStatus=1`

## 3. Khoi chay production

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env up -d --build
```

Kiem tra:

```bash
docker compose --env-file .env ps
curl -fsS http://127.0.0.1:8000/actuator/health
docker compose --env-file .env logs -f app
```

## 4. Reverse proxy HTTPS

Nen de app bind noi bo `127.0.0.1:8000`, sau do proxy HTTPS vao app.

Nginx mau:

```nginx
server {
    listen 80;
    server_name greenbus.example.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name greenbus.example.com;

    ssl_certificate /etc/letsencrypt/live/greenbus.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/greenbus.example.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Port 443;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 5. Backup va restore MySQL

Backup:

```bash
mkdir -p backups
docker compose --env-file .env exec mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers ticketbooker' > backups/ticketbooker-$(date +%F-%H%M).sql
```

Restore:

```bash
docker compose --env-file .env exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ticketbooker' < backups/file-can-restore.sql
```

Nen cron backup toi thieu moi ngay va day file backup ra object storage/offsite.

## 6. Cap nhat phien ban

```bash
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env build app
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env up -d app
docker compose --env-file .env logs -f app
```

Sau cap nhat:

```bash
curl -fsS http://127.0.0.1:8000/actuator/health
```

## 7. Giam sat van hanh

Endpoint san co:

- `/actuator/health`
- `/actuator/prometheus`

Can theo doi:

- CPU/RAM container app va MySQL.
- Dung luong volume `mysql-data`.
- Log loi cua app: `docker compose logs app`.
- Trang thai thanh toan VNPay/ZaloPay callback.
- Backup co tao thanh cong va restore test duoc hay khong.

## 8. Luu y production

- Khong commit `.env`, `application-local.properties`, file backup SQL.
- Khong expose port MySQL ra public.
- Dung HTTPS bat buoc cho OAuth va payment callback.
- SQL init trong `docker-entrypoint-initdb.d` chi chay khi volume MySQL moi. Neu database da ton tai, thay doi schema phai chay migration/ALTER rieng.
- Truoc khi release nen chay:

```bash
mvn verify
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env config
```
