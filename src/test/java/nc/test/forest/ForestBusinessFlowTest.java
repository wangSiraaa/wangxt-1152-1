package nc.test.forest;

import nc.vo.forest.disposal.DisposalPhotoVO;
import nc.vo.forest.disposal.ForestDisposalVO;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.forest.review.ForestReviewVO;
import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.forest.user.ForestUserVO;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.Assert.*;

public class ForestBusinessFlowTest {

    @Test
    public void testFullBusinessFlow() throws Exception {
        ForestUserVO ranger = createUser("user001", "张护林", ForestUserVO.ROLE_RANGER);
        ForestUserVO quarantine = createUser("user002", "李检疫", ForestUserVO.ROLE_QUARANTINE);
        ForestUserVO disposal = createUser("user003", "王处置", ForestUserVO.ROLE_DISPOSAL);

        assertNotNull(ranger);
        assertEquals(ForestUserVO.ROLE_RANGER, ranger.getRole_type().intValue());
        assertEquals(ForestUserVO.ROLE_QUARANTINE, quarantine.getRole_type().intValue());
        assertEquals(ForestUserVO.ROLE_DISPOSAL, disposal.getRole_type().intValue());

        ForestTrapVO trap = createTrap("trap001", 116.397, 39.908, "东城区山林");
        assertNotNull(trap);
        assertEquals(116.397, trap.getLongitude().doubleValue(), 0.001);
        assertEquals(39.908, trap.getLatitude().doubleValue(), 0.001);
        assertEquals(ForestTrapVO.NOT_KEY_PATROL, trap.getIs_key_patrol().intValue());

        TrapRecordVO record = createTrapRecord("rec001", trap.getPk_forest_trap(),
                ranger.getUser_code(), TrapRecordVO.IS_SUSPECT, 15, TrapRecordVO.STATUS_PENDING_REVIEW);
        assertNotNull(record);
        assertEquals(TrapRecordVO.IS_SUSPECT, record.getIs_suspect_quarantine().intValue());
        assertEquals(15, record.getInsect_count().intValue());
        assertEquals(TrapRecordVO.STATUS_PENDING_REVIEW, record.getRecord_status().intValue());

        ForestReviewVO review = createReview("rev001", record.getPk_trap_record(),
                quarantine.getUser_code(), TrapRecordVO.RISK_HIGH, ForestReviewVO.ALLOW_DISPOSAL);
        assertNotNull(review);
        assertEquals(TrapRecordVO.RISK_HIGH, review.getRisk_level().intValue());
        assertEquals(ForestReviewVO.ALLOW_DISPOSAL, review.getIs_allow_disposal().intValue());

        ForestDisposalVO disposalVO = createDisposal("disp001", record.getPk_trap_record(),
                disposal.getUser_code(), ForestDisposalVO.NO_PHOTO, ForestDisposalVO.STATUS_PROCESSING);
        assertNotNull(disposalVO);
        assertEquals(ForestDisposalVO.NO_PHOTO, disposalVO.getHas_photo().intValue());
        assertEquals(ForestDisposalVO.STATUS_PROCESSING, disposalVO.getDisposal_status().intValue());

        DisposalPhotoVO photo = createPhoto("photo001", disposalVO.getPk_forest_disposal(), "http://example.com/photo1.jpg");
        assertNotNull(photo);
        assertEquals("http://example.com/photo1.jpg", photo.getPhoto_url());

        disposalVO.setHas_photo(ForestDisposalVO.HAS_PHOTO_YES);
        disposalVO.setDisposal_status(ForestDisposalVO.STATUS_COMPLETED);
        assertEquals(ForestDisposalVO.HAS_PHOTO_YES, disposalVO.getHas_photo().intValue());
        assertEquals(ForestDisposalVO.STATUS_COMPLETED, disposalVO.getDisposal_status().intValue());
    }

