#!/bin/bash
set -e

if [ -t 0 ]; then
  JSON="$1"
else
  JSON=$(cat)
fi

if [ -z "$JSON" ]; then
  echo "Usage:"
  echo "  ./decrypt.sh '<singleLineJson>'"
  echo "  cat document.json | ./decrypt.sh"
  exit 1
fi

ENCODED=$(echo "$JSON" | base64 -w 0)

sbt "runMain uk.gov.hmrc.overseaspensiontransferbackend.utils.LocalDecrypt $ENCODED"