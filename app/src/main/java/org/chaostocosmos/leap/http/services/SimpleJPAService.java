package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.AutowiredJPA;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.BasicHttpFilter;
import org.chaostocosmos.leap.http.services.entity.Users;
import org.chaostocosmos.leap.http.services.repository.IUsersRespository;
import org.springframework.stereotype.Service;

@ServiceMapper(path = "/simple/jpa")
@Service
public class SimpleJPAService extends AbstractLeapService {

    @AutowiredJPA
    private IUsersRespository usersRepo;

    @MethodMappper(mappingMethod = REQUEST_TYPE.GET, path="/users")
    @FilterMapper(preFilters = {BasicHttpFilter.class})    
    public void getUsers(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        System.out.println("Simple JPA Service called.......................................");
        System.out.println(usersRepo);        
        Users users = usersRepo.findByName("Tim");
        if(users == null) {
            users = new Users();
            users.setName("Tim");
            users.setAge(55);
            users.setAddress("Seoul, West Side");
            users.setJob("Developer");    
            usersRepo.save(users);
        }
        Users user = usersRepo.findByName("Tim");
        System.out.println(user.toString()+" ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((");
        response.setStatusCode(200);
        response.setBody(user.toString());
    }    

    @Override
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable throwable) throws Throwable {
        return throwable;
    }    
}
