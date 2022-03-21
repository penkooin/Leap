package org.chaostocosmos.leap.http.services.repository;

import org.chaostocosmos.leap.http.services.entity.Users;
import org.springframework.data.repository.CrudRepository;

public interface IUsersRespository extends CrudRepository<Users, Long> {

    public Users findByName(String username);

    public Users findById(long id);
}
