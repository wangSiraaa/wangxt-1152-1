package nc.vo.forest.user;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;

public class ForestUserVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_FOREST_USER = "pk_forest_user";
    public static final String USER_CODE = "user_code";
    public static final String USER_NAME = "user_name";
    public static final String USER_ROLE = "user_role";
    public static final String PHONE = "phone";
    public static final String ID_CARD = "id_card";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String MODIFIER = "modifier";
    public static final String MODIFIEDTIME = "modifiedtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    public static final int ROLE_RANGER = 1;
    public static final int ROLE_QUARANTINE = 2;
    public static final int ROLE_DISPOSAL = 3;

    private String pk_forest_user;
    private String user_code;
    private String user_name;
    private Integer user_role;
    private String phone;
    private String id_card;
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
        return "forest_user";
    }

    @Override
    public String getPKFieldName() {
        return PK_FOREST_USER;
    }

    @Override
    public String getParentPKFieldName() {
        return null;
    }

    public String getPk_forest_user() {
        return pk_forest_user;
    }

    public void setPk_forest_user(String pk_forest_user) {
        this.pk_forest_user = pk_forest_user;
    }

    public String getUser_code() {
        return user_code;
    }

    public void setUser_code(String user_code) {
        this.user_code = user_code;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public Integer getUser_role() {
        return user_role;
    }

    public void setUser_role(Integer user_role) {
        this.user_role = user_role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId_card() {
        return id_card;
    }

    public void setId_card(String id_card) {
        this.id_card = id_card;
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
