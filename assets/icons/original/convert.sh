#!/bin/bash

#Requires ImageMagick to be installed.
#Some builds of ImageMagick on OSX have problems generating the images correctly.

#This script scales and creates images at the correct dpi level for Android.
#It gets placed in a folder called res/drawable/source_images/ in your #Android project along with all your svg files.
#When creating svg files set the image size to the size that you want your hdpi images to be.
#To use simply run the create_images script from its folder and it will generate images for all the svg files.

# purge generated files
rm ../res/drawable/*
rm ../res/drawable-xhdpi/*
rm ../res/drawable-hdpi/*
rm ../res/drawable-mdpi/*
rm ../res/drawable-ldpi/*

for f in *.svg;
do
	echo "Processing $f"
	
	if [ -f drawable/$f ]; then
		echo "manually scaled file exists at drawable"
		convert -background none drawable/$f ../res/drawable/${f/.svg}.png
#	else
#		convert -background none $f ../res/drawable/${f/.svg}.png
	fi


	if [ -f drawable-xhdpi/$f ]; then
		echo "manually scaled file exists at drawable-xhdpi"
		convert -background none drawable-xhdpi/$f ../res/drawable-xhdpi/${f/.svg}.png
	else
		convert -background none $f ../res/drawable-xhdpi/${f/.svg}.png
	fi

	if [ -f drawable-hdpi/$f ]; then
		echo "manually scaled file exists in drawable-hdpi"
		convert -background none drawable-hdpi/$f ../res/drawable-hdpi/${f/.svg}.png
	else
		convert -background none $f[75%] ../res/drawable-hdpi/${f/.svg}.png
	fi

	if [ -f drawable-mdpi/$f ]; then
		echo "manually scaled file exists drawable-mdpi"
		convert -background none drawable-mdpi/$f ../res/drawable-mdpi/${f/.svg}.png
	else
		convert -background none $f[50%] ../res/drawable-mdpi/${f/.svg}.png
	fi

	if [ -f drawable-ldpi/$f ]; then
		echo "manually scaled file exists in drawable-ldpi"
		convert -background none drawable-ldpi/$f ../res/drawable-ldpi/${f/.svg}.png
	else
		convert -background none $f[37.5%] ../res/drawable-ldpi/${f/.svg}.png
	fi
done
