#!/bin/bash
cd `dirname $0`
mkdir KifuAnalyzer_v1.x
cp -r ../img KifuAnalyzer_v1.x/
cp -r ../kifu KifuAnalyzer_v1.x/
cp -r ../kifu2022 KifuAnalyzer_v1.x/
cp -r ../kifu2023 KifuAnalyzer_v1.x/
cp -r ../sound KifuAnalyzer_v1.x/
cp -r ../strategy KifuAnalyzer_v1.x/
cp ../src/KifuAnalyzer.jar KifuAnalyzer_v1.x/
cp ReadMe KifuAnalyzer_v1.x/
cp start.command KifuAnalyzer_v1.x/