#!/bin/bash

sh newcategory.sh
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

python merge_new_drawables.py ../app/src/main/res/xml/drawable.xml

mv -f new_drawable.xml drawable.xml
cp -f drawable.xml ${EXPORT}../app/src/main/res/xml/
mv -f drawable.xml ${EXPORT}../app/src/main/assets/ 