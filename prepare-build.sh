#!/bin/bash
set -e

webdav_url="https://webdav.hwangdo.kr/algohub-server/"
resources_path="./src/main/resources"


# 환경 변수 읽기
is_empty_or_null() {
  [ -z "$1" ] || [ "$1" = "null" ]
}

if is_empty_or_null "$CREDENTIAL_NAME" || is_empty_or_null "$CREDENTIAL_PW"; then
  echo "필요한 환경변수(CREDENTIAL_NAME, CREDENTIAL_PW) 중 하나 이상이 설정되어 있지 않거나 null입니다."
  exit 1
fi
download_yml() {
  local file_name=$1
  echo "Downloading ${file_name}"
  curl -Ss -f -X GET "${webdav_url}/${file_name}"  --user "${CREDENTIAL_NAME}:${CREDENTIAL_PW}" -o "${resources_path}/${file_name}" &
}

download_yml "aws-dev.yml"
download_yml "aws-prod.yml"
download_yml "jwt.yml"
download_yml "mysql-dev.yml"
download_yml "mysql-prod.yml"
download_yml "webhook-prod.yml"
download_yml "webhook-rc.yml"
download_yml "github.yml"
download_yml "smtp.yml"

wait
