package com.lht.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @Description: TODO
 * @author: lhtao
 * @date: 2022年10月18日 13:53
 */
public class SendRejectionMail implements JavaDelegate {

    /**
     * 这是一个flowable中的触发器
     * @param delegateExecution
     */
    @Override
    public void execute(DelegateExecution delegateExecution) {
        //触发执行的逻辑，按照我们在流程中的定义应该给被拒绝的员工发送通知的邮件
        System.out.println("不好意思，你的请假申请被拒绝了");
    }
}
