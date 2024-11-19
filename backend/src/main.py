"""
Backend para el prototipo web del proyecto de servicio
becario: Toma de Desciciones con Algoritmos Geneticos

Juan Sebastian Gonzalez A01644942

Commando de ejecucion:
# dev
uvicorn main:app --reload
# Production
python3 main.py
"""

import logging
import traceback
from typing import Any, Awaitable, Callable

import uvicorn
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from paths import ERRORS_LOG
from routers import algorithm, user

# API

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)


@app.middleware("http")
async def exception_middleware(
    request: Request, call_next: Callable[[Request], Awaitable[Any]]
):
    """TODO"""
    try:
        return await call_next(request)
    except Exception as e:  # pylint: disable=broad-except
        exc_traceback = traceback.format_exception(type(e), value=e, tb=e.__traceback__)
        logging.error("%s\n - END OF EXCEPT -", "\n".join(exc_traceback))
        return JSONResponse(content=str(e), status_code=500)


app.include_router(user.router)
app.include_router(algorithm.router)


@app.get("/")
async def root() -> dict[str, str]:
    """Root de la api"""
    return {"response": "root"}


# Logging

logging.basicConfig(
    level=logging.ERROR,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[
        logging.FileHandler(ERRORS_LOG),
    ],
)

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)
