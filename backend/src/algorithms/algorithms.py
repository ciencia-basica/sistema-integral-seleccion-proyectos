"""
Modulo para la funcionalidad de carga de matrices de credibilidad
"""

from os import listdir, path
from typing import Callable
from uuid import UUID

from algorithms.base_algorithm import ExecAlgorithm
from fastapi import HTTPException
from paths import TXT_EXT


def get_algorithm(algorithm: str) -> type[ExecAlgorithm]:
    """Obtiene el algoritmo a partir de su nombre"""
    parsed_algorithm: type[ExecAlgorithm]
    match algorithm:
        case CredibilityMatrixAlgorithm.config_dir:
            parsed_algorithm = CredibilityMatrixAlgorithm
        case SortingAlgorithm.config_dir:
            parsed_algorithm = SortingAlgorithm
        case _:
            error_msg = f"Algoritmo '{algorithm}' no encontrado"
            raise HTTPException(status_code=404, detail=error_msg)
    return parsed_algorithm


class CredibilityMatrixAlgorithm(ExecAlgorithm):
    """Representa el algoritmo de ejecucion Matrices de Credibilidad"""

    config_dir = "Calculate credibility matrix"
    output_dir = "Credibility matrices"
    exec_param = 4

    @classmethod
    def get_outputs(cls, user_id: UUID) -> list[str]:
        """Obtiene los nombres de los tipos de resultados"""
        output_dir = cls._get_output_dir(user_id)
        return [f.split("-")[0] for f in listdir(output_dir)]

    @classmethod
    def _get_output_path(cls, output_type: str) -> str:
        return f"{output_type}-CredibilityMatrix{TXT_EXT}"


class SortingAlgorithm(ExecAlgorithm):
    """Representa el algoritmo de ejecucion Calculo de Sorteo"""

    config_dir = "Calculate sorting"
    output_dir = "Sorting"
    exec_param = 5

    @classmethod
    def get_outputs(cls, user_id: UUID) -> list[str]:
        """Obtiene los nombres de los tipos de resultados"""
        output_dir = cls._get_output_dir(user_id)
        exists_out: Callable[[str], bool] = lambda dir: path.exists(
            path.join(output_dir, dir, f"Assignments{TXT_EXT}")
        )
        return [dir for dir in listdir(output_dir) if exists_out(dir)]

    @classmethod
    def _get_output_path(cls, output_type: str) -> str:
        return path.join(output_type, f"Assignments{TXT_EXT}")
