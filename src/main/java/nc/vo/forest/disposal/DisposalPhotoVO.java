package nc.vo.forest.disposal;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;

public class DisposalPhotoVO extends SuperVO {
    private static final long serialVersionUID = 1L;

    public static final String PK_DISPOSAL_PHOTO = "pk_disposal_photo";
    public static final String PK_FOREST_DISPOSAL = "pk_forest_disposal";
    public static final String PHOTO_URL = "photo_url";
    public static final String PHOTO_TYPE = "photo_type";
    public static final String PHOTO_REMARK = "photo_remark";
    public static final String UPLOAD_DATE = "upload_date";
    public static final String PK_ORG = "pk_org";
    public static final String PK_GROUP = "pk_group";
    public static final String CREATOR = "creator";
    public static final String CREATIONTIME = "creationtime";
    public static final String DR = "dr";
    public static final String TS = "ts";

    private String pk_disposal_photo;
    private String pk_forest_disposal;
    private String photo_url;
    private String photo_type;
    private String photo_remark;
    private UFDateTime upload_date;
    private String pk_org;
    private String pk_group;
    private String creator;
    private UFDateTime creationtime;
    private Integer dr = 0;
    private UFDateTime ts;

    @Override
    public String getTableName() {
        return "forest_disposal_photo";
    }

    @Override
    public String getPKFieldName() {
        return PK_DISPOSAL_PHOTO;
    }

    @Override
    public String getParentPKFieldName() {
        return PK_FOREST_DISPOSAL;
    }

    public String getPk_disposal_photo() {
        return pk_disposal_photo;
    }

    public void setPk_disposal_photo(String pk_disposal_photo) {
        this.pk_disposal_photo = pk_disposal_photo;
    }

    public String getPk_forest_disposal() {
        return pk_forest_disposal;
    }

    public void setPk_forest_disposal(String pk_forest_disposal) {
        this.pk_forest_disposal = pk_forest_disposal;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getPhoto_type() {
        return photo_type;
    }

    public void setPhoto_type(String photo_type) {
        this.photo_type = photo_type;
    }

    public String getPhoto_remark() {
        return photo_remark;
    }

    public void setPhoto_remark(String photo_remark) {
        this.photo_remark = photo_remark;
    }

    public UFDateTime getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(UFDateTime upload_date) {
        this.upload_date = upload_date;
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
