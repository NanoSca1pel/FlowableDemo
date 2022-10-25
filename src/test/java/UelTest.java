import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: UEL表达式
 * @author: lhtao
 * @date: 2022年10月18日 9:31
 */
public class UelTest {

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
     * 部署流程涉及到三张表：
     * 流程部署表：ACT_RE_DEPLOYMENT 一次流程部署操作就会生成一条表记录
     * 流程定义表：ACT_RE_PROCDEF 一次部署操作中包含几个流程定义文件就会产生几条记录
     * 流程定义资源文件表：ACT_GE_BYTEARRAY 有多少资源就会生成几条记录
     */
    @Test
    public void testDeploy() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();

        //获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-uel.bpmn20.xml")
                .name("请假流程-uel")
                .deploy();
        System.out.println(deployment);
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
        map.put("assignee0", "钱晓琦");
        map.put("assignee1", "孙宁");
        map.put("holidayNums", 3);
        map.put("reason", "家中有事");
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("holiday-uel", map);
        System.out.println("id: " + holidayRequest.getId());
        System.out.println("processDefinitionId: " + holidayRequest.getProcessDefinitionId());
        System.out.println("activityId: " + holidayRequest.getActivityId());

    }

    /**
     * 任务完成
     */
    @Test
    public void testTaskComplete() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionId("holiday-uel:1:37504")
                .taskAssignee("孙宁")
                .singleResult();

        //完成任务
        taskService.complete(task.getId());
    }
}
