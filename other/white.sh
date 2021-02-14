#!/bin/bash

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  sed -r -i 's:fill="#\w{3,6}":fill="#fff":' ${FILE}
  # A check is needed in case some files have both a style and fill attribute
  if [ $(grep -o fill= ${FILE} | wc -l) -ge 1 ]
  then
    sed -r -i 's/style="[^"]*"//' ${FILE}
  else
    sed -r -i 's/style="[^"]*"/fill="#fff"/' ${FILE}
  fi
  sed -r -i 's:opacity="[^"]*"::' ${FILE}
done
