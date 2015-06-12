@echo off

mvn -Dmaven.repo.local=m2 -f dependencies-pom.xml dependency:copy-dependencies 

mkdir lib
copy target\dependency\*.jar lib

