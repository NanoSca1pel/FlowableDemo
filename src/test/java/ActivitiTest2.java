import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

/**
 * @Description: 流程2
 * @author: lhtao
 * @date: 2022年10月18日 9:31
 */
public class ActivitiTest2 {

    /**
     * 加载默认名称的配置文件（flowable.cfg.xml）
     * @return
     */
    private ProcessEngine getProcessEngineByDefaultNameXml() {
        //获取ProcessEngineConfiguration对象
        return ProcessEngines.getDefaultProcessEngine();
    }

    /**
     * 流程部署
     */
    @Test
    public void testDeploy() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-easy.bpmn20.xml")
                .addClasspathResource("holiday-easy.png")
                .name("简单请假流程")
                .deploy();
        System.out.println("deploymentId: " + deployment.getId());
    }

    /**
     * 流程部署压缩文件
     */
    @Test
    public void testDeployZip() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("holiday-easy.zip");
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(new ZipInputStream(is))
                .name("简单请假流程-压缩文件")
                .deploy();
        System.out.println("deploymentId: " + deployment.getId());
    }

    /**
     * 启动一个流程实例
     */
    @Test
    public void testProcessRun() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        //获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //构建流程变量
        Map<String, Object> map = new HashMap<>();
        map.put("employee", "钱晓琦");
        map.put("holidayNums", 3);
        map.put("reason", "家中有事");
        //ProcessInstance holidayRequest = runtimeService.startProcessInstanceById("holiday-easy:2:22504", map);
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceById("holiday-easy:2:22504", "businessId", map);
        System.out.println("id: " + holidayRequest.getId());
        System.out.println("processDefinitionId: " + holidayRequest.getProcessDefinitionId());
        System.out.println("activityId: " + holidayRequest.getActivityId());

    }

    /**
     * 流程的挂起和激活
     */
    @Test
    public void testSuspended() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //获取对应的流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId("holiday-easy:2:22504")
                .singleResult();

        //获取当前的流程定义的状态信息
        boolean suspended = processDefinition.isSuspended();
        if (suspended) {
            //当前流程是挂起状态
            //激活流程
            System.out.println("激活流程：" + processDefinition.getId() + ":" + processDefinition.getName());
            repositoryService.activateProcessDefinitionById(processDefinition.getId());
        } else {
            //当前流程是激活状态
            //挂起流程
            System.out.println("挂起流程：" + processDefinition.getId() + ":" + processDefinition.getName());
            repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        }
    }

    /**
     * 任务完成
     */
    @Test
    public void testTaskComplete() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionId("holiday-easy:2:22504")
                .taskAssignee("钱晓琦")
                .singleResult();
        //获取当前流程实例绑定的流程变量
        Map<String, Object> processVariables = task.getProcessVariables();
        Set<String> keys = processVariables.keySet();
        keys.forEach(k -> System.out.println(k + ":" + processVariables.get(k)));

        //创建流程变量
        processVariables.put("approved", true);
        processVariables.put("reason", "我要出去碗玩");

        //完成任务
        taskService.complete(task.getId(), processVariables);
    }
}
