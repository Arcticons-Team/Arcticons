#!/bin/bash

sh whiteold.sh
sh rasterdark.sh
sh rasterlight.sh

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  rm ${FILE}
done

sh xml.sh
