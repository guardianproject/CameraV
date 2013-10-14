InformaApp
=========

An InformaCam app to generate verifiable media.

Building
--------

git clone https://github.com/guardianproject/InformaApp

cd InformaApp/

git submodules update --init --recursive

./setup-ant.sh

./build-native.sh

cd app/

ant clean debug

=======
# InformaCam

This is information for building the primary submodule for InformaApp, the InformaCam Core Library, located at external/InformCam

## Setting up Development Environment

**Prerequisites:**

* [Android SDK](https://developer.android.com/sdk/installing/index.html)
* Working [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html) toolchain

Follow these steps to setup your dev environment:

1. Checkout InformaCam git repo
2. Init and update git submodules

    git submodule update --init --recursive

3. Ensure `NDK_BASE` env variable is set to the location of your NDK, example:

    export NDK_BASE=/path/to/android-ndk

4. Build android-ffmpeg

    cd external/android-ffmpeg-java/external/android-ffmpeg/
    ./configure_make_everything.sh

5. Build IOCipher

    cd external/IOCipher/
    make -C external
    ndk-build

6. **Using Eclipse**

    Import into Eclipse (using the "existing projects" option) the projects in this order:

        external/OnionKit/library
        external/android-ffmpeg-java/
        external/IOCipher/
        external/ODKFormParser/

   **Using ANT**

        ./setup-ant.sh
        ant clean debug

