#!/bin/bash

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  echo "Working on" ${NAME}
  cp ${FILE} ${FILE}.tmp
  scour --remove-descriptive-elements --enable-id-stripping --enable-viewboxing --enable-comment-stripping --nindent=4 -i ${FILE}.tmp -o ${FILE}
  rm ${FILE}.tmp
done
