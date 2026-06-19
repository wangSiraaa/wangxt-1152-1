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

        String sql = "update forest_trap set is_key_patrol = ?, key_patrol_reason = ?, modifier = ?, modifiedtime = ?, ts = ? "
                + "where pk_forest_trap = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(ForestTrapVO.IS_KEY_PATROL_YES);
        param.addParam("连续" + highRiskCount + "次高风险，自动进入重点巡查");
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkForestTrap);

        dao.executeUpdate(sql, param);
    }
}
