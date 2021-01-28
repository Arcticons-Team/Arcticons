#!/bin/bash

# not a script
# command used for changelog

# git diff {version} {version} --name-only -- icons | cut -c7- | sed 's/.\{4\}$//' | sort >> file
# comm -13 <(cat oldVersion.log) <(cat newVersion.log) >> newVersion.uni
