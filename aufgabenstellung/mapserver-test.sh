#!/bin/bash
function removeDataStore() {
    rm  -f /tmp/datastore.ser
}


function restartServer() {
    if jps | grep -q -w Server; then
        jkill Server
    fi
    while  netstat -l | grep -q -w 4938 ; do
        echo -n .
    done
    echo server stopped

#Pfad ist gegebenenfalls anzupassen
    java -cp $(find /tmp/ -name Server.class 2> /dev/null | sed -r -e "s|/de.+||"):$CLASSPATH de/haw_landshut/gschied/ws2016/praktikum5_mapserver/Server &
    while ! netstat -l | grep -q -w 4938 ; do
        echo -n .
    done
    echo server up
}

#$1 = http-methode z.B. GET
#$2 = Resoource z.B. foo/1/One
#$3 = Erwarteter Resonse Code z.b. 200
#$4 = Substring des Response Body (Optional default ist: wird nicht gesprueft)

function testCall() { 
    echo curl -s  --no-buffer -D /dev/stdout -X $1 http://localhost:4938/$2
    curl -s --no-buffer -o - -D - -X $1 http://localhost:4938/$2 > /tmp/curl.out 
#    echo wget -O - --method=$1 --save-headers --quiet http://localhost:4938/$2
#    wget -O - --method=$1 --save-headers --quiet http://localhost:4938/$2 &> /tmp/curl.out
    grep  $3 /tmp/curl.out
    if [ -n "$4" ] ; then
        if ! grep $4 /tmp/curl.out; then
           echo ERROR wanted: $4 
           echo have: $(cat /tmp/curl.out)
           exit -1
        fi
    fi
           
    if [ $? != 0 ]; then
        echo ERROR 
        cat /tmp/curl.out
        exit -1
    fi
}
# --------------------------------------------------------------------------------------------
#Put und GET Test
#figlet PUT, GET
removeDataStore
restartServer
#POST, PU und GET testen
testCall POST foo 201
#erwarteter Fehler, da die id bereits existiert
testCall POST foo 409
testCall PUT foo/1/One 200
testCall PUT foo/2/Two 200
testCall PUT foo/3/Three 200
testCall GET foo/2 200 Two
testCall GET foo/1 200 One
testCall GET foo/3 200 Three

#----------------------------------------------------------
#Test fehlendes Element
#figlet GET ohne PUT
removeDataStore
restartServer
#POST, PU und GET testen
testCall POST foo 201
testCall GET foo/3 404 

#------------------------------------------------------
#nichtexistierende map 
#figlet GET ohne Map
removeDataStore
restartServer
testCall GET foo/3 404 



#-----------------------------------------------------------------
#mehrere maps
#figlet mehrere Maps
removeDataStore
restartServer
#POST, PU und GET testen
testCall POST foo 201
#erwarteter Fehler, da die id bereits existiert
testCall POST foo 409
testCall PUT foo/1/One 200
testCall PUT foo/2/Two 200
testCall PUT foo/3/Three 200
testCall GET foo/2 200 Two
testCall GET foo/1 200 One
testCall GET foo/3 200 Three

#Aufruf via Browser http://localhost:4938/foo/2 liefert two
testCall POST bla 201
testCall PUT bla/Sonntag/Sunday 200
testCall PUT bla/Montag/Monday 200
testCall PUT bla/Dienstag/Tuesday 200
testCall GET bla/Sonntag 200 Sunday
testCall GET bla/Montag 200 Monday
testCall GET bla/Dienstag 200 Tuesday

#Aufruf via Browser http://localhost:4938/bla/Sonntag liefert Sunday

testCall POST fasel 201
testCall PUT fasel/1/I 200
testCall PUT fasel/5/V 200
testCall PUT fasel/10/X 200
testCall PUT fasel/9/IX 200
testCall GET fasel/9 200 IX
#Aufruf via Browser http://localhost:4938/fasel/10 liefert X

#----------------------------------------------------------
#Elemente loeschen
#figlet PUT, DELETE, GET
removeDataStore
restartServer
testCall DELETE foo/2 404
testCall POST foo 201
testCall DELETE foo/2 404
testCall PUT foo/1/One 200
testCall PUT foo/2/Two 200
testCall DELETE foo/1 204
testCall GET foo/2 200 Two
testCall GET foo/1 404 
testCall DELETE foo/1 404





#----------------------------------------------------------
#Elemente loeschen
#figlet PUT, DELETE all
removeDataStore
restartServer
testCall POST foo 201
testCall PUT foo/1/One 200
testCall PUT foo/2/Two 200
#alle Elemente in der Map loeschen, erwarteter Fehler korrekt angezeigt
testCall DELETE foo/\* 204
testCall GET foo/2 404
testCall GET foo/1 404




#------------------------------------------------------------
#Map foo selbst loeschen und neu anlegen
#figlet DELETE Map
removeDataStore
restartServer
testCall POST foo 201
testCall PUT foo/1/One 200
testCall PUT foo/2/Two 200

testCall DELETE foo 204
#Erwarteter Fehler ein weiteres Mal löschen geht nicht
testCall DELETE foo 404
testCall GET foo/2 404
testCall GET foo/1 404


#nach dem loeschen der id foo, wieder eine Map zur id foo anlegen
testCall POST foo 201

# --------------------------------------------------------------------------------------------
#Persistenztest
#figlet persistenz
removeDataStore
restartServer
testCall POST foo 201
testCall PUT foo/1/One 200
testCall PUT foo/2/Two 200
testCall PUT foo/3/Three 200
restartServer
testCall GET foo/2 200 Two
testCall GET foo/1 200 One
testCall GET foo/3 200 Three

