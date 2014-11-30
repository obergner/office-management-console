#!/bin/sh

if [ "$1" = 0 ] ; then
    # if this is uninstallation as opposed to upgrade, delete the service
    /sbin/service office-account-manager stop > /dev/null 2>&1
    /sbin/chkconfig --del office-account-manager
fi
exit 0
