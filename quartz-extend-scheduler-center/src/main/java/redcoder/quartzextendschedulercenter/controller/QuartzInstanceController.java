package redcoder.quartzextendschedulercenter.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import redcoder.quartzextendschedulercenter.dto.ApiResult;
import redcoder.quartzextendschedulercenter.dto.PageResponse;
import redcoder.quartzextendschedulercenter.dto.instance.QuartzInstanceDTO;
import redcoder.quartzextendschedulercenter.dto.instance.QueryInstanceInfo;
import redcoder.quartzextendschedulercenter.entity.QuartzSchedulerInstance;
import redcoder.quartzextendschedulercenter.repository.InstanceRepository;
import redcoder.quartzextendschedulercenter.service.QuartzJobSchedulerService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author redcoder54
 * @since 2021-04-26
 */
@RestController
@RequestMapping("/api/instance")
@Api(tags = "Quartz实例管理")
public class QuartzInstanceController {

    @Resource
    private InstanceRepository instanceRepository;
    @Resource
    private QuartzJobSchedulerService quartzJobSchedulerService;

    @GetMapping("/list")
    @ApiOperation(value = "获取实例", httpMethod = "GET")
    public ApiResult<PageResponse<QuartzInstanceDTO>> getInstancesList(QueryInstanceInfo queryInstanceInfo) {
        String schedName = queryInstanceInfo.getSchedName();
        int pageNo = queryInstanceInfo.getPageNo() - 1;
        int pageSize = queryInstanceInfo.getPageSize();

        Page<QuartzSchedulerInstance> page;
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by("schedName"));
        if (StringUtils.hasText(schedName)) {
            page = instanceRepository.findBySchedName(schedName, pageRequest);
        } else {
            page = instanceRepository.findAll(pageRequest);
        }
        List<QuartzInstanceDTO> data = page.stream().map(QuartzInstanceDTO::valueOf).collect(Collectors.toList());
        return ApiResult.success(new PageResponse<>(page.getTotalElements(), pageNo, pageSize, data));
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除实例", httpMethod = "DELETE")
    public ApiResult<Boolean> delete(@Valid @RequestBody QuartzInstanceDTO dto) {
        return ApiResult.success(quartzJobSchedulerService.deleteInstance(dto));
    }
}
