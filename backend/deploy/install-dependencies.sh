#!/bin/bash

# snap and git are assumed to be installed
# install python, pip, py virtualenv, certbot, nginx, java

set -e

sudo apt update && sudo apt upgrade -y

if ! python3 --version &> /dev/null; then
    echo "installing python..."
fi

if ! pip3 --version &> /dev/null; then
    echo "installing python..."
    sudo apt install python3-pip -y
fi

if ! virtualenv --version &> /dev/null; then
    echo "installing virtualenv..."
    sudo apt install python3-virtualenv -y
fi

if ! nginx -v &> /dev/null; then
    echo "installing nginx..."
    sudo apt install nginx -y
    sudo systemctl enable nginx
fi

if ! certbot --version &> /dev/null; then
    echo "installing certbot..."
    sudo snap install --classic certbot
    if [ ! -f /usr/bin/certbot ]; then
        sudo ln -s /snap/bin/certbot /usr/bin/certbot
    fi
fi

if ! java --version &> /dev/null; then
    echo "installing java..."
    sudo apt install openjdk-21-jre-headless -y
fi
