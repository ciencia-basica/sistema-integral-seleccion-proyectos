"""
Modulo que continue la funcionalidad para correr el ejecutable
"""

from subprocess import CalledProcessError, TimeoutExpired, run
from uuid import UUID

from algorithms.base_algorithm import ExecAlgorithm
from fastapi import HTTPException
from paths import get_user_path

COMMAND = "java -jar executable.jar"


def run_executable(algorithm: type[ExecAlgorithm], user_id: UUID) -> str:
    """Ejecuta el algoritmo"""
    try:
        command = f"{COMMAND} {algorithm.exec_param}"
        user_path = get_user_path(user_id)
        result = run(
            command,
            cwd=user_path,
            shell=True,
            capture_output=True,
            text=True,
            check=True,
        )
        return result.stdout
    except CalledProcessError as exc:
        raise HTTPException(status_code=500, detail=exc.stderr) from exc
    except TimeoutExpired as exc:
        error_msg = "timeout en la ejecucion del algoritmo"
        raise HTTPException(status_code=500, detail=error_msg) from exc
