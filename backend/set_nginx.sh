#! /bin/bash


sudo cp cert.pem /etc/ssl/algoritmos-geneticos.crt
sudo cp key.pem /etc/ssl/algoritmos-geneticos.key
sudo cp nginx.conf /etc/nginx/sites-enabled/fastapi_nginx
sudo service nginx restart
