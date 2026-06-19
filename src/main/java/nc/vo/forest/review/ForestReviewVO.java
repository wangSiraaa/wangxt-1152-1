package nc.vo.forest.review;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

public class ForestReviewVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_FOREST_REVIEW = "pk_forest_review";
    public static final String PK_TRAP_RECORD = "pk_trap_record";
    public static final String REVIEW_DATE = "review_date";
    public static final String RISK_LEVEL = "risk_level";
    public static final String IS_QUARANTINE = "is_quarantine";
    public static final String REVIEW_REMARK = "review_remark";
    public static final String REVIEW_RESULT = "review_result";
    public static final String IS_ALLOW_DISPOSAL = "is_allow_disposal";
    public static final String PK_QUARANTINE = "pk_quarantine";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int NOT_QUARANTINE = 0;
    public static final int IS_QUARANTINE_YES = 1;

    public static final int NOT_ALLOW_DISPOSAL = 0;
    public static final int ALLOW_DISPOSAL = 1;

    public static final int RISK_LOW = 1;
    public static final int RISK_MEDIUM = 2;
    public static final int RISK_HIGH = 3;

    private String pk_forest_review;
    private String pk_trap_record;
    private UFDate review_date;
    private Integer risk_level;
    private Integer is_quarantine = 0;
    private String review_remark;
    private String review_result;
    private Integer is_allow_disposal = 0;
    private String pk_quarantine;
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
        return "forest_review";
    }

    @Override
    public String getPKFieldName() {
        return PK_FOREST_REVIEW;
    }

    @Override
    public String getParentPKFieldName() {
        return null;
    }

    public String getPk_forest_review() {
        return pk_forest_review;
    }

    public void setPk_forest_review(String pk_forest_review) {
        this.pk_forest_review = pk_forest_review;
    }

    public String getPk_trap_record() {
        return pk_trap_record;
    }

    public void setPk_trap_record(String pk_trap_record) {
        this.pk_trap_record = pk_trap_record;
    }

    public UFDate getReview_date() {
        return review_date;
    }

    public void setReview_date(UFDate review_date) {
        this.review_date = review_date;
    }

    public Integer getRisk_level() {
        return risk_level;
    }

    public void setRisk_level(Integer risk_level) {
        this.risk_level = risk_level;
    }

    public Integer getIs_quarantine() {
        return is_quarantine;
    }

    public void setIs_quarantine(Integer is_quarantine) {
        this.is_quarantine = is_quarantine;
    }

    public String getReview_remark() {
        return review_remark;
    }

    public void setReview_remark(String review_remark) {
        this.review_remark = review_remark;
    }

    public String getReview_result() {
        return review_result;
    }

    public void setReview_result(String review_result) {
        this.review_result = review_result;
    }

    public Integer getIs_allow_disposal() {
        return is_allow_disposal;
    }

    public void setIs_allow_disposal(Integer is_allow_disposal) {
        this.is_allow_disposal = is_allow_disposal;
    }

    public String getPk_quarantine() {
        return pk_quarantine;
    }

    public void setPk_quarantine(String pk_quarantine) {
        this.pk_quarantine = pk_quarantine;
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
