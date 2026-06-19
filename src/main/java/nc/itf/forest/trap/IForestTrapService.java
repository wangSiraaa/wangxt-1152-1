package nc.itf.forest.trap;

import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.pub.BusinessException;

public interface IForestTrapService {

    ForestTrapVO[] queryAllTraps(String pkOrg) throws BusinessException;

    ForestTrapVO[] queryKeyPatrolTraps(String pkOrg) throws BusinessException;

    ForestTrapVO queryTrapByCode(String trapCode, String pkOrg) throws BusinessException;

    ForestTrapVO saveTrap(ForestTrapVO trapVO) throws BusinessException;

    ForestTrapVO updateTrap(ForestTrapVO trapVO) throws BusinessException;

    void deleteTrap(String pkTrap) throws BusinessException;

    ForestTrapVO queryTrapById(String pkTrap) throws BusinessException;

    ForestTrapVO[] queryTrapsByRanger(String pkRanger, String pkOrg) throws BusinessException;
}
