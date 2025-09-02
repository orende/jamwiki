# JAMWiki

JAMWiki is a Wiki engine implemented using Java/JSP that attempts to provide much of the functionality of MediaWiki. It can be run with or without a database and is designed to be fast and easy to set up.

## Running

The modern way of running this application is by deploying it to a Tomcat instance running in Docker.

    ./mvnw clean install
    export VERSION=$(./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
    docker run -d --rm \
        -v ./jamwiki-war/target/jamwiki-$VERSION.war:/usr/local/tomcat/webapps/jamwiki.war \
        -v ./hsqldb/:/database/ \
        -v ./wikidata:/usr/local/tomcat/data \
        -v ./files/:/usr/local/tomcat/data/files/ \
        -v ./docker/logging.properties:/usr/local/tomcat/conf/logging.properties \
        -v ./docker/jamwiki.properties:/usr/local/tomcat/conf/jamwiki.properties \
        -e PROPERTY_FILE_PATH=/usr/local/tomcat/conf/jamwiki.properties \
        -p 8888:8080 \
        tomcat:9.0.108-jdk17-temurin-noble

Then navigate to http://localhost:8080/jamwiki/en/Special:Setup in a browser.
