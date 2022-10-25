import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 流程变量
 * @author: lhtao
 * @date: 2022年10月22日 8:55
 */
public class VariableTest {

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
                .addClasspathResource("holiday-variable.bpmn20.xml")
                .name("出差申请单")
                .deploy();
        System.out.println("Id: " + deploy.getId());
        System.out.println(deploy.getName());
    }

    @Test
    public void testProcessRun() {

        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String,Object> map = new HashMap<>();
        map.put("assignee0", "钱晓琦");
        map.put("assignee1", "孙宁");
        map.put("assignee2", "陆洪涛");
        map.put("assignee3", "朱小婷");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday-variable", map);
        System.out.println("id: " + processInstance.getId());
        System.out.println("processDefinitionId: " + processInstance.getProcessDefinitionId());
        System.out.println("name: " + processInstance.getName());
    }

    /**
     * 完成任务时更新流程变量
     */
    @Test
    public void testTaskComplete() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionId("holiday-variable:1:57504")
                .taskAssignee("钱晓琦")
                .singleResult();
        if (task != null) {

            //模拟获取当前任务的流程变量
            Map<String, Object> processVariables = task.getProcessVariables();
            //额外设置流程变量
            processVariables.put("num", 2);

            taskService.complete(task.getId(), processVariables);
        }
    }

    /**
     * 根据task编号更新局部流程变量
     */
    @Test
    public void updateVariableLocal() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionId("holiday-variable:1:57504")
                .taskAssignee("孙宁")
                .singleResult();
        if (task != null) {

            //模拟获取当前任务的流程变量
            Map<String, Object> processVariables = task.getProcessVariables();
            //额外设置流程变量
            processVariables.put("num", 5);
            //设置成局部流程变量
            taskService.setVariablesLocal(task.getId(), processVariables);
        }
    }

    /**
     * 根据task编号更新全局流程变量
     */
    @Test
    public void updateVariableGlobal() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionId("holiday-variable:1:57504")
                .taskAssignee("孙宁")
                .singleResult();
        if (task != null) {

            //模拟获取当前任务的流程变量，如果全局和局部变量同时存在，则取局部变量部分
            Map<String, Object> processVariables = task.getProcessVariables();
            //额外设置流程变量
            processVariables.put("num", 3);
            //设置成全局流程变量
            taskService.setVariables(task.getId(), processVariables);
        }
    }
}
