#!/bin/bash

SIZES="256"
EXPORT="../app/src/dark/res"
ICON="../icons/white"

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  cp ${FILE} ${FILE}.tmp
  rm ${FILE}.tmp
  cp -f ${FILE} ${ICON}/${FILE}
  for SIZE in ${SIZES}
  do
    inkscape --export-filename=${NAME}.png --export-width=${SIZE} --export-height=${SIZE} ${NAME}.svg
    case ${SIZE} in
      256)
	mv ${NAME}.png ${EXPORT}/drawable-nodpi/
	;;
    esac
  done
  rm ${FILE}
done
