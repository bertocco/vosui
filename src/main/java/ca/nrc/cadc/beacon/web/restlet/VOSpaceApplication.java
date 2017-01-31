/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2016.                            (c) 2016.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *
 ************************************************************************
 */

package ca.nrc.cadc.beacon.web.restlet;

import ca.nrc.cadc.accesscontrol.AccessControlClient;

import ca.nrc.cadc.beacon.web.resources.*;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.vos.client.VOSpaceClient;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;

import javax.servlet.ServletContext;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class VOSpaceApplication extends Application
{
    // Public properties are made available in the Context.
    public static final String VOSPACE_CLIENT_KEY =
            "org.opencadc.vospace.client";
    public static final String REGISTRY_CLIENT_KEY =
            "org.opencadc.registry.client";
    public static final String ACCESS_CONTROL_CLIENT_KEY =
            "org.opencadc.ac.client";
    public static final String VOSPACE_SERVICE_ID_KEY =
            "org.opencadc.vospace.service_id";

    public static final String SERVLET_CONTEXT_ATTRIBUTE_KEY =
            "org.restlet.ext.servlet.ServletContext";

    public static final String DEFAULT_CONTEXT_PATH = "/storage/";

    private static final String DEFAULT_SERVICE_ID =
            "ivo://oats.inaf.it/vospace";
            //"ivo://cadc.nrc.ca/vospace";

    private static final String DEFAULT_GMS_SERVICE_ID =
            "ivo://oats.inaf.it/gms";
            //"ivo://cadc.nrc.ca/gms";
    private static final String GMS_SERVICE_PROPERTY_KEY =
            "org.opencadc.gms.service_id";


    private final Configuration configuration = new SystemConfiguration();


    /**
     * Constructor.
     *
     * @param context The context to use based on parent component context. This
     *                context should be created using the
     *                {@link Context#createChildContext()} method to ensure a proper
     *                isolation with the other applications.
     */
    public VOSpaceApplication(Context context)
    {
        super(context);
        setStatusService(new VOSpaceStatusService());
    }


    /**
     * Creates a inbound root Restlet that will receive all incoming calls. In
     * general, instances of Router, Filter or Finder classes will be used as
     * initial application Restlet. The default implementation returns null by
     * default. This method is intended to be overridden by subclasses.
     *
     * @return The inbound root Restlet.
     */
    @Override
    public Restlet createInboundRoot()
    {
        final Context context = getContext();

        context.getAttributes().put(VOSPACE_CLIENT_KEY, createVOSpaceClient());
        context.getAttributes().put(REGISTRY_CLIENT_KEY,
                                    createRegistryClient());
        context.getAttributes().put(ACCESS_CONTROL_CLIENT_KEY,
                                    createAccessControlClient());
        context.getAttributes().put(VOSPACE_SERVICE_ID_KEY,
                                    URI.create(configuration.getString(
                                            VOSPACE_SERVICE_ID_KEY,
                                            DEFAULT_SERVICE_ID)));

        final ServletContext servletContext =
                (ServletContext) context.getAttributes().get(
                        SERVLET_CONTEXT_ATTRIBUTE_KEY);



        final String contextPath = (servletContext == null)
                                   ? DEFAULT_CONTEXT_PATH : "/";

        final Router router = new Router(context);

        router.attach(contextPath + "ac/authenticate",
                      AccessControlServerResource.class);

        router.attach(contextPath + "page", PageServerResource.class);
        final TemplateRoute pageRoute =
                router.attach(contextPath + "page/{path}", PageServerResource.class);

        // Allow for an empty path to be the root.
        router.attach(contextPath + "list", MainPageServerResource.class);
        router.attach(contextPath + "list/", MainPageServerResource.class);

        router.attach(contextPath + "batch-download", BatchDownloadServerResource.class);
        router.attach(contextPath + "batch-download/", BatchDownloadServerResource.class);

        final TemplateRoute bachUploadRoute =
                router.attach(contextPath + "batch-upload/{path}",
                              BatchUploadServerResource.class);

        // Generic endpoint for files, folders, or links.
        final TemplateRoute itemRoute =
                router.attach(contextPath + "item/{path}", StorageItemServerResource.class);
        final TemplateRoute folderRoute =
                router.attach(contextPath + "folder/{path}", FolderItemServerResource.class);
        final TemplateRoute fileRoute =
                router.attach(contextPath + "file/{path}", FileItemServerResource.class);
        final TemplateRoute linkRoute =
                router.attach(contextPath + "link/{path}", LinkItemServerResource.class);
        final TemplateRoute listRoute =
                router.attach(contextPath + "list/{path}", MainPageServerResource.class);
        final TemplateRoute rawRoute =
                router.attach(contextPath + "raw/{path}", MainPageServerResource.class);

        final Map<String, Variable> routeVariables = new HashMap<>();
        routeVariables.put("path", new Variable(Variable.TYPE_URI_PATH));

        bachUploadRoute.getTemplate().getVariables().putAll(routeVariables);
        itemRoute.getTemplate().getVariables().putAll(routeVariables);
        folderRoute.getTemplate().getVariables().putAll(routeVariables);
        linkRoute.getTemplate().getVariables().putAll(routeVariables);
        pageRoute.getTemplate().getVariables().putAll(routeVariables);
        fileRoute.getTemplate().getVariables().putAll(routeVariables);
        listRoute.getTemplate().getVariables().putAll(routeVariables);
        rawRoute.getTemplate().getVariables().putAll(routeVariables);

        router.setContext(getContext());
        return router;
    }

    private VOSpaceClient createVOSpaceClient()
    {
        return new VOSpaceClient(URI.create(
                configuration.getString(VOSPACE_SERVICE_ID_KEY,
                                        DEFAULT_SERVICE_ID)));
    }

    private RegistryClient createRegistryClient()
    {
        return new RegistryClient();
    }

    private AccessControlClient createAccessControlClient()
    {
        return new AccessControlClient(URI.create(
                configuration.getString(
                        VOSpaceApplication.GMS_SERVICE_PROPERTY_KEY,
                        VOSpaceApplication.DEFAULT_GMS_SERVICE_ID)));
    }


    public static void main(final String[] args) throws Exception
    {
        final Component component = new Component();
        final Application application = new VOSpaceApplication(null)
        {
            /**
             * Creates a inbound root Restlet that will receive all incoming calls. In
             * general, instances of Router, Filter or Finder classes will be used as
             * initial application Restlet. The default implementation returns null by
             * default. This method is intended to be overridden by subclasses.
             *
             * @return The inbound root Restlet.
             */
            @Override
            public Restlet createInboundRoot()
            {
                final Context context = getContext();
                final Router router = (Router) super.createInboundRoot();
                final String[] staticDirs = {"js", "css", "scripts", "fonts",
                                             "themes"};

                for (final String dir : staticDirs)
                {
                    router.attach(DEFAULT_CONTEXT_PATH + dir + "/",
                                  new Directory(context,
                                                "file://"
                                                + System.getProperty("user.dir")
                                                + "/src/main/webapp/" + dir));
                }

                return router;
            }
        };

        component.getServers().add(Protocol.HTTP, 8080);
        component.getClients().add(Protocol.FILE);

        component.getDefaultHost().attach(application);
        component.start();
    }
}