    @Test
    public void testContinuousHighRiskFlow() throws Exception {
        ForestTrapVO trap = createTrap("trap002", 116.400, 39.910, "西城区山林");
        trap.setPk_forest_trap("trap_pk_002");

        for (int i = 0; i < 3; i++) {
            TrapRecordVO record = createTrapRecord("rec_high_" + i, trap.getPk_forest_trap(),
                    "ranger001", TrapRecordVO.NOT_SUSPECT, 20 + i, TrapRecordVO.STATUS_REVIEWED);
            record.setRisk_level(TrapRecordVO.RISK_HIGH);
            assertEquals(TrapRecordVO.RISK_HIGH, record.getRisk_level().intValue());
        }

        trap.setIs_key_patrol(ForestTrapVO.IS_KEY_PATROL);
        assertEquals(ForestTrapVO.IS_KEY_PATROL, trap.getIs_key_patrol().intValue());
    }

    @Test
    public void testStatusEnums() {
        assertEquals(0, TrapRecordVO.STATUS_PENDING_REVIEW);
        assertEquals(1, TrapRecordVO.STATUS_REVIEWED);
        assertEquals(2, TrapRecordVO.STATUS_DISPOSED);

        assertEquals(1, TrapRecordVO.RISK_LOW);
        assertEquals(2, TrapRecordVO.RISK_MEDIUM);
        assertEquals(3, TrapRecordVO.RISK_HIGH);

        assertEquals(0, ForestDisposalVO.NO_PHOTO);
        assertEquals(1, ForestDisposalVO.HAS_PHOTO_YES);

        assertEquals(0, ForestDisposalVO.STATUS_PENDING);
        assertEquals(1, ForestDisposalVO.STATUS_PROCESSING);
        assertEquals(2, ForestDisposalVO.STATUS_COMPLETED);

        assertEquals(0, ForestReviewVO.NOT_ALLOW_DISPOSAL);
        assertEquals(1, ForestReviewVO.ALLOW_DISPOSAL);
    }

    @Test
    public void testVOTableNames() throws Exception {
        ForestUserVO userVO = new ForestUserVO();
        assertEquals("forest_user", userVO.getTableName());

        ForestTrapVO trapVO = new ForestTrapVO();
        assertEquals("forest_trap", trapVO.getTableName());

        TrapRecordVO recordVO = new TrapRecordVO();
        assertEquals("forest_trap_record", recordVO.getTableName());

        ForestReviewVO reviewVO = new ForestReviewVO();
        assertEquals("forest_review", reviewVO.getTableName());

        ForestDisposalVO disposalVO = new ForestDisposalVO();
        assertEquals("forest_disposal", disposalVO.getTableName());

        DisposalPhotoVO photoVO = new DisposalPhotoVO();
        assertEquals("forest_disposal_photo", photoVO.getTableName());
        assertEquals("pk_forest_disposal", photoVO.getParentPKFieldName());
    }

    @Test
    public void testAuditFields() throws Exception {
        ForestUserVO userVO = createUser("user004", "测试用户", ForestUserVO.ROLE_RANGER);

        Field creatorField = ForestUserVO.class.getDeclaredField("creator");
        creatorField.setAccessible(true);
        creatorField.set(userVO, "test_creator");

        Field creationTimeField = ForestUserVO.class.getDeclaredField("creationtime");
        creationTimeField.setAccessible(true);
        creationTimeField.set(userVO, new nc.vo.pub.lang.UFDateTime());

        Field modifierField = ForestUserVO.class.getDeclaredField("modifier");
        modifierField.setAccessible(true);
        modifierField.set(userVO, "test_modifier");

        Field modifiedTimeField = ForestUserVO.class.getDeclaredField("modifiedtime");
        modifiedTimeField.setAccessible(true);
        modifiedTimeField.set(userVO, new nc.vo.pub.lang.UFDateTime());

        Field drField = ForestUserVO.class.getDeclaredField("dr");
        drField.setAccessible(true);
        drField.set(userVO, 0);

        assertEquals("test_creator", userVO.getCreator());
        assertNotNull(userVO.getCreationtime());
        assertEquals("test_modifier", userVO.getModifier());
        assertNotNull(userVO.getModifiedtime());
        assertEquals(0, userVO.getDr().intValue());
    }

