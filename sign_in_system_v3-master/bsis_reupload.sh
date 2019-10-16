#!/bin/bash
#JARNAME=
#USERNAME=
#HOST=
#KEYPATH=
#APPDIR=
#TARGETDIR=
#PASSWD=
#PASSPHRASE=

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
    expect -re {[#\$]+} {
        send "cd ${APPDIR}\r"
    }
    expect -re {[#\$]+} {
        send "./killAndDelete.sh\r"
        send "logout\r"
    }
    expect eof
EOF

echo "success delete"

cd ${TARGETDIR}
FILENAME=$(ls | egrep "${JARNAME}" | sed -n 1p)
echo "find jar ${FILENAME}"

/usr/bin/expect << EOF
    set timeout -1
    spawn scp ${ID} ${FILENAME} ${USERNAME}@${HOST}:${APPDIR}
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

echo "success upload"

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
        send "cd ${APPDIR}/\r"
    expect -re {[#\$]+}
        send "./start.sh\r"
    expect -re {[#\$]+} {
        sleep 10
        send "cat nohup.out\r"
        sleep 10
        send "cat nohup.out\r"
        send "logout\r"
    }
    expect eof
EOF

echo "success start"
