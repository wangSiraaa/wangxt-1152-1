package nc.vo.forest.recheck;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

public class RecheckPlanVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_RECHECK_PLAN = "pk_recheck_plan";
    public static final String PK_FOREST_DISPOSAL = "pk_forest_disposal";
    public static final String PK_TRAP_RECORD = "pk_trap_record";
    public static final String PK_FOREST_TRAP = "pk_forest_trap";
    public static final String PLAN_DATE = "plan_date";
    public static final String RECHECK_TYPE = "recheck_type";
    public static final String RECHECK_STATUS = "recheck_status";
    public static final String ACTUAL_DATE = "actual_date";
    public static final String RECHECK_RESULT = "recheck_result";
    public static final String RECHECK_REMARK = "recheck_remark";
    public static final String PK_RANGER = "pk_ranger";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int TYPE_FIRST = 1;
    public static final int TYPE_SECOND = 2;
    public static final int TYPE_THIRD = 3;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_CANCELLED = 2;

    public static final int RESULT_NORMAL = 1;
    public static final int RESULT_SUSPECT = 2;
    public static final int RESULT_SERIOUS = 3;

    private String pk_recheck_plan;
    private String pk_forest_disposal;
    private String pk_trap_record;
    private String pk_forest_trap;
    private UFDate plan_date;
    private Integer recheck_type;
    private Integer recheck_status = 0;
    private UFDate actual_date;
    private Integer recheck_result;
    private String recheck_remark;
    private String pk_ranger;
    private String pk_org;
    private String pk_group;
    private String creator;
    private UFDateTime creationtime;
    private String modifier;
    private UFDateTime modifiedtime;
    private Integer dr = 0;
    private UFDateTime ts;

    @Override
    public String getTableName() {
        return "forest_recheck_plan";
    }

    @Override
    public String getPKFieldName() {
        return PK_RECHECK_PLAN;
    }

    @Override
    public String getParentPKFieldName() {
        return PK_FOREST_DISPOSAL;
    }

    public String getPk_recheck_plan() {
        return pk_recheck_plan;
    }

    public void setPk_recheck_plan(String pk_recheck_plan) {
        this.pk_recheck_plan = pk_recheck_plan;
    }

    public String getPk_forest_disposal() {
        return pk_forest_disposal;
    }

    public void setPk_forest_disposal(String pk_forest_disposal) {
        this.pk_forest_disposal = pk_forest_disposal;
    }

    public String getPk_trap_record() {
        return pk_trap_record;
    }

    public void setPk_trap_record(String pk_trap_record) {
        this.pk_trap_record = pk_trap_record;
    }

    public String getPk_forest_trap() {
        return pk_forest_trap;
    }

    public void setPk_forest_trap(String pk_forest_trap) {
        this.pk_forest_trap = pk_forest_trap;
    }

    public UFDate getPlan_date() {
        return plan_date;
    }

    public void setPlan_date(UFDate plan_date) {
        this.plan_date = plan_date;
    }

    public Integer getRecheck_type() {
        return recheck_type;
    }

    public void setRecheck_type(Integer recheck_type) {
        this.recheck_type = recheck_type;
    }

    public Integer getRecheck_status() {
        return recheck_status;
    }

    public void setRecheck_status(Integer recheck_status) {
        this.recheck_status = recheck_status;
    }

    public UFDate getActual_date() {
        return actual_date;
    }

    public void setActual_date(UFDate actual_date) {
        this.actual_date = actual_date;
    }

    public Integer getRecheck_result() {
        return recheck_result;
    }

    public void setRecheck_result(Integer recheck_result) {
        this.recheck_result = recheck_result;
    }

    public String getRecheck_remark() {
        return recheck_remark;
    }

    public void setRecheck_remark(String recheck_remark) {
        this.recheck_remark = recheck_remark;
    }

    public String getPk_ranger() {
        return pk_ranger;
    }

    public void setPk_ranger(String pk_ranger) {
        this.pk_ranger = pk_ranger;
    }

    public String getPk_org() {
        return pk_org;
    }

    public void setPk_org(String pk_org) {
        this.pk_org = pk_org;
    }

    public String getPk_group() {
        return pk_group;
    }

    public void setPk_group(String pk_group) {
        this.pk_group = pk_group;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public UFDateTime getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(UFDateTime creationtime) {
        this.creationtime = creationtime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public UFDateTime getModifiedtime() {
        return modifiedtime;
    }

    public void setModifiedtime(UFDateTime modifiedtime) {
        this.modifiedtime = modifiedtime;
    }

    public Integer getDr() {
        return dr;
    }

    public void setDr(Integer dr) {
        this.dr = dr;
    }

    public UFDateTime getTs() {
        return ts;
    }

    public void setTs(UFDateTime ts) {
        this.ts = ts;
    }
}
