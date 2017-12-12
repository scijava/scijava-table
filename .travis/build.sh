#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_171ab8eccb0e_key $encrypted_171ab8eccb0e_iv
