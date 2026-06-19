package nc.action.forest.review;

import nc.action.forest.Result;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.forest.review.IForestReviewService;
import nc.vo.forest.review.ForestReviewVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.web.processor.ServiceContext;
import nccloud.framework.web.ui.pf.itf.ICommonAction;
import nccloud.framework.web.ui.pf.itf.IServiceContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForestReviewAction implements ICommonAction {

    private IForestReviewService getService() {
        return NCLocator.getInstance().lookup(IForestReviewService.class);
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
                case "queryByQuarantine":
                    result = queryByQuarantine(request);
                    break;
                case "queryLatest":
                    result = queryLatest(request);
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

    private ForestReviewVO parseRequest(HttpServletRequest request) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ForestReviewVO.class);
    }

    private Object query(HttpServletRequest request) throws BusinessException {
        String pkOrg = request.getParameter("pkOrg");
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        String condition = "pk_org = ? and dr = 0 order by review_date desc, creationtime desc";
        return getService().queryReviewsByRecord(null, pkOrg);
    }

    private Object queryById(HttpServletRequest request) throws BusinessException {
        String pkReview = request.getParameter("pkReview");
        if (pkReview == null) {
            throw new BusinessException("复核记录主键不能为空");
        }
        return getService().queryReviewById(pkReview);
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
        return getService().queryReviewsByRecord(pkTrapRecord, pkOrg);
    }

    private Object queryByQuarantine(HttpServletRequest request) throws BusinessException {
        String pkQuarantine = request.getParameter("pkQuarantine");
        String pkOrg = request.getParameter("pkOrg");
        if (pkQuarantine == null) {
            pkQuarantine = InvocationInfoProxy.getInstance().getUserId();
        }
        if (pkOrg == null) {
            pkOrg = InvocationInfoProxy.getInstance().getPk_org();
        }
        return getService().queryReviewsByQuarantine(pkQuarantine, pkOrg);
    }

    private Object queryLatest(HttpServletRequest request) throws BusinessException {
        String pkTrapRecord = request.getParameter("pkTrapRecord");
        if (pkTrapRecord == null) {
            throw new BusinessException("诱捕记录主键不能为空");
        }
        return getService().queryLatestReview(pkTrapRecord);
    }

    private Object save(HttpServletRequest request) throws Exception {
        ForestReviewVO reviewVO = parseRequest(request);
        return getService().saveReview(reviewVO);
    }

    private Object update(HttpServletRequest request) throws Exception {
        ForestReviewVO reviewVO = parseRequest(request);
        return getService().updateReview(reviewVO);
    }

    private Object delete(HttpServletRequest request) throws BusinessException {
        String pkReview = request.getParameter("pkReview");
        if (pkReview == null) {
            throw new BusinessException("复核记录主键不能为空");
        }
        getService().deleteReview(pkReview);
        return null;
    }
}
