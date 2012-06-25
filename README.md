MediaShelf fedora-batch
========================

A batch controller for Fedora's REST API.

fedora-batch is a proof-of-concept implementation of a batch API for Fedora.

Say, for example, you want to display the metadata for 10 different Fedora 
objects on a web page. For the sake of example, let's assume we're working with 
10 Fedora objects which each represent a video resource and that the metadata 
we're interested in is contained in each object's DC datastream.

Currently, you might tackle this by issuing 10 separate getDatastreamDissemination 
requests to Fedora. However, what if you could just issue a single batch 
request to Fedora and have Fedora assemble and return a single response?

And because requirements always change, now imagine that you also need some 
metadata that's only captured in the RELS-EXT datastream, so now instead of 10,
you face 20 separate HTTP requests. But with the batch request, you just add 
the second datastream ID parameter and batch controller takes care of the rest.

For complete documentation see: http://mediashelf.github.com/fedora-batch/

Usage
------------

Sample request:

    http://localhost:8080/fedora/batch/getDatastreams?pid=demo:1&pid=demo:2&dsid=DC
    
The request above fetches the DC datastreams from demo:1 and demo:2 objects, and 
returns the result as a multipart/mixed message, e.g.:

```
--uuid:3d31e842-3291-44ca-ad66-43f6cea63f90
Content-Type: text/xml
Content-Transfer-Encoding: binary
Content-ID: <demo:1/DC>


<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
  <dc:title>Demo Object 1</dc:title>
  <dc:identifier>demo:1</dc:identifier>
</oai_dc:dc>

--uuid:3d31e842-3291-44ca-ad66-43f6cea63f90--
Content-Type: text/xml
Content-Transfer-Encoding: binary
Content-ID: <demo:2/DC>


<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
  <dc:title>Demo Object 2</dc:title>
  <dc:identifier>demo:2</dc:identifier>
</oai_dc:dc>

--uuid:0e61693c-18ea-4b53-912f-b4d15dd00283--
```

As of version 0.5, fedora-client provides basic support for BatchGetDatastreams:

```
BatchGetDatastreams req = new BatchGetDatastreams();
BatchResponse res = req.pids("demo:1", "demo:2").dsids("DC").execute(fedora);
for (BodyPart part : res.getBodyParts()) {
  // do something...
  System.out.println(part.getEntityAs(String.class);
}
```

Requirements
------------

* Fedora 3.6 (if you're interested in undertaking a backport for 3.5, contact me)

Installation
------------

Installation of fedora-batch requires updating the web.xml of the Fedora webapp, 
copying a configuration file to FEDORA_HOME, and adding the fedora-batch jar to 
the Fedora web app:

1. Edit your Fedora web.xml to include the following:

        <servlet>
          <display-name>CXF Batch Servlet</display-name>
          <servlet-name>CXFBatchServlet</servlet-name>
          <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
          <init-param>
            <param-name>config-location</param-name>
            <param-value>file:${fedora.home}/server/config/spring/web/jaxrs/batch-jaxrs.xml</param-value>
          </init-param>
          <load-on-startup>3</load-on-startup>
        </servlet>

        <servlet-mapping>
          <servlet-name>CXFBatchServlet</servlet-name>
          <url-pattern>/batch/*</url-pattern>
        </servlet-mapping>

    (Tomcat users will find your web.xml in `$CATALINA_HOME/webapps/fedora/WEB-INF/web.xml`)

2. Copy batch-jaxrs.xml to `$FEDORA_HOME/server/config/spring/web/jaxrs/`
3. Add `fedora-batch-NNN.jar` to your Fedora webapp.

    (Tomcat users can copy the fedora-batch jar to `$CATALINA_HOME/webapps/fedora/WEB-INF/lib`)

Building from source
--------------------

1. Download the source, e.g.

        git clone git://github.com/mediashelf/fedora-batch.git

2. Build the project

        cd fedora-batch
        mvn install
        
    fedora-batch ships with integration tests that require a running instance of 
    Fedora that has the ResourceIndex module enabled. 
    Set the testing properties (e.g. username, password) in pom.xml (or pass 
    them in as arguments to Maven), or to skip the integration tests entirely:
   
        mvn install -DskipTests
        

License & Copyright
-------------------

fedora-batch is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

fedora-batch is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with fedora-client.  If not, see <http://www.gnu.org/licenses/>.

Copyright &copy; 2012 MediaShelf
