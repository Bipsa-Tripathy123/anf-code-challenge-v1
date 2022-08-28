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
package com.anf.core.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;



@Component(service = EventHandler.class,
         immediate = true,
         property = {EventConstants.EVENT_TOPIC + "=" + PageEvent.EVENT_TOPIC})
public class PageEventHandler implements EventHandler {

   //@Reference
  // private ResourceResolverService resourceResolverService;
   
   @Reference
	private ResourceResolverFactory resolverFactory;
   
   private ResourceResolver resolver;
   

   private static final Logger LOG = LoggerFactory.getLogger(PageEventHandler.class);

   @Override
   public void handleEvent(Event event) {
	   LOG.info("Start..............");
      Iterator<PageModification> pageInfo = PageEvent.fromEvent(event).getModifications();
      while (pageInfo.hasNext()) {
         PageModification pageModification = pageInfo.next();
         String pagePath = pageModification.getPath();
         LOG.info("pagePath.............." + pagePath);
         if (pageModification.getType().equals(PageModification.ModificationType.CREATED)) {

            addPageIdToPage(pagePath);
            LOG.info("pagePath1.............."+ pagePath);
         }
      }
   }

   private void addPageIdToPage(String pagePath) {
	   LOG.info("Inside..........");
      try {
    	  Map <String,Object> param = new HashMap<>() ;
  		 param.put(ResourceResolverFactory.SUBSERVICE, "readservice");
         resolver = resolverFactory.getServiceResourceResolver(param);
         LOG.info("SystemUser" + resolver.getUserID());
         LOG.info("resolver"+ resolver);
			//Resource res = resolver.getResource(pagePath +"jcr:content");
       Resource  res = resolver.getResource(pagePath + "/jcr:content");
       LOG.info("res"+ res);
         //res = resolver.getResource("/content/anf/jcr:content");
         LOG.info("Resource" + res );
			ValueMap readMap = res.getValueMap();
			LOG.info("readMap" + readMap );
			LOG.info(readMap.get("jcr:primaryType", ""));
			ModifiableValueMap modMap = res.adaptTo(ModifiableValueMap.class);
			if(modMap != null){
				modMap.put("pageCreated", true);
				resolver.commit();
				LOG.info("Successfully saved");
			}
         LOG.info("CREATE ID FOR page {}", pagePath);
      } catch (LoginException e) {
    	  LOG.error("LoginException",e);
		} catch (PersistenceException e) {
			LOG.error("PersistenceException",e);
		}finally{
			if(resolver != null && resolver.isLive()){
				resolver.close();
			}
   }
}
}