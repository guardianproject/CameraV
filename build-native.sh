#!/bin/sh

make -C external/InformaCam/external/IOCipher/external
ndk-build -C external/InformaCam/external/IOCipher
ndk-build -C external/InformaCam

cd external/InformaCam/external/android-ffmpeg-java/external/android-ffmpeg/
./configure_make_everything.sh
