package redcoder.quartzextendcore.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import redcoder.quartzextendcommon.utils.HttpTemplate;
import redcoder.quartzextendcommon.utils.IpUtils;
import redcoder.quartzextendcommon.utils.JsonUtils;
import redcoder.quartzextendcommon.utils.MapUtils;
import redcoder.quartzextendcore.core.QuartzJobTriggerInfoCreator;
import redcoder.quartzextendcore.core.dto.QuartzApiResult;
import redcoder.quartzextendcore.core.dto.QuartzJobTriggerInfo;
import redcoder.quartzextendcore.core.dto.QuartzSchedulerInstance;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import redcoder.quartzextendcore.core.dto.ScheduleJob;

import java.util.*;

import static org.springframework.util.StringUtils.isEmpty;

public class DefaultQuartzService implements QuartzService, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(DefaultQuartzService.class);

    private Scheduler scheduler;
    private Environment environment;
    private QuartzJobSchedulerProperties properties;
    private QuartzJobTriggerInfoCreator creator;

    public DefaultQuartzService(Scheduler scheduler,
                                Environment environment,
                                QuartzJobSchedulerProperties properties,
                                QuartzJobTriggerInfoCreator creator) {
        this.scheduler = scheduler;
        this.environment = environment;
        this.properties = properties;
        this.creator = creator;
    }

    @Override
    public List<QuartzJobTriggerInfo> getQuartzJobTriggerInfoList() throws SchedulerException {
        List<QuartzJobTriggerInfo> quartzJobTriggerInfos = new ArrayList<>();

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        for (TriggerKey triggerKey : triggerKeys) {
            try {
                QuartzJobTriggerInfo quartzJobTriggerInfo = creator.create(triggerKey);
                quartzJobTriggerInfos.add(quartzJobTriggerInfo);
            } catch (SchedulerException e) {
                log.warn("createQuartBean error", e);
            }
        }

        return quartzJobTriggerInfos;
    }

    @Override
    public QuartzJobTriggerInfo getQuartzJobTriggerInfo(String triggerName, String triggerGroup) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        QuartzJobTriggerInfo quartBean = creator.create(triggerKey);
        Assert.notNull(quartBean, "QuartzJobTriggerInfo not exist");
        return quartBean;
    }

    @Override
    public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);
    }

    @Override
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);
    }

    @Override
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);
    }

    @Override
    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);
    }

    @Override
    public void scheduleJob(ScheduleJob scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        Set<Trigger> triggers = new HashSet<>();
        for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .forJob(jobKey)
                    .withIdentity(trigger.getKey())
                    .withDescription(trigger.getDescription())
                    .withSchedule(CronScheduleBuilder.cronSchedule(scheduleJob.getCron()))
                    .build();
            triggers.add(newTrigger);
        }
        Map<JobDetail, Set<? extends Trigger>> triggersAndJobs = MapUtils.buildMap(jobDetail, triggers);
        scheduler.scheduleJobs(triggersAndJobs, true);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 异步注册
        new Thread(() -> {
            String registerUrl = properties.getRegisterUrl();
            if (isEmpty(registerUrl)) {
                return;
            }

            try {
                String schedName = scheduler.getSchedulerName();
                String host = IpUtils.getLocalIp();
                String port = environment.getProperty("server.port");
                if (isEmpty(port)) {
                    log.warn("注册实例信息失败：未配置\"server.port\"");
                    return;
                }
                QuartzSchedulerInstance instance = new QuartzSchedulerInstance(schedName, host, Integer.valueOf(port));
                QuartzApiResult<Boolean> apiResult = HttpTemplate.doPost(registerUrl, JsonUtils.beanToJsonString(instance),
                        new TypeReference<QuartzApiResult<Boolean>>() {
                        });
                if (apiResult.getStatus() == 0 && Boolean.TRUE.equals(apiResult.getData())) {
                    log.info("注册实例信息成功");
                } else {
                    log.warn("注册实例信息失败：" + apiResult.getMessage());
                }
            } catch (Exception e) {
                log.warn("注册实例信息失败", e);
            }
        }, "Quartz-Register-Thread").start();
    }

    @Override
    public void destroy() throws Exception {
        String unregisterUrl = properties.getUnregisterUrl();
        if (isEmpty(unregisterUrl)) {
            return;
        }
        try {
            String schedName = scheduler.getSchedulerName();
            String host = IpUtils.getLocalIp();
            String port = environment.getProperty("server.port");
            if (isEmpty(port)) {
                log.warn("解除注册的实例信息失败：未配置\"server.port\"");
                return;
            }
            QuartzSchedulerInstance instance = new QuartzSchedulerInstance(schedName, host, Integer.valueOf(port));
            QuartzApiResult<Boolean> apiResult = HttpTemplate.doPost(unregisterUrl, JsonUtils.beanToJsonString(instance),
                    new TypeReference<QuartzApiResult<Boolean>>() {
                    });
            if (apiResult.getStatus() == 0 && Boolean.TRUE.equals(apiResult.getData())) {
                log.info("解除注册的实例信息成功");
            } else {
                log.warn("解除注册的实例信息失败：" + apiResult.getMessage());
            }
        } catch (Exception e) {
            log.warn("解除注册的注册实例信息失败", e);
        }
    }
}
