package nc.vo.forest.track;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

public class RiskSpreadTrackVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_RISK_TRACK = "pk_risk_track";
    public static final String PK_FOREST_TRAP = "pk_forest_trap";
    public static final String PK_TRAP_RECORD = "pk_trap_record";
    public static final String TRACK_DATE = "track_date";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String RISK_LEVEL = "risk_level";
    public static final String RISK_TREND = "risk_trend";
    public static final String INSECT_TYPE = "insect_type";
    public static final String INSECT_COUNT = "insect_count";
    public static final String FOREST_BLOCK = "forest_block";
    public static final String TREE_SPECIES = "tree_species";
    public static final String SPREAD_RADIUS = "spread_radius";
    public static final String TRACK_REMARK = "track_remark";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int TREND_STABLE = 1;
    public static final int TREND_UP = 2;
    public static final int TREND_DOWN = 3;
    public static final int TREND_SPREAD = 4;

    private String pk_risk_track;
    private String pk_forest_trap;
    private String pk_trap_record;
    private UFDate track_date;
    private UFDouble longitude;
    private UFDouble latitude;
    private Integer risk_level;
    private Integer risk_trend;
    private String insect_type;
    private Integer insect_count;
    private String forest_block;
    private String tree_species;
    private UFDouble spread_radius;
    private String track_remark;
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
        return "forest_risk_spread_track";
    }

    @Override
    public String getPKFieldName() {
        return PK_RISK_TRACK;
    }

    @Override
    public String getParentPKFieldName() {
        return PK_FOREST_TRAP;
    }

    public String getPk_risk_track() {
        return pk_risk_track;
    }

    public void setPk_risk_track(String pk_risk_track) {
        this.pk_risk_track = pk_risk_track;
    }

    public String getPk_forest_trap() {
        return pk_forest_trap;
    }

    public void setPk_forest_trap(String pk_forest_trap) {
        this.pk_forest_trap = pk_forest_trap;
    }

    public String getPk_trap_record() {
        return pk_trap_record;
    }

    public void setPk_trap_record(String pk_trap_record) {
        this.pk_trap_record = pk_trap_record;
    }

    public UFDate getTrack_date() {
        return track_date;
    }

    public void setTrack_date(UFDate track_date) {
        this.track_date = track_date;
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

    public Integer getRisk_level() {
        return risk_level;
    }

    public void setRisk_level(Integer risk_level) {
        this.risk_level = risk_level;
    }

    public Integer getRisk_trend() {
        return risk_trend;
    }

    public void setRisk_trend(Integer risk_trend) {
        this.risk_trend = risk_trend;
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

    public String getForest_block() {
        return forest_block;
    }

    public void setForest_block(String forest_block) {
        this.forest_block = forest_block;
    }

    public String getTree_species() {
        return tree_species;
    }

    public void setTree_species(String tree_species) {
        this.tree_species = tree_species;
    }

    public UFDouble getSpread_radius() {
        return spread_radius;
    }

    public void setSpread_radius(UFDouble spread_radius) {
        this.spread_radius = spread_radius;
    }

    public String getTrack_remark() {
        return track_remark;
    }

    public void setTrack_remark(String track_remark) {
        this.track_remark = track_remark;
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
