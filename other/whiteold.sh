#!/bin/bash

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  sed -r -i 's:stroke="#\w{3,6}":fill="#ffffff":' ${FILE}

  if [ $(grep -o stroke= ${FILE} | wc -l) -ge 1 ]
  then
    sed -r -i 's/style="[^"]*"//' ${FILE}
  else
    sed -r -i 's/style="[^"]*"/fill="#ffffff"/' ${FILE}
  fi
  sed -r -i 's:opacity="[^"]*"::' ${FILE}
done