    private ForestUserVO createUser(String code, String name, int roleType) {
        ForestUserVO user = new ForestUserVO();
        user.setUser_code(code);
        user.setUser_name(name);
        user.setRole_type(roleType);
        user.setPk_group("group001");
        user.setPk_org("org001");
        user.setCreator("system");
        user.setCreationtime(new nc.vo.pub.lang.UFDateTime());
        user.setDr(0);
        return user;
    }

    private ForestTrapVO createTrap(String code, double longitude, double latitude, String location) {
        ForestTrapVO trap = new ForestTrapVO();
        trap.setTrap_code(code);
        trap.setTrap_name("诱捕器-" + code);
        trap.setLongitude(new nc.vo.pub.lang.UFDouble(longitude));
        trap.setLatitude(new nc.vo.pub.lang.UFDouble(latitude));
        trap.setLocation_desc(location);
        trap.setIs_key_patrol(ForestTrapVO.NOT_KEY_PATROL);
        trap.setPk_group("group001");
        trap.setPk_org("org001");
        trap.setCreator("system");
        trap.setCreationtime(new nc.vo.pub.lang.UFDateTime());
        trap.setDr(0);
        return trap;
    }

    private TrapRecordVO createTrapRecord(String code, String pkTrap, String pkRanger,
                                           int isSuspect, int insectCount, int status) {
        TrapRecordVO record = new TrapRecordVO();
        record.setRecord_code(code);
        record.setPk_forest_trap(pkTrap);
        record.setPk_ranger(pkRanger);
        record.setRecord_date(new nc.vo.pub.lang.UFDate());
        record.setIs_suspect_quarantine(isSuspect);
        record.setInsect_count(insectCount);
        record.setRecord_status(status);
        record.setPk_group("group001");
        record.setPk_org("org001");
        record.setCreator(pkRanger);
        record.setCreationtime(new nc.vo.pub.lang.UFDateTime());
        record.setDr(0);
        return record;
    }

    private ForestReviewVO createReview(String code, String pkRecord, String pkQuarantine,
                                         int riskLevel, int allowDisposal) {
        ForestReviewVO review = new ForestReviewVO();
        review.setReview_code(code);
        review.setPk_trap_record(pkRecord);
        review.setPk_quarantine(pkQuarantine);
        review.setReview_date(new nc.vo.pub.lang.UFDate());
        review.setRisk_level(riskLevel);
        review.setIs_quarantine(ForestReviewVO.IS_QUARANTINE);
        review.setIs_allow_disposal(allowDisposal);
        review.setPk_group("group001");
        review.setPk_org("org001");
        review.setCreator(pkQuarantine);
        review.setCreationtime(new nc.vo.pub.lang.UFDateTime());
        review.setDr(0);
        return review;
    }

    private ForestDisposalVO createDisposal(String code, String pkRecord, String pkTeam,
                                             int hasPhoto, int status) {
        ForestDisposalVO disposal = new ForestDisposalVO();
        disposal.setPk_trap_record(pkRecord);
        disposal.setDisposal_date(new nc.vo.pub.lang.UFDate());
        disposal.setDisposal_type("疫木清理");
        disposal.setDisposal_method("焚烧处理");
        disposal.setDisposal_area(new nc.vo.pub.lang.UFDouble("100.5"));
        disposal.setTree_count(5);
        disposal.setHas_photo(hasPhoto);
        disposal.setDisposal_status(status);
        disposal.setPk_disposal_team(pkTeam);
        disposal.setPk_group("group001");
        disposal.setPk_org("org001");
        disposal.setCreator(pkTeam);
        disposal.setCreationtime(new nc.vo.pub.lang.UFDateTime());
        disposal.setDr(0);
        return disposal;
    }

    private DisposalPhotoVO createPhoto(String code, String pkDisposal, String photoUrl) {
        DisposalPhotoVO photo = new DisposalPhotoVO();
        photo.setPk_forest_disposal(pkDisposal);
        photo.setPhoto_url(photoUrl);
        photo.setPhoto_type("清理现场照");
        photo.setUpload_date(new nc.vo.pub.lang.UFDateTime());
        photo.setPk_group("group001");
        photo.setPk_org("org001");
        photo.setCreator("system");
        photo.setCreationtime(new nc.vo.pub.lang.UFDateTime());
        photo.setDr(0);
        return photo;
    }
}
