package nc.itf.forest.recheck;

import nc.vo.forest.recheck.RecheckPlanVO;
import nc.vo.pub.BusinessException;

public interface IRecheckPlanService {

    RecheckPlanVO[] queryPlansByDisposal(String pkDisposal, String pkOrg) throws BusinessException;

    RecheckPlanVO[] queryPlansByTrap(String pkForestTrap, String pkOrg) throws BusinessException;

    RecheckPlanVO[] queryPendingPlans(String pkOrg) throws BusinessException;

    RecheckPlanVO[] queryPlansByRanger(String pkRanger, String pkOrg) throws BusinessException;

    RecheckPlanVO savePlan(RecheckPlanVO planVO) throws BusinessException;

    RecheckPlanVO updatePlan(RecheckPlanVO planVO) throws BusinessException;

    void deletePlan(String pkPlan) throws BusinessException;

    RecheckPlanVO queryPlanById(String pkPlan) throws BusinessException;

    RecheckPlanVO completePlan(String pkPlan, Integer recheckResult, String recheckRemark) throws BusinessException;

    RecheckPlanVO[] queryPlansByStatus(int status, String pkOrg) throws BusinessException;
}
