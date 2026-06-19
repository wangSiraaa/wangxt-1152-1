package nc.impl.forest.user;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.itf.forest.user.IForestUserService;
import nc.jdbc.framework.SQLParameter;
import nc.vo.forest.user.ForestUserVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.VOStatus;
import nccloud.framework.core.exception.ExceptionUtils;

import java.util.Collection;

public class ForestUserServiceImpl implements IForestUserService {

    private BaseDAO dao = new BaseDAO();

    @Override
    public ForestUserVO[] queryUsersByRole(int userRole, String pkOrg) throws BusinessException {
        String condition = "user_role = ? and pk_org = ? and dr = 0 order by user_code";
        SQLParameter param = new SQLParameter();
        param.addParam(userRole);
        param.addParam(pkOrg);
        Collection<ForestUserVO> users = dao.retrieveByClause(ForestUserVO.class, condition, param);
        return users != null ? users.toArray(new ForestUserVO[0]) : new ForestUserVO[0];
    }

    @Override
    public ForestUserVO queryUserByCode(String userCode, String pkOrg) throws BusinessException {
        String condition = "user_code = ? and pk_org = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(userCode);
        param.addParam(pkOrg);
        Collection<ForestUserVO> users = dao.retrieveByClause(ForestUserVO.class, condition, param);
        if (users != null && users.size() > 0) {
            return users.iterator().next();
        }
        return null;
    }

    @Override
    public ForestUserVO saveUser(ForestUserVO userVO) throws BusinessException {
        if (userVO.getUser_code() == null || userVO.getUser_code().trim().isEmpty()) {
            ExceptionUtils.wrapBusinessException("用户编码不能为空");
        }
        if (userVO.getUser_name() == null || userVO.getUser_name().trim().isEmpty()) {
            ExceptionUtils.wrapBusinessException("用户名称不能为空");
        }
        if (userVO.getUser_role() == null) {
            ExceptionUtils.wrapBusinessException("用户角色不能为空");
        }

        ForestUserVO existing = queryUserByCode(userVO.getUser_code(), userVO.getPk_org());
        if (existing != null) {
            ExceptionUtils.wrapBusinessException("用户编码已存在");
        }

        fillAuditFields(userVO, true);
        userVO.setStatus(VOStatus.NEW);
        dao.insertVO(userVO);
        return userVO;
    }

    @Override
    public ForestUserVO updateUser(ForestUserVO userVO) throws BusinessException {
        if (userVO.getPk_forest_user() == null) {
            ExceptionUtils.wrapBusinessException("用户主键不能为空");
        }

        fillAuditFields(userVO, false);
        userVO.setStatus(VOStatus.UPDATED);
        dao.updateVO(userVO);
        return userVO;
    }

    @Override
    public void deleteUser(String pkUser) throws BusinessException {
        if (pkUser == null) {
            ExceptionUtils.wrapBusinessException("用户主键不能为空");
        }
        String sql = "update forest_user set dr = 1, modifier = ?, modifiedtime = ?, ts = ? where pk_forest_user = ? and dr = 0";
        SQLParameter param = new SQLParameter();
        param.addParam(InvocationInfoProxy.getInstance().getUserId());
        param.addParam(new UFDateTime().toString());
        param.addParam(new UFDateTime().toString());
        param.addParam(pkUser);
        dao.executeUpdate(sql, param);
    }

    @Override
    public ForestUserVO[] queryAllUsers(String pkOrg) throws BusinessException {
        String condition = "pk_org = ? and dr = 0 order by user_code";
        SQLParameter param = new SQLParameter();
        param.addParam(pkOrg);
        Collection<ForestUserVO> users = dao.retrieveByClause(ForestUserVO.class, condition, param);
        return users != null ? users.toArray(new ForestUserVO[0]) : new ForestUserVO[0];
    }

    private void fillAuditFields(ForestUserVO vo, boolean isNew) {
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

        if (isNew) {
            vo.setCreator(userId);
            vo.setCreationtime(now);
        }
        vo.setModifier(userId);
        vo.setModifiedtime(now);
    }
}
