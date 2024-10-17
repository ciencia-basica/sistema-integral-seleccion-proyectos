"""
Este módulo contiene la validación de formato, para cada implementación de archivo de configuración.
"""

import re

from algorithms.base_config_file import ConfigFile
from fastapi import HTTPException

# import re


def get_config_type(config_type: str) -> type[ConfigFile]:
    """Obtiene el tipo correspondiente de archivo de configuracion"""
    config: type[ConfigFile]
    match config_type:
        case AdditionalCriteriaParametersConfig.config_type:
            config = AdditionalCriteriaParametersConfig
        case CredibilityCriteriaConfig.config_type:
            config = CredibilityCriteriaConfig
        case CriteriaDirectionsConfig.config_type:
            config = CriteriaDirectionsConfig
        case CriteriaHierarchyConfig.config_type:
            config = CriteriaHierarchyConfig
        case CriteriaInteractionsConfig.config_type:
            config = CriteriaInteractionsConfig
        case CriteriaParametersConfig.config_type:
            config = CriteriaParametersConfig
        case PerformanceMatrixConfig.config_type:
            config = PerformanceMatrixConfig
        case UseValueFunctionConfig.config_type:
            config = UseValueFunctionConfig
        case VetoThresholdsForSupercriteriaConfig.config_type:
            config = VetoThresholdsForSupercriteriaConfig
        case WeightsConfig.config_type:
            config = WeightsConfig
        case SortingCriteria.config_type:
            config = SortingCriteria
        case _:
            error_msg = f"archivo de configuracion '{config_type}' no encontrado"
            raise HTTPException(status_code=404, detail=error_msg)
    return config


DATA_SEPARATOR = "\t"


class AdditionalCriteriaParametersConfig(ConfigFile):
    """Clase para configuracion de Additional criteria parameters"""

    config_type = "Additional criteria parameters"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        # Splits data into lines
        lines = data.splitlines()

        # Ensure there are at least 3 lines
        if len(lines) != 3:
            return False

        if " " in data:
            return False

        # Check first line for two real numbers separated by a tab
        pattern = re.compile(
            r"^\s*([-+]?\d*\.\d+|[-+]?\d+)\t([-+]?\d*\.\d+|[-+]?\d+)\s*$"
        )
        matches = pattern.findall(lines[0])
        if not matches:  # If the pattern doesn't match
            return False

            # Check second line for a single real number
        pattern = re.compile(r"^\s*([-+]?\d*\.\d+|[-+]?\d+)\s*$")
        matches = pattern.findall(lines[1])
        if not matches:  # If the pattern doesn't match
            return False

        # Check third line for a single integer
        pattern = re.compile(r"^\s*([-+]?\d+)\s*$")
        matches = pattern.findall(lines[2])
        if not matches:  # If the pattern doesn't match
            return False

        # If all conditions are met, the file is valid
        return True


class CredibilityCriteriaConfig(ConfigFile):
    """Clase para configuracion de Credibility criteria"""

    config_type = "Credibility criteria"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class CriteriaDirectionsConfig(ConfigFile):
    """Clase para configuracion de Criteria directions"""

    config_type = "Criteria directions"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        # Split the data into lines
        lines = data.splitlines()

        # Check if there are at least two lines
        if len(lines) < 2:
            return False

        # Split the second line into elements and check each one
        second_line_elements = lines[1].strip().split()
        for element in second_line_elements:
            if element not in ("1", "-1"):
                return False

        return True


class CriteriaHierarchyConfig(ConfigFile):
    """Clase para configuracion de Criteria hierarchy"""

    config_type = "Criteria hierarchy"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        lines = data.strip().split("\n")

        if not lines:
            return False

        node_regex = re.compile(r"^g\d*(?:=\{(g\d+(?:,g\d+)*)\})?$")

        node_hierarchy: dict[str, list[str]] = {}

        for line in lines:
            if not node_regex.match(line):
                return False

            node, *children = line.split("=")

            if not node.startswith("g"):
                return False

            if children:
                children = children[0].strip("{}").split(",")
                for child in children:
                    if not child.startswith(node):
                        return False

                    if node in node_hierarchy:
                        node_hierarchy[node].append(child)
                    else:
                        node_hierarchy[node] = [child]
            else:
                node_hierarchy[node] = []

        if "g" not in node_hierarchy:
            return False

        return True


class CriteriaInteractionsConfig(ConfigFile):
    """Clase para configuracion de Criteria interactions"""

    config_type = "Criteria interactions"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class CriteriaParametersConfig(ConfigFile):
    """Clase para configuracion de Criteria parameters"""

    config_type = "Criteria parameters"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class PerformanceMatrixConfig(ConfigFile):
    """Clase para configuracion de Performance matrix"""

    config_type = "Performance matrix"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class UseValueFunctionConfig(ConfigFile):
    """Clase para configuracion de Use value function"""

    config_type = "Use value function"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class VetoThresholdsForSupercriteriaConfig(ConfigFile):
    """Clase para configuracion de Veto thresholds for supercriteria"""

    config_type = "Veto thresholds for supercriteria"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class WeightsConfig(ConfigFile):
    """Clase para configuracion de Weights"""

    config_type = "Weights"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True


class SortingCriteria(ConfigFile):
    """Clase para configuracion de Sorting criteria"""

    config_type = "Sorting criteria"

    @classmethod
    def is_valid_format(cls, data: str) -> bool:
        return True
