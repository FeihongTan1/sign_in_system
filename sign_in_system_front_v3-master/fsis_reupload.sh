#!/bin/bash

source ./private.sh -p "edu"

if [ -n "${KEYPATH}" ]; then
    ID=" -i ${KEYPATH} "
fi

/usr/bin/expect << EOF
    set timeout -1
    spawn ssh ${USERNAME}@${HOST} ${ID}
    if { "" == "${KEYPATH}" } {
        if { "" != "${PASSWD}" } {
            expect "*password"
                send "${PASSWD}\r"
        }
    } else {
        if { "" != "${PASSPHRASE}" } {
            expect "*:*"
                send "${PASSPHRASE}\r"
        }
    }
    expect -re {[#\$]+}
        send "cd ${APPPATH}\r"
    expect -re {[#\$]+}
        send "rm -rf dist/\r"
    expect -re {[#\$]+}
        send "logout\r"
    expect eof
EOF

/usr/bin/expect << EOF
    set timeout -1
    spawn scp ${ID} -r ${DISTPATH} ${USERNAME}@${HOST}:${APPPATH}
    if { "" == "${KEYPATH}" } {
        if { "" != "${PASSWD}" } {
            expect "*password"
                send "${PASSWD}\r"
        }
    } else {
        if { "" != "${PASSPHRASE}" } {
            expect "*:*"
                send "${PASSPHRASE}\r"
        }
    }
    expect "ETA" {
        exp_continue;
    }
EOF

echo "upload success"
