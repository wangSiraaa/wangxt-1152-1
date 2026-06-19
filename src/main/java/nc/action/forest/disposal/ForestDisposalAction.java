package nc.action.forest.disposal;

import nc.action.forest.Result;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.forest.disposal.IForestDisposalService;
import nc.vo.forest.disposal.DisposalPhotoVO;
import nc.vo.forest.disposal.ForestDisposalVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.web.processor.ServiceContext;
import nccloud.framework.web.ui.pf.itf.ICommonAction;
import nccloud.framework.web.ui.pf.itf.IServiceContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForestDisposalAction implements ICommonAction {

    private IForestDisposalService getService() {
        return NCLocator.getInstance().lookup(IForestDisposalService.class);
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
                case "queryByRecord":
                    result = queryByRecord(request);
                    break;
                case "queryByTeam":
                    result = queryByTeam(request);
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
                case "close":
                    result = close(request);
                    break;
                case "savePhoto":
                    result = savePhoto(request);
                    break;
                case "queryPhotos":
                    result = queryPhotos(request);
                    break;
                case "deletePhoto":
                    result = deletePhoto(request);
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

    private ForestDisposalVO parseDisposalRequest(HttpServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ForestDisposalVO.class);
    }

    private DisposalPhotoVO parsePhotoRequest(HttpServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, DisposalPhotoVO.class);
    }

    private Object query(HttpServletRequest request) throws BusinessException {
        String pkOrg = request.getParameter("pkOrg");
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        String condition = "pk_org = ? and dr = 0 order by disposal_date desc, creationtime desc";
        return getService().queryDisposalsByRecord(null, pkOrg);
    }

    private Object queryById(HttpServletRequest request) throws BusinessException {
        String pkDisposal = request.getParameter("pkDisposal");
        if (pkDisposal == null) {
            throw new BusinessException("处置记录主键不能为空");
        }
        return getService().queryDisposalById(pkDisposal);
    }

    private Object queryByRecord(HttpServletRequest request) throws BusinessException {
        String pkTrapRecord = request.getParameter("pkTrapRecord");
        String pkOrg = request.getParameter("pkOrg");
        if (pkTrapRecord == null) {
            throw new BusinessException("诱捕记录主键不能为空");
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryDisposalsByRecord(pkTrapRecord, pkOrg);
    }

    private Object queryByTeam(HttpServletRequest request) throws BusinessException {
        String pkDisposalTeam = request.getParameter("pkDisposalTeam");
        String pkOrg = request.getParameter("pkOrg");
        if (pkDisposalTeam == null) {
            pkDisposalTeam = InvocationInfoProxy.getInstance().getUserId();
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryDisposalsByTeam(pkDisposalTeam, pkOrg);
    }

    private Object save(HttpServletRequest request) throws Exception {
        ForestDisposalVO disposalVO = parseDisposalRequest(request);
        return getService().saveDisposal(disposalVO);
    }

    private Object update(HttpServletRequest request) throws Exception {
        ForestDisposalVO disposalVO = parseDisposalRequest(request);
        return getService().updateDisposal(disposalVO);
    }

    private Object delete(HttpServletRequest request) throws BusinessException {
        String pkDisposal = request.getParameter("pkDisposal");
        if (pkDisposal == null) {
            throw new BusinessException("处置记录主键不能为空");
        }
        getService().deleteDisposal(pkDisposal);
        return null;
    }

    private Object close(HttpServletRequest request) throws BusinessException {
        String pkDisposal = request.getParameter("pkDisposal");
        if (pkDisposal == null) {
            throw new BusinessException("处置记录主键不能为空");
        }
        return getService().closeDisposal(pkDisposal);
    }

    private Object savePhoto(HttpServletRequest request) throws Exception {
        DisposalPhotoVO photoVO = parsePhotoRequest(request);
        return getService().savePhoto(photoVO);
    }

    private Object queryPhotos(HttpServletRequest request) throws BusinessException {
        String pkDisposal = request.getParameter("pkDisposal");
        if (pkDisposal == null) {
            throw new BusinessException("处置记录主键不能为空");
        }
        return getService().queryPhotosByDisposal(pkDisposal);
    }

    private Object deletePhoto(HttpServletRequest request) throws BusinessException {
        String pkPhoto = request.getParameter("pkPhoto");
        if (pkPhoto == null) {
            throw new BusinessException("照片主键不能为空");
        }
        getService().deletePhoto(pkPhoto);
        return null;
    }
}
