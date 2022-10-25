import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 排他网关
 * @author: lhtao
 * @date: 2022年10月25日 8:50
 */
public class GatewayExclusiveTest {

    /**
     * 加载默认名称的配置文件（flowable.cfg.xml）
     * @return
     */
    private ProcessEngine getProcessEngineByDefaultNameXml() {
        //获取ProcessEngineConfiguration对象
        return ProcessEngines.getDefaultProcessEngine();
    }

    /**
     * 部署流程
     */
    @Test
    public void testDeploy() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("holiday-gateway-exclusive.bpmn20.xml")
                .name("请假审批单-排他网关")
                .deploy();
        System.out.println("Id: " + deploy.getId());
        System.out.println(deploy.getName());
    }

    @Test
    public void testProcessRun() {

        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String,Object> map = new HashMap<>();
        map.put("num", "3");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday-gateway-exclusive", map);
        System.out.println("id: " + processInstance.getId());
        System.out.println("processDefinitionId: " + processInstance.getProcessDefinitionId());
        System.out.println("name: " + processInstance.getName());
    }

    /**
     * 任务完成
     */
    @Test
    public void testTaskComplete() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("2501")
                .taskAssignee("朱小婷")
                .singleResult();
        if (task != null) {
            //完成任务
            taskService.complete(task.getId());
        }
    }
}
