package nc.itf.forest.user;

import nc.vo.forest.user.ForestUserVO;
import nc.vo.pub.BusinessException;

public interface IForestUserService {

    ForestUserVO[] queryUsersByRole(int userRole, String pkOrg) throws BusinessException;

    ForestUserVO queryUserByCode(String userCode, String pkOrg) throws BusinessException;

    ForestUserVO saveUser(ForestUserVO userVO) throws BusinessException;

    ForestUserVO updateUser(ForestUserVO userVO) throws BusinessException;

    void deleteUser(String pkUser) throws BusinessException;

    ForestUserVO[] queryAllUsers(String pkOrg) throws BusinessException;
}
