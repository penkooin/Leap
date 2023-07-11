package org.chaostocosmos.leap.service;

import java.util.List;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.spring.entity.Users;
import org.chaostocosmos.leap.spring.repository.IUsersRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@ServiceMapper(mappingPath = "/simple/jpa")
public class SimpleJPAService extends AbstractService {

    @Autowired
    private IUsersRespository usersRepo;    

    @Autowired
    private SimpleSpringService springService;

    public SimpleJPAService() {
        System.out.println("Simple Spring service injected.............repo: "+usersRepo+"   sping serv: "+springService);
    }

    @MethodMapper(method = REQUEST.GET, mappingPath="/users")
    public void getUsers(HttpRequest request, HttpResponse response) {
        System.out.println("Simple JPA Service called.......................................");
        System.out.println(usersRepo);
        List<Users> userList = usersRepo.findByName("Tim");
        if(userList == null) {
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
