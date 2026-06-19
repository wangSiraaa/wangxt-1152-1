package nc.action.forest.record;

import nc.action.forest.Result;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.forest.record.ITrapRecordService;
import nc.vo.forest.record.TrapRecordVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.web.processor.ServiceContext;
import nccloud.framework.web.ui.pf.itf.ICommonAction;
import nccloud.framework.web.ui.pf.itf.IServiceContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TrapRecordAction implements ICommonAction {

    private ITrapRecordService getService() {
        return NCLocator.getInstance().lookup(ITrapRecordService.class);
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
                case "queryByTrap":
                    result = queryByTrap(request);
                    break;
                case "queryByRanger":
                    result = queryByRanger(request);
                    break;
                case "queryByStatus":
                    result = queryByStatus(request);
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

    private TrapRecordVO parseRequest(HttpServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, TrapRecordVO.class);
    }

    private Object query(HttpServletRequest request) throws BusinessException {
        String pkOrg = request.getParameter("pkOrg");
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryAllRecords(pkOrg);
    }

    private Object queryById(HttpServletRequest request) throws BusinessException {
        String pkRecord = request.getParameter("pkRecord");
        if (pkRecord == null) {
            throw new BusinessException("诱捕记录主键不能为空");
        }
        return getService().queryRecordById(pkRecord);
    }

    private Object queryByTrap(HttpServletRequest request) throws BusinessException {
        String pkTrap = request.getParameter("pkTrap");
        String pkOrg = request.getParameter("pkOrg");
        if (pkTrap == null) {
            throw new BusinessException("诱捕器主键不能为空");
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryRecordsByTrap(pkTrap, pkOrg);
    }

    private Object queryByRanger(HttpServletRequest request) throws BusinessException {
        String pkRanger = request.getParameter("pkRanger");
        String pkOrg = request.getParameter("pkOrg");
        if (pkRanger == null) {
            pkRanger = InvocationInfoProxy.getInstance().getUserId();
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryRecordsByRanger(pkRanger, pkOrg);
    }

    private Object queryByStatus(HttpServletRequest request) throws BusinessException {
        String recordStatus = request.getParameter("recordStatus");
        String pkOrg = request.getParameter("pkOrg");
        if (recordStatus == null) {
            throw new BusinessException("记录状态不能为空");
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryRecordsByStatus(Integer.parseInt(recordStatus), pkOrg);
    }

    private Object save(HttpServletRequest request) throws Exception {
        TrapRecordVO recordVO = parseRequest(request);
        return getService().saveRecord(recordVO);
    }

    private Object update(HttpServletRequest request) throws Exception {
        TrapRecordVO recordVO = parseRequest(request);
        return getService().updateRecord(recordVO);
    }

    private Object delete(HttpServletRequest request) throws BusinessException {
        String pkRecord = request.getParameter("pkRecord");
        if (pkRecord == null) {
            throw new BusinessException("诱捕记录主键不能为空");
        }
        getService().deleteRecord(pkRecord);
        return null;
    }
}
