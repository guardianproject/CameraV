#!/bin/sh

for f in `find external/ -name project.properties`; do
    android update lib-project -p `dirname $f`
done
android update project -p app/ --subprojects
