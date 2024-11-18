#!/bin/bash

# snap and git are assumed to be installed
# installs python, certbot, nginx, java

sudo apt update && sudo apt upgrade -y

if ! python3 &> /dev/null || ! pip3 &> /dev/null ; then
    echo "installing python..."
    sudo apt install python3
    sudo apt install python3-pip
fi

if ! nginx &> /dev/null; then
    echo "installing nginx..."
    sudo apt install nginx
    sudo systemctl enable nginx
fi

if ! certbot &> /dev/null; then
    echo "installing certbot..."
    sudo snap install --classic certbot
    sudo ln -s /snap/bin/certbot /usr/bin/certbot
fi

if ! java &> /dev/null; then
    echo "installing java..."
    sudo apt install openjdk-21-jre-headless
fi
