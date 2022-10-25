import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 候选组
 * @author: lhtao
 * @date: 2022年10月24日 16:25
 */
public class CandidateGroupsTest {

    /**
     * 加载默认名称的配置文件（flowable.cfg.xml）
     * @return
     */
    private ProcessEngine getProcessEngineByDefaultNameXml() {
        //获取ProcessEngineConfiguration对象
        return ProcessEngines.getDefaultProcessEngine();
    }

    @Test
    public void createUser() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        //通过IdentityService完成相关用户和组的管理
        IdentityService identityService = processEngine.getIdentityService();
        User 钱晓琦 = identityService.newUser("钱晓琦");
        identityService.saveUser(钱晓琦);

        User 孙宁 = identityService.newUser("孙宁");
        identityService.saveUser(孙宁);

        User 陆洪涛 = identityService.newUser("陆洪涛");
        identityService.saveUser(陆洪涛);

        User 朱小婷 = identityService.newUser("朱小婷");
        identityService.saveUser(朱小婷);
    }

    /**
     * 创建组
     */
    @Test
    public void createGroup() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        IdentityService identityService = processEngine.getIdentityService();
        Group 技术部 = identityService.newGroup("技术部");
        技术部.setName("技术部");
        技术部.setType("java");
        identityService.saveGroup(技术部);

        Group 人事部 = identityService.newGroup("人事部");
        人事部.setName("人事部");
        人事部.setType("personal");
        identityService.saveGroup(人事部);
    }

    /**
     * 将用户分配到组
     */
    @Test
    public void userGroup() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        IdentityService identityService = processEngine.getIdentityService();
        //根据组的编号找到对应的group对象
        Group 技术部 = identityService.createGroupQuery()
                .groupName("技术部")
                .singleResult();

        //查询用户并创建用户组关系
        List<User> 技术员 = identityService.createUserQuery()
                .userIds(Arrays.asList("钱晓琦", "孙宁", "陆洪涛"))
                .list();
        技术员.forEach(u -> {
            //将用户分配给对应的组
            identityService.createMembership(u.getId(), 技术部.getId());
        });

        Group 人事部 = identityService.createGroupQuery()
                .groupName("人事部")
                .singleResult();

        //查询用户并创建用户组关系
        List<User> 人事员 = identityService.createUserQuery()
                .userIds(Arrays.asList("朱小婷"))
                .list();
        人事员.forEach(u -> {
            //将用户分配给对应的组
            identityService.createMembership(u.getId(), 人事部.getId());
        });
    }

    /**
     * 部署流程
     */
    @Test
    public void testDeploy() {
        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("holiday-candidateGroups.bpmn20.xml")
                .name("出差申请单-候选人组")
                .deploy();
        System.out.println("Id: " + deploy.getId());
        System.out.println(deploy.getName());
    }

    @Test
    public void testProcessRun() {

        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String,Object> map = new HashMap<>();
        map.put("group1", "技术部");
        //map.put("group2", "人事部");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday-candidateGroups", map);
        System.out.println("id: " + processInstance.getId());
        System.out.println("processDefinitionId: " + processInstance.getProcessDefinitionId());
        System.out.println("name: " + processInstance.getName());
    }

    /**
     * 根据登陆的用户组查询可以拾取的任务
     */
    @Test
    public void queryTaskCandidateGroups() {

        ProcessEngine processEngine = getProcessEngineByDefaultNameXml();
        TaskService taskService = processEngine.getTaskService();
        IdentityService identityService = processEngine.getIdentityService();
        //根据用户名查询到对应的用户组
        Group group = identityService.createGroupQuery()
                .groupMember("钱晓琦")
                .singleResult();
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId("90001")
                .taskCandidateGroup(group.getId())
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
        IdentityService identityService = processEngine.getIdentityService();
        //根据用户名查询到对应的用户组
        Group group = identityService.createGroupQuery()
                .groupMember("钱晓琦")
                .singleResult();
        Task task = taskService.createTaskQuery()
                .processInstanceId("90001")
                .taskCandidateGroup(group.getId())
                .singleResult();

        if (task != null) {
            //拾取对应的任务
            taskService.claim(task.getId(), "钱晓琦");
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
                .processInstanceId("90001")
                .taskAssignee("钱晓琦")
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
                .processInstanceId("90001")
                .taskAssignee("钱晓琦")
                .singleResult();
        if (task != null) {
            //交接对应的任务
            taskService.setAssignee(task.getId(), "孙宁");
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
                .processInstanceId("90001")
                .taskAssignee("孙宁")
                .singleResult();
        if (task != null) {
            //完成对应的任务
            taskService.complete(task.getId());
            System.out.println("任务完成成功");
        }
    }
}
