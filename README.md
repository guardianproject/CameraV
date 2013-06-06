
iWitness
========

An InformaCam app to generate verifiable media.


Building
--------

git clone https://github.com/guardianproject/iWitnes
cd iWitness/
git submodules update --init --recursive
./setup-ant.sh
./build-native.sh
cd app/
ant clean debug
