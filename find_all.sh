#!/bin/bash

# Obtain the directory names for Patch Finder can switch to available branch of the corresponding page
for directory in *
do
    if test -d "$directory"
    then
       if test -f "$directory/config.properties"
       then
            if [ -z "$branchDirectories" ]
            then
                branchDirectories=$directory
            else
                branchDirectories=$branchDirectories\|$directory
            fi
       fi
    fi
done
echo We have these branches: $branchDirectories
echo

# Start finding patches
for directory in *
do
    if test -d "$directory"
    then
       if test -f "$directory/config.properties"
       then
            echo "Start finding in $directory ..."
            java -jar res/patch_finder.jar $directory/config.properties $directory:$branchDirectories
            if test -d "$directory/out"
            then
                if test -d "$directory/latest"
                then
                    mv $directory/latest $directory/temp
                fi
                mv $directory/out $directory/latest
                echo "Copying to $directory/${BUILD_ID} ..."
                cp -R $directory/latest $directory/${BUILD_ID}
                rm -Rf $directory/temp
            else
                echo "No output was generated. Exit with $?"
            fi
            echo "Done in $directory"
            echo
       fi
    fi
done

