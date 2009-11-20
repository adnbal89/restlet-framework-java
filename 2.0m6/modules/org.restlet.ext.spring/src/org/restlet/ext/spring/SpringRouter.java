/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.spring;

import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.routing.Router;

/**
 * Router that is easily configurable with Spring. Here is a usage example:
 * 
 * <pre>
 * &lt;bean class=&quot;org.restlet.ext.spring.SpringRouter&quot;&gt;
 *     &lt;constructor-arg ref=&quot;application&quot; /&gt;
 * 
 *     &lt;property name=&quot;attachments&quot;&gt;
 *         &lt;map&gt;
 *             &lt;entry key=&quot;/users/{user}&quot;                  value=&quot;org.restlet.example.tutorial.UserResource&quot; /&gt;
 *             &lt;entry key=&quot;/users/{user}/orders&quot;           value=&quot;org.restlet.example.tutorial.OrdersResource&quot; /&gt;
 *             &lt;entry key=&quot;/users/{user}/orders/{order}&quot;   value=&quot;org.restlet.example.tutorial.OrderResource&quot; /&gt;
 *         &lt;/map&gt;
 *     &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
 * @author Jerome Louvel
 */
public class SpringRouter extends Router {

    /**
     * Sets the map of routes to attach.
     * 
     * @param router
     *            The router to attach to.
     * @param routes
     *            The map of routes to attach
     */
    @SuppressWarnings( { "unchecked", "deprecation" })
    public static void setAttachments(Router router, Map<String, Object> routes) {
        Object value;
        Class resourceClass;

        try {
            for (final String key : routes.keySet()) {
                value = routes.get(key);

                if (value instanceof Restlet) {
                    router.attach(key, (Restlet) value);
                } else if (value instanceof Class) {
                    router.attach(key, (Class<?>) value);
                } else if (value instanceof String) {
                    resourceClass = Engine.loadClass((String) value);

                    if (org.restlet.resource.Resource.class
                            .isAssignableFrom(resourceClass)) {
                        router.attach(key, resourceClass);
                    } else if (org.restlet.resource.ServerResource.class
                            .isAssignableFrom(resourceClass)) {
                        router.attach(key, resourceClass);
                    } else {
                        router
                                .getLogger()
                                .warning(
                                        "Unknown class found in the mappings. Only subclasses of org.restlet.resource.Resource and ServerResource are allowed.");
                    }
                } else {
                    router
                            .getLogger()
                            .warning(
                                    "Unknown object found in the mappings. Only instances of Restlet and subclasses of org.restlet.resource.Resource and ServerResource are allowed.");
                }
            }
        } catch (ClassNotFoundException e) {
            router.getLogger().log(Level.WARNING,
                    "Unable to set the router mappings", e);
        }
    }

    /**
     * Constructor.
     */
    public SpringRouter() {
        super();
    }

    /**
     * Constructor with a parent context.
     * 
     * @param context
     *            The parent context.
     */
    public SpringRouter(Context context) {
        super(context);
    }

    /**
     * Constructor with a parent Restlet.
     * 
     * @param parent
     *            The parent Restlet.
     */
    public SpringRouter(Restlet parent) {
        super(parent.getContext());
    }

    /**
     * Sets the map of routes to attach. The map keys are the URI templates and
     * the values can be either Restlet instances, Resource subclasses (as Class
     * instances or as qualified class names).
     * 
     * @param routes
     *            The map of routes to attach.
     */
    public void setAttachments(Map<String, Object> routes) {
        setAttachments(this, routes);
    }

}
