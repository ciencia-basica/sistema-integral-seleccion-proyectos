# Dependencies: git, python, nginx, certbot

# exit on any error
set -e

sudo apt update && sudo apt upgrade -y

# environment variables
export SISP_DOMAIN="https://decisionesanaliticas.com/"
export SISP_PYVENV="venv"

cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

# python environment setup
virtualenv "$SISP_PYVENV"
source "$SISP_PYVENV/bin/activate"
pip install -r requirements.txt

# nginx and https config
NGINX_CONF="/etc/nginx/conf.d/$SISP_DOMAIN.conf"

sudo cp ./nginx.conf "$NGINX_CONF"

sudo nginx -t
sudo systemctl restart nginx

sudo certbot --nginx -d $SISP_DOMAIN -d www.$SISP_DOMAIN --non-interactive --agree-tos --email admin@$SISP_DOMAIN
