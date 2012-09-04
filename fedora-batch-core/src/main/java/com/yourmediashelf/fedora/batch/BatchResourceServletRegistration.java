/**
 * Copyright (C) 2012 MediaShelf <http://www.yourmediashelf.com/>
 *
 * This file is part of fedora-batch.
 *
 * fedora-batch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * fedora-batch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with fedora-batch.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.yourmediashelf.fedora.batch;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

/**
 * {@link ServletContextListener} implementation that registers 
 * {@link org.apache.cxf.transport.servlet.CXFServlet CXFServlet} with the 
 * url pattern "/batch/*" and sets config-location to "classpath:batch-jaxrs.xml".
 * 
 * <p>This class requires Servlet 3.0. Servlet 2.x containers (e.g. Tomcat 6.x)
 * will require registering the servlet in web.xml.
 * 
 * @author Edwin Shin
 *
 */

@WebListener()
public class BatchResourceServletRegistration implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        ServletRegistration sr =
                sc.addServlet("CXFBatchServlet",
                        "org.apache.cxf.transport.servlet.CXFServlet");
        sr.setInitParameter("config-location", "classpath:batch-jaxrs.xml");
        sr.addMapping("/batch/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
