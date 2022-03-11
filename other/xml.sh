#!/bin/bash

SVGDIR="../icons/white/"
EXPORT="../app/src/main/"
ICPACK_PRE='	    <item>'
ICPACK_SUF='</item>\n'
DRAWABLE_PRE='	  <item drawable="'
DRAWABLE_SUF='" />\n'
CODE_PRE='	  R.drawable.nodpi_'
CODE_SUF=',\n'

printf '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n	 <string-array name="icon_pack" translatable="false">\n' > iconpack.xml
printf '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n	 <version>1</version>\n	  <category title="New" />\n' > drawable.xml
printf '    private Integer[] mImages = {\n' > code.xml

for DIR in $(find ${SVGDIR} -name "*.svg" | sort -V)
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  printf "${ICPACK_PRE}${NAME}${ICPACK_SUF}" >> iconpack.xml
  printf "${DRAWABLE_PRE}${NAME}${DRAWABLE_SUF}" >> drawable.xml
  printf "${CODE_PRE}${NAME}${CODE_SUF}" >> code.xml
done

printf '    </string-array>\n</resources>\n' >> iconpack.xml
printf '</resources>\n' >> drawable.xml
printf '    };' >> code.xml

rm -rf code.xml

cp -f iconpack.xml ${EXPORT}/res/xml/
mv -f iconpack.xml ${EXPORT}/res/values/
cp -f drawable.xml ${EXPORT}/res/xml/
mv -f drawable.xml ${EXPORT}/assets/

cp -f appfilter.xml ${EXPORT}/assets/
cp -f appfilter.xml ${EXPORT}/res/xml/