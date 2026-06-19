package nc.rule.forest;

import nc.bs.dao.BaseDAO;
import nc.bs.uif2.validation.ValidationException;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.forest.disposal.ForestDisposalVO;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.forest.review.ForestReviewVO;
import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.core.exception.ExceptionUtils;

public class ForestBusinessValidator {

    private static BaseDAO dao = new BaseDAO();

    public static void validateAllowDisposal(String pkTrapRecord) throws BusinessException {
        TrapRecordVO recordVO = queryTrapRecord(pkTrapRecord);
        if (recordVO == null) {
            ExceptionUtils.wrapBusinessException("诱捕记录不存在");
        }

        if (TrapRecordVO.IS_SUSPECT == recordVO.getIs_suspect_quarantine()) {
            ForestReviewVO reviewVO = queryReviewByRecord(pkTrapRecord);
            if (reviewVO == null) {
                ExceptionUtils.wrapBusinessException("疑似检疫对象未经过复核，不能进行清理处置");
            }
            if (ForestReviewVO.ALLOW_DISPOSAL != reviewVO.getIs_allow_disposal()) {
                ExceptionUtils.wrapBusinessException("复核未通过，不允许进行清理处置");
            }
        }

        validateKeyPatrolSecondReview(pkTrapRecord);
    }

    public static void validateCloseDisposal(String pkDisposal) throws BusinessException {
        ForestDisposalVO disposalVO = queryDisposal(pkDisposal);
        if (disposalVO == null) {
            ExceptionUtils.wrapBusinessException("处置记录不存在");
        }

        if (ForestDisposalVO.NO_PHOTO == disposalVO.getHas_photo()) {
            ExceptionUtils.wrapBusinessException("疫木清理未拍照，不能关闭处置单");
        }
        if (disposalVO.getDisposal_longitude() == null || disposalVO.getDisposal_latitude() == null) {
            ExceptionUtils.wrapBusinessException("疫木清理必须上传位置信息（经纬度），不能关闭处置单");
        }
        if (disposalVO.getDisposal_method() == null || disposalVO.getDisposal_method().trim().isEmpty()) {
            ExceptionUtils.wrapBusinessException("疫木清理必须填写处置方式，不能关闭处置单");
        }
    }

    public static void validateKeyPatrolSecondReview(String pkTrapRecord) throws BusinessException {
        TrapRecordVO recordVO = queryTrapRecord(pkTrapRecord);
        if (recordVO == null) {
            return;
        }

        String pkForestTrap = recordVO.getPk_forest_trap();
        ForestTrapVO trapVO = queryForestTrap(pkForestTrap);
        if (trapVO == null || ForestTrapVO.NOT_KEY_PATROL == trapVO.getIs_key_patrol()) {
            return;
        }

        int reviewCount = countReviewsByRecord(pkTrapRecord);
        if (reviewCount < 2) {
            ExceptionUtils.wrapBusinessException("该点位为重点巡查区域，必须经过检疫员二次复核后方可进行处置");
        }
    }

    public static void validateTrapRecordStatus(String pkTrapRecord, int expectedStatus) throws BusinessException {
        TrapRecordVO recordVO = queryTrapRecord(pkTrapRecord);
        if (recordVO == null) {
            ExceptionUtils.wrapBusinessException("诱捕记录不存在");
        }
        if (expectedStatus != recordVO.getRecord_status()) {
            String statusDesc = getStatusDesc(expectedStatus);
            ExceptionUtils.wrapBusinessException("诱捕记录状态不正确，当前状态应为：" + statusDesc);
        }
    }

    private static String getStatusDesc(int status) {
        switch (status) {
            case TrapRecordVO.STATUS_PENDING_REVIEW:
                return "待复核";
            case TrapRecordVO.STATUS_REVIEWED:
                return "已复核";
            case TrapRecordVO.STATUS_DISPOSED:
                return "已处置";
            default:
                return "未知状态";
        }
    }

    private static TrapRecordVO queryTrapRecord(String pkTrapRecord) throws BusinessException {
        String sql = "select * from forest_trap_record where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        return (TrapRecordVO) dao.executeQuery(sql, param, new nc.jdbc.framework.processor.BeanProcessor(TrapRecordVO.class));
    }

    private static ForestReviewVO queryReviewByRecord(String pkTrapRecord) throws BusinessException {
        String sql = "select * from forest_review where pk_trap_record = ? and dr = 0 order by creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        return (ForestReviewVO) dao.executeQuery(sql, param, new nc.jdbc.framework.processor.BeanProcessor(ForestReviewVO.class));
    }

    private static ForestDisposalVO queryDisposal(String pkDisposal) throws BusinessException {
        String sql = "select * from forest_disposal where pk_forest_disposal = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkDisposal);
        return (ForestDisposalVO) dao.executeQuery(sql, param, new nc.jdbc.framework.processor.BeanProcessor(ForestDisposalVO.class));
    }

    public static int countContinuousHighRisk(String pkForestTrap) throws BusinessException {
        String sql = "select count(*) from ("
                + "select risk_level from forest_trap_record "
                + "where pk_forest_trap = ? and dr = 0 and risk_level is not null "
                + "order by record_date desc, creationtime desc "
                + "limit 3) t "
                + "where t.risk_level = ?";
        SQLParameter param = new SQLParameter();
        param.addParam(pkForestTrap);
        param.addParam(TrapRecordVO.RISK_HIGH);
        Object result = dao.executeQuery(sql, param, new ColumnProcessor());
        if (result == null) {
            return 0;
        }
        return ((Number) result).intValue();
    }

    private static ForestTrapVO queryForestTrap(String pkForestTrap) throws BusinessException {
        if (pkForestTrap == null) {
            return null;
        }
        String sql = "select * from forest_trap where pk_forest_trap = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkForestTrap);
        return (ForestTrapVO) dao.executeQuery(sql, param, new nc.jdbc.framework.processor.BeanProcessor(ForestTrapVO.class));
    }

    private static int countReviewsByRecord(String pkTrapRecord) throws BusinessException {
        String sql = "select count(*) from forest_review where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        Object result = dao.executeQuery(sql, param, new ColumnProcessor());
        if (result == null) {
            return 0;
        }
        return ((Number) result).intValue();
    }
}
