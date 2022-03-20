package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.BasicHttpFilter;
import org.chaostocosmos.leap.http.services.entity.Users;
import org.chaostocosmos.leap.http.services.repository.IUsersRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@ServiceMapper(path = "/simple/jpa")
@Service
public class SimpleJPAService extends AbstractLeapService {

    @Autowired
    private IUsersRespository usersRepo;

    @MethodMappper(mappingMethod = REQUEST_TYPE.GET, path="/users")
    @FilterMapper(preFilters = {BasicHttpFilter.class})    
    public void getUsers(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        System.out.println("Simple JPA Service called.......................................");
        System.out.println(usersRepo);
        Users user = usersRepo.findByName("Kooin-Shin");
        System.out.println(user.toString()+" ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((");
        
    }    

    @Override
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable throwable) throws Throwable {
        return throwable;
    }    
}
