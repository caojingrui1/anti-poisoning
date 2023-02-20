package com.huawei.antipoisoning.business.entity.shield;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.CseSpringBootProviderCodegen", date = "2023-02-15T11:43:22.224+08:00")

public class QueryShieldModel {
  @JsonProperty("projectName")
  private String projectName = null;

  @JsonProperty("repoName")
  private String repoName = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("defectCheckerName")
  private String defectCheckerName = null;

  @JsonProperty("shieldType")
  private String shieldType = null;

  @JsonProperty("applyUser")
  private String applyUser = null;

  @JsonProperty("auditUser")
  private String auditUser = null;

  @JsonProperty("reason")
  private String reason = null;

  @JsonProperty("startTime")
  private String startTime = null;

  @JsonProperty("endTime")
  private String endTime = null;

  @JsonProperty("pageNum")
  private Integer pageNum = null;

  @JsonProperty("pageSize")
  private Integer pageSize = null;

  public QueryShieldModel projectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public QueryShieldModel repoName(String repoName) {
    this.repoName = repoName;
    return this;
  }

  public String getRepoName() {
    return repoName;
  }

  public void setRepoName(String repoName) {
    this.repoName = repoName;
  }

  public QueryShieldModel type(String type) {
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public QueryShieldModel defectCheckerName(String defectCheckerName) {
    this.defectCheckerName = defectCheckerName;
    return this;
  }

  public String getDefectCheckerName() {
    return defectCheckerName;
  }

  public void setDefectCheckerName(String defectCheckerName) {
    this.defectCheckerName = defectCheckerName;
  }

  public QueryShieldModel shieldType(String shieldType) {
    this.shieldType = shieldType;
    return this;
  }

  public String getShieldType() {
    return shieldType;
  }

  public void setShieldType(String shieldType) {
    this.shieldType = shieldType;
  }

  public QueryShieldModel applyUser(String applyUser) {
    this.applyUser = applyUser;
    return this;
  }

  public String getApplyUser() {
    return applyUser;
  }

  public void setApplyUser(String applyUser) {
    this.applyUser = applyUser;
  }

  public QueryShieldModel auditUser(String auditUser) {
    this.auditUser = auditUser;
    return this;
  }

  public String getAuditUser() {
    return auditUser;
  }

  public void setAuditUser(String auditUser) {
    this.auditUser = auditUser;
  }

  public QueryShieldModel reason(String reason) {
    this.reason = reason;
    return this;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public QueryShieldModel startTime(String startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public QueryShieldModel endTime(String endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public QueryShieldModel pageNum(Integer pageNum) {
    this.pageNum = pageNum;
    return this;
  }

  public Integer getPageNum() {
    return pageNum;
  }

  public void setPageNum(Integer pageNum) {
    this.pageNum = pageNum;
  }

  public QueryShieldModel pageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryShieldModel queryShieldModel = (QueryShieldModel) o;
    return Objects.equals(this.projectName, queryShieldModel.projectName) &&
        Objects.equals(this.repoName, queryShieldModel.repoName) &&
        Objects.equals(this.type, queryShieldModel.type) &&
        Objects.equals(this.defectCheckerName, queryShieldModel.defectCheckerName) &&
        Objects.equals(this.shieldType, queryShieldModel.shieldType) &&
        Objects.equals(this.applyUser, queryShieldModel.applyUser) &&
        Objects.equals(this.auditUser, queryShieldModel.auditUser) &&
        Objects.equals(this.reason, queryShieldModel.reason) &&
        Objects.equals(this.startTime, queryShieldModel.startTime) &&
        Objects.equals(this.endTime, queryShieldModel.endTime) &&
        Objects.equals(this.pageNum, queryShieldModel.pageNum) &&
        Objects.equals(this.pageSize, queryShieldModel.pageSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectName, repoName, type, defectCheckerName, shieldType, applyUser, auditUser, reason, startTime, endTime, pageNum, pageSize);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryShieldModel {\n");
    
    sb.append("    projectName: ").append(toIndentedString(projectName)).append("\n");
    sb.append("    repoName: ").append(toIndentedString(repoName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    defectCheckerName: ").append(toIndentedString(defectCheckerName)).append("\n");
    sb.append("    shieldType: ").append(toIndentedString(shieldType)).append("\n");
    sb.append("    applyUser: ").append(toIndentedString(applyUser)).append("\n");
    sb.append("    auditUser: ").append(toIndentedString(auditUser)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
    sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
    sb.append("    pageNum: ").append(toIndentedString(pageNum)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

