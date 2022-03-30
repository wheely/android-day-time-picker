#!/bin/bash
VERSION=$(./getversion.bash)
if [[ $(git tag | grep $VERSION) ]]; then
    echo "version already exists"
else
    git tag -a $VERSION
fi
