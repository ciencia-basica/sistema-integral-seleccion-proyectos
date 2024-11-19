#!/bin/bash

# Dependencies: tmux
# run after running successfully setup-server.sh

# expected environment variables
# SISP_PYVENV="python virtualenv dirname"
# SISP_SESSION_NAME="name of the tmux session running the server"

cd "$(dirname "${BASH_SOURCE[0]}")/../" || exit 1

if tmux has-session -t "$SISP_SESSION_NAME" &>/dev/null; then
    echo "Tmux session '$SISP_SESSION_NAME' already exists. Attaching..."
    tmux attach-session -t "$SISP_SESSION_NAME"
    exit 0
fi

tmux new-session -d -s "$SISP_SESSION_NAME" bash -c "
    source \"$SISP_PYVENV/bin/activate\";
    python3 src/main.py;
"

tmux attach-session -t "$SISP_SESSION_NAME"
