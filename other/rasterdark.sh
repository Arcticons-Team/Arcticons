#!/bin/bash

SIZES="256"
EXPORT="../app/src/dark/res"
ICON="../icons/white"

sh white.sh

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  cp ${FILE} ${FILE}.tmp
  rm ${FILE}.tmp
  cp ${FILE} ${ICON}/${FILE}
  echo "Working on" ${FILE} "Dark Mode"
  for SIZE in ${SIZES}
  do
    inkscape --export-filename=${NAME}.png --export-width=${SIZE} --export-height=${SIZE} ${NAME}.svg
    case ${SIZE} in
      256)
	mv ${NAME}.png ${EXPORT}/drawable-nodpi/
	;;
    esac
  done
done
