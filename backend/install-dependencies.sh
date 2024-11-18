#!/bin/bash

# snap and git are assumed to be installed
# installs python, certbot, nginx

sudo apt-get update && sudo apt-get upgrade -y

if ! python3 &> /dev/null || ! pip3 &> /dev/null ; then
    echo "installing python..."
    sudo apt-get install python3
    sudo apt-get install python3-pip
fi

if ! nginx &> /dev/null; then
    echo "installing nginx..."
    sudo apt-get install nginx
    sudo systemctl enable nginx
fi

if ! certbot &> /dev/null; then
    echo "installing certbot..."
    sudo snap install --classic certbot
    sudo ln -s /snap/bin/certbot /usr/bin/certbot
fi
