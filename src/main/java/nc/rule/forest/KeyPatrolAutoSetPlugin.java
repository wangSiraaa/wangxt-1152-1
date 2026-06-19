package nc.rule.forest;

import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.IBusinessListener;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.jdbc.framework.SQLParameter;
import nc.vo.forest.review.ForestReviewVO;
import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nccloud.framework.core.exception.ExceptionUtils;

public class KeyPatrolAutoSetPlugin implements IBusinessListener {

    private static final int CONTINUOUS_HIGH_RISK_COUNT = 3;

    @Override
    public void doAction(BusinessEvent event) throws BusinessException {
        try {
            Object data = event.getUserObject();
            if (data == null) {
                return;
            }

            String pkTrapRecord = null;
            String pkForestTrap = null;

            if (data instanceof ForestReviewVO) {
                ForestReviewVO reviewVO = (ForestReviewVO) data;
                pkTrapRecord = reviewVO.getPk_trap_record();
                pkForestTrap = getTrapPkByRecord(pkTrapRecord);
            } else if (data instanceof String) {
                pkTrapRecord = (String) data;
                pkForestTrap = getTrapPkByRecord(pkTrapRecord);
            }

            if (pkForestTrap == null) {
                return;
            }

            int highRiskCount = ForestBusinessValidator.countContinuousHighRisk(pkForestTrap);

            if (highRiskCount >= CONTINUOUS_HIGH_RISK_COUNT) {
                autoSetKeyPatrol(pkForestTrap, highRiskCount);
            }
        } catch (Exception e) {
            ExceptionUtils.wrapBusinessException("自动设置重点巡查失败: " + e.getMessage());
        }
    }

    private String getTrapPkByRecord(String pkTrapRecord) throws BusinessException {
        BaseDAO dao = new BaseDAO();
        String sql = "select pk_forest_trap from forest_trap_record where pk_trap_record = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrapRecord);
        Object result = dao.executeQuery(sql, param, new nc.jdbc.framework.processor.ColumnProcessor());
        return result != null ? result.toString() : null;
    }

    private void autoSetKeyPatrol(String pkForestTrap, int highRiskCount) throws BusinessException {
        BaseDAO dao = new BaseDAO();

        String checkSql = "select is_key_patrol from forest_trap where pk_forest_trap = ? and dr = 0";
        SQLParameter checkParam = new SQLParameter();
        checkParam.addParam(pkForestTrap);
        Object currentStatus = dao.executeQuery(checkSql, checkParam, new nc.jdbc.framework.processor.ColumnProcessor());

        if (currentStatus != null && ForestTrapVO.IS_KEY_PATROL_YES == Integer.parseInt(currentStatus.toString())) {
            return;
        }

        String reason = "连续" + highRiskCount + "次高风险，自动进入重点巡查，需检疫员二次复核";
        String sql = "update forest_trap set is_key_patrol = ?, key_patrol_reason = ?, modifier = ?, modifiedtime = ?, ts = ? "
                + "where pk_forest_trap = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(ForestTrapVO.IS_KEY_PATROL_YES);
        param.addParam(reason);
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkForestTrap);

        dao.executeUpdate(sql, param);

        markLatestRecordNeedSecondReview(pkForestTrap);
    }

    private void markLatestRecordNeedSecondReview(String pkForestTrap) throws BusinessException {
        BaseDAO dao = new BaseDAO();

        String querySql = "select pk_trap_record from forest_trap_record "
                + "where pk_forest_trap = ? and dr = 0 "
                + "order by record_date desc, creationtime desc limit 1";
        SQLParameter queryParam = new SQLParameter();
        queryParam.addParam(pkForestTrap);
        Object result = dao.executeQuery(querySql, queryParam, new nc.jdbc.framework.processor.ColumnProcessor());

        if (result == null) {
            return;
        }

        String pkLatestRecord = result.toString();

        String checkReviewSql = "select count(*) from forest_review where pk_trap_record = ? and dr = 0";
        SQLParameter checkReviewParam = new SQLParameter();
        checkReviewParam.addParam(pkLatestRecord);
        Object reviewCount = dao.executeQuery(checkReviewSql, checkReviewParam, new nc.jdbc.framework.processor.ColumnProcessor());

        int count = reviewCount != null ? ((Number) reviewCount).intValue() : 0;
        if (count >= 2) {
            return;
        }

        String updateSql = "update forest_trap_record set record_status = ?, modifier = ?, modifiedtime = ?, ts = ? "
                + "where pk_trap_record = ? and dr = 0 and record_status <> ?";
        SQLParameter updateParam = new SQLParameter();
        updateParam.addParam(nc.vo.forest.record.TrapRecordVO.STATUS_PENDING_REVIEW);
        updateParam.addParam(InvocationInfoProxy.getInstance().getUserId());
        updateParam.addParam(new UFDateTime().toString());
        updateParam.addParam(new UFDateTime().toString());
        updateParam.addParam(pkLatestRecord);
        updateParam.addParam(nc.vo.forest.record.TrapRecordVO.STATUS_DISPOSED);

        dao.executeUpdate(updateSql, updateParam);
    }
}
