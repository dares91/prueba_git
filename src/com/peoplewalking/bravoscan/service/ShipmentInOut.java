package com.peoplewalking.bravoscan.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.WebService;

public class ShipmentInOut implements WebService {

    @Override
    public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
	// TODO Auto-generated method stub
	/*Probando modificar fichero .. agregando comentarios*/
    }

    @Override
    public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
	final Logger log = Logger.getLogger(ShipmentInOut.class);
  /*Probando modificar fichero .. agregando comentarios*/
	try {
	    // 1 - do some checking of parameters
	    final String businessPartner = request.getParameter("businessPartner");

	    // 2 - create an OBCriteria object and add a filter
	    StringBuilder queryBuilder = new StringBuilder();


	    queryBuilder.append(" as po ")
	    .append(" where exists ( ")
	    .append("   SELECT distinct o FROM Order o ")
	    .append("	left join o.orderLineList ol ")
	    .append("   left join ol.procurementPOInvoiceMatchList pom ")
	    .append("   left join o.documentType dt ")
	    .append("	WHERE o.salesTransaction = 'N' ")
	    .append("   AND pom.salesOrderLine is not null ")
	    .append(" 	AND o.documentStatus = 'CO' ")	
	    .append(" 	AND dt.return = 'N' ")
	    .append("   GROUP BY o, ol ")	    
	    .append("   HAVING ol.orderedQuantity = sum(coalesce(pom.quantity,0))")
	    .append(" )"); 

	    OBQuery<Order> pendingOrdersQuery = OBDal.getInstance().createQuery(Order.class, queryBuilder.toString()); 

	    // 3 - perform the actual query returning a typed list
	    final List<Order> pendingOrders = pendingOrdersQuery.list();

	    if(pendingOrders != null){

		final Order oneOrder = pendingOrders.get(0);

		// 4 - get an json converter and set some options
		final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
			DataToJsonConverter.class);

		final Map<String, String> parameters = getParameterMap(request);

		toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));

		// 5 - and get the result
		final JSONObject json = toJsonConverter.toJsonObject(oneOrder,
			DataResolvingMode.FULL);

		// 6 - write to the response
		writeResult(response, json.toString());
	    }
	} catch (Throwable t) {
	    SessionHandler.getInstance().setDoRollback(true);
	    response.setStatus(500);
	    log.error(t.getMessage(), t);
	    writeResult(response, JsonUtils.convertExceptionToJson(t));
	}
    }

    @Override
    public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {

    }

    @Override
    public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
	// TODO Auto-generated method stub
    }

    private Map<String, String> getParameterMap(HttpServletRequest request) {
	final Map<String, String> parameterMap = new HashMap<String, String>();
	for (@SuppressWarnings("rawtypes")
	Enumeration keys = request.getParameterNames(); keys.hasMoreElements();) {
	    final String key = (String) keys.nextElement();
	    parameterMap.put(key, request.getParameter(key));
	}
	return parameterMap;
    }

    private void writeResult(HttpServletResponse response, String result) throws IOException {
	response.setContentType("application/json;charset=UTF-8");
	response.setHeader("Content-Type", "application/json;charset=UTF-8");

	final Writer w = response.getWriter();
	w.write(result);
	w.close();
    }

    private String getContentFromRequest(HttpServletRequest request) throws IOException{
	final BufferedReader reader = request.getReader();
	if (reader == null) {
	    return "";
	}
	String line;
	final StringBuilder sb = new StringBuilder();
	while ((line = reader.readLine()) != null) {
    	    if (sb.length() > 0) {
    	        sb.append("\n");
    	    }
            sb.append(line);
        }
        return sb.toString();
    }
}
