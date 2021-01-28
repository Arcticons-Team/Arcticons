#!/bin/bash

# Quickly verify the cotents of svgs do not contain
# features that are unsupported in avd

# URL containing info for correcting issues
URL='https://github.com/dkanada/frost/blob/master/CONTRIBUTING.md'

# Set to true to enable batch mode
# can also be enabled at run time using "-b"
BATCH_MODE=false

###############################################################################

if [ "$1" == "-b" ]
then
  batch=true
else
  batch=$BATCH_MODE
fi

if $batch
then
  grep_switches='liq'
else
  grep_switches='li'
fi

if ! \
  grep \
    -"$grep_switches" \
    "[0-9]{1,3}\.[0-9]{1,3}e-[0-9]\|fill-rule:evenodd\|matrix\|transform" \
     ./*.svg
then
  exit 0
else
  if ! $batch
  then
    echo 'FAIL: The listed icons do not pass *.svg -> *.avd verification.'
    echo '	 For info on how to fix this, please see:'
    echo "	 $URL"
  fi
  exit 1
fi
