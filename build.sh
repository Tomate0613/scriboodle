#! /usr/bin/env bash

cd libpen
nix build
mkdir ../src/main/resources/natives
cp  --no-preserve=mode,ownership result/lib/libpen.so ../src/main/resources/natives
cd ..
./gradlew build
