package nc.vo.forest.trap;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

public class ForestTrapVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_FOREST_TRAP = "pk_forest_trap";
    public static final String TRAP_CODE = "trap_code";
    public static final String TRAP_NAME = "trap_name";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String LOCATION_DESC = "location_desc";
    public static final String FOREST_TYPE = "forest_type";
    public static final String TRAP_TYPE = "trap_type";
    public static final String INSTALL_DATE = "install_date";
    public static final String IS_KEY_PATROL = "is_key_patrol";
    public static final String KEY_PATROL_REASON = "key_patrol_reason";
    public static final String PK_RANGER = "pk_ranger";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int NOT_KEY_PATROL = 0;
    public static final int IS_KEY_PATROL_YES = 1;

    private String pk_forest_trap;
    private String trap_code;
    private String trap_name;
    private UFDouble longitude;
    private UFDouble latitude;
    private String location_desc;
    private String forest_type;
    private String trap_type;
    private UFDate install_date;
    private Integer is_key_patrol = 0;
    private String key_patrol_reason;
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
        return "forest_trap";
    }

    @Override
    public String getPKFieldName() {
        return PK_FOREST_TRAP;
    }

    @Override
    public String getParentPKFieldName() {
        return null;
    }

    public String getPk_forest_trap() {
        return pk_forest_trap;
    }

    public void setPk_forest_trap(String pk_forest_trap) {
        this.pk_forest_trap = pk_forest_trap;
    }

    public String getTrap_code() {
        return trap_code;
    }

    public void setTrap_code(String trap_code) {
        this.trap_code = trap_code;
    }

    public String getTrap_name() {
        return trap_name;
    }

    public void setTrap_name(String trap_name) {
        this.trap_name = trap_name;
    }

    public UFDouble getLongitude() {
        return longitude;
    }

    public void setLongitude(UFDouble longitude) {
        this.longitude = longitude;
    }

    public UFDouble getLatitude() {
        return latitude;
    }

    public void setLatitude(UFDouble latitude) {
        this.latitude = latitude;
    }

    public String getLocation_desc() {
        return location_desc;
    }

    public void setLocation_desc(String location_desc) {
        this.location_desc = location_desc;
    }

    public String getForest_type() {
        return forest_type;
    }

    public void setForest_type(String forest_type) {
        this.forest_type = forest_type;
    }

    public String getTrap_type() {
        return trap_type;
    }

    public void setTrap_type(String trap_type) {
        this.trap_type = trap_type;
    }

    public UFDate getInstall_date() {
        return install_date;
    }

    public void setInstall_date(UFDate install_date) {
        this.install_date = install_date;
    }

    public Integer getIs_key_patrol() {
        return is_key_patrol;
    }

    public void setIs_key_patrol(Integer is_key_patrol) {
        this.is_key_patrol = is_key_patrol;
    }

    public String getKey_patrol_reason() {
        return key_patrol_reason;
    }

    public void setKey_patrol_reason(String key_patrol_reason) {
        this.key_patrol_reason = key_patrol_reason;
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
