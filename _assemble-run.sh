#!/bin/sh

# remove the old log file, if it exists
rm log.out 2> /dev/null

# ------------
# SBT Assembly
# ------------

sbt assembly

if [ $? != 0 ]
then
    echo "Compile/assemble failed, exiting"
    exit 1
fi

# ---
# ANT
# ---

cd deploy
ant

if [ $? != 0 ]
then
    echo "Ant failed, exiting"
    exit 2
fi

sleep 1

#--------------------------------------------------
# Manually Adjust Info.plist File (Add Hi-Res Key)
#--------------------------------------------------

cd ..
sh _addHiResKeyToPlistFile.sh

# ---
# RUN
# ---

open deploy/release/Hyde.app



