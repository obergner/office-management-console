#!/bin/sh

/usr/sbin/groupadd -r office-account-manager &>/dev/null || :
# SUSE version had -o here, but in Fedora -o isn't allowed without -u
/usr/sbin/useradd -g office-account-manager -s /bin/false -r -c "A convenient web interface for managing Office accounts" \
	-d "/usr/share/office-account-manager" office-account-manager &>/dev/null || :
