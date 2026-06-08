#!/usr/bin/env bash
set -e

host="$1"
shift
port="$1"
shift

until nc -z "$host" "$port"; do
  sleep 2
done

exec "$@"