@echo off
ant clean
ant jar
copy dist\P11.HierarchicalOutranking.jar dist\executable.jar