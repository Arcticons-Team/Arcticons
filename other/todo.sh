#!/bin/bash

PNGDIR="../app/src/main/res/drawable-xxxhdpi/"
SVGDIR="../icons/"
NEWDIR="../todo/old/"

for DIR in $(find ${PNGDIR} -name "*.png")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  if [ ! -f "${SVGDIR}${NAME}.svg" ]
  then
    echo ${NAME}
    cp "${DIR}" "${NEWDIR}${FILE}"
  fi
done
