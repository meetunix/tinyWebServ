# tinyWebServ

tinyWebServ is a lightweight multithreaded Webserver written in Java.
If you want to serve a static website without configuration just start tinyWebserServ
with the path to the directory where `index.html` is located.

Per default tinyWebServ listen to port 8080 for incomming connections.

    java -jar tinyWebserv.jar -d /website/ 


## installation
    
    cd tinyWebServ
    
    mvn package
    
    cp target/tinyWebServ-0.1.0-jar-with-dependencies.jar ../tinyWebServ.jar


## features

- multithreaded
- keep-alive
- access and error logging
- session-cookie
- HTTP-Methods: GET,HEAD


## planned features

- HTTP-Methods: POST,PUT
- better mime-type handling


## command line options

    Usage: tinyWebServ [-hV] [-d=<directory>] [-p=<port>] [-w=<workers>]
    small and fast multithreaded webserver
      -d, --directory=<directory>
                                The directory to serve.
      -h, --help                Show this help message and exit.
      -p, --port=<port>         TCP port.
      -V, --version             Print version information and exit.
      -w, --workers=<workers>   Number of maximum worker threads.





