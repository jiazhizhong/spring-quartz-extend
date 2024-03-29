package redcoder.quartzextendschedulercenter.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import redcoder.quartzextendschedulercenter.dto.ApiResult;
import redcoder.quartzextendschedulercenter.dto.sys.MenuDto;
import redcoder.quartzextendschedulercenter.dto.sys.RoleDto;
import redcoder.quartzextendschedulercenter.dto.sys.RolePermissionDto;
import redcoder.quartzextendschedulercenter.dto.sys.UserPermissionDto;
import redcoder.quartzextendschedulercenter.service.RolePermissionService;
import redcoder.quartzextendschedulercenter.service.UserPermissionService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user/permission")
@Api(tags = "用户权限")
public class UserPermissionController {

    @Resource
    private UserPermissionService userPermissionService;

    @GetMapping("/list/{roleId}")
    @ApiOperation("获取角色权限")
    public ApiResult<List<RoleDto>> getList(@PathVariable int roleId) {
        List<RoleDto> data = userPermissionService.getList(roleId);
        return ApiResult.success(data);
    }

    @PostMapping("/addOrUpdate")
    @ApiOperation("新增|更新用户权限")
    public ApiResult<String> addOrUpdate(@RequestBody @Valid UserPermissionDto dto) {
        return userPermissionService.addOrUpdate(dto);
    }
}
