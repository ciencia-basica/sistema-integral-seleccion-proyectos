#!/bin/bash

# Dependencies: screen
# run after running successfully setup-server.sh

# expected environment variables
# SISP_PYVENV="python virtualenv dirname"
# SISP_SCREEN_NAME="name of the screen session running the server"

cd "$(dirname "${BASH_SOURCE[0]}")../" || exit 1

screen -S "$SISP_SCREEN_NAME" -dm bash -c "
    set -e
    virtualenv \"$SISP_PYVENV\"
    python3 src/main.py
"

screen -r "$SISP_SCREEN_NAME"
