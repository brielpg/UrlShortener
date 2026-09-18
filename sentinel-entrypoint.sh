#!/bin/sh
set -eu

echo "Aguardando redis-master..."

until REDIS_MASTER_IP=$(getent hosts redis-master | awk 'NR==1 {print $1}') && [ -n "$REDIS_MASTER_IP" ]; do
    sleep 3
done

echo "redis-master resolvido:"
echo "IP: $REDIS_MASTER_IP"

echo "Aguardando Redis na porta 6379..."

until redis-cli -h "$REDIS_MASTER_IP" -a "$REDIS_PASSWORD" ping 2>/dev/null | grep -q PONG; do
    sleep 3
done

echo "Redis disponível."

cat > /tmp/sentinel.conf <<EOF
port 26379
dir /tmp

sentinel monitor mymaster $REDIS_MASTER_IP 6379 1
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 10000
sentinel auth-pass mymaster $REDIS_PASSWORD
EOF

echo "Configuração do Sentinel:"
cat /tmp/sentinel.conf

echo "Iniciando Sentinel..."

exec redis-server /tmp/sentinel.conf --sentinel
