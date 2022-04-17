#!/bin/bash

SVGDIR=$(dirname $0)

DRAWABLE_PRE='	<item drawable="'
DRAWABLE_SUF='" />\n'

printf '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n	<version>1</version>\n	<category title="New" />\n' > newdrawables.xml

for DIR in $(find ${SVGDIR} -name "*.svg" | sort -V)
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  printf "${DRAWABLE_PRE}${NAME}${DRAWABLE_SUF}" >> newdrawables.xml
done

printf '</resources>\n' >> newdrawables.xml
