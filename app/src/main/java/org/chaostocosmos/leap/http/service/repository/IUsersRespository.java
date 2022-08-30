package org.chaostocosmos.leap.http.service.repository;

import java.util.List;

import org.chaostocosmos.leap.http.service.entity.Users;
import org.springframework.data.repository.CrudRepository;

public interface IUsersRespository extends CrudRepository<Users, Long> {

    public List<Users> findByName(String username);

    public Users findById(long id);
}
