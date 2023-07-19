package org.chaostocosmos.leap.service;

import java.util.List;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.DATASOURCE;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.service.configuration.RoutingDataSource;
import org.chaostocosmos.leap.service.entity.Users;
import org.chaostocosmos.leap.service.repository.IUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceMapper(mappingPath = "/simple/jpa")
public class SimpleJPAService extends AbstractService {

    @Autowired
    private IUsersRepository usersRepo;    

    @Autowired
    private SimpleSpringService springService;

    public SimpleJPAService() {
        System.out.println("Simple Spring service injected.............repo: "+usersRepo+"   sping serv: "+springService);
    }

    @MethodMapper(method = REQUEST.GET, mappingPath="/users/oracle")    
    public void getUsers(HttpRequest request, HttpResponse response) {
        System.out.println("Simple JPA Service called.......................................");
        System.out.println(usersRepo);
        RoutingDataSource.setDataSourceKey(DATASOURCE.MYSQL);
        List<Users> userList = usersRepo.findByName("Tim");
        System.out.println(usersRepo.getClass().getName()+"********************************************");
        if(userList == null || userList.size() == 0) {
            Users users = new Users();
            users.setName("Tim");
            users.setAge(55);
            users.setAddress("Seoul, West Side");
            users.setJob("Developer");
            usersRepo.save(users);
        }
        response.setResponseCode(200);
        //response.setBody(userList.toString());
        response.setBody(springService.helloLeap());
    }    

    @Override
    public Exception errorHandling(HttpResponse response, Exception e) {
        return e;
    }    
}
