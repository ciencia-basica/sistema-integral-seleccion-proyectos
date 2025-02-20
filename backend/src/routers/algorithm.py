"""
Contiene los endpoint relacionados a la interfaz de algoritmos
"""

from uuid import UUID

from algorithms.algorithms import get_algorithm
from algorithms.base_algorithm import ExecAlgorithm
from algorithms.config_files import get_config_type
from algorithms.execute import run_executable
from fastapi import APIRouter, Depends
from user.user_auth import get_user_id

router = APIRouter(
    # prefix="/algorithm",
    tags=["algorithm"],
)


@router.get("/config/{user}/{algorithm}/{config_type}")
async def get_config(
    config_type: str,
    user: UUID = Depends(get_user_id),
    algorithm: type[ExecAlgorithm] = Depends(get_algorithm),
) -> dict[str, str]:
    """Retorna la information actual del archivo de configuracion solicitado"""
    config = get_config_type(config_type).load_file(algorithm, user)
    return {"config": config}


@router.post("/config/{user}/{algorithm}/{config_type}")
async def modify_config(
    config_type: str,
    config_data: str,
    user: UUID = Depends(get_user_id),
    algorithm: type[ExecAlgorithm] = Depends(get_algorithm),
) -> dict[str, str]:
    """Guarda la configuracion en su archivo correspondiente"""
    get_config_type(config_type).save_file(algorithm, user, config_data)
    return {"response": f"Configuracion ({config_type}) guardada"}


@router.get("/outputs/{user}/{algorithm}")
async def get_outputs(
    user: UUID = Depends(get_user_id),
    algorithm: type[ExecAlgorithm] = Depends(get_algorithm),
) -> dict[str, list[str]]:
    """Obtiene los nombres de resultados de los algoritmos"""
    outputs = algorithm.get_outputs(user)
    return {"outputs": outputs}


@router.get("/output/{user}/{algorithm}/{output_type}")
async def get_output(
    output_type: str,
    user: UUID = Depends(get_user_id),
    algorithm: type[ExecAlgorithm] = Depends(get_algorithm),
) -> dict[str, str]:
    """Retorna la matriz de credibilidad del indice especificado"""
    output = algorithm.get_output(user, output_type)
    return {"output": output}


@router.post("/execute/{user}/{algorithm}")
async def execute(
    user: UUID = Depends(get_user_id),
    algorithm: type[ExecAlgorithm] = Depends(get_algorithm),
) -> dict[str, str]:
    """Ejecuta el archivo .jar calculando las matrices de credibilidad"""
    result = run_executable(algorithm, user)
    return {"response": result}
