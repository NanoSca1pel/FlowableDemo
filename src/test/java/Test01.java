import com.sun.corba.se.spi.ior.ObjectKey;
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

/**
 * @Description: TODO
 * @author: lhtao
 * @date: 2022年10月18日 9:31
 */
public class Test01 {

    private ProcessEngine getProcessEngine() {
        //获取ProcessEngineConfiguration对象
        ProcessEngineConfiguration config = new StandaloneInMemProcessEngineConfiguration();

        //配置数据库的连接信息
        config.setJdbcDriver("com.mysql.cj.jdbc.Driver");
        config.setJdbcUsername("root");
        config.setJdbcPassword("123456");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/flowable?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT&nullCatalogMeansCurrent=true");
        config.setJdbcMaxActiveConnections(3);
        config.setJdbcMaxIdleConnections(1);
        /*
            #1.flase：默认值。activiti在启动时，对比数据库表中保存的版本，如果没有表或者版本不匹配，将抛出异常
            #2.true： activiti会对数据库中所有表进行更新操作。如果表不存在，则自动创建
            #3.create_drop： 在activiti启动时创建表，在关闭时删除表（必须手动关闭引擎，才能删除表）
            #4.drop-create： 在activiti启动时删除原来的旧表，然后在创建新表（不需要手动关闭引擎）
        */
        //如果数据库中的表结构不存在就新建。模式与activiti相同
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        //通过ProcessEngineConfiguration构建我们需要的processEngine对象
        ProcessEngine processEngine = config.buildProcessEngine();
        return processEngine;
    }

    @Test
    public void testProcessEngine() {
        ProcessEngine processEngine = getProcessEngine();
        System.out.println(processEngine);
    }

    /**
     * 流程部署
     */
    @Test
    public void testDeploy() {
        ProcessEngine processEngine = getProcessEngine();

        //获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .name("请假流程")
                .deploy();
        System.out.println(deployment);
    }

    /**
     * 查询流程定义信息
     */
    @Test
    public void testDeployQuery() {
        ProcessEngine processEngine = getProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId("1")
                .singleResult();
        System.out.println(deployment);
    }

    /**
     * 删除流程定义
     */
    /**
     * 查询流程定义信息
     */
    @Test
    public void testDeployDelete() {
        ProcessEngine processEngine = getProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //删除流程，第一个参数是id，如果部署的流程启动了就不允许删除了。此时如果需要删除，则需要设置成级联删除，设置第二个参数为true,相关正在执行或已执行的任务会一并删除掉
        //repositoryService.deleteDeployment("1");
        repositoryService.deleteDeployment("5001",true);
    }

    /**
     * 启动一个流程实例
     */
    @Test
    public void testProcessRun() {
        ProcessEngine processEngine = getProcessEngine();
        //获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //构建流程变量
        Map<String, Object> map = new HashMap<>();
        map.put("employee", "钱晓琦");
        map.put("holidayNums", 3);
        map.put("reason", "家中有事");
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("holidayRequest", map);
        System.out.println("id: " + holidayRequest.getId());
        System.out.println("processDefinitionId: " + holidayRequest.getProcessDefinitionId());
        System.out.println("activityId: " + holidayRequest.getActivityId());

    }

    /**
     * 任务查询
     */
    @Test
    public void testTaskQuery() {
        ProcessEngine processEngine = getProcessEngine();
        //获取TaskService对象
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("holidayRequest")
                .taskAssignee("孙宁")
                .list();

        for (Task task : list) {
            System.out.println("processDefinitionId: " + task.getProcessDefinitionId());
            System.out.println("name: " + task.getName());
            System.out.println("assignee: " + task.getAssignee());
            System.out.println("description: " + task.getDescription());
            System.out.println("id: " + task.getId());
        }
    }

    /**
     * 任务完成
     */
    @Test
    public void testTaskComplete() {
        ProcessEngine processEngine = getProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("holidayRequest")
                .taskAssignee("孙宁")
                .singleResult();
        //创建流程变量
        Map<String, Object> map = new HashMap<>();
        map.put("approved", false);

        //完成任务
        taskService.complete(task.getId(), map);
    }

    /**
     * 查询历史任务信息
     */
    @Test
    public void testHistoryQuery() {

        ProcessEngine processEngine = getProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId("12501")
                .finished() //查询历史记录的状态是已经完成的
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        list.forEach(h -> {
            System.out.println("================================");
            System.out.println("historyId: " + h.getActivityId());
            System.out.println("assignee: " + h.getAssignee());
            System.out.println("endTime: " + h.getEndTime());
        });
    }
}
