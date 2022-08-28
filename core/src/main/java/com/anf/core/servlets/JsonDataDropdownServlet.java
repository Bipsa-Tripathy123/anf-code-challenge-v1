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

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.dam.api.Asset;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import org.json.JSONObject;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import javax.servlet.Servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Component(service = Servlet.class, property = {
  Constants.SERVICE_DESCRIPTION + "= Json Data in dynamic Dropdown",
  "sling.servlet.resourceTypes=utils/granite/components/select/datasource/json",
  "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class JsonDataDropdownServlet extends SlingSafeMethodsServlet {

  private static final Logger log = LoggerFactory.getLogger(JsonDataDropdownServlet.class);

  @Reference
  private ResourceResolverFactory resolverFactory;

  transient ValueMap valueMap;

  transient List < Resource > resourceList;
  transient ResourceResolver resolver;
  transient Resource pathResource;

  protected final String OPTIONS_PROPERTY = "options";

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
    log.info("inside doget.......");
    final ArrayList < Resource > resourceList = new ArrayList < Resource > ();
    pathResource = request.getResource();
    DataSource dataSource = null;

    try {

      String jsonDataPath = Objects.requireNonNull(pathResource.getChild("datasource")).getValueMap().get("options", String.class);
      log.info("jsonDataPath " + jsonDataPath);
      assert jsonDataPath != null;
      Resource jsonResource = request.getResourceResolver().getResource(jsonDataPath);
      log.info("jsonResource " + jsonResource);
      assert jsonResource != null;
      Asset asset = jsonResource.adaptTo(Asset.class);
      log.info("Asset " + asset);
      assert asset != null;
      InputStream inputStream = asset.getOriginal().getStream();
      log.info("inputStream " + inputStream);

      StringBuilder stringBuilder = new StringBuilder();
      log.info("stringBuilder " + stringBuilder);
      String eachLine;
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      log.info("bufferedReader " + bufferedReader);
      while ((eachLine = bufferedReader.readLine()) != null) {
        log.info("Inside while");
        stringBuilder.append(eachLine);
      }
      String json = stringBuilder.toString();
      log.info("json" + json);
      JSONObject jsonObject = new JSONObject(json);
      log.info("Inside jsonObject");

      Iterator < String > jsonKeys = jsonObject.keys();
      log.info("jsonKeys" + jsonKeys);
      //Iterating JSON Objects over key
      while (jsonKeys.hasNext()) {
        String jsonKey = jsonKeys.next();
        log.info("jsonKeyss" + jsonKey);
        String jsonValue = jsonObject.getString(jsonKey);
        log.info("jsonValue" + jsonValue);

        valueMap = new ValueMapDecorator(new HashMap < > ());
        // ModifiableValueMap valueMap = jsonResource.adaptTo(ModifiableValueMap.class);
        valueMap.put("value", jsonKey);
        valueMap.put("text", jsonValue);
        resourceList.add(new ValueMapResource(request.getResourceResolver(), new ResourceMetadata(), "", valueMap));
        log.info("resourceList" + resourceList);

      }

      /*Create a DataSource that is used to populate the drop-down control*/
      if (resourceList.size() > 0) {
        log.info("inside dataif");
        dataSource = new SimpleDataSource(resourceList.iterator());
      } else {
        log.info("inside dataelse");
        dataSource = EmptyDataSource.instance();
      }

      request.setAttribute(DataSource.class.getName(), dataSource);
      log.info("dataSource" + dataSource.getClass().getName());

    } catch (IOException e) {
      log.error("Error in Json Data Exporting : {}", e.getMessage());
    } catch (Exception e) {
      log.info("Error in Getting Drop Down Values ");
      e.printStackTrace();

    }
  }
}