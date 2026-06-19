package nc.impl.forest.recheck;

import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.EventDispatcher;
import nc.bs.businessevent.IEventType;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.itf.forest.recheck.IRecheckPlanService;
import nc.jdbc.framework.SQLParameter;
import nc.vo.forest.recheck.RecheckPlanVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

import java.util.Collection;

public class RecheckPlanServiceImpl implements IRecheckPlanService {

    public static final String SOURCE_ID = "FOREST_RECHECK_PLAN";

    private BaseDAO dao = new BaseDAO();

    @Override
    public RecheckPlanVO[] queryPlansByDisposal(String pkDisposal, String pkOrg) throws BusinessException {
        String condition = "pk_forest_disposal = ? and pk_org = ? and dr = 0 order by plan_date, recheck_type";
        SQLParameter param = new SQLParameter();
        param.addParam(pkDisposal);
        param.addParam(pkOrg);
        Collection<RecheckPlanVO> plans = dao.retrieveByClause(RecheckPlanVO.class, condition, param);
        return plans != null ? plans.toArray(new RecheckPlanVO[0]) : new RecheckPlanVO[0];
    }

    @Override
    public RecheckPlanVO[] queryPlansByTrap(String pkForestTrap, String pkOrg) throws BusinessException {
        String condition = "pk_forest_trap = ? and pk_org = ? and dr = 0 order by plan_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkForestTrap);
        param.addParam(pkOrg);
        Collection<RecheckPlanVO> plans = dao.retrieveByClause(RecheckPlanVO.class, condition, param);
        return plans != null ? plans.toArray(new RecheckPlanVO[0]) : new RecheckPlanVO[0];
    }

    @Override
    public RecheckPlanVO[] queryPendingPlans(String pkOrg) throws BusinessException {
        return queryPlansByStatus(RecheckPlanVO.STATUS_PENDING, pkOrg);
    }

    @Override
    public RecheckPlanVO[] queryPlansByRanger(String pkRanger, String pkOrg) throws BusinessException {
        String condition = "pk_ranger = ? and pk_org = ? and dr = 0 order by plan_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(pkRanger);
        param.addParam(pkOrg);
        Collection<RecheckPlanVO> plans = dao.retrieveByClause(RecheckPlanVO.class, condition, param);
        return plans != null ? plans.toArray(new RecheckPlanVO[0]) : new RecheckPlanVO[0];
    }

    @Override
    public RecheckPlanVO savePlan(RecheckPlanVO planVO) throws BusinessException {
        if (planVO.getPk_forest_disposal() == null && planVO.getPk_forest_trap() == null) {
            ExceptionUtils.wrapBusinessException("处置记录或诱捕器不能为空");
        }
        if (planVO.getPlan_date() == null) {
            ExceptionUtils.wrapBusinessException("计划复查日期不能为空");
        }
        if (planVO.getRecheck_type() == null) {
            ExceptionUtils.wrapBusinessException("复查类型不能为空");
        }

        fillAuditFields(planVO, true);
        planVO.setRecheck_status(RecheckPlanVO.STATUS_PENDING);
        planVO.setStatus(VOStatus.NEW);
        dao.insertVO(planVO);

        fireEvent(planVO, IEventType.TYPE_INSERT_AFTER);

        return planVO;
    }

    @Override
    public RecheckPlanVO updatePlan(RecheckPlanVO planVO) throws BusinessException {
        if (planVO.getPk_recheck_plan() == null) {
            ExceptionUtils.wrapBusinessException("复查计划主键不能为空");
        }

        fillAuditFields(planVO, false);
        planVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(planVO);

        fireEvent(planVO, IEventType.TYPE_UPDATE_AFTER);

        return planVO;
    }

    @Override
    public void deletePlan(String pkPlan) throws BusinessException {
        if (pkPlan == null) {
            ExceptionUtils.wrapBusinessException("复查计划主键不能为空");
        }
        String sql = "update forest_recheck_plan set dr = 1, modifier = ?, modifiedtime = ?, ts = ? where pk_recheck_plan = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkPlan);
        dao.executeUpdate(sql, param);
    }

    @Override
    public RecheckPlanVO queryPlanById(String pkPlan) throws BusinessException {
        String condition = "pk_recheck_plan = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkPlan);
        Collection<RecheckPlanVO> plans = dao.retrieveByClause(RecheckPlanVO.class, condition, param);
        if (plans != null && plans.size() > 0) {
            return plans.iterator().next();
        }
        return null;
    }

    @Override
    public RecheckPlanVO completePlan(String pkPlan, Integer recheckResult, String recheckRemark) throws BusinessException {
        if (pkPlan == null) {
            ExceptionUtils.wrapBusinessException("复查计划主键不能为空");
        }
        if (recheckResult == null) {
            ExceptionUtils.wrapBusinessException("复查结果不能为空");
        }

        RecheckPlanVO planVO = queryPlanById(pkPlan);
        if (planVO == null) {
            ExceptionUtils.wrapBusinessException("复查计划不存在");
        }
        if (RecheckPlanVO.STATUS_COMPLETED == planVO.getRecheck_status()) {
            ExceptionUtils.wrapBusinessException("该复查计划已完成");
        }
        if (RecheckPlanVO.STATUS_CANCELLED == planVO.getRecheck_status()) {
            ExceptionUtils.wrapBusinessException("该复查计划已取消");
        }

        planVO.setRecheck_status(RecheckPlanVO.STATUS_COMPLETED);
        planVO.setActual_date(new UFDate());
        planVO.setRecheck_result(recheckResult);
        planVO.setRecheck_remark(recheckRemark);

        fillAuditFields(planVO, false);
        planVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(planVO);

        fireEvent(planVO, IEventType.TYPE_UPDATE_AFTER);

        return planVO;
    }

    @Override
    public RecheckPlanVO[] queryPlansByStatus(int status, String pkOrg) throws BusinessException {
        String condition = "recheck_status = ? and pk_org = ? and dr = 0 order by plan_date desc, creationtime desc";
        SQLParameter param = new SQLParameter();
        param.addParam(status);
        param.addParam(pkOrg);
        Collection<RecheckPlanVO> plans = dao.retrieveByClause(RecheckPlanVO.class, condition, param);
        return plans != null ? plans.toArray(new RecheckPlanVO[0]) : new RecheckPlanVO[0];
    }

    private void fillAuditFields(RecheckPlanVO vo, boolean isNew) {
        String userId = InvocationInfoProxy.getInstance().getUserId();
        String pkGroup = InvocationInfoProxy.getInstance().getGroupId();
        String pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        UFDateTime now = new UFDateTime();

        if (vo.getPk_group() == null) {
            vo.setPk_group(pkGroup);
        }
        if (vo.getPk_org() == null) {
            vo.setPk_org(pkOrg);
        }
        if (vo.getPk_ranger() == null) {
            vo.setPk_ranger(userId);
        }
        if (vo.getRecheck_status() == null) {
            vo.setRecheck_status(RecheckPlanVO.STATUS_PENDING);
        }

        if (isNew) {
            vo.setCreator(userId);
            vo.setCreationtime(now);
        }
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }

    private void fireEvent(RecheckPlanVO planVO, String eventType) {
        try {
            BusinessEvent event = new BusinessEvent(SOURCE_ID, eventType, planVO);
            EventDispatcher.fireEvent(event);
        } catch (Exception e) {
        }
    }
}
