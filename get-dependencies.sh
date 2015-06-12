#!/bin/sh

mvn -Dmaven.repo.local=m2 -f dependencies-pom.xml dependency:copy-dependencies 

mkdir lib
cp target/dependency/*.jar lib

