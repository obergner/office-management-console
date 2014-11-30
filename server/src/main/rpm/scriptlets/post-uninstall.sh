#!/bin/sh

if [ "$1" -ge 1 ]; then
    /sbin/service office-account-manager condrestart > /dev/null 2>&1
fi
exit 0
