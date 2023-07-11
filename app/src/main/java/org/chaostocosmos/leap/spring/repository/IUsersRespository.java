package org.chaostocosmos.leap.spring.repository;

import java.util.List;

import org.chaostocosmos.leap.spring.entity.Users;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;

public interface IUsersRespository extends CrudRepository<Users, Long> {

    public List<Users> findByName(String username);

    public Users findById(long id);
}
