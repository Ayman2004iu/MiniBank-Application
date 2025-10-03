#!/usr/bin/env bash

set -e

host="$1"
shift
port="$1"
shift

echo "Waiting for MySQL at $host:$port ..."

until mysqladmin ping -h"$host" --silent; do
  echo "MySQL is not ready yet, retrying..."
  sleep 2
done

echo "MySQL is ready! Starting application..."
exec "$@"