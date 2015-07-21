#!/bin/sh

for f in `find external/ -name project.properties`; do
    android update lib-project -p `dirname $f` -t android-19
done
android update project -p app/ -t android-19 -n CameraV --subprojects

cp external/InformaCam/libs/android-support-v4.jar external/ActionBarSherlock/actionbarsherlock/libs/android-support-v4.jar
cp external/InformaCam/libs/android-support-v4.jar external/InformaCam/external/OnionKit/libnetcipher/libs/android-support-v4.jar
cp external/InformaCam/libs/android-support-v4.jar external/InformaCam/external/CacheWord/cachewordlib/libs/android-support-v4.jar
cp external/InformaCam/libs/iocipher.jar external/InformaCam/external/CacheWord/cachewordlib/libs/iocipher.jar


rm -rf external/ActionBarSherlock/actionbarsherlock-samples
rm -rf app/bin/dexedLibs/android-support-v4*

