#!/bin/bash

if ! type -P android &> /dev/null; then
    echo "Error: 'android' utility is not in your path."
    echo "  Did you forget to setup the SDK?"
    exit 1
fi
readarray <<END
../external/ActionBarSherlock/actionbarsherlock
../external/InformCam
END

for project in "${MAPFILE[@]}"; do
    android update lib-project --path $project -t 1
done

android update project --path .


