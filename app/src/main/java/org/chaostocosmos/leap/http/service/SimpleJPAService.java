package org.chaostocosmos.leap.http.service;

import java.util.List;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.AutowiredJPA;
import org.chaostocosmos.leap.http.annotation.FilterIndicates;
import org.chaostocosmos.leap.http.annotation.MethodIndicates;
import org.chaostocosmos.leap.http.annotation.ServiceIndicates;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.service.entity.Users;
import org.chaostocosmos.leap.http.service.filter.BasicHttpRequestFilter;
import org.chaostocosmos.leap.http.service.repository.IUsersRespository;
import org.springframework.stereotype.Service;

@ServiceIndicates(path = "/simple/jpa")
@Service
public class SimpleJPAService extends AbstractService {

    @AutowiredJPA
    private IUsersRespository usersRepo;

    @AutowiredJPA
    private SimpleSpringService springService;

    @MethodIndicates(mappingMethod = REQUEST_TYPE.GET, path="/users")
    @FilterIndicates(preFilters = {BasicHttpRequestFilter.class})    
    public void getUsers(Request request, Response response) {
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
        System.out.println(userList.toString()+" ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((");
        response.setResponseCode(200);
        //response.setBody(userList.toString());
        response.setBody(springService.helloLeap());
    }    

    @Override
    public Exception errorHandling(Response response, Exception e) {
        return e;
    }    
}
