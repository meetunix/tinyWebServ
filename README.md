# tinyWebServ

tinyWebServ is a lightweight multithreaded Webserver. If you want to serve a static
website without configuration just start tinyWebserServ with the path to the `ìndex.html`
file. Per default tinyWebServ listen to port 8080 for incomming connections.

    java -jar tinyWebserv-0.1.jar -w /website/ 

## installation
    
    cd tinyWebServ
    mvn package
    cp target/tinyWebServ-0.1-jar-with-dependencies.jar ../tinyWebServ-0.1.jar


## command line options


