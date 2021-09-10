#!/bin/bash

SIZES="256"
EXPORT="../app/src/dark/res"
ICON="../icons/white"

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  cp ${FILE} ${FILE}.tmp
  scour --remove-descriptive-elements --enable-id-stripping --enable-viewboxing --enable-comment-stripping --nindent=4 -i ${FILE}.tmp -o ${FILE}
  rm ${FILE}.tmp
  cp -f ${FILE} ${ICON}/${FILE}
  for SIZE in ${SIZES}
  do
    inkscape --export-filename=${NAME}.png --export-width=${SIZE} --export-height=${SIZE} ${NAME}.svg
    case ${SIZE} in
      48)
	mv ${NAME}.png ${EXPORT}/drawable-mdpi/
	;;
      72)
	mv ${NAME}.png ${EXPORT}/drawable-hdpi/
	;;
      96)
	mv ${NAME}.png ${EXPORT}/drawable-xhdpi/
	;;
      144)
	mv ${NAME}.png ${EXPORT}/drawable-xxhdpi/
	;;
      192)
	mv ${NAME}.png ${EXPORT}/drawable-xxxhdpi/
	;;
      256)
	mv ${NAME}.png ${EXPORT}/drawable-nodpi/
	;;
    esac
  done
  rm ${FILE}
done
