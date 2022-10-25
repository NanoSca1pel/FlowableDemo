import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 候选人
 * @author: lhtao
 * @date: 2022年10月24日 15:13
 */
public class CandidateUsersTest {

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
                .addClasspathResource("holiday-candidateUsers.bpmn20.xml")
                .name("出差申请单-候选人")
                .deploy();
        System.out.println("Id: " + deploy.getId());
        System.out.println(deploy.getName());
    }

    @Test
    public void testProcessRun() {

        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String,Object> map = new HashMap<>();
        map.put("candidate1", "钱晓琦");
        map.put("candidate2", "徐春生");
        map.put("candidate3", "蔡屹峰");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday-candidateUsers", map);
        System.out.println("id: " + processInstance.getId());
        System.out.println("processDefinitionId: " + processInstance.getProcessDefinitionId());
        System.out.println("name: " + processInstance.getName());
    }

    /**
     * 根据登陆的用户查询可以拾取的任务
     */
    @Test
    public void queryTaskCandidateUsers() {

        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId("72501")
                .taskCandidateUser("徐春生")
                .list();
        list.forEach(task -> System.out.println(task));
    }

    /**
     * 拾取任务
     * 一个候选人拾取这个任务之后，则其他用户就没办法拾取同一个任务了
     */
    @Test
    public void testClaimTask() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("72501")
                .taskCandidateUser("徐春生")
                .singleResult();

        if (task != null) {
            //拾取对应的任务
            taskService.claim(task.getId(), "徐春生");
            System.out.println("任务拾取成功");
        }
    }

    /**
     * 退还任务
     * 如果拾取任务的人不想处理任务了，则需要将任务退还，交给其他人拾取处理
     */
    @Test
    public void testReturnTask() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("72501")
                .taskAssignee("徐春生")
                .singleResult();
        if (task != null) {
            //归还对应的任务
            taskService.unclaim(task.getId());
            System.out.println("任务归还成功");
        }
    }

    /**
     * 交接任务
     * 如果某人拾取了任务却无法处理，可以将任务交接给其他人
     */
    @Test
    public void testHandoverTask() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("72501")
                .taskAssignee("徐春生")
                .singleResult();
        if (task != null) {
            //交接对应的任务
            taskService.setAssignee(task.getId(), "李杰");
            System.out.println("任务交接成功");
        }
    }

    /**
     * 完成任务
     */
    @Test
    public void testCompleteTask() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("72501")
                .taskAssignee("李杰")
                .singleResult();
        if (task != null) {
            //完成对应的任务
            taskService.complete(task.getId());
            System.out.println("任务完成成功");
        }
    }
}
