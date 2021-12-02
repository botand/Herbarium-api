#!/bin/bash
# Script to decrypt all the secrets files.

pwd
openssl version -v

# Decrypt Firebase service account file.
if [[ -n "$ENCRYPTED_FIREBASE_SERVICE_ACCOUNT_CREDENTIALS" ]]; then
  echo "Decoding Firebase service account file."
  openssl enc -aes-256-cbc -pbkdf2 -d -k "$ENCRYPTED_FIREBASE_SERVICE_ACCOUNT_CREDENTIALS" -in ./encryptedFiles/firebase_service_account_credentials.json.enc -out ./firebase_service_account_credentials.json -md md5
fi