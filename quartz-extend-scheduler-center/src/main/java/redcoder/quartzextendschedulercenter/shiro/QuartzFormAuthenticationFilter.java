package redcoder.quartzextendschedulercenter.shiro;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import redcoder.quartzextendcommon.utils.JsonUtils;
import redcoder.quartzextendschedulercenter.constant.ApiStatus;
import redcoder.quartzextendschedulercenter.dto.ApiResult;
import redcoder.quartzextendschedulercenter.utils.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重写{@link AccessControlFilter#saveRequestAndRedirectToLogin}，对于未登录的用户，返回json格式的错误信息，替代默认的重定向行为。
 *
 * @author redcoder
 * @since 2022-12-20
 */
public class QuartzFormAuthenticationFilter extends PassThruAuthenticationFilter {

    @Override
    protected void saveRequestAndRedirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        WebUtils.writeResponse(httpServletResponse, getErrorMessage());
    }

    private String getErrorMessage() {
        ApiResult<String> result = ApiResult.failure(ApiStatus.UNAUTHORIZED_REQUEST);
        return JsonUtils.beanToJsonString(result);
    }
}
