package nc.action.forest.trap;

import nc.action.forest.Result;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.forest.trap.IForestTrapService;
import nc.vo.forest.trap.ForestTrapVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.web.processor.ServiceContext;
import nccloud.framework.web.ui.pf.itf.ICommonAction;
import nccloud.framework.web.ui.pf.itf.IServiceContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForestTrapAction implements ICommonAction {

    private IForestTrapService getService() {
        return NCLocator.getInstance().lookup(IForestTrapService.class);
    }

    @Override
    public String doAction(HttpServletRequest request, HttpServletResponse response, IServiceContext context) throws Exception {
        ServiceContext serviceContext = (ServiceContext) context;
        String action = serviceContext.getAction();

        try {
            initContext(request);

            Object result;
            switch (action) {
                case "query":
                    result = query(request);
                    break;
                case "queryById":
                    result = queryById(request);
                    break;
                case "queryByArea":
                    result = queryByArea(request);
                    break;
                case "queryKeyPatrol":
                    result = queryKeyPatrol(request);
                    break;
                case "save":
                    result = save(request);
                    break;
                case "update":
                    result = update(request);
                    break;
                case "delete":
                    result = delete(request);
                    break;
                case "setKeyPatrol":
                    result = setKeyPatrol(request);
                    break;
                default:
                    return Result.error("不支持的操作类型").toString();
            }
            return Result.success(result).toString();
        } catch (BusinessException e) {
            return Result.error(e.getMessage()).toString();
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage()).toString();
        }
    }

    private void initContext(HttpServletRequest request) {
        String userId = request.getHeader("userId");
        String pkOrg = request.getHeader("pkOrg");
        String pkGroup = request.getHeader("pkGroup");

        if (userId != null) {
            InvocationInfoProxy.getInstance().setUserId(userId);
        }
        if (pkOrg != null) {
            InvocationInfoProxy.getInstance().setPk_org(pkOrg);
        }
        if (pkGroup != null) {
            InvocationInfoProxy.getInstance().setGroupId(pkGroup);
        }
    }

    private ForestTrapVO parseRequest(HttpServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ForestTrapVO.class);
    }

    private Object query(HttpServletRequest request) throws BusinessException {
        String pkOrg = request.getParameter("pkOrg");
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryAllTraps(pkOrg);
    }

    private Object queryById(HttpServletRequest request) throws BusinessException {
        String pkTrap = request.getParameter("pkTrap");
        if (pkTrap == null) {
            throw new BusinessException("诱捕器主键不能为空");
        }
        return getService().queryTrapById(pkTrap);
    }

    private Object queryByArea(HttpServletRequest request) throws BusinessException {
        String areaCode = request.getParameter("areaCode");
        String pkOrg = request.getParameter("pkOrg");
        if (areaCode == null) {
            throw new BusinessException("区域编码不能为空");
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryTrapsByArea(areaCode, pkOrg);
    }

    private Object queryKeyPatrol(HttpServletRequest request) throws BusinessException {
        String pkOrg = request.getParameter("pkOrg");
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryKeyPatrolTraps(pkOrg);
    }

    private Object save(HttpServletRequest request) throws Exception {
        ForestTrapVO trapVO = parseRequest(request);
        return getService().saveTrap(trapVO);
    }

    private Object update(HttpServletRequest request) throws Exception {
        ForestTrapVO trapVO = parseRequest(request);
        return getService().updateTrap(trapVO);
    }

    private Object delete(HttpServletRequest request) throws BusinessException {
        String pkTrap = request.getParameter("pkTrap");
        if (pkTrap == null) {
            throw new BusinessException("诱捕器主键不能为空");
        }
        getService().deleteTrap(pkTrap);
        return null;
    }

    private Object setKeyPatrol(HttpServletRequest request) throws BusinessException {
        String pkTrap = request.getParameter("pkTrap");
        String isKeyPatrol = request.getParameter("isKeyPatrol");
        if (pkTrap == null) {
            throw new BusinessException("诱捕器主键不能为空");
        }
        if (isKeyPatrol == null) {
            throw new BusinessException("重点巡查标识不能为空");
        }
        getService().setKeyPatrol(pkTrap, Integer.parseInt(isKeyPatrol));
        return null;
    }
}
