#!/bin/bash

# Dependencies: git, python, nginx, certbot

# expected environment variables
# SISP_DOMAIN="domain for the api"
# SISP_PYVENV="python virtualenv dirname"
# SISP_ADMIN_EMAIL="admin email for certbot"

# exit on err
set -e

cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

./install-dependencies.sh

# nginx and https config
sudo nginx -t
sudo systemctl restart nginx
sudo cp cors_support /etc/nginx/conf.d/

# http and https traffic must be enabled
sudo certbot --nginx \
    -d "$SISP_DOMAIN" \
    --non-interactive \
    --agree-tos \
    --email "$SISP_ADMIN_EMAIL"

# cp nginx.conf into the corrsesponding file
# /etc/nginx/sites-available/default

# python environment setup

cd .. || exit 1
virtualenv "$SISP_PYVENV"
source "$SISP_PYVENV/bin/activate"
pip install -r requirements.txt
