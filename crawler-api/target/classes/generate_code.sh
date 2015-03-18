#!/bin/bash

thrift_files=`ls *.thrift`

for var in ${thrift_files}
do
	thrift --gen java $var
done
