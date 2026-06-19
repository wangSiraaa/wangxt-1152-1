package nc.action.forest.user;

import nc.action.forest.Result;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.forest.user.IForestUserService;
import nc.vo.forest.user.ForestUserVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.web.container.SessionContext;
import nccloud.framework.web.processor.ServiceContext;
import nccloud.framework.web.ui.pf.itf.ICommonAction;
import nccloud.framework.web.ui.pf.itf.IServiceContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForestUserAction implements ICommonAction {

    private IForestUserService getService() {
        return NCLocator.getInstance().lookup(IForestUserService.class);
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
                case "save":
                    result = save(request);
                    break;
                case "update":
                    result = update(request);
                    break;
                case "delete":
                    result = delete(request);
                    break;
                case "queryByRole":
                    result = queryByRole(request);
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

    private ForestUserVO parseRequest(HttpServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ForestUserVO.class);
    }

    private Object query(HttpServletRequest request) throws BusinessException {
        String pkOrg = request.getParameter("pkOrg");
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryAllUsers(pkOrg);
    }

    private Object queryById(HttpServletRequest request) throws BusinessException {
        String pkUser = request.getParameter("pkUser");
        if (pkUser == null) {
            throw new BusinessException("用户主键不能为空");
        }
        return getService().queryUserById(pkUser);
    }

    private Object save(HttpServletRequest request) throws Exception {
        ForestUserVO userVO = parseRequest(request);
        return getService().saveUser(userVO);
    }

    private Object update(HttpServletRequest request) throws Exception {
        ForestUserVO userVO = parseRequest(request);
        return getService().updateUser(userVO);
    }

    private Object delete(HttpServletRequest request) throws BusinessException {
        String pkUser = request.getParameter("pkUser");
        if (pkUser == null) {
            throw new BusinessException("用户主键不能为空");
        }
        getService().deleteUser(pkUser);
        return null;
    }

    private Object queryByRole(HttpServletRequest request) throws BusinessException {
        String roleType = request.getParameter("roleType");
        String pkOrg = request.getParameter("pkOrg");
        if (roleType == null) {
            throw new BusinessException("角色类型不能为空");
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryUsersByRole(Integer.parseInt(roleType), pkOrg);
    }
}
