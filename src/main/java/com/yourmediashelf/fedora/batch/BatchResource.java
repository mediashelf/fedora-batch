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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.codehaus.jackson.map.ObjectMapper;
import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.Server;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.rest.BaseRestResource;
import org.fcrepo.server.storage.types.MIMETypedStream;
import org.fcrepo.utilities.DateUtility;
import org.springframework.stereotype.Component;

/**
 * <p>Rest controller for batch operations on Fedora's REST API.
 *
 * @author Edwin Shin
 */
@Path("/")
@Component
public class BatchResource extends BaseRestResource implements Constants {

    public BatchResource(Server server) {
        super(server);
    }
    
    /**
     * <p>Get the specified datastream content from the selected objects.
     * 
     * @param pidList a List of pids (pidList will be union'ed with pidsCSV)
     * @param dsidList a List of datastream ids (dsidList will be union'ed with dsidsCSV)
     * @param pidsCsv a comma-separated list of pids
     * @param dsidsCsv a comma-separated list of datastream ids
     * @return a multipart/mixed response
     */
    @Path("/getDatastreams")
    @Produces("multipart/mixed")
    @GET
    public MultipartBody getDatastreams(
            @QueryParam("pid") List<String> pidList, 
            @QueryParam("dsid") List<String> dsidList,
            @QueryParam("pids") String pidsCsv, 
            @QueryParam("dsids") String dsidsCsv) {
        
        Set<String> pids = combine(pidList, pidsCsv);
        Set<String> dsids = combine(dsidList, dsidsCsv);
        return getDatastreams(pids, dsids);
    }
    
    /**
     * <p>Delete the specified datastreams from the selected objects.
     * 
     * @param pidList a List of pids (pidList will be union'ed with pidsCSV)
     * @param dsidList a List of datastream ids (dsidList will be union'ed with dsidsCSV)
     * @param pidsCsv a comma-separated list of pids
     * @param dsidsCsv a comma-separated list of datastream ids
     * @return a multipart/mixed response
     */
    @Path("/deleteDatastreams")
    @Produces("multipart/mixed")
    @DELETE
    public MultipartBody deleteDatastreams(
            @QueryParam("pid") List<String> pidList, 
            @QueryParam("dsid") List<String> dsidList,
            @QueryParam("pids") String pidsCsv, 
            @QueryParam("dsids") String dsidsCsv,
            @QueryParam("startDT") String startDT,
            @QueryParam("endDT") String endDT,
            @QueryParam("logMessage") String logMessage) {
        
        Set<String> pids = combine(pidList, pidsCsv);
        Set<String> dsids = combine(dsidList, dsidsCsv);
        return deleteDatastreams(pids, dsids, startDT, endDT, logMessage);
    }
    
    private MultipartBody getDatastreams(Set<String> pids, Set<String> dsids) {
        List<Attachment> atts =
                new ArrayList<Attachment>(pids.size() * dsids.size());

        Context context = getContext();
        MIMETypedStream mts = null;

        for (String pid : pids) {
            for (String dsid : dsids) {
                try {
                    mts =
                            m_access.getDatastreamDissemination(context, pid,
                                    dsid, null);                    
                    atts.add(new Attachment(pid + "/" + dsid, mts.MIMEType, mts
                            .getStream()));
                } catch (ServerException e) {
                    throw new WebApplicationException(handleException(e, false));
                }
            }
        }
        return new MultipartBody(atts, true);
    }
    
    private MultipartBody deleteDatastreams(Set<String> pids, Set<String> dsids, 
            String startDT, String endDT, String logMessage) {
        List<Attachment> atts =
                new ArrayList<Attachment>(pids.size() * dsids.size());

        Context context = getContext();
        Date[] purged = null;
        ObjectMapper mapper = new ObjectMapper();

        for (String pid : pids) {
            for (String dsid : dsids) {
                try {
                    purged =
                            m_management.purgeDatastream(context, pid, dsid,
                                    DateUtility.parseDateOrNull(startDT),
                                    DateUtility.parseDateOrNull(endDT),
                                    logMessage);

                    // convert purged into a String[] so we can return it as a JSON array
                    List<String> results = new ArrayList<String>();
                    for (Date d : purged) {
                        results.add(DateUtility.convertDateToXSDString(d));
                    }
                    atts.add(new Attachment(pid + "/" + dsid,
                            MediaType.APPLICATION_JSON, mapper
                                    .writeValueAsString(results)));
                } catch (Exception e) {
                    throw new WebApplicationException(handleException(e, false));
                }
            }
        }
        return new MultipartBody(atts, true);
    }
    
    private Set<String> combine(Collection<String> coll, String csv) {
        Set<String> combined = new HashSet<String>();
        if (coll != null) {
            combined.addAll(coll);
        }
        if (csv != null && !csv.trim().isEmpty()) {
            combined.addAll(Arrays.asList(csv.split(",")));
        }
        return combined;
    }
}
