#!/bin/sh

for f in `find external/ -name project.properties`; do
    android update lib-project -p `dirname $f` -t android-17
done
android update project -p app/ -t android-17 --subprojects

cp external/InformaCam/libs/android-support-v4.jar external/ActionBarSherlock/actionbarsherlock/libs/android-support-v4.jar

