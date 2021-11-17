#!/bin/bash

EXPORT="../app/src/main/"

sh whiteold.sh
sh rasterdark.sh
sh rasterlight.sh

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  rm ${FILE}
done

sh sort.sh
sh xml.sh

python merge_new_drawables.py drawable.xml

mv -f new_drawable.xml drawable.xml
cp -f drawable.xml ${EXPORT}/res/xml/
mv -f drawable.xml ${EXPORT}/assets/