package com.vacrodex.jvultr.entities.baremetal.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.vacrodex.jvultr.entities.application.Application;
import com.vacrodex.jvultr.entities.bandwidth.Bandwidth;
import com.vacrodex.jvultr.entities.baremetal.Baremetal;
import com.vacrodex.jvultr.entities.baremetal.MetalPlan;
import com.vacrodex.jvultr.entities.regions.Datacenter;
import com.vacrodex.jvultr.entities.regions.Region;
import com.vacrodex.jvultr.entities.system.Memory;
import com.vacrodex.jvultr.entities.system.Status;
import com.vacrodex.jvultr.exceptions.TooSoonException;
import com.vacrodex.jvultr.jVultr;
import com.vacrodex.jvultr.utils.DateUtil;
import com.vacrodex.jvultr.utils.rest.RestEndpoints;
import com.vacrodex.jvultr.utils.rest.RestMethods;
import com.vacrodex.jvultr.utils.rest.RestRequest;
import com.vacrodex.jvultr.utils.rest.RestRequestResult;
import java.text.ParseException;
import java.util.Date;
import lombok.Getter;
import lombok.ToString;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Response;

/**
 * @author Cameron Wolfe
 */
@Getter
@ToString(exclude = {"jVultr"})
public class ImplBaremetal implements Baremetal {
  
  private jVultr jVultr;
  
  private int subId;
  private String label;
  private String tag;
  private String ip;
  private com.vacrodex.jvultr.entities.os.OS OS;
  private Memory memory;
  private Datacenter datacenter;
  private Region region;
  private String defaultPassword;
  private Date creation;
  private Status status;
  private String netmaskv4;
  private String gatewayv4;
  private MetalPlan plan;
  private Bandwidth bandwidth;
  private Application application;
  private Date creationDate;
  
  public ImplBaremetal(jVultr jVultr, int subId) {
    this.jVultr = jVultr;
    this.subId = subId;
    refreshInformation();
  }
  
  @Override
  public void setLabel(String label) {
    this.label = label;
    
    //todo Send request
  }
  
  @Override
  public boolean halt() {
    return new RestRequest<Response>(getJVultr(), RestMethods.POST, RestEndpoints.BAREMETAL_HALT)
        .setMultipartBody(new MultipartBody.Builder().addFormDataPart("SUBID", String.valueOf(getSubId())).build())
        .execute(RestRequestResult::getResponse)
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        }).join().code() == 200;
  }
  
  @Override
  public boolean destory() throws TooSoonException {
    //todo Send request
    return new RestRequest<Response>(getJVultr(), RestMethods.POST, RestEndpoints.BAREMTEAL_DESTROY)
        .setBody("SUBID="+getSubId())
        .execute(RestRequestResult::getResponse)
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        }).join().code() == 200;
  }
  
  @Override
  public boolean stop() {
    return halt();
  }
  
  @Override
  public boolean restart() {
    return new RestRequest<Response>(getJVultr(), RestMethods.POST, RestEndpoints.BAREMETAL_REBOOT)
        .setMultipartBody(new MultipartBody.Builder().addFormDataPart("SUBID", String.valueOf(getSubId())).build())
        .execute(RestRequestResult::getResponse)
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        }).join().code() == 200;
  }
  
  @Override
  public boolean start() {
    return restart();
  }
  
  @Override
  public boolean reinstall() {
    return new RestRequest<Response>(getJVultr(), RestMethods.POST, RestEndpoints.BAREMETAL_REINSTALL)
        .setMultipartBody(new MultipartBody.Builder().addFormDataPart("SUBID", String.valueOf(getSubId())).build())
        .execute(RestRequestResult::getResponse)
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        }).join().code() == 200;
  }
  
  @Override
  public boolean setUserData() {
    //todo Send request
    return false;
  }
  
  @Override
  public boolean enableIPV6() {
    //todo Send request
    return false;
  }
  
  @Override
  public boolean refreshInformation() {

    JsonNode body = new RestRequest<JsonNode>(getJVultr(), RestMethods.GET, RestEndpoints.BAREMETAL_LIST)
        .addQueryParameter("SUBID", String.valueOf(getSubId()))
        .execute(RestRequestResult::getJsonBody)
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        }).join();
    
    if (body == null) {
      return false;
    }
  
    System.out.println(body);
    
    // TODO: Implement: OS, Metal Plan, Data Center,
    ip = body.get("main_ip").asText();
    label = body.get("label").asText();
    tag = body.get("tag").asText();
    gatewayv4 = body.get("gateway_v4").asText();
    netmaskv4 = body.get("netmask_v4").asText();
    
    application = getJVultr().getApplicationById(body.get("APPID").asInt());
    status = Status.ofVultr(body.get("status").asText());
    
    try {
      creationDate = DateUtil.VULTR_DATE.parse(body.get("date_created").asText());
    } catch (ParseException exception) {
      // Implement stuff later, for now, we can just set the date as a current one
      creationDate = new Date();
    }
    
    defaultPassword = body.get("default_password").asText();
    region = getJVultr().getRegionByName(body.get("location").asText());
    
    return true;
  }
}
