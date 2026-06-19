package nc.vo.forest.record;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

public class TrapRecordVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_TRAP_RECORD = "pk_trap_record";
    public static final String PK_FOREST_TRAP = "pk_forest_trap";
    public static final String RECORD_DATE = "record_date";
    public static final String INSECT_TYPE = "insect_type";
    public static final String INSECT_COUNT = "insect_count";
    public static final String IS_SUSPECT_QUARANTINE = "is_suspect_quarantine";
    public static final String SUSPECT_REMARK = "suspect_remark";
    public static final String RECORD_STATUS = "record_status";
    public static final String RISK_LEVEL = "risk_level";
    public static final String PK_RANGER = "pk_ranger";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int NOT_SUSPECT = 0;
    public static final int IS_SUSPECT = 1;

    public static final int STATUS_PENDING_REVIEW = 0;
    public static final int STATUS_REVIEWED = 1;
    public static final int STATUS_DISPOSED = 2;

    public static final int RISK_LOW = 1;
    public static final int RISK_MEDIUM = 2;
    public static final int RISK_HIGH = 3;

    private String pk_trap_record;
    private String pk_forest_trap;
    private UFDate record_date;
    private String insect_type;
    private Integer insect_count;
    private Integer is_suspect_quarantine = 0;
    private String suspect_remark;
    private Integer record_status = 0;
    private Integer risk_level;
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
        return "forest_trap_record";
    }

    @Override
    public String getPKFieldName() {
        return PK_TRAP_RECORD;
    }

    @Override
    public String getParentPKFieldName() {
        return null;
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

    public UFDate getRecord_date() {
        return record_date;
    }

    public void setRecord_date(UFDate record_date) {
        this.record_date = record_date;
    }

    public String getInsect_type() {
        return insect_type;
    }

    public void setInsect_type(String insect_type) {
        this.insect_type = insect_type;
    }

    public Integer getInsect_count() {
        return insect_count;
    }

    public void setInsect_count(Integer insect_count) {
        this.insect_count = insect_count;
    }

    public Integer getIs_suspect_quarantine() {
        return is_suspect_quarantine;
    }

    public void setIs_suspect_quarantine(Integer is_suspect_quarantine) {
        this.is_suspect_quarantine = is_suspect_quarantine;
    }

    public String getSuspect_remark() {
        return suspect_remark;
    }

    public void setSuspect_remark(String suspect_remark) {
        this.suspect_remark = suspect_remark;
    }

    public Integer getRecord_status() {
        return record_status;
    }

    public void setRecord_status(Integer record_status) {
        this.record_status = record_status;
    }

    public Integer getRisk_level() {
        return risk_level;
    }

    public void setRisk_level(Integer risk_level) {
        this.risk_level = risk_level;
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
