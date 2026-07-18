#!/usr/bin/env bash
# fillsa-vm(OCI A1, Ubuntu) 최초 부트스트랩 스크립트 — docs/deploy-plan.md §6
# VM 재생성 시 이 스크립트 1회 실행으로 배포 가능한 상태를 복원한다. (멱등)
#
# 실행 후 수동 확인 사항:
#   1. Cloudflare DNS: api.fillsa.com / admin-api.fillsa.com A 레코드 → 새 공인 IP (Proxied)
#   2. OCI VCN Security List: 80/443 인그레스 허용
#   3. GitHub Actions 재실행(또는 main 재푸시)으로 컨테이너 배포
set -euo pipefail

echo "== [1/5] Docker 설치 =="
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sudo sh
  sudo usermod -aG docker "$USER"
  echo "  docker 설치됨 (그룹 반영은 재로그인 필요 — 이 스크립트는 sudo 로 계속 진행)"
else
  echo "  이미 설치됨: $(docker --version)"
fi

echo "== [2/5] 스왑 2G (6GB RAM + JVM 2개 OOM 방어, §3-E1) =="
if ! sudo swapon --show | grep -q '/swapfile'; then
  sudo fallocate -l 2G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  echo "  스왑 2G 활성화"
else
  echo "  이미 활성화됨"
fi

echo "== [3/5] 호스트 방화벽(iptables) 80/443 오픈 — OCI Ubuntu 기본 REJECT 규칙 대응 =="
for PORT in 80 443; do
  if ! sudo iptables -C INPUT -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null; then
    # OCI 기본 이미지의 REJECT 규칙보다 앞(5번째)에 삽입
    sudo iptables -I INPUT 5 -p tcp --dport "$PORT" -m state --state NEW -j ACCEPT
    echo "  $PORT 오픈"
  else
    echo "  $PORT 이미 오픈"
  fi
done
# 재부팅 후에도 유지
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -q netfilter-persistent iptables-persistent >/dev/null 2>&1 || true
sudo netfilter-persistent save >/dev/null 2>&1 || echo "  (iptables 영속화 실패 — 재부팅 시 재실행 필요)"

echo "== [4/5] Docker 네트워크 + Redis =="
sudo docker network create fillsa-net 2>/dev/null || echo "  fillsa-net 이미 존재"
if ! sudo docker ps --format '{{.Names}}' | grep -q '^fillsa-redis$'; then
  sudo docker rm -f fillsa-redis 2>/dev/null || true
  sudo docker run -d --name fillsa-redis --network fillsa-net --restart unless-stopped redis:7-alpine
  echo "  fillsa-redis 기동"
else
  echo "  fillsa-redis 이미 실행 중"
fi

echo "== [5/5] 배포 디렉터리 =="
mkdir -p ~/app/config-app ~/app/config-admin ~/app/logs-app ~/app/logs-admin ~/app/nginx
echo "  ~/app/{config-app,config-admin,logs-app,logs-admin,nginx} 준비"

echo
echo "✅ 부트스트랩 완료. 위 '수동 확인 사항' 1~3 을 진행하세요."
