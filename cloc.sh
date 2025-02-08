#!/bin/sh
echo "lib:"
cloc neoforge/src/main/java fabric/src/main/java common/src/main/java/com/limachi/arss/utils
echo ""
echo "arss:"
cloc common/src/main/java/com/limachi/arss/client common/src/main/java/com/limachi/arss/common common/src/main/java/com/limachi/arss/mixin common/src/main/java/com/limachi/arss/Arss.java