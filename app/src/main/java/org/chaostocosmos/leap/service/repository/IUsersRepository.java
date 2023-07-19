package org.chaostocosmos.leap.service.repository;

import java.util.List;

import org.chaostocosmos.leap.enums.DATASOURCE;
import org.chaostocosmos.leap.service.datasource.LeapDataSource;
import org.chaostocosmos.leap.service.entity.Users;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * IUsersRespository
 * 
 * @author 9ins
 */
@Repository
public interface IUsersRepository extends CrudRepository<Users, Long> {
    /**
     * Find by username
     * @param dataSource
     * @param username
     * @return
     */
    public List<Users> findByName(String username);
    /**
     * Find by user id
     * @param id
     * @return
     */
    public Users findById(long id);
}
