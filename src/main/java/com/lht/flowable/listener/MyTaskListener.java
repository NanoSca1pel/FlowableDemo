package com.lht.flowable.listener;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @Description: 自定义监听器
 * @author: lhtao
 * @date: 2022年10月20日 16:50
 */
public class MyTaskListener implements TaskListener {
    /**
     * 监听器触发的方法
     * @param delegateTask
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("MyTaskListener触发了..." + delegateTask.getName());
        if ("申请".equals(delegateTask.getName()) && "create".equals(delegateTask.getEventName())) {
            //满足触发条件则设置assignee
            delegateTask.setAssignee("钱晓琦");
        } else {
            delegateTask.setAssignee("孙宁");
        }
    }
}
