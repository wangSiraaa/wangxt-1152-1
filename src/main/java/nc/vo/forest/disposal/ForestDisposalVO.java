package nc.vo.forest.disposal;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

public class ForestDisposalVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_FOREST_DISPOSAL = "pk_forest_disposal";
    public static final String PK_TRAP_RECORD = "pk_trap_record";
    public static final String DISPOSAL_DATE = "disposal_date";
    public static final String DISPOSAL_LONGITUDE = "disposal_longitude";
    public static final String DISPOSAL_LATITUDE = "disposal_latitude";
    public static final String DISPOSAL_TYPE = "disposal_type";
    public static final String DISPOSAL_METHOD = "disposal_method";
    public static final String DISPOSAL_AREA = "disposal_area";
    public static final String TREE_COUNT = "tree_count";
    public static final String DISPOSAL_REMARK = "disposal_remark";
    public static final String HAS_PHOTO = "has_photo";
    public static final String DISPOSAL_STATUS = "disposal_status";
    public static final String PK_DISPOSAL_TEAM = "pk_disposal_team";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int NO_PHOTO = 0;
    public static final int HAS_PHOTO_YES = 1;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_COMPLETED = 2;

    private String pk_forest_disposal;
    private String pk_trap_record;
    private UFDate disposal_date;
    private UFDouble disposal_longitude;
    private UFDouble disposal_latitude;
    private String disposal_type;
    private String disposal_method;
    private UFDouble disposal_area;
    private Integer tree_count;
    private String disposal_remark;
    private Integer has_photo = 0;
    private Integer disposal_status = 0;
    private String pk_disposal_team;
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
        return "forest_disposal";
    }

    @Override
    public String getPKFieldName() {
        return PK_FOREST_DISPOSAL;
    }

    @Override
    public String getParentPKFieldName() {
        return null;
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

    public UFDate getDisposal_date() {
        return disposal_date;
    }

    public void setDisposal_date(UFDate disposal_date) {
        this.disposal_date = disposal_date;
    }

    public UFDouble getDisposal_longitude() {
        return disposal_longitude;
    }

    public void setDisposal_longitude(UFDouble disposal_longitude) {
        this.disposal_longitude = disposal_longitude;
    }

    public UFDouble getDisposal_latitude() {
        return disposal_latitude;
    }

    public void setDisposal_latitude(UFDouble disposal_latitude) {
        this.disposal_latitude = disposal_latitude;
    }

    public String getDisposal_type() {
        return disposal_type;
    }

    public void setDisposal_type(String disposal_type) {
        this.disposal_type = disposal_type;
    }

    public String getDisposal_method() {
        return disposal_method;
    }

    public void setDisposal_method(String disposal_method) {
        this.disposal_method = disposal_method;
    }

    public UFDouble getDisposal_area() {
        return disposal_area;
    }

    public void setDisposal_area(UFDouble disposal_area) {
        this.disposal_area = disposal_area;
    }

    public Integer getTree_count() {
        return tree_count;
    }

    public void setTree_count(Integer tree_count) {
        this.tree_count = tree_count;
    }

    public String getDisposal_remark() {
        return disposal_remark;
    }

    public void setDisposal_remark(String disposal_remark) {
        this.disposal_remark = disposal_remark;
    }

    public Integer getHas_photo() {
        return has_photo;
    }

    public void setHas_photo(Integer has_photo) {
        this.has_photo = has_photo;
    }

    public Integer getDisposal_status() {
        return disposal_status;
    }

    public void setDisposal_status(Integer disposal_status) {
        this.disposal_status = disposal_status;
    }

    public String getPk_disposal_team() {
        return pk_disposal_team;
    }

    public void setPk_disposal_team(String pk_disposal_team) {
        this.pk_disposal_team = pk_disposal_team;
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
