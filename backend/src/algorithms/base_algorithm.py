"""
Este modulo contiene la clase base para la representacion de un algoritmo de ejecucion
"""

from abc import ABC, abstractmethod
from os import path
from uuid import UUID

from fastapi import HTTPException
from paths import ENCODING, EXEC_FILES_DIR, get_user_path


class ExecAlgorithm(ABC):
    """Clase base de los diferentes algoritmos de ejecucion"""

    config_dir: str
    output_dir: str
    exec_param: int

    @classmethod
    def _get_output_dir(cls, user_id: UUID) -> str:
        return path.join(
            get_user_path(user_id), EXEC_FILES_DIR, cls.config_dir, cls.output_dir
        )

    @classmethod
    def get_output(cls, user_id: UUID, output_type: str) -> str:
        """Obtiene los resultados del algoritmo correspondiente"""
        try:
            output_path = path.join(
                cls._get_output_dir(user_id), cls._get_output_path(output_type)
            )
            with open(output_path, "r", encoding=ENCODING) as file:
                return file.read()
        except FileNotFoundError as exc:
            error_msg = f"resultados del algoritmo '{cls.config_dir}'\
                          '{output_type}' no encontrado"
            raise HTTPException(status_code=404, detail=error_msg) from exc

    @classmethod
    @abstractmethod
    def get_outputs(cls, user_id: UUID) -> list[str]:
        """Obtiene los nombres de los tipos de resultados"""

    @classmethod
    @abstractmethod
    def _get_output_path(cls, output_type: str) -> str:
        """Obtiene la direction del archivo de resultados"""
