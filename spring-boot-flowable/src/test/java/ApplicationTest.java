import com.lht.flowable.Application;
import liquibase.pro.packaged.A;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description: TODO
 * @author: lhtao
 * @date: 2022年10月25日 13:13
 */
@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class ApplicationTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 会自动部署resources/processes目录下的xml文件
     */
    @Test
    public void autoDeploy() {
        System.out.println(processEngine);
    }

    @Test
    public void deploy() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("holiday-easy.bpmn20.xml")
                .name("请假单")
                .deploy();
        System.out.println(deploy);
    }

    @Test
    public void run() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday-auto");
        System.out.println(processInstance.getProcessDefinitionId());
    }

    @Test
    public void complete() {
        Task task = taskService.createTaskQuery()
                .processDefinitionId("holiday-auto:1:de54f93f-5424-11ed-b681-84c5a65caf6d")
                .taskAssignee("孙宁")
                .singleResult();
        if (task != null) {
            taskService.complete(task.getId());
        }
    }
}
