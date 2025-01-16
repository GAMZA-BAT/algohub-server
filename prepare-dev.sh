#!/bin/bash

set -e

api_endpoint="https://api.gamzabat.store"
is_empty_or_null() {
  [ -z "$1" ] || [ "$1" = "null" ]
}

if is_empty_or_null "$DEV_NAME"  || is_empty_or_null "$CREDENTIAL_PW"; then
  echo "필요한 환경 변수 (DEV_NAME, CREDENTIAL_PW)중 하나 이상이 설정되어 있지 않습니다."
  exit 1
fi

DB_NAME="algohub_${DEV_NAME}"
BUCKET_NAME="algohub-${DEV_NAME}"

curl -Ss -f -X POST "${api_endpoint}/create-database?dbName=${DB_NAME}&credential=${CREDENTIAL_PW}"
curl -Ss -f -X POST "${api_endpoint}/create-bucket?bucketName=${BUCKET_NAME}&credential=${CREDENTIAL_PW}"

