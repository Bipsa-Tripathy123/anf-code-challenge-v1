/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anf.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(service = Servlet.class, property = {
    Constants.SERVICE_DESCRIPTION + "= Query",
    SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET,
    SLING_SERVLET_PATHS + "=" + "/bin/anf/querybuilder"
})
public class QueryDataServlet extends SlingSafeMethodsServlet {

        private static final Logger log = LoggerFactory.getLogger(QueryDataServlet.class);
	
	@Reference
	private QueryBuilder builder;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

		try {

			log.info(" start..");

			
			ResourceResolver resourceResolver = request.getResourceResolver();
			
			/**
			 * Adapting the resource resolver to the session object
			 */
		Session	session = resourceResolver.adaptTo(Session.class);
			
			/**
			 * Map for the predicates
			 */
			Map<String, String> predicate = new HashMap<>();

			/**
			 * Configuring the Map for the predicate
			 */
			predicate.put("path", "/content/anf-code-challenge");
			predicate.put("type", "cq:page");
			predicate.put("group.1_property", "jcr:content/anfCodeChallenge");
			predicate.put("group.1_value", "true");
			predicate.put("p.limit", "10");
			
			/**
			 * Creating the Query instance
			 */
			Query query = builder.createQuery(PredicateGroup.create(predicate), session);
			
			
			/**
			 * Getting the search results
			 */
			SearchResult searchResult = query.getResult();
			
			for(Hit hit : searchResult.getHits()) {
				
				String path = hit.getPath();
				
				response.getWriter().println(path);
			}
		} catch (Exception e) {

			log.error(e.getMessage(), e);
		} 
	}
}
