#!/bin/bash
if [[ $(./getversion.bash) ]]; then
    echo "version already exists"
else
    git tag -a $(./getversion.bash)
fi

