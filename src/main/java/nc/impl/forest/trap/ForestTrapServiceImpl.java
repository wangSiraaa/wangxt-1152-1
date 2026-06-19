package nc.impl.forest.trap;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.itf.forest.trap.IForestTrapService;
import nc.jdbc.framework.SQLParameter;
import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

import java.util.Collection;

public class ForestTrapServiceImpl implements IForestTrapService {

    private BaseDAO dao = new BaseDAO();

    @Override
    public ForestTrapVO[] queryAllTraps(String pkOrg) throws BusinessException {
        String condition = "pk_org = ? and dr = 0 order by trap_code";
        SQLParameter param = new SQLParameter();
        param.addParam(pkOrg);
        Collection<ForestTrapVO> traps = dao.retrieveByClause(ForestTrapVO.class, condition, param);
        return traps != null ? traps.toArray(new ForestTrapVO[0]) : new ForestTrapVO[0];
    }

    @Override
    public ForestTrapVO[] queryKeyPatrolTraps(String pkOrg) throws BusinessException {
        String condition = "pk_org = ? and is_key_patrol = ? and dr = 0 order by trap_code";
        SQLParameter param = new SQLParameter();
        param.addParam(pkOrg);
        param.addParam(ForestTrapVO.IS_KEY_PATROL_YES);
        Collection<ForestTrapVO> traps = dao.retrieveByClause(ForestTrapVO.class, condition, param);
        return traps != null ? traps.toArray(new ForestTrapVO[0]) : new ForestTrapVO[0];
    }

    @Override
    public ForestTrapVO queryTrapByCode(String trapCode, String pkOrg) throws BusinessException {
        String condition = "trap_code = ? and pk_org = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(trapCode);
        param.addParam(pkOrg);
        Collection<ForestTrapVO> traps = dao.retrieveByClause(ForestTrapVO.class, condition, param);
        if (traps != null && traps.size() > 0) {
            return traps.iterator().next();
        }
        return null;
    }

    @Override
    public ForestTrapVO saveTrap(ForestTrapVO trapVO) throws BusinessException {
        if (trapVO.getTrap_code() == null || trapVO.getTrap_code().trim().isEmpty()) {
            ExceptionUtils.wrapBusinessException("诱捕器编码不能为空");
        }
        if (trapVO.getLongitude() == null) {
            ExceptionUtils.wrapBusinessException("经度不能为空");
        }
        if (trapVO.getLatitude() == null) {
            ExceptionUtils.wrapBusinessException("纬度不能为空");
        }

        ForestTrapVO existing = queryTrapByCode(trapVO.getTrap_code(), trapVO.getPk_org());
        if (existing != null) {
            ExceptionUtils.wrapBusinessException("诱捕器编码已存在");
        }

        fillAuditFields(trapVO, true);
        trapVO.setStatus(VOStatus.NEW);
        dao.insertVO(trapVO);
        return trapVO;
    }

    @Override
    public ForestTrapVO updateTrap(ForestTrapVO trapVO) throws BusinessException {
        if (trapVO.getPk_forest_trap() == null) {
            ExceptionUtils.wrapBusinessException("诱捕器主键不能为空");
        }

        fillAuditFields(trapVO, false);
        trapVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(trapVO);
        return trapVO;
    }

    @Override
    public void deleteTrap(String pkTrap) throws BusinessException {
        if (pkTrap == null) {
            ExceptionUtils.wrapBusinessException("诱捕器主键不能为空");
        }
        String sql = "update forest_trap set dr = 1, modifier = ?, modifiedtime = ?, ts = ? where pk_forest_trap = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkTrap);
        dao.executeUpdate(sql, param);
    }

    @Override
    public ForestTrapVO queryTrapById(String pkTrap) throws BusinessException {
        String condition = "pk_forest_trap = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(pkTrap);
        Collection<ForestTrapVO> traps = dao.retrieveByClause(ForestTrapVO.class, condition, param);
        if (traps != null && traps.size() > 0) {
            return traps.iterator().next();
        }
        return null;
    }

    @Override
    public ForestTrapVO[] queryTrapsByRanger(String pkRanger, String pkOrg) throws BusinessException {
        String condition = "pk_ranger = ? and pk_org = ? and dr = 0 order by trap_code";
        SQLParameter param = new SQLParameter();
        param.addParam(pkRanger);
        param.addParam(pkOrg);
        Collection<ForestTrapVO> traps = dao.retrieveByClause(ForestTrapVO.class, condition, param);
        return traps != null ? traps.toArray(new ForestTrapVO[0]) : new ForestTrapVO[0];
    }

    private void fillAuditFields(ForestTrapVO vo, boolean isNew) {
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
        if (vo.getIs_key_patrol() == null) {
            vo.setIs_key_patrol(ForestTrapVO.NOT_KEY_PATROL);
        }

        if (isNew) {
            vo.setCreator(userId);
            vo.setCreationtime(now);
        }
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }
}
